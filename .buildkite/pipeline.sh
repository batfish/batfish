#!/usr/bin/env bash
### Build and quick lint
set -euo pipefail

BATFISH_ARTIFACTS_PLUGIN_VERSION="${BATFISH_ARTIFACTS_PLUGIN_VERSION:-v1.2.0}"
BATFISH_DOCKER_PLUGIN_VERSION="${BATFISH_DOCKER_PLUGIN_VERSION:-v2.2.0}"
BATFISH_DOCKER_CI_BASE_IMAGE="${BATFISH_DOCKER_CI_BASE_IMAGE:-batfish/ci-base:latest}"

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
    command: "tools/fix_java_format.sh --check"
    plugins:
      - docker#${BATFISH_DOCKER_PLUGIN_VERSION}:
          image: ${BATFISH_DOCKER_CI_BASE_IMAGE}
          always-pull: true
  - label: "Check Python templates"
    command:
      - "python3 -m virtualenv .venv"
      - ". .venv/bin/activate"
      - "python3 -m pip install pytest"
      - "cd tests && pytest"
    plugins:
      - docker#${BATFISH_DOCKER_PLUGIN_VERSION}:
          image: ${BATFISH_DOCKER_CI_BASE_IMAGE}
          always-pull: true
  - label: "Build"
    command:
      - "mkdir workspace"
      - "mvn -f projects package"
      - "cp projects/allinone/target/allinone-bundle-*.jar workspace/allinone.jar"
    artifact_paths:
      - workspace/allinone.jar
    plugins:
      - docker#${BATFISH_DOCKER_PLUGIN_VERSION}:
          image: ${BATFISH_DOCKER_CI_BASE_IMAGE}
          always-pull: true
EOF

###### WAIT for jar to be built and format checks to pass before heavier tests
cat <<EOF
  - wait
EOF

###### Maven tests
cat <<EOF
  - label: "Maven tests + Coverage"
    command:
      - mvn -f projects/pom.xml test -DskipTests=false -Djacoco.skip=false
      - mkdir -p workspace
      - rsync -zarv --prune-empty-dirs --include '*/' --include 'jacoco*.exec' --exclude '*' projects/ workspace/
    artifact_paths:
      - workspace/**/jacoco.exec
    plugins:
      - docker#${BATFISH_DOCKER_PLUGIN_VERSION}:
          image: ${BATFISH_DOCKER_CI_BASE_IMAGE}
          always-pull: true
  - label: "Maven checkstyle"
    command: "mvn -f projects/pom.xml compile checkstyle:checkstyle -Dcheckstyle.skip=false"
    plugins:
      - docker#${BATFISH_DOCKER_PLUGIN_VERSION}:
          image: ${BATFISH_DOCKER_CI_BASE_IMAGE}
          always-pull: true
  - label: "Maven dependency analysis"
    command: "mvn -f projects/pom.xml verify -Dmdep.analyze.skip=false"
    plugins:
      - docker#${BATFISH_DOCKER_PLUGIN_VERSION}:
          image: ${BATFISH_DOCKER_CI_BASE_IMAGE}
          always-pull: true
  - label: "Maven findbugs"
    command: "mvn -f projects/pom.xml verify -Dfindbugs.skip=false"
    plugins:
      - docker#${BATFISH_DOCKER_PLUGIN_VERSION}:
          image: ${BATFISH_DOCKER_CI_BASE_IMAGE}
          always-pull: true
  - label: "Maven javadoc"
    command: "mvn -f projects/pom.xml verify -Dmaven.javadoc.skip=false"
    plugins:
      - docker#${BATFISH_DOCKER_PLUGIN_VERSION}:
          image: ${BATFISH_DOCKER_CI_BASE_IMAGE}
          always-pull: true
  - label: "Maven pmd"
    command: "mvn -f projects/pom.xml verify -Dpmd.skip=false"
    plugins:
      - docker#${BATFISH_DOCKER_PLUGIN_VERSION}:
          image: ${BATFISH_DOCKER_CI_BASE_IMAGE}
          always-pull: true
EOF

###### Ensure the code still compiles with Bazel
cat <<EOF
  - label: "Bazel compilation"
    command: "bazel build -- //... -projects:javadoc"
    plugins:
      - docker#${BATFISH_DOCKER_PLUGIN_VERSION}:
          image: ${BATFISH_DOCKER_CI_BASE_IMAGE}
          always-pull: true
EOF

###### Ref tests
# TODO: consider parallel builds for this?
# https://buildkite.com/docs/tutorials/parallel-builds#parallel-jobs
for cmd in $(find tests -name commands); do
  cat <<EOF
  - label: "${cmd} ref tests"
    command: ".buildkite/ref_test.sh ${cmd}"
    plugins:
      - docker#${BATFISH_DOCKER_PLUGIN_VERSION}:
          image: ${BATFISH_DOCKER_CI_BASE_IMAGE}
          always-pull: true
      - artifacts#${BATFISH_ARTIFACTS_PLUGIN_VERSION}:
          download: workspace/allinone.jar
    artifact_paths:
      - workspace/**/jacoco.exec
EOF
done

###### Code coverage
cat <<EOF
  - wait
  - label: "Code coverage"
    command:
      - ".buildkite/jacoco_report.sh"
    plugins:
      - docker#${BATFISH_DOCKER_PLUGIN_VERSION}:
          image: "${BATFISH_DOCKER_CI_BASE_IMAGE}"
          always-pull: true
          propagate-environment: true
      - artifacts#${BATFISH_ARTIFACTS_PLUGIN_VERSION}:
          download:
            - "workspace/**/jacoco.exec"
            - "workspace/allinone.jar"
EOF
