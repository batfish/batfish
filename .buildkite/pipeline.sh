#!/usr/bin/env bash
### Build and quick lint
set -euo pipefail

DOCKER_VERSION="v2.2.0"
DOCKER_IMAGE="batfish/ci-base:latest"

cat <<EOF
steps:
  - wait
  - label: "Check Java formatting"
    command: ".buildkite/check_java_format.sh"
    plugins:
      - docker#${DOCKER_VERSION}:
          image: ${DOCKER_IMAGE}
          debug: true
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
  - wait
EOF
