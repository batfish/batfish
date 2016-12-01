#!/usr/bin/env bash

TMP_DIR=$(mktemp -d)
cd ${TMP_DIR} || exit 1

if [[ $TRAVIS_OS_NAME == 'linux' ]]; then
   ### install java 8 and ant
   sudo add-apt-repository -y ppa:webupd8team/java
   sudo apt-get update
   sudo apt-get -y install oracle-java8-installer ant

   ### install z3
   Z3_ZIP_URL=https://github.com/Z3Prover/z3/releases/download/z3-4.5.0/z3-4.5.0-x64-ubuntu-14.04.zip
   Z3_ZIP=$(basename ${Z3_ZIP_URL})
   Z3_DIR=$(basename ${Z3_ZIP} .zip)
   wget ${Z3_ZIP_URL} || exit 1
   unzip ${Z3_ZIP} || exit 1
   cd ${Z3_DIR}
   sudo cp bin/libz3.so bin/libz3java.so bin/com.microsoft.z3.jar /usr/lib/ || exit 1
   sudo cp bin/z3 /usr/bin/ || exit 1
   sudo cp include/* /usr/include/ || exit 1
   z3 --version || exit 1
   ### install python packages
   echo -e "\n   ............. Installing pip"
   sudo -H apt-get -y install python-pip || exit 1
   echo -e "\n   ............. Installing requests"
   pip install requests || exit 1
   echo -e "\n   ............. Installing requests_toolbelt"
   pip install requests_toolbelt || exit 1
else
   exit 1 # CI not supported in this case
fi

