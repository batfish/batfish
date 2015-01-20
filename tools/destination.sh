#!/usr/bin/env bash

batfish_analyze_destination_consistency() {
   local TEST_RIG_RELATIVE=$1
   shift
   local PREFIX=$1
   shift
   local MACHINES="$@"
   local NUM_MACHINES=$#
   if [ -z "$PREFIX" ]; then
         echo "ERROR: Empty prefix" 1>&2
         return 1
   fi
   if [ "$NUM_MACHINES" -eq 0 ]; then
      local MACHINES=localhost
      local NUM_MACHINES=1
   fi
   local OLD_PWD=$PWD
   if [ "$(echo $TEST_RIG_RELATIVE | head -c1)" = "/" ]; then
      local TEST_RIG=$TEST_RIG_RELATIVE
   else
      local TEST_RIG=$PWD/$TEST_RIG_RELATIVE
   fi
   local LOG_FILE=$OLD_PWD/$PREFIX-destination-consistency-scenarios.log
   local FLOWS=$OLD_PWD/$PREFIX-flows
   local EDGE_PREDICATE=LanAdjacent
   local SCENARIO_BASE_DIR=$OLD_PWD/$PREFIX-destination-consistency-scenarios
   local NODES=$OLD_PWD/$PREFIX-node-set.txt
   
   local INDEX=1
   local NUM_NODES=$(cat $NODES | wc -l)
   local CURRENT_MACHINE=0
   local NUM_NODES_PER_MACHINE=$(($NUM_NODES / $NUM_MACHINES))
   local WORKSPACE=batfish-${USER}-destination-consistency
   for machine in $MACHINES; do
      local CURRENT_MACHINE=$(($CURRENT_MACHINE + 1))
      local LOCAL_NODES=${NODES}-local
      ssh $machine "mkdir -p $SCENARIO_BASE_DIR"
      if [ "$CURRENT_MACHINE" -eq "$NUM_MACHINES" ]; then
         tail -n+$INDEX $NODES | ssh $machine "cat > $LOCAL_NODES"
         if [ "${PIPESTATUS[0]}" -ne 0 -o "${PIPESTATUS[1]}" -ne 0 ]; then
            return 1
         fi
      else
         sed -n -e "$INDEX,$(($INDEX + $NUM_NODES_PER_MACHINE - 1))p" $NODES | ssh $machine "cat > ${NODES}-local"
         if [ "${PIPESTATUS[0]}" -ne 0 -o "${PIPESTATUS[1]}" -ne 0 ]; then
            return 1
         fi
         local INDEX=$(($INDEX + $NUM_NODES_PER_MACHINE))
      fi
   done
   for machine in $MACHINES; do
      echo "nohup ssh $machine bash --login -c \"cd $OLD_PWD || exit; pwd; batfish_analyze_destination_consistency_machine $TEST_RIG $PREFIX $SCENARIO_BASE_DIR $LOCAL_NODES $WORKSPACE >& $LOG_FILE\" >& /dev/null &"
      nohup ssh $machine bash --login -c "pwd; echo $OLD_PWD; cd $OLD_PWD || exit; pwd; batfish_analyze_destination_consistency_machine $TEST_RIG $PREFIX $SCENARIO_BASE_DIR $LOCAL_NODES $WORKSPACE >& $LOG_FILE"  >& /dev/null &
   done
}
export -f batfish_analyze_destination_consistency

