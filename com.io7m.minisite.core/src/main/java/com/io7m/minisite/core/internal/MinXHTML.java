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

package com.io7m.minisite.core.internal;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Functions to generate XHTML elements.
 */

public final class MinXHTML
{
  /**
   * The XHTML namespace.
   */

  public static final String XHTML =
    "http://www.w3.org/1999/xhtml";

  private MinXHTML()
  {

  }

  /**
   * Generate an h2 element.
   *
   * @param document The document owner
   * @param text     The element text
   *
   * @return The element
   */

  public static Element h2(
    final Document document,
    final String text)
  {
    final var h2 = document.createElementNS(XHTML, "h2");
    h2.appendChild(document.createTextNode(text));
    return h2;
  }

  /**
   * Generate a list item element.
   *
   * @param document The document owner
   * @param e        The element
   *
   * @return The element
   */

  public static Element listItem(
    final Document document,
    final Element e)
  {
    final Element li = document.createElementNS(XHTML, "li");
    li.appendChild(e);
    return li;
  }

  /**
   * Generate a link element.
   *
   * @param document The document owner
   * @param target   The link target
   * @param text     The link text
   *
   * @return The element
   */

  public static Element link(
    final Document document,
    final String target,
    final String text)
  {
    final Element a = document.createElementNS(XHTML, "a");
    a.setAttribute("href", target);
    a.appendChild(document.createTextNode(text));
    return a;
  }
}
