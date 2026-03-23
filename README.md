# java

 [![build](https://github.com/ngeor/java/actions/workflows/build.yml/badge.svg)](https://github.com/ngeor/java/actions/workflows/build.yml)

A CLI that releases Java libraries.

Essentially a wrapper around `mvn release:prepare`, but also takes care of
generating the changelog with `git-cliff`.

Additional points:

- Ensures the code is on the latest version and the master branch
- Ensures the tag doesn't already exist
- Validates the given versions are SemVer
- Non-interactive
