#!/usr/bin/env bash

set -e

if [[ $(uname) == 'Darwin' ]]; then
   GNU_FIND=gfind
else
   GNU_FIND=find
fi

GJF_VERSION=1.7
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
  git diff --cached --name-only --diff-filter=ACMR | grep ".*java$" > files_to_check
else
  # All java files in the project
  ${GNU_FIND} projects -regex '.*/src/main/.*\.java' -or -regex '.*/src/test/.*\.java' > files_to_check
fi

# On travis, print affected filenames and fail. Locally, just fix the files.
if [[ -z ${TRAVIS} ]]; then
  FORMAT_ARGS="--replace"
  echo "Fixing all misformatted java files"
else
  FORMAT_ARGS="--dry-run --set-exit-if-changed"
  echo "Checking that all java files are formatted correctly"
fi

# Run the check
java -jar ${JAR} ${FORMAT_ARGS} @files_to_check && rm files_to_check \
  || (echo -e "\nThe files listed above are not formatted correctly. Use $0 to fix these issues. We recommend you install the Eclipse or IntelliJ plugin for google-java-format version ${GJF_VERSION}." && exit 1)
