minisite
===

[![Maven Central](https://img.shields.io/maven-central/v/com.io7m.minisite/com.io7m.minisite.svg?style=flat-square)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.io7m.minisite%22)
[![Maven Central (snapshot)](https://img.shields.io/nexus/s/com.io7m.minisite/com.io7m.minisite?server=https%3A%2F%2Fs01.oss.sonatype.org&style=flat-square)](https://s01.oss.sonatype.org/content/repositories/snapshots/com/io7m/minisite/)
[![Codecov](https://img.shields.io/codecov/c/github/io7m-com/minisite.svg?style=flat-square)](https://codecov.io/gh/io7m-com/minisite)
![Java Version](https://img.shields.io/badge/21-java?label=java&color=e6c35c)

![com.io7m.minisite](./src/site/resources/minisite.jpg?raw=true)

| JVM | Platform | Status |
|-----|----------|--------|
| OpenJDK (Temurin) Current | Linux | [![Build (OpenJDK (Temurin) Current, Linux)](https://img.shields.io/github/actions/workflow/status/io7m-com/minisite/main.linux.temurin.current.yml)](https://www.github.com/io7m-com/minisite/actions?query=workflow%3Amain.linux.temurin.current)|
| OpenJDK (Temurin) LTS | Linux | [![Build (OpenJDK (Temurin) LTS, Linux)](https://img.shields.io/github/actions/workflow/status/io7m-com/minisite/main.linux.temurin.lts.yml)](https://www.github.com/io7m-com/minisite/actions?query=workflow%3Amain.linux.temurin.lts)|
| OpenJDK (Temurin) Current | Windows | [![Build (OpenJDK (Temurin) Current, Windows)](https://img.shields.io/github/actions/workflow/status/io7m-com/minisite/main.windows.temurin.current.yml)](https://www.github.com/io7m-com/minisite/actions?query=workflow%3Amain.windows.temurin.current)|
| OpenJDK (Temurin) LTS | Windows | [![Build (OpenJDK (Temurin) LTS, Windows)](https://img.shields.io/github/actions/workflow/status/io7m-com/minisite/main.windows.temurin.lts.yml)](https://www.github.com/io7m-com/minisite/actions?query=workflow%3Amain.windows.temurin.lts)|

## minisite

The `minisite` package package implements a trivial replacement for the Maven 
site plugin.

## Features

* Single-page, static, XHTML 1.0 Strict, Javascript-free sites.
* Optional [com.io7m.changelog](https://www.io7m.com/software/changelog/) integration.
* ISC license.

## Usage

The plugin is designed to produce an extremely simple, static, single-page,
XHTML 1.0 Strict site with no Javascript. The plugin assumes the existence of a
64x64 PNG file at `src/site/resources/icon.png`.

Add the following execution to the root module in your project:

```
<build>
  <plugins>

    <!-- Disable the existing maven-site-plugin -->
    <plugin>
      <artifactId>maven-site-plugin</artifactId>
      <version>...</version>
      <executions>
        <execution>
          <id>default-site</id>
          <phase>none</phase>
          <goals>
            <goal>site</goal>
          </goals>
        </execution>
      </executions>
    </plugin>

    <plugin>
      <groupId>com.io7m.minisite</groupId>
      <artifactId>com.io7m.minisite.maven_plugin</artifactId>
      <version>...</version>
      <executions>
        <execution>
          <id>minisite</id>
          <phase>site</phase>
          <goals>
            <goal>generateSite</goal>
          </goals>
          <configuration>
            <headerFile>src/site/resources/header.xml</headerFile>
            <overviewFile>src/site/resources/overview.xml</overviewFile>
            <featuresFile>src/site/resources/features.xml</featuresFile>
            <documentationFile>src/site/resources/documentation.xml</documentationFile>
            <changelogFile>README-CHANGES.xml</changelogFile>
            <changelogFeedEmail>youremail@example.com</changelogFeedEmail>
            <outputDirectory>${project.build.directory}/minisite</outputDirectory>
            <resourcesDirectory>${project.base.directory}/src/site/resources</resourcesDirectory>
            <cssGenerateDefault>true</cssGenerateDefault>
            <cssStyles>
              <cssStyle>minisite.css</cssStyle>
              <cssStyle>example0.css</cssStyle>
              <cssStyle>example1.css</cssStyle>
            </cssStyles>
          </configuration>
        </execution>
      </executions>
      <inherited>false</inherited>
    </plugin>
  </plugins>
</build>
```

By default, the
[maven-site-plugin](https://maven.apache.org/plugins/maven-site-plugin/)
is bound to the Maven `site` lifecycle phase, so it's
generally preferred to disable the plugin by explicitly binding it to the
`none` phase. The `minisite`
plugin is intended for use with multi-module builds and is designed to run
at most once for the parent module, in contrast to the once-per-module execution
model of the `maven-site-plugin`. It's therefore recommended
to set `inherited` to `false` as
shown so that the plugin won't execute for any child modules.

The `overviewFile` parameter specifies an XHTML
file that will be inserted into the `overview` section of
the generated site.

The `documentationFile` parameter specifies an XHTML
file that will be inserted into the `documentation` section of
the generated site.

The `featuresFile` parameter specifies an XHTML
file that will be inserted into the `features` section of
the generated site.

The `headerFile` parameter specifies an XHTML
file that will be inserted into the header section of the generated site
(above the logo and title).

The `changelogFile` parameter specifies an XML
changelog in [changelog](https://www.io7m.com/software/changelog)
format that will be converted into XHTML for the generated site. If no
file is specified, no `changes` section will be generated.

The `changelogFeedEmail` parameter the email address
that will be used when generating an RSS feed for the changelog.

The `outputDirectory` parameter specifies the directory
to which site files will be generated, and resources copied. This parameter is
optional and defaults to `${project.build.directory}/minisite`.

The `resourcesDirectory` parameter specifies the directory
from which site resources will be copied copied. This parameter is
optional and defaults to `${project.base.directory}/src/site/resources`.

The `cssGenerateDefault` parameter specifies that
the plugin should copy a default CSS style to the output directory. The parameter
is optional and defaults to `true`.

The `cssStyles` parameter specifies a list of
CSS styles that should be imported by the generated site.

