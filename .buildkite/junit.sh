#!/usr/bin/env bash
set -e
set -x
export PATH="$PATH:/root/workdir/apache-maven-3.6.0/bin"
export JAVA_HOME="/usr/lib/jvm/java-8-openjdk-amd64"
mvn -f projects verify -P '!fast'

