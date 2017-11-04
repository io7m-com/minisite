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


package com.io7m.minisite.tests.core;

import com.io7m.minisite.core.MinBugTrackerConfiguration;
import com.io7m.minisite.core.MinChangesConfiguration;
import com.io7m.minisite.core.MinConfiguration;
import com.io7m.minisite.core.MinSite;
import com.io7m.minisite.core.MinSourcesConfiguration;
import com.io7m.minisite.core.MinXHTMLReindent;
import nu.xom.DocType;
import nu.xom.Document;
import nu.xom.Serializer;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class Demo
{
  private Demo()
  {

  }

  public static void main(
    final String[] args)
    throws IOException
  {
    final MinConfiguration c =
      MinConfiguration.builder()
        .setProjectName("com.io7m.r2")
        .setRelease("0.3.0")
        .setCentralReposPath("/com/io7m/r2/")
        .setOverview(Paths.get("overview.xml"))
        .setFeatures(Paths.get("features.xml"))
        .setChangelog(MinChangesConfiguration.builder()
                        .setFeedEmail("nobody@example.com")
                        .setFile(Paths.get("/tmp/README-CHANGES.xml"))
                        .build())
        .setLicense(Paths.get("README-LICENSE.txt"))
        .setSources(
          MinSourcesConfiguration.builder()
            .setSystem("Git")
            .setUri(URI.create("https://github.com/io7m/r2"))
            .build())
        .setBugTracker(
          MinBugTrackerConfiguration.builder()
            .setSystem("GitHub Issues")
            .setUri(URI.create("https://github.com/io7m/r2/issues"))
            .build())
        .build();

    final MinSite site = MinSite.create(c);

    try {
      final Path directory = Paths.get("/shared-tmp");
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
      throw new IOException(e);
    }
  }
}
