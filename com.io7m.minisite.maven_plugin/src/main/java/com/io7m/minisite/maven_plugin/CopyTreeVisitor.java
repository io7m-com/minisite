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

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;

final class CopyTreeVisitor extends SimpleFileVisitor<Path>
{
  private final Path output;
  private Path source;

  CopyTreeVisitor(
    final Path in_output)
  {
    this.output = Objects.requireNonNull(in_output, "Output");
  }

  @Override
  public FileVisitResult preVisitDirectory(
    final Path dir,
    final BasicFileAttributes attrs)
    throws IOException
  {
    if (this.source == null) {
      this.source = dir;
    } else {
      Files.createDirectories(this.output.resolve(this.source.relativize(dir)));
    }
    return FileVisitResult.CONTINUE;
  }

  @Override
  public FileVisitResult visitFile(
    final Path file,
    final BasicFileAttributes attrs)
    throws IOException
  {
    Files.copy(file, this.output.resolve(this.source.relativize(file)));
    return FileVisitResult.CONTINUE;
  }
}
