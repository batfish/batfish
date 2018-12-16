#!/usr/bin/env bash

set -e
set -x

ALLINONE_JAR="workspace/allinone.jar"
MAIN_CLASS="org.batfish.allinone.Main"

if [ $# -lt 1 -o $# -gt 2 ]; then
  echo "Usage: $0 <cmdfile> [coordinator_args]" 1>&2
  exit 1
fi

CMD_FILE="$1"

if [ -n "$2" ]; then
  COORDINATOR_ARGS="$2"
else
  COORDINATOR_ARGS="-periodassignworkms 5"
fi

java -cp "${ALLINONE_JAR}" "${MAIN_CLASS}" -coordinatorargs="${COORDINATOR_ARGS}" -cmdfile "${CMD_FILE}"

