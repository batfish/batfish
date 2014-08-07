#!/usr/bin/env bash

export BATFISH_SOURCED_SCRIPT=$BASH_SOURCE
export BATFISH_ROOT=$(readlink -f $(dirname $BATFISH_SOURCED_SCRIPT)/..)
export BATFISH_PATH=$BATFISH_ROOT/projects/batfish
export BATFISH_TEST_RIG_PATH=$BATFISH_ROOT/test_rigs
export BATFISH=$BATFISH_PATH/batfish
export BATFISH_Z3=$(which z3)
export BATFISH_Z3_DATALOG="$BATFISH_Z3 fixedpoint.engine=datalog fixedpoint.default_relation=hassel_diff fixedpoint.unbound_compressor=false fixedpoint.print_answer=true"

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
   $BATFISH $BATFISH_COMMON_ARGS $@
}
export -f batfish

batfish_confirm_analyze() {
   BATFISH_CONFIRM=batfish_confirm batfish_analyze $@
}
export -f batfish_confirm_analyze
   
batfish_analyze() {
   batfish_expect_args 2 $# || return 1
   if [ -z "$BATFISH_CONFIRM" ]; then
      local BATFISH_CONFIRM=true
   fi
   local TEST_RIG=$1
   local PREFIX=$2
   local WORKSPACE=batfish-$USER-$2
   local OLD_PWD=$PWD
   local REACH_PATH=$OLD_PWD/$PREFIX-reach.smt2
   local QUERY_PATH=$OLD_PWD/$PREFIX-query
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

   echo "Query data plane predicates"
   $BATFISH_CONFIRM && { batfish_query_data_plane $WORKSPACE $DP_DIR || return 1 ; }

   echo "Extract z3 reachability relations"
   $BATFISH_CONFIRM && { batfish_generate_z3_reachability $DP_DIR $INDEP_SERIAL_DIR $REACH_PATH  || return 1 ; }

   echo "Find inconsistent packet constraints"
   $BATFISH_CONFIRM && { batfish_find_inconsistent_packet_constraints $REACH_PATH $QUERY_PATH || return 1 ; }

   echo "Generate constraints z3 queries for concretizer"
   $BATFISH_CONFIRM && { batfish_generate_constraints_queries $QUERY_PATH || return 1 ; }

   echo "Get concrete inconsistent packets"
   $BATFISH_CONFIRM && { batfish_get_concrete_inconsistent_packets $QUERY_PATH || return 1 ; }

   echo "Inject concrete packets into network model"
   $BATFISH_CONFIRM && { batfish_inject_packets $WORKSPACE $QUERY_PATH $DUMP_DIR || return 1 ; }

   echo "Query flow results from LogicBlox"
   $BATFISH_CONFIRM && { batfish_query_flows $FLOWS $WORKSPACE || return 1 ; }
}
export -f batfish_analyze

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
      echo "${FUNCNAME[1]}: Expected $EXPECTED_NUMARGS arguments" >&2
      return 1
   fi   
}
export -f batfish_expect_args

