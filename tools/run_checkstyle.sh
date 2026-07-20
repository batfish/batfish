#!/usr/bin/env bash

set -euo pipefail

# Checkstyle runs as a Bazel target so the jar is resolved via Maven rather
# than downloaded ad hoc. Any extra arguments are passed through as
# additional paths to check.
exec bazel run //tools/checkstyle:checkstyle.check -- "$@"
