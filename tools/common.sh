#!/usr/bin/env bash

export BATFISH_ROOT="$($GNU_READLINK -f "$BATFISH_TOOLS_PATH/..")"
export BATFISH_PATH="$BATFISH_ROOT/projects/batfish"
export BATFISH_TEST_RIG_PATH="$BATFISH_ROOT/test_rigs"
export BATFISH="$BATFISH_PATH/batfish"
export BATFISH_Z3=z3
export BATFISH_Z3_DATALOG="$BATFISH_Z3 fixedpoint.engine=datalog fixedpoint.datalog.default_relation=doc fixedpoint.print_answer=true"
export BATFISH_NLS=nls
export BATFISH_PARALLEL='parallel --tag -v --eta --halt 2'
export BATFISH_NESTED_PARALLEL='parallel --tag -v --halt 2 -j1'

export BATFISH_CLIENT_PATH="$BATFISH_ROOT/projects/batfish-client"
export BATFISH_CLIENT="$BATFISH_CLIENT_PATH/batfish-client"

export BATFISH_DOCS_ROOT="$BATFISH_ROOT/docs"
export BATFISH_DOCS_DATAMODEL="$BATFISH_DOCS_ROOT/datamodel.json"

export BATFISH_WIKI_ROOT="$BATFISH_ROOT/../batfish.wiki"
export BATFISH_WIKI_DATAMODEL="$BATFISH_WIKI_ROOT/Datamodel.md"
export BATFISH_WIKI_QUESTIONS="$BATFISH_WIKI_ROOT/Questions.md"
export BATFISH_DATAMODEL_PAGE_SCRIPT="${BATFISH_TOOLS_PATH}/datamodel_page.py"
export BATFISH_QUESTIONS_PAGE_SCRIPT="${BATFISH_TOOLS_PATH}/questions_page.py"

export COORDINATOR_PATH="$BATFISH_ROOT/projects/coordinator"
export COORDINATOR="$COORDINATOR_PATH/coordinator"

export ALLINONE_PATH="$BATFISH_ROOT/projects/allinone"
export ALLINONE="$ALLINONE_PATH/allinone"

export COMMON_PATH="$BATFISH_ROOT/projects/batfish-common-protocol"
export COMMON_JAR="$COMMON_PATH/out/batfish-common-protocol.jar"

export QUESTION_PATH="$BATFISH_ROOT/projects/question"
export BATFISH_QUESTION_PLUGIN_DIR="$BATFISH_ROOT/projects/question/out"

batfish() {
   # if cygwin, shift and replace each parameter
   if batfish_cygwin; then
      local NUMARGS=$#
      local IGNORE_CURRENT_ARG=no;
      for i in $(seq 1 $NUMARGS); do
         local CURRENT_ARG=$1
         if [ "$IGNORE_CURRENT_ARG" = "yes" ]; then
            local NEW_ARG="$CURRENT_ARG"
            local IGNORE_CURRENT_ARG=no
         else
            local NEW_ARG="$(cygpath -w -- $CURRENT_ARG)"
         fi
         if [ "$CURRENT_ARG" = "-logicdir" -o "$CURRENT_ARG" = "-workspace" ]; then
            local IGNORE_CURRENT_ARG=yes
         fi
         set -- "$@" "$NEW_ARG"
         shift
      done
   fi
   if [ "$BATFISH_PRINT_CMDLINE" = "yes" ]; then
      echo "$BATFISH $BATFISH_COMMON_ARGS $@" >&2
   fi
   $BATFISH $BATFISH_COMMON_ARGS "$@"
}
export -f batfish

batfish_answer_example_cp() {
   batfish_date
   echo ": START: Answer provided example question"
   batfish_expect_args 3 $# || return 1
   local BASE=$1
   local ENVIRONMENT=$2
   local QUESTIONNAME=$3
   local QUESTION_SRC=$BATFISH_ROOT/example_questions/${QUESTIONNAME}.q
   local QUESTION_DST_DIR=$BASE/questions/$QUESTIONNAME
   local QUESTION_DST=$QUESTION_DST_DIR/question
   mkdir -p $QUESTION_DST_DIR
   cp $QUESTION_SRC $QUESTION_DST
   batfish -autobasedir $BASE -env $ENVIRONMENT -answer -questionname $QUESTIONNAME -loglevel output
   batfish_date
   echo ": END: Answer provided example question"
}
export -f batfish_answer_example_cp

