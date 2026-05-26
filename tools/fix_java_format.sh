#!/usr/bin/env bash

set -euo pipefail

# Locate google-java-format jar. Built via Bazel from the @maven repo so
# there is no network dependency at script-run time. The version is set
# in MODULE.bazel.
# Callers may pre-build the jar and pass its path via $GJF_JAR to skip the
# bazel invocation.
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
WORKSPACE_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
JAR="${GJF_JAR:-}"
if [ -z "${JAR}" ]; then
  (cd "${WORKSPACE_ROOT}" && bazel build //tools/format:google_java_format_jar)
  JAR="$(cd "${WORKSPACE_ROOT}" && bazel info bazel-bin)/tools/format/google-java-format.jar"
fi
if [ ! -f "${JAR}" ]; then
  echo "google-java-format jar not found at ${JAR}" >&2
  exit 1
fi

# Some OS X users have installed GNU find as gfind.
if [ "$(uname)" = "Darwin" ] && [ -x "$(command -v gfind)" ]; then
  GNU_FIND=gfind
else
  GNU_FIND=find
fi

# Collect all the arguments passed in by CLI or pre-commit.
all_args=("$@")

# If no args are provided user is running manually, so find all Java files and fix all issues.
if [ ${#all_args[@]} -eq 0 ]; then
    echo "--replace" > args_and_files
    ${GNU_FIND} projects -regex '.*/src/main/.*\.java' -or -regex '.*/src/test/.*\.java' >> args_and_files
# If the --check arg is provided, this is a CI run. Find all files, but fail instead of fixing.
elif [ ${all_args[0]} = "--check" ]; then
    echo "--dry-run --set-exit-if-changed" > args_and_files
    ${GNU_FIND} projects -regex '.*/src/main/.*\.java' -or -regex '.*/src/test/.*\.java' >> args_and_files
# Args were provided by pre-commit
else
    echo "${all_args[@]}" > args_and_files
fi

# Run the check
java -jar ${JAR} @args_and_files || FAIL="fail"
rm -f args_and_files
if [ "${FAIL:-}" = "fail" ]; then
  echo -e "\nThe files listed above are not formatted correctly. Use $0 to fix these issues. We recommend you install the IntelliJ plugin for google-java-format (matching the version pinned in MODULE.bazel)."
  echo
  echo "To never have to deal with this again, enable Batfish's pre-commit integration: https://pre-commit.com/#install"
  exit 1
fi
