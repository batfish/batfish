package org.batfish.common;

//
// IMPORTANT
// if you change the value of these constants,
// ensure that the clients (javascript, python, ... ) are up to date
//

public class BfConsts {

  public enum TaskStatus {
    InProgress,
    RequeueFailure,
    TerminatedAbnormally,
    TerminatedByUser,
    TerminatedNormally,
    Unknown,
    UnreachableOrBadResponse,
    Unscheduled;

    public boolean isTerminated() {
      return (this == TaskStatus.TerminatedAbnormally
          || this == TaskStatus.TerminatedByUser
          || this == TaskStatus.TerminatedNormally
          || this == TaskStatus.RequeueFailure);
    }
  }

  public static final String ABSPATH_CONFIG_FILE_NAME_ALLINONE =
      "/org/batfish/allinone/config/allinone.properties";
  public static final String ABSPATH_CONFIG_FILE_NAME_BATFISH =
      "/org/batfish/config/batfish.properties";
  public static final String ABSPATH_CONFIG_FILE_NAME_CLIENT =
      "/org/batfish/client/config/client.properties";
  public static final String ABSPATH_CONFIG_FILE_NAME_COORDINATOR =
      "/org/batfish/coordinator/config/coordinator.properties";
  public static final String ABSPATH_DEFAULT_SALT = "org/batfish/common/util/salt";

  public static final String ARG_ALWAYS_INCLUDE_ANSWER_IN_WORK_JSON_LOG = "alwaysanswerworkjson";
  public static final String ARG_BDP_DETAIL = "bdpdetail";
  public static final String ARG_BDP_MAX_OSCILLATION_RECOVERY_ATTEMPTS =
      "bdpmaxoscillationrecoveryattempts";
  public static final String ARG_BDP_MAX_RECORDED_ITERATIONS = "bdpmaxrecordediterations";
  public static final String ARG_BDP_PRINT_ALL_ITERATIONS = "bdpprintalliterations";
  public static final String ARG_BDP_PRINT_OSCILLATING_ITERATIONS = "bdpprintoscillatingiterations";
  public static final String ARG_BDP_RECORD_ALL_ITERATIONS = "bdprecordalliterations";
  public static final String ARG_CONTAINER = "container";
  public static final String ARG_DELTA_TESTRIG = "deltatestrig";
  public static final String ARG_DIFFERENTIAL = "differential";
  public static final String ARG_DISABLE_UNRECOGNIZED = "disableunrecognized";
  public static final String ARG_HALT_ON_CONVERT_ERROR = "haltonconverterror";
  public static final String ARG_HALT_ON_PARSE_ERROR = "haltonparseerror";
  public static final String ARG_IGNORE_FILES_WITH_STRINGS = "ignorefileswithstrings";
  public static final String ARG_IGNORE_MANAGEMENT_INTERFACES = "ignoremanagementinterfaces";
  public static final String ARG_LOG_LEVEL = "loglevel";
  public static final String ARG_QUESTION_NAME = "questionname";
  public static final String ARG_SNAPSHOT_NAME = "snapshotname";
  public static final String ARG_STORAGE_BASE = "storagebase";
  public static final String ARG_SYNTHESIZE_TOPOLOGY = "synthesizetopology";
  public static final String ARG_TASK_PLUGIN = "taskplugin";
  public static final String ARG_TESTRIG = "testrig";
  public static final String ARG_VERBOSE_PARSE = "verboseparse";
  public static final String COMMAND_ANSWER = "answer";
  public static final String COMMAND_DUMP_DP = "dp";
  public static final String COMMAND_INIT_INFO = "initinfo";
  public static final String COMMAND_PARSE_VENDOR_INDEPENDENT = "si";
  public static final String COMMAND_PARSE_VENDOR_SPECIFIC = "sv";
  @Deprecated public static final String COMMAND_VALIDATE_SNAPSHOT = "venv";

