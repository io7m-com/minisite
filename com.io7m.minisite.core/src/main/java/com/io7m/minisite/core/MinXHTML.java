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

import nu.xom.Attribute;
import nu.xom.Element;

final class MinXHTML
{
  static final String XHTML = "http://www.w3.org/1999/xhtml";

  private MinXHTML()
  {

  }

  static Element h2(
    final String text)
  {
    final Element h2 = new Element("h2", XHTML);
    h2.appendChild(text);
    return h2;
  }

  static Element listItem(
    final Element e)
  {
    final Element li = new Element("li", XHTML);
    li.appendChild(e);
    return li;
  }

  static Element link(
    final String target,
    final String text)
  {
    final Element a = new Element("a", XHTML);
    a.addAttribute(new Attribute("href", target));
    a.appendChild(text);
    return a;
  }
}
