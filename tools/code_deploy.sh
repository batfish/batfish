#!/usr/bin/env bash

NUMARGS=$#

if [ ${NUMARGS} != 2 ]; then
   echo "Provide exactly two argument: The remote site and the name of the binary (batfish/coordinator)";
   exit 1
fi

TARGET_HOST=$1
BINARY=$2

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

# copy the pieces to the target host
rsync -rvzL ${SRC_OUTDIR} ${TARGET_HOST}:${DST_OUTDIR} 

# stop and start the service
ssh ${TARGET_HOST} sudo service ${BINARY} stop
ssh ${TARGET_HOST} sudo service ${BINARY} start

# if the service is batfish, start the 10001 as well
if [ ${BINARY} == "batfish" ]; then
    ssh ${TARGET_HOST} sudo service batfish-10001 stop
    ssh ${TARGET_HOST} sudo service batfish-10001 start
fi
