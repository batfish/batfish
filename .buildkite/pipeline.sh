#!/usr/bin/env bash
### Build and quick lint
set -e
cat <<EOF
steps:
  - label: "Java formatting"
    command: ".buildkite/check_java_format.sh"
    plugins:
      - docker#v1.1.1:
          image: "dhalperi/build-base:latest"
  - label: "Python templates"
    command: "cd tests && python3 -m pytest"
    plugins:
      - docker#v1.1.1:
          image: "dhalperi/build-base:latest"
  - label: "Build"
    command:
      - "mkdir workspace"
      - "mvn -f projects install"
      - "cp projects/allinone/target/allinone-bundle-*.jar workspace/allinone.jar"
      - "tar -cf workspace/workdir.tar \$(find -maxdepth 1 -mindepth 1 -not -name '.*' -not -name workspace)"
      - "tar -cf workspace/mvn-repo.tar -C /root/.m2/repository/org/batfish ."
      - "tar -cf workspace/questions.tar questions"
    artifact_paths:
      - "workspace/*"
    plugins:
      - docker#v1.1.1:
          image: "dhalperi/build-base:latest"
  - wait
EOF
### maven javadoc, mpd, tests
for proj in allinone batfish batfish-client batfish-common-protocol coordinator question; do
  for prof in javadoc pmd tests; do
    cat <<EOF
  - label: "${proj} ${prof}"
    command:
      - "tar -x --no-same-owner -f workspace/workdir.tar"
      - "mkdir -p /root/.m2/repository/org/batfish"
      - "tar -x --no-same-owner -f workspace/mvn-repo.tar -C /root/.m2/repository/org/batfish"
      - "mvn -f projects -pl allinone install -P '!fast,${prof}'"
    plugins:
      - docker#v1.1.1:
          image: "dhalperi/build-base:latest"
      - artifacts#v1.2.0:
          download:
            - "workspace/workdir.tar"
            - "workspace/mvn-repo.tar"
EOF
  done
done
### maven checkstyle, findbugs, depedency analysis
cat <<EOF
  - label: "maven lint checks"
    command:
      - "tar -x --no-same-owner -f workspace/workdir.tar"
      - "mkdir -p /root/.m2/repository/org/batfish"
      - "tar -x --no-same-owner -f workspace/mvn-repo.tar -C /root/.m2/repository/org/batfish"
      - "mvn -f projects install -P '!fast,lint'"
    plugins:
      - docker#v1.1.1:
          image: "dhalperi/build-base:latest"
      - artifacts#v1.2.0:
          download:
            - "workspace/workdir.tar"
            - "workspace/mvn-repo.tar"
EOF
### Ref tests
for testdir in aws basic java-smt jsonpath-addons jsonpathtotable parsing-errors-tests parsing-tests questions/experimental questions/stable roles ui-focused; do
  cat <<EOF
  - label: "${testdir} ref tests"
    command: ".buildkite/ref_test.sh tests/${testdir}/commands"
    plugins:
      - docker#v1.1.1:
          image: "dhalperi/build-base:latest"
      - artifacts#v1.2.0:
          download: "workspace/allinone.jar"
EOF
done

### Trigger docker tests
cat <<EOF
  - wait
  - label: "Trigger batfish-docker build"
    trigger: "batfish-docker"
    branches: "master"
    build:
      env:
        BATFISH_TAG: "\$(git rev-parse --short HEAD)"
        BATFISH_VERSION: "\$(grep -1 batfish-parent 'projects/pom.xml' | grep version | sed 's/[<>]/|/g' | cut -f3 -d\\|)"
EOF

### Branches
cat <<EOF
branches: "*"
EOF
