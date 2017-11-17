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

package com.io7m.minisite.core;

import com.io7m.changelog.core.CChangelog;
import com.io7m.changelog.parser.api.CParseErrorHandlers;
import com.io7m.changelog.xml.api.CXHTMLChangelogWriterProviderType;
import com.io7m.changelog.xml.api.CXHTMLChangelogWriterType;
import com.io7m.changelog.xml.api.CXMLChangelogParserProviderType;
import com.io7m.changelog.xml.api.CXMLChangelogParserType;
import nu.xom.Attribute;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.ParsingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;

/**
 * A site generator.
 */

public final class MinSite
{
  private static final Logger LOG =
    LoggerFactory.getLogger(MinSite.class);

  private final MinConfiguration config;

  private MinSite(
    final MinConfiguration in_config)
  {
    this.config = Objects.requireNonNull(in_config, "Configuration");
  }

  /**
   * Create a site generator for the given config.
   *
   * @param config The config
   *
   * @return A site generator
   */

  public static MinSite create(
    final MinConfiguration config)
  {
    Objects.requireNonNull(config, "Configuration");
    return new MinSite(config);
  }

  private static Element sources(
    final MinSourcesConfiguration sources)
  {
    final ServiceLoader<MinSourcesProviderType> loader =
      ServiceLoader.load(MinSourcesProviderType.class);

    final Iterator<MinSourcesProviderType> iter = loader.iterator();
    while (iter.hasNext()) {
      final MinSourcesProviderType provider = iter.next();
      if (Objects.equals(provider.system(), sources.system())) {
        return provider.evaluate(sources);
      }
    }

    throw new NoSuchElementException(
      "No providers are available for source repositories of type: " + sources.system());
  }

  private static Element changelog(
    final MinChangesConfiguration changes_config)
  {
    final Optional<CXMLChangelogParserProviderType> parser_provider_opt =
      ServiceLoader.load(CXMLChangelogParserProviderType.class).findFirst();

    if (!parser_provider_opt.isPresent()) {
      throw new NoSuchElementException(
        "No XML changelog parser providers are available");
    }

    final Optional<CXHTMLChangelogWriterProviderType> writer_provider_opt =
      ServiceLoader.load(CXHTMLChangelogWriterProviderType.class).findFirst();

    if (!writer_provider_opt.isPresent()) {
      throw new NoSuchElementException(
        "No XHTML changelog writer providers are available");
    }

    final CXMLChangelogParserProviderType parser_provider =
      parser_provider_opt.get();

    try (InputStream input = Files.newInputStream(changes_config.file())) {
      final CXMLChangelogParserType parser =
        parser_provider.create(
          changes_config.file().toUri(),
          input,
          CParseErrorHandlers.loggingHandler(LOG));

      final CChangelog changelog = parser.parse();

      final Element changes = new Element("div", MinXHTML.XHTML);
      changes.addAttribute(new Attribute("id", "changes"));
      changes.appendChild(MinXHTML.h2("Changes"));

      {
        final Element p = new Element("p", MinXHTML.XHTML);
        p.appendChild("Subscribe to the releases ");
        p.appendChild(MinXHTML.link("releases.atom", "atom feed"));
        p.appendChild(".");
        changes.appendChild(p);
      }

      if (changelog.releases().isEmpty()) {
        final Element p = new Element("p", MinXHTML.XHTML);
        p.appendChild("No formal releases have been made.");
        changes.appendChild(p);
      } else {
        changes.appendChild(
          serializeChangelog(writer_provider_opt.get(), changelog).copy());
      }

      return changes;
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    } catch (final ParsingException e) {
      throw new UncheckedIOException(new IOException(e));
    }
  }

  private static Element serializeChangelog(
    final CXHTMLChangelogWriterProviderType writer_provider,
    final CChangelog changelog)
    throws IOException, ParsingException
  {
    try (ByteArrayOutputStream bao = new ByteArrayOutputStream()) {
      final CXHTMLChangelogWriterType writer =
        writer_provider.create(URI.create("urn:stdout"), bao);
      writer.write(changelog);
      final Builder b = new Builder(false);
      try (ByteArrayInputStream bai = new ByteArrayInputStream(bao.toByteArray())) {
        final Document doc = b.build(bai);
        return doc.getRootElement();
      }
    }
  }

