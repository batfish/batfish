#!/usr/bin/env bash

set -euo pipefail

# Find the jar, and download it if needed.
GJF_VERSION=1.25.1
JAR_NAME="google-java-format-${GJF_VERSION}-all-deps.jar"
JAR_URL="https://github.com/google/google-java-format/releases/download/v${GJF_VERSION}/${JAR_NAME}"
JAR_DIR="${HOME}/.cache/google-java-format"
JAR="${JAR_DIR}/${JAR_NAME}"
if [ ! -f ${JAR} ]; then
  mkdir -p "${JAR_DIR}"
  wget "${JAR_URL}" -O "${JAR}"
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
  echo -e "\nThe files listed above are not formatted correctly. Use $0 to fix these issues. We recommend you install the IntelliJ plugin for google-java-format version ${GJF_VERSION}."
  echo
  echo "To never have to deal with this again, enable Batfish's pre-commit integration: https://pre-commit.com/#install"
  exit 1
fi
