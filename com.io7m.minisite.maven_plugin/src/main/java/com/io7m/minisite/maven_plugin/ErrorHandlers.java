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

import com.io7m.changelog.parser.api.CParseError;
import com.io7m.junreachable.UnreachableCodeException;
import org.apache.maven.plugin.logging.Log;
import org.slf4j.Logger;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Functions to handle parse errors.
 */

public final class ErrorHandlers
{
  private ErrorHandlers()
  {
    throw new UnreachableCodeException();
  }

  /**
   * A parse error handler that formats and logs all errors to the given
   * logger.
   *
   * @param logger The logger
   *
   * @return An error handler
   */

  public static Consumer<CParseError> loggingHandler(
    final Log logger)
  {
    Objects.requireNonNull(logger, "Logger");

    return error -> {
      final Optional<URI> file_opt = error.lexical().file();
      final Optional<Exception> ex_opt = error.exception();
      switch (error.severity()) {
        case WARNING: {
          onWarn(logger, error, file_opt, ex_opt);
          break;
        }
        case ERROR: {
          onError(logger, error, file_opt, ex_opt);
          break;
        }
        case CRITICAL: {
          onError(logger, error, file_opt, ex_opt);
          break;
        }
      }
    };
  }

  private static void onWarn(
    final Log logger,
    final CParseError error,
    final Optional<URI> file_opt,
    final Optional<Exception> ex_opt)
  {
    if (file_opt.isPresent()) {
      if (ex_opt.isPresent()) {
        logger.warn(
          String.format(
            "%s:%d:%d: %s: (%s)",
            file_opt.get(),
            Integer.valueOf(error.lexical().line()),
            Integer.valueOf(error.lexical().column()),
            error.message(),
            ex_opt.get()));
      } else {
        logger.warn(
          String.format(
            "%s:%d:%d: %s",
            file_opt.get(),
            Integer.valueOf(error.lexical().line()),
            Integer.valueOf(error.lexical().column()),
            error.message()));
      }
    } else {
      if (ex_opt.isPresent()) {
        logger.warn(
          String.format(
            "%d:%d: %s: (%s)",
            Integer.valueOf(error.lexical().line()),
            Integer.valueOf(error.lexical().column()),
            error.message(),
            ex_opt.get()));
      } else {
        logger.warn(
          String.format(
            "%d:%d: %s",
            Integer.valueOf(error.lexical().line()),
            Integer.valueOf(error.lexical().column()),
            error.message()));
      }
    }
  }

  private static void onError(
    final Log logger,
    final CParseError error,
    final Optional<URI> file_opt,
    final Optional<Exception> ex_opt)
  {
    if (file_opt.isPresent()) {
      if (ex_opt.isPresent()) {
        logger.error(
          String.format(
            "%s:%d:%d: %s: (%s)",
            file_opt.get(),
            Integer.valueOf(error.lexical().line()),
            Integer.valueOf(error.lexical().column()),
            error.message(),
            ex_opt.get()));
      } else {
        logger.error(
          String.format(
            "%s:%d:%d: %s",
            file_opt.get(),
            Integer.valueOf(error.lexical().line()),
            Integer.valueOf(error.lexical().column()),
            error.message()));
      }
    } else {
      if (ex_opt.isPresent()) {
        logger.error(
          String.format(
            "%d:%d: %s: (%s)",
            Integer.valueOf(error.lexical().line()),
            Integer.valueOf(error.lexical().column()),
            error.message(),
            ex_opt.get()));
      } else {
        logger.error(
          String.format(
            "%d:%d: %s",
            Integer.valueOf(error.lexical().line()),
            Integer.valueOf(error.lexical().column()),
            error.message()));
      }
    }
  }
}