batfish_find_failure_packet_constraints() {
   date | tr -d '\n'
   echo ": START: Find differential reachability packet constraints"
   batfish_expect_args 5 $# || return 1
   local REACH_PATH=$1
   local FAILURE_REACH_PATH=$2
   local FAILURE_PATH=$3
   local NUM_NETWORK_BITS=$4
   local NETWORK_BITS=$5
   local OLD_PWD=$PWD
   local FIRST_BIT=$((32 - $NUM_NETWORK_BITS))
#   local ORIG_NODES=nodes-$
   if [ ! -e "$REACH_PATH" ] ; then
      echo "Missing base reachability logic: $REACH_PATH"
      return 1
   fi
   if [ ! -e "$FAILURE_REACH_PATH" ] ; then
      echo "Missing failure reachability logic: $FAILURE_RREACH_PATH"
      return 1
   fi
   mkdir -p $FAILURE_PATH
   cd $FAILURE_PATH
   grep 'declare-rel' $REACH_PATH | tr ' ' '\n' | tr -d '()' | grep 'R_postin_' | sed -e 's/.*R_postin_//g' | sort -u > $ORIG_NODES
   grep 'declare-rel' $FAILURE_REACH_PATH | tr ' ' '\n' | tr -d '()' | grep 'R_postin_' | sed -e 's/.*R_postin_//g' | sort -u > $FAILURE_NODES
   cat $NODES | while read node
   do
      local ORIG_QUERY=failure-query-orig-${node}.smt2
      {
         echo "(rule (R_postin_$node src_ip dst_ip src_port dst_port ip_prot) )" ;
         echo "(query" ;
         echo "   (and" ;
         echo "      (not (= ((_ extract 31 $FIRST_BIT) dst_ip) ${NETWORK_BITS}))" ;
         echo "      (R_accept src_ip dst_ip src_port dst_port ip_prot) ) )" ;
         echo "(query" ;
         echo "   (and" ;
         echo "      (not (= ((_ extract 31 $FIRST_BIT) dst_ip) ${NETWORK_BITS}))" ;
         echo "      (R_drop src_ip dst_ip src_port dst_port ip_prot) ) )" ;
      } > $ORIG_QUERY
      local QUERY=failure-query-${node}.smt2
      {
         echo "(rule (R_postin_$node src_ip dst_ip src_port dst_port ip_prot) )" ;
         echo "(query" ;
         echo "   (and" ;
         echo "      (not (= ((_ extract 31 $FIRST_BIT) dst_ip) ${NETWORK_BITS}))" ;
         echo "      (R_drop src_ip dst_ip src_port dst_port ip_prot) ) )" ;
         echo "(query" ;
         echo "   (and" ;
         echo "      (not (= ((_ extract 31 $FIRST_BIT) dst_ip) ${NETWORK_BITS}))" ;
         echo "      (R_accept src_ip dst_ip src_port dst_port ip_prot) ) )" ;
      } > $QUERY
   done
   cat $NODES | parallel --halt 2 batfish_find_failure_packet_constraints_helper {} $REACH_PATH $FAILURE_REACH_PATH \;
   if [ "${PIPESTATUS[0]}" -ne 0 -o "${PIPESTATUS[1]}" -ne 0 ]; then
      return 1
   fi
   cd $OLD_PWD
   date | tr -d '\n'
   echo ": END: Find differential reachability packet constraints"
}
export -f batfish_find_failure_packet_constraints

batfish_find_failure_packet_constraints_helper() {
   batfish_expect_args 3 $# || return 1
   local NODE=$1
   local REACH_PATH=$2
   local RREACH_PATH=$3
   local QUERY=$PWD/failure-query-${NODE}.smt2
   local QUERY_OUT=$PWD/failure-query-${NODE}.smt2.out
   echo -n "   "
   date | tr -d '\n'
   echo ": START: Generate differential reachability constraints for $NODE (\"$QUERY_OUT\")"
   cat $REACH_PATH $RREACH_PATH $QUERY | $BATFISH_Z3_DATALOG -smt2 -in > $QUERY_OUT
   if [ "${PIPESTATUS[0]}" -ne 0 -o "${PIPESTATUS[1]}" -ne 0 ]; then
      return 1
   fi
   echo -n "   "
   date | tr -d '\n'
   echo ": END: Generate differential reachability constraints for $NODE (\"$QUERY_OUT\")"
}
export -f batfish_find_failure_packet_constraints_helper

batfish_find_inconsistent_packet_constraints() {
   date | tr -d '\n'
   echo ": START: Find inconsistent packet constraints"
   batfish_expect_args 2 $# || return 1
   local REACH_PATH=$1
   local QUERY_PATH=$2
   local OLD_PWD=$PWD
   mkdir -p $QUERY_PATH
   cd $QUERY_PATH
   grep 'declare-rel' $REACH_PATH | tr ' ' '\n' | tr -d '()' | grep 'R_postin_' | sed -e 's/.*R_postin_//g' | sort -u > nodes
   cat nodes | while read node
   do
      local QUERY=incons-query-${node}.smt2
      {
         echo "(rule (R_postin_$node src_ip dst_ip src_port dst_port ip_prot) )" ;
         echo "(query" ;
         echo "   (and" ;
         echo "      (R_accept src_ip dst_ip src_port dst_port ip_prot)" ;
         echo "      (R_drop src_ip dst_ip src_port dst_port ip_prot) ) )" ;
      } > $QUERY
   done
   cat nodes | parallel --halt 2 batfish_find_inconsistent_packet_constraints_helper {} $REACH_PATH \;
   if [ "${PIPESTATUS[0]}" -ne 0 -o "${PIPESTATUS[1]}" -ne 0 ]; then
      return 1
   fi
   cd $OLD_PWD
   date | tr -d '\n'
   echo ": END: Find inconsistent packet constraints"
}
export -f batfish_find_inconsistent_packet_constraints

