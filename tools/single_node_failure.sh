#!/usr/bin/env bash

batfish_analyze_node_failures() {
   local TEST_RIG_RELATIVE=$1
   shift
   local PREFIX=$1
   shift
   local MACHINES="${@}"
   local NUM_MACHINES=$#
   if [ "${NUM_MACHINES}" -eq 0 ]; then
      local MACHINES=localhost
      local NUM_MACHINES=1
   fi
   local OLD_PWD=$PWD
   if [ "$(echo $TEST_RIG_RELATIVE | head -c1)" = "/" ]; then
      local TEST_RIG=$TEST_RIG_RELATIVE
   else
      local TEST_RIG=$PWD/$TEST_RIG_RELATIVE
   fi
   local LOG_FILE=$OLD_PWD/$PREFIX-node-failure-scenarios.log
   local FLOWS=$OLD_PWD/$PREFIX-flows
   local SCENARIO_BASE_DIR=$OLD_PWD/$PREFIX-node-failure-scenarios
   local NODES=$OLD_PWD/$PREFIX-node-set.txt

   local INDEX=1
   local NUM_NODES=$(cat $NODES | wc -l)
   local CURRENT_MACHINE=0
   local NUM_NODES_PER_MACHINE=$(($NUM_NODES / $NUM_MACHINES))
   local WORKSPACE=batfish-${USER}-node-failure
   for machine in $MACHINES; do
      local CURRENT_MACHINE=$(($CURRENT_MACHINE + 1))
      local LOCAL_NODES=${NODES}-local
      ssh $machine "mkdir -p ${SCENARIO_BASE_DIR}"
      if [ "${CURRENT_MACHINE}" -eq "${NUM_MACHINES}" ]; then
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
      echo "nohup ssh $machine bash --login -c \"cd ${OLD_PWD}; batfish_analyze_node_failures_machine ${TEST_RIG} ${PREFIX} ${SCENARIO_BASE_DIR} ${LOCAL_NODES} ${WORKSPACE} >& ${LOG_FILE}\" >& /dev/null &"
      nohup ssh $machine bash --login -c "cd ${OLD_PWD}; batfish_analyze_node_failures_machine ${TEST_RIG} ${PREFIX} ${SCENARIO_BASE_DIR} ${LOCAL_NODES} ${WORKSPACE} >& ${LOG_FILE}" >& /dev/null &
   done
}
export -f batfish_analyze_node_failures

