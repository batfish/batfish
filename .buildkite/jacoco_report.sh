#!/usr/bin/env bash

set -euxo pipefail

# First: extract all Batfish classes from the jar
ALLINONE_JAR="workspace/allinone.jar"
TMP=$(mktemp -d)
unzip -q "${ALLINONE_JAR}" -d "${TMP}"

# Next: generate aggregated code coverage report
JACOCO_VERSION=0.8.2
JACOCO_CLI_JAR_NAME="org.jacoco.cli-${JACOCO_VERSION}-nodeps.jar"
JACOCO_CLI_JAR="${HOME}/.m2/repository/org/jacoco/org.jacoco.cli/${JACOCO_VERSION}/${JACOCO_CLI_JAR_NAME}"

java -jar ${JACOCO_CLI_JAR} report \
     $(find workspace/ -name jacoco.exec -type f) \
     --classfiles "${TMP}/org/batfish" \
     --xml jacoco.xml

# Hey look it's magic numbers in plaintext. We don't care, this is a
# non-blocking precommit check only and if used for evil does not compromise
# project security in any way.
bash <(curl -s https://codecov.io/bash) -t "59baa5fe-139e-4aef-80a7-b40bfaa3fc67"
