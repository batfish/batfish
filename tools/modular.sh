#!/usr/bin/env bash

batfish_confirm_analyze_modular() {
   BATFISH_CONFIRM=batfish_confirm batfish_analyze_modular $@
}
export -f batfish_confirm_analyze_modular

batfish_analyze_modular() {
   local TEST_RIG_RELATIVE=$1
   if [ -z "$BATFISH_CONFIRM" ]; then
      local BATFISH_CONFIRM=true
   fi
   local OLD_PWD=$PWD
   if [ "$(echo $TEST_RIG_RELATIVE | head -c1)" = "/" ]; then
      local TEST_RIG=$TEST_RIG_RELATIVE
   else
      local TEST_RIG=$PWD/$TEST_RIG_RELATIVE
   fi
   local NAME=$(basename $TEST_RIG)
   local BASE=$PWD/$NAME
   local ENV_DEFAULT=modular-default
   local ENV_EBGP=modular-ebgp
   local ENV_IBGP=modular-ibgp
   local ENV_STAGE1=modular-stage1
   local ENV_STAGE2=modular-stage2
   local ENV_STAGE3=modular-stage3
   local ENV_STAGE4=modular-stage4
   local STAGE1_ROUTES=$PWD/stage1-routes
   local STAGE1_IBGP_NEIGHBORS=$PWD/stage1-ibgp-neighbors
   local STAGE2_ROUTES=$PWD/stage2-routes
   local STAGE2_ADVERTISEMENTS=$PWD/stage2-advertisements
   local STAGE3_ADVERTISEMENTS=$PWD/stage3-advertisements
   local QUESTIONNAME=multipath
   local RESULT=$PWD/$NAME-$ENV-$QUESTIONNAME-result

   echo "Prepare test-rig"
   $BATFISH_CONFIRM && { batfish_prepare_test_rig $TEST_RIG $BASE || return 1 ; }

   echo "Parse vendor configuration files and serialize vendor structures"
   $BATFISH_CONFIRM && { batfish_serialize_vendor $BASE || return 1 ; }

   echo "Parse vendor structures and serialize vendor-independent structures"
   $BATFISH_CONFIRM && { batfish_serialize_independent $BASE || return 1 ; }

   echo "Prepare default environment"
   $BATFISH_CONFIRM && { batfish_prepare_default_environment $BASE $ENV_DEFAULT || return 1 ; }

   echo "Prepare ebgp environment"
   $BATFISH_CONFIRM && { batfish_prepare_ebgp_environment $BASE $ENV_EBGP || return 1 ; }

   echo "Prepare ibgp environment"
   $BATFISH_CONFIRM && { batfish_prepare_ibgp_environment $BASE $ENV_IBGP || return 1 ; }

   echo "Prepare stage1 environment"
   $BATFISH_CONFIRM && { batfish_clone_environment $BASE $ENV_DEFAULT $ENV_STAGE1 || return 1 ; }

   echo "Prepare stage2 environment"
   $BATFISH_CONFIRM && { batfish_clone_environment $BASE $ENV_EBGP $ENV_STAGE2 || return 1 ; }

   echo "Prepare stage3 environment"
   $BATFISH_CONFIRM && { batfish_clone_environment $BASE $ENV_IBGP $ENV_STAGE3 || return 1 ; }

   echo "Prepare stage4 environment"
   $BATFISH_CONFIRM && { batfish_clone_environment $BASE $ENV_EBGP $ENV_STAGE4 || return 1 ; }

   echo "Run stage 1"
   $BATFISH_CONFIRM && { batfish_compile_modular_stage1 $BASE $ENV_STAGE1 $STAGE1_ROUTES $STAGE1_IBGP_NEIGHBORS || return 1 ; }

   echo "Run stage 2"
   $BATFISH_CONFIRM && { batfish_compile_modular_stage2 $BASE $ENV_STAGE2 $STAGE1_ROUTES $STAGE2_ROUTES $STAGE2_ADVERTISEMENTS || return 1 ; }

   echo "Run stage 3"
   $BATFISH_CONFIRM && { batfish_compile_modular_stage3 $BASE $ENV_STAGE3 $STAGE1_IBGP_NEIGHBORS $STAGE2_ROUTES $STAGE2_ADVERTISEMENTS $STAGE3_ADVERTISEMENTS || return 1 ; }

   echo "Run stage 4"
   $BATFISH_CONFIRM && { batfish_compile_modular_stage4 $BASE $ENV_STAGE4 $STAGE2_ROUTES $STAGE3_ADVERTISEMENTS || return 1 ; }

}
export -f batfish_analyze_modular