batfish_analyze_node_failures_machine() {
   batfish_expect_args 5 $# || return 1
   local TEST_RIG=$1
   local PREFIX=$2
   local SCENARIO_BASE_DIR=$3
   local NODES=$4
   local WORKSPACE=$5
   local OLD_PWD=$PWD
   local ORIG_DP_DIR=$SCENARIO_BASE_DIR/../$PREFIX-dp
   local ORIG_QUERY_PATH=$SCENARIO_BASE_DIR/../$PREFIX-query
   local ORIG_FI_QUERY_BASE_PATH=$ORIG_QUERY_PATH/node-failure-inconsistency-query
   local ORIG_REACH_PATH=$SCENARIO_BASE_DIR/../$PREFIX-reach.smt2
   local INDEP_SERIAL_DIR=$SCENARIO_BASE_DIR/../$PREFIX-indep
   local NODE_SET_PATH=$SCENARIO_BASE_DIR/../$PREFIX-node-set

   local ORIG_FLOW_SINKS=$ORIG_DP_DIR/flow-sinks

   #Extract z3 reachability relations for no-failure scenario
   cd $SCENARIO_BASE_DIR
   batfish_generate_z3_reachability $ORIG_DP_DIR $INDEP_SERIAL_DIR $ORIG_REACH_PATH $NODE_SET_PATH || return 1
   cd $OLD_PWD

   # Find failure-inconsistent reachable packet constraints for no-failure scenario
   batfish_find_node_failure_reachable_packet_constraints $ORIG_REACH_PATH $ORIG_QUERY_PATH $ORIG_FI_QUERY_BASE_PATH $NODE_SET_PATH || return 1

   # Find failure-inconsistent black-hole packet constraints for no-failure scenario
   batfish_find_node_failure_black_hole_packet_constraints $ORIG_REACH_PATH $ORIG_QUERY_PATH $ORIG_FI_QUERY_BASE_PATH $NODE_SET_PATH || return 1

   cat $NODES | while read node; do
      local NODE_SANITIZED=$(echo $node | tr '/' '_')
      cd $SCENARIO_BASE_DIR

      # Make directory for current failure scenario
      if [ -d "$NODE_SANITIZED" ]; then
         echo "Skipping node with existing output: \"${node}\""
         continue
      else
         mkdir $NODE_SANITIZED || return 1
      fi
#      mkdir -p $NODE_SANITIZED
      # Enter node directory
      cd $NODE_SANITIZED

      local QUERY_PATH=$PWD/$PREFIX-query
      local FI_QUERY_BASE_PATH=$QUERY_PATH/node-failure-inconsistency-query
      local DST_IP_BLACKLIST_PATH=${QUERY_PATH}/blacklist-ip-${NODE_SANITIZED}

      local BGP=$PWD/$PREFIX-bgp
      local DP_DIR=$PWD/$PREFIX-dp
      local DUMP_DIR=$PWD/$PREFIX-dump
      local FLOWS=$PWD/$PREFIX-flows
      local OSPF=$PWD/$PREFIX-ospf
      local POLICY=$PWD/$PREFIX-policy
      local REACH_PATH=$PWD/$PREFIX-reach.smt2
      local ROUTES=$PWD/$PREFIX-routes

      # WORKAROUND: LogicBlox uses too much memory if we do not clear its state periodically
      batfish_nuke_reset_logicblox || return 1

      # Compute the fixed point of the control plane with failed node
      batfish_compile_blacklist_node $WORKSPACE $TEST_RIG $DUMP_DIR $INDEP_SERIAL_DIR $node $ORIG_FLOW_SINKS || return 1

      # Query bgp
      batfish_query_bgp $BGP $WORKSPACE || return 1

      # Query ospf
      batfish_query_ospf $OSPF $WORKSPACE || return 1

      # Query policy
      batfish_query_policy $POLICY $WORKSPACE || return 1

      # Query routes
      batfish_query_routes $ROUTES $WORKSPACE || return 1

      # Query data plane predicates
      batfish_query_data_plane $WORKSPACE $DP_DIR || return 1

      # Extract z3 reachability relations
      batfish_generate_z3_reachability $DP_DIR $INDEP_SERIAL_DIR $REACH_PATH $NODE_SET_PATH || return 1

      # Find failure-inconsistent black-hole packet constraints
      batfish_find_node_failure_black_hole_packet_constraints_node $REACH_PATH $QUERY_PATH $FI_QUERY_BASE_PATH $NODE_SET_PATH $node || return 1

      # Ignore packets with destination ip belonging to blacklisted node
      batfish_find_node_failure_destination_ip_blacklist_constraints $WORKSPACE $DST_IP_BLACKLIST_PATH $node || return 1

      # Generate node-failure-inconsistency concretizer queries
      batfish_generate_node_failure_inconsistency_concretizer_queries $ORIG_FI_QUERY_BASE_PATH $FI_QUERY_BASE_PATH $NODE_SET_PATH $node $DST_IP_BLACKLIST_PATH || return 1

      # Inject concrete packets into network model
      batfish_inject_packets $WORKSPACE $QUERY_PATH $DUMP_DIR || return 1

      # Query flow results from LogicBlox
      batfish_query_flows $FLOWS $WORKSPACE || return 1

   done
   cd $OLD_PWD
}
export -f batfish_analyze_node_failures_machine

batfish_compile_blacklist_node() {
   batfish_date
   local WORKSPACE=$1
   local TEST_RIG=$2
   local DUMP_DIR=$3
   local INDEP_SERIAL_DIR=$4
   local BLACKLISTED_NODE=$5
   local ORIG_FLOW_SINKS=$6
   echo ": START: Compute the fixed point of the control plane with blacklisted node: \"${BLACKLISTED_NODE}\""
   batfish_expect_args 6 $# || return 1
   batfish -workspace $WORKSPACE -testrig $TEST_RIG -sipath $INDEP_SERIAL_DIR -compile -facts -dumpcp -dumpdir $DUMP_DIR -blnode $BLACKLISTED_NODE -flowsink $ORIG_FLOW_SINKS || return 1
   batfish_date
   echo ": END: Compute the fixed point of the control plane with blacklisted node: \"${BLACKLISTED_NODE}\""
}
export -f batfish_compile_blacklist_node

