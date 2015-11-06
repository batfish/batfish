package org.batfish.common;

public class BfConsts {

   public enum TaskStatus {
      InProgress,
      TerminatedAbnormally,
      TerminatedNormally,
      Unknown,
      UnreachableOrBadResponse,
      Unscheduled
   }

   //
   // IMPORTANT
   // if you change the value of these constants,
   // ensure that the clients (including the javascript client) are up to date
   //
   public static final String ARG_BLOCK_NAMES = "blocknames";
   public static final String ARG_DIFF_ACTIVE = "diffactive";
   public static final String ARG_DIFF_ENVIRONMENT_NAME = "diffenv";
   public static final String ARG_ENVIRONMENT_NAME = "env";
   public static final String ARG_LOG_LEVEL = "loglevel";
   public static final String ARG_OUTPUT_ENV = "outputenv";
   public static final String ARG_PEDANTIC_AS_ERROR = "pedanticerror";
   public static final String ARG_PEDANTIC_SUPPRESS = "pedanticsuppress";
   public static final String ARG_PREDICATES = "predicates";
   public static final String ARG_QUESTION_NAME = "questionname";
   public static final String ARG_RED_FLAG_AS_ERROR = "redflagerror";
   public static final String ARG_RED_FLAG_SUPPRESS = "redflagsuppress";
   public static final String ARG_UNIMPLEMENTED_AS_ERROR = "unimplementederror";
   public static final String ARG_UNIMPLEMENTED_SUPPRESS = "unimplementedsuppress";
   public static final String ARG_USE_PRECOMPUTED_ADVERTISEMENTS = "useprecomputedadvertisements";
   public static final String ARG_USE_PRECOMPUTED_IBGP_NEIGHBORS = "useprecomputedibgpneighbors";
   public static final String ARG_USE_PRECOMPUTED_ROUTES = "useprecomputedroutes";

   public static final String COMMAND_ANSWER = "answer";
   public static final String COMMAND_CREATE_WORKSPACE = "createworkspace";
   public static final String COMMAND_DUMP_DP = "dp";
   public static final String COMMAND_FACTS = "facts";
   public static final String COMMAND_GENERATE_FACT = "dumpcp";
   public static final String COMMAND_GET_DIFFERENTIAL_HISTORY = "getdiffhistory";
   public static final String COMMAND_GET_HISTORY = "gethistory";
   public static final String COMMAND_KEEP_BLOCKS = "keepblocks";
   public static final String COMMAND_PARSE_VENDOR_INDEPENDENT = "si";
   public static final String COMMAND_PARSE_VENDOR_SPECIFIC = "sv";
   public static final String COMMAND_POST_DIFFERENTIAL_FLOWS = "postdiffflows";
   public static final String COMMAND_POST_FLOWS = "postflows";
   public static final String COMMAND_QUERY = "query";
   public static final String COMMAND_REMOVE_BLOCKS = "removeblocks";
   public static final String COMMAND_SYNTHESIZE_Z3_DATA_PLANE = "z3";
   public static final String COMMAND_WRITE_ADVERTISEMENTS = "writeadvertisements";
   public static final String COMMAND_WRITE_IBGP_NEIGHBORS = "writeibgpneighbors";

   public static final String COMMAND_WRITE_ROUTES = "writeroutes";

   public static final String PREDICATE_FLOW_PATH_HISTORY = "FlowPathHistory";
   public static final String RELPATH_BASE = "base";
   public static final String RELPATH_CONFIG_FILE_NAME_DIR = "config.Properties";
   public static final String RELPATH_CONFIGURATIONS_DIR = "configs";
   public static final String RELPATH_DATA_PLANE_DIR = "dp";
   public static final String RELPATH_DIFF = "diff";
   public static final String RELPATH_EDGE_BLACKLIST_FILE = "edge_blacklist";
   public static final String RELPATH_ENV_DIR = "env";
   public static final String RELPATH_ENV_NODE_SET = "env-node-set";
   public static final String RELPATH_ENVIRONMENTS_DIR = "environments";
   public static final String RELPATH_FACT_DUMP_DIR = "dump";
   public static final String RELPATH_FAILURE_QUERY_PREFIX = "failure-query";
   public static final String RELPATH_FLOWS_DUMP_DIR = "flowdump";
   public static final String RELPATH_INTERFACE_BLACKLIST_FILE = "interface_blacklist";
   public static final String RELPATH_LB_HOSTNAME_PATH = "lb";
   public static final String RELPATH_MULTIPATH_QUERY_PREFIX = "multipath-query";
   public static final String RELPATH_NODE_BLACKLIST_FILE = "node_blacklist";
   public static final String RELPATH_PRECOMPUTED_ROUTES = "precomputedroutes";
   public static final String RELPATH_QUERIES_DIR = "queries";
   public static final String RELPATH_QUERY_DUMP_DIR = "querydump";
   public static final String RELPATH_QUESTION_FILE = "question";
   public static final String RELPATH_QUESTION_PARAM_FILE = "parameters";
   public static final String RELPATH_QUESTIONS_DIR = "questions";
   public static final String RELPATH_TEST_RIG_DIR = "testrig";
   public static final String RELPATH_TOPOLOGY_FILE = "topology";
   public static final String RELPATH_VENDOR_INDEPENDENT_CONFIG_DIR = "indep";
   public static final String RELPATH_VENDOR_SPECIFIC_CONFIG_DIR = "vendor";

   public static final String RELPATH_Z3_DATA_PLANE_FILE = "dataplane.smt2";
   public static final String SVC_BASE_RSC = "/batfishservice";
   public static final String SVC_FAILURE_KEY = "failure";
   public static final String SVC_GET_STATUS_RSC = "getstatus";
   public static final String SVC_GET_TASKSTATUS_RSC = "gettaskstatus";
   public static final Integer SVC_PORT = 9999;
   public static final String SVC_RUN_TASK_RSC = "run";
   public static final String SVC_SUCCESS_KEY = "success";
   public static final String SVC_TASK_KEY = "task";
   public static final String SVC_TASKID_KEY = "taskid";

}
