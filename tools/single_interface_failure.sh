#!/usr/bin/env bash

batfish_analyze_interface_failures() {
   local TEST_RIG_RELATIVE=$1
   shift
   local PREFIX=$1
   shift
   local MACHINES="$@"
   local NUM_MACHINES=$#
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
   local LOG_FILE=$OLD_PWD/$PREFIX-interface-failure-scenarios.log
   local FLOWS=$OLD_PWD/$PREFIX-flows
   local EDGE_PREDICATE=LanAdjacent
   local SCENARIO_BASE_DIR=$OLD_PWD/$PREFIX-interface-failure-scenarios
   local INTERFACES=$OLD_PWD/$PREFIX-topology-interfaces
   
   # Extract interface lines from topology
   grep "^$EDGE_PREDICATE(" $FLOWS | cut -d'(' -f 2 | cut -d',' -f 1,2 | tr -d ' ' | sort -u > $INTERFACES
   
   local INDEX=1
   local NUM_INTERFACES=$(cat $INTERFACES | wc -l)
   local CURRENT_MACHINE=0
   local NUM_INTERFACES_PER_MACHINE=$(($NUM_INTERFACES / $NUM_MACHINES))
   local WORKSPACE=batfish-${USER}-interface-failure
   for machine in $MACHINES; do
      local CURRENT_MACHINE=$(($CURRENT_MACHINE + 1))
      local LOCAL_INTERFACES=${INTERFACES}-local
      ssh $machine "mkdir -p $SCENARIO_BASE_DIR"
      if [ "$CURRENT_MACHINE" -eq "$NUM_MACHINES" ]; then
         tail -n+$INDEX $INTERFACES | ssh $machine "cat > $LOCAL_INTERFACES"
         if [ "${PIPESTATUS[0]}" -ne 0 -o "${PIPESTATUS[1]}" -ne 0 ]; then
            return 1
         fi
      else
         sed -n -e "$INDEX,$(($INDEX + $NUM_INTERFACES_PER_MACHINE - 1))p" $INTERFACES | ssh $machine "cat > ${INTERFACES}-local"
         if [ "${PIPESTATUS[0]}" -ne 0 -o "${PIPESTATUS[1]}" -ne 0 ]; then
            return 1
         fi
         local INDEX=$(($INDEX + $NUM_INTERFACES_PER_MACHINE))
      fi
   done
   for machine in $MACHINES; do
      echo "nohup ssh $machine bash --login -c \"cd $OLD_PWD; batfish_analyze_interface_failures_machine $TEST_RIG $PREFIX $SCENARIO_BASE_DIR $LOCAL_INTERFACES $WORKSPACE >& $LOG_FILE\" >& /dev/null &"
      nohup ssh $machine bash --login -c "cd $OLD_PWD; batfish_analyze_interface_failures_machine $TEST_RIG $PREFIX $SCENARIO_BASE_DIR $LOCAL_INTERFACES $WORKSPACE >& $LOG_FILE" >& /dev/null &
   done
}
export -f batfish_analyze_interface_failures

