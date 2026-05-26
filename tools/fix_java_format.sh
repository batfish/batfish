#!/usr/bin/env bash
# Pre-commit wrapper for //tools/format:format.fix.
#
# Writes the file list to an @argfile and passes only that single arg
# through `bazel run`. Avoids ARG_MAX (E2BIG) when bazel's launcher
# execvs /bin/bash on commits that touch thousands of .java files.

set -euo pipefail

REPO_ROOT="$(git rev-parse --show-toplevel)"
ARGFILE="$(mktemp -t batfish-format-XXXXXX)"
trap 'rm -f "${ARGFILE}"' EXIT

# Pre-commit passes paths relative to the repo root; absolutize them so
# bazel run (which changes cwd) can resolve them.
for f in "$@"; do
  case "${f}" in
    /*) printf '%s\n' "${f}" ;;
    *)  printf '%s/%s\n' "${REPO_ROOT}" "${f}" ;;
  esac
done > "${ARGFILE}"

cd "${REPO_ROOT}"
bazel --noblock_for_lock run //tools/format:format.fix -- "@${ARGFILE}"