batfish_find_inconsistent_packet_constraints_helper() {
   batfish_expect_args 2 $# || return 1
   local NODE=$1
   local REACH_PATH=$2
   local QUERY=$PWD/incons-query-${NODE}.smt2
   local QUERY_OUT=$PWD/incons-query-${NODE}.smt2.out
   echo -n "   "
   date | tr -d '\n'
   echo ": START: Generate constraints for $NODE (\"$QUERY_OUT\")"
   cat $REACH_PATH $QUERY | $BATFISH_Z3_DATALOG -smt2 -in > $QUERY_OUT
   if [ "${PIPESTATUS[0]}" -ne 0 -o "${PIPESTATUS[1]}" -ne 0 ]; then
      return 1
   fi
   echo -n "   "
   date | tr -d '\n'
   echo ": END: Generate constraints for $NODE (\"$QUERY_OUT\")"
}
export -f batfish_find_inconsistent_packet_constraints_helper

batfish_find_lost_packet_constraints() {
   date | tr -d '\n'
   echo ": START: Find lost packet constraints"
   batfish_expect_args 4 $# || return 1
   local REACH_PATH=$1
   local QUERY_DIR=$2
   local QUERY_NAME=$3
   local LABEL=$4
   local OLD_PWD=$PWD
   local NODES=nodes-$LABEL
   mkdir -p $QUERY_DIR
   cd $QUERY_DIR
   grep 'declare-rel' $REACH_PATH | tr ' ' '\n' | tr -d '()' | grep 'R_postin_' | sed -e 's/.*R_postin_//g' | sort -u > $NODES
   cat $NODES | while read node
   do
      local QUERY=query-${QUERY_NAME}-${node}.smt2
      {
         echo "(rule (R_postin_$node src_ip dst_ip src_port dst_port ip_prot) )" ;
         echo "(query" ;
         echo "   (R_drop src_ip dst_ip src_port dst_port ip_prot) )" ;
      } > $QUERY
   done
   cat $NODES | parallel --halt 2 batfish_find_lost_packet_constraints_helper {} $REACH_PATH $QUERY_NAME \;
   if [ "${PIPESTATUS[0]}" -ne 0 -o "${PIPESTATUS[1]}" -ne 0 ]; then
      return 1
   fi
   cd $OLD_PWD
   date | tr -d '\n'
   echo ": END: Find lost packet constraints"
}
export -f batfish_find_lost_packet_constraints

batfish_find_lost_packet_constraints_helper() {
   local NODE=$1
   local REACH_PATH=$2
   local QUERY_NAME=$3
   local QUERY=$PWD/query-${QUERY_NAME}-${NODE}.smt2
   local QUERY_OUT=${QUERY}.out
   echo -n "   "
   date | tr -d '\n'
   echo ": START: Generate lost packet constraints for $NODE (\"$QUERY_OUT\")"
   cat $REACH_PATH $QUERY | $BATFISH_Z3_DATALOG -smt2 -in > $QUERY_OUT
   if [ "${PIPESTATUS[0]}" -ne 0 -o "${PIPESTATUS[1]}" -ne 0 ]; then
      return 1
   fi
   echo -n "   "
   date | tr -d '\n'
   echo ": END: Generate lost packet constraints for $NODE (\"$QUERY_OUT\")"
}
export -f batfish_find_lost_packet_constraints_helper
 
batfish_find_reachability_packet_constraints() {
   date | tr -d '\n'
   echo ": START: Find reachability packet constraints"
   echo "$@"
   batfish_expect_args 4 $# || return 1
   local REACH_PATH=$1
   local QUERY_DIR=$2
   local QUERY_NAME=$3
   local LABEL=$4
   local OLD_PWD=$PWD
   local NODES=nodes-$LABEL
   mkdir -p $QUERY_DIR
   cd $QUERY_DIR
   grep 'declare-rel' $REACH_PATH | tr ' ' '\n' | tr -d '()' | grep 'R_postin_' | sed -e 's/.*R_postin_//g' | sort -u > $NODES
   cat $NODES | while read node
   do
      local QUERY=query-${QUERY_NAME}-${node}.smt2
      {
         echo "(rule (R_postin_$node src_ip dst_ip src_port dst_port ip_prot) )" ;
         echo "(query" ;
         echo "   (R_accept src_ip dst_ip src_port dst_port ip_prot) )" ;
      } > $QUERY
   done
   cat $NODES | parallel --halt 2 batfish_find_reachability_packet_constraints_helper {} $REACH_PATH $QUERY_NAME \;
   if [ "${PIPESTATUS[0]}" -ne 0 -o "${PIPESTATUS[1]}" -ne 0 ]; then
      return 1
   fi
   cd $OLD_PWD
   date | tr -d '\n'
   echo ": END: Find reachability packet constraints"
}
export -f batfish_find_reachability_packet_constraints

