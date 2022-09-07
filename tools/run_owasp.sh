#!/usr/bin/env bash

set -euo pipefail

RELEASE_FILENAME="dependency-check-7.1.3-SNAPSHOT-release.zip"
RELEASE_URL="https://github.com/dhalperi/DependencyCheck/releases/download/v7.1.2-SNAPSHOT-maven-install/${RELEASE_FILENAME}"

# Check cache, and and download+cache jar if not present.
CACHE_DIR="${HOME}/.cache/owasp"
RELEASE_FILE="${CACHE_DIR}/${RELEASE_FILENAME}"
if [ ! -f ${RELEASE_FILE} ]; then
  mkdir -p "${CACHE_DIR}"
  wget "${RELEASE_URL}" -O "${RELEASE_FILE}"
  pushd "${CACHE_DIR}" && unzip -o "${RELEASE_FILENAME}" && popd
fi

# Run the check
${CACHE_DIR}/dependency-check/bin/dependency-check.sh \
  --failOnCVSS 4 \
  --enableExperimental \
  --scan ${1:-maven_install.json} \
  --suppression tools/owasp-suppressions.xml
