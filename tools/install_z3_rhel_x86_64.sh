#!/usr/bin/env bash
if [ "$#" -ne 1 ]; then
   COMMAND="$(basename "${BASH_SOURCE}")"
   echo "Usage: ${COMMAND} <install-prefix>"
   exit 1
fi
INSTALL_PREFIX="${1}"
if ! which python2.7; then
   echo Missing python2.7
   exit 1
fi
if ! which java; then
   echo Missing java
   exit 1
fi
if ! which javac; then
   echo Missing javac
   exit 1
fi
OLD_PWD="${PWD}"
OLD_UMASK="$(umask)"
WORKING="$(mktemp -d)"
set -e
cd "${WORKING}"
umask 0022
VERSION="2018-01-12-450f3c9b459d128135abb5bbd4fa0508fe26bfae"
Z3_REPO_URL="https://github.com/batfish/z3"
wget "${Z3_REPO_URL}/archive/z3-${VERSION}.tar.gz"
tar -xf "z3-${VERSION}.tar.gz"
cd "z3-z3-${VERSION}"
Z3_INSTALL_LIB_DIR=lib64 python2.7 scripts/mk_make.py --java --prefix="${INSTALL_PREFIX}"
cd build
NUMJOBS="$(grep -i processor /proc/cpuinfo | wc -l)"
make -j"${NUMJOBS}"
make install
umask "${OLD_UMASK}"
cd "${OLD_PWD}"
rm -rf "${WORKING}"

