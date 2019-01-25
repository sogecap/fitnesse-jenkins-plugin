# Fitnesse Jenkins Plugin

## About

This plugin enables launching and publishing [FitNesse](http://fitnesse.org/) pages (tests or suites) as a build and post-build step respectively. 
Tests reports are presented similarly to the ones published by [the existing JUnit plugin](https://wiki.jenkins.io/display/JENKINS/JUnit+Plugin/).

This plugin is written in Java 8.

It consists in a [Builder](https://javadoc.jenkins-ci.org/hudson/tasks/Builder.html), which is in charge of executing the FitNesse pages via the 
existing [REST API](http://fitnesse.org/FitNesse.UserGuide.AdministeringFitNesse.RestfulServices), and a [Publisher](https://javadoc.jenkins-ci.org/hudson/tasks/Publisher.html),
which collects, parses and publishes the produced Fitnesse report files.

Three executions modes are available : 

- execute a suite from the plugin's configuration
- execute a pages list from the plugin's configuration
- execute a pages list from a text file residing in the workspace, e.g. after a Git or SVN checkout

This was developed with distributed Jenkins deployments in mind, and as such both the build and post-build steps 
are executed on a remote node if applicable.

## Development

### Structure

This project adheres to the [standard Maven layout](https://maven.apache.org/guides/introduction/introduction-to-the-standard-directory-layout.html).

All static resources (CSS, JS, images) can be found in the `/src/main/webapp` directory.

[Jelly](http://commons.apache.org/proper/commons-jelly/index.html) templates and l10n properties files can be found in the `/src/main/resources`directory.

### Installation

Once the plugin is packaged (debug .hpl version: `mvn clean package hpi:hpl`, standard .hpi version: `mvn clean package hpi:hpi`), it can be [uploaded and installed](https://jenkins.io/doc/book/managing/plugins/)
to a Jenkins instance right away.

Alternatively, the plugin can be placed in the appropriate `$JENKINS_HOME/plugins` directory on the Jenkins master. Don't forget to restart it.  

### Debug

A local Jenkins instance can be run with the plugin pre-installed: 

```
mvn clean package hpi:run -DskipTests -Djetty.port=8090
```

The `jetty.port` argument is optional but useful if other servers are running on your machine
and the default port (8080) is not available.

This will setup a Jenkins instance at the following adress:

```
http://localhost:8090/jenkins
```

The Jenkins directory will be persisted in the project root, in the `work` directory.

You can attach a debugger by setting the following environment variables:

- Unix : `export MAVEN_OPTS="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,address=8000,suspend=n"`
- WIndows : `set MAVEN_OPTS=-Xdebug -Xrunjdwp:transport=dt_socket,server=y,address=8000,suspend=n`

#### Notes

The `Messages.properties` files found in `src/main/resources` contain localization strings,
which are made available in the code via the `Messages` class. In case you want to update them, run the `generate-sources` Maven goal.

Two build profiles are available: The default one (`2.89.4`) will build an hpi archive compatible with Jenkins 2.89.4+, and the other one (`2.73.3`) will build an archive that is compatible with Jenkins 2.73.3+.
