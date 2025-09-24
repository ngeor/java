# Changelog
All notable changes to this project will be documented in this file.

## [4.9.5] - 2025-09-24

### Bug Fixes

- Switch to PIPE

## [4.9.4] - 2025-09-24

### Miscellaneous Tasks

- Added Pipfile for release.py

### Refactor

- Move all release logic into release.py

## [4.9.3] - 2025-09-24

### Miscellaneous Tasks

- Migrate from OSSRH to Central Maven
- Updated changelog

## [4.9.2] - 2025-02-24

### Bug Fixes

- Adding back dependency management for 3 libraries

## [4.9.1] - 2025-02-17

### Miscellaneous Tasks

- Moved java back to its own repo

## [4.2.0] - 2024-01-26

### Dependencies

- Upgraded dependencies

### Miscellaneous Tasks

- Updated readme

### Deps

- Updated dependency versions

## [4.1.1] - 2022-12-08

### Miscellaneous Tasks

- Fix deployment

## [4.1.0] - 2022-12-08

### Features

- Added sortpom

## [4.0.0] - 2022-12-08

### Miscellaneous Tasks

- [**breaking**] Removed dependency management and most plugins
- Updated CI for Java 17
- [**breaking**] Upgraded to Java 17

## [3.4.0] - 2022-11-17

### Dependencies

- Updated Jackson to 2.14.0
- Updated MapStruct to 1.5.3
- Updated Maven Shade plugin to 3.4.1
- Updated Mockito to 4.9.0
- Updated auto-value to 1.10
- Updated checkstyle rules to 6.3.0
- Updated checkstyle to 10.4
- Update dependency com.puppycrawl.tools:checkstyle to v10.3.4
- Update dependency org.apache.maven.plugins:maven-jar-plugin to v3.3.0
- Update dependency org.apache.maven.plugins:maven-shade-plugin to v3.4.0
- Update dependency org.codehaus.groovy:groovy-all to v3.0.13
- Update dependency org.junit.jupiter:junit-jupiter to v5.9.1
- Update dependency org.mockito:mockito-core to v4.8.0

## [3.3.0] - 2022-09-07

### Dependencies

- Update dependency com.github.ngeor:checkstyle-rules to v6.2.1
- Update dependency com.puppycrawl.tools:checkstyle to v10.3.1
- Update dependency com.puppycrawl.tools:checkstyle to v10.3.3
- Update dependency com.squareup.okhttp3:okhttp to v4.10.0
- Update dependency org.apache.maven.plugins:maven-checkstyle-plugin to v3.2.0
- Update dependency org.apache.maven.plugins:maven-deploy-plugin to v3.0.0
- Update dependency org.apache.maven.plugins:maven-javadoc-plugin to v3.4.1
- Update dependency org.codehaus.groovy:groovy-all to v3.0.12
- Update dependency org.junit.jupiter:junit-jupiter to v5.9.0
- Update dependency org.mapstruct:mapstruct to v1.5.2.final
- Update dependency org.mockito:mockito-core to v4.7.0
- Update jackson.version to v2.13.4

### Features

- Use tag based release workflow

## [3.2.0] - 2022-06-12

### Dependencies

