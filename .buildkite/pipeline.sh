#!/usr/bin/env bash
### Build and quick lint
set -euo pipefail

DOCKER_VERSION="v2.2.0"
DOCKER_IMAGE="batfish/ci-base:latest"

cat <<EOF
steps:
EOF

###### WAIT before starting any of the jobs.
cat <<EOF
  - wait
EOF

###### Initial checks plus building the jar
cat <<EOF
  - label: "Check Java formatting"
    command: ".buildkite/check_java_format.sh"
    plugins:
      - docker#${DOCKER_VERSION}:
          image: ${DOCKER_IMAGE}
  - label: "Check Python templates"
    command:
      - "python3 -m virtualenv .venv"
      - ". .venv/bin/activate"
      - "python3 -m pip install pytest"
      - "cd tests && pytest"
    plugins:
      - docker#${DOCKER_VERSION}:
          image: ${DOCKER_IMAGE}
  - label: "Build"
    command:
      - "mkdir workspace"
      - "mvn -f projects package"
      - "cp projects/allinone/target/allinone-bundle-*.jar workspace/allinone.jar"
    artifact_paths:
      - workspace/allinone.jar
    plugins:
      - docker#${DOCKER_VERSION}:
          image: ${DOCKER_IMAGE}
EOF

###### WAIT for job to be build before starting any heavier tests
cat <<EOF
  - wait
EOF

###### Maven tests
cat <<EOF
  - label: "Maven tests"
    command: "mvn -f projects/pom.xml verify -DskipTests=false"
    plugins:
      - docker#${DOCKER_VERSION}:
          image: ${DOCKER_IMAGE}
  - label: "Maven checkstyle"
    command: "mvn -f projects/pom.xml verify -Dcheckstyle.skip=false"
    plugins:
      - docker#${DOCKER_VERSION}:
          image: ${DOCKER_IMAGE}
  - label: "Maven dependency analysis"
    command: "mvn -f projects/pom.xml verify -Dmdep.analyze.skip=false"
    plugins:
      - docker#${DOCKER_VERSION}:
          image: ${DOCKER_IMAGE}
  - label: "Maven findbugs"
    command: "mvn -f projects/pom.xml verify -Dfindbugs.skip=false"
    plugins:
      - docker#${DOCKER_VERSION}:
          image: ${DOCKER_IMAGE}
  - label: "Maven javadoc"
    command: "mvn -f projects/pom.xml verify -Dmaven.javadoc.skip=false"
    plugins:
      - docker#${DOCKER_VERSION}:
          image: ${DOCKER_IMAGE}
  - label: "Maven pmd"
    command: "mvn -f projects/pom.xml verify -Dpmd.skip=false"
    plugins:
      - docker#${DOCKER_VERSION}:
          image: ${DOCKER_IMAGE}
EOF

###### Ref tests
# TODO: consider parallel builds for this?
# https://buildkite.com/docs/tutorials/parallel-builds#parallel-jobs
for cmd in tests/*/commands; do
  cat <<EOF
  - label: "${cmd} ref tests"
    command: ".buildkite/ref_test.sh ${cmd}"
    plugins:
      - docker#${DOCKER_VERSION}:
          image: ${DOCKER_IMAGE}
      - artifacts#v1.2.0:
          download: workspace/allinone.jar
EOF
done
