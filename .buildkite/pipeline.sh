#!/usr/bin/env bash
### Build and quick lint
set -e
cat <<EOF
steps:
  - label: look around env
    command:
      - whoami
      - ls -lat
      - getent group
      - getent passwd
      - cat /etc/docker/daemon.json
      - ls /etc/docker
  - label: "Check Java formatting"
    command:
      - ls -latr
      - whoami
      - ".buildkite/check_java_format.sh"
    plugins:
      - docker#v2.2.0:
          image: "batfish/ci-base:latest"
          debug: true
  - label: "Check Python templates"
    command:
      - "python3 -m virtualenv .venv"
      - ". .venv/bin/activate"
      - "python3 -m pip install pytest"
      - "cd tests && pytest"
    plugins:
      - docker#v2.2.0:
          image: "batfish/ci-base:latest"
  - label: "Build"
    command:
      - "mkdir workspace"
      - "mvn -f projects package"
      - "cp projects/allinone/target/allinone-bundle-*.jar workspace/allinone.jar"
    artifact_paths:
      - workspace/allinone.jar
    plugins:
      - docker#v2.2.0:
          image: "batfish/ci-base:latest"
  - wait
EOF
