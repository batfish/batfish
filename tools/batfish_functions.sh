#!/usr/bin/env bash

export BATFISH_SOURCED_SCRIPT=$BASH_SOURCE
export BATFISH_ROOT=$(readlink -f $(dirname $BATFISH_SOURCED_SCRIPT)/..)
export BATFISH_PATH=$BATFISH_ROOT/projects/batfish
export BATFISH_TEST_RIG_PATH=$BATFISH_ROOT/test_rigs
export BATFISH=$BATFISH_PATH/batfish
export BATFISH_Z3=$(which z3)
export BATFISH_Z3_DATALOG="$BATFISH_Z3 fixedpoint.engine=datalog fixedpoint.default_relation=hassel_diff fixedpoint.unbound_compressor=false fixedpoint.print_answer=true fixedpoint.inline_eager=false"

batfish() {
   # if cygwin, shift and replace each parameter
   if [ "Cygwin" = "$(uname -o)" ]; then
      local NUMARGS=$#
      local IGNORE_NEXT_ARG=no;
      for i in $(seq 1 $NUMARGS); do
         if [ "$IGNORE_NEXT_ARG" = "yes" ]; then
            local IGNORE_NEXT_ARG=no
            continue
         fi
         local CURRENT_ARG=$1
         if [ "$CURRENT_ARG" = "-logicdir" ]; then
            local IGNORE_NEXT_ARG=yes
         fi
         local NEW_ARG="$(cygpath -w -- $CURRENT_ARG)"
         set -- "$@" "$NEW_ARG"
         shift
      done
   fi
   if [ "$BATFISH_PRINT_CMDLINE" = "yes" ]; then
      echo "$BATFISH $BATFISH_COMMON_ARGS $@"
   fi
   $BATFISH $BATFISH_COMMON_ARGS $@
}
export -f batfish

batfish_confirm_analyze() {
   BATFISH_CONFIRM=batfish_confirm batfish_analyze $@
}
export -f batfish_confirm_analyze
   
batfish_confirm_analyze_role_reachability() {
   BATFISH_CONFIRM=batfish_confirm batfish_analyze_role_reachability $@
}
export -f batfish_confirm_analyze_role_reachability
   
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
      BATFISH_COMMON_ARGS="-flowsink $ORIG_FLOW_SINKS" batfish_compile_blacklist_node $WORKSPACE $TEST_RIG $DUMP_DIR $INDEP_SERIAL_DIR $NODE || return 1

      # Query data plane predicates
      batfish_query_data_plane $WORKSPACE $DP_DIR || return 1

      # Extract z3 reachability relations
      BATFISH_COMMON_ARGS="-blnode $NODE" batfish_generate_z3_reachability $DP_DIR $INDEP_SERIAL_DIR $REACH_PATH $NODE_SET_PATH || return 1
   
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
      batfish_compile_blacklist_interface $WORKSPACE $TEST_RIG $DUMP_DIR $INDEP_SERIAL_DIR $interface || return 1

      # Get interesting predicate data
      batfish -log 0 -workspace $WORKSPACE -query -predicates InstalledRoute BestOspfE2Route BestOspfE1Route OspfRoute_advertiser OspfE2Route > $PREDS_PATH || return 1
      
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

batfish_analyze_role_reachability() {
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
   local REACH_PATH=$OLD_PWD/$PREFIX-reach.smt2
   local NODE_SET_PATH=$OLD_PWD/$PREFIX-node-set
   local QUERY_PATH=$OLD_PWD/$PREFIX-query
   local RR_QUERY_BASE_PATH=$QUERY_PATH/role-reachability-query
   local DUMP_DIR=$OLD_PWD/$PREFIX-dump
   local FLOWS=$OLD_PWD/$PREFIX-flows
   local ROUTES=$OLD_PWD/$PREFIX-routes
   local VENDOR_SERIAL_DIR=$OLD_PWD/$PREFIX-vendor
   local INDEP_SERIAL_DIR=$OLD_PWD/$PREFIX-indep
   local DP_DIR=$OLD_PWD/$PREFIX-dp
   local NODE_ROLES_PATH=$OLD_PWD/$PREFIX-node_roles
   local ROLE_SET_PATH=$OLD_PWD/$PREFIX-role_set

   echo "Parse vendor configuration files and serialize vendor structures"
   $BATFISH_CONFIRM && { batfish_serialize_vendor_with_roles $TEST_RIG $VENDOR_SERIAL_DIR $NODE_ROLES_PATH || return 1 ; }

   echo "Parse vendor structures and serialize vendor-independent structures"
   $BATFISH_CONFIRM && { batfish_serialize_independent $VENDOR_SERIAL_DIR $INDEP_SERIAL_DIR || return 1 ; }

   echo "Compute the fixed point of the control plane"
   $BATFISH_CONFIRM && { batfish_compile $WORKSPACE $TEST_RIG $DUMP_DIR $INDEP_SERIAL_DIR || return 1 ; }

   echo "Query routes"
   $BATFISH_CONFIRM && { batfish_query_routes $ROUTES $WORKSPACE || return 1 ; }

   echo "Query data plane predicates"
   $BATFISH_CONFIRM && { batfish_query_data_plane $WORKSPACE $DP_DIR || return 1 ; }

   echo "Extract z3 reachability relations"
   $BATFISH_CONFIRM && { batfish_generate_z3_reachability $DP_DIR $INDEP_SERIAL_DIR $REACH_PATH $NODE_SET_PATH || return 1 ; }

   echo "Find role-reachability packet constraints"
   $BATFISH_CONFIRM && { batfish_find_role_reachability_packet_constraints $REACH_PATH $QUERY_PATH $RR_QUERY_BASE_PATH $NODE_SET_PATH $NODE_ROLES_PATH $ROLE_SET_PATH || return 1 ; }

   echo "Generate role-reachability concretizer queries"
   $BATFISH_CONFIRM && { batfish_generate_role_reachability_concretizer_queries $RR_QUERY_BASE_PATH $NODE_ROLES_PATH || return 1 ; }

   echo "Inject concrete packets into network model"
   $BATFISH_CONFIRM && { batfish_inject_packets_with_role_flow_duplication $WORKSPACE $QUERY_PATH $DUMP_DIR || return 1 ; }

   echo "Query flow results from LogicBlox"
   $BATFISH_CONFIRM && { batfish_query_flows $FLOWS $WORKSPACE || return 1 ; }
}
export -f batfish_analyze_role_reachability

