#!/usr/bin/env bash
if [ $# -ne 1 ]; then
   COMMAND=$(basename "$BASH_SOURCE")
   echo "Usage: $COMMAND <install-prefix>"
   exit 1
fi
INSTALL_PREFIX="$1"
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
OLD_PWD=$PWD
OLD_UMASK=$(umask)
WORKING=$(mktemp -d)
cd $WORKING || exit 1
umask 0022 || exit 1
VERSION=4.6.0
wget https://github.com/Z3Prover/z3/archive/z3-${VERSION}.tar.gz || exit 1
tar -xf z3-${VERSION}.tar.gz || exit 1
cd z3-z3-${VERSION} || exit 1
Z3_INSTALL_LIB_DIR=lib64 python2.7 scripts/mk_make.py --java --prefix="$INSTALL_PREFIX"
cd build || exit 1
NUMJOBS=$(grep -i processor /proc/cpuinfo | wc -l)
make -j${NUMJOBS} || exit 1
make install || exit 1
umask $OLD_UMASK || exit 1
cd $OLD_PWD || exit 1
rm -rf ${WORKING} || exit 1

