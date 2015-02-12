#!/usr/bin/env bash

which z3 > /dev/null || return
which parallel > /dev/null || return

export BATFISH_ROOT="$BATFISH_TOOLS_PATH/.."
export BATFISH_PATH="$BATFISH_ROOT/projects/batfish"
export BATFISH_TEST_RIG_PATH="$BATFISH_ROOT/test_rigs"
export BATFISH="$BATFISH_PATH/batfish"
export BATFISH_Z3=z3
export BATFISH_Z3_DATALOG="$BATFISH_Z3 fixedpoint.engine=datalog fixedpoint.datalog.default_relation=doc fixedpoint.print.answer=true"
export BATFISH_PARALLEL='parallel --tag -v --eta --halt 2'
export BATFISH_NESTED_PARALLEL='parallel --tag -v --halt 2 -j1'

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
   batfish_date
   echo ": START: Compute the fixed point of the control plane"
   batfish_expect_args 4 $# || return 1
   local WORKSPACE=$1
   local TEST_RIG=$2
   local DUMP_DIR=$3
   local INDEP_SERIAL_DIR=$4
   if [ -n "${BATFISH_REMOTE_LOGIC_DIR}" ]; then
      local LOGIC_DIR_ARG="-logicdir ${BATFISH_REMOTE_LOGIC_DIR}"
   fi
   batfish -workspace $LOGIC_DIR_ARG $WORKSPACE -testrig $TEST_RIG -sipath $INDEP_SERIAL_DIR -compile -facts -dumpcp -dumpdir $DUMP_DIR || return 1
   batfish_date
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

batfish_date() {
   { hostname; echo -n ': '; date ; } | tr -d '\n'
}
export -f batfish_date

batfish_expect_args() {
   local EXPECTED_NUMARGS=$1
   local ACTUAL_NUMARGS=$2
   if [ "$EXPECTED_NUMARGS" -ne "$ACTUAL_NUMARGS" ]; then
      echo "${FUNCNAME[1]}: Expected $EXPECTED_NUMARGS arguments, but got $ACTUAL_NUMARGS" >&2
      return 1
   fi   
}
export -f batfish_expect_args

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

batfish_inject_packets() {
   batfish_date
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
   batfish_date
   echo ": END: Inject concrete packets into network model"
}
export -f batfish_inject_packets

batfish_generate_concretizer_query_output() {
   batfish_expect_args 2 $# || return 1
   local INPUT_FILE=$1
   local NODE=$2
   local OUTPUT_FILE=${INPUT_FILE}.out
   batfish_date
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
            echo FAILED: z3 output follows
            tail -n+2 $OUTPUT_FILE
            return 1
         fi
      fi
   fi
   batfish_date
   echo ": END: Generate concretizer output for $NODE (\"$OUTPUT_FILE\")"
}
export -f batfish_generate_concretizer_query_output

batfish_generate_z3_reachability() {
   batfish_date
   echo ": START: Extract z3 reachability relations"
   batfish_expect_args 4 $# || return 1
   local DP_DIR=$1
   local INDEP_SERIAL_PATH=$2
   local REACH_PATH=$3
   local NODE_SET_PATH=$4
   batfish -sipath $INDEP_SERIAL_PATH -dpdir $DP_DIR -z3 -z3path $REACH_PATH -nodes $NODE_SET_PATH || return 1
   batfish_date
   echo ": END: Extract z3 reachability relations"
}
export -f batfish_generate_z3_reachability

