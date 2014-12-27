#!/usr/bin/env bash

batfish_confirm_analyze() {
   BATFISH_CONFIRM=batfish_confirm batfish_analyze $@
}
export -f batfish_confirm_analyze
   
batfish_analyze() {
   batfish_expect_args 2 $# || return 1
   if [ -z "$BATFISH_CONFIRM" ]; then
      local BATFISH_CONFIRM=true
   fi
   local TEST_RIG_RELATIVE=$1
   local PREFIX=$2
   local WORKSPACE=batfish-$USER-$PREFIX
   local OLD_PWD=$PWD
   if [ "$(echo $TEST_RIG_RELATIVE | head -c1)" = "/" ]; then
      local TEST_RIG=$TEST_RIG_RELATIVE
   else
      local TEST_RIG=$PWD/$TEST_RIG_RELATIVE
   fi
   local BGP=$OLD_PWD/$PREFIX-bgp
   local REACH_PATH=$OLD_PWD/$PREFIX-reach.smt2
   local NODE_SET_PATH=$OLD_PWD/$PREFIX-node-set
   local QUERY_PATH=$OLD_PWD/$PREFIX-query
   local MPI_QUERY_BASE_PATH=$QUERY_PATH/multipath-inconsistency-query
   local DUMP_DIR=$OLD_PWD/$PREFIX-dump
   local FLOWS=$OLD_PWD/$PREFIX-flows
   local ROUTES=$OLD_PWD/$PREFIX-routes
   local VENDOR_SERIAL_DIR=$OLD_PWD/$PREFIX-vendor
   local INDEP_SERIAL_DIR=$OLD_PWD/$PREFIX-indep
   local DP_DIR=$OLD_PWD/$PREFIX-dp

   echo "Parse vendor configuration files and serialize vendor structures"
   $BATFISH_CONFIRM && { batfish_serialize_vendor $TEST_RIG $VENDOR_SERIAL_DIR || return 1 ; }

   echo "Parse vendor structures and serialize vendor-independent structures"
   $BATFISH_CONFIRM && { batfish_serialize_independent $VENDOR_SERIAL_DIR $INDEP_SERIAL_DIR || return 1 ; }

   echo "Compute the fixed point of the control plane"
   $BATFISH_CONFIRM && { batfish_compile $WORKSPACE $TEST_RIG $DUMP_DIR $INDEP_SERIAL_DIR || return 1 ; }

   echo "Query routes"
   $BATFISH_CONFIRM && { batfish_query_routes $ROUTES $WORKSPACE || return 1 ; }

   echo "Query bgp"
   $BATFISH_CONFIRM && { batfish_query_bgp $BGP $WORKSPACE || return 1 ; }

   echo "Query data plane predicates"
   $BATFISH_CONFIRM && { batfish_query_data_plane $WORKSPACE $DP_DIR || return 1 ; }

   echo "Extract z3 reachability relations"
   $BATFISH_CONFIRM && { batfish_generate_z3_reachability $DP_DIR $INDEP_SERIAL_DIR $REACH_PATH $NODE_SET_PATH || return 1 ; }

   echo "Find multipath-inconsistent packet constraints"
   $BATFISH_CONFIRM && { batfish_find_multipath_inconsistent_packet_constraints $REACH_PATH $QUERY_PATH $MPI_QUERY_BASE_PATH $NODE_SET_PATH || return 1 ; }

   echo "Generate multipath-inconsistency concretizer queries"
   $BATFISH_CONFIRM && { batfish_generate_multipath_inconsistency_concretizer_queries $MPI_QUERY_BASE_PATH $NODE_SET_PATH || return 1 ; }

   echo "Inject concrete packets into network model"
   $BATFISH_CONFIRM && { batfish_inject_packets $WORKSPACE $QUERY_PATH $DUMP_DIR || return 1 ; }

   echo "Query flow results from LogicBlox"
   $BATFISH_CONFIRM && { batfish_query_flows $FLOWS $WORKSPACE || return 1 ; }
}
export -f batfish_analyze

