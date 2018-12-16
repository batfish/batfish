#!/usr/bin/env bash
set -e
set -x
if [ -n "${BUILDKITE}" ]; then
  export PATH="$PATH:/root/workdir/apache-maven-3.6.0/bin"
  export JAVA_HOME="/usr/lib/jvm/java-8-openjdk-amd64"
fi
mvn -f projects verify -P '!fast'

