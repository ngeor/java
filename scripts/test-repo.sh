#!/usr/bin/env bash
#
# Use with find in this way:
#
# find . -type d -depth 1 -exec ./java/scripts/test-repo.sh {} \; -print
#
# Can also be used multiple times:
# find . -type d -depth 1 \
#   Exclude multi-module projects
#   -exec ./java/scripts/test-repo.sh -n --modules {} \;
#   Exclude projects that already have the sortpom plugin
#   -exec ./java/scripts/test-repo.sh -n -c sort \{\} \; \
#   -print

POM_TOKENS=()
NEGATE=0
ORIGIN=0
HAS_TAGS=0

while [[ "$1" =~ ^- && ! "$1" == "--" ]]; do case $1 in
    --sonatype )
        POM_TOKENS+=("sonatype")
        ;;
    --main-class )
        POM_TOKENS+=("<mainClass>")
        ;;
    --graal )
        POM_TOKENS+=("graal")
        ;;
    --tomcat )
        POM_TOKENS+=("tomcat")
        ;;
    --spring )
        POM_TOKENS+=("spring")
        ;;
    --modules )
        POM_TOKENS+=("<modules>")
        ;;
    -c | --custom )
        shift; POM_TOKENS+=($1)
        ;;
    -n )
        NEGATE=1
        ;;
    --origin )
        ORIGIN=1
        ;;
    --has-tags )
        HAS_TAGS=1
        ;;
esac; shift; done
if [[ "$1" == '--' ]]; then shift; fi

if [[ ! -d "$1/.git" ]]; then
    exit 1
fi

if [[ $ORIGIN -eq 1 ]]; then
    # special, does not combine with others
    git -C $1 remote get-url origin >/dev/null 2>&1
    # 0 if it has a remote, non-zero if it does not have a remote
    GIT_RESULT=$?
    if [[ $NEGATE -eq 0 ]]; then
        exit $GIT_RESULT
    else
        if [[ $GIT_RESULT -eq 0 ]]; then
            exit 1
        else
            exit 0
        fi
    fi
fi

if [[ $HAS_TAGS -eq 1 ]]; then
    # special, does not combine with others
    git -C $1 tag | grep -q v
    # 0 if it has tags, non-zero if it does not have tags
    GIT_RESULT=$?
    if [[ $NEGATE -eq 0 ]]; then
        exit $GIT_RESULT
    else
        if [[ $GIT_RESULT -eq 0 ]]; then
            exit 1
        else
            exit 0
        fi
    fi
fi

if [[ ! -f "$1/pom.xml" ]]; then
    exit 2
fi

for pom_token in "${POM_TOKENS[@]}"; do
    grep -q "$pom_token" "$1/pom.xml"
    GREP_RESULT=$?
    if [[ $NEGATE -eq 0 && $GREP_RESULT -ne 0 || $NEGATE -ne 0 && $GREP_RESULT -eq 0 ]]; then
        exit 3
    fi
done