batfish_find_node_failure_black_hole_packet_constraints() {
   batfish_expect_args 4 $# || return 1
   local BLACK_HOLE_PATH=$1
   local QUERY_PATH=$2
   local FI_QUERY_BASE_PATH=$3
   local NODE_SET_PATH=$4
   local NODE_SET_TEXT_PATH=${NODE_SET_PATH}.txt
   local OLD_PWD=$PWD
   local FI_QUERY_PRED_PATH=${FI_QUERY_BASE_PATH}_black-hole
   batfish_date
   echo ": START: Find black-hole packet constraints"
   mkdir -p $QUERY_PATH
   cd $QUERY_PATH
   batfish -blackhole -blackholepath $FI_QUERY_PRED_PATH -nodes $NODE_SET_PATH || return 1
   cat $NODE_SET_TEXT_PATH | $BATFISH_PARALLEL batfish_find_node_failure_black_hole_packet_constraints_helper {} $BLACK_HOLE_PATH $FI_QUERY_PRED_PATH
   if [ "${PIPESTATUS[0]}" -ne 0 -o "${PIPESTATUS[1]}" -ne 0 ]; then
      return 1
   fi
   cd $OLD_PWD
   batfish_date
   echo ": END: Find black-hole packet constraints"
}
export -f batfish_find_node_failure_black_hole_packet_constraints

batfish_find_node_failure_black_hole_packet_constraints_helper() {
   batfish_expect_args 3 $# || return 1
   local NODE=$1
   local BLACK_HOLE_PATH=$2
   local FI_QUERY_BASE_PATH=$3
   local FI_QUERY_PATH=${FI_QUERY_BASE_PATH}-${NODE}.smt2
   local FI_QUERY_OUTPUT_PATH=${FI_QUERY_PATH}.out
   batfish_date
   echo ": START: Find black-hole packet constraints for \"${NODE}\" ==> \"${FI_QUERY_OUTPUT_PATH}\""
   cat $BLACK_HOLE_PATH $FI_QUERY_PATH | batfish_time $BATFISH_Z3_DATALOG -smt2 -in 3>&1 1>$FI_QUERY_OUTPUT_PATH 2>&3
   if [ "${PIPESTATUS[0]}" -ne 0 -o "${PIPESTATUS[1]}" -ne 0 ]; then
      return 1
   fi
   batfish_date
   echo ": END: Find black-hole packet constraints for \"${NODE}\" ==> \"${FI_QUERY_OUTPUT_PATH}\""
}
export -f batfish_find_node_failure_black_hole_packet_constraints_helper

batfish_find_node_failure_black_hole_packet_constraints_node() {
   batfish_expect_args 5 $# || return 1
   local REACH_PATH=$1
   local QUERY_PATH=$2
   local FI_QUERY_BASE_PATH=$3
   local NODE_SET_PATH=$4
   local BLACKLISTED_NODE=$5
   local BLACKLISTED_NODE_SANITIZED=$(echo $BLACKLISTED_NODE | tr '/' '_')
   local NODE_SET_TEXT_PATH=${NODE_SET_PATH}.txt
   local OLD_PWD=$PWD
   local FI_QUERY_PRED_PATH=${FI_QUERY_BASE_PATH}_black-hole-${BLACKLISTED_NODE_SANITIZED}
   batfish_date
   echo ": START: Find black-hole packet constraints with blacklisted node \"${BLACKLISTED_NODE}\""
   mkdir -p $QUERY_PATH
   cd $QUERY_PATH
   batfish -blackhole -blackholepath $FI_QUERY_PRED_PATH -nodes $NODE_SET_PATH || return 1
   cat $NODE_SET_TEXT_PATH | $BATFISH_PARALLEL batfish_find_node_failure_black_hole_packet_constraints_node_helper {} $REACH_PATH $FI_QUERY_PRED_PATH $BLACKLISTED_NODE
   if [ "${PIPESTATUS[0]}" -ne 0 -o "${PIPESTATUS[1]}" -ne 0 ]; then
      return 1
   fi
   cd $OLD_PWD
   batfish_date
   echo ": END: Find black-hole packet constraints with blacklisted node \"${BLACKLISTED_NODE}\""
}
export -f batfish_find_node_failure_black_hole_packet_constraints_node

