# java

[![Maven Central](https://img.shields.io/maven-central/v/com.github.ngeor/java.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/com.github.ngeor/java)
 [![build](https://github.com/ngeor/java/actions/workflows/build.yml/badge.svg)](https://github.com/ngeor/java/actions/workflows/build.yml)

Parent pom for Java projects

The goal of the project is to configure Maven plugins in a standard way.
Dependency management is out of scope.

## Releasing

- Make sure you're on the default branch and there are no pending changes
- Push a tag in the naming convention `vx.y.z`

e.g.

```sh
mvn release:clean
mvn -DtagNameFormat='v@{project.version}' release:prepare
// alternatively to not push changes and also format the pom.xml again
mvn -DtagNameFormat='v@{project.version}' -DpushChanges=false -DcompletionGoals=validate release:prepare
mvn release:clean
```