batfish_build() {
   bash -c '_batfish_build "$@"' _batfish_build "$@" || return 1
   if [ "$BATFISH_COMPLETION_FILE" -ot "$BATFISH_PATH/out/batfish.jar" -a -e "$BATFISH_PATH/out/batfish.jar" ]; then
      echo -n "Generating bash completion file.."
      BATFISH_PRINT_CMDLINE=no batfish -help | grep -o '^ *-[a-zA-Z0-9]*' | tr -d ' ' | tr '\n' ' ' > $BATFISH_COMPLETION_FILE
      echo "OK"
   fi
}
export -f batfish_build

_batfish_build() {
   common_build || return 1
   cd $BATFISH_PATH
   ant "$@" || return 1
}
export -f _batfish_build

batfish_build_all() {
   bash -c '_batfish_build_all "$@"' _batfish_build_all "$@" || return 1
   if [ "$BATFISH_COMPLETION_FILE" -ot "$BATFISH_PATH/out/batfish.jar" -a -e "$BATFISH_PATH/out/batfish.jar" ]; then
      echo -n "Generating bash completion file.."
      BATFISH_PRINT_CMDLINE=no batfish -help | grep -o '^ *-[a-zA-Z0-9]*' | tr -d ' ' | tr '\n' ' ' > $BATFISH_COMPLETION_FILE
      echo "OK"
   fi
}
export -f batfish_build_all

_batfish_build_all() {
   common_build "$@" || return 1
   cd $BATFISH_PATH
   ant "$@" || return 1
   cd $COORDINATOR_PATH
   ant "$@" || return 1
   cd $BATFISH_CLIENT_PATH
   ant "$@" || return 1
   cd $QUESTION_PATH
   ant "$@" || return 1  
   cd $ALLINONE_PATH
   ant "$@" || return 1  
}
export -f _batfish_build_all

batfish_clone_environment() {
   batfish_date
   echo ": START: Clone environment"
   batfish_expect_args 3 $# || return 1
   local BASE=$1
   local ENV_SRC=$2
   local ENV_DST=$3
   cp -a $BASE/environments/$ENV_SRC $BASE/environments/$ENV_DST
   batfish_date
   echo ": END: Clone environment"
}
export -f batfish_clone_environment

batfish_compile() {
   batfish_date
   echo ": START: Compute the fixed point of the control plane"
   batfish_expect_args 2 $# || return 1
   local BASE=$1
   local ENV=$2
   batfish -autobasedir $BASE -env $ENV -cpfacts  -nlsdp || return 1
   batfish_date
   echo ": END: Compute the fixed point of the control plane"
}
export -f batfish_compile

batfish_compile_diff() {
   batfish_date
   echo ": START: Compute the fixed point of the control plane (differential)"
   batfish_expect_args 3 $# || return 1
   local BASE=$1
   local ENV=$2
   local DIFF_ENV=$3
   batfish -autobasedir $BASE -env $ENV -diffenv $DIFF_ENV -cpfacts -nlsdp -diffactive || return 1
   batfish_date
   echo ": END: Compute the fixed point of the control plane (differential)"
}
export -f batfish_compile_diff

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

batfish_cygwin() {
   [[ $(uname) == *"CYGWIN"* ]]
}
export -f batfish_cygwin

batfish_date() {
   { hostname; echo -n ': '; date ; } | tr -d '\n'
}
export -f batfish_date

batfish_delete_workspace() {
   batfish_date
   echo ": START: Delete LogicBlox workspace"
   batfish_expect_args 2 $# || return 1
   local BASE=$1
   local ENV=$2
   batfish -autobasedir $BASE -env $ENV -deleteworkspace || return 1
   batfish_date
   echo ": END: Delete LogicBlox workspace"
}
export -f batfish_delete_workspace

batfish_expect_args() {
   local EXPECTED_NUMARGS=$1
   local ACTUAL_NUMARGS=$2
   if [ "$EXPECTED_NUMARGS" -ne "$ACTUAL_NUMARGS" ]; then
      echo "${FUNCNAME[1]}: Expected $EXPECTED_NUMARGS arguments, but got $ACTUAL_NUMARGS" >&2
      return 1
   fi   
}
export -f batfish_expect_args

