#!/bin/bash

set -euo pipefail

CMD="bazelisk"
if ! type "${CMD}" &> /dev/null; then
  echo "This script works better with bazelisk. Use 'go get github.com/bazelbuild/bazelisk' to get it.'"
  echo
  CMD="bazel"
fi

${CMD} build //projects/allinone:allinone_main && ./bazel-bin/projects/allinone/allinone_main --jvm_flag=-Xmx12g -runclient false -coordinatorargs "-templatedirs ./questions"