batfish_build() {
   local RESTORE_FILE='cygwin-symlink-restore-data'
   local OLD_PWD=$(pwd)
   cd $BATFISH_PATH
   if [ "Cygwin" = "$(uname -o)" -a ! -e "$RESTORE_FILE" ]; then
      echo "Replacing symlinks (Cygwin workaround)"
      ./cygwin-replace-symlinks
   fi
   ant $@ || { cd $OLD_PWD ; return 1 ; } 
   cd $OLD_PWD
}
export -f batfish_build

batfish_compile() {
   date | tr -d '\n'
   echo ": START: Compute the fixed point of the control plane"
   batfish_expect_args 4 $# || return 1
   local WORKSPACE=$1
   local TEST_RIG=$2
   local DUMP_DIR=$3
   local INDEP_SERIAL_DIR=$4
   batfish -workspace $WORKSPACE -testrig $TEST_RIG -sipath $INDEP_SERIAL_DIR -compile -facts -dumpcp -dumpdir $DUMP_DIR || return 1
   date | tr -d '\n'
   echo ": END: Compute the fixed point of the control plane"
}
export -f batfish_compile

batfish_compile_blacklist_interface() {
   date | tr -d '\n'
   local WORKSPACE=$1
   local TEST_RIG=$2
   local DUMP_DIR=$3
   local INDEP_SERIAL_DIR=$4
   local BLACKLISTED_INTERFACE=$5
   echo ": START: Compute the fixed point of the control plane with blacklisted interface: $BLACKLISTED_INTERFACE"
   batfish_expect_args 5 $# || return 1
   batfish -workspace $WORKSPACE -testrig $TEST_RIG -sipath $INDEP_SERIAL_DIR -compile -facts -dumpcp -dumpdir $DUMP_DIR -blint $BLACKLISTED_INTERFACE || return 1
   date | tr -d '\n'
   echo ": END: Compute the fixed point of the control plane with blacklisted interface: \"$BLACKLISTED_INTERFACE\""
}
export -f batfish_compile_blacklist_interface

batfish_compile_blacklist_node() {
   date | tr -d '\n'
   local WORKSPACE=$1
   local TEST_RIG=$2
   local DUMP_DIR=$3
   local INDEP_SERIAL_DIR=$4
   local BLACKLISTED_NODE=$5
   echo ": START: Compute the fixed point of the control plane with blacklisted node: $BLACKLISTED_NODE"
   batfish_expect_args 5 $# || return 1
   batfish -workspace $WORKSPACE -testrig $TEST_RIG -sipath $INDEP_SERIAL_DIR -compile -facts -dumpcp -dumpdir $DUMP_DIR -blnode $BLACKLISTED_NODE || return 1
   date | tr -d '\n'
   echo ": END: Compute the fixed point of the control plane with blacklisted node: \"$BLACKLISTED_NODE\""
}
export -f batfish_compile_blacklist_node

batfish_confirm() {
   # call with a prompt string or use a default
   read -r -p "${1:-Are you sure? [y/N]} " response < /dev/tty
   case $response in
      [yY][eE][sS]|[yY])
         true
      ;;
      *)
         false
      ;;
   esac
}
export -f batfish_confirm

batfish_expect_args() {
   local EXPECTED_NUMARGS=$1
   local ACTUAL_NUMARGS=$2
   if [ "$EXPECTED_NUMARGS" -ne "$ACTUAL_NUMARGS" ]; then
      echo "${FUNCNAME[1]}: Expected $EXPECTED_NUMARGS arguments, but got $ACTUAL_NUMARGS" >&2
      return 1
   fi   
}
export -f batfish_expect_args

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
   date | tr -d '\n'
   echo ": START: Find accept packet constraints"
   mkdir -p $QUERY_PATH
   cd $QUERY_PATH
   batfish -reach -reachpath $DI_QUERY_PRED_PATH -nodes $NODE_SET_PATH -blnode $BLACKLISTED_NODE || return 1
   cat $NODE_SET_TEXT_PATH | parallel --halt 2 batfish_find_destination_consistency_accept_packet_constraints_helper {} $REACH_PATH $DI_QUERY_PRED_PATH $BLACKLISTED_NODE
   if [ "${PIPESTATUS[0]}" -ne 0 -o "${PIPESTATUS[1]}" -ne 0 ]; then
      return 1
   fi
   cd $OLD_PWD
   date | tr -d '\n'
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
   date | tr -d '\n'
   echo ": START: Find accept packet constraints for \"$NODE\" ==> \"$DI_QUERY_OUTPUT_PATH\""
   cat $REACH_PATH $DI_QUERY_PATH | batfish_time $BATFISH_Z3_DATALOG -smt2 -in 3>&1 1>$DI_QUERY_OUTPUT_PATH 2>&3
   if [ "${PIPESTATUS[0]}" -ne 0 -o "${PIPESTATUS[1]}" -ne 0 ]; then
      return 1
   fi
   date | tr -d '\n'
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
   date | tr -d '\n'
   echo ": START: Find node accept packet constraints for node: \"$BLACKLIST_NODE"
   mkdir -p $QUERY_PATH
   cd $QUERY_PATH
   batfish -reach -reachpath $DI_QUERY_PRED_PATH -nodes $NODE_SET_PATH -acceptnode $BLACKLIST_NODE || return 1
   cat $NODE_SET_TEXT_PATH | parallel --halt 2 batfish_find_destination_consistency_node_accept_packet_constraints_helper {} $REACH_PATH $DI_QUERY_PRED_PATH $BLACKLIST_NODE
   if [ "${PIPESTATUS[0]}" -ne 0 -o "${PIPESTATUS[1]}" -ne 0 ]; then
      return 1
   fi
   cd $OLD_PWD
   date | tr -d '\n'
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
   date | tr -d '\n'
   echo ": START: Find node accept packet constraints for \"$NODE\" ==> \"$DI_QUERY_OUTPUT_PATH\""
   cat $REACH_PATH $DI_QUERY_PATH | batfish_time $BATFISH_Z3_DATALOG -smt2 -in 3>&1 1>$DI_QUERY_OUTPUT_PATH 2>&3
   if [ "${PIPESTATUS[0]}" -ne 0 -o "${PIPESTATUS[1]}" -ne 0 ]; then
      return 1
   fi
   date | tr -d '\n'
   echo ": END: Find node accept packet constraints for \"$NODE\" ==> \"$DI_QUERY_OUTPUT_PATH\""
}
export -f batfish_find_destination_consistency_node_accept_packet_constraints_helper