batfish_analyze_destination_consistency_machine() {
   batfish_expect_args 5 $# || return 1
   local TEST_RIG=$1
   local PREFIX=$2
   local SCENARIO_BASE_DIR=$3
   local NODES=$4
   local WORKSPACE=$5
   local OLD_PWD=$PWD
   local ORIG_DP_DIR=$SCENARIO_BASE_DIR/../$PREFIX-dp
   local ORIG_FLOW_SINKS=$ORIG_DP_DIR/flow-sinks
   local ORIG_QUERY_PATH=$SCENARIO_BASE_DIR/../$PREFIX-query
   local ORIG_DI_QUERY_BASE_PATH=$ORIG_QUERY_PATH/destination-consistency-query
   local ORIG_REACH_PATH=$SCENARIO_BASE_DIR/../$PREFIX-reach.smt2
   local INDEP_SERIAL_DIR=$SCENARIO_BASE_DIR/../$PREFIX-indep
   local NODE_SET_PATH=$SCENARIO_BASE_DIR/../$PREFIX-node-set
   #Extract z3 reachability relations for no-failure scenario
   cd $SCENARIO_BASE_DIR
   batfish_generate_z3_reachability $ORIG_DP_DIR $INDEP_SERIAL_DIR $ORIG_REACH_PATH $NODE_SET_PATH || return 1
   cd $OLD_PWD
   cat $NODES | while read NODE; do
      cd $SCENARIO_BASE_DIR
      
      # Make directory for current failure scenario
      if [ -d "$NODE" ]; then
         echo "Skipping node with existing output: \"${NODE}\""
         continue
      else
         mkdir $NODE || return 1
      fi
#      mkdir -p $NODE
      # Enter node directory
      cd $NODE

      local QUERY_PATH=$PWD/$PREFIX-query
      local DP_DIR=$PWD/$PREFIX-dp
      local DUMP_DIR=$PWD/$PREFIX-dump
      local FLOWS=$PWD/$PREFIX-flows
      local REACH_PATH=$PWD/$PREFIX-reach.smt2
      local VENDOR_SERIAL_DIR=$PWD/$PREFIX-vendor
      local DI_QUERY_BASE_PATH=$QUERY_PATH/destination-consistency-query

      # WORKAROUND: LogicBlox uses too much memory if we do not clear its state periodically
      batfish_nuke_reset_logicblox || return 1
      
      # Compute the fixed point of the control plane with failed interface
      batfish_compile_blacklist_node $WORKSPACE $TEST_RIG $DUMP_DIR $INDEP_SERIAL_DIR $NODE $ORIG_FLOW_SINKS || return 1

      # Query data plane predicates
      batfish_query_data_plane $WORKSPACE $DP_DIR || return 1

      # Extract z3 reachability relations
      BATFISH_COMMON_ARGS="$BATFISH_COMMON_ARGS -blnode $NODE" batfish_generate_z3_reachability $DP_DIR $INDEP_SERIAL_DIR $REACH_PATH $NODE_SET_PATH || return 1
   
      # Find destination-consistency reachable packet constraints for node for no-failure scenario
      batfish_find_destination_consistency_node_accept_packet_constraints $ORIG_REACH_PATH $QUERY_PATH $DI_QUERY_BASE_PATH $NODE_SET_PATH $NODE || return 1

      # Find destination-consistency general reachable packet constraints for node-failure scenario
      batfish_find_destination_consistency_accept_packet_constraints $REACH_PATH $QUERY_PATH $DI_QUERY_BASE_PATH $NODE_SET_PATH $NODE || return 1

      # Generate destination-consistency concretizer queries
      batfish_generate_destination_consistency_concretizer_queries $DI_QUERY_BASE_PATH $NODE_SET_PATH $NODE || return 1

      # Inject concrete packets into network model
      batfish_inject_packets $WORKSPACE $QUERY_PATH $DUMP_DIR || return 1

      # Query flow results from LogicBlox
      batfish_query_flows $FLOWS $WORKSPACE || return 1
      
   done
   cd $OLD_PWD
}
export -f batfish_analyze_destination_consistency_machine