  /*
   * JSON key names
   */
  public static final String PROP_AGGREGATION = "aggregation";
  public static final String PROP_AGGREGATIONS = "aggregations";
  public static final String PROP_ALLINONE_PROPERTIES_PATH = "batfishAllinonePropertiesPath";
  @Deprecated public static final String PROP_ALLOWED_VALUES = "allowedValues";
  public static final String PROP_ANSWER_ELEMENTS = "answerElements";
  public static final String PROP_ASSERTION = "assertion";
  public static final String PROP_BATFISH_PROPERTIES_PATH = "batfishBatfishPropertiesPath";
  public static final String PROP_BGP_ANNOUNCEMENTS = "Announcements";
  public static final String PROP_CLIENT_PROPERTIES_PATH = "batfishClientPropertiesPath";
  public static final String PROP_COLUMN = "column";
  public static final String PROP_COLUMNS = "columns";
  public static final String PROP_COORDINATOR_PROPERTIES_PATH = "batfishCoordinatorPropertiesPath";
  public static final String PROP_DESCRIPTION = "description";
  public static final String PROP_DIFFERENTIAL = "differential";
  public static final String PROP_DISPLAY_HINTS = "displayHints";
  public static final String PROP_DISPLAY_NAME = "displayName";
  public static final String PROP_EXCLUSIONS = "exclusions";
  public static final String PROP_EMPTY_COLUMNS = "emptyColumns";
  public static final String PROP_FIELDS = "fields";
  public static final String PROP_FILTERS = "filters";
  public static final String PROP_INCLUDE_ONE_TABLE_KEYS = "includeOneTableKeys";
  public static final String PROP_INSTANCE = "instance";
  public static final String PROP_INSTANCE_NAME = "instanceName";
  public static final String PROP_LONG_DESCRIPTION = "longDescription";
  public static final String PROP_MAX_ROWS = "maxRows";
  public static final String PROP_METADATA = "metadata";
  public static final String PROP_METRICS = "metrics";
  public static final String PROP_MIN_ELEMENTS = "minElements";
  public static final String PROP_MIN_LENGTH = "minLength";
  public static final String PROP_NAME = "name";
  public static final String PROP_NUM_EXCLUDED_ROWS = "numExcludedRows";
  public static final String PROP_NUM_ROWS = "numRows";
  public static final String PROP_OPTIONAL = "optional";
  public static final String PROP_ORDERED_VARIABLE_NAMES = "orderedVariableNames";
  public static final String PROP_QUESTION = "question";
  public static final String PROP_RESULTS = "results";
  public static final String PROP_REVERSED = "reversed";
  public static final String PROP_ROW_OFFSET = "rowOffset";
  public static final String PROP_SORT_ORDER = "sortOrder";
  public static final String PROP_STATUS = "status";
  public static final String PROP_SUMMARY = "summary";
  public static final String PROP_TAGS = "tags";
  public static final String PROP_TYPE = "type";
  public static final String PROP_UNIQUE_ROWS = "uniqueRows";
  public static final String PROP_VALUE = "value";
  public static final String PROP_VALUES = "values";
  public static final String PROP_VARIABLES = "variables";

  public static final String RELPATH_AWS_CONFIGS_DIR = "aws_configs";
  public static final String RELPATH_AZURE_CONFIGS_DIR = "azure_configs";
  public static final String RELPATH_AWS_CONFIGS_FILE = "aws_configs";
  public static final String RELPATH_BATFISH = "batfish";
  public static final String RELPATH_CHECKPOINT_MANAGEMENT_DIR = "checkpoint_management";
  public static final String RELPATH_CISCO_ACI_CONFIGS_DIR = "cisco_aci_configs";
  public static final String RELPATH_CONFIGURATIONS_DIR = "configs";
  public static final String RELPATH_EDGE_BLACKLIST_FILE = "edge_blacklist";
  public static final String RELPATH_ENVIRONMENT_BGP_TABLES = "bgp";
  public static final String RELPATH_EXTERNAL_BGP_ANNOUNCEMENTS = "external_bgp_announcements.json";
  public static final String RELPATH_HOST_CONFIGS_DIR = "hosts";
  public static final String RELPATH_INPUT = "input";
  public static final String RELPATH_INTERFACE_BLACKLIST_FILE = "interface_blacklist";
  public static final String RELPATH_ISP_CONFIG_FILE = "isp_config.json";
  public static final String RELPATH_L1_TOPOLOGY_PATH = "layer1_topology.json";
  public static final String RELPATH_NODE_BLACKLIST_FILE = "node_blacklist";
  public static final String RELPATH_NODE_ROLES_PATH = "node_roles.json";
  public static final String RELPATH_REFERENCE_LIBRARY_PATH = "address_library.json";
  public static final String RELPATH_SONIC_CONFIGS_DIR = "sonic_configs";
  public static final String RELPATH_RUNTIME_DATA_FILE = "runtime_data.json";
  public static final String RELPATH_QUESTION_FILE = "question.json";

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