batfish_find_reachability_packet_constraints_helper() {
   local NODE=$1
   local REACH_PATH=$2
   local QUERY_NAME=$3
   local QUERY=$PWD/query-${QUERY_NAME}-${NODE}.smt2
   local QUERY_OUT=${QUERY}.out
   echo -n "   "
   date | tr -d '\n'
   echo ": START: Find reachability packet constraints for $NODE (\"$QUERY_OUT\")"
   cat $REACH_PATH $QUERY | $BATFISH_Z3_DATALOG -smt2 -in > $QUERY_OUT
   if [ "${PIPESTATUS[0]}" -ne 0 -o "${PIPESTATUS[1]}" -ne 0 ]; then
      return 1
   fi
   echo -n "   "
   date | tr -d '\n'
   echo ": END: Find reachability packet constraints for $NODE (\"$QUERY_OUT\")"
}
export -f batfish_find_reachability_packet_constraints_helper
 
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

batfish_generate_constraints_queries() {
   date | tr -d '\n'
   echo ": START: Generate constraints z3 queries for concretizer"
   batfish_expect_args 1 $# || return 1
   local QUERY_PATH=$1
   local OLD_PWD=$PWD
   cd $QUERY_PATH
   cat nodes | parallel --halt 2 batfish_generate_constraints_queries_helper {} \;
   if [ "${PIPESTATUS[0]}" -ne 0 -o "${PIPESTATUS[1]}" -ne 0 ]; then
      return 1
   fi
   cd $OLD_PWD
   date | tr -d '\n'
   echo ": END: Generate constraints z3 queries for concretizer"
}
export -f batfish_generate_constraints_queries

batfish_generate_constraints_queries_helper() {
   batfish_expect_args 1 $# || return 1
   local NODE=$1
   local QUERY_OUT=$PWD/incons-query-${NODE}.smt2.out
   local CONC_QUERY=$PWD/incons-constraints-${NODE}.smt2
   batfish -conc -concin $QUERY_OUT -concout $CONC_QUERY || return 1
}
export -f batfish_generate_constraints_queries_helper