batfish_analyze_interface_failures_machine() {
   batfish_expect_args 5 $# || return 1
   local TEST_RIG=$1
   local PREFIX=$2
   local SCENARIO_BASE_DIR=$3
   local INTERFACES=$4
   local WORKSPACE=$5
   local OLD_PWD=$PWD
   local ORIG_DP_DIR=$SCENARIO_BASE_DIR/../$PREFIX-dp
   local ORIG_QUERY_PATH=$SCENARIO_BASE_DIR/../$PREFIX-query
   local ORIG_FI_QUERY_BASE_PATH=$ORIG_QUERY_PATH/interface-failure-inconsistency-query
   local ORIG_REACH_PATH=$SCENARIO_BASE_DIR/../$PREFIX-reach.smt2
   local INDEP_SERIAL_DIR=$SCENARIO_BASE_DIR/../$PREFIX-indep
   local NODE_SET_PATH=$SCENARIO_BASE_DIR/../$PREFIX-node-set

   local ORIG_FLOW_SINKS=$ORIG_DP_DIR/flow-sinks

   #Extract z3 reachability relations for no-failure scenario
   cd $SCENARIO_BASE_DIR
   batfish_generate_z3_reachability $ORIG_DP_DIR $INDEP_SERIAL_DIR $ORIG_REACH_PATH $NODE_SET_PATH || return 1
   cd $OLD_PWD

   # Find failure-inconsistent reachable packet constraints for no-failure scenario
   batfish_find_interface_failure_reachable_packet_constraints $ORIG_REACH_PATH $ORIG_QUERY_PATH $ORIG_FI_QUERY_BASE_PATH $NODE_SET_PATH || return 1

   # Find failure-inconsistent black-hole packet constraints for no-failure scenario
   batfish_find_interface_failure_black_hole_packet_constraints $ORIG_REACH_PATH $ORIG_QUERY_PATH $ORIG_FI_QUERY_BASE_PATH $NODE_SET_PATH || return 1

   cat $INTERFACES | while read interface; do
      local INTERFACE_SANITIZED=$(echo $interface | tr '/' '_')
      cd $SCENARIO_BASE_DIR
      
      # Make directory for current failure scenario
      if [ -d "$INTERFACE_SANITIZED" ]; then
         echo "Skipping interface with existing output: \"${interface}\""
         continue
      else
         mkdir $INTERFACE_SANITIZED || return 1
      fi
#      mkdir -p $INTERFACE_SANITIZED
      # Enter interface directory
      cd $INTERFACE_SANITIZED

      local QUERY_PATH=$PWD/$PREFIX-query
      local DP_DIR=$PWD/$PREFIX-dp
      local DUMP_DIR=$PWD/$PREFIX-dump
      local FLOWS=$PWD/$PREFIX-flows
      local REACH_PATH=$PWD/$PREFIX-reach.smt2
      local PREDS_PATH=$PWD/$PREFIX-preds
      local VENDOR_SERIAL_DIR=$PWD/$PREFIX-vendor
      local FI_QUERY_BASE_PATH=$QUERY_PATH/interface-failure-inconsistency-query
      local DST_IP_BLACKLIST_PATH=${QUERY_PATH}/blacklist-ip-${INTERFACE_SANITIZED}

      # WORKAROUND: LogicBlox uses too much memory if we do not clear its state periodically
      batfish_nuke_reset_logicblox || return 1

      # Compute the fixed point of the control plane with failed interface
      batfish_compile_blacklist_interface $WORKSPACE $TEST_RIG $DUMP_DIR $INDEP_SERIAL_DIR $interface $ORIG_FLOW_SINKS || return 1

      # Get interesting predicate data
      batfish -log output -workspace $WORKSPACE -query -predicates InstalledRoute BestOspfE2Route BestOspfE1Route OspfRoute_advertiser OspfE2Route > $PREDS_PATH || return 1
      
      # Query data plane predicates
      batfish_query_data_plane $WORKSPACE $DP_DIR || return 1

      # Extract z3 reachability relations
      batfish_generate_z3_reachability $DP_DIR $INDEP_SERIAL_DIR $REACH_PATH $NODE_SET_PATH || return 1
   
      # Find failure-inconsistent black-hole packet constraints
      batfish_find_interface_failure_black_hole_packet_constraints_interface $REACH_PATH $QUERY_PATH $FI_QUERY_BASE_PATH $NODE_SET_PATH $interface || return 1

      # Ignore packets with destination ip equal to that assigned to blacklisted interface
      batfish_find_interface_failure_destination_ip_blacklist_constraints $WORKSPACE $DST_IP_BLACKLIST_PATH $interface || return 1
      
      # Generate interface-failure-inconsistency concretizer queries
      batfish_generate_interface_failure_inconsistency_concretizer_queries $ORIG_FI_QUERY_BASE_PATH $FI_QUERY_BASE_PATH $NODE_SET_PATH $interface $DST_IP_BLACKLIST_PATH || return 1

      # Inject concrete packets into network model
      batfish_inject_packets $WORKSPACE $QUERY_PATH $DUMP_DIR || return 1

      # Query flow results from LogicBlox
      batfish_query_flows $FLOWS $WORKSPACE || return 1
      
   done
   cd $OLD_PWD
}
export -f batfish_analyze_interface_failures_machine

batfish_compile_blacklist_interface() {
   batfish_date
   local WORKSPACE=$1
   local TEST_RIG=$2
   local DUMP_DIR=$3
   local INDEP_SERIAL_DIR=$4
   local BLACKLISTED_INTERFACE=$5
   local ORIG_FLOW_SINKS=$6
   echo ": START: Compute the fixed point of the control plane with blacklisted interface: \"${BLACKLISTED_INTERFACE}\""
   batfish_expect_args 6 $# || return 1
   batfish -workspace $WORKSPACE -testrig $TEST_RIG -sipath $INDEP_SERIAL_DIR -compile -facts -dumpcp -dumpdir $DUMP_DIR -blint $BLACKLISTED_INTERFACE -flowsink $ORIG_FLOW_SINKS || return 1
   batfish_date
   echo ": END: Compute the fixed point of the control plane with blacklisted interface: \"${BLACKLISTED_INTERFACE}\""
}
export -f batfish_compile_blacklist_interface

