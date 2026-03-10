#!/bin/bash

set -euo pipefail

if [[ $(bazel version) != *azelisk* ]]; then
  echo "The recommended way to use bazel is to install bazelisk as the bazel command."
fi

if [ "${1-}" = "-d" ]
then
  DEBUG="--jvm_flag=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5009 --jvm_flag=-ea"
else
  DEBUG=
fi


bazel build //projects/allinone:allinone_main
./bazel-bin/projects/allinone/allinone_main \
    --jvm_flag=-Xmx12g \
    --jvm_flag=-Dlog4j2.configurationFile=tools/log4j2.yaml \
    ${DEBUG} \
    -runclient false \
    -coordinatorargs "-templatedirs ./questions"