batfish_find_destination_consistency_accept_packet_constraints() {
   batfish_expect_args 5 $# || return 1
   local REACH_PATH=$1
   local QUERY_PATH=$2
   local DI_QUERY_BASE_PATH=$3
   local NODE_SET_PATH=$4
   local BLACKLISTED_NODE=$5
   local NODE_SET_TEXT_PATH=${NODE_SET_PATH}.txt
   local OLD_PWD=$PWD
   local DI_QUERY_PRED_PATH=${DI_QUERY_BASE_PATH}_accept
   batfish_date
   echo ": START: Find accept packet constraints"
   mkdir -p $QUERY_PATH
   cd $QUERY_PATH
   batfish -reach -reachpath $DI_QUERY_PRED_PATH -nodes $NODE_SET_PATH -blnode $BLACKLISTED_NODE || return 1
   cat $NODE_SET_TEXT_PATH | $BATFISH_PARALLEL batfish_find_destination_consistency_accept_packet_constraints_helper {} $REACH_PATH $DI_QUERY_PRED_PATH $BLACKLISTED_NODE
   if [ "${PIPESTATUS[0]}" -ne 0 -o "${PIPESTATUS[1]}" -ne 0 ]; then
      return 1
   fi
   cd $OLD_PWD
   batfish_date
   echo ": END: Find accept packet constraints"
}
export -f batfish_find_destination_consistency_accept_packet_constraints

batfish_find_destination_consistency_accept_packet_constraints_helper() {
   batfish_expect_args 4 $# || return 1
   local NODE=$1
   local REACH_PATH=$2
   local DI_QUERY_BASE_PATH=$3
   local BLACKLISTED_NODE=$4
   local DI_QUERY_PATH=${DI_QUERY_BASE_PATH}-${NODE}.smt2
   local DI_QUERY_OUTPUT_PATH=${DI_QUERY_PATH}.out
   if [ "$NODE" = "$BLACKLISTED_NODE" ]; then
      return
   fi
   batfish_date
   echo ": START: Find accept packet constraints for \"$NODE\" ==> \"$DI_QUERY_OUTPUT_PATH\""
   cat $REACH_PATH $DI_QUERY_PATH | batfish_time $BATFISH_Z3_DATALOG -smt2 -in 3>&1 1>$DI_QUERY_OUTPUT_PATH 2>&3
   if [ "${PIPESTATUS[0]}" -ne 0 -o "${PIPESTATUS[1]}" -ne 0 ]; then
      return 1
   fi
   batfish_date
   echo ": END: Find reachable packet constraints for \"$NODE\" ==> \"$DI_QUERY_OUTPUT_PATH\""
}
export -f batfish_find_destination_consistency_accept_packet_constraints_helper

batfish_find_destination_consistency_node_accept_packet_constraints() {
   batfish_expect_args 5 $# || return 1
   local REACH_PATH=$1
   local QUERY_PATH=$2
   local DI_QUERY_BASE_PATH=$3
   local NODE_SET_PATH=$4
   local BLACKLIST_NODE=$5
   local NODE_SET_TEXT_PATH=${NODE_SET_PATH}.txt
   local OLD_PWD=$PWD
   local DI_QUERY_PRED_PATH=${DI_QUERY_BASE_PATH}_node_accept_${BLACKLIST_NODE}
   batfish_date
   echo ": START: Find node accept packet constraints for node: \"$BLACKLIST_NODE"
   mkdir -p $QUERY_PATH
   cd $QUERY_PATH
   batfish -reach -reachpath $DI_QUERY_PRED_PATH -nodes $NODE_SET_PATH -acceptnode $BLACKLIST_NODE || return 1
   cat $NODE_SET_TEXT_PATH | $BATFISH_PARALLEL batfish_find_destination_consistency_node_accept_packet_constraints_helper {} $REACH_PATH $DI_QUERY_PRED_PATH $BLACKLIST_NODE
   if [ "${PIPESTATUS[0]}" -ne 0 -o "${PIPESTATUS[1]}" -ne 0 ]; then
      return 1
   fi
   cd $OLD_PWD
   batfish_date
   echo ": END: Find node accept packet constraints"
}
export -f batfish_find_destination_consistency_node_accept_packet_constraints