  private static Element features(
    final Path path)
  {
    final Element features = new Element("div", MinXHTML.XHTML);
    features.addAttribute(new Attribute("id", "features"));
    features.appendChild(MinXHTML.h2("Features"));

    {
      final Builder b = new Builder();
      try (InputStream stream = Files.newInputStream(path.toAbsolutePath())) {
        final Document doc = b.build(stream);
        features.appendChild(doc.getRootElement().copy());
      } catch (final IOException e) {
        throw new UncheckedIOException(e);
      } catch (final ParsingException e) {
        throw new UncheckedIOException(new IOException(e));
      }
    }

    return features;
  }

  private static Element bugTracker(
    final MinBugTrackerConfiguration tracker)
  {
    final Element container = new Element("div", MinXHTML.XHTML);
    container.addAttribute(new Attribute("id", "bug-tracker"));
    container.appendChild(MinXHTML.h2("Bug Tracker"));

    {
      final Element p = new Element("p", MinXHTML.XHTML);
      p.appendChild("The project uses ");
      p.appendChild(MinXHTML.link(tracker.uri().toString(), tracker.system()));
      p.appendChild(" to track issues.");
      container.appendChild(p);
    }

    return container;
  }

  private static Element license(
    final Path path)
  {
    final Element license = new Element("div", MinXHTML.XHTML);
    license.addAttribute(new Attribute("id", "license"));
    license.appendChild(MinXHTML.h2("License"));

    final Element pre = new Element("pre", MinXHTML.XHTML);
    try {
      pre.appendChild(new String(
        Files.readAllBytes(path),
        StandardCharsets.UTF_8));
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }

    license.appendChild(pre);
    return license;
  }

  private static String cleanReposPath(
    final String path)
  {
    return path.replaceAll("^[/]+", "");
  }

  private static Element contentsLicenseLink()
  {
    return MinXHTML.link("#license", "License");
  }

  private static Element contentsSourcesLink()
  {
    return MinXHTML.link("#sources", "Sources");
  }

  private static Element contentsChangesLink()
  {
    return MinXHTML.link("#changes", "Changes");
  }

  private static Element contentsDocumentationLink()
  {
    return MinXHTML.link("#documentation", "Documentation");
  }

  private static Element contentsReleasesLink()
  {
    return MinXHTML.link("#releases", "Releases");
  }

  private static Element contentsBugTrackerLink()
  {
    return MinXHTML.link("#bug-tracker", "Bug Tracker");
  }

  private static Element contentsFeaturesLink()
  {
    return MinXHTML.link("#features", "Features");
  }

  private static Element contentsMavenLink()
  {
    return MinXHTML.link("#maven", "Maven");
  }

  private static Element css()
  {
    final Element style = new Element("style", MinXHTML.XHTML);
    style.addAttribute(new Attribute("type", "text/css"));

    try (InputStream stream = MinSite.class.getResourceAsStream(
      "style.css")) {
      try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
        final byte[] buffer = new byte[1024];
        while (true) {
          final int r = stream.read(buffer);
          if (r <= 0) {
            break;
          }
          out.write(buffer, 0, r);
        }
        style.appendChild(new String(
          out.toByteArray(),
          StandardCharsets.UTF_8));
      }
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }

