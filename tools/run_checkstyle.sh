#!/usr/bin/env bash

set -euo pipefail

CHECKSTYLE_VERSION=10.12.3
JAR_NAME="checkstyle-${CHECKSTYLE_VERSION}-all.jar"
JAR_URL="https://github.com/checkstyle/checkstyle/releases/download/checkstyle-${CHECKSTYLE_VERSION}/${JAR_NAME}"

# Check cache, and and download+cache jar if not present.
JAR_DIR="${HOME}/.cache/checkstyle"
JAR="${JAR_DIR}/${JAR_NAME}"
if [ ! -f ${JAR} ]; then
  mkdir -p "${JAR_DIR}"
  wget "${JAR_URL}" -O "${JAR}"
fi

# Run the check
java -jar ${JAR} -c projects/checkstyle.xml projects --exclude projects/bdd