batfish_find_interface_failure_black_hole_packet_constraints() {
   batfish_expect_args 4 $# || return 1
   local BLACK_HOLE_PATH=$1
   local QUERY_PATH=$2
   local FI_QUERY_BASE_PATH=$3
   local NODE_SET_PATH=$4
   local NODE_SET_TEXT_PATH=${NODE_SET_PATH}.txt
   local OLD_PWD=$PWD
   local FI_QUERY_PRED_PATH=${FI_QUERY_BASE_PATH}_black-hole
   date | tr -d '\n'
   echo ": START: Find black-hole packet constraints"
   mkdir -p $QUERY_PATH
   cd $QUERY_PATH
   batfish -blackhole -blackholepath $FI_QUERY_PRED_PATH -nodes $NODE_SET_PATH || return 1
   cat $NODE_SET_TEXT_PATH | parallel --halt 2 batfish_find_interface_failure_black_hole_packet_constraints_helper {} $BLACK_HOLE_PATH $FI_QUERY_PRED_PATH
   if [ "${PIPESTATUS[0]}" -ne 0 -o "${PIPESTATUS[1]}" -ne 0 ]; then
      return 1
   fi
   cd $OLD_PWD
   date | tr -d '\n'
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
   date | tr -d '\n'
   echo ": START: Find black-hole packet constraints for \"$NODE\" ==> \"$FI_QUERY_OUTPUT_PATH\""
   cat $BLACK_HOLE_PATH $FI_QUERY_PATH | batfish_time $BATFISH_Z3_DATALOG -smt2 -in 3>&1 1>$FI_QUERY_OUTPUT_PATH 2>&3
   if [ "${PIPESTATUS[0]}" -ne 0 -o "${PIPESTATUS[1]}" -ne 0 ]; then
      return 1
   fi
   date | tr -d '\n'
   echo ": END: Find black-hole packet constraints for \"$NODE\" ==> \"$FI_QUERY_OUTPUT_PATH\""
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
   date | tr -d '\n'
   echo ": START: Find black-hole packet constraints with blacklisted interface \"$BLACKLISTED_INTERFACE\""
   mkdir -p $QUERY_PATH
   cd $QUERY_PATH
   batfish -blackhole -blackholepath $FI_QUERY_PRED_PATH -nodes $NODE_SET_PATH || return 1
   cat $NODE_SET_TEXT_PATH | parallel --halt 2 batfish_find_interface_failure_black_hole_packet_constraints_interface_helper {} $REACH_PATH $FI_QUERY_PRED_PATH $BLACKLISTED_INTERFACE
   if [ "${PIPESTATUS[0]}" -ne 0 -o "${PIPESTATUS[1]}" -ne 0 ]; then
      return 1
   fi
   cd $OLD_PWD
   date | tr -d '\n'
   echo ": END: Find black-hole packet constraints with blacklisted interface \"$BLACKLISTED_INTERFACE\""
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
   date | tr -d '\n'
   echo ": START: Find black-hole packet constraints for \"$NODE\" with blacklisted interface \"$BLACKLISTED_INTERFACE\" ==> \"$FI_QUERY_OUTPUT_PATH\""
   cat $REACH_PATH $FI_QUERY_PATH | batfish_time $BATFISH_Z3_DATALOG -smt2 -in 3>&1 1>$FI_QUERY_OUTPUT_PATH 2>&3
   if [ "${PIPESTATUS[0]}" -ne 0 -o "${PIPESTATUS[1]}" -ne 0 ]; then
      return 1
   fi
   date | tr -d '\n'
   echo ": END: Find black-hole packet constraints for \"$NODE\" with blacklisted interface \"$BLACKLISTED_INTERFACE\" ==> \"$FI_QUERY_OUTPUT_PATH\""
}
export -f batfish_find_interface_failure_black_hole_packet_constraints_interface_helper

