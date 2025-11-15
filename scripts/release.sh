#!/usr/bin/env bash
set -e
mvn -q release:clean
mvn -DtagNameFormat='v@{project.version}' \
    -DpushChanges=false \
    -DcompletionGoals=validate \
    release:prepare

git-cliff -o CHANGELOG.md
git add CHANGELOG.md
git commit -m "Update changelog"
git push --follow-tags
mvn -q release:clean
