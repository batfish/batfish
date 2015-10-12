#!/usr/bin/env bash

batfish_confirm_analyze_multipath() {
   BATFISH_CONFIRM=batfish_confirm batfish_analyze_multipath $@
}
export -f batfish_confirm_analyze_multipath
   
batfish_analyze_multipath() {
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
   local ENV=multipath-default
   local QUESTIONNAME=multipath
   local RESULT=$PWD/$NAME-$ENV-$QUESTIONNAME-result

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

   echo "Answer multipath consistency question"
   $BATFISH_CONFIRM && { batfish_answer_multipath $BASE $ENV $QUESTIONNAME|| return 1 ; }

   echo "Inject discovered packets into network model"
   $BATFISH_CONFIRM && { batfish_post_flows $BASE $ENV $QUESTIONNAME || return 1 ; }

   echo "Get flow histories from LogicBlox"
   $BATFISH_CONFIRM && { batfish_get_history $BASE $ENV $QUESTIONNAME $RESULT|| return 1 ; }
}
export -f batfish_analyze_multipath

batfish_answer_multipath() {
   batfish_date
   echo ": START: Answer multipath consistency question"
   batfish_expect_args 3 $# || return 1
   local BASE=$1
   local ENVIRONMENT=$2
   local QUESTIONNAME=$3
   local QUESTION_SRC=$BATFISH_ROOT/example_questions/multipath.q
   local QUESTION_DST_DIR=$BASE/questions/$QUESTIONNAME
   local QUESTION_DST=$QUESTION_DST_DIR/question
   mkdir -p $QUESTION_DST_DIR
   cp $QUESTION_SRC $QUESTION_DST
   batfish -autobasedir $BASE -env $ENVIRONMENT -answer -questionname $QUESTIONNAME
   batfish_date
   echo ": END: Answer multipath consistency question"
}
export -f batfish_answer_multipath