    return style;
  }

  private static Element metaGenerator()
  {
    final String content =
      new StringBuilder(128)
        .append("https://www.github.com/io7m/minisite; ")
        .append(version())
        .toString();

    final Element meta = new Element("meta", MinXHTML.XHTML);
    meta.addAttribute(new Attribute("name", "generator"));
    meta.addAttribute(new Attribute("content", content));
    return meta;
  }

  private static Element metaType()
  {
    final Element meta = new Element("meta", MinXHTML.XHTML);
    meta.addAttribute(
      new Attribute("http-equiv", "Content-Type"));
    meta.addAttribute(
      new Attribute("content", "application/xhtml+xml; charset=UTF-8"));
    return meta;
  }

  private static String version()
  {
    final String version = MinSite.class.getPackage().getImplementationVersion();
    return version == null ? "UNKNOWN" : version;
  }

  /**
   * Generate a site.
   *
   * @return A generated page
   */

  public Element document()
  {
    final Element head = this.head();
    final Element body = this.body();

    final Element xhtml = new Element("html", MinXHTML.XHTML);
    xhtml.appendChild(head);
    xhtml.appendChild(body);
    return xhtml;
  }

  private Element body()
  {
    final Element body = new Element("body", MinXHTML.XHTML);
    final Element main = this.main();

    body.appendChild(main);
    return body;
  }

  private Element main()
  {
    final Element main = new Element("div", MinXHTML.XHTML);
    main.addAttribute(new Attribute("id", "main"));
    main.appendChild(this.overview());
    main.appendChild(this.contents());

    this.config.features().ifPresent(
      path -> main.appendChild(features(path)));

    main.appendChild(this.releases());

    if (!this.config.documentation().isEmpty()) {
      main.appendChild(this.documentation());
    }

    main.appendChild(this.maven());

    this.config.changelog().ifPresent(
      changelog -> main.appendChild(changelog(changelog)));
    this.config.sources().ifPresent(
      sources -> main.appendChild(sources(sources)));
    this.config.license().ifPresent(
      path -> main.appendChild(license(path)));
    this.config.bugTracker().ifPresent(
      tracker -> main.appendChild(bugTracker(tracker)));

    return main;
  }

  private Element maven()
  {
    final Element maven = new Element("div", MinXHTML.XHTML);
    maven.addAttribute(new Attribute("id", "maven"));
    maven.appendChild(MinXHTML.h2("Maven"));

    {
      final Element p = new Element("p", MinXHTML.XHTML);
      p.appendChild(
        "The following is a complete list of the project's modules expressed as Maven dependencies: ");
      maven.appendChild(p);
    }

    {
      final Element pre = new Element("pre", MinXHTML.XHTML);
      final String group = this.config.projectGroupName();
      final String version = this.config.release();
      mavenDependency(pre, this.config.projectName(), group, version);
      for (final String module : this.config.projectModules()) {
        mavenDependency(pre, module, group, version);
      }
      maven.appendChild(pre);
    }

    {
      final Element p = new Element("p", MinXHTML.XHTML);
      p.appendChild("Each release of the project is made available on ");
      p.appendChild(MinXHTML.link("http://search.maven.org", "Maven Central"));
      p.appendChild(" within ten minutes of the release announcement.");
      maven.appendChild(p);
    }

    return maven;
  }

  private static void mavenDependency(
    final Element pre,
    final String module,
    final String group,
    final String version)
  {
    final String link_group =
      new StringBuilder(64)
        .append("http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22")
        .append(group)
        .append("%22")
        .toString();

    final String link_artifact =
      new StringBuilder(64)
        .append("http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22")
        .append(module)
        .append("%22")
        .toString();

    final String link_version =
      new StringBuilder(64)
        .append("http://search.maven.org/#artifactdetails%7C")
        .append(group)
        .append("%7C")
        .append(module)
        .append("%7C")
        .append(version)
        .append("%7Cjar")
        .toString();

    pre.appendChild(
      new StringBuilder(64)
        .append("<dependency>")
        .append(System.lineSeparator())
        .append("  <groupId>")
        .toString());

    pre.appendChild(MinXHTML.link(link_group, group));

    pre.appendChild(
      new StringBuilder(64)
        .append("</groupId>")
        .append(System.lineSeparator())
        .append("  <artifactId>")
        .toString());

    pre.appendChild(MinXHTML.link(link_artifact, module));

    pre.appendChild(
      new StringBuilder(64)
        .append("</artifactId>")
        .append(System.lineSeparator())
        .append("  <version>")
        .toString());

    pre.appendChild(MinXHTML.link(link_version, version));

    pre.appendChild(
      new StringBuilder(64)
        .append("</version>")
        .append(System.lineSeparator())
        .append("</dependency>")
        .append(System.lineSeparator())
        .append(System.lineSeparator())
        .toString());
  }

  private Element documentation()
  {
    final Element documentation = new Element("div", MinXHTML.XHTML);
    documentation.addAttribute(new Attribute("id", "documentation"));
    documentation.appendChild(MinXHTML.h2("Documentation"));

    {
      final Element p = new Element("p", MinXHTML.XHTML);
      p.appendChild("Documentation for the ");
      final Element tt = new Element("tt", MinXHTML.XHTML);
      tt.appendChild(this.config.release());
      p.appendChild(tt);
      p.appendChild(" release is available for reading online.");
      documentation.appendChild(p);
    }

    {
      final Element p = new Element("p", MinXHTML.XHTML);
      p.appendChild(
        "Documentation for current and older releases is archived in the ");
      p.appendChild(MinXHTML.link(this.centralRepos(), "repository"));
      p.appendChild(".");
      documentation.appendChild(p);
    }

    this.config.documentation().forEach(item -> {
      final Element h3 = new Element("h3", MinXHTML.XHTML);
      h3.appendChild(item.name());
      documentation.appendChild(h3);
      final Element ul = new Element("ul", MinXHTML.XHTML);
      item.formats().forEach(format -> {
        ul.appendChild(MinXHTML.listItem(
          MinXHTML.link(format.path().toString(), format.name())));
      });
      documentation.appendChild(ul);
    });

    return documentation;
  }

  private Element releases()
  {
    final Element releases = new Element("div", MinXHTML.XHTML);
    releases.addAttribute(new Attribute("id", "releases"));
    releases.appendChild(MinXHTML.h2("Releases"));

    {
      final Element p = new Element("p", MinXHTML.XHTML);
      p.appendChild("The current release is ");
      final Element tt = new Element("tt", MinXHTML.XHTML);
      tt.appendChild(this.config.release());
      p.appendChild(tt);
      p.appendChild(".");
      releases.appendChild(p);
    }

    {
      final Element p = new Element("p", MinXHTML.XHTML);
      p.appendChild("Source code and binaries are available from the ");
      p.appendChild(MinXHTML.link(this.centralRepos(), "repository"));
      p.appendChild(".");
      releases.appendChild(p);
    }

    return releases;
  }

  private String centralRepos()
  {
    return new StringBuilder(64)
      .append("https://repo1.maven.org/maven2/")
      .append(cleanReposPath(this.config.centralReposPath()))
      .toString();
  }

  private Element overview()
  {
    final Element overview = new Element("div", MinXHTML.XHTML);
    overview.addAttribute(new Attribute("id", "overview"));
    overview.appendChild(this.overviewTitleArea());
    overview.appendChild(this.overviewContentArea());
    return overview;
  }

  private Element overviewTitleArea()
  {
    final Element area = new Element("div", MinXHTML.XHTML);
    area.addAttribute(new Attribute("class", "overview_title_area"));

    final Element img = new Element("img", MinXHTML.XHTML);
    img.addAttribute(new Attribute("src", "icon.png"));
    img.addAttribute(new Attribute("width", "64"));
    img.addAttribute(new Attribute("height", "64"));
    img.addAttribute(new Attribute("class", "icon"));
    img.addAttribute(new Attribute("alt", "Project icon"));

    final Element title = new Element("h1", MinXHTML.XHTML);
    title.appendChild(this.config.projectName());

    area.appendChild(img);
    area.appendChild(title);
    return area;
  }

  private Element overviewContentArea()
  {
    final Element area = new Element("div", MinXHTML.XHTML);
    area.addAttribute(new Attribute("class", "overview_content_area"));

    this.config.overview().ifPresent(path -> {
      final Builder b = new Builder();
      try (InputStream stream = Files.newInputStream(path.toAbsolutePath())) {
        final Document doc = b.build(stream);
        area.appendChild(doc.getRootElement().copy());
      } catch (final IOException e) {
        throw new UncheckedIOException(e);
      } catch (final ParsingException e) {
        throw new UncheckedIOException(new IOException(e));
      }
    });

    return area;
  }

  private Element contents()
  {
    final Element area = new Element("div", MinXHTML.XHTML);
    area.appendChild(MinXHTML.h2("Contents"));

    final Element contents = new Element("ul", MinXHTML.XHTML);

    this.config.features().ifPresent(
      path -> contents.appendChild(MinXHTML.listItem(contentsFeaturesLink())));
    contents.appendChild(MinXHTML.listItem(contentsReleasesLink()));

    if (!this.config.documentation().isEmpty()) {
      contents.appendChild(MinXHTML.listItem(contentsDocumentationLink()));
    }

    contents.appendChild(MinXHTML.listItem(contentsMavenLink()));
    this.config.changelog().ifPresent(
      path -> contents.appendChild(MinXHTML.listItem(contentsChangesLink())));
    this.config.sources().ifPresent(
      sources -> contents.appendChild(MinXHTML.listItem(contentsSourcesLink())));
    this.config.license().ifPresent(
      path -> contents.appendChild(MinXHTML.listItem(contentsLicenseLink())));
    this.config.bugTracker().ifPresent(
      path -> contents.appendChild(MinXHTML.listItem(contentsBugTrackerLink())));

    area.appendChild(contents);
    return area;
  }

  private Element head()
  {
    final Element head = new Element("head", MinXHTML.XHTML);
    final Element title = new Element("title", MinXHTML.XHTML);
    title.appendChild(this.config.projectName());

    head.appendChild(metaGenerator());
    head.appendChild(metaType());
    head.appendChild(css());
    head.appendChild(title);
    return head;
  }
}
