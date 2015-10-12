#!/usr/bin/env bash

batfish_analyze_link_failures() {
   batfish_expect_args 1 $# || return 1
   local TEST_RIG_RELATIVE=$1
   if [ "$(echo $TEST_RIG_RELATIVE | head -c1)" = "/" ]; then
      local TEST_RIG=$TEST_RIG_RELATIVE
   else
      local TEST_RIG=$PWD/$TEST_RIG_RELATIVE
   fi
   local TEST_RIG=$1
   local NAME=$(basename $TEST_RIG)
   local BASE=$PWD/$NAME
   local ENV=failure-default
   local QUESTIONNAME=failure
   local TOPOLOGY_HEADER=BATFISH_TOPOLOGY

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

   batfish_print_symmetric_edges $BASE | while read EDGE_RAW REV_EDGE_RAW; do
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

      echo "Get flow histories from LogicBlox for $EDGE_RAW"
      $BATFISH_CONFIRM && { batfish_get_history_diff $BASE $ENV $ENV_CURRENT $QUESTIONNAME $RESULT_CURRENT || return 1 ; }

      echo "Delete current workspace (workaround)"
      $BATFISH_CONFIRM && { batfish_delete_workspace $BASE $ENV_CURRENT || return 1 ; }
      
   done
}
export -f batfish_analyze_link_failures

batfish_analyze_node_failures() {
   batfish_expect_args 1 $# || return 1
   local TEST_RIG_RELATIVE=$1
   if [ "$(echo $TEST_RIG_RELATIVE | head -c1)" = "/" ]; then
      local TEST_RIG=$TEST_RIG_RELATIVE
   else
      local TEST_RIG=$PWD/$TEST_RIG_RELATIVE
   fi
   local TEST_RIG=$1
   local NAME=$(basename $TEST_RIG)
   local BASE=$PWD/$NAME
   local ENV=failure-default
   local QUESTIONNAME=failure

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

   $GNU_FIND $BASE/indep -type f -printf '%f\n' | sort | while read NODE_RAW; do
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

      echo "Get flow histories from LogicBlox for $NODE"
      $BATFISH_CONFIRM && { batfish_get_history_diff $BASE $ENV $ENV_CURRENT $QUESTIONNAME $RESULT_CURRENT || return 1 ; }

      echo "Delete current workspace (workaround)"
      $BATFISH_CONFIRM && { batfish_delete_workspace $BASE $ENV_CURRENT || return 1 ; }
      
   done
}
export -f batfish_analyze_node_failures

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