batfish_get_concrete_failure_packets() {
   batfish_date
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
   cat $NODES | $BATFISH_PARALLEL batfish_get_concrete_failure_packets_decreased {} $FAILURE_REACH_QUERY_NAME \;
   if [ "${PIPESTATUS[0]}" -ne 0 -o "${PIPESTATUS[1]}" -ne 0 ]; then
      return 1
   fi
   cat $FAILURE_NODES | $BATFISH_PARALLEL batfish_get_concrete_failure_packets_increased {} $FAILURE_REACH_QUERY_NAME \;
   if [ "${PIPESTATUS[0]}" -ne 0 -o "${PIPESTATUS[1]}" -ne 0 ]; then
      return 1
   fi
   cd $OLD_PWD
   batfish_date
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

batfish_query_bgp() {
   batfish_date
   echo ": START: Query bgp (informational only)"
   batfish_expect_args 2 $# || return 1
   local BGP=$1
   local WORKSPACE=$2
   batfish -log output -workspace $WORKSPACE -query -predicates \
      AdvertisementPath \
      AdvertisementPathSize \
      BgpAdvertisement \
      BgpGeneratedRoute \
      BgpNeighborGeneratedRoute \
      BgpNeighbors \
      IbgpNeighbors \
      InstalledBgpAdvertisementRoute \
      OriginalBgpAdvertisementRoute \
      &> $BGP
   batfish_date
   echo ": END: Query bgp (informational only)"
}
export -f batfish_query_bgp

batfish_query_data_plane() {
   batfish_date
   echo ": START: Query data plane predicates"
   batfish_expect_args 2 $# || return 1
   local WORKSPACE=$1
   local DP_DIR=$2
   mkdir -p $DP_DIR
   batfish -workspace $WORKSPACE -dp -dpdir $DP_DIR || return 1
   batfish_date
   echo ": END: Query data plane predicates"
}
export -f batfish_query_data_plane

batfish_query_flows() {
   batfish_date
   echo ": START: Query flow results from LogicBlox"
   batfish_expect_args 2 $# || return 1
   local FLOW_RESULTS=$1
   local WORKSPACE=$2
   batfish -log output -workspace $WORKSPACE -query -predicates \
      Flow \
      FlowAccepted \
      FlowAllowedIn \
      FlowAllowedOut \
      FlowDeniedIn \
      FlowDeniedOut \
      FlowDropped \
      FlowLoop \
      FlowLost \
      FlowMatchRoute \
      FlowMultipathInconsistent \
      FlowNeighborUnreachable \
      FlowNoRoute \
      FlowNullRouted \
      FlowPathAcceptedEdge \
      FlowPathDeniedInEdge \
      FlowPathDeniedOutEdge \
      FlowPathHistory \
      FlowPathIntermediateEdge \
      FlowPathNeighborUnreachableEdge \
      FlowPathNoRouteEdge \
      FlowPathNullRoutedEdge \
      FlowPolicyDenied \
      FlowReach \
      FlowReachPolicyRoute \
      FlowReachPostIn \
      FlowReachPostInInterface \
      FlowReachPostOutInterface \
      FlowReachPreInInterface \
      FlowReachPreOut \
      FlowReachPreOutEdge \
      FlowReachPreOutEdgeOrigin \
      FlowReachPreOutEdgePolicyRoute \
      FlowReachPreOutEdgeStandard \
      FlowReachPreOutInterface \
      FlowReachStep \
      FlowRoleAccepted \
      FlowRoleInconsistent \
      FlowRoleTransitInconsistent \
      FlowRoleTransitNode \
      FlowSameHeaderRoleTransitNode \
      FlowUnknown \
      LanAdjacent \
      > $FLOW_RESULTS || return 1
   batfish_date
   echo ": END: Query flow results from LogicBlox"
}
export -f batfish_query_flows

batfish_query_ospf() {
   batfish_date
   echo ": START: Query ospf (informational only)"
   batfish_expect_args 2 $# || return 1
   local OSPF=$1
   local WORKSPACE=$2
   batfish -log output -workspace $WORKSPACE -query -predicates \
      BestOspfE1Route \
      BestOspfE2Route \
      BestOspfIARoute \
      BestOspfRoute \
      OspfE1Route \
      OspfE2Route \
      OspfExport \
      OspfGeneratedRoute \
      OspfIARoute \
      OspfRoute \
      SetOspfGeneratedRoute \
      &> $OSPF
   batfish_date
   echo ": END: Query ospf (informational only)"
}
export -f batfish_query_ospf

batfish_query_policy() {
   batfish_date
   echo ": START: Query policy (informational only)"
   batfish_expect_args 2 $# || return 1
   local POLICY=$1
   local WORKSPACE=$2
   batfish -log output -workspace $WORKSPACE -query -predicates \
      AsPathDenyAdvert \
      AsPathLineMatchAs \
      AsPathLineMatchAsAtBeginning \
      AsPathLineMatchAsPair \
      AsPathLineMatchAsPairAtBeginning \
      AsPathLineMatchEmpty \
      AsPathPermitAdvert \
      PolicyMapDenyAdvert \
      PolicyMapDenyRoute \
      PolicyMapPermitAdvert \
      PolicyMapPermitRoute \
      &> $POLICY
   batfish_date
   echo ": END: Query policy (informational only)"
}
export -f batfish_query_policy

batfish_query_routes() {
   batfish_date
   echo ": START: Query routes (informational only)"
   batfish_expect_args 2 $# || return 1
   local ROUTES=$1
   local WORKSPACE=$2
   batfish -log output -workspace $WORKSPACE -query -predicates \
      ActiveGeneratedRoute \
      InstalledRoute \
      &> $ROUTES
   batfish_date
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
   batfish_date
   echo ": START: Parse vendor structures and serialize vendor-independent structures"
   batfish_expect_args 2 $# || return 1
   local VENDOR_SERIAL_DIR=$1
   local INDEP_SERIAL_DIR=$2
   mkdir -p $INDEP_SERIAL_DIR
   batfish -svpath $VENDOR_SERIAL_DIR -si -sipath $INDEP_SERIAL_DIR || return 1
   batfish_date
   echo ": END: Parse vendor structures and serialize vendor-independent structures"
}
export -f batfish_serialize_independent

batfish_serialize_vendor() {
   batfish_date
   echo ": START: Parse vendor configuration files and serialize vendor structures"
   batfish_expect_args 2 $# || return 1
   local TEST_RIG=$1
   local VENDOR_SERIAL_DIR=$2
   mkdir -p $VENDOR_SERIAL_DIR
   batfish -testrig $TEST_RIG -sv -svpath $VENDOR_SERIAL_DIR -ee -throwparser -throwlexer || return 1
   batfish_date
   echo ": END: Parse vendor configuration files and serialize vendor structures"
}
export -f batfish_serialize_vendor

batfish_serialize_vendor_with_roles() {
   batfish_date
   echo ": START: Parse vendor configuration files and serialize vendor structures"
   batfish_expect_args 3 $# || return 1
   local TEST_RIG=$1
   local VENDOR_SERIAL_DIR=$2
   local NODE_ROLES_PATH=$3
   mkdir -p $VENDOR_SERIAL_DIR
   batfish -testrig $TEST_RIG -sv -svpath $VENDOR_SERIAL_DIR -ee -nrpath $NODE_ROLES_PATH -throwparser -throwlexer || return 1
   batfish_date
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
   batfish_date
   echo ": START UNIT TEST: Vendor configuration parser"
   mkdir -p $OUTPUT_DIR
   batfish -testrig $UNIT_TEST_DIR -sv -svpath $OUTPUT_DIR -ppt
   batfish_date
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
