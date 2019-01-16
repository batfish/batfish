#!/usr/bin/env bash

set -e

GJF_VERSION=1.6
JAR_NAME="google-java-format-${GJF_VERSION}-all-deps.jar"
JAR_SUFFIX=".m2/repository/com/google/googlejavaformat/google-java-format/${GJF_VERSION}/${JAR_NAME}"
JAR="${HOME}/${JAR_SUFFIX}"
if [ ! -f ${JAR} ]; then
  mvn dependency:get -Dartifact=com.google.googlejavaformat:google-java-format:${GJF_VERSION}:jar:all-deps
fi

# All java files in the project
${GNU_FIND} projects -regex '.*/src/main/.*\.java' -or -regex '.*/src/test/.*\.java' > files_to_check

# On travis, print affected filenames and fail. Locally, just fix the files.
FORMAT_ARGS="--dry-run --set-exit-if-changed"
echo "Checking that all java files are formatted correctly"

# Run the check
java -jar ${JAR} ${FORMAT_ARGS} @files_to_check && rm files_to_check \
  || (echo -e "\nThe files listed above are not formatted correctly. Use $0 to fix these issues. We recommend you install the Eclipse or IntelliJ plugin for google-java-format version ${GJF_VERSION}." && exit 1)
