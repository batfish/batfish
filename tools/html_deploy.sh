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

export WWWROOT_SRC="${BATFISH_TOOLS_PATH}/../projects/wwwroot"
export DOC_SRC="${BATFISH_TOOLS_PATH}/../doc"
export DOC_LOCAL_DST="${WWWROOT_SRC}/doc"
mkdir -p $DOC_LOCAL_DST
cp -r $DOC_SRC/. $DOC_LOCAL_DST/.
export WWWROOT_TMP_DST=wwwroot

export HTML_ROOT="/var/www/www.batfish.org";
export WWWROOT_FINAL_DST="${HTML_ROOT}/trybatfish"

TARGET_HOST="www.batfish.org"

# copy the pieces to temporary resting place on the target host
ssh ${TARGET_HOST} mkdir -p $WWWROOT_TMP_DST
rsync -rvzL --delete ${WWWROOT_SRC}/. ${TARGET_HOST}:${WWWROOT_TMP_DST}/.

# copy from the temporay place to the final resting place (requires sudo)
ssh ${TARGET_HOST} sudo mkdir -p ${WWWROOT_FINAL_DST}
ssh ${TARGET_HOST} sudo cp -r ${WWWROOT_TMP_DST}/. ${WWWROOT_FINAL_DST}/.
