#!/usr/bin/env bash
set -e
set -x
BATFISH_TAG="$(git rev-parse HEAD)"
ARTIFACTS_DIR=artifacts/batfish
ARTIFACT_TAR="${BATFISH_TAG}.tar"
mkdir -p "${ARTIFACTS_DIR}"
tar -czf "${ARTIFACTS_DIR}/questions.tgz" questions
cp workspace/allinone.jar "${ARTIFACTS_DIR}/"
cd "${ARTIFACTS_DIR}"
echo "${BATFISH_TAG}" > tag
tar -cf "${ARTIFACT_TAR}" allinone.jar questions.tgz tag
ln "${ARTIFACT_TAR}" dev.tar
buildkite-agent artifact upload "${ARTIFACT_TAR}" s3://batfish-build-artifacts-arifogel
buildkite-agent artifact upload dev.tar s3://batfish-build-artifacts-arifogel

