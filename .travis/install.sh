#!/usr/bin/env bash

TMP_DIR=$(mktemp -d)
cd ${TMP_DIR} || exit 1

if [[ ${TRAVIS_OS_NAME} == 'linux' ]]; then
   sudo "${TRAVIS_BUILD_DIR}/tools/install_z3_ubuntu.sh" /usr || exit 1
   z3 --version || exit 1
elif [[ ${TRAVIS_OS_NAME} == 'osx' ]]; then
   sudo ${TRAVIS_BUILD_DIR}/tools/install_z3_osx.sh || exit 1
   brew update || exit 1
   which gfind || brew install findutils || exit 1
   gfind --version || exit 1
   which ant || brew install ant || exit 1
   ant -version || exit 1
   echo $PATH
   java -version || exit 1
   javac -version || exit 1
else
   echo "Unsupported TRAVIS_OS_NAME: $TRAVIS_OS_NAME"
   exit 1 # CI not supported in this case
fi

