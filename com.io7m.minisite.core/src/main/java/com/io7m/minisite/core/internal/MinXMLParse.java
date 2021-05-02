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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Terse functions to parse XML files.
 */

public final class MinXMLParse
{
  private MinXMLParse()
  {

  }

  /**
   * Parse a file and return the root element, owned by {@code document}.
   *
   * @param document The new owner document
   * @param file     The file to be parsed
   *
   * @return A parsed element
   *
   * @throws IOException On errors
   */

  private static Element parseFile(
    final Document document,
    final Path file)
    throws IOException
  {
    try (var stream = Files.newInputStream(file)) {
      final var docBuilderFactory =
        DocumentBuilderFactory.newInstance();

      docBuilderFactory.setXIncludeAware(false);
      docBuilderFactory.setExpandEntityReferences(false);
      docBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

      final var docBuilder =
        docBuilderFactory.newDocumentBuilder();

      final var parsedDocument =
        docBuilder.parse(stream);
      final var root =
        parsedDocument.getDocumentElement();

      document.adoptNode(root);
      return root;
    } catch (final ParserConfigurationException | SAXException e) {
      throw new IOException(e);
    }
  }

  /**
   * Parse a file and return the root element, owned by {@code document}.
   *
   * @param document The new owner document
   * @param file     The file to be parsed
   *
   * @return A parsed element
   *
   * @throws UncheckedIOException On errors
   */

  public static Element parseFileUnchecked(
    final Document document,
    final Path file)
    throws UncheckedIOException
  {
    try {
      return parseFile(document, file);
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
