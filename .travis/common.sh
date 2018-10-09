#!/usr/bin/env bash
if [[ "$(uname)" == 'Darwin' && -n "$(which gfind)" ]]; then
   GNU_FIND=gfind
else
   GNU_FIND=find
fi

JACOCO_VERSION=0.8.2
JACOCO_AGENT_JAR_NAME="org.jacoco.agent-${JACOCO_VERSION}-runtime.jar"
JACOCO_AGENT_JAR="${HOME}/.m2/repository/org/jacoco/org.jacoco.agent/${JACOCO_VERSION}/${JACOCO_AGENT_JAR_NAME}"
JACOCO_CLI_JAR_NAME="org.jacoco.cli-${JACOCO_VERSION}-nodeps.jar"
JACOCO_CLI_JAR="${HOME}/.m2/repository/org/jacoco/org.jacoco.cli/${JACOCO_VERSION}/${JACOCO_CLI_JAR_NAME}"

# the destfile for ref tests
JACOCO_REF_DESTFILE=projects/target/jacoco-ref.exec

# the destfile for the aggregate of all junit project tests
JACOCO_JUNIT_DESTFILE=projects/target/jacoco-junit.exec

# the destfile for the aggregate of the ref tests and all junit project tests
JACOCO_ALL_DESTFILE=projects/target/jacoco-all.exec

JACOCO_COVERAGE_REPORT_XML=coverage.xml
JACOCO_COVERAGE_REPORT_HTML=coverage

if [ ! -f "${JACOCO_AGENT_JAR}" ]; then
  mvn dependency:get -Dartifact=org.jacoco:org.jacoco.agent:${JACOCO_VERSION}:jar:runtime
fi

if [ ! -f "${JACOCO_CLI_JAR}" ]; then
  mvn dependency:get -Dartifact=org.jacoco:org.jacoco.cli:${JACOCO_VERSION}:jar:nodeps
fi

JACOCO_CLASSES_DIR="$(mktemp -d)"
