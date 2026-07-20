#!/usr/bin/env bash
# Runs checkstyle against the Java sources.
#
# Invoked via `bazel run //tools/checkstyle:checkstyle.check`. Any extra
# arguments are passed through to checkstyle as additional paths to check;
# with no arguments the default source trees are scanned.

set -o pipefail

# --- begin runfiles.bash initialization v2 ---
f=bazel_tools/tools/bash/runfiles/runfiles.bash
source "${RUNFILES_DIR:-/dev/null}/$f" 2>/dev/null || \
  source "$(grep -sm1 "^$f " "${RUNFILES_MANIFEST_FILE:-/dev/null}" | cut -f2- -d' ')" 2>/dev/null || \
  source "$0.runfiles/$f" 2>/dev/null || \
  source "$(grep -sm1 "^$f " "$0.runfiles_manifest" | cut -f2- -d' ')" 2>/dev/null || \
  source "$(grep -sm1 "^$f " "$0.exe.runfiles_manifest" | cut -f2- -d' ')" 2>/dev/null || \
  { echo>&2 "ERROR: cannot find $f"; exit 1; }
# --- end runfiles.bash initialization v2 ---

CHECKSTYLE="$(rlocation _main/tools/checkstyle/checkstyle)"
if [ -z "${CHECKSTYLE}" ] || [ ! -x "${CHECKSTYLE}" ]; then
  echo "ERROR: cannot locate checkstyle launcher in runfiles" >&2
  exit 1
fi
# The java_binary launcher looks for its own runfiles tree next to its
# script, which doesn't exist when nested inside another binary's
# runfiles. Point it at ours.
if [ -n "${RUNFILES_DIR:-}" ]; then
  export JAVA_RUNFILES="${RUNFILES_DIR}"
elif [ -d "${0}.runfiles" ]; then
  export JAVA_RUNFILES="${0}.runfiles"
fi

# checkstyle reads its config and the source trees from the workspace, not
# from runfiles. `bazel run` sets BUILD_WORKSPACE_DIRECTORY to the workspace
# root; fall back to the current directory otherwise.
WORKSPACE="${BUILD_WORKSPACE_DIRECTORY:-$(pwd)}"
cd "${WORKSPACE}"

if [ "$#" -gt 0 ]; then
  PATHS=("$@")
else
  PATHS=(projects)
fi

"${CHECKSTYLE}" -c projects/checkstyle.xml "${PATHS[@]}" --exclude projects/bdd