batfish_expect_min_args() {
   local EXPECTED_NUMARGS=$1
   local ACTUAL_NUMARGS=$2
   if [ "$EXPECTED_NUMARGS" -gt "$ACTUAL_NUMARGS" ]; then
      echo "${FUNCNAME[1]}: Expected at least $EXPECTED_NUMARGS arguments, but got $ACTUAL_NUMARGS" >&2
      return 1
   fi
}
export -f batfish_expect_min_args

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
      echo "Node=$NODE, SrcIp=$SRCIP, DstIp=$DSTIP, SrcPort=$SRCPORT, DstPort=$DSTPORT, IpProtocol=$PROT"
   done > $DUMP_DIR/SetFlowOriginate.formatted
}
export -f batfish_format_flows

batfish_javadocs() {
   echo "Generating batfish project javadocs"
   batfish_build_all doc
   cp -r $COMMON_PATH/doc $BATFISH_ROOT/doc/batfish-common-protocol/
   cp -r $BATFISH_PATH/doc $BATFISH_ROOT/doc/batfish/
   cp -r $BATFISH_CLIENT_PATH/doc $BATFISH_ROOT/doc/batfish-client/
   cp -r $COORDINATOR_PATH/doc $BATFISH_ROOT/doc/coordinator/
   cp -r $ALLINONE_PATH/doc $BATFISH_ROOT/doc/coordinator/
}
export -f batfish_javadocs

batfish_get_history() {
   batfish_date
   echo ": START: Get flow histories"
   batfish_expect_args 4 $# || return 1
   local BASE=$1
   local ENV=$2
   local QUESTIONNAME=$3
   local RESULT=$4
   batfish -autobasedir $BASE -env $ENV -questionname $QUESTIONNAME -history -logtee -loglevel output -logfile $RESULT || return 1
   batfish_date
   echo ": END: Get flow histories"
}
export -f batfish_get_history

batfish_get_history_diff() {
   batfish_date
   echo ": START: Get flow"
   batfish_expect_args 6 $# || return 1
   local BASE=$1
   local ENV=$2
   local DIFF_ENV=$3
   local QUESTIONNAME=$4
   local RESULT=$5
   local RESULT_JSON=$6
   batfish -autobasedir $BASE -env $ENV -diffquestion -diffenv $DIFF_ENV -questionname $QUESTIONNAME -history -logtee -loglevel output -logfile $RESULT -answerjsonpath $RESULT_JSON || return 1
   batfish_date
   echo ": END: Get flow histories"
}
export -f batfish_get_history_diff

batfish_get_history_diff_active() {
   batfish_date
   echo ": START: Get flow"
   batfish_expect_args 6 $# || return 1
   local BASE=$1
   local ENV=$2
   local DIFF_ENV=$3
   local QUESTIONNAME=$4
   local RESULT=$5
   local RESULT_JSON=$6
   batfish -autobasedir $BASE -env $ENV -diffactive -diffenv $DIFF_ENV -questionname $QUESTIONNAME -history -logtee -loglevel output -logfile $RESULT -answerjsonpath $RESULT_JSON || return 1
   batfish_date
   echo ": END: Get flow histories"
}
export -f batfish_get_history_diff_active

batfish_get_topology_interfaces() {
   batfish_date
   echo ": START: Get topology interfaces"
   batfish_expect_args 5 $# || return 1
   local BASE=$1
   local ENV=$2
   local DIFF_ENV=$3
   local QUESTIONNAME=$4
   local RESULT=$5
   batfish -autobasedir $BASE -env $ENV -diffenv $DIFF_ENV -questionname $QUESTIONNAME -getdiffhistory -logtee -loglevel output -logfile $RESULT || return 1
   batfish_date
   echo ": END: Get topology interfaces"
}
export -f batfish_get_topology_interfaces

batfish_output_topology() {
   batfish_expect_args 1 $# || return 1
   local BASE=$1
   batfish -autobasedir $BASE -synthesizetopology -loglevel output
}
export -f batfish_output_topology

batfish_post_flows() {
   batfish_date
   echo ": START: Inject discovered packets into network model"
   batfish_expect_args 3 $# || return 1
   local BASE=$1
   local ENV=$2
   local QUESTIONNAME=$3
   batfish -autobasedir $BASE -env $ENV -questionname $QUESTIONNAME -nlstraffic || return 1
   batfish_date
   echo ": END: Inject discovered packets into network model"
}
export -f batfish_post_flows

