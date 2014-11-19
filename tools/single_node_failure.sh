#!/usr/bin/env bash

batfish_compile_blacklist_node() {
   batfish_date
   local WORKSPACE=$1
   local TEST_RIG=$2
   local DUMP_DIR=$3
   local INDEP_SERIAL_DIR=$4
   local BLACKLISTED_NODE=$5
   echo ": START: Compute the fixed point of the control plane with blacklisted node: $BLACKLISTED_NODE"
   batfish_expect_args 5 $# || return 1
   batfish -workspace $WORKSPACE -testrig $TEST_RIG -sipath $INDEP_SERIAL_DIR -compile -facts -dumpcp -dumpdir $DUMP_DIR -blnode $BLACKLISTED_NODE || return 1
   batfish_date
   echo ": END: Compute the fixed point of the control plane with blacklisted node: \"$BLACKLISTED_NODE\""
}
export -f batfish_compile_blacklist_node

