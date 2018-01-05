#!/usr/bin/env bash
WDIR="$(cygpath -u "${WINDIR}")"
W32DIR=$WDIR/SysWOW64
W64DIR=$WDIR/System32
OLD_PWD=$PWD
WORKING=$(mktemp -d)
VERSION=4.6.0
Z364_ZIP_URL=https://github.com/Z3Prover/z3/releases/download/z3-${VERSION}/z3-${VERSION}-x64-win.zip
Z364_ZIP=$(basename ${Z364_ZIP_URL})
Z364_DIR=$(basename ${Z364_ZIP} .zip)
Z332_ZIP_URL=https://github.com/Z3Prover/z3/releases/download/z3-${VERSION}/z3-${VERSION}-x86-win.zip
Z332_ZIP=$(basename ${Z332_ZIP_URL})
Z332_DIR=$(basename ${Z332_ZIP} .zip)
set -x
cd $WORKING || exit 1
wget "${Z364_ZIP_URL}" || exit 1
unzip ${Z364_ZIP} || exit 1
cp ${Z364_DIR}/bin/libz3.dll $W64DIR || exit 1
getfacl $W64DIR/msvcr110.dll | setfacl -f - $W64DIR/libz3.dll
cp ${Z364_DIR}/bin/libz3java.dll $W64DIR || exit 1
getfacl $W64DIR/msvcr110.dll | setfacl -f - $W64DIR/libz3java.dll
cp ${Z364_DIR}/bin/libz3.dll $W64DIR/z3.dll || exit 1
getfacl $W64DIR/msvcr110.dll | setfacl -f - $W64DIR/z3.dll
cp ${Z364_DIR}/bin/libz3java.dll $W64DIR/z3java.dll || exit 1
getfacl $W64DIR/msvcr110.dll | setfacl -f - $W64DIR/z3java.dll
cp ${Z364_DIR}/bin/Microsoft.Z3.dll $W64DIR/ || exit 1
getfacl $W64DIR/msvcr110.dll | setfacl -f - $W64DIR/Microsoft.Z3.dll
wget "${Z332_ZIP_URL}" || exit 1
unzip ${Z332_ZIP} || exit 1
cp ${Z332_DIR}/bin/libz3.dll $W32DIR/ || exit 1
getfacl $W32DIR/msvcr110.dll | setfacl -f - $W32DIR/libz3.dll
cp ${Z332_DIR}/bin/libz3java.dll $W32DIR/ || exit 1
getfacl $W32DIR/msvcr110.dll | setfacl -f - $W32DIR/libz3java.dll
cp ${Z332_DIR}/bin/libz3.dll $W32DIR/z3.dll || exit 1
getfacl $W32DIR/msvcr110.dll | setfacl -f - $W32DIR/z3.dll
cp ${Z332_DIR}/bin/libz3java.dll $W32DIR/z3java.dll || exit 1
getfacl $W32DIR/msvcr110.dll | setfacl -f - $W32DIR/z3java.dll
cp ${Z332_DIR}/bin/Microsoft.Z3.dll $W32DIR/ || exit 1
getfacl $W32DIR/msvcr110.dll | setfacl -f - $W32DIR/Microsoft.Z3.dll
cd $OLD_PWD || exit 1
rm -rf ${WORKING} || exit 1

