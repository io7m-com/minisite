/*
 * Copyright © 2017 <code@io7m.com> http://io7m.com
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package com.io7m.minisite.maven_plugin;

import com.io7m.changelog.xml.api.CAtomChangelogWriterConfiguration;
import com.io7m.changelog.xml.api.CAtomChangelogWriterProviderType;
import com.io7m.changelog.xml.api.CXMLChangelogParserProviderType;
import com.io7m.minisite.core.MinBugTrackerConfiguration;
import com.io7m.minisite.core.MinChangesConfiguration;
import com.io7m.minisite.core.MinConfiguration;
import com.io7m.minisite.core.MinSite;
import com.io7m.minisite.core.MinSourcesConfiguration;
import org.apache.maven.model.License;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;

// CHECKSTYLE:OFF

/**
 * The main site generation mojo.
 */

@Mojo(
  name = "generateSite",
  defaultPhase = LifecyclePhase.SITE)
public final class MinSiteMojo extends AbstractMojo
{
  /**
   * Instantiate the mojo.
   */

  public MinSiteMojo()
  {
    this.skip = false;
  }

  /**
   * Parameter to allow skipping of the generation.
   */

  @Parameter(
    name = "skip",
    property = "minisite.skip",
    required = false)
  private boolean skip;

  /**
   * Access to the Maven project.
   */

  @Parameter(
    defaultValue = "${project}",
    required = true,
    readonly = true)
  private MavenProject project;

  /**
   * The overview file.
   */

  @Parameter(
    name = "overviewFile",
    property = "minisite.overviewFile",
    required = false)
  private String overviewFile;

  /**
   * The features file.
   */

  @Parameter(
    name = "featuresFile",
    property = "minisite.featuresFile",
    required = false)
  private String featuresFile;

  /**
   * The header file.
   */

  @Parameter(
    name = "headerFile",
    property = "minisite.headerFile",
    required = false)
  private String headerFile;

  /**
   * The documentation configuration.
   */

  @Parameter(
    name = "documentationFile",
    property = "minisite.documentationFile",
    required = false)
  private String documentationFile;

  /**
   * The changelog file.
   */

  @Parameter(
    name = "changelogFile",
    property = "minisite.changelogFile",
    required = false)
  private String changelogFile;

  /**
   * The changelog feed email.
   */

  @Parameter(
    name = "changelogFeedEmail",
    property = "minisite.changelogFeedEmail",
    defaultValue = "nobody@example.com",
    required = false)
  private String changelogFeedEmail;

  /**
   * The resources directory.
   */

  @Parameter(
    name = "resourceDirectory",
    property = "minisite.resourceDirectory",
    defaultValue = "${project.basedir}/src/site/resources",
    required = false)
  private String resourceDirectory;

  /**
   * The output directory.
   */

  @Parameter(
    name = "outputDirectory",
    property = "minisite.outputDirectory",
    defaultValue = "${project.build.directory}/minisite",
    required = false)
  private String outputDirectory;

  /**
   * The CSS styles that will be included.
   */

  @Parameter(
    name = "cssStyles",
    required = false)
  private String[] cssStyles = {
    "minisite.css",
    "site.css"
  };

  /**
   * A specification of whether or not the default CSS style should be copied
   * to the generated site.
   */

  @Parameter(
    name = "cssGenerateDefault",
    defaultValue = "true",
    required = false)
  private boolean cssGenerateDefault;

  /**
   * The current Maven settings.
   */

  @Parameter(
    defaultValue = "${settings}",
    readonly = true,
    required = true)
  private Settings settings;

