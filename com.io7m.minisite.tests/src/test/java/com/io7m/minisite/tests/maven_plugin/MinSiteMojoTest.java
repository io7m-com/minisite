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

package com.io7m.minisite.tests.maven_plugin;

import io.takari.maven.testing.TestMavenRuntime;
import io.takari.maven.testing.TestResources;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.File;
import java.io.UncheckedIOException;
import java.nio.file.NoSuchFileException;

import static io.takari.maven.testing.TestResources.assertFilesPresent;
import static org.hamcrest.core.IsInstanceOf.instanceOf;

public final class MinSiteMojoTest
{
  @Rule
  public final TestResources resources = new TestResources();

  @Rule
  public final TestMavenRuntime maven = new TestMavenRuntime();

  @Rule
  public final ExpectedException expected = ExpectedException.none();

  @Test
  public void testOptionalFiles()
    throws Exception
  {
    final File basedir = this.resources.getBasedir("optional_files");
    this.maven.executeMojo(basedir, "generateSite");
    assertFilesPresent(basedir, "target/minisite/index.xhtml");
  }

  @Test
  public void testOptionalFilesEmptyChanges()
    throws Exception
  {
    final File basedir = this.resources.getBasedir("optional_files_empty_changes");
    this.maven.executeMojo(basedir, "generateSite");
    assertFilesPresent(basedir, "target/minisite/index.xhtml");
  }

  @Test
  public void testOptionalFilesMissingFeatures()
    throws Exception
  {
    final File basedir = this.resources.getBasedir("optional_files_missing_features");
    this.expected.expect(UncheckedIOException.class);
    this.expected.expectCause(instanceOf(NoSuchFileException.class));
    this.maven.executeMojo(basedir, "generateSite");
  }

  @Test
  public void testOptionalFilesMissingOverview()
    throws Exception
  {
    final File basedir = this.resources.getBasedir("optional_files_missing_overview");
    this.expected.expect(UncheckedIOException.class);
    this.expected.expectCause(instanceOf(NoSuchFileException.class));
    this.maven.executeMojo(basedir, "generateSite");
  }

  @Test
  public void testOptionalFilesMissingChanges()
    throws Exception
  {
    final File basedir = this.resources.getBasedir("optional_files_missing_changes");
    this.expected.expect(UncheckedIOException.class);
    this.expected.expectCause(instanceOf(NoSuchFileException.class));
    this.maven.executeMojo(basedir, "generateSite");
  }

  @Test
  public void testLicenses()
    throws Exception
  {
    final File basedir = this.resources.getBasedir("licenses");
    this.maven.executeMojo(basedir, "generateSite");
    assertFilesPresent(basedir, "target/minisite/index.xhtml");
  }

  @Test
  public void testLicensesLocal()
    throws Exception
  {
    final File basedir = this.resources.getBasedir("licenses_local");
    this.maven.executeMojo(basedir, "generateSite");
    assertFilesPresent(basedir, "target/minisite/index.xhtml");
  }

  @Test
  public void testIssues()
    throws Exception
  {
    final File basedir = this.resources.getBasedir("issues");
    this.maven.executeMojo(basedir, "generateSite");
    assertFilesPresent(basedir, "target/minisite/index.xhtml");
  }

  @Test
  public void testSCMGit()
    throws Exception
  {
    final File basedir = this.resources.getBasedir("scm_git");
    this.maven.executeMojo(basedir, "generateSite");
    assertFilesPresent(basedir, "target/minisite/index.xhtml");
  }

  @Test
  public void testSCMUnknown()
    throws Exception
  {
    final File basedir = this.resources.getBasedir("scm_unknown");
    this.maven.executeMojo(basedir, "generateSite");
    assertFilesPresent(basedir, "target/minisite/index.xhtml");
  }
}
