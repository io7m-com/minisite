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

package com.io7m.minisite.tests;

import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

public final class XHTMLValidation
{
  private static final Logger LOG =
    LoggerFactory.getLogger(XHTMLValidation.class);

  private XHTMLValidation()
  {

  }

  public static void validate(
    final File baseDirectory,
    final String file)
    throws Exception
  {
    validate(new File(baseDirectory, file));
  }

  public static void validate(
    final File file)
    throws Exception
  {
    final var streamURL =
      XHTMLValidation.class.getResource(
        "/com/io7m/minisite/tests/xhtml11/xhtml11.xsd"
      );

    final var factory =
      SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    final var schema =
      factory.newSchema(streamURL);
    final var validator =
      schema.newValidator();

    final var failed =
      new AtomicBoolean(false);

    validator.setErrorHandler(new ErrorHandler()
    {
      @Override
      public void warning(
        final SAXParseException exception)
      {
        LOG.warn("warning: ", exception);
        failed.set(true);
      }

      @Override
      public void error(
        final SAXParseException exception)
      {
        LOG.error("error: ", exception);
        failed.set(true);
      }

      @Override
      public void fatalError(
        final SAXParseException exception)
        throws SAXParseException
      {
        LOG.error("fatal: ", exception);
        failed.set(true);
        throw exception;
      }
    });

    validator.validate(new StreamSource(file));
    Assert.assertFalse("Validation must succeed", failed.get());
  }
}
