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

export WWWROOT_SRC="${BATFISH_TOOLS_PATH}/../projects/wwwroot/"
export DOC_SRC="${BATFISH_TOOLS_PATH}/../doc/"

export WWWROOT_TMP_DST=wwwroot
export DOC_TMP_DST=doc

export HTML_ROOT="/var/www/www.batfish.org/";
export WWWROOT_FINAL_DST="${HTML_ROOT}/trybatfish"
export DOC_FINAL_DST="${HTML_ROOT}/doc"

TARGET_HOST="www.batfish.org"

# copy the pieces to temporary resting place on the target host
rsync -rvzL ${WWWROOT_SRC} ${TARGET_HOST}:${WWWROOT_TMP_DST} 
rsync -rvzL ${DOC_SRC} ${TARGET_HOST}:${DOC_TMP_DST} 

# copy from the temporay place to the final resting place (requires sudo)
ssh ${TARGET_HOST} sudo cp -r ${WWWROOT_TMP_DST}/* ${WWWROOT_FINAL_DST}/
ssh ${TARGET_HOST} sudo cp -r ${DOC_TMP_DST}/* ${DOC_FINAL_DST}/
