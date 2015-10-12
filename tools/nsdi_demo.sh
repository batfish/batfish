batfish_nsdi_demo_computation() {
   BATFISH_CONFIRM=batfish_confirm _batfish_nsdi_demo_computation $@
}
export -f batfish_nsdi_demo_computation

_batfish_nsdi_demo_computation() {
   local PREFIX=demo
   local NUM_MACHINES=0
   if [ -z "$PREFIX" ]; then
         echo "ERROR: Empty prefix" 1>&2
         return 1
   fi
   if [ -z "$BATFISH_CONFIRM" ]; then
      local BATFISH_CONFIRM=true
   fi
   local OLD_PWD=$PWD
   local TEST_RIG=$BATFISH_TEST_RIG_PATH/example-nsdi
   local QUERY_PATH=$OLD_PWD/$PREFIX-query
   local MPI_QUERY_BASE_PATH=$QUERY_PATH/multipath-inconsistency-query

   local BGP=$OLD_PWD/$PREFIX-bgp
   local DP_DIR=$OLD_PWD/$PREFIX-dp
   local DUMP_DIR=$OLD_PWD/$PREFIX-dump
   local FLOWS=$OLD_PWD/$PREFIX-flows
   local INDEP_SERIAL_DIR=$OLD_PWD/$PREFIX-indep
   local NODE_SET_PATH=$OLD_PWD/$PREFIX-node-set
   local OSPF=$OLD_PWD/$PREFIX-ospf
   local POLICY=$OLD_PWD/$PREFIX-policy
   local REACH_PATH=$OLD_PWD/$PREFIX-reach.smt2
   local ROUTES=$OLD_PWD/$PREFIX-routes
   local VENDOR_SERIAL_DIR=$OLD_PWD/$PREFIX-vendor
   local WORKSPACE=batfish-$USER-$PREFIX

   local STAGE1_PREDICATE=SetOspfInterfaceCost

   echo "Stage 1 Computation"
   $BATFISH_CONFIRM && \
   {
      echo "Parse vendor configuration files and serialize vendor structures"
      batfish_serialize_vendor $TEST_RIG $VENDOR_SERIAL_DIR || return 1 ;

      echo "Parse vendor structures and serialize vendor-independent structures"
      batfish_serialize_independent $VENDOR_SERIAL_DIR $INDEP_SERIAL_DIR || return 1 ;

      echo "Dump control plane facts"
      batfish -sipath $INDEP_SERIAL_DIR -dumpcp -dumpdir $DUMP_DIR || return 1 ;
   }

   echo "Stage 2 Computation"
   $BATFISH_CONFIRM && \
   {
      echo "Compute the fixed point of the control plane"
      batfish_compile $WORKSPACE $TEST_RIG $DUMP_DIR $INDEP_SERIAL_DIR || return 1 ;

      echo "Query bgp"
      batfish_query_bgp $BGP $WORKSPACE || return 1 ;

      echo "Query bgp"
      batfish_query_ospf $OSPF $WORKSPACE || return 1 ;

      echo "Query routes"
      batfish_query_routes $ROUTES $WORKSPACE || return 1 ;

      echo "Query data plane predicates"
      batfish_query_data_plane $WORKSPACE $DP_DIR || return 1 ;
   }

   echo "Stage 3 Computation"
   $BATFISH_CONFIRM && \
   {
      echo "Extract z3 reachability relations"
      batfish_generate_z3_reachability $DP_DIR $INDEP_SERIAL_DIR $REACH_PATH $NODE_SET_PATH || return 1 ;

      echo "Find multipath-inconsistent packet constraints"
      batfish_find_multipath_inconsistent_packet_constraints $REACH_PATH $QUERY_PATH $MPI_QUERY_BASE_PATH $NODE_SET_PATH "$MACHINES" "$NUM_MACHINES" || return 1 ;

      echo "Generate multipath-inconsistency concretizer queries"
      batfish_generate_multipath_inconsistency_concretizer_queries $MPI_QUERY_BASE_PATH $NODE_SET_PATH "$MACHINES" "$NUM_MACHINES"|| return 1 ;

      echo "Dump flows"
      batfish -workspace $WORKSPACE -flow -flowpath $QUERY_PATH -dumptraffic -dumpdir $DUMP_DIR || return 1

      echo "Format flows"
      batfish_format_flows $DUMP_DIR
   }

   echo "Stage 4 Computation"
   $BATFISH_CONFIRM && \
   {
      echo "Inject concrete packets into network model"
      batfish_inject_packets $WORKSPACE $QUERY_PATH $DUMP_DIR || return 1 ;

      echo "Query flow results from LogicBlox"
      batfish_query_flows $FLOWS $WORKSPACE || return 1 ;
   }

}
export -f _batfish_nsdi_demo_computation