batfish_find_interface_failure_destination_ip_blacklist_constraints() {
   batfish_expect_args 3 $# || return 1
   local WORKSPACE=$1
   local OUTPUT_PATH=$2
   local BLACKLISTED_INTERFACE=$3
   local BLACKLISTED_INTERFACE_SANITIZED=$(echo $BLACKLISTED_INTERFACE | tr '/' '_')
   local INTERFACE_IP_PREDICATE=SetIpInt
   local INTERFACE_IP_PATH=$PWD/${INTERFACE_IP_PREDICATE}.txt
   date | tr -d '\n'
   echo ": START: Find destination ip blacklist packet constraints with blacklisted interface \"${BLACKLISTED_INTERFACE}\" ==> \"${OUTPUT_PATH}\""
   batfish -log 0 -workspace $WORKSPACE -query -predicates $INTERFACE_IP_PREDICATE > $INTERFACE_IP_PATH || return 1
   head -n1 $INTERFACE_IP_PATH || return 1
   cat $INTERFACE_IP_PATH | tr -d ' ' | grep "$BLACKLISTED_INTERFACE" | cut -d',' -f 3 > $OUTPUT_PATH
   date | tr -d '\n'
   echo ": END: Find destination ip blacklist packet constraints with blacklisted interface \"${BLACKLISTED_INTERFACE}\" ==> \"${OUTPUT_PATH}\""
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
   date | tr -d '\n'
   echo ": START: Find reachable packet constraints"
   mkdir -p $QUERY_PATH
   cd $QUERY_PATH
   batfish -reach -reachpath $FI_QUERY_PRED_PATH -nodes $NODE_SET_PATH || return 1
   cat $NODE_SET_TEXT_PATH | parallel --halt 2 batfish_find_interface_failure_reachable_packet_constraints_helper {} $REACH_PATH $FI_QUERY_PRED_PATH
   if [ "${PIPESTATUS[0]}" -ne 0 -o "${PIPESTATUS[1]}" -ne 0 ]; then
      return 1
   fi
   cd $OLD_PWD
   date | tr -d '\n'
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
   date | tr -d '\n'
   echo ": START: Find reachable packet constraints for \"$NODE\" ==> \"$FI_QUERY_OUTPUT_PATH\""
   cat $REACH_PATH $FI_QUERY_PATH | batfish_time $BATFISH_Z3_DATALOG -smt2 -in 3>&1 1>$FI_QUERY_OUTPUT_PATH 2>&3
   if [ "${PIPESTATUS[0]}" -ne 0 -o "${PIPESTATUS[1]}" -ne 0 ]; then
      return 1
   fi
   date | tr -d '\n'
   echo ": END: Find reachable packet constraints for \"$NODE\" ==> \"$FI_QUERY_OUTPUT_PATH\""
}
export -f batfish_find_interface_failure_reachable_packet_constraints_helper

batfish_find_multipath_inconsistent_packet_constraints() {
   date | tr -d '\n'
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
   date | tr -d '\n'
   echo ": END: Find inconsistent packet constraints"
}
export -f batfish_find_multipath_inconsistent_packet_constraints

batfish_find_multipath_inconsistent_packet_constraints_helper() {
   batfish_expect_args 3 $# || return 1
   local NODE=$1
   local REACH_PATH=$2
   local MPI_QUERY_BASE_PATH=$3
   date | tr -d '\n'
   local MPI_QUERY_PATH=${MPI_QUERY_BASE_PATH}-${NODE}.smt2
   local MPI_QUERY_OUTPUT_PATH=${MPI_QUERY_PATH}.out
   echo ": START: Find inconsistent packet constraints for \"$NODE\" (\"$MPI_QUERY_OUTPUT_PATH\")"
   cat $REACH_PATH $MPI_QUERY_PATH | batfish_time $BATFISH_Z3_DATALOG -smt2 -in 3>&1 1> $MPI_QUERY_OUTPUT_PATH 2>&3
   if [ "${PIPESTATUS[0]}" -ne 0 -o "${PIPESTATUS[1]}" -ne 0 ]; then
      return 1
   fi
   date | tr -d '\n'
   echo ": END: Find inconsistent packet constraints for \"$NODE\" (\"$MPI_QUERY_OUTPUT_PATH\")"
}
export -f batfish_find_multipath_inconsistent_packet_constraints_helper

batfish_find_role_reachability_packet_constraints() {
   date | tr -d '\n'
   echo ": START: Find role-reachability packet constraints"
   batfish_expect_args 6 $# || return 1
   local REACH_PATH=$1
   local QUERY_PATH=$2
   local QUERY_BASE_PATH=$3
   local NODE_SET_PATH=$4
   local NODE_ROLES_PATH=$5
   local ROLE_SET_PATH=$6
   local NODE_SET_TEXT_PATH=${NODE_SET_PATH}.txt
   local OLD_PWD=$PWD
   mkdir -p $QUERY_PATH
   cd $QUERY_PATH
   batfish -rr -rrpath $QUERY_BASE_PATH -nodes $NODE_SET_PATH -nrpath $NODE_ROLES_PATH -rspath $ROLE_SET_PATH || return 1
   cat $NODE_SET_TEXT_PATH | while read NODE
   do
      cat $ROLE_SET_PATH | while read ROLE
      do
         echo "${NODE}:${ROLE}"
      done
   done | parallel --halt 2 batfish_find_role_reachability_packet_constraints_helper {} $REACH_PATH $QUERY_BASE_PATH
   cd $OLD_PWD
   date | tr -d '\n'
   echo ": END: Find role-reachability packet constraints"
}
export -f batfish_find_multipath_inconsistent_packet_constraints

batfish_find_role_reachability_packet_constraints_helper() {
   batfish_expect_args 3 $# || return 1
   local NODE=$(echo "$1" | cut -d':' -f 1)
   local ROLE=$(echo "$1" | cut -d':' -f 2)
   local REACH_PATH=$2
   local QUERY_BASE_PATH=$3
   date | tr -d '\n'
   local QUERY_PATH=${QUERY_BASE_PATH}-${NODE}-${ROLE}.smt2
   local QUERY_OUTPUT_PATH=${QUERY_PATH}.out
   echo ": START: Find role-reachability packet constraints from node \"${NODE}\" to role \"${ROLE}\" (\"${QUERY_OUTPUT_PATH}\")"
   cat $REACH_PATH $QUERY_PATH | batfish_time $BATFISH_Z3_DATALOG -smt2 -in 3>&1 1> $QUERY_OUTPUT_PATH 2>&3
   if [ "${PIPESTATUS[0]}" -ne 0 -o "${PIPESTATUS[1]}" -ne 0 ]; then
      return 1
   fi
   date | tr -d '\n'
   echo ": END: Find role-reachability packet constraints from node \"${NODE}\" to role \"${ROLE}\" (\"${QUERY_OUTPUT_PATH}\")"
}
export -f batfish_find_role_reachability_packet_constraints_helper