  @Override
  public void execute()
    throws MojoFailureException
  {
    if (this.skip) {
      return;
    }

    final var log = this.getLog();
    log.debug("Generating site...");

    final var config =
      MinConfiguration.builder()
        .setProjectName(this.project.getName())
        .setProjectGroupName(this.project.getGroupId())
        .setProjectModules(this.modules())
        .setDocumentation(this.documentation())
        .setRelease(this.project.getVersion())
        .setSources(this.sources())
        .setHeader(this.header())
        .setLicense(this.license(log))
        .setBugTracker(this.bugTracker())
        .setOverview(this.overview())
        .setFeatures(this.features())
        .setChangelog(this.changelog())
        .setCssGenerateStyle(this.cssGenerateDefault)
        .setCssIncludes(List.of(this.cssStyles))
        .setCentralReposPath(this.project.getGroupId().replace(".", "/"))
        .build();

    final var site = MinSite.create(config);
    final var directory = Paths.get(this.outputDirectory);

    try {
      Files.createDirectories(directory);

      final var fileOutput =
        directory.resolve("index.xhtml");
      final var docBuilderFactory =
        DocumentBuilderFactory.newInstance();
      final var docBuilder =
        docBuilderFactory.newDocumentBuilder();
      final var document =
        docBuilder.newDocument();

      document.appendChild(site.document(document));

      final var source = new DOMSource(document);
      try (var output = Files.newBufferedWriter(fileOutput)) {
        final var result = new StreamResult(output);
        final var transformerFactory = TransformerFactory.newInstance();
        final var transformer = transformerFactory.newTransformer();
        transformer.transform(source, result);
        output.flush();
      }
    } catch (final UncheckedIOException e) {
      throw new MojoFailureException(e.getCause().getMessage(), e.getCause());
    } catch (final Exception e) {
      throw new MojoFailureException(e.getMessage(), e);
    }

    try {
      if (config.cssGenerateStyle()) {
        final var file_output = directory.resolve("minisite.css");
        try (final var out = Files.newOutputStream(file_output)) {
          try (final var in =
                 MinSite.class.getResourceAsStream("minisite.css")) {
            in.transferTo(out);
          }
        }
      }
    } catch (final UncheckedIOException e) {
      throw new MojoFailureException(e.getCause().getMessage(), e.getCause());
    } catch (final Exception e) {
      throw new MojoFailureException(e.getMessage(), e);
    }

    try {
      config.changelog().ifPresent(changes_config -> {
        final var parser_provider_opt =
          ServiceLoader.load(CXMLChangelogParserProviderType.class).findFirst();

        if (!parser_provider_opt.isPresent()) {
          throw new NoSuchElementException(
            "No XML changelog parser providers are available");
        }

        final var writer_provider_opt =
          ServiceLoader.load(CAtomChangelogWriterProviderType.class).findFirst();

        if (!writer_provider_opt.isPresent()) {
          throw new NoSuchElementException(
            "No Atom changelog writer providers are available");
        }

        final var parser_provider =
          parser_provider_opt.get();
        final var writer_provider =
          writer_provider_opt.get();

        try (final var input =
               Files.newInputStream(changes_config.file())) {

          final var parser =
            parser_provider.create(
              changes_config.file().toUri(),
              input,
              ErrorHandlers.loggingHandler(log));

          final var changelog = parser.parse();

          final var meta =
            CAtomChangelogWriterConfiguration.builder()
              .setAuthorEmail(changes_config.feedEmail())
              .setAuthorName("minisite")
              .setUpdated(ZonedDateTime.now(ZoneId.of("UTC")))
              .setTitle(config.projectName() + " Releases")
              .setUri(URI.create(this.project.getUrl() + "/releases.atom"))
              .build();

          final var releases = directory.resolve("releases.atom");
          try (final var output = Files.newOutputStream(releases)) {
            final var writer =
              writer_provider.createWithConfiguration(
                meta,
                changes_config.file().toUri(),
                output);
            writer.write(changelog);
          }
        } catch (final IOException e) {
          throw new UncheckedIOException(e);
        }
      });
    } catch (final UncheckedIOException e) {
      throw new MojoFailureException(e.getCause().getMessage(), e.getCause());
    }

    if (this.resourceDirectory != null) {
      log.debug("copying resources");
      try {
        Files.walkFileTree(
          Paths.get(this.resourceDirectory),
          new CopyTreeVisitor(Paths.get(this.outputDirectory)));
      } catch (final IOException e) {
        throw new MojoFailureException(e.getMessage(), e);
      }
    }
  }

  private Optional<Path> documentation()
  {
    if (this.documentationFile != null) {
      return Optional.of(
        this.project.getBasedir().toPath().resolve(this.documentationFile));
    }
    return Optional.empty();
  }

  private List<String> modules()
  {
    /// XXX: Possibly incorrect assumptions:
    /// 1. Module names match their artifact IDs.
    /// 2. Module groupIds match that of their parent module.
    /// 3. Modules don't have child modules.
    return this.project.getModules();
  }

  private Optional<Path> features()
  {
    if (this.featuresFile != null) {
      return Optional.of(
        this.project.getBasedir().toPath().resolve(this.featuresFile));
    }
    return Optional.empty();
  }

  private Optional<Path> header()
  {
    if (this.headerFile != null) {
      return Optional.of(
        this.project.getBasedir().toPath().resolve(this.headerFile));
    }
    return Optional.empty();
  }

