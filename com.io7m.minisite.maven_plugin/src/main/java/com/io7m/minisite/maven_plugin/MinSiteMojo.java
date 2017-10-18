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

import com.io7m.changelog.core.CChangelog;
import com.io7m.changelog.xom.CAtomFeedMeta;
import com.io7m.changelog.xom.CChangelogAtomWriter;
import com.io7m.changelog.xom.CChangelogXMLReader;
import com.io7m.minisite.core.MinBugTrackerConfiguration;
import com.io7m.minisite.core.MinChangesConfiguration;
import com.io7m.minisite.core.MinConfiguration;
import com.io7m.minisite.core.MinSite;
import com.io7m.minisite.core.MinSourcesConfiguration;
import com.io7m.minisite.core.MinXHTMLReindent;
import io.vavr.collection.Vector;
import nu.xom.DocType;
import nu.xom.Document;
import nu.xom.ParsingException;
import nu.xom.Serializer;
import org.apache.maven.model.IssueManagement;
import org.apache.maven.model.License;
import org.apache.maven.model.Scm;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.Optional;

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

  @Parameter(name = "skip", property = "minisite.skip", required = false)
  private boolean skip;

  /**
   * Access to the Maven project.
   */

  @Parameter(defaultValue = "${project}", required = true, readonly = true)
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
   * The output directory.
   */

  @Parameter(
    name = "outputDirectory",
    property = "minisite.outputDirectory",
    defaultValue = "${project.build.directory}/minisite",
    required = false)
  private String outputDirectory;

  @Override
  public void execute()
    throws MojoExecutionException, MojoFailureException
  {
    if (this.skip) {
      return;
    }

    final Log log = this.getLog();
    log.debug("Generating site...");

    final MinConfiguration config =
      MinConfiguration.builder()
        .setProjectName(this.project.getName())
        .setProjectGroupName(this.project.getGroupId())
        .setProjectModules(this.modules())
        .setRelease(this.project.getVersion())
        .setSources(this.sources())
        .setLicense(this.license())
        .setBugTracker(this.bugTracker())
        .setOverview(this.overview())
        .setFeatures(this.features())
        .setChangelog(this.changelog())
        .setCentralReposPath(this.project.getGroupId().replace(".", "/"))
        .build();

    final MinSite site = MinSite.create(config);
    final Path directory = Paths.get(this.outputDirectory);

    try {
      Files.createDirectories(directory);

      final Path file_output = directory.resolve("index.xhtml");
      try (final OutputStream output = Files.newOutputStream(file_output)) {
        final Document doc = new Document(site.document());
        doc.setDocType(new DocType(
          "html",
          "-//W3C//DTD XHTML 1.0 Strict//EN",
          "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"));
        final Serializer serial = new Serializer(output, "UTF-8");
        serial.write(doc);
        serial.flush();
      }

      final Path file_tmp = directory.resolve("index.xhtml.tmp");
      MinXHTMLReindent.indent(file_output, file_tmp, file_output);

    } catch (final IOException
      | TransformerException
      | ClassNotFoundException
      | SAXException
      | IllegalAccessException
      | ParserConfigurationException
      | InstantiationException e) {
      throw new MojoFailureException(e.getMessage(), e);
    }

    config.changelog().ifPresent(changes_config -> {
      try (final InputStream input =
             Files.newInputStream(changes_config.file())) {
        final CChangelog changelog =
          CChangelogXMLReader.readFromStream(
            changes_config.file().toUri(), input);

        final CAtomFeedMeta meta =
          CAtomFeedMeta.builder()
            .setAuthorEmail(changes_config.feedEmail())
            .setAuthorName("minisite")
            .setTitle(config.projectName() + " Releases")
            .setUri(URI.create(this.project.getUrl() + "/releases.atom"))
            .build();

        final Path releases = directory.resolve("releases.atom");
        try (final OutputStream output = Files.newOutputStream(releases)) {
          final Serializer serial = new Serializer(output, "UTF-8");
          serial.setIndent(2);
          serial.setMaxLength(72);
          serial.write(new Document(CChangelogAtomWriter.writeElement(meta, changelog)));
          serial.flush();
        }
      } catch (final IOException e) {
        throw new UncheckedIOException(e);
      } catch (URISyntaxException
        | ParseException
        | ParsingException
        | SAXException
        | ParserConfigurationException e) {
        throw new UncheckedIOException(new IOException(e));
      }
    });
  }

  private Vector<String> modules()
  {
    /// XXX: Possibly incorrect assumptions:
    /// 1. Module names match their artifact IDs.
    /// 2. Module groupIds match that of their parent module.
    /// 3. Modules don't have child modules.
    return Vector.ofAll(this.project.getModules());
  }

  private Optional<Path> features()
  {
    if (this.featuresFile != null) {
      return Optional.of(
        this.project.getBasedir().toPath().resolve(this.featuresFile));
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

  private Optional<Path> license()
  {
    return this.project.getLicenses().stream().findFirst().map(license -> {
      try {
        final URL url = this.transformURIToPath(license);
        final Path path = Files.createTempFile("minisite-", ".txt");
        try (final OutputStream out = Files.newOutputStream(path)) {

          try (final InputStream input = url.openStream()) {
            final byte[] buffer = new byte[1024];
            while (true) {
              final int r = input.read(buffer);
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

  private URL transformURIToPath(
    final License license)
    throws MalformedURLException
  {
    final Log log = this.getLog();
    final URI uri = URI.create(license.getUrl());
    if (uri.getScheme() == null && uri.getPath() != null) {
      log.debug(
        "License URI has no scheme and no path; transforming to local file path");
      final String result =
        "file:///" + new File(this.project.getBasedir(), uri.getPath());
      log.debug("License URL: " + result);
      return new URL(result);
    }
    return uri.toURL();
  }

  private Optional<MinBugTrackerConfiguration> bugTracker()
  {
    final IssueManagement issues = this.project.getIssueManagement();
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
    final Log log = this.getLog();
    final Scm scm = this.project.getScm();
    if (scm != null) {
      final String connection = scm.getConnection();
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