batfish_compile_modular_stage1() {
   batfish_date
   echo ": START: Run stage 1"
   batfish_expect_args 4 $# || return 1
   local BASE=$1
   local ENV=$2
   local WORKSPACE="$(basename ${BASE}):${ENV}"
   local OUT_ROUTES=$3
   local OUT_IBGP_NEIGHBORS=$4
   batfish -autobasedir $BASE -env $ENV -createworkspace -facts -keepblocks -dumpcp -blocknames Route Ospf_inter_area Ospf_intra_area Static_recursive Static_interface Bgp_ibgp_neighbors || return 1
   batfish_trace_import_transaction $WORKSPACE || return 1
   batfish -autobasedir $BASE -env $ENV -writeibgpneighbors -writeroutes -precomputedibgpneighborspath $OUT_IBGP_NEIGHBORS -precomputedroutespath $OUT_ROUTES || return 1
   batfish_date
   echo ": END: Run stage 1"
}
export -f batfish_compile_modular_stage1

batfish_compile_modular_stage2() {
   batfish_date
   echo ": START: Run stage 2"
   batfish_expect_args 5 $# || return 1
   local BASE=$1
   local ENV=$2
   local WORKSPACE="$(basename ${BASE}):${ENV}"
   local IN_ROUTES=$3
   local OUT_ROUTES=$4
   local OUT_ADVERTISEMENTS=$5
   batfish -autobasedir $BASE -env $ENV -createworkspace -facts -keepblocks -dumpcp -useprecomputedroutes -precomputedroutespath $IN_ROUTES -blocknames Route Precomputed Ospf_e1 Ospf_e2 Ospf_generation Static_recursive Static_interface Bgp_ebgp_igp_origination Bgp_ebgp_incoming_transformation Bgp_ebgp_outgoing_transformation || return 1
   batfish_trace_import_transaction $WORKSPACE || return 1
   batfish -autobasedir $BASE -env $ENV -writeroutes -precomputedroutespath $OUT_ROUTES -writeadvertisements -precomputedadvertisementspath $OUT_ADVERTISEMENTS || return 1
   batfish_date
   echo ": END: Run stage 2"
}
export -f batfish_compile_modular_stage2

batfish_compile_modular_stage3() {
   batfish_date
   echo ": START: Run stage 3"
   batfish_expect_args 6 $# || return 1
   local BASE=$1
   local ENV=$2
   local WORKSPACE="$(basename ${BASE}):${ENV}"
   local IN_IBGP_NEIGHBORS=$3
   local IN_ROUTES=$4
   local IN_ADVERTISEMENTS=$5
   local OUT_ADVERTISEMENTS=$6
   batfish -autobasedir $BASE -env $ENV -createworkspace -facts -keepblocks -dumpcp -useprecomputedadvertisements -useprecomputedroutes -useprecomputedibgpneighbors -precomputedroutespath $IN_ROUTES -precomputedibgpneighborspath $IN_IBGP_NEIGHBORS -precomputedadvertisementspath $IN_ADVERTISEMENTS -blocknames Route Precomputed Static_recursive Static_interface Bgp_ebgp_incoming_transformation Bgp_ibgp_igp_origination Bgp_ibgp_ebgp_origination Bgp_ibgp_incoming_transformation Bgp_ibgp_outgoing_transformation || return 1
   batfish_trace_import_transaction $WORKSPACE || return 1
   batfish -autobasedir $BASE -env $ENV -writeadvertisements -precomputedadvertisementspath $OUT_ADVERTISEMENTS || return 1
   batfish_date
   echo ": END: Run stage 3"
}
export -f batfish_compile_modular_stage3