batfish_find_multipath_inconsistent_packet_constraints() {
   batfish_date
   echo ": START: Find inconsistent packet constraints"
   batfish_expect_args 4 $# || return 1
   local REACH_PATH=$1
   local QUERY_PATH=$2
   local MPI_QUERY_BASE_PATH=$3
   local NODE_SET_PATH=$4
   local NODE_SET_TEXT_PATH=${NODE_SET_PATH}.txt
   local OLD_PWD=$PWD
   mkdir -p $QUERY_PATH
   cd $QUERY_PATH
   batfish -mpi -mpipath $MPI_QUERY_BASE_PATH -nodes $NODE_SET_PATH || return 1
   cat $NODE_SET_TEXT_PATH | parallel --halt 2 batfish_find_multipath_inconsistent_packet_constraints_helper {} $REACH_PATH $MPI_QUERY_BASE_PATH
   cd $OLD_PWD
   batfish_date
   echo ": END: Find inconsistent packet constraints"
}
export -f batfish_find_multipath_inconsistent_packet_constraints

batfish_find_multipath_inconsistent_packet_constraints_helper() {
   batfish_expect_args 3 $# || return 1
   local NODE=$1
   local REACH_PATH=$2
   local MPI_QUERY_BASE_PATH=$3
   batfish_date
   local MPI_QUERY_PATH=${MPI_QUERY_BASE_PATH}-${NODE}.smt2
   local MPI_QUERY_OUTPUT_PATH=${MPI_QUERY_PATH}.out
   echo ": START: Find inconsistent packet constraints for \"$NODE\" (\"$MPI_QUERY_OUTPUT_PATH\")"
   cat $REACH_PATH $MPI_QUERY_PATH | batfish_time $BATFISH_Z3_DATALOG -smt2 -in 3>&1 1> $MPI_QUERY_OUTPUT_PATH 2>&3
   if [ "${PIPESTATUS[0]}" -ne 0 -o "${PIPESTATUS[1]}" -ne 0 ]; then
      return 1
   fi
   batfish_date
   echo ": END: Find inconsistent packet constraints for \"$NODE\" (\"$MPI_QUERY_OUTPUT_PATH\")"
}
export -f batfish_find_multipath_inconsistent_packet_constraints_helper

batfish_generate_multipath_inconsistency_concretizer_queries() {
   batfish_date
   echo ": START: Generate multipath-inconsistency concretizer queries"
   batfish_expect_args 2 $# || return 1
   local MPI_QUERY_BASE_PATH=$1
   local NODE_SET_PATH=$2
   local QUERY_PATH="$(dirname $MPI_QUERY_BASE_PATH)"
   local NODE_SET_TEXT_PATH=${NODE_SET_PATH}.txt
   local OLD_PWD=$PWD
   cd $QUERY_PATH
   cat $NODE_SET_TEXT_PATH | parallel --halt 2 batfish_generate_multipath_inconsistency_concretizer_queries_helper {} $MPI_QUERY_BASE_PATH \;
   if [ "${PIPESTATUS[0]}" -ne 0 -o "${PIPESTATUS[1]}" -ne 0 ]; then
      return 1
   fi
   cd $OLD_PWD
   batfish_date
   echo ": END: Generate multipath-inconsistency concretizer queries"
}
export -f batfish_generate_multipath_inconsistency_concretizer_queries

batfish_generate_multipath_inconsistency_concretizer_queries_helper() {
   batfish_expect_args 2 $# || return 1
   local NODE=$1
   local MPI_QUERY_BASE_PATH=$2
   local QUERY_OUT=${MPI_QUERY_BASE_PATH}-${NODE}.smt2.out
   local MPI_CONCRETIZER_QUERY_BASE_PATH=${MPI_QUERY_BASE_PATH}-${NODE}-concrete
   batfish -conc -concin $QUERY_OUT -concout $MPI_CONCRETIZER_QUERY_BASE_PATH || return 1
   find $PWD -regextype posix-extended -regex "${MPI_CONCRETIZER_QUERY_BASE_PATH}-[0-9]+.smt2" | \
      parallel --halt 2 -j1 batfish_generate_concretizer_query_output {} $NODE \;
   if [ "${PIPESTATUS[0]}" -ne 0 -o "${PIPESTATUS[1]}" -ne 0 ]; then
      return 1
   fi
}
export -f batfish_generate_multipath_inconsistency_concretizer_queries_helper

