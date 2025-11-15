#!/usr/bin/env bash
#
# Use with find in this way:
#
# find . -type d -depth 1 -exec ./java/scripts/test-repo.sh {} \; -print

if [[ ! -d "$1/.git" ]]; then
    exit 1
fi

if [[ ! -f "$1/pom.xml" ]]; then
    exit 2
fi
