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

import com.io7m.minisite.core.internal.MinXHTMLChangelogs;
import com.io7m.minisite.core.internal.MinXMLParse;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

import static com.io7m.minisite.core.internal.MinXHTML.XHTML;
import static com.io7m.minisite.core.internal.MinXHTML.h2;
import static com.io7m.minisite.core.internal.MinXHTML.link;
import static com.io7m.minisite.core.internal.MinXHTML.listItem;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * A site generator.
 */

public final class MinSite
{
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
    final Document document,
    final MinSourcesConfiguration sources)
  {
    final var loader =
      ServiceLoader.load(MinSourcesProviderType.class);

    final var iter = loader.iterator();
    while (iter.hasNext()) {
      final var provider = iter.next();
      if (Objects.equals(provider.system(), sources.system())) {
        return provider.evaluate(document, sources);
      }
    }

    throw new NoSuchElementException(
      "No providers are available for source repositories of type: " + sources.system());
  }

  private static Element features(
    final Document document,
    final Path path)
  {
    final var features = document.createElementNS(XHTML, "div");
    features.setAttribute("id", "features");
    features.appendChild(h2(document, "Features"));
    features.appendChild(MinXMLParse.parseFileUnchecked(
      document,
      path.toAbsolutePath()));
    return features;
  }

  private static Element bugTracker(
    final Document document,
    final MinBugTrackerConfiguration tracker)
  {
    final var container = document.createElementNS(XHTML, "div");
    container.setAttribute("id", "bug-tracker");
    container.appendChild(h2(document, "Bug Tracker"));

    {
      final var p = document.createElementNS(XHTML, "p");
      p.appendChild(document.createTextNode("The project uses "));
      p.appendChild(link(
        document,
        tracker.uri().toString(),
        tracker.system()));
      p.appendChild(document.createTextNode(" to track issues."));
      container.appendChild(p);
    }

    return container;
  }

  private static Element license(
    final Document document,
    final Path path)
  {
    final var license = document.createElementNS(XHTML, "div");
    license.setAttribute("id", "license");
    license.appendChild(h2(document, "License"));

    final var pre = document.createElementNS(XHTML, "pre");
    try {
      pre.appendChild(
        document.createTextNode(
          Files.readAllLines(path, UTF_8)
            .stream()
            .collect(Collectors.joining(System.lineSeparator()))
        )
      );
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

  private static Element contentsLicenseLink(
    final Document document)
  {
    return link(document, "#license", "License");
  }

  private static Element contentsSourcesLink(
    final Document document)
  {
    return link(document, "#sources", "Sources");
  }

  private static Element contentsChangesLink(
    final Document document)
  {
    return link(document, "#changes", "Changes");
  }

  private static Element contentsDocumentationLink(
    final Document document)
  {
    return link(document, "#documentation", "Documentation");
  }

  private static Element contentsReleasesLink(
    final Document document)
  {
    return link(document, "#releases", "Releases");
  }

  private static Element contentsBugTrackerLink(
    final Document document)
  {
    return link(document, "#bug-tracker", "Bug Tracker");
  }

  private static Element contentsFeaturesLink(
    final Document document)
  {
    return link(document, "#features", "Features");
  }

  private static Element contentsMavenLink(
    final Document document)
  {
    return link(document, "#maven", "Maven");
  }

  private static Element css(
    final Document document,
    final String name)
  {
    Objects.requireNonNull(name, "name");
    final var style = document.createElementNS(XHTML, "link");
    style.setAttribute("type", "text/css");
    style.setAttribute("rel", "stylesheet");
    style.setAttribute("href", name);
    return style;
  }

  private static Element metaGenerator(
    final Document document)
  {
    final var content =
      new StringBuilder(128)
        .append("https://www.github.com/io7m/minisite; ")
        .append(version())
        .toString();

    final var meta = document.createElementNS(XHTML, "meta");
    meta.setAttribute("name", "generator");
    meta.setAttribute("content", content);
    return meta;
  }

  private static Element metaType(
    final Document document)
  {
    final var meta = document.createElementNS(XHTML, "meta");
    meta.setAttribute("http-equiv", "Content-Type");
    meta.setAttribute("content", "application/xhtml+xml; charset=UTF-8");
    return meta;
  }

  private static String version()
  {
    final var version = MinSite.class.getPackage().getImplementationVersion();
    return version == null ? "UNKNOWN" : version;
  }

  private static Element header(
    final Document document,
    final Path path)
  {
    final var header = document.createElementNS(XHTML, "div");
    header.setAttribute("id", "header");
    header.appendChild(MinXMLParse.parseFileUnchecked(
      document,
      path.toAbsolutePath()));
    return header;
  }

  private static void mavenDependency(
    final Document document,
    final Element pre,
    final String module,
    final String group,
    final String version)
  {
    final var linkGroup =
      new StringBuilder(64)
        .append("http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22")
        .append(group)
        .append("%22")
        .toString();

    final var linkArtifact =
      new StringBuilder(64)
        .append("http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22")
        .append(module)
        .append("%22")
        .toString();

    final var linkVersion =
      new StringBuilder(64)
        .append("http://search.maven.org/#artifactdetails%7C")
        .append(group)
        .append("%7C")
        .append(module)
        .append("%7C")
        .append(version)
        .append("%7Cjar")
        .toString();

    final var separator = System.lineSeparator();
    pre.appendChild(
      document.createTextNode(
        new StringBuilder(64)
          .append("<dependency>")
          .append(separator)
          .append("  <groupId>")
          .toString())
    );

    pre.appendChild(link(document, linkGroup, group));

    pre.appendChild(
      document.createTextNode(
        new StringBuilder(64)
          .append("</groupId>")
          .append(separator)
          .append("  <artifactId>")
          .toString())
    );

    pre.appendChild(link(document, linkArtifact, module));

    pre.appendChild(
      document.createTextNode(
        new StringBuilder(64)
          .append("</artifactId>")
          .append(separator)
          .append("  <version>")
          .toString())
    );

    pre.appendChild(link(document, linkVersion, version));

    pre.appendChild(
      document.createTextNode(
        new StringBuilder(64)
          .append("</version>")
          .append(separator)
          .append("</dependency>")
          .append(separator)
          .append(separator)
          .toString())
    );
  }

  /**
   * Generate a site.
   *
   * @param document The document that will own the generated elements
   *
   * @return A generated page
   */

  public Element document(
    final Document document)
  {
    final var head = this.head(document);
    final var body = this.body(document);

    final var xhtml = document.createElementNS(XHTML, "html");
    xhtml.appendChild(head);
    xhtml.appendChild(body);
    return xhtml;
  }

  private Element body(
    final Document document)
  {
    final var body = document.createElementNS(XHTML, "body");
    final var main = this.main(document);

    body.appendChild(main);
    return body;
  }

  private Element main(
    final Document document)
  {
    final var main = document.createElementNS(XHTML, "div");
    main.setAttribute("id", "main");

    this.config.header()
      .ifPresent(path -> {
        main.appendChild(header(document, path));
      });

    main.appendChild(this.overview(document));
    main.appendChild(this.contents(document));

    this.config.features()
      .ifPresent(path -> {
        main.appendChild(features(document, path));
      });

    main.appendChild(this.releases(document));

    this.config.documentation()
      .ifPresent(path -> {
        main.appendChild(this.documentation(document, path));
      });

    main.appendChild(this.maven(document));

    this.config.changelog()
      .ifPresent(changelog -> {
        main.appendChild(MinXHTMLChangelogs.changelog(document, changelog));
      });

    this.config.sources()
      .ifPresent(sources -> {
        main.appendChild(sources(document, sources));
      });

    this.config.license()
      .ifPresent(path -> {
        main.appendChild(license(document, path));
      });

    this.config.bugTracker()
      .ifPresent(tracker -> {
        main.appendChild(bugTracker(document, tracker));
      });

    return main;
  }

  private Element maven(
    final Document document)
  {
    final var maven = document.createElementNS(XHTML, "div");
    maven.setAttribute("id", "maven");
    maven.appendChild(h2(document, "Maven"));

    {
      final var p = document.createElementNS(XHTML, "p");
      p.appendChild(document.createTextNode(
        "The following is a complete list of the project's modules expressed as Maven dependencies: "
      ));
      maven.appendChild(p);
    }

    {
      final var pre = document.createElementNS(XHTML, "pre");
      final var group = this.config.projectGroupName();
      final var version = this.config.release();
      mavenDependency(document, pre, this.config.projectName(), group, version);
      for (final var module : this.config.projectModules()) {
        mavenDependency(document, pre, module, group, version);
      }
      maven.appendChild(pre);
    }

    {
      final var p = document.createElementNS(XHTML, "p");
      p.appendChild(document.createTextNode(
        "Each release of the project is made available on "));
      p.appendChild(link(document, "http://search.maven.org", "Maven Central"));
      p.appendChild(document.createTextNode(
        " within ten minutes of the release announcement."));
      maven.appendChild(p);
    }

    return maven;
  }

  private Element documentation(
    final Document document,
    final Path path)
  {
    final var documentation =
      document.createElementNS(XHTML, "div");
    documentation.setAttribute("id", "documentation");
    documentation.appendChild(h2(document, "Documentation"));

    {
      final var p = document.createElementNS(XHTML, "p");
      p.appendChild(document.createTextNode("Documentation for the "));
      final var tt = document.createElementNS(XHTML, "tt");
      tt.appendChild(document.createTextNode(this.config.release()));
      p.appendChild(tt);
      p.appendChild(document.createTextNode(
        " release is available for reading online."));
      documentation.appendChild(p);
    }

    {
      final var p = document.createElementNS(XHTML, "p");
      p.appendChild(
        document.createTextNode(
          "Documentation for current and older releases is archived in the "));
      p.appendChild(link(document, this.centralRepos(), "repository"));
      p.appendChild(document.createTextNode("."));
      documentation.appendChild(p);
    }

    documentation.appendChild(MinXMLParse.parseFileUnchecked(document, path));
    return documentation;
  }

  private Element releases(
    final Document document)
  {
    final var releases = document.createElementNS(XHTML, "div");
    releases.setAttribute("id", "releases");
    releases.appendChild(h2(document, "Releases"));

    {
      final var p = document.createElementNS(XHTML, "p");
      p.appendChild(document.createTextNode(
        "The most recently published version of the software is "));
      final var tt = document.createElementNS(XHTML, "tt");
      tt.appendChild(document.createTextNode(this.config.release()));
      p.appendChild(tt);
      p.appendChild(document.createTextNode("."));
      releases.appendChild(p);
    }

    {
      final var p = document.createElementNS(XHTML, "p");
      p.appendChild(document.createTextNode(
        "Source code and binaries are available from the "));
      p.appendChild(link(document, this.centralRepos(), "repository"));
      p.appendChild(document.createTextNode("."));
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

  private Element overview(
    final Document document)
  {
    final var overview = document.createElementNS(XHTML, "div");
    overview.setAttribute("id", "overview");
    overview.appendChild(this.overviewTitleArea(document));
    overview.appendChild(this.overviewContentArea(document));
    return overview;
  }

  private Element overviewTitleArea(
    final Document document)
  {
    final var area = document.createElementNS(XHTML, "div");
    area.setAttribute("class", "overview_title_area");

    final var img = document.createElementNS(XHTML, "img");
    img.setAttribute("src", "icon.png");
    img.setAttribute("width", "64");
    img.setAttribute("height", "64");
    img.setAttribute("class", "icon");
    img.setAttribute("alt", "Project icon");

    final var title = document.createElementNS(XHTML, "h1");
    title.appendChild(document.createTextNode(this.config.projectName()));

    area.appendChild(img);
    area.appendChild(title);
    return area;
  }

  private Element overviewContentArea(
    final Document document)
  {
    final var area = document.createElementNS(XHTML, "div");
    area.setAttribute("class", "overview_content_area");

    this.config.overview().ifPresent(path -> {
      area.appendChild(MinXMLParse.parseFileUnchecked(document, path));
    });

    return area;
  }

  private Element contents(
    final Document document)
  {
    final var area = document.createElementNS(XHTML, "div");
    area.appendChild(h2(document, "Contents"));

    final var contents = document.createElementNS(XHTML, "ul");

    this.config.features().ifPresent(
      path -> contents.appendChild(listItem(
        document,
        contentsFeaturesLink(document))));

    contents.appendChild(listItem(
      document,
      contentsReleasesLink(document)));

    this.config.documentation().ifPresent(
      path -> contents.appendChild(listItem(
        document,
        contentsDocumentationLink(document))));

    contents.appendChild(listItem(
      document,
      contentsMavenLink(document)));

    this.config.changelog().ifPresent(
      path -> contents.appendChild(listItem(
        document,
        contentsChangesLink(document))));

    this.config.sources().ifPresent(
      sources -> contents.appendChild(listItem(
        document,
        contentsSourcesLink(document))));

    this.config.license().ifPresent(
      path -> contents.appendChild(listItem(
        document,
        contentsLicenseLink(document))));

    this.config.bugTracker().ifPresent(
      path -> contents.appendChild(listItem(
        document,
        contentsBugTrackerLink(document))));

    area.appendChild(contents);
    return area;
  }

  private Element head(
    final Document document)
  {
    final var head = document.createElementNS(XHTML, "head");
    final var title = document.createElementNS(XHTML, "title");
    title.appendChild(document.createTextNode(this.config.projectName()));

    head.appendChild(metaGenerator(document));
    head.appendChild(metaType(document));
    this.config.cssIncludes()
      .forEach(name -> head.appendChild(css(document, name)));

    head.appendChild(title);
    return head;
  }
}
