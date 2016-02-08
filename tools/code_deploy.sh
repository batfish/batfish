#!/usr/bin/env bash

NUMARGS=$#

if [ ${NUMARGS} != 1 ]; then
   echo "Provide exactly one argument: The name of the binary (batfish/coordinator)";
   exit 1
fi

BINARY=$1

if [[ $(uname) == *"Darwin"* ]]; then
   export GNU_READLINK=greadlink
   export GNU_FIND=gfind
else
   export GNU_READLINK=readlink
   export GNU_FIND=find
fi

export BATFISH_SOURCED_SCRIPT=$BASH_SOURCE
export BATFISH_TOOLS_PATH="$($GNU_READLINK -f $(dirname $BATFISH_SOURCED_SCRIPT))"

export SRC_OUTDIR="${BATFISH_TOOLS_PATH}/../projects/${BINARY}/out/"

# the person doing ssh should have access to this directory
# i have set this directory to be writeable by everyone for now 
export DST_OUTDIR="/home/batfish_services/batfish/projects/${BINARY}/out/"

TARGET_HOST="www.batfish.org"

# copy the pieces to the target host
rsync -rvzL ${SRC_OUTDIR} ${TARGET_HOST}:${DST_OUTDIR} 

# stop and start the service
ssh ${TARGET_HOST} sudo stop ${BINARY}
ssh ${TARGET_HOST} sudo start ${BINARY}

# if the service is batfish, start the 10001 as well
if [ ${BINARY} == "batfish" ]; then
    ssh ${TARGET_HOST} sudo stop batfish-10001
    ssh ${TARGET_HOST} sudo start batfish-10001
fi
