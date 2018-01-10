#!/usr/bin/env bash
## for now, just install the libs in /usr/local/lib and /Library/Java/Extensions
INSTALL_PREFIX="/usr/local"
EXTDIR=/Library/Java/Extensions
LIBDIR=$INSTALL_PREFIX/lib
OLD_PWD=$PWD
OLD_UMASK=$(umask)
WORKING=$(mktemp -d)
VERSION=4.6.0
Z3_ZIP_URL=https://github.com/Z3Prover/z3/releases/download/z3-${VERSION}/z3-${VERSION}-x64-osx-10.11.6.zip
Z3_ZIP=$(basename ${Z3_ZIP_URL})
Z3_DIR=$(basename ${Z3_ZIP} .zip)
set -x
cd $WORKING || exit 1
umask 0022 || exit 1
curl -L "${Z3_ZIP_URL}" -o "${Z3_ZIP}" || exit 1
unzip ${Z3_ZIP} || exit 1
cd ${Z3_DIR} || exit 1
mkdir -p $LIBDIR $EXTDIR
cp bin/libz3.dylib bin/libz3java.dylib $LIBDIR/ || exit 1
ln -s $LIBDIR/libz3.dylib $EXTDIR/libz3.dylib
ln -s $LIBDIR/libz3java.dylib $EXTDIR/libz3java.dylib
umask $OLD_UMASK || exit 1
cd $OLD_PWD || exit 1
rm -rf ${WORKING} || exit 1