- Add renovate.json (#19)
- Update dependency com.fasterxml.jackson.core:jackson-databind to v2.13.2.1
- Update dependency com.fasterxml.jackson.core:jackson-databind to v2.13.2.2
- Update dependency com.github.ngeor:checkstyle-rules to v5.1.0
- Update dependency com.github.ngeor:checkstyle-rules to v5.2.0
- Update dependency com.github.ngeor:checkstyle-rules to v5.3.0
- Update dependency com.github.ngeor:checkstyle-rules to v6
- Update dependency com.github.ngeor:checkstyle-rules to v6.1.0
- Update dependency com.puppycrawl.tools:checkstyle to v10
- Update dependency com.puppycrawl.tools:checkstyle to v10.1
- Update dependency com.puppycrawl.tools:checkstyle to v10.2
- Update dependency com.puppycrawl.tools:checkstyle to v10.3
- Update dependency org.apache.maven.plugins:maven-compiler-plugin to v3.10.0
- Update dependency org.apache.maven.plugins:maven-compiler-plugin to v3.10.1
- Update dependency org.apache.maven.plugins:maven-dependency-plugin to v3.3.0
- Update dependency org.apache.maven.plugins:maven-enforcer-plugin to v3.1.0
- Update dependency org.apache.maven.plugins:maven-failsafe-plugin to v3.0.0-m6
- Update dependency org.apache.maven.plugins:maven-failsafe-plugin to v3.0.0-m7
- Update dependency org.apache.maven.plugins:maven-javadoc-plugin to v3.3.2
- Update dependency org.apache.maven.plugins:maven-javadoc-plugin to v3.4.0
- Update dependency org.apache.maven.plugins:maven-release-plugin to v3.0.0-m6
- Update dependency org.apache.maven.plugins:maven-shade-plugin to v3.3.0
- Update dependency org.apache.maven.plugins:maven-surefire-plugin to v3.0.0-m6
- Update dependency org.apache.maven.plugins:maven-surefire-plugin to v3.0.0-m7
- Update dependency org.assertj:assertj-core to v3.23.1
- Update dependency org.codehaus.groovy:groovy-all to v3.0.10
- Update dependency org.codehaus.groovy:groovy-all to v3.0.11
- Update dependency org.jacoco:jacoco-maven-plugin to v0.8.8
- Update dependency org.mapstruct:mapstruct to v1.5.0.final
- Update dependency org.mapstruct:mapstruct to v1.5.1.final
- Update dependency org.mockito:mockito-core to v4 (#20)
- Update dependency org.mockito:mockito-core to v4 (#20)
- Update dependency org.mockito:mockito-core to v4.4.0
- Update dependency org.mockito:mockito-core to v4.5.1
- Update dependency org.mockito:mockito-core to v4.6.0
- Update dependency org.mockito:mockito-core to v4.6.1
- Update dependency org.sonatype.plugins:nexus-staging-maven-plugin to v1.6.10
- Update dependency org.sonatype.plugins:nexus-staging-maven-plugin to v1.6.11
- Update dependency org.sonatype.plugins:nexus-staging-maven-plugin to v1.6.12
- Update dependency org.sonatype.plugins:nexus-staging-maven-plugin to v1.6.13
- Update dependency org.sonatype.plugins:nexus-staging-maven-plugin to v1.6.9
- Update jackson.version to v2.13.2
- Update jackson.version to v2.13.3

### Miscellaneous Tasks

- Group dependencies separately in changelog

## [3.1.1] - 2022-02-05

### Bug Fixes

- Generate changelog during release (#18)

## [3.1.0] - 2022-02-05

### Dependencies

- Updated ${checkstyle.version} from 9.2.1 to 9.3

### Features

- Install and use git-cliff during release GitHub Action

## [3.0.0] - 2022-02-02

### Dependencies

- Updated pom properties

### Features

- [**breaking**] Upgrade checkstyle to 9.2.1
- Using properties to manage dependency and plugin versions

## [2.4.0] - 2022-01-27

### Bug Fixes

- Make release script executable upon copying to other repo

### Features

- Support finalizing release

## [2.3.0] - 2022-01-26

### Features

- Adding script to install workflows and release script in other repos
- Support initialization step in release script

### Miscellaneous Tasks

- Excluding changelog commits from changelog
- Remove obsolete release files
- Removed duplicate badge
- Removed unused workflow

## [2.2.1] - 2022-01-26

### Features

- Adding a Python script for releasing
- Performing the release with the Python script

### Miscellaneous Tasks

- Update readme

## [2.2.0] - 2022-01-23

### Features

- Performing release
- Testing release branch

## [1.0.0] - 2021-06-26

### Yak4j-cli

- Added list command
- Added picocli
- Registering new module in parent pom

<!-- generated by git-cliff -->
