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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// CHECKSTYLE:OFF

public final class DocumentationItem
{
  private String name;
  private List<DocumentationFormat> formats;

  public String getName()
  {
    return this.name;
  }

  public DocumentationItem setName(
    final String name)
  {
    this.name = Objects.requireNonNull(name, "name");
    return this;
  }

  public List<DocumentationFormat> getFormats()
  {
    return this.formats;
  }

  public DocumentationItem setFormats(
    final List<DocumentationFormat> formats)
  {
    this.formats = Objects.requireNonNull(formats, "formats");
    return this;
  }

  public DocumentationItem()
  {
    this.formats = new ArrayList<>();
  }
}
