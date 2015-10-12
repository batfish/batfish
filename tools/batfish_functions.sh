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

. "$BATFISH_TOOLS_PATH/common.sh" || return 1
. "$BATFISH_TOOLS_PATH/destination.sh"
. "$BATFISH_TOOLS_PATH/multipath.sh"
. "$BATFISH_TOOLS_PATH/role_reachability.sh"
. "$BATFISH_TOOLS_PATH/failure.sh"
. "$BATFISH_TOOLS_PATH/role_transit.sh"

. "$BATFISH_TOOLS_PATH/nsdi_demo.sh"
. "$BATFISH_TOOLS_PATH/modular.sh"
. "$BATFISH_TOOLS_PATH/completion.sh"