batfish_find_interface_failure_black_hole_packet_constraints() {
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
   cat $NODE_SET_TEXT_PATH | $BATFISH_PARALLEL batfish_find_interface_failure_black_hole_packet_constraints_helper {} $BLACK_HOLE_PATH $FI_QUERY_PRED_PATH
   if [ "${PIPESTATUS[0]}" -ne 0 -o "${PIPESTATUS[1]}" -ne 0 ]; then
      return 1
   fi
   cd $OLD_PWD
   batfish_date
   echo ": END: Find black-hole packet constraints"
}
export -f batfish_find_interface_failure_black_hole_packet_constraints

batfish_find_interface_failure_black_hole_packet_constraints_helper() {
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
export -f batfish_find_interface_failure_black_hole_packet_constraints_helper

batfish_find_interface_failure_black_hole_packet_constraints_interface() {
   batfish_expect_args 5 $# || return 1
   local REACH_PATH=$1
   local QUERY_PATH=$2
   local FI_QUERY_BASE_PATH=$3
   local NODE_SET_PATH=$4
   local BLACKLISTED_INTERFACE=$5
   local BLACKLISTED_INTERFACE_SANITIZED=$(echo $BLACKLISTED_INTERFACE | tr '/' '_')
   local NODE_SET_TEXT_PATH=${NODE_SET_PATH}.txt
   local OLD_PWD=$PWD
   local FI_QUERY_PRED_PATH=${FI_QUERY_BASE_PATH}_black-hole-${BLACKLISTED_INTERFACE_SANITIZED}
   batfish_date
   echo ": START: Find black-hole packet constraints with blacklisted interface \"${BLACKLISTED_INTERFACE}\""
   mkdir -p $QUERY_PATH
   cd $QUERY_PATH
   batfish -blackhole -blackholepath $FI_QUERY_PRED_PATH -nodes $NODE_SET_PATH || return 1
   cat $NODE_SET_TEXT_PATH | $BATFISH_PARALLEL batfish_find_interface_failure_black_hole_packet_constraints_interface_helper {} $REACH_PATH $FI_QUERY_PRED_PATH $BLACKLISTED_INTERFACE
   if [ "${PIPESTATUS[0]}" -ne 0 -o "${PIPESTATUS[1]}" -ne 0 ]; then
      return 1
   fi
   cd $OLD_PWD
   batfish_date
   echo ": END: Find black-hole packet constraints with blacklisted interface \"${BLACKLISTED_INTERFACE}\""
}
export -f batfish_find_interface_failure_black_hole_packet_constraints_interface

batfish_find_interface_failure_black_hole_packet_constraints_interface_helper() {
   batfish_expect_args 4 $# || return 1
   local NODE=$1
   local REACH_PATH=$2
   local FI_QUERY_BASE_PATH=$3
   local BLACKLISTED_INTERFACE=$4
   local FI_QUERY_PATH=${FI_QUERY_BASE_PATH}-${NODE}.smt2
   local FI_QUERY_OUTPUT_PATH=${FI_QUERY_PATH}.out
   batfish_date
   echo ": START: Find black-hole packet constraints for \"${NODE}\" with blacklisted interface \"${BLACKLISTED_INTERFACE}\" ==> \"${FI_QUERY_OUTPUT_PATH}\""
   cat $REACH_PATH $FI_QUERY_PATH | batfish_time $BATFISH_Z3_DATALOG -smt2 -in 3>&1 1>$FI_QUERY_OUTPUT_PATH 2>&3
   if [ "${PIPESTATUS[0]}" -ne 0 -o "${PIPESTATUS[1]}" -ne 0 ]; then
      return 1
   fi
   batfish_date
   echo ": END: Find black-hole packet constraints for \"${NODE}\" with blacklisted interface \"${BLACKLISTED_INTERFACE}\" ==> \"${FI_QUERY_OUTPUT_PATH}\""
}
export -f batfish_find_interface_failure_black_hole_packet_constraints_interface_helper

batfish_find_interface_failure_destination_ip_blacklist_constraints() {
   batfish_expect_args 3 $# || return 1
   local WORKSPACE=$1
   local DST_IP_BLACKLIST_PATH=$2
   local BLACKLISTED_INTERFACE=$3
   local BLACKLISTED_INTERFACE_SANITIZED=$(echo $BLACKLISTED_INTERFACE | tr '/' '_')
   local INTERFACE_IP_PREDICATE=SetIpInt
   local INTERFACE_IP_PATH=$PWD/${INTERFACE_IP_PREDICATE}.txt
   batfish_date
   echo ": START: Find destination ip blacklist packet constraints with blacklisted interface \"${BLACKLISTED_INTERFACE}\" ==> \"${DST_IP_BLACKLIST_PATH}\""
   batfish -log output -workspace $WORKSPACE -query -predicates $INTERFACE_IP_PREDICATE > $INTERFACE_IP_PATH || return 1
   head -n1 $INTERFACE_IP_PATH || return 1
   cat $INTERFACE_IP_PATH | tr -d ' ' | grep "(${BLACKLISTED_INTERFACE}," | cut -d',' -f 3 > $DST_IP_BLACKLIST_PATH
   batfish_date
   echo ": END: Find destination ip blacklist packet constraints with blacklisted interface \"${BLACKLISTED_INTERFACE}\" ==> \"${DST_IP_BLACKLIST_PATH}\""
}
export -f batfish_find_interface_failure_destination_ip_blacklist_constraints

batfish_find_interface_failure_reachable_packet_constraints() {
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
   cat $NODE_SET_TEXT_PATH | $BATFISH_PARALLEL batfish_find_interface_failure_reachable_packet_constraints_helper {} $REACH_PATH $FI_QUERY_PRED_PATH
   if [ "${PIPESTATUS[0]}" -ne 0 -o "${PIPESTATUS[1]}" -ne 0 ]; then
      return 1
   fi
   cd $OLD_PWD
   batfish_date
   echo ": END: Find reachable packet constraints"
}
export -f batfish_find_interface_failure_reachable_packet_constraints

batfish_find_interface_failure_reachable_packet_constraints_helper() {
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
export -f batfish_find_interface_failure_reachable_packet_constraints_helper

batfish_generate_interface_failure_inconsistency_concretizer_queries() {
   batfish_date
   echo ": START: Generate interface-failure-inconsistency concretizer queries"
   batfish_expect_args 5 $# || return 1
   local ORIG_FI_QUERY_BASE_PATH=$1
   local FI_QUERY_BASE_PATH=$2
   local NODE_SET_PATH=$3
   local BLACKLISTED_INTERFACE=$4
   local DST_IP_BLACKLIST_PATH=$5
   local QUERY_PATH="$(dirname $FI_QUERY_BASE_PATH)"
   local NODE_SET_TEXT_PATH=${NODE_SET_PATH}.txt
   local OLD_PWD=$PWD
   cd $QUERY_PATH
   cat $NODE_SET_TEXT_PATH | $BATFISH_PARALLEL batfish_generate_interface_failure_inconsistency_concretizer_queries_helper {} $ORIG_FI_QUERY_BASE_PATH $FI_QUERY_BASE_PATH $BLACKLISTED_INTERFACE $DST_IP_BLACKLIST_PATH \;
   if [ "${PIPESTATUS[0]}" -ne 0 -o "${PIPESTATUS[1]}" -ne 0 ]; then
      return 1
   fi
   cd $OLD_PWD
   batfish_date
   echo ": END: Generate interface-failure-inconsistency concretizer queries"
}
export -f batfish_generate_interface_failure_inconsistency_concretizer_queries

batfish_generate_interface_failure_inconsistency_concretizer_queries_helper() {
   batfish_expect_args 5 $# || return 1
   local NODE=$1
   local ORIG_FI_QUERY_BASE_PATH=$2
   local FI_QUERY_BASE_PATH=$3
   local BLACKLISTED_INTERFACE=$4
   local DST_IP_BLACKLIST_PATH=$5
   local BLACKLISTED_INTERFACE_SANITIZED=$(echo $BLACKLISTED_INTERFACE | tr '/' '_')
   local QUERY_BASE=${FI_QUERY_BASE_PATH}-${BLACKLISTED_INTERFACE_SANITIZED}-${NODE}
   local QUERY_OUT=${QUERY_BASE}.smt2.out
   local FI_CONCRETIZER_QUERY_BASE_PATH=${QUERY_BASE}-concrete
   local REACHABLE_QUERY_OUT=${ORIG_FI_QUERY_BASE_PATH}_reachable-${NODE}.smt2.out
   local BLACK_HOLE_QUERY_OUT=${ORIG_FI_QUERY_BASE_PATH}_black-hole-${NODE}.smt2.out
   local BLACK_HOLE_INTERFACE_QUERY_OUT=${FI_QUERY_BASE_PATH}_black-hole-${BLACKLISTED_INTERFACE_SANITIZED}-${NODE}.smt2.out
   batfish -conc -concin $REACHABLE_QUERY_OUT $BLACK_HOLE_INTERFACE_QUERY_OUT -concinneg $BLACK_HOLE_QUERY_OUT -concout $FI_CONCRETIZER_QUERY_BASE_PATH -blacklistdstippath $DST_IP_BLACKLIST_PATH -concunique || return 1
   find $PWD -regextype posix-extended -regex "${FI_CONCRETIZER_QUERY_BASE_PATH}-[0-9]+.smt2" | \
      $BATFISH_NESTED_PARALLEL batfish_generate_concretizer_query_output {} $NODE \;
   if [ "${PIPESTATUS[0]}" -ne 0 -o "${PIPESTATUS[1]}" -ne 0 ]; then
      return 1
   fi
}
export -f batfish_generate_interface_failure_inconsistency_concretizer_queries_helper
