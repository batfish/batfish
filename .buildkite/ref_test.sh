#!/usr/bin/env bash

set -euxo pipefail

ALLINONE_JAR="workspace/allinone.jar"
MAIN_CLASS="org.batfish.allinone.Main"

if [ $# -lt 1 -o $# -gt 2 ]; then
  echo "Usage: $0 <cmdfile> [coordinator_args]" 1>&2
  exit 1
fi

JACOCO_VERSION=0.8.2
JACOCO_AGENT_JAR_NAME="org.jacoco.agent-${JACOCO_VERSION}-runtime.jar"
JACOCO_AGENT_JAR="${HOME}/.m2/repository/org/jacoco/org.jacoco.agent/${JACOCO_VERSION}/${JACOCO_AGENT_JAR_NAME}"

CMD_FILE="$1"
JACOCO_FILE="workspace/${CMD_FILE}/jacoco.exec"

if [ -n "${2:-}" ]; then
  COORDINATOR_ARGS="$2"
else
  COORDINATOR_ARGS="-periodassignworkms 5"
fi

java -enableassertions \
     "-javaagent:${JACOCO_AGENT_JAR}=destfile=${JACOCO_FILE}" \
     -cp "${ALLINONE_JAR}" \
     "${MAIN_CLASS}" \
     -coordinatorargs="${COORDINATOR_ARGS}" -cmdfile="${CMD_FILE}"