batfish_find_destination_consistency_node_accept_packet_constraints_helper() {
   batfish_expect_args 4 $# || return 1
   local NODE=$1
   local REACH_PATH=$2
   local DI_QUERY_BASE_PATH=$3
   local BLACKLIST_NODE=$4
   local DI_QUERY_PATH=${DI_QUERY_BASE_PATH}-${NODE}.smt2
   local DI_QUERY_OUTPUT_PATH=${DI_QUERY_PATH}.out
   if [ "$NODE" = "$BLACKLIST_NODE" ]; then
      return
   fi
   batfish_date
   echo ": START: Find node accept packet constraints for \"$NODE\" ==> \"$DI_QUERY_OUTPUT_PATH\""
   cat $REACH_PATH $DI_QUERY_PATH | batfish_time $BATFISH_Z3_DATALOG -smt2 -in 3>&1 1>$DI_QUERY_OUTPUT_PATH 2>&3
   if [ "${PIPESTATUS[0]}" -ne 0 -o "${PIPESTATUS[1]}" -ne 0 ]; then
      return 1
   fi
   batfish_date
   echo ": END: Find node accept packet constraints for \"$NODE\" ==> \"$DI_QUERY_OUTPUT_PATH\""
}
export -f batfish_find_destination_consistency_node_accept_packet_constraints_helper

batfish_generate_destination_consistency_concretizer_queries() {
   batfish_date
   echo ": START: Generate destination-consistency concretizer queries"
   batfish_expect_args 3 $# || return 1
   local DI_QUERY_BASE_PATH=$1
   local NODE_SET_PATH=$2
   local BLACKLISTED_NODE=$3
   local QUERY_PATH="$(dirname $DI_QUERY_BASE_PATH)"
   local NODE_SET_TEXT_PATH=${NODE_SET_PATH}.txt
   local OLD_PWD=$PWD
   cd $QUERY_PATH
   cat $NODE_SET_TEXT_PATH | $BATFISH_PARALLEL batfish_generate_destination_consistency_concretizer_queries_helper {} $DI_QUERY_BASE_PATH $BLACKLISTED_NODE \;
   if [ "${PIPESTATUS[0]}" -ne 0 -o "${PIPESTATUS[1]}" -ne 0 ]; then
      return 1
   fi
   cd $OLD_PWD
   batfish_date
   echo ": END: Generate destination-consistency concretizer queries"
}
export -f batfish_generate_destination_consistency_concretizer_queries

batfish_generate_destination_consistency_concretizer_queries_helper() {
   batfish_expect_args 3 $# || return 1
   local NODE=$1
   local DI_QUERY_BASE_PATH=$2
   local BLACKLISTED_NODE=$3
   local QUERY_BASE=${DI_QUERY_BASE_PATH}-${BLACKLISTED_NODE}-${NODE}
   local QUERY_OUT=${QUERY_BASE}.smt2.out
   local DI_CONCRETIZER_QUERY_BASE_PATH=${QUERY_BASE}-concrete
   local ACCEPT_QUERY_OUT=${DI_QUERY_BASE_PATH}_accept-${NODE}.smt2.out
   local NODE_ACCEPT_QUERY_OUT=${DI_QUERY_BASE_PATH}_node_accept_${BLACKLISTED_NODE}-${NODE}.smt2.out
   if [ "$NODE" = "$BLACKLISTED_NODE" ]; then
      return
   fi
   batfish -conc -concin $ACCEPT_QUERY_OUT $NODE_ACCEPT_QUERY_OUT -concout $DI_CONCRETIZER_QUERY_BASE_PATH -concunique || return 1
   find $PWD -regextype posix-extended -regex "${DI_CONCRETIZER_QUERY_BASE_PATH}-[0-9]+.smt2" | \
      $BATFISH_NESTED_PARALLEL batfish_generate_concretizer_query_output {} $NODE \;
   if [ "${PIPESTATUS[0]}" -ne 0 -o "${PIPESTATUS[1]}" -ne 0 ]; then
      return 1
   fi
}
export -f batfish_generate_destination_consistency_concretizer_queries_helper

