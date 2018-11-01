#!/usr/bin/env bash

set -e

if [[ $(uname) == 'Darwin' ]]; then
   GNU_FIND=gfind
else
   GNU_FIND=find
fi

GJF_VERSION=1.6
JAR_NAME="google-java-format-${GJF_VERSION}-all-deps.jar"
JAR_SUFFIX=".m2/repository/com/google/googlejavaformat/google-java-format/${GJF_VERSION}/${JAR_NAME}"
JAR="${HOME}/${JAR_SUFFIX}"
if [[ $(uname) == *"CYGWIN"* ]]; then
   JAR="${USERPROFILE}\\$(cygpath -w "${JAR_SUFFIX}")"
fi
if [ ! -f ${JAR} ]; then
  mvn dependency:get -Dartifact=com.google.googlejavaformat:google-java-format:${GJF_VERSION}:jar:all-deps
fi

if [ "$1" = "--diff" ]
then
  # All java files in the diff
  FILES_TO_CHECK=$(git diff --cached --name-only --diff-filter=ACMR | grep ".*java$" || true)
else
  # All java files in the project
  FILES_TO_CHECK=$(${GNU_FIND} projects -regex '.*/src/main/.*\.java' -or -regex '.*/src/test/.*\.java')
fi
FILES_TO_CHECK=(${FILES_TO_CHECK})
NUM_FILES_TO_CHECK=${#FILES_TO_CHECK[@]}

# Exit early if no files to check
if [[ 0 -eq ${NUM_FILES_TO_CHECK} ]]
then
  exit 0
fi

# On travis, print affected filenames and fail. Locally, just fix the files.
if [[ -z ${TRAVIS} ]]; then
  FORMAT_ARGS="--replace"
  echo "Fixing ${NUM_FILES_TO_CHECK} misformatted java files"
else
  FORMAT_ARGS="--dry-run --set-exit-if-changed"
  echo "Checking that all java files are formatted correctly"
fi

# Run the check
java -jar ${JAR} ${FORMAT_ARGS} ${FILES_TO_CHECK[@]} \
  || (echo -e "\nThe files listed above are not formatted correctly. Use $0 to fix these issues. We recommend you install the Eclipse or IntelliJ plugin for google-java-format version ${GJF_VERSION}." && exit 1)
