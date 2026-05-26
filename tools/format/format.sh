#!/usr/bin/env bash
# Runs google-java-format against Java sources.
#
# Mode is bound by the //tools/format:format.{check,fix} target's args=.
# Remaining arguments are file paths. If no file paths are given the
# whole tree is scanned; pre-commit always passes the changed paths so
# this stays fast there.

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

GJF="$(rlocation _main/tools/format/google_java_format)"
if [ -z "${GJF}" ] || [ ! -x "${GJF}" ]; then
  echo "ERROR: cannot locate google_java_format launcher in runfiles" >&2
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

MODE="${1:-}"
shift || true
case "${MODE}" in
  check) GJF_ARGS=(--dry-run --set-exit-if-changed) ;;
  fix)   GJF_ARGS=(--replace) ;;
  *) echo "ERROR: unknown mode '${MODE}' (expected 'check' or 'fix')" >&2; exit 2 ;;
esac

# `bazel run` sets BUILD_WORKING_DIRECTORY to the caller's cwd; resolve
# any relative file arguments against it so we can leave the runfiles
# environment intact for the java launcher.
WORKDIR="${BUILD_WORKING_DIRECTORY:-$(pwd)}"
if [ "$#" -gt 0 ]; then
  FILES=()
  for f in "$@"; do
    case "${f}" in
      /*) FILES+=("${f}") ;;
      *)  FILES+=("${WORKDIR}/${f}") ;;
    esac
  done
else
  # No paths given: scan all sources. Some macOS users have GNU find as gfind.
  if [ "$(uname)" = "Darwin" ] && [ -x "$(command -v gfind)" ]; then
    GNU_FIND=gfind
  else
    GNU_FIND=find
  fi
  mapfile -t FILES < <(cd "${WORKDIR}" && ${GNU_FIND} "${WORKDIR}/projects" \
    -regex '.*/src/main/.*\.java' -or -regex '.*/src/test/.*\.java')
fi

"${GJF}" "${GJF_ARGS[@]}" "${FILES[@]}"
STATUS=$?

if [ "${MODE}" = "check" ] && [ "${STATUS}" -ne 0 ]; then
  cat >&2 <<'EOF'

The files listed above are not formatted correctly. Run
  bazel run //tools/format:format.fix
to fix all sources, or
  bazel run //tools/format:format.fix -- path/to/File.java
to fix a specific file. We recommend installing the IntelliJ
google-java-format plugin (matching the version pinned in MODULE.bazel).

To never have to deal with this again, enable Batfish's pre-commit
integration: https://pre-commit.com/#install
EOF
fi
exit "${STATUS}"
