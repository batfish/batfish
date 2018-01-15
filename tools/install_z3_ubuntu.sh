#!/usr/bin/env bash
if [ "${#}" -ne 1 ]; then
   COMMAND=$(basename "${BASH_SOURCE}")
   echo "Usage: ${COMMAND} <install-prefix>"
   exit 1
fi
set -e
UBUNTU_VERSION="$(lsb_release -rs)"
[ -n "${UBUNTU_VERSION}" ]
INSTALL_PREFIX="${1}"
BINDIR="${INSTALL_PREFIX}/bin"
INCLUDEDIR="${INSTALL_PREFIX}/include"
JAVADIR="${INSTALL_PREFIX}/share/java"
LIBDIR="${INSTALL_PREFIX}/lib"
OLD_PWD="${PWD}"
OLD_UMASK="$(umask)"
WORKING="$(mktemp -d)"
VERSION="2018-01-12-450f3c9b459d128135abb5bbd4fa0508fe26bfae"
MACHINE="$(uname -m)"
if [ "${MACHINE}" = "x86_64" ]; then
	ARCH="x64"
elif [ "${MACHINE}" = "i386" ]; then
	ARCH="x86"
else
	echo "Unsupported machine: ${MACHINE}"
	exit 1
fi
Z3_REPO_URL="https://github.com/batfish/z3"
Z3_ZIP_URL="${Z3_REPO_URL}/releases/download/z3-${VERSION}/z3-${VERSION}-${ARCH}-ubuntu-${UBUNTU_VERSION}.zip"
Z3_ZIP="$(basename "${Z3_ZIP_URL}")"
Z3_DIR="$(basename "${Z3_ZIP}" .zip)"
cd "${WORKING}"
wget "${Z3_ZIP_URL}"
unzip "${Z3_ZIP}"
cd "${Z3_DIR}"
umask 0022
mkdir -p "${LIBDIR}" "${BINDIR}" "${INCLUDEDIR}" "${JAVADIR}"
cp "bin/libz3.so" "bin/libz3java.so" "${LIBDIR}/"
cp "bin/com.microsoft.z3.jar" "${JAVADIR}/"
cp "bin/z3" "${BINDIR}/"
cp include/* "${INCLUDEDIR}/"
strip "${LIBDIR}/libz3.so"
strip "${LIBDIR}/libz3java.so"
strip "${BINDIR}/z3"
umask "${OLD_UMASK}"
cd "${OLD_PWD}"
rm -rf "${WORKING}"