batfish_find_node_failure_black_hole_packet_constraints_node_helper() {
   batfish_expect_args 4 $# || return 1
   local NODE=$1
   local REACH_PATH=$2
   local FI_QUERY_BASE_PATH=$3
   local BLACKLISTED_NODE=$4
   if [ "${NODE}" = "${BLACKLISTED_NODE}" ]; then
      return
   fi
   local FI_QUERY_PATH=${FI_QUERY_BASE_PATH}-${NODE}.smt2
   local FI_QUERY_OUTPUT_PATH=${FI_QUERY_PATH}.out
   batfish_date
   echo ": START: Find black-hole packet constraints for \"${NODE}\" with blacklisted node \"${BLACKLISTED_NODE}\" ==> \"${FI_QUERY_OUTPUT_PATH}\""
   cat $REACH_PATH $FI_QUERY_PATH | batfish_time $BATFISH_Z3_DATALOG -smt2 -in 3>&1 1>$FI_QUERY_OUTPUT_PATH 2>&3
   if [ "${PIPESTATUS[0]}" -ne 0 -o "${PIPESTATUS[1]}" -ne 0 ]; then
      return 1
   fi
   batfish_date
   echo ": END: Find black-hole packet constraints for \"${NODE}\" with blacklisted node \"${BLACKLISTED_NODE}\" ==> \"${FI_QUERY_OUTPUT_PATH}\""
}
export -f batfish_find_node_failure_black_hole_packet_constraints_node_helper

batfish_find_node_failure_destination_ip_blacklist_constraints() {
   batfish_expect_args 3 $# || return 1
   local WORKSPACE=$1
   local OUTPUT_PATH=$2
   local BLACKLISTED_NODE=$3
   local BLACKLISTED_NODE_SANITIZED=$(echo $BLACKLISTED_NODE | tr '/' '_')
   local INTERFACE_IP_PREDICATE=SetIpInt
   local INTERFACE_IP_PATH=$PWD/${INTERFACE_IP_PREDICATE}.txt
   batfish_date
   echo ": START: Find destination ip blacklist packet constraints with blacklisted node \"${BLACKLISTED_NODE}\" ==> \"${OUTPUT_PATH}\""
   batfish -log output -workspace $WORKSPACE -query -predicates $INTERFACE_IP_PREDICATE > $INTERFACE_IP_PATH || return 1
   head -n1 $INTERFACE_IP_PATH || return 1
   cat $INTERFACE_IP_PATH | tr -d ' ' | grep "(${BLACKLISTED_NODE}," | cut -d',' -f 3 > $OUTPUT_PATH
   batfish_date
   echo ": END: Find destination ip blacklist packet constraints with blacklisted node \"${BLACKLISTED_NODE}\" ==> \"${OUTPUT_PATH}\""
}
export -f batfish_find_node_failure_destination_ip_blacklist_constraints

batfish_find_node_failure_reachable_packet_constraints() {
   batfish_expect_args 4 $# || return 1
   local REACH_PATH=$1
   local QUERY_PATH=$2
   local FI_QUERY_BASE_PATH=$3
   local NODE_SET_PATH=$4
   local NODE_SET_TEXT_PATH=${NODE_SET_PATH}.txt
   local OLD_PWD=$PWD
   local FI_QUERY_PRED_PATH=${FI_QUERY_BASE_PATH}_reachable
   batfish_date
   echo ": START: Find reachable packet constraints"
   mkdir -p $QUERY_PATH
   cd $QUERY_PATH
   batfish -reach -reachpath $FI_QUERY_PRED_PATH -nodes $NODE_SET_PATH || return 1
   cat $NODE_SET_TEXT_PATH | $BATFISH_PARALLEL batfish_find_node_failure_reachable_packet_constraints_helper {} $REACH_PATH $FI_QUERY_PRED_PATH
   if [ "${PIPESTATUS[0]}" -ne 0 -o "${PIPESTATUS[1]}" -ne 0 ]; then
      return 1
   fi
   cd $OLD_PWD
   batfish_date
   echo ": END: Find reachable packet constraints"
}
export -f batfish_find_node_failure_reachable_packet_constraints

