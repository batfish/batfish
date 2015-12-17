#!/usr/bin/env bash

batfish_analyze_link_failures() {
   bash -c '_batfish_analyze_link_failures "$@"' _batfish_analyze_link_failures "$@" || return 1
}
export -f batfish_analyze_link_failures

_batfish_analyze_link_failures() {
   batfish_expect_args 1 $# || return 1
   export TEST_RIG_RELATIVE=$1
   if [ "$(echo $TEST_RIG_RELATIVE | head -c1)" = "/" ]; then
      export TEST_RIG=$TEST_RIG_RELATIVE
   else
      export TEST_RIG=$PWD/$TEST_RIG_RELATIVE
   fi
   export TEST_RIG=$1
   export NAME=$(basename $TEST_RIG)
   export BASE=$PWD/$NAME
   export ENV=failure-default
   export QUESTIONNAME=failure
   export TOPOLOGY_HEADER=BATFISH_TOPOLOGY

   echo "Prepare test-rig"
   $BATFISH_CONFIRM && { batfish_prepare_test_rig $TEST_RIG $BASE || return 1 ; }

   echo "Prepare default environment"
   $BATFISH_CONFIRM && { batfish_prepare_default_environment $BASE $ENV || return 1 ; }

   echo "Parse vendor configuration files and serialize vendor structures"
   $BATFISH_CONFIRM && { batfish_serialize_vendor $BASE || return 1 ; }

   echo "Parse vendor structures and serialize vendor-independent structures"
   $BATFISH_CONFIRM && { batfish_serialize_independent $BASE || return 1 ; }

   echo "Compute the fixed point of the control plane"
   $BATFISH_CONFIRM && { batfish_compile $BASE $ENV || return 1 ; }

   echo "Query data plane predicates"
   $BATFISH_CONFIRM && { batfish_query_data_plane $BASE $ENV || return 1 ; }

   batfish_analyze_link_failures_helper() {
      local EDGE_RAW="$1"
      local REV_EDGE_RAW="$2"
      local EDGE=$(echo $EDGE_RAW | tr '/' '_' | tr ':' '_')
      local ENV_CURRENT=failure-$EDGE
      local RESULT_CURRENT=$PWD/$NAME-$ENV_CURRENT-$QUESTIONNAME-result

      echo "Prepare failure environment for $EDGE_RAW"
      $BATFISH_CONFIRM && { batfish_prepare_single_edge_failure_environment $BASE $ENV_CURRENT $TOPOLOGY_HEADER "$EDGE_RAW" "$REV_EDGE_RAW" || return 1 ; }
      
      echo "Compute the fixed point of the control plane for failure environment for $EDGE_RAW"
      $BATFISH_CONFIRM && { batfish_compile_diff $BASE $ENV $ENV_CURRENT || return 1 ; }

      echo "Query data plane predicates for $EDGE_RAW"
      $BATFISH_CONFIRM && { batfish_query_data_plane_diff $BASE $ENV $ENV_CURRENT || return 1 ; }

      echo "Answer failure consistency question for $EDGE_RAW"
      $BATFISH_CONFIRM && { batfish_answer_failure $BASE $ENV $ENV_CURRENT $QUESTIONNAME || return 1 ; }
   
      echo "Inject discovered packets into network model for $EDGE_RAW"
      $BATFISH_CONFIRM && { batfish_post_flows_diff $BASE $ENV $ENV_CURRENT $QUESTIONNAME || return 1 ; }

      echo "Get flow histories for $EDGE_RAW"
      $BATFISH_CONFIRM && { batfish_get_history_diff $BASE $ENV $ENV_CURRENT $QUESTIONNAME $RESULT_CURRENT || return 1 ; }

   }
   export -f batfish_analyze_link_failures_helper

   batfish_print_symmetric_edges $BASE | $BATFISH_PARALLEL --colsep ' ' batfish_analyze_link_failures_helper "{1}" "{2}"

}
export -f _batfish_analyze_link_failures

batfish_analyze_node_failures() {
   bash -c '_batfish_analyze_node_failures "$@"' _batfish_analyze_node_failures "$@" || return 1
}
export -f batfish_analyze_node_failures