  private Optional<Path> overview()
  {
    if (this.overviewFile != null) {
      return Optional.of(
        this.project.getBasedir().toPath().resolve(this.overviewFile));
    }
    return Optional.empty();
  }

  private Optional<MinChangesConfiguration> changelog()
  {
    if (this.changelogFile != null) {
      return Optional.of(
        MinChangesConfiguration.builder()
          .setFeedEmail(this.changelogFeedEmail)
          .setFile(this.project.getBasedir().toPath().resolve(this.changelogFile))
          .build());
    }
    return Optional.empty();
  }

  private Optional<Path> license(final Log log)
  {
    return this.project.getLicenses().stream().findFirst().map(license -> {
      try {
        final var url = this.transformURIToPath(license);

        final var net_proxy = configureProxyForURL(log, this.settings, url);
        final var path = Files.createTempFile("minisite-", ".txt");
        try (final var out = Files.newOutputStream(path)) {
          final var conn = url.openConnection(net_proxy);
          conn.connect();

          try (final var input = conn.getInputStream()) {
            final var buffer = new byte[1024];
            while (true) {
              final var r = input.read(buffer);
              if (r <= 0) {
                break;
              }
              out.write(buffer, 0, r);
            }
          }
        }
        return path;
      } catch (final IOException e) {
        throw new UncheckedIOException(e);
      }
    });
  }

  private static Proxy configureProxyForURL(
    final Log log,
    final Settings settings,
    final URL url)
  {
    final var target_protocol = url.getProtocol();
    for (final var maven_proxy : settings.getProxies()) {

      /*
       * Ignore inactive proxies.
       */

      if (!maven_proxy.isActive()) {
        log.debug(new StringBuilder(32)
                    .append("proxy ")
                    .append(maven_proxy.getId())
                    .append(" is not active, ignoring")
                    .toString());
        continue;
      }

      /*
       * Try to find a proxy with the right protocol.
       */

      final var proxy_protocol = maven_proxy.getProtocol();
      if (!Objects.equals(proxy_protocol, target_protocol)) {
        log.debug(new StringBuilder(32)
                    .append("proxy ")
                    .append(maven_proxy.getId())
                    .append(" protocol ")
                    .append(proxy_protocol)
                    .append(" does not match target protocol ")
                    .append(target_protocol)
                    .append(", ignoring")
                    .toString());
        continue;
      }

      /*
       * Assume an HTTP(s) proxy without authentication.
       * XXX: Add authentication!
       */

      if ("http".equalsIgnoreCase(target_protocol)
        || "https".equalsIgnoreCase(target_protocol)) {

        log.info(new StringBuilder(32)
                   .append("using proxy ")
                   .append(maven_proxy.getId())
                   .append(" as http/https proxy")
                   .toString());
        return new Proxy(
          Proxy.Type.HTTP,
          InetSocketAddress.createUnresolved(
            maven_proxy.getHost(),
            maven_proxy.getPort()));
      }

      log.warn("do not know how to configure a non-http(s) proxy");
    }

    log.debug("no usable proxy found");
    return Proxy.NO_PROXY;
  }

  private URL transformURIToPath(
    final License license)
    throws MalformedURLException
  {
    final var log = this.getLog();
    final var uri = URI.create(license.getUrl());
    if (uri.getScheme() == null && uri.getPath() != null) {
      log.debug(
        "License URI has no scheme and no path; transforming to local file path");
      final var result =
        "file:///" + new File(this.project.getBasedir(), uri.getPath());
      log.debug("License URL: " + result);
      return new URL(result);
    }
    return uri.toURL();
  }

  private Optional<MinBugTrackerConfiguration> bugTracker()
  {
    final var issues = this.project.getIssueManagement();
    if (issues != null) {
      return Optional.of(
        MinBugTrackerConfiguration.builder()
          .setUri(URI.create(issues.getUrl()))
          .setSystem(issues.getSystem())
          .build());
    }

    return Optional.empty();
  }

  private Optional<MinSourcesConfiguration> sources()
  {
    final var log = this.getLog();
    final var scm = this.project.getScm();
    if (scm != null) {
      final var connection = scm.getConnection();
      if (connection.startsWith("scm:git")) {
        return Optional.of(
          MinSourcesConfiguration.of(
            "Git", URI.create(scm.getUrl())));
      }

      log.error("Unrecognized SCM type: " + connection);
      return Optional.empty();
    }

    return Optional.empty();
  }
}