batfish_compile_modular_stage4() {
   batfish_date
   echo ": START: Run stage 4"
   batfish_expect_args 4 $# || return 1
   local BASE=$1
   local ENV=$2
   local WORKSPACE="$(basename ${BASE}):${ENV}"
   local IN_ROUTES=$3
   local IN_ADVERTISEMENTS=$4
   batfish -autobasedir $BASE -env $ENV -createworkspace -facts -keepblocks -dumpcp -useprecomputedroutes -precomputedroutespath $IN_ROUTES -useprecomputedadvertisements -precomputedadvertisementspath $IN_ADVERTISEMENTS -blocknames Route Precomputed Static_recursive Static_interface Bgp_ebgp_incoming_transformation Bgp_ibgp_incoming_transformation Bgp_ebgp_outgoing_transformation Bgp_ebgp_bgp_origination Bgp_generation Bgp_ebgp_igp_origination || return 1
   batfish_trace_import_transaction $WORKSPACE || return 1
   batfish_date
   echo ": END: Run stage 4"
}
export -f batfish_compile_modular_stage4

batfish_prepare_ebgp_environment() {
   batfish_date
   echo ": START: Prepare ebgp environment"
   batfish_expect_args 2 $# || return 1
   local BASE=$1
   local ENV=$2
   local ENV_DIR=$BASE/environments/$ENV/env
   local QUESTION_NAME=print-ebgp-nodes
   local EBGP_QUESTION_SRC=$BATFISH_ROOT/example_questions/print-ebgp-nodes.q
   local EBGP_QUESTION_DST=$BASE/questions/$QUESTION_NAME/question
   mkdir -p $ENV_DIR || return 1
   mkdir -p $(dirname $EBGP_QUESTION_DST) || return 1
   cp $EBGP_QUESTION_SRC $EBGP_QUESTION_DST || return 1
   local RESULT=$(comm -23 <($GNU_FIND $BASE/indep -type f -printf '%f\n' | sort) <(batfish -autobasedir $BASE -env $ENV -loglevel output -answer -questionname $QUESTION_NAME | sort) || echo error) 
   if [[ $RESULT == "error" ]]; then 
      return 1 
   else
      echo $RESULT > $ENV_DIR/node_blacklist
   fi
   batfish_date
   echo ": END: Prepare ebgp environment"
}
export -f batfish_prepare_ebgp_environment

batfish_prepare_ibgp_environment() {
   batfish_date
   echo ": START: Prepare ibgp environment"
   batfish_expect_args 2 $# || return 1
   local BASE=$1
   local ENV=$2
   local ENV_DIR=$BASE/environments/$ENV/env
   local QUESTION_NAME=print-ibgp-nodes
   local IBGP_QUESTION_SRC=$BATFISH_ROOT/example_questions/print-ibgp-nodes.q
   local IBGP_QUESTION_DST=$BASE/questions/$QUESTION_NAME/question
   mkdir -p $ENV_DIR || return 1
   mkdir -p $(dirname $IBGP_QUESTION_DST) || return 1
   cp $IBGP_QUESTION_SRC $IBGP_QUESTION_DST || return 1
   local RESULT=$(comm -23 <($GNU_FIND $BASE/indep -type f -printf '%f\n' | sort) <(batfish -autobasedir $BASE -env $ENV -loglevel output -answer -questionname $QUESTION_NAME | sort) || echo error)
   if [[ $RESULT == "error" ]]; then 
      return 1 
   else
      echo $RESULT > $ENV_DIR/node_blacklist
   fi
   batfish_date
   echo ": END: Prepare ibgp environment"
}
export -f batfish_prepare_ibgp_environment

batfish_trace_import_transaction() {
   batfish_expect_args 1 $# || return 1
   local WORKSPACE=$1
   local LATEST_TXN=$($GNU_FIND -mindepth 1 -maxdepth 1 -name 'txndata*' -printf '%T@ %p\n' | sort -nr | cut -d' ' -f2- | head -n1)
   if [ -n "$LATEST_TXN" ]; then
      bash -c "_batfish_trace_import_transaction $WORKSPACE $LATEST_TXN" || return 1
      rm -rf txndata*
   fi
}
export -f batfish_trace_import_transaction

_batfish_trace_import_transaction() {
   batfish_expect_args 2 $# || return 1
   set -x
   local WORKSPACE=$1
   local TXN=$2
   cd $TXN
   sed -i -e "s/create --overwrite abc/open $WORKSPACE/g" import.lb || return 1
   lb import.lb || return 1
   set +x
}
export -f _batfish_trace_import_transaction
