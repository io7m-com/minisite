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

import io.vavr.collection.Vector;
import org.immutables.value.Value;
import org.immutables.vavr.encodings.VavrEncodingEnabled;

import java.nio.file.Path;
import java.util.Optional;

/**
 * Configurations for sites.
 */

@Value.Immutable
@VavrEncodingEnabled
@MinImmutableStyleType
public interface MinConfigurationType
{
  /**
   * @return The name of the project
   */

  @Value.Parameter
  String projectName();

  /**
   * @return The group name of the project
   */

  @Value.Parameter
  String projectGroupName();

  /**
   * @return The project modules
   */

  @Value.Parameter
  Vector<String> projectModules();

  /**
   * @return The version number of the current release
   */

  @Value.Parameter
  String release();

  /**
   * @return The path to the project within the Central Repository
   */

  @Value.Parameter
  String centralReposPath();

  /**
   * @return An XHTML file containing an overview of the project
   */

  @Value.Parameter
  Optional<Path> overview();

  /**
   * @return An XHTML file containing the features of the project
   */

  @Value.Parameter
  Optional<Path> features();

  /**
   * @return The changelog configuration, if any
   */

  @Value.Parameter
  Optional<MinChangesConfiguration> changelog();

  /**
   * @return A plain text file containing the project license
   */

  @Value.Parameter
  Optional<Path> license();

  /**
   * @return The bug tracker configuration, if any
   */

  @Value.Parameter
  Optional<MinBugTrackerConfiguration> bugTracker();

  /**
   * @return The project source configuration, if any
   */

  @Value.Parameter
  Optional<MinSourcesConfiguration> sources();
}
