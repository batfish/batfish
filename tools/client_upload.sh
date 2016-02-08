#!/usr/bin/env bash

if [[ $(uname) == *"Darwin"* ]]; then
   export GNU_READLINK=greadlink
   export GNU_FIND=gfind
else
   export GNU_READLINK=readlink
   export GNU_FIND=find
fi

export BATFISH_SOURCED_SCRIPT=$BASH_SOURCE
export BATFISH_TOOLS_PATH="$($GNU_READLINK -f $(dirname $BATFISH_SOURCED_SCRIPT))"

export SRC_DIR="${BATFISH_TOOLS_PATH}/../projects/batfish-client/"
export TMP_DIR="."
export FINAL_DIR="/var/www/www.batfish.org/trybatfish";

export ZIPNAME="batfish-client.zip"

TARGET_HOST="www.batfish.org"

#got to the parent folder and zip the file
cd $SRC_DIR
zip -r ${ZIPNAME} out/

# copy the pieces to temporary resting place on the target host
rsync -rvzL ${ZIPNAME} ${TARGET_HOST}:${TMP_DIR}

#delete the zip file and come back to the original directory
rm ${ZIPNAME}
cd -

# copy from the temporay place to the final resting place (requires sudo)
ssh ${TARGET_HOST} sudo cp -r ${TMP_DIR}/${ZIPNAME} ${FINAL_DIR}/