batfish_generate_z3_reachability() {
   date | tr -d '\n'
   echo ": START: Extract z3 reachability relations"
   batfish_expect_args 3 $# || return 1
   local DP_DIR=$1
   local INDEP_SERIAL_PATH=$2
   local REACH_PATH=$3
   batfish -sipath $INDEP_SERIAL_PATH -dpdir $DP_DIR -z3 -z3out $REACH_PATH || return 1
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

batfish_get_concrete_failure_packets_decreased() {
   batfish_expect_args 2 $# || return 1
   local NODE=$1
   local FAILURE_REACH_QUERY_NAME=$2
   local DECREASED_QUERY_NAME=decreased-$FAILURE_REACH_QUERY_NAME
   local Z3_IN=$PWD/constraints-${DECREASED_QUERY_NAME}-${NODE}.smt2
   local Z3_OUT=${Z3_IN}.out
   date | tr -d '\n'
   echo ": START: Get concrete decreased reachability packet ( $Z3_IN => $Z3_OUT"
   $BATFISH_Z3 $Z3_IN > $Z3_OUT
   HEADER=$(head -c5 $Z3_OUT)
   if [ "$HEADER" = "unsat" ]; then
      echo unsat > $Z3_OUT
   fi
   date | tr -d '\n'
   echo ": END: Get concrete decreased reachability packet ( $Z3_IN => $Z3_OUT"
}
export -f batfish_get_concrete_failure_packets_decreased

batfish_get_concrete_failure_packets_increased() {
   batfish_expect_args 2 $# || return 1
   local NODE=$1
   local FAILURE_REACH_QUERY_NAME=$2
   local INCREASED_QUERY_NAME=increased-$FAILURE_REACH_QUERY_NAME
   local Z3_IN=$PWD/constraints-${INCREASED_QUERY_NAME}-${NODE}.smt2
   local Z3_OUT=${Z3_IN}.out
   date | tr -d '\n'
   echo ": START: Get concrete increased reachability packet ( $Z3_IN => $Z3_OUT"
   $BATFISH_Z3 $Z3_IN > $Z3_OUT
   HEADER=$(head -c5 $Z3_OUT)
   if [ "$HEADER" = "unsat" ]; then
      echo unsat > $Z3_OUT
   fi
   date | tr -d '\n'
   echo ": END: Get concrete increased reachability packet ( $Z3_IN => $Z3_OUT"
}
export -f batfish_get_concrete_failure_packets_increased

batfish_get_concrete_inconsistent_packets() {
   date | tr -d '\n'
   echo ": START: Get concrete inconsistent packets"
   batfish_expect_args 1 $# || return 1
   local QUERY_PATH=$1
   local OLD_PWD=$PWD
   cd $QUERY_PATH
   cat nodes | parallel --halt 2 batfish_get_concrete_inconsistent_packets_helper {}  \;  || return 1
   cd $OLD_PWD
   date | tr -d '\n'
   echo ": END: Get concrete inconsistent packets"
}
export -f batfish_get_concrete_inconsistent_packets

batfish_get_concrete_inconsistent_packets_helper() {
   batfish_expect_args 1 $# || return 1
   local NODE=$1
   local CONC_QUERY=$PWD/incons-constraints-${NODE}.smt2
   local CONC_QUERY_OUT=$PWD/incons-constraints-${NODE}.smt2.out
   echo -n "   "
   date | tr -d '\n'
   echo ": START: Get concrete inconsistent packets for $NODE (\"$CONC_QUERY_OUT\")"
   HEADER=$(head -c5 $CONC_QUERY)
   if [ "$HEADER" = "unsat" ]; then
      echo unsat > $CONC_QUERY_OUT
   else
      $BATFISH_Z3 $CONC_QUERY > $CONC_QUERY_OUT || return 1
   fi
   echo -n "   "
   date | tr -d '\n'
   echo ": END: Get concrete inconsistent packets for $NODE (\"$CONC_QUERY_OUT\")"
}
export -f batfish_get_concrete_inconsistent_packets_helper

batfish_inject_packets() {
   date | tr -d '\n'
   echo ": START: Inject concrete packets into network model"
   batfish_expect_args 3 $# || return 1
   local WORKSPACE=$1
   local QUERY_PATH=$2
   local DUMP_DIR=$3
   local OLD_PWD=$PWD
   #local FLOW_SINK_PATH=$TEST_RIG/flow_sinks
   cd $QUERY_PATH
   #batfish -testrig $TEST_RIG -flow -flowpath $QUERY_PATH -flowsink $FLOW_SINK_PATH -dumptraffic -dumpdir $DUMP_DIR
   batfish -workspace $WORKSPACE -flow -flowpath $QUERY_PATH -dumptraffic -dumpdir $DUMP_DIR || return 1
   batfish_format_flows $DUMP_DIR || return 1
   cd $OLD_PWD
   date | tr -d '\n'
   echo ": END: Inject concrete packets into network model"
}
export -f batfish_inject_packets

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
   batfish -log 0 -workspace $WORKSPACE -query -predicates Flow FlowUnknown FlowInconsistent FlowAccepted FlowAllowedIn FlowAllowedOut FlowDropped FlowDeniedIn FlowDeniedOut FlowNoRoute FlowReachPostIn FlowReachPreOut FlowReachPreOutInterface FlowReachPostOutInterface FlowReachPreInInterface FlowReachPostInInterface FlowReach FlowReachStep FlowLost FlowLoop LanAdjacent &> $FLOW_RESULTS
   date | tr -d '\n'
   echo ": END: Query flow results from LogicBlox"
}
export -f batfish_query_flows

batfish_query_routes() {
   date | tr -d '\n'
   echo ": START: Query routes (informational only)"
   batfish_expect_args 2 $# || return 1
   local ROUTES=$1
   local TEST_RIG=$2
   batfish -log 0 -testrig $TEST_RIG -query -predicates InstalledRoute &> $ROUTES
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
   batfish -testrig $UNIT_TEST_DIR -sv -svpath $OUTPUT_DIR
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

