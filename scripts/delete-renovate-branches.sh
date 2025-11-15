#!/usr/bin/env bash

REPO=$1
git -C $REPO branch -la \
    | grep renovate \
    | sed -e 's|remotes/origin/||' \
    | awk '{$1=$1};1' \
    | xargs -n1 git -C $REPO push origin --delete
