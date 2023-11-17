#!/bin/bash

set -euo pipefail

CMD="bazelisk"
if ! type "${CMD}" &> /dev/null; then
  echo "This script works better with bazelisk. Use 'go get github.com/bazelbuild/bazelisk' to get it.'"
  echo
  CMD="bazel"
fi

if [ "${1-}" = "-d" ]
then
  DEBUG="--jvm_flag=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5009 --jvm_flag=-ea"
else
  DEBUG=
fi


${CMD} build //projects/allinone:allinone_main
./bazel-bin/projects/allinone/allinone_main \
    --jvm_flag=-Xmx12g \
    --jvm_flag=-Dlog4j2.configurationFile=tools/log4j2.yaml \
    ${DEBUG} \
    -runclient false \
    -coordinatorargs "-templatedirs ./questions"