batfish_format_flows() {
   batfish_expect_args 1 $# || return 1
   local DUMP_DIR=$1
   tail -n+2 $DUMP_DIR/SetFlowOriginate | while read line;
   do
      local NODE=$(echo $line | cut -d'|' -f 1 )
      local SRCIP=$(int_to_ip $(echo $line | cut -d'|' -f 2 ) )
      local DSTIP=$(int_to_ip $(echo $line | cut -d'|' -f 3 ) )
      local SRCPORT=$(echo $line | cut -d'|' -f 4)
      local DSTPORT=$(echo $line | cut -d'|' -f 5)
      local PROT=$(echo $line | cut -d'|' -f 6)
      echo "Node=$NODE, SrcIp=$SRCIP, DstIp=$DSTIP, SRCPORT=$SRCPORT, DSTPORT=$DSTPORT, PROT=$PROT"
   done > $DUMP_DIR/SetFlowOriginate.formatted
}
export -f batfish_format_flows

batfish_generate_concretizer_query_output() {
   batfish_expect_args 2 $# || return 1
   local INPUT_FILE=$1
   local NODE=$2
   local OUTPUT_FILE=${INPUT_FILE}.out
   date | tr -d '\n'
   echo ": START: Generate concretizer output for $NODE (\"$OUTPUT_FILE\")"
   local FIRST_LINE="$(head -n1 $INPUT_FILE | tr -d '\n')"
   if [ "$FIRST_LINE" = "unsat" ]; then
      echo unsat > $OUTPUT_FILE || return 1
   else
      { echo ";$NODE" ; $BATFISH_Z3 $INPUT_FILE ; } >& $OUTPUT_FILE
      local SECOND_OUTPUT_LINE="$(sed -n -e '2p' $OUTPUT_FILE | tr -d '\n')"
      if [ "$SECOND_OUTPUT_LINE" = "unsat" ]; then
         echo "unsat" > $OUTPUT_FILE
         else if [ ! "$SECOND_OUTPUT_LINE" = "sat" ]; then
            return 1
         fi
      fi
   fi
   date | tr -d '\n'
   echo ": END: Generate concretizer output for $NODE (\"$OUTPUT_FILE\")"
}
export -f batfish_generate_concretizer_query_output

batfish_generate_destination_consistency_concretizer_queries() {
   date | tr -d '\n'
   echo ": START: Generate destination-consistency concretizer queries"
   batfish_expect_args 3 $# || return 1
   local DI_QUERY_BASE_PATH=$1
   local NODE_SET_PATH=$2
   local BLACKLISTED_NODE=$3
   local QUERY_PATH="$(dirname $DI_QUERY_BASE_PATH)"
   local NODE_SET_TEXT_PATH=${NODE_SET_PATH}.txt
   local OLD_PWD=$PWD
   cd $QUERY_PATH
   cat $NODE_SET_TEXT_PATH | parallel --halt 2 batfish_generate_destination_consistency_concretizer_queries_helper {} $DI_QUERY_BASE_PATH $BLACKLISTED_NODE \;
   if [ "${PIPESTATUS[0]}" -ne 0 -o "${PIPESTATUS[1]}" -ne 0 ]; then
      return 1
   fi
   cd $OLD_PWD
   date | tr -d '\n'
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
      parallel --halt 2 -j1 batfish_generate_concretizer_query_output {} $NODE \;
   if [ "${PIPESTATUS[0]}" -ne 0 -o "${PIPESTATUS[1]}" -ne 0 ]; then
      return 1
   fi
}
export -f batfish_generate_destination_consistency_concretizer_queries_helper

batfish_generate_interface_failure_inconsistency_concretizer_queries() {
   date | tr -d '\n'
   echo ": START: Generate interface-failure-inconsistency concretizer queries"
   batfish_expect_args 5 $# || return 1
   local ORIG_FI_QUERY_BASE_PATH=$1
   local FI_QUERY_BASE_PATH=$2
   local NODE_SET_PATH=$3
   local BLACKLISTED_INTERFACE=$4
   local DST_IP_BLACKLIST_PATH=$5
   local BLACKLISTED_IP=$(cat $DST_IP_BLACKLIST_PATH | tr -d '\n') 
   local QUERY_PATH="$(dirname $FI_QUERY_BASE_PATH)"
   local NODE_SET_TEXT_PATH=${NODE_SET_PATH}.txt
   local OLD_PWD=$PWD
   cd $QUERY_PATH
   cat $NODE_SET_TEXT_PATH | parallel --halt 2 batfish_generate_interface_failure_inconsistency_concretizer_queries_helper {} $ORIG_FI_QUERY_BASE_PATH $FI_QUERY_BASE_PATH $BLACKLISTED_INTERFACE $BLACKLISTED_IP \;
   if [ "${PIPESTATUS[0]}" -ne 0 -o "${PIPESTATUS[1]}" -ne 0 ]; then
      return 1
   fi
   cd $OLD_PWD
   date | tr -d '\n'
   echo ": END: Generate interface-failure-inconsistency concretizer queries"
}
export -f batfish_generate_interface_failure_inconsistency_concretizer_queries