batfish_post_flows_diff() {
   batfish_date
   echo ": START: Inject discovered packets into network model (differential)"
   batfish_expect_args 4 $# || return 1
   local BASE=$1
   local ENV=$2
   local DIFF_ENV=$3
   local QUESTIONNAME=$4
   batfish -autobasedir $BASE -env $ENV -diffenv $DIFF_ENV -diffquestion -questionname $QUESTIONNAME -nlstraffic || return 1
   batfish_date
   echo ": END: Inject discovered packets into network model (differential)"
}
export -f batfish_post_flows_diff

batfish_post_flows_diff_active() {
   batfish_date
   echo ": START: Inject discovered packets into network model (use delta environment)"
   batfish_expect_args 4 $# || return 1
   local BASE=$1
   local ENV=$2
   local DIFF_ENV=$3
   local QUESTIONNAME=$4
   batfish -autobasedir $BASE -env $ENV -diffenv $DIFF_ENV -diffactive -questionname $QUESTIONNAME -nlstraffic || return 1
   batfish_date
   echo ": END: Inject discovered packets into network model (use delta environment)"
}
export -f batfish_post_flows_diff_active

batfish_prepare_default_environment() {
   batfish_date
   echo ": START: Prepare default environment"
   batfish_expect_args 2 $# || return 1
   local BASE=$1
   local ENV=$2
   mkdir -p $BASE/environments/$ENV/env || return 1
   batfish_date
   echo ": END: Prepare default environment"
}
export -f batfish_prepare_default_environment

batfish_prepare_test_rig() {
   batfish_date
   echo ": START: Prepare test-rig"
   batfish_expect_args 3 $# || return 1
   local TEST_RIG=$1
   local BASE=$2
   local NAME=$3
   mkdir -p $BASE/$NAME/testrig || return 1
   mkdir -p $BASE/$NAME/environments/default/env_default
   cp -r $TEST_RIG/. $BASE/$NAME/testrig/.
   batfish_date
   echo ": END: Prepare test-rig"
}
export -f batfish_prepare_test_rig

batfish_print_symmetric_edges() {
   batfish_expect_args 1 $# || return 1
   local BASE=$1
   batfish -autobasedir $BASE -printsymmetricedges -loglevel output
}
export -f batfish_print_symmetric_edges

batfish_query_data_plane() {
   batfish_date
   echo ": START: Query data plane predicates"
   batfish_expect_args 2 $# || return 1
   local BASE=$1
   local ENV=$2
   batfish -autobasedir $BASE -env $ENV -dp || return 1
   batfish_date
   echo ": END: Query data plane predicates"
}
export -f batfish_query_data_plane

batfish_query_data_plane_diff() {
   batfish_date
   echo ": START: Query data plane predicates (differential)"
   batfish_expect_args 3 $# || return 1
   local BASE=$1
   local ENV=$2
   local DIFF_ENV=$3
   batfish -autobasedir $BASE -env $ENV -diffenv $DIFF_ENV -diffactive -dp || return 1
   batfish_date
   echo ": END: Query data plane predicates (differential)"
}
export -f batfish_query_data_plane_diff

