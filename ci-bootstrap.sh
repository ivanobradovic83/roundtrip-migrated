#!/usr/bin/env bash

export CI_BUILD=true
git submodule update --init -- ci
$(dirname "$0")/ci/ci-wrapper.sh "$@"