batfish_generate_interface_failure_inconsistency_concretizer_queries_helper() {
   batfish_expect_args 5 $# || return 1
   local NODE=$1
   local ORIG_FI_QUERY_BASE_PATH=$2
   local FI_QUERY_BASE_PATH=$3
   local BLACKLISTED_INTERFACE=$4
   local BLACKLISTED_IP=$5
   local BLACKLISTED_INTERFACE_SANITIZED=$(echo $BLACKLISTED_INTERFACE | tr '/' '_')
   local QUERY_BASE=${FI_QUERY_BASE_PATH}-${BLACKLISTED_INTERFACE_SANITIZED}-${NODE}
   local QUERY_OUT=${QUERY_BASE}.smt2.out
   local FI_CONCRETIZER_QUERY_BASE_PATH=${QUERY_BASE}-concrete
   local REACHABLE_QUERY_OUT=${ORIG_FI_QUERY_BASE_PATH}_reachable-${NODE}.smt2.out
   local BLACK_HOLE_QUERY_OUT=${ORIG_FI_QUERY_BASE_PATH}_black-hole-${NODE}.smt2.out
   local BLACK_HOLE_INTERFACE_QUERY_OUT=${FI_QUERY_BASE_PATH}_black-hole-${BLACKLISTED_INTERFACE_SANITIZED}-${NODE}.smt2.out
   batfish -conc -concin $REACHABLE_QUERY_OUT $BLACK_HOLE_INTERFACE_QUERY_OUT -concinneg $BLACK_HOLE_QUERY_OUT -concout $FI_CONCRETIZER_QUERY_BASE_PATH -blacklistdstip $BLACKLISTED_IP -concunique || return 1
   find $PWD -regextype posix-extended -regex "${FI_CONCRETIZER_QUERY_BASE_PATH}-[0-9]+.smt2" | \
      parallel --halt 2 -j1 batfish_generate_concretizer_query_output {} $NODE \;
   if [ "${PIPESTATUS[0]}" -ne 0 -o "${PIPESTATUS[1]}" -ne 0 ]; then
      return 1
   fi
}
export -f batfish_generate_interface_failure_inconsistency_concretizer_queries_helper