batfish_query_flows() {
   batfish_date
   echo ": START: Query flow results from LogicBlox"
   batfish_expect_args 2 $# || return 1
   local FLOW_RESULTS=$1
   local WORKSPACE=$2
   batfish -loglevel output -workspace $WORKSPACE -query -predicates \
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
   batfish -loglevel output -workspace $WORKSPACE -query -predicates \
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
   batfish -loglevel output -workspace $WORKSPACE -query -predicates \
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

batfish_query_predicate() {
   [ "$#" -gt 2 ] || return 1
   local BASE=$1
   shift
   local ENV=$1
   shift
   local PREDICATES="$@"
   batfish -loglevel output -autobasedir $BASE -env $ENV -query -predicates $PREDICATES
}
export -f batfish_query_predicate

batfish_query_routes() {
   batfish_date
   echo ": START: Query routes (informational only)"
   batfish_expect_args 2 $# || return 1
   local ROUTES=$1
   local WORKSPACE=$2
   batfish -loglevel output -workspace $WORKSPACE -query -predicates \
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
   if batfish_cygwin; then
      bash -c _batfish_replace_symlinks || return 1
   fi
}
export -f batfish_replace_symlinks

_batfish_replace_symlinks() {
   if [[ "$CYGWIN" =~ .*winsymlinks:native.* ]]; then
      return
   fi
   cd $BATFISH_ROOT
   if [ -d ".git" ]; then
      echo "(Cygwin workaround) Updating git index to ignore changes to symlinks"
      git update-index --assume-unchanged $($GNU_FIND projects -type l) || return 1
   fi
   echo "(Cygwin workaround) Replacing symlinks"
   $GNU_FIND projects -type l | parallel _batfish_replace_symlink "{}" \;
   if [ "${PIPESTATUS[0]}" -ne 0 -o "${PIPESTATUS[1]}" -ne 0 ]; then
      return 1
   fi
}
export -f _batfish_replace_symlinks

_batfish_replace_symlink() {
   SYMLINK=$1
   echo "SYMLINK=$SYMLINK"
   TARGET="$($GNU_READLINK $SYMLINK)"
   echo "TARGET=$TARGET"
   ABSOLUTE_TARGET="$($GNU_READLINK -f $SYMLINK)"
   echo "ABSOLUTE_TARGET=$ABSOLUTE_TARGET"
   rm "$SYMLINK" || return 1
   cp -a "$ABSOLUTE_TARGET" "$SYMLINK" || return 1
}
export -f _batfish_replace_symlink

batfish_serialize_independent() {
   batfish_date
   echo ": START: Parse vendor structures and serialize vendor-independent structures"
   batfish_expect_args 1 $# || return 1
   local BASE=$1
   batfish -autobasedir $BASE -si || return 1
   batfish_date
   echo ": END: Parse vendor structures and serialize vendor-independent structures"
}
export -f batfish_serialize_independent

batfish_serialize_vendor() {
   batfish_date
   echo ": START: Parse vendor configuration files and serialize vendor structures"
   batfish_expect_args 1 $# || return 1
   local BASE=$1
   batfish -autobasedir $BASE -sv -ee -throwparser -throwlexer -unimplementedsuppress || return 1
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

batfish_unit_tests_parser() {
   local UNIT_TEST_NAME=unit-tests
   local UNIT_TEST_DIR=$BATFISH_TEST_RIG_PATH/$UNIT_TEST_NAME
   batfish_date
   echo ": START UNIT TEST: Vendor configuration parser"
   batfish_prepare_test_rig $UNIT_TEST_DIR $PWD $UNIT_TEST_NAME
   batfish -containerdir $PWD -testrig $UNIT_TEST_NAME -sv "$@" -haltonparseerror -haltonconverterror -urf false
   batfish_date
   echo ": END UNIT TEST: Vendor configuration parser"
}
export -f batfish_unit_tests_parser

batfish_datamodel() {
   echo "Generating datamodel to " $BATFISH_DOCS_DATAMODEL
   batfish_client -runmode gendatamodel > $BATFISH_DOCS_DATAMODEL

   echo "Generating wiki page to " $BATFISH_WIKI_DATAMODEL
   pybatfish $BATFISH_DATAMODEL_PAGE_SCRIPT $BATFISH_DOCS_DATAMODEL > $BATFISH_WIKI_DATAMODEL
}
export -f batfish_datamodel

batfish_wiki_questions() {
   echo "Generating questions to " $BATFISH_WIKI_QUESTIONS
   pybatfish $BATFISH_QUESTIONS_PAGE_SCRIPT "$QUESTION_PATH/src" > $BATFISH_WIKI_QUESTIONS
}
export -f batfish_wiki_questions

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
      time "$@"
   }
fi
export -f batfish_time

coordinator() {
   # if cygwin, shift and replace each parameter
   if batfish_cygwin; then
      local NUMARGS=$#
      for i in $(seq 1 $NUMARGS); do
         local CURRENT_ARG=$1
         local NEW_ARG="$(cygpath -w -- $CURRENT_ARG)"
         set -- "$@" "$NEW_ARG"
         shift
      done
   fi
   if [ "$COORDINATOR_PRINT_CMDLINE" = "yes" ]; then
      echo "$COORDINATOR $COORDINATOR_COMMON_ARGS $@"
   fi
   $COORDINATOR $COORDINATOR_COMMON_ARGS "$@"
}
export -f coordinator

batfish_client() {
   # if cygwin, shift and replace each parameter
   if batfish_cygwin; then
      local NUMARGS=$#
      for i in $(seq 1 $NUMARGS); do
         local CURRENT_ARG=$1
         local NEW_ARG="$(cygpath -w -- $CURRENT_ARG)"
         set -- "$@" "$NEW_ARG"
         shift
      done
   fi
   if [ "$BATFISH_CLIENT_PRINT_CMDLINE" = "yes" ]; then
      echo "$BATFISH_CLIENT $BATFISH_CLIENT_COMMON_ARGS $@"
   fi
   $BATFISH_CLIENT $BATFISH_CLIENT_COMMON_ARGS "$@"
}
export -f batfish_client

client_build() {
   bash -c '_client_build "$@"' _client_build "$@" || return 1
}
export -f client_build

_client_build() {
   common_build || return 1
   cd $BATFISH_CLIENT_PATH
   ant "$@" || return 1
}
export -f _client_build

pybatfish() {
   bash -c '_pybatfish "$@"' _pybatfish "$@" || return 1
}
export -f pybatfish

_pybatfish() {
   PYTHONPATH="${PYBATFISH_PATH}:${PYTHONPATH}" python2.7 "$@" || return 1
}
export -f _pybatfish

ipybatfish() {
   bash -c '_ipybatfish "$@"' _ipybatfish "$@" || return 1
}
export -f ipybatfish

_ipybatfish() {
   PYTHONPATH="${PYBATFISH_PATH}:${PYTHONPATH}" ipython2 "$@" || return 1
}
export -f _ipybatfish

allinone() {
   # if cygwin, shift and replace each parameter
   if batfish_cygwin; then
      local NUMARGS=$#
      for i in $(seq 1 $NUMARGS); do
         local CURRENT_ARG=$1
         local NEW_ARG="$(cygpath -w -- $CURRENT_ARG)"
         set -- "$@" "$NEW_ARG"
         shift
      done
   fi
   if [ "$ALLINONE_PRINT_CMDLINE" = "yes" ]; then
      echo "$ALLINONE $ALLINONE_COMMON_ARGS $@"
   fi
   $ALLINONE $ALLINONE_COMMON_ARGS "$@"
}
export -f allinone

allinone_build() {
   bash -c '_allinone_build "$@"' _allinone_build "$@" || return 1
}

_allinone_build() {
   common_build || return 1
   cd $BATFISH_PATH
   ant "$@" || return 1
   cd $COORDINATOR_PATH
   ant "$@" || return 1
   cd $BATFISH_CLIENT_PATH
   ant "$@" || return 1
   cd $ALLINONE_PATH
   ant "$@" || return 1
}
export -f _allinone_build

coordinator_build() {
   bash -c '_coordinator_build "$@"' _coordinator_build "$@" || return 1
}
export -f coordinator_build

_coordinator_build() {
   common_build || return 1
   cd $COORDINATOR_PATH
   ant "$@" || return 1
}
export -f _coordinator_build

common_build() {
   bash -c '_common_build "$@"' _common_build "$@" || return 1
}
export -f common_build

_common_build() {
   batfish_replace_symlinks || return 1
   cd $COMMON_PATH
   ant "$@" || return 1
}
export -f _common_build

batfish_tests_update() {
   bash -c '_batfish_tests_update' _batfish_tests_update || return 1
}
export -f batfish_tests_update

_batfish_tests_update() {
   cd $BATFISH_ROOT
   find -name '*.testout' | while read f; do mv $f "$(dirname "$f")/$(basename "$f" .testout)"; done
}
export -f _batfish_tests_update

if batfish_cygwin; then
   export ANT_BATFISH_PATH="$(cygpath -w "${BATFISH_PATH}")"
   export ANT_BATFISH_CLIENT_PATH="$(cygpath -w "${BATFISH_CLIENT_PATH}")"
   export ANT_COMMON_PATH="$(cygpath -w "${COMMON_PATH}")"
   export BATFISH_JAVA_QUESTION_PLUGIN_DIR="$(cygpath -w "${BATFISH_QUESTION_PLUGIN_DIR}")"
else
   export ANT_BATFISH_PATH="${BATFISH_PATH}"
   export ANT_BATFISH_CLIENT_PATH="${BATFISH_CLIENT_PATH}"
   export ANT_COMMON_PATH="${COMMON_PATH}"
   export BATFISH_JAVA_QUESTION_PLUGIN_DIR="${BATFISH_QUESTION_PLUGIN_DIR}"
fi
