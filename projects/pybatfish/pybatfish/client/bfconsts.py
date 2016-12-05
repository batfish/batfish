
#
# IMPORTANT
# This class was derived BfConsts.java in batfish-common
# It is important to keep the two in sync
#

class BfConsts:

    class TaskStatus:
        InProgress = 0
        TerminatedAbnormally = 1
        TerminatedNormally = 2
        Unknown = 3
        UnreachableOrBadResponse = 4
        Unscheduled = 5
   


    ARG_ANSWER_JSON_PATH = "answerjsonpath";
    ARG_BLOCK_NAMES = "blocknames";
    ARG_CONTAINER_DIR = "containerdir";
    ARG_DELTA_ENVIRONMENT_NAME = "deltaenv";
    ARG_DELTA_TESTRIG = "deltatestrig";
    ARG_DIFF_ACTIVE = "diffactive";
    ARG_ENVIRONMENT_NAME = "env";
    ARG_IGNORE_FILES_WITH_STRINGS = "ignorefileswithstrings";
    ARG_LOG_FILE = "logfile";
    ARG_LOG_LEVEL = "loglevel";
    ARG_OUTPUT_ENV = "outputenv";
    ARG_PEDANTIC_AS_ERROR = "pedanticerror";
    ARG_PEDANTIC_SUPPRESS = "pedanticsuppress";
    ARG_PLUGIN_DIRS = "plugindirs";
    ARG_QUESTION_NAME = "questionname";
    ARG_RED_FLAG_AS_ERROR = "redflagerror";
    ARG_RED_FLAG_SUPPRESS = "redflagsuppress";
    ARG_SYNTHESIZE_JSON_TOPOLOGY = "synthesizejsontopology";
    ARG_SYNTHESIZE_TOPOLOGY = "synthesizetopology";
    ARG_TASK_PLUGIN = "taskplugin";
    ARG_TESTRIG = "testrig";
    ARG_UNIMPLEMENTED_AS_ERROR = "unimplementederror";
    ARG_UNIMPLEMENTED_SUPPRESS = "unimplementedsuppress";
    ARG_UNRECOGNIZED_AS_RED_FLAG = "urf";
    ARG_USE_PRECOMPUTED_ADVERTISEMENTS = "useprecomputedadvertisements";
    ARG_USE_PRECOMPUTED_IBGP_NEIGHBORS = "useprecomputedibgpneighbors";
    ARG_USE_PRECOMPUTED_ROUTES = "useprecomputedroutes";

    COMMAND_ANSWER = "answer";
    COMMAND_COMPILE_DIFF_ENVIRONMENT = "diffcompile";
    COMMAND_DUMP_DP = "dp";
    COMMAND_PARSE_VENDOR_INDEPENDENT = "si";
    COMMAND_PARSE_VENDOR_SPECIFIC = "sv";
    COMMAND_QUERY = "query";
    COMMAND_REPORT = "report";
    COMMAND_WRITE_ADVERTISEMENTS = "writeadvertisements";
    COMMAND_WRITE_IBGP_NEIGHBORS = "writeibgpneighbors";
    COMMAND_WRITE_ROUTES = "writeroutes";

    KEY_BGP_ANNOUNCEMENTS = "Announcements";

    PROP_QUESTION_PLUGIN_DIR = "batfishQuestionPluginDir";

    RELPATH_ANSWER_HTML = "answer.html";
    RELPATH_ANSWER_JSON = "answer.json";
    RELPATH_AWS_VPC_CONFIGS_DIR = "aws_vpc_configs";
    RELPATH_AWS_VPC_CONFIGS_FILE = "aws_vpc_configs";
    RELPATH_BASE = "base";
    RELPATH_CONFIG_FILE_NAME_ALLINONE = "allinone.properties";
    RELPATH_CONFIG_FILE_NAME_BATFISH = "batfish.properties";
    RELPATH_CONFIG_FILE_NAME_CLIENT = "client.properties";
    RELPATH_CONFIG_FILE_NAME_COORDINATOR = "coordinator.properties";
    RELPATH_CONFIGURATIONS_DIR = "configs";
    RELPATH_CONVERT_ANSWER_PATH = "convert_answer";
    RELPATH_DATA_PLANE_DIR = "dp";
    RELPATH_DEFAULT_ENVIRONMENT_NAME = "env_default";
    RELPATH_DIFF = "diff";
    RELPATH_EDGE_BLACKLIST_FILE = "edge_blacklist";
    RELPATH_ENV_DIR = "env";
    RELPATH_ENV_NODE_SET = "env-node-set";
    RELPATH_ENVIRONMENTS_DIR = "environments";
    RELPATH_EXTERNAL_BGP_ANNOUNCEMENTS = "external_bgp_announcements.json";
    RELPATH_FAILURE_QUERY_PREFIX = "failure-query";
    RELPATH_FLOWS_DUMP_DIR = "flowdump";
    RELPATH_HOST_CONFIGS_DIR = "hosts";
    RELPATH_INTERFACE_BLACKLIST_FILE = "interface_blacklist";
    RELPATH_MULTIPATH_QUERY_PREFIX = "multipath-query";
    RELPATH_NODE_BLACKLIST_FILE = "node_blacklist";
    RELPATH_PARSE_ANSWER_PATH = "parse_answer";
    RELPATH_PRECOMPUTED_ROUTES = "precomputedroutes";
    RELPATH_QUERIES_DIR = "queries";
    RELPATH_QUESTION_FILE = "question";
    RELPATH_QUESTION_PARAM_FILE = "parameters";
    RELPATH_QUESTIONS_DIR = "questions";
    RELPATH_TEST_RIG_DIR = "testrig";
    RELPATH_TOPOLOGY_FILE = "topology";
    RELPATH_VENDOR_INDEPENDENT_CONFIG_DIR = "indep";
    RELPATH_VENDOR_SPECIFIC_CONFIG_DIR = "vendor";
    RELPATH_Z3_DATA_PLANE_FILE = "dataplane.smt2";

    SUFFIX_ANSWER_JSON_FILE = ".json";
    SUFFIX_LOG_FILE = ".log";

    SVC_BASE_RSC = "/batfishservice";
    SVC_FAILURE_KEY = "failure";
    SVC_GET_STATUS_RSC = "getstatus";
    SVC_GET_TASKSTATUS_RSC = "gettaskstatus";
    SVC_PORT = 9999;
    SVC_RUN_TASK_RSC = "run";
    SVC_SUCCESS_KEY = "success";
    SVC_TASK_KEY = "task";
    SVC_TASKID_KEY = "taskid";