batfish_generate_multipath_inconsistency_concretizer_queries() {
   date | tr -d '\n'
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
   date | tr -d '\n'
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

batfish_generate_role_reachability_concretizer_queries() {
   date | tr -d '\n'
   echo ": START: Generate role-reachability concretizer queries"
   batfish_expect_args 2 $# || return 1
   local QUERY_BASE_PATH=$1
   local ROLE_NODES_PATH=$2
   local ITERATIONS_PATH=${ROLE_NODES_PATH}.iterations
   local QUERY_PATH="$(dirname $QUERY_BASE_PATH)"
   local OLD_PWD=$PWD
   cd $QUERY_PATH
   cat $ITERATIONS_PATH | parallel --halt 2 batfish_generate_role_reachability_concretizer_queries_helper {} $QUERY_BASE_PATH \;
   if [ "${PIPESTATUS[0]}" -ne 0 -o "${PIPESTATUS[1]}" -ne 0 ]; then
      return 1
   fi
   cd $OLD_PWD
   date | tr -d '\n'
   echo ": END: Generate role-reachability concretizer queries"
}
export -f batfish_generate_role_reachability_concretizer_queries

batfish_generate_role_reachability_concretizer_queries_helper() {
   batfish_expect_args 2 $# || return 1
   local ITERATION_LINE=$1
   local QUERY_BASE_PATH=$2
   local TRANSMITTING_ROLE=$(echo $ITERATION_LINE | cut -d':' -f 1)
   local MASTER_NODE=$(echo $ITERATION_LINE | cut -d':' -f 2)
   local SLAVE_NODE=$(echo $ITERATION_LINE | cut -d':' -f 3)
   local RECEIVING_ROLE=$(echo $ITERATION_LINE | cut -d':' -f 4)
   local MASTER_QUERY_OUT=${QUERY_BASE_PATH}-${MASTER_NODE}-${RECEIVING_ROLE}.smt2.out
   local SLAVE_QUERY_OUT=${QUERY_BASE_PATH}-${SLAVE_NODE}-${RECEIVING_ROLE}.smt2.out
   local MASTER_CONCRETIZER_QUERY_BASE_PATH=${QUERY_BASE_PATH}-${MASTER_NODE}-${SLAVE_NODE}-${RECEIVING_ROLE}-concrete
   local SLAVE_CONCRETIZER_QUERY_BASE_PATH=${QUERY_BASE_PATH}-${SLAVE_NODE}-${MASTER_NODE}-${RECEIVING_ROLE}-concrete
   date | tr -d '\n'
   echo ": START: Generate role-reachability concretizer queries for transmitting role \"${TRANSMITTING_ROLE}\", master node \"${MASTER_NODE}\", slave node \"${SLAVE_NODE}\", receiving role \"${RECEIVING_ROLE}\"" 
   batfish -conc -concin $MASTER_QUERY_OUT -concinneg $SLAVE_QUERY_OUT -concunique -concout $MASTER_CONCRETIZER_QUERY_BASE_PATH || return 1
   batfish -conc -concinneg $MASTER_QUERY_OUT -concin $SLAVE_QUERY_OUT -concunique -concout $SLAVE_CONCRETIZER_QUERY_BASE_PATH || return 1
   find $PWD -regextype posix-extended -regex "${MASTER_CONCRETIZER_QUERY_BASE_PATH}-[0-9]+.smt2" | \
      parallel --halt 2 -j1 batfish_generate_concretizer_query_output {} $MASTER_NODE \;
   if [ "${PIPESTATUS[0]}" -ne 0 -o "${PIPESTATUS[1]}" -ne 0 ]; then
      return 1
   fi
   find $PWD -regextype posix-extended -regex "${SLAVE_CONCRETIZER_QUERY_BASE_PATH}-[0-9]+.smt2" | \
      parallel --halt 2 -j1 batfish_generate_concretizer_query_output {} $SLAVE_NODE \;
   if [ "${PIPESTATUS[0]}" -ne 0 -o "${PIPESTATUS[1]}" -ne 0 ]; then
      return 1
   fi
   date | tr -d '\n'
   echo ": END: Generate role-reachability concretizer queries for transmitting role \"${TRANSMITTING_ROLE}\", master node \"${MASTER_NODE}\", slave node \"${SLAVE_NODE}\", receiving role \"${RECEIVING_ROLE}\"" 
}
export -f batfish_generate_role_reachability_concretizer_queries_helper

batfish_generate_z3_reachability() {
   date | tr -d '\n'
   echo ": START: Extract z3 reachability relations"
   batfish_expect_args 4 $# || return 1
   local DP_DIR=$1
   local INDEP_SERIAL_PATH=$2
   local REACH_PATH=$3
   local NODE_SET_PATH=$4
   batfish -sipath $INDEP_SERIAL_PATH -dpdir $DP_DIR -z3 -z3path $REACH_PATH -nodes $NODE_SET_PATH || return 1
   date | tr -d '\n'
   echo ": END: Extract z3 reachability relations"
}
export -f batfish_generate_z3_reachability

batfish_get_concrete_failure_packets() {
   date | tr -d '\n'
   echo ": START: Get concrete failure packets"
   batfish_expect_args 5 $# || return 1
   local QUERY_PATH=$1
   local FAILURE_QUERY_PATH=$2
   local FAILURE_REACH_QUERY_NAME=$3
   local LABEL=$4
   local FAILURE_LABEL=$5
   local OLD_PWD=$PWD
   local NODES=$QUERY_PATH/nodes-$LABEL                                                                                                    
   local FAILURE_NODES=$FAILURE_QUERY_PATH/nodes-$FAILURE_LABEL
   local COMBINED_NODES=$FAILURE_QUERY_PATH/nodes
   cat $NODES $FAILURE_NODES | sort -u > $COMBINED_NODES
   if [ "${PIPESTATUS[0]}" -ne 0 -o "${PIPESTATUS[1]}" -ne 0 ]; then
      return 1
   fi
   cd $FAILURE_QUERY_PATH
   cat $NODES | parallel --halt 2 batfish_get_concrete_failure_packets_decreased {} $FAILURE_REACH_QUERY_NAME \;
   if [ "${PIPESTATUS[0]}" -ne 0 -o "${PIPESTATUS[1]}" -ne 0 ]; then
      return 1
   fi
   cat $FAILURE_NODES | parallel --halt 2 batfish_get_concrete_failure_packets_increased {} $FAILURE_REACH_QUERY_NAME \;
   if [ "${PIPESTATUS[0]}" -ne 0 -o "${PIPESTATUS[1]}" -ne 0 ]; then
      return 1
   fi
   cd $OLD_PWD
   date | tr -d '\n'
   echo ": END: Get concrete inconsistent packets"
}
export -f batfish_get_concrete_failure_packets

batfish_nuke_reset_logicblox() {
   killall -9 lb-server
   killall -9 lb-pager
   lb services stop || return 1
   rm -rf ~/lb_deployment/*
   LB_CONNECTBLOX_ENABLE_ADMIN=1 lb services start || return 1
}
export -f batfish_nuke_reset_logicblox

batfish_inject_packets() {
   date | tr -d '\n'
   echo ": START: Inject concrete packets into network model"
   batfish_expect_args 3 $# || return 1
   local WORKSPACE=$1
   local QUERY_PATH=$2
   local DUMP_DIR=$3
   local OLD_PWD=$PWD
   cd $QUERY_PATH
   batfish -workspace $WORKSPACE -flow -flowpath $QUERY_PATH -dumptraffic -dumpdir $DUMP_DIR || return 1
   batfish_format_flows $DUMP_DIR || return 1
   cd $OLD_PWD
   date | tr -d '\n'
   echo ": END: Inject concrete packets into network model"
}
export -f batfish_inject_packets

batfish_inject_packets_with_role_flow_duplication() {
   date | tr -d '\n'
   echo ": START: Inject concrete packets into network model"
   batfish_expect_args 3 $# || return 1
   local WORKSPACE=$1
   local QUERY_PATH=$2
   local DUMP_DIR=$3
   local OLD_PWD=$PWD
   cd $QUERY_PATH
   batfish -workspace $WORKSPACE -flow -flowpath $QUERY_PATH -drf -dumptraffic -dumpdir $DUMP_DIR || return 1
   batfish_format_flows $DUMP_DIR || return 1
   cd $OLD_PWD
   date | tr -d '\n'
   echo ": END: Inject concrete packets into network model"
}
export -f batfish_inject_packets_with_role_flow_duplication

batfish_query_data_plane() {
   date | tr -d '\n'
   echo ": START: Query data plane predicates"
   batfish_expect_args 2 $# || return 1
   local WORKSPACE=$1
   local DP_DIR=$2
   mkdir -p $DP_DIR
   batfish -workspace $WORKSPACE -dp -dpdir $DP_DIR || return 1
   date | tr -d '\n'
   echo ": END: Query data plane predicates"
}
export -f batfish_query_data_plane

batfish_query_flows() {
   date | tr -d '\n'
   echo ": START: Query flow results from LogicBlox"
   batfish_expect_args 2 $# || return 1
   local FLOW_RESULTS=$1
   local WORKSPACE=$2
   batfish -log 0 -workspace $WORKSPACE -query -predicates Flow FlowUnknown FlowInconsistent FlowAccepted FlowAllowedIn FlowAllowedOut FlowDropped FlowDeniedIn FlowDeniedOut FlowNoRoute FlowNullRouted FlowPolicyDenied FlowReachPolicyRoute FlowReachPostIn FlowReachPreOut FlowReachPreOutInterface FlowReachPostOutInterface FlowReachPreOutEdgeOrigin FlowReachPreOutEdgePolicyRoute FlowReachPreOutEdgeStandard FlowReachPreOutEdge FlowReachPreInInterface FlowReachPostInInterface FlowReach FlowReachStep FlowLost FlowLoop FlowPathHistory FlowPathAcceptedEdge FlowPathDeniedOutEdge FlowPathDeniedInEdge FlowPathNoRouteEdge FlowPathNullRoutedEdge FlowPathIntermediateEdge LanAdjacent &> $FLOW_RESULTS
   date | tr -d '\n'
   echo ": END: Query flow results from LogicBlox"
}
export -f batfish_query_flows

batfish_query_predicate() {
   date | tr -d '\n'
   echo ": START: Query predicate (informational only)"
   batfish_expect_args 2 $# || return 1
   local PREFIX=$1
   local PREDICATE=$2
   local WORKSPACE=batfish-$USER-$PREFIX
   batfish -workspace $WORKSPACE -query -predicates $PREDICATE
   date | tr -d '\n'
   echo ": END: Query predicate (informational only)"
}
export -f batfish_query_predicate

batfish_query_routes() {
   date | tr -d '\n'
   echo ": START: Query routes (informational only)"
   batfish_expect_args 2 $# || return 1
   local ROUTES=$1
   local WORKSPACE=$2
   batfish -log 0 -workspace $WORKSPACE -query -predicates InstalledRoute &> $ROUTES
   date | tr -d '\n'
   echo ": END: Query routes (informational only)"
}
export -f batfish_query_routes

batfish_reload() {
   . $BATFISH_SOURCED_SCRIPT
}
export -f batfish_reload

batfish_replace_symlinks() {
   OLDPWD=$PWD
   cd $BATFISH_PATH
   ./cygwin-replace-symlinks
   cd $OLDPWD
}
export batfish_replace_symlinks

batfish_serialize_independent() {
   date | tr -d '\n'
   echo ": START: Parse vendor structures and serialize vendor-independent structures"
   batfish_expect_args 2 $# || return 1
   local VENDOR_SERIAL_DIR=$1
   local INDEP_SERIAL_DIR=$2
   mkdir -p $INDEP_SERIAL_DIR
   batfish -svpath $VENDOR_SERIAL_DIR -si -sipath $INDEP_SERIAL_DIR || return 1
   date | tr -d '\n'
   echo ": END: Parse vendor structures and serialize vendor-independent structures"
}
export -f batfish_serialize_independent

batfish_serialize_vendor() {
   date | tr -d '\n'
   echo ": START: Parse vendor configuration files and serialize vendor structures"
   batfish_expect_args 2 $# || return 1
   local TEST_RIG=$1
   local VENDOR_SERIAL_DIR=$2
   mkdir -p $VENDOR_SERIAL_DIR
   batfish -testrig $TEST_RIG -sv -svpath $VENDOR_SERIAL_DIR -ee || return 1
   date | tr -d '\n'
   echo ": END: Parse vendor configuration files and serialize vendor structures"
}
export -f batfish_serialize_vendor

batfish_serialize_vendor_with_roles() {
   date | tr -d '\n'
   echo ": START: Parse vendor configuration files and serialize vendor structures"
   batfish_expect_args 3 $# || return 1
   local TEST_RIG=$1
   local VENDOR_SERIAL_DIR=$2
   local NODE_ROLES_PATH=$3
   mkdir -p $VENDOR_SERIAL_DIR
   batfish -testrig $TEST_RIG -sv -svpath $VENDOR_SERIAL_DIR -ee -nrpath $NODE_ROLES_PATH || return 1
   date | tr -d '\n'
   echo ": END: Parse vendor configuration files and serialize vendor structures"
}
export -f batfish_serialize_vendor_with_roles

batfish_restore_symlinks() {
   OLDPWD=$PWD
   cd $BATFISH_PATH
   ./cygwin-restore-symlinks
   cd $OLDPWD
}
export batfish_restore_symlinks

batfish_unit_tests_parser() {
   batfish_expect_args 1 $# || return 1
   local OUTPUT_DIR=$1
   local UNIT_TEST_DIR=$BATFISH_TEST_RIG_PATH/unit-tests
   date | tr -d '\n'
   echo ": START UNIT TEST: Vendor configuration parser"
   mkdir -p $OUTPUT_DIR
   batfish -testrig $UNIT_TEST_DIR -sv -svpath $OUTPUT_DIR -ppt
   date | tr -d '\n'
   echo ": END UNIT TEST: Vendor configuration parser"
}
export -f batfish_unit_tests_parser

int_to_ip() {
   batfish_expect_args 1 $# || return 1
   local INPUT=$1
   local OCTET_0=$(( INPUT % 256 ))
   local OCTET_1=$(( (INPUT / 256) % 256 ))
   local OCTET_2=$(( (INPUT / 65536) % 256 ))
   local OCTET_3=$(( INPUT / 16777216 ))
   echo "${OCTET_3}.${OCTET_2}.${OCTET_1}.${OCTET_0}"
}
export -f int_to_ip

ip_to_int() {
   batfish_expect_args 1 $# || return 1
   local INPUT=$1
   local OCTET_0=$(echo "$INPUT" | cut -d'.' -f 4)
   local OCTET_1=$(echo "$INPUT" | cut -d'.' -f 3)
   local OCTET_2=$(echo "$INPUT" | cut -d'.' -f 2)
   local OCTET_3=$(echo "$INPUT" | cut -d'.' -f 1)
   echo $((${OCTET_3} * 16777216 + ${OCTET_2} * 65536 + ${OCTET_1} * 256 + ${OCTET_0}))
}
export -f ip_to_int

# if the 'time' binary is not available (e.g. on cygwin it is a bash builtin), define it
if [ -z "$(which time 2>&1)" ]; then
   batfish_time() {
      bash -c "time $@"
   }
else
   batfish_time() {
      time $@
   }
fi
export -f batfish_time

