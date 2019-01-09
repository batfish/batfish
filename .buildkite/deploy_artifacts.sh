#!/usr/bin/env bash
set -e
set -x
S3_BUCKET_NAME="batfish-build-artifacts-arifogel"
S3_BUCKET="s3://${S3_BUCKET_NAME}"
BATFISH_TAG="$(git rev-parse HEAD)"
BATFISH_VERSION="$(grep -1 batfish-parent "projects/pom.xml" | grep version | sed 's/[<>]/|/g' | cut -f3 -d\|)"
ARTIFACTS_DIR=artifacts/batfish
ARTIFACT_TAR="${BATFISH_TAG}.tar"
mkdir -p "${ARTIFACTS_DIR}"
tar -czf "${ARTIFACTS_DIR}/questions.tgz" questions
cp workspace/allinone.jar "${ARTIFACTS_DIR}/"
cd "${ARTIFACTS_DIR}"
echo "${BATFISH_TAG}" > tag
echo "${BATFISH_VERSION}" > version
tar -cf "${ARTIFACT_TAR}" allinone.jar questions.tgz tag version
ln "${ARTIFACT_TAR}" dev.tar
ARTIFACT_KEY="${ARTIFACTS_DIR}/${ARTIFACT_TAR}"
if aws s3api head-object --bucket "${S3_BUCKET_NAME}" --key "${ARTIFACT_KEY}" >& /dev/null; then
  echo "Skipping upload since artifact already exists for this commit"
else
  buildkite-agent artifact upload "${ARTIFACT_TAR}" "${S3_BUCKET}/${ARTIFACT_KEY}"
  buildkite-agent artifact upload dev.tar "${S3_BUCKET}/${ARTIFACTS_DIR}/"
fi