_batfish_analyze_node_failures() {
   batfish_expect_args 1 $# || return 1
   export TEST_RIG_RELATIVE=$1
   if [ "$(echo $TEST_RIG_RELATIVE | head -c1)" = "/" ]; then
      export TEST_RIG=$TEST_RIG_RELATIVE
   else
      export TEST_RIG=$PWD/$TEST_RIG_RELATIVE
   fi
   export TEST_RIG=$1
   export NAME=$(basename $TEST_RIG)
   export BASE=$PWD/$NAME
   export ENV=failure-default
   export QUESTIONNAME=failure

   echo "Prepare test-rig"
   $BATFISH_CONFIRM && { batfish_prepare_test_rig $TEST_RIG $BASE || return 1 ; }

   echo "Prepare default environment"
   $BATFISH_CONFIRM && { batfish_prepare_default_environment $BASE $ENV || return 1 ; }

   echo "Parse vendor configuration files and serialize vendor structures"
   $BATFISH_CONFIRM && { batfish_serialize_vendor $BASE || return 1 ; }

   echo "Parse vendor structures and serialize vendor-independent structures"
   $BATFISH_CONFIRM && { batfish_serialize_independent $BASE || return 1 ; }

   echo "Compute the fixed point of the control plane"
   $BATFISH_CONFIRM && { batfish_compile $BASE $ENV || return 1 ; }

   echo "Query data plane predicates"
   $BATFISH_CONFIRM && { batfish_query_data_plane $BASE $ENV || return 1 ; }

   batfish_analyze_node_failures_helper() {
      local NODE_RAW=$1
      local NODE=$(echo $NODE_RAW | tr '/' '_')
      local ENV_CURRENT=failure-$NODE
      local RESULT_CURRENT=$PWD/$NAME-$ENV_CURRENT-$QUESTIONNAME-result

      echo "Prepare failure environment for $NODE"
      $BATFISH_CONFIRM && { batfish_prepare_single_node_failure_environment $BASE $ENV_CURRENT $NODE || return 1 ; }
      
      echo "Compute the fixed point of the control plane for failure environment for $NODE"
      $BATFISH_CONFIRM && { batfish_compile_diff $BASE $ENV $ENV_CURRENT || return 1 ; }

      echo "Query data plane predicates for $NODE"
      $BATFISH_CONFIRM && { batfish_query_data_plane_diff $BASE $ENV $ENV_CURRENT || return 1 ; }

      echo "Answer failure consistency question for $NODE"
      $BATFISH_CONFIRM && { batfish_answer_failure $BASE $ENV $ENV_CURRENT $QUESTIONNAME || return 1 ; }
   
      echo "Inject discovered packets into network model for $NODE"
      $BATFISH_CONFIRM && { batfish_post_flows_diff $BASE $ENV $ENV_CURRENT $QUESTIONNAME || return 1 ; }

      echo "Get flow histories for $NODE"
      $BATFISH_CONFIRM && { batfish_get_history_diff $BASE $ENV $ENV_CURRENT $QUESTIONNAME $RESULT_CURRENT || return 1 ; }
   }
   export -f batfish_analyze_node_failures_helper

   $GNU_FIND $BASE/indep -type f -printf '%f\n' | sort | $BATFISH_PARALLEL batfish_analyze_node_failures_helper {}
}
export -f _batfish_analyze_node_failures

batfish_answer_failure() {
   batfish_date
   echo ": START: Answer failure consistency question"
   batfish_expect_args 4 $# || return 1
   local BASE=$1
   local ENV=$2
   local DIFF_ENV=$3
   local QUESTIONNAME=$4
   local QUESTION_SRC=$BATFISH_ROOT/example_questions/failure.q
   local QUESTION_DST_DIR=$BASE/questions/$QUESTIONNAME
   local QUESTION_DST=$QUESTION_DST_DIR/question
   mkdir -p $QUESTION_DST_DIR
   cp $QUESTION_SRC $QUESTION_DST
   batfish -autobasedir $BASE -env $ENV -diffenv $DIFF_ENV -answer -questionname $QUESTIONNAME
   batfish_date
   echo ": END: Answer failure consistency question"
}
export -f batfish_answer_failure

batfish_confirm_analyze_link_failures() {
   BATFISH_CONFIRM=batfish_confirm batfish_analyze_link_failures $@
}
export batfish_confirm_analyze_link_failures

batfish_confirm_analyze_node_failures() {
   BATFISH_CONFIRM=batfish_confirm batfish_analyze_node_failures $@
}
export batfish_confirm_analyze_node_failures

batfish_prepare_single_edge_failure_environment() {
   batfish_date
   echo ": START: Prepare failure environment"
   batfish_expect_args 5 $# || return 1
   local BASE=$1
   local ENV=$2
   local TOPOLOGY_HEADER=$3
   local EDGE="$4"
   local REV_EDGE="$5"
   local ENV_DIR=$BASE/environments/$ENV/env
   mkdir -p $ENV_DIR || return 1
   echo $TOPOLOGY_HEADER > $ENV_DIR/edge_blacklist || return 1
   echo "$EDGE" >> $ENV_DIR/edge_blacklist || return 1
   echo "$REV_EDGE" >> $ENV_DIR/edge_blacklist || return 1
   batfish_date
   echo ": END: Prepare failure environment"
}
export -f batfish_prepare_single_edge_failure_environment

batfish_prepare_single_node_failure_environment() {
   batfish_date
   echo ": START: Prepare failure environment"
   batfish_expect_args 3 $# || return 1
   local BASE=$1
   local ENV=$2
   local NODE=$3
   local ENV_DIR=$BASE/environments/$ENV/env
   mkdir -p $ENV_DIR || return 1
   echo $NODE > $ENV_DIR/node_blacklist || return 1
   batfish_date
   echo ": END: Prepare failure environment"
}
export -f batfish_prepare_single_node_failure_environment

