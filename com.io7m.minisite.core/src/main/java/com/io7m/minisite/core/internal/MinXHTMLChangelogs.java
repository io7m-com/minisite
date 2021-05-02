/*
 * Copyright © 2021 Mark Raynsford <code@io7m.com> http://io7m.com
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

package com.io7m.minisite.core.internal;

import com.io7m.changelog.core.CChangelog;
import com.io7m.changelog.parser.api.CParseErrorHandlers;
import com.io7m.changelog.xml.api.CXHTMLChangelogWriterProviderType;
import com.io7m.changelog.xml.api.CXMLChangelogParserProviderType;
import com.io7m.minisite.core.MinChangesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.file.Files;
import java.util.NoSuchElementException;
import java.util.ServiceLoader;

/**
 * Terse functions to serialize changelogs.
 */

public final class MinXHTMLChangelogs
{
  private static final Logger LOG =
    LoggerFactory.getLogger(MinXHTMLChangelogs.class);

  private MinXHTMLChangelogs()
  {

  }

  /**
   * Serialize the changelog.
   *
   * @param document      The owning document
   * @param changesConfig The configuration
   *
   * @return The changelog element
   */

  public static Element changelog(
    final Document document,
    final MinChangesConfiguration changesConfig)
  {
    final var parserProviderOpt =
      ServiceLoader.load(CXMLChangelogParserProviderType.class)
        .findFirst();

    if (parserProviderOpt.isEmpty()) {
      throw new NoSuchElementException(
        "No XML changelog parser providers are available");
    }

    final var writerProviderOpt =
      ServiceLoader.load(CXHTMLChangelogWriterProviderType.class)
        .findFirst();

    if (writerProviderOpt.isEmpty()) {
      throw new NoSuchElementException(
        "No XHTML changelog writer providers are available");
    }

    final var parserProvider =
      parserProviderOpt.get();

    final var changesFile = changesConfig.file();
    try (var input = Files.newInputStream(changesFile)) {
      final var parser =
        parserProvider.create(
          changesFile.toUri(),
          input,
          CParseErrorHandlers.loggingHandler(LOG)
        );

      final var changelog = parser.parse();

      final var changes = document.createElementNS(MinXHTML.XHTML, "div");
      changes.setAttribute("id", "changes");
      changes.appendChild(MinXHTML.h2(document, "Changes"));

      {
        final var p = document.createElementNS(MinXHTML.XHTML, "p");
        p.appendChild(document.createTextNode("Subscribe to the releases "));
        p.appendChild(MinXHTML.link(document, "releases.atom", "atom feed"));
        p.appendChild(document.createTextNode("."));
        changes.appendChild(p);
      }

      if (changelog.releases().isEmpty()) {
        final var p = document.createElementNS(MinXHTML.XHTML, "p");
        p.appendChild(document.createTextNode(
          "No formal releases have been made."));
        changes.appendChild(p);
      } else {
        changes.appendChild(serializeChangelog(
          document,
          writerProviderOpt.get(),
          changelog)
        );
      }

      return changes;
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private static Element serializeChangelog(
    final Document document,
    final CXHTMLChangelogWriterProviderType writerProvider,
    final CChangelog changelog)
    throws IOException
  {
    try (var bao = new ByteArrayOutputStream()) {
      final var writer =
        writerProvider.create(URI.create("urn:stdout"), bao);
      writer.write(changelog);

      final var docBuilderFactory =
        DocumentBuilderFactory.newInstance();

      docBuilderFactory.setXIncludeAware(false);
      docBuilderFactory.setExpandEntityReferences(false);
      docBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

      final var docBuilder =
        docBuilderFactory.newDocumentBuilder();

      try (var bai = new ByteArrayInputStream(bao.toByteArray())) {
        final var parsedDocument = docBuilder.parse(bai);
        final var root = parsedDocument.getDocumentElement();
        document.adoptNode(root);
        return root;
      }
    } catch (final ParserConfigurationException | SAXException e) {
      throw new IOException(e);
    }
  }
}