batfish_find_node_failure_reachable_packet_constraints_helper() {
   batfish_expect_args 3 $# || return 1
   local NODE=$1
   local REACH_PATH=$2
   local FI_QUERY_BASE_PATH=$3
   local FI_QUERY_PATH=${FI_QUERY_BASE_PATH}-${NODE}.smt2
   local FI_QUERY_OUTPUT_PATH=${FI_QUERY_PATH}.out
   batfish_date
   echo ": START: Find reachable packet constraints for \"${NODE}\" ==> \"${FI_QUERY_OUTPUT_PATH}\""
   cat $REACH_PATH $FI_QUERY_PATH | batfish_time $BATFISH_Z3_DATALOG -smt2 -in 3>&1 1>$FI_QUERY_OUTPUT_PATH 2>&3
   if [ "${PIPESTATUS[0]}" -ne 0 -o "${PIPESTATUS[1]}" -ne 0 ]; then
      return 1
   fi
   batfish_date
   echo ": END: Find reachable packet constraints for \"${NODE}\" ==> \"${FI_QUERY_OUTPUT_PATH}\""
}
export -f batfish_find_node_failure_reachable_packet_constraints_helper

batfish_generate_node_failure_inconsistency_concretizer_queries() {
   batfish_date
   echo ": START: Generate node-failure-inconsistency concretizer queries"
   batfish_expect_args 5 $# || return 1
   local ORIG_FI_QUERY_BASE_PATH=$1
   local FI_QUERY_BASE_PATH=$2
   local NODE_SET_PATH=$3
   local BLACKLISTED_NODE=$4
   local DST_IP_BLACKLIST_PATH=$5
   local QUERY_PATH="$(dirname $FI_QUERY_BASE_PATH)"
   local NODE_SET_TEXT_PATH=${NODE_SET_PATH}.txt
   local OLD_PWD=$PWD
   cd $QUERY_PATH
   cat $NODE_SET_TEXT_PATH | $BATFISH_PARALLEL batfish_generate_node_failure_inconsistency_concretizer_queries_helper {} $ORIG_FI_QUERY_BASE_PATH $FI_QUERY_BASE_PATH $BLACKLISTED_NODE $DST_IP_BLACKLIST_PATH \;
   if [ "${PIPESTATUS[0]}" -ne 0 -o "${PIPESTATUS[1]}" -ne 0 ]; then
      return 1
   fi
   cd $OLD_PWD
   batfish_date
   echo ": END: Generate node-failure-inconsistency concretizer queries"
}
export -f batfish_generate_node_failure_inconsistency_concretizer_queries

batfish_generate_node_failure_inconsistency_concretizer_queries_helper() {
   batfish_expect_args 5 $# || return 1
   local NODE=$1
   local ORIG_FI_QUERY_BASE_PATH=$2
   local FI_QUERY_BASE_PATH=$3
   local BLACKLISTED_NODE=$4
   local DST_IP_BLACKLIST_PATH=$5
   if [ "${NODE}" = "${BLACKLISTED_NODE}" ]; then
      return
   fi
   local BLACKLISTED_NODE_SANITIZED=$(echo $BLACKLISTED_NODE | tr '/' '_')
   local QUERY_BASE=${FI_QUERY_BASE_PATH}-${BLACKLISTED_NODE_SANITIZED}-${NODE}
   local QUERY_OUT=${QUERY_BASE}.smt2.out
   local FI_CONCRETIZER_QUERY_BASE_PATH=${QUERY_BASE}-concrete
   local REACHABLE_QUERY_OUT=${ORIG_FI_QUERY_BASE_PATH}_reachable-${NODE}.smt2.out
   local BLACK_HOLE_QUERY_OUT=${ORIG_FI_QUERY_BASE_PATH}_black-hole-${NODE}.smt2.out
   local BLACK_HOLE_NODE_QUERY_OUT=${FI_QUERY_BASE_PATH}_black-hole-${BLACKLISTED_NODE_SANITIZED}-${NODE}.smt2.out
   batfish -conc -concin $REACHABLE_QUERY_OUT $BLACK_HOLE_NODE_QUERY_OUT -concinneg $BLACK_HOLE_QUERY_OUT -concout $FI_CONCRETIZER_QUERY_BASE_PATH -blacklistdstippath $DST_IP_BLACKLIST_PATH -concunique || return 1
   find $PWD -regextype posix-extended -regex "${FI_CONCRETIZER_QUERY_BASE_PATH}-[0-9]+.smt2" | \
      $BATFISH_NESTED_PARALLEL batfish_generate_concretizer_query_output {} $NODE \;
   if [ "${PIPESTATUS[0]}" -ne 0 -o "${PIPESTATUS[1]}" -ne 0 ]; then
      return 1
   fi
}
export -f batfish_generate_node_failure_inconsistency_concretizer_queries_helper

