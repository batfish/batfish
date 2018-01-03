#!/usr/bin/env bash
if [ $# -ne 1 ]; then
   COMMAND=$(basename "$BASH_SOURCE")
   echo "Usage: $COMMAND <install-prefix>"
   exit 1
fi
INSTALL_PREFIX="$1"
BINDIR=$INSTALL_PREFIX/bin
INCLUDEDIR=$INSTALL_PREFIX/include
JAVADIR=$INSTALL_PREFIX/share/java
LIBDIR=$INSTALL_PREFIX/lib
OLD_PWD=$PWD
OLD_UMASK=$(umask)
WORKING=$(mktemp -d)
VERSION=4.6.0
MACHINE=$(uname -m)
if [ "$MACHINE" = "x86_64" ]; then
	ARCH=x64
elif [ "$MACHINE" = "i386" ]; then
	ARCH=x86
else
	echo "Unsupported machine: $MACHINE"
	exit 1
fi
Z3_ZIP_URL=https://github.com/Z3Prover/z3/releases/download/z3-4.6.0/z3-${VERSION}-${ARCH}-ubuntu-14.04.zip
Z3_ZIP=$(basename ${Z3_ZIP_URL})
Z3_DIR=$(basename ${Z3_ZIP} .zip)
cd $WORKING || exit 1
wget ${Z3_ZIP_URL} || exit 1
unzip ${Z3_ZIP} || exit 1
cd ${Z3_DIR} || exit 1
umask 0022 || exit 1
mkdir -p $LIBDIR $BINDIR $INCLUDEDIR $JAVADIR
cp bin/libz3.so bin/libz3java.so $LIBDIR/ || exit 1
cp bin/com.microsoft.z3.jar $JAVADIR/ || exit 1
cp bin/z3 $BINDIR/ || exit 1
cp include/* $INCLUDEDIR/ || exit 1
strip $LIBDIR/libz3.so || exit 1
strip $LIBDIR/libz3java.so || exit 1
strip $BINDIR/z3 || exit 1
umask $OLD_UMASK || exit 1
cd $OLD_PWD || exit 1
rm -rf ${WORKING} || exit 1

