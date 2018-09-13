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
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

import static io.takari.maven.testing.TestResources.assertFilesNotPresent;
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

  public static final class NoOpEntityResolver implements EntityResolver
  {
    public InputSource resolveEntity(
      final String publicId,
      final String systemId)
    {
      return new InputSource(
        new ByteArrayInputStream(" ".getBytes(StandardCharsets.UTF_8)));
    }
  }

  private static String xpathOn(
    final Path url,
    final String xpath_expr)
    throws Exception
  {
    final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
    factory.setValidating(false);
    factory.setExpandEntityReferences(false);
    factory.setFeature(
      "http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

    final DocumentBuilder builder = factory.newDocumentBuilder();
    builder.setEntityResolver(new NoOpEntityResolver());

    final Document doc;
    try (InputStream stream = Files.newInputStream(url)) {
      doc = builder.parse(stream);
    }

    final XPathFactory xPathfactory = XPathFactory.newInstance();
    final XPath xpath = xPathfactory.newXPath();
    final XPathExpression expr = xpath.compile(xpath_expr);
    return expr.evaluate(doc);
  }

  @Test
  public void testOptionalFiles()
    throws Exception
  {
    final File basedir = this.resources.getBasedir("optional_files");
    this.maven.executeMojo(basedir, "generateSite");
    assertFilesPresent(basedir, "target/minisite/index.xhtml");
    assertFilesPresent(basedir, "target/minisite/minisite.css");

    final Path file =
      basedir.toPath().resolve("target/minisite/index.xhtml");
    Assert.assertEquals(
      "true", xpathOn(file, "//@id=\"features\""));
    Assert.assertEquals(
      "true", xpathOn(file, "//@id=\"changes\""));
    Assert.assertEquals(
      "true", xpathOn(file, "//@id=\"overview\""));
    Assert.assertEquals(
      "true", xpathOn(file, "//@id=\"releases\""));
    Assert.assertEquals(
      "true", xpathOn(file, "//@id=\"documentation\""));
    Assert.assertEquals(
      "true", xpathOn(file, "//@id=\"maven\""));
    Assert.assertEquals(
      "false", xpathOn(file, "//@id=\"license\""));
  }

  @Test
  public void testOptionalFilesEmptyChanges()
    throws Exception
  {
    final File basedir = this.resources.getBasedir(
      "optional_files_empty_changes");
    this.maven.executeMojo(basedir, "generateSite");
    assertFilesPresent(basedir, "target/minisite/index.xhtml");
    assertFilesPresent(basedir, "target/minisite/minisite.css");

    final Path file =
      basedir.toPath().resolve("target/minisite/index.xhtml");
    Assert.assertEquals(
      "true", xpathOn(file, "//@id=\"features\""));
    Assert.assertEquals(
      "true", xpathOn(file, "//@id=\"changes\""));
    Assert.assertEquals(
      "true", xpathOn(file, "//@id=\"overview\""));
    Assert.assertEquals(
      "true", xpathOn(file, "//@id=\"releases\""));
    Assert.assertEquals(
      "false", xpathOn(file, "//@id=\"documentation\""));
    Assert.assertEquals(
      "true", xpathOn(file, "//@id=\"maven\""));
    Assert.assertEquals(
      "false", xpathOn(file, "//@id=\"license\""));
  }

  @Test
  public void testOptionalHeader()
    throws Exception
  {
    final File basedir = this.resources.getBasedir(
      "optional_files_header");
    this.maven.executeMojo(basedir, "generateSite");
    assertFilesPresent(basedir, "target/minisite/index.xhtml");
    assertFilesPresent(basedir, "target/minisite/minisite.css");

    final Path file =
      basedir.toPath().resolve("target/minisite/index.xhtml");
    Assert.assertEquals(
      "true", xpathOn(file, "//@id=\"header\""));
  }

  @Test
  public void testOptionalFilesMissingFeatures()
    throws Exception
  {
    final File basedir = this.resources.getBasedir(
      "optional_files_missing_features");
    this.expected.expect(MojoFailureException.class);
    this.expected.expectCause(instanceOf(NoSuchFileException.class));
    this.maven.executeMojo(basedir, "generateSite");
  }

  @Test
  public void testOptionalFilesMissingOverview()
    throws Exception
  {
    final File basedir = this.resources.getBasedir(
      "optional_files_missing_overview");
    this.expected.expect(MojoFailureException.class);
    this.expected.expectCause(instanceOf(NoSuchFileException.class));
    this.maven.executeMojo(basedir, "generateSite");
  }

  @Test
  public void testOptionalFilesMissingChanges()
    throws Exception
  {
    final File basedir = this.resources.getBasedir(
      "optional_files_missing_changes");
    this.expected.expect(MojoFailureException.class);
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
    assertFilesPresent(basedir, "target/minisite/minisite.css");

    final Path file =
      basedir.toPath().resolve("target/minisite/index.xhtml");
    Assert.assertEquals(
      "false", xpathOn(file, "//@id=\"features\""));
    Assert.assertEquals(
      "false", xpathOn(file, "//@id=\"changes\""));
    Assert.assertEquals(
      "true", xpathOn(file, "//@id=\"overview\""));
    Assert.assertEquals(
      "true", xpathOn(file, "//@id=\"releases\""));
    Assert.assertEquals(
      "false", xpathOn(file, "//@id=\"documentation\""));
    Assert.assertEquals(
      "true", xpathOn(file, "//@id=\"maven\""));
    Assert.assertEquals(
      "true", xpathOn(file, "//@id=\"license\""));
  }

  @Test
  public void testLicensesLocal()
    throws Exception
  {
    final File basedir = this.resources.getBasedir("licenses_local");
    this.maven.executeMojo(basedir, "generateSite");
    assertFilesPresent(basedir, "target/minisite/index.xhtml");
    assertFilesPresent(basedir, "target/minisite/minisite.css");

    final Path file =
      basedir.toPath().resolve("target/minisite/index.xhtml");
    Assert.assertEquals(
      "false", xpathOn(file, "//@id=\"features\""));
    Assert.assertEquals(
      "false", xpathOn(file, "//@id=\"changes\""));
    Assert.assertEquals(
      "true", xpathOn(file, "//@id=\"overview\""));
    Assert.assertEquals(
      "true", xpathOn(file, "//@id=\"releases\""));
    Assert.assertEquals(
      "false", xpathOn(file, "//@id=\"documentation\""));
    Assert.assertEquals(
      "true", xpathOn(file, "//@id=\"maven\""));
    Assert.assertEquals(
      "true", xpathOn(file, "//@id=\"license\""));
  }

  @Test
  public void testIssues()
    throws Exception
  {
    final File basedir = this.resources.getBasedir("issues");
    this.maven.executeMojo(basedir, "generateSite");
    assertFilesPresent(basedir, "target/minisite/index.xhtml");
    assertFilesPresent(basedir, "target/minisite/minisite.css");

    final Path file =
      basedir.toPath().resolve("target/minisite/index.xhtml");
    Assert.assertEquals(
      "false", xpathOn(file, "//@id=\"features\""));
    Assert.assertEquals(
      "false", xpathOn(file, "//@id=\"changes\""));
    Assert.assertEquals(
      "true", xpathOn(file, "//@id=\"overview\""));
    Assert.assertEquals(
      "true", xpathOn(file, "//@id=\"releases\""));
    Assert.assertEquals(
      "false", xpathOn(file, "//@id=\"documentation\""));
    Assert.assertEquals(
      "true", xpathOn(file, "//@id=\"maven\""));
    Assert.assertEquals(
      "false", xpathOn(file, "//@id=\"license\""));
  }

  @Test
  public void testSCMGit()
    throws Exception
  {
    final File basedir = this.resources.getBasedir("scm_git");
    this.maven.executeMojo(basedir, "generateSite");
    assertFilesPresent(basedir, "target/minisite/index.xhtml");
    assertFilesPresent(basedir, "target/minisite/minisite.css");

    final Path file =
      basedir.toPath().resolve("target/minisite/index.xhtml");
    Assert.assertEquals(
      "false", xpathOn(file, "//@id=\"features\""));
    Assert.assertEquals(
      "false", xpathOn(file, "//@id=\"changes\""));
    Assert.assertEquals(
      "true", xpathOn(file, "//@id=\"overview\""));
    Assert.assertEquals(
      "true", xpathOn(file, "//@id=\"releases\""));
    Assert.assertEquals(
      "false", xpathOn(file, "//@id=\"documentation\""));
    Assert.assertEquals(
      "true", xpathOn(file, "//@id=\"maven\""));
    Assert.assertEquals(
      "false", xpathOn(file, "//@id=\"license\""));
  }

  @Test
  public void testSCMUnknown()
    throws Exception
  {
    final File basedir = this.resources.getBasedir("scm_unknown");
    this.maven.executeMojo(basedir, "generateSite");
    assertFilesPresent(basedir, "target/minisite/index.xhtml");
    assertFilesPresent(basedir, "target/minisite/minisite.css");

    final Path file =
      basedir.toPath().resolve("target/minisite/index.xhtml");
    Assert.assertEquals(
      "false", xpathOn(file, "//@id=\"features\""));
    Assert.assertEquals(
      "false", xpathOn(file, "//@id=\"changes\""));
    Assert.assertEquals(
      "true", xpathOn(file, "//@id=\"overview\""));
    Assert.assertEquals(
      "true", xpathOn(file, "//@id=\"releases\""));
    Assert.assertEquals(
      "false", xpathOn(file, "//@id=\"documentation\""));
    Assert.assertEquals(
      "true", xpathOn(file, "//@id=\"maven\""));
    Assert.assertEquals(
      "false", xpathOn(file, "//@id=\"license\""));
  }

  @Test
  public void testNoCSS()
    throws Exception
  {
    final File basedir = this.resources.getBasedir("no_css");
    this.maven.executeMojo(basedir, "generateSite");
    assertFilesPresent(basedir, "target/minisite/index.xhtml");
    assertFilesNotPresent(basedir, "target/minisite/minisite.css");
  }
}
