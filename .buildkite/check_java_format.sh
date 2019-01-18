#!/usr/bin/env bash

set -euo pipefail

GJF_VERSION=1.7
JAR_NAME="google-java-format-${GJF_VERSION}-all-deps.jar"
JAR_SUFFIX=".m2/repository/com/google/googlejavaformat/google-java-format/${GJF_VERSION}/${JAR_NAME}"
JAR="${HOME}/${JAR_SUFFIX}"
if [ ! -f ${JAR} ]; then
  mvn dependency:get -Dartifact=com.google.googlejavaformat:google-java-format:${GJF_VERSION}:jar:all-deps
fi

# All java files in the project
find projects -regex '.*/src/main/.*\.java' -or -regex '.*/src/test/.*\.java' > files_to_check

# Run the check
echo "Checking that all java files are formatted correctly"
java -jar ${JAR} --dry-run --set-exit-if-changed @files_to_check && rm files_to_check \
  || (echo -e "\nThe files listed above are not formatted correctly. Use $0 to fix these issues. We recommend you install the Eclipse or IntelliJ plugin for google-java-format version ${GJF_VERSION}." && exit 1)
