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
FILES_TO_CHECK=$(find projects -regex '.*/src/main/.*\.java' -or -regex '.*/src/test/.*\.java')

FORMAT_ARGS="--dry-run --set-exit-if-changed"
echo "Checking that all java files are formatted correctly"

# Run the check
java -jar ${JAR} ${FORMAT_ARGS} ${FILES_TO_CHECK} \
  || (echo -e "\nThe files listed above are not formatted correctly. Use .travis/fix_java_format.sh to fix these issues. We recommend you install the Eclipse or IntelliJ plugin for google-java-format version ${GJF_VERSION}." && exit 1)
