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

import org.w3c.dom.Element;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Reindent XHTML files.
 */

public final class MinXHTMLReindent
{
  private MinXHTMLReindent()
  {

  }

  /**
   * Read the file at {@code path_input}, indent it and write the output to
   * {@code path_tmp}, then atomically rename {@code path_tmp} to {@code
   * path_output}.
   *
   * @param path_input  The input file
   * @param path_tmp    The temporary output file
   * @param path_output The final output file
   *
   * @throws TransformerException         On XML or I/O errors
   * @throws IOException                  On XML or I/O errors
   * @throws ParserConfigurationException On XML or I/O errors
   * @throws SAXException                 On XML or I/O errors
   * @throws InstantiationException       On XML or I/O errors
   * @throws IllegalAccessException       On XML or I/O errors
   * @throws ClassNotFoundException       On XML or I/O errors
   */

  public static void indent(
    final Path path_input,
    final Path path_tmp,
    final Path path_output)
    throws
    TransformerException,
    IOException,
    ParserConfigurationException,
    SAXException,
    InstantiationException,
    IllegalAccessException,
    ClassNotFoundException
  {
    try (InputStream input = Files.newInputStream(path_input)) {
      final InputSource src =
        new InputSource(input);

      final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      dbf.setValidating(false);
      dbf.setNamespaceAware(true);
      dbf.setFeature(
        "http://xml.org/sax/features/namespaces", false);
      dbf.setFeature(
        "http://xml.org/sax/features/validation", false);
      dbf.setFeature(
        "http://apache.org/xml/features/nonvalidating/load-dtd-grammar",
        false);
      dbf.setFeature(
        "http://apache.org/xml/features/nonvalidating/load-external-dtd",
        false);

      final Element document =
        dbf.newDocumentBuilder()
          .parse(src)
          .getDocumentElement();

      final DOMImplementationRegistry registry =
        DOMImplementationRegistry.newInstance();
      final DOMImplementationLS impl =
        (DOMImplementationLS) registry.getDOMImplementation("LS");

      final LSSerializer writer = impl.createLSSerializer();
      writer.getDomConfig().setParameter(
        "format-pretty-print",
        Boolean.TRUE);
      writer.getDomConfig().setParameter(
        "xml-declaration",
        Boolean.FALSE);

      try (OutputStream output = Files.newOutputStream(path_tmp)) {
        try (BufferedWriter buffered =
               new BufferedWriter(new OutputStreamWriter(output, UTF_8))) {
          buffered.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
          buffered.append(System.lineSeparator());
          buffered.append(
            "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">");
          buffered.append(System.lineSeparator());
          buffered.flush();

          output.write(writer.writeToString(document).getBytes(UTF_8));
          output.flush();
        }
      }

      Files.move(
        path_tmp,
        path_output,
        StandardCopyOption.ATOMIC_MOVE,
        StandardCopyOption.REPLACE_EXISTING);
    }
  }
}
