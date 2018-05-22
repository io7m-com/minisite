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

import java.net.URI;
import java.util.Objects;

/**
 * A source provider for git repositories.
 */

public final class MinSourcesGitProvider implements MinSourcesProviderType
{
  /**
   * Construct a provider.
   */

  public MinSourcesGitProvider()
  {

  }

  @Override
  public String system()
  {
    return "Git";
  }

  @Override
  public Element evaluate(
    final MinSourcesConfiguration configuration)
  {
    Objects.requireNonNull(configuration, "Configuration");

    final Element documentation = new Element("div", MinXHTML.XHTML);
    documentation.addAttribute(new Attribute("id", "Sources"));
    documentation.appendChild(MinXHTML.h2("Sources"));

    {
      final Element p = new Element("p", MinXHTML.XHTML);
      p.appendChild("This project uses ");
      p.appendChild(MinXHTML.link("http://www.git-scm.com", "Git"));
      p.appendChild(" to manage source code.");
      documentation.appendChild(p);
    }

    final URI configuration_uri = configuration.uri();
    final String uri_text = configuration_uri.toString();

    {
      final Element p = new Element("p", MinXHTML.XHTML);
      p.appendChild("Repository: ");
      p.appendChild(MinXHTML.link(uri_text, uri_text));
      documentation.appendChild(p);
    }

    {
      final Element p = new Element("pre", MinXHTML.XHTML);
      p.appendChild("$ git clone ");
      p.appendChild(uri_text);
      documentation.appendChild(p);
    }

    return documentation;
  }
}