batfish_nsdi_demo_results() {
   BATFISH_CONFIRM=batfish_confirm _batfish_nsdi_demo_results $@
}
export -f batfish_nsdi_demo_results

_batfish_nsdi_demo_results() {
   local PREFIX=demo
   local NUM_MACHINES=0
   if [ -z "$PREFIX" ]; then
         echo "ERROR: Empty prefix" 1>&2
         return 1
   fi
   if [ -z "$BATFISH_CONFIRM" ]; then
      local BATFISH_CONFIRM=true
   fi
   local OLD_PWD=$PWD
   local TEST_RIG=$BATFISH_TEST_RIG_PATH/example-nsdi
   local QUERY_PATH=$OLD_PWD/$PREFIX-query
   local MPI_QUERY_BASE_PATH=$QUERY_PATH/multipath-inconsistency-query

   local BGP=$OLD_PWD/$PREFIX-bgp
   local DP_DIR=$OLD_PWD/$PREFIX-dp
   local DUMP_DIR=$OLD_PWD/$PREFIX-dump
   local FLOWS=$OLD_PWD/$PREFIX-flows
   local INDEP_SERIAL_DIR=$OLD_PWD/$PREFIX-indep
   local NODE_SET_PATH=$OLD_PWD/$PREFIX-node-set
   local OSPF=$OLD_PWD/$PREFIX-ospf
   local POLICY=$OLD_PWD/$PREFIX-policy
   local REACH_PATH=$OLD_PWD/$PREFIX-reach.smt2
   local ROUTES=$OLD_PWD/$PREFIX-routes
   local VENDOR_SERIAL_DIR=$OLD_PWD/$PREFIX-vendor
   local WORKSPACE=batfish-$USER-$PREFIX

   local STAGE1_PREDICATE=SetOspfInterfaceCost

   echo "Stage 1 View Results"
   $BATFISH_CONFIRM && \
   {
      tail -n+2 $DUMP_DIR/$STAGE1_PREDICATE | sed 's/|/, /g' | sed "s/^\(.*\)\$/$STAGE1_PREDICATE(\\1)./g" | less ;
   }

   echo "Stage 2 View Results"
   $BATFISH_CONFIRM && \
   {
      grep '^OspfExport(' $OSPF | sort | less ;
      grep '^InstalledRoute(' $ROUTES | sort | less ;
      grep '^InstalledRoute(' $ROUTES | sort | grep '10.0.0.0/24' | less ;
      grep '^BgpAdvertisement(' $BGP | sort | less ;
   }

   echo "Stage 3 View Results"
   $BATFISH_CONFIRM && \
   {
      #less $($GNU_FIND $QUERY_PATH -type f -not -name '*concrete*' -and -name '*.smt2' | head -n1)
      less $DUMP_DIR/SetFlowOriginate.formatted
   }

   echo "Stage 4 View Results"
   $BATFISH_CONFIRM && \
   {
      sort $FLOWS | grep '^FlowPathHistory(' | sed -e 's/;/\n\t/g' | sed -e 's/\([^:]*\))/\n\1)\n/g' | sed -e 's/, \[/,\n\t[/g' | sed -e 's/Flow/\nFlow/g' | less ;
   }

}
export -f _batfish_nsdi_demo_results
