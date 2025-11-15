#!/usr/bin/env bash
#
# Use with find in this way:
#
# find . -type d -depth 1 -exec ./java/scripts/test-repo.sh {} \; -print

POM_TOKENS=()
NEGATE=0

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
    -c | --custom )
        shift; POM_TOKENS+=($1)
        ;;
    -n )
        NEGATE=1
        ;;
esac; shift; done
if [[ "$1" == '--' ]]; then shift; fi

if [[ ! -d "$1/.git" ]]; then
    exit 1
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
