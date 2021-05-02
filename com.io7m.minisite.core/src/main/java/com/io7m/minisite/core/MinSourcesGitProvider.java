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

import com.io7m.minisite.core.internal.MinXHTML;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

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
    final Document document,
    final MinSourcesConfiguration configuration)
  {
    Objects.requireNonNull(configuration, "Configuration");

    final var documentation =
      document.createElementNS(MinXHTML.XHTML, "div");
    documentation.setAttribute("id", "Sources");
    documentation.appendChild(MinXHTML.h2(document, "Sources"));

    {
      final var p =
        document.createElementNS(MinXHTML.XHTML, "p");
      p.appendChild(document.createTextNode("This project uses "));
      p.appendChild(MinXHTML.link(document, "http://www.git-scm.com", "Git"));
      p.appendChild(document.createTextNode(" to manage source code."));
      documentation.appendChild(p);
    }

    final var configuration_uri = configuration.uri();
    final var uri_text = configuration_uri.toString();

    {
      final var p =
        document.createElementNS(MinXHTML.XHTML, "p");
      p.appendChild(document.createTextNode("Repository: "));
      p.appendChild(MinXHTML.link(document, uri_text, uri_text));
      documentation.appendChild(p);
    }

    {
      final var p =
        document.createElementNS(MinXHTML.XHTML, "pre");
      p.appendChild(document.createTextNode("$ git clone "));
      p.appendChild(document.createTextNode(uri_text));
      documentation.appendChild(p);
    }

    return documentation;
  }
}
