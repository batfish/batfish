package org.batfish.main;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.batfish.common.BatfishLogger;
import org.batfish.common.BfConsts;
import org.batfish.common.CoordConsts;

public final class Settings {

   public final class EnvironmentSettings {

      private String _dataPlanePath;

      private String _deltaConfigurationsDir;

      private String _dumpFactsDir;

      private String _edgeBlacklistPath;

      private String _interfaceBlacklistPath;

      private String _jobLogicBloxHostnamePath;

      private String _name;

      private String _nodeBlacklistPath;

      private String _serializedTopologyPath;

      private String _trafficFactDumpDir;

      private String _workspaceName;

      public String getDataPlanePath() {
         return _dataPlanePath;
      }

      public String getDeltaConfigurationsDir() {
         return _deltaConfigurationsDir;
      }

      public String getDumpFactsDir() {
         return _dumpFactsDir;
      }

      public String getEdgeBlacklistPath() {
         return _edgeBlacklistPath;
      }

      public String getInterfaceBlacklistPath() {
         return _interfaceBlacklistPath;
      }

      public String getJobLogicBloxHostnamePath() {
         return _jobLogicBloxHostnamePath;
      }

      public String getName() {
         return _name;
      }

      public String getNodeBlacklistPath() {
         return _nodeBlacklistPath;
      }

      public String getSerializedTopologyPath() {
         return _serializedTopologyPath;
      }

      public String getTrafficFactDumpDir() {
         return _trafficFactDumpDir;
      }

      public String getWorkspaceName() {
         return _workspaceName;
      }

      public void setDataPlanePath(String path) {
         _dataPlanePath = path;
      }

      public void setDeltaConfigurationsDir(String deltaConfigurationsDir) {
         _deltaConfigurationsDir = deltaConfigurationsDir;
      }

      public void setDumpFactsDir(String path) {
         _dumpFactsDir = path;
      }

      public void setEdgeBlacklistPath(String edgeBlacklistPath) {
         _edgeBlacklistPath = edgeBlacklistPath;
      }

      public void setInterfaceBlacklistPath(String interfaceBlacklistPath) {
         _interfaceBlacklistPath = interfaceBlacklistPath;
      }

      public void setJobLogicBloxHostnamePath(String path) {
         _jobLogicBloxHostnamePath = path;
      }

      public void setName(String name) {
         _name = name;
      }

      public void setNodeBlacklistPath(String nodeBlacklistPath) {
         _nodeBlacklistPath = nodeBlacklistPath;
      }

      public void setSerializedTopologyPath(String serializedTopologyPath) {
         _serializedTopologyPath = serializedTopologyPath;
      }

      public void setTrafficFactDumpDir(String trafficFactDumpDir) {
         _trafficFactDumpDir = trafficFactDumpDir;
      }

      public void setWorkspaceName(String name) {
         _workspaceName = name;
      }

   }

   private static final String ARG_ACCEPT_NODE = "acceptnode";
   private static final String ARG_ANONYMIZE = "anonymize";
   private static final String ARG_AUTO_BASE_DIR = "autobasedir";
   private static final String ARG_BLACK_HOLE = "blackhole";
   private static final String ARG_BLACK_HOLE_PATH = "blackholepath";
   private static final String ARG_BLACKLIST_DST_IP_PATH = "blacklistdstippath";
   private static final String ARG_BLACKLIST_INTERFACE = "blint";
   private static final String ARG_BLACKLIST_NODE = "blnode";
   private static final String ARG_BUILD_PREDICATE_INFO = "bpi";
   private static final String ARG_CB_HOST = "lbhost";
   private static final String ARG_CB_PORT = "lbport";
   private static final String ARG_CONC_UNIQUE = "concunique";
   private static final String ARG_COORDINATOR_HOST = "coordinatorhost";
   private static final String ARG_COORDINATOR_POOL_PORT = "coordinatorpoolport";
   private static final String ARG_COORDINATOR_WORK_PORT = "coordinatorworkport";
   private static final String ARG_COUNT = "count";
   private static final String ARG_DATA_PLANE = "dp";
   private static final String ARG_DATA_PLANE_PATH = "dppath";
   private static final String ARG_DELETE_WORKSPACE = "deleteworkspace";
   private static final String ARG_DISABLE_Z3_SIMPLIFICATION = "nosimplify";
   private static final String ARG_DISABLED_FACTS = "disablefacts";
   private static final String ARG_DUMP_CONTROL_PLANE_FACTS = "dumpcp";
   private static final String ARG_DUMP_FACTS_DIR = "dumpdir";
   private static final String ARG_DUMP_IF = "dumpif";
   private static final String ARG_DUMP_IF_DIR = "dumpifdir";
   private static final String ARG_DUMP_INTERFACE_DESCRIPTIONS = "id";
   private static final String ARG_DUMP_INTERFACE_DESCRIPTIONS_PATH = "idpath";
   private static final String ARG_DUMP_TRAFFIC_FACTS = "dumptraffic";
   private static final String ARG_DUPLICATE_ROLE_FLOWS = "drf";
   private static final String ARG_EXIT_ON_FIRST_ERROR = "ee";
   private static final String ARG_FACTS = "facts";
   private static final String ARG_FLATTEN = "flatten";
   private static final String ARG_FLATTEN_DESTINATION = "flattendst";
   private static final String ARG_FLATTEN_ON_THE_FLY = "flattenonthefly";
   private static final String ARG_FLOW_PATH = "flowpath";
   private static final String ARG_FLOW_SINK_PATH = "flowsink";
   private static final String ARG_FLOWS = "flow";
   private static final String ARG_GEN_OSPF = "genospf";
   private static final String ARG_GENERATE_STUBS = "gs";
   private static final String ARG_GENERATE_STUBS_INPUT_ROLE = "gsinputrole";
   private static final String ARG_GENERATE_STUBS_INTERFACE_DESCRIPTION_REGEX = "gsidregex";
   private static final String ARG_GENERATE_STUBS_REMOTE_AS = "gsremoteas";
   private static final String ARG_GUI = "gui";
   private static final String ARG_HELP = "help";
   private static final String ARG_HISTOGRAM = "histogram";
   private static final String ARG_IGNORE_UNSUPPORTED = "ignoreunsupported";
   private static final String ARG_INTERFACE_MAP_PATH = "impath";
   private static final String ARG_JOBS = "jobs";
   private static final String ARG_LB_WEB_ADMIN_PORT = "lbwebadminport";
   private static final String ARG_LB_WEB_PORT = "lbwebport";
   private static final String ARG_LOG_FILE = "logfile";
   private static final String ARG_LOG_TEE = "logtee";
   private static final String ARG_LOGICDIR = "logicdir";
   private static final String ARG_MPI = "mpi";
   private static final String ARG_MPI_PATH = "mpipath";
   private static final String ARG_NO_OUTPUT = "nooutput";
   private static final String ARG_NO_SHUFFLE = "noshuffle";
   private static final String ARG_NO_TRAFFIC = "notraffic";
   private static final String ARG_NODE_ROLES_PATH = "nrpath";
   private static final String ARG_NODE_SET_PATH = "nodes";
   private static final String ARG_PRECOMPUTED_ADVERTISEMENTS_PATH = "precomputedadvertisementspath";
   private static final String ARG_PRECOMPUTED_FACTS_PATH = "precomputedfactspath";
   private static final String ARG_PRECOMPUTED_IBGP_NEIGHBORS_PATH = "precomputedibgpneighborspath";
   private static final String ARG_PRECOMPUTED_ROUTES_PATH = "precomputedroutespath";
   private static final String ARG_PRECOMPUTED_ROUTES_PATHS = "precomputedroutespaths";
   private static final String ARG_PREDHELP = "predhelp";
   private static final String ARG_PREDICATES = "predicates";
   private static final String ARG_PRINT_PARSE_TREES = "ppt";
   private static final String ARG_PRINT_SYMMETRIC_EDGES = "printsymmetricedges";
   private static final String ARG_QUERY = "query";
   private static final String ARG_QUERY_ALL = "all";
   private static final String ARG_QUESTION_PATH = "questionpath";
   private static final String ARG_REACH = "reach";
   private static final String ARG_REACH_PATH = "reachpath";
   private static final String ARG_REMOVE_FACTS = "remove";
   private static final String ARG_REVERT = "revert";
   private static final String ARG_ROLE_HEADERS = "rh";
   private static final String ARG_ROLE_NODES_PATH = "rnpath";
   private static final String ARG_ROLE_REACHABILITY_QUERY = "rr";
   private static final String ARG_ROLE_REACHABILITY_QUERY_PATH = "rrpath";
   private static final String ARG_ROLE_SET_PATH = "rspath";
   private static final String ARG_ROLE_TRANSIT_QUERY = "rt";
   private static final String ARG_ROLE_TRANSIT_QUERY_PATH = "rtpath";
   private static final String ARG_SEQUENTIAL = "sequential";
   private static final String ARG_SERIALIZE_INDEPENDENT = "si";
   private static final String ARG_SERIALIZE_INDEPENDENT_PATH = "sipath";
   private static final String ARG_SERIALIZE_TO_TEXT = "stext";
   private static final String ARG_SERIALIZE_VENDOR = "sv";
   private static final String ARG_SERIALIZE_VENDOR_PATH = "svpath";
   private static final String ARG_SERVICE_HOST = "servicehost";
   private static final String ARG_SERVICE_LOGICBLOX_HOSTNAME = "servicelbhostname";
   private static final String ARG_SERVICE_MODE = "servicemode";
   private static final String ARG_SERVICE_PORT = "serviceport";
   private static final String ARG_SERVICE_URL = "serviceurl";
   private static final String ARG_SYNTHESIZE_TOPOLOGY = "synthesizetopology";
   private static final String ARG_TEST_RIG_PATH = "testrig";
   private static final String ARG_THROW_ON_LEXER_ERROR = "throwlexer";
   private static final String ARG_THROW_ON_PARSER_ERROR = "throwparser";
   private static final String ARG_TIMESTAMP = "timestamp";
   private static final String ARG_TRACE_QUERY = "tracequery";
   private static final String ARG_UPDATE = "update";
   private static final String ARG_USE_PRECOMPUTED_FACTS = "useprecomputedfacts";
   private static final String ARG_VAR_SIZE_MAP_PATH = "vsmpath";
   private static final String ARG_WORKSPACE = "workspace";
   private static final String ARG_Z3 = "z3";
   private static final String ARG_Z3_CONCRETIZE = "conc";
   private static final String ARG_Z3_CONCRETIZER_INPUT_FILES = "concin";
   private static final String ARG_Z3_CONCRETIZER_NEGATED_INPUT_FILES = "concinneg";
   private static final String ARG_Z3_CONCRETIZER_OUTPUT_FILE = "concout";
   private static final String ARG_Z3_OUTPUT = "z3path";
   private static final String ARGNAME_ACCEPT_NODE = "node";
   private static final String ARGNAME_ANONYMIZE = "path";
   private static final String ARGNAME_AUTO_BASE_DIR = "path";
   private static final String ARGNAME_BLACK_HOLE_PATH = "path";
   private static final String ARGNAME_BLACKLIST_DST_IP = "ip";
   private static final String ARGNAME_BLACKLIST_INTERFACE = "node,interface";
   private static final String ARGNAME_BLACKLIST_NODE = "node";
   private static final String ARGNAME_BUILD_PREDICATE_INFO = "path";
   private static final String ARGNAME_COORDINATOR_HOST = "hostname";
   private static final String ARGNAME_DATA_PLANE_DIR = "path";
   private static final String ARGNAME_DUMP_FACTS_DIR = "path";
   private static final String ARGNAME_DUMP_IF_DIR = "path";
   private static final String ARGNAME_DUMP_INTERFACE_DESCRIPTIONS_PATH = "path";
   private static final String ARGNAME_FLATTEN_DESTINATION = "path";
   private static final String ARGNAME_FLOW_PATH = "path";
   private static final String ARGNAME_FLOW_SINK_PATH = "path";
   private static final String ARGNAME_GEN_OSPF = "path";
   private static final String ARGNAME_GENERATE_STUBS_INPUT_ROLE = "role";
   private static final String ARGNAME_GENERATE_STUBS_INTERFACE_DESCRIPTION_REGEX = "java-regex";
   private static final String ARGNAME_GENERATE_STUBS_REMOTE_AS = "as";
   private static final String ARGNAME_INTERFACE_MAP_PATH = "path";
   private static final String ARGNAME_LB_WEB_ADMIN_PORT = "port";
   private static final String ARGNAME_LB_WEB_PORT = "port";
   private static final String ARGNAME_LOG_FILE = "path";
   private static final String ARGNAME_LOG_LEVEL = "level";
   private static final String ARGNAME_LOGICDIR = "path";
   private static final String ARGNAME_MPI_PATH = "path";
   private static final String ARGNAME_NODE_ROLES_PATH = "path";
   private static final String ARGNAME_NODE_SET_PATH = "path";
   private static final String ARGNAME_PRECOMPUTED_ROUTES_PATH = "path";
   private static final String ARGNAME_QUESTION_NAME = "name";
   private static final String ARGNAME_QUESTION_PATH = "path";
   private static final String ARGNAME_REACH_PATH = "path";
   private static final String ARGNAME_REVERT = "branch-name";
   private static final String ARGNAME_ROLE_NODES_PATH = "path";
   private static final String ARGNAME_ROLE_REACHABILITY_QUERY_PATH = "path";
   private static final String ARGNAME_ROLE_SET_PATH = "path";
   private static final String ARGNAME_ROLE_TRANSIT_QUERY_PATH = "path";
   private static final String ARGNAME_SERIALIZE_INDEPENDENT_PATH = "path";
   private static final String ARGNAME_SERIALIZE_VENDOR_PATH = "path";
   private static final String ARGNAME_SERVICE_HOST = "hostname";
   private static final String ARGNAME_SERVICE_LOGICBLOX_HOSTNAME = "hostname";
   private static final String ARGNAME_VAR_SIZE_MAP_PATH = "path";
   private static final String ARGNAME_Z3_CONCRETIZER_INPUT_FILES = "paths";
   private static final String ARGNAME_Z3_CONCRETIZER_NEGATED_INPUT_FILES = "paths";
   private static final String ARGNAME_Z3_CONCRETIZER_OUTPUT_FILE = "path";
   private static final String ARGNAME_Z3_OUTPUT = "path";
   public static final String DEFAULT_CONNECTBLOX_ADMIN_PORT = "5519";
   public static final String DEFAULT_CONNECTBLOX_HOST = "localhost";
   public static final String DEFAULT_CONNECTBLOX_REGULAR_PORT = "5518";
   private static final String DEFAULT_DUMP_IF_DIR = "if";
   private static final String DEFAULT_DUMP_INTERFACE_DESCRIPTIONS_PATH = "interface_descriptions";
   private static final String DEFAULT_FLOW_PATH = "flows";
   private static final String DEFAULT_JOBS = Integer
         .toString(Integer.MAX_VALUE);
   private static final String DEFAULT_LB_WEB_ADMIN_PORT = "55183";
   private static final String DEFAULT_LB_WEB_PORT = "8080";
   private static final String DEFAULT_LOG_LEVEL = "debug";
   private static final List<String> DEFAULT_PREDICATES = Collections
         .singletonList("InstalledRoute");
   private static final String DEFAULT_SERIALIZE_INDEPENDENT_PATH = "serialized-independent-configs";
   private static final String DEFAULT_SERIALIZE_VENDOR_PATH = "serialized-vendor-configs";
   private static final String DEFAULT_SERVICE_PORT = BfConsts.SVC_PORT
         .toString();
   private static final String DEFAULT_SERVICE_URL = "http://0.0.0.0";
   private static final String DEFAULT_TEST_RIG_PATH = "default_test_rig";
   private static final boolean DEFAULT_Z3_SIMPLIFY = true;
   private static final String EXECUTABLE_NAME = "batfish";

   private String _acceptNode;
   private EnvironmentSettings _activeEnvironmentSettings;
   private boolean _anonymize;
   private String _anonymizeDir;
   private boolean _answer;
   private String _autoBaseDir;
   private EnvironmentSettings _baseEnvironmentSettings;
   private boolean _blackHole;
   private String _blackHolePath;
   private String _blacklistDstIpPath;
   private String _blacklistInterface;
   private String _blacklistNode;
   private List<String> _blockNames;
   private boolean _buildPredicateInfo;
   private boolean _canExecute;
   private String _cbHost;
   private int _cbPort;
   private boolean _concretize;
   private String[] _concretizerInputFilePaths;
   private String _concretizerOutputFilePath;
   private boolean _concUnique;
   private String _coordinatorHost;
   private int _coordinatorPoolPort;
   private int _coordinatorWorkPort;
   private boolean _counts;
   private boolean _createWorkspace;
   private boolean _dataPlane;
   private boolean _deleteWorkspace;
   private boolean _diffActive;
   private String _diffEnvironmentName;
   private EnvironmentSettings _diffEnvironmentSettings;
   private boolean _differentialHistory;
   private Set<String> _disabledFacts;
   private boolean _dumpControlPlaneFacts;
   private boolean _dumpIF;
   private String _dumpIFDir;
   private boolean _dumpInterfaceDescriptions;
   private String _dumpInterfaceDescriptionsPath;
   private boolean _dumpTrafficFacts;
   private boolean _duplicateRoleFlows;
   private String _environmentName;
   private boolean _exitOnFirstError;
   private boolean _facts;
   private String _failureInconsistencyQueryPath;
   private boolean _flatten;
   private String _flattenDestination;
   private boolean _flattenOnTheFly;
   private String _flattenSource;
   private String _flowPath;
   private boolean _flows;
   private String _flowSinkPath;
   private boolean _generateStubs;
   private String _generateStubsInputRole;
   private String _generateStubsInterfaceDescriptionRegex;
   private Integer _generateStubsRemoteAs;
   private boolean _genMultipath;
   private String _genOspfTopology;
   private List<String> _helpPredicates;
   private boolean _histogram;
   private boolean _history;
   private String _hsaInputDir;
   private String _hsaOutputDir;
   private boolean _ignoreUnsupported;
   private String _interfaceMapPath;
   private int _jobs;
   private boolean _keepBlocks;
   private int _lbWebAdminPort;
   private int _lbWebPort;
   private String _logFile;
   private BatfishLogger _logger;
   private String _logicDir;
   private String _logicSrcDir;
   private String _logLevel;
   private boolean _logTee;
   private String _mpiPath;
   private String[] _negatedConcretizerInputFilePaths;
   private String _nodeRolesPath;
   private String _nodeSetPath;
   private boolean _noOutput;
   private boolean _noTraffic;
   private Options _options;
   private String _outputEnvironmentName;
   private boolean _pedanticAsError;
   private boolean _pedanticRecord;
   private boolean _postDifferentialFlows;
   private boolean _postFlows;
   private String _precomputedBgpAdvertisementsPath;
   private String _precomputedFactsPath;
   private String _precomputedIbgpNeighborsPath;
   private String _precomputedRoutesPath;
   private Set<String> _precomputedRoutesPaths;
   private List<String> _predicates;
   private boolean _printParseTree;
   private boolean _printSemantics;
   private boolean _printSymmetricEdges;
   private boolean _query;
   private boolean _queryAll;
   private String _queryDumpDir;
   private String _questionName;
   private String _questionParametersPath;
   private String _questionPath;
   private boolean _reach;
   private String _reachPath;
   private boolean _redFlagAsError;
   private boolean _redFlagRecord;
   private boolean _removeBlocks;
   private boolean _removeFacts;
   private boolean _revert;
   private String _revertBranchName;
   private boolean _roleHeaders;
   private String _roleNodesPath;
   private boolean _roleReachabilityQuery;
   private String _roleReachabilityQueryPath;
   private String _roleSetPath;
   private boolean _roleTransitQuery;
   private String _roleTransitQueryPath;
   private boolean _runInServiceMode;
   private boolean _sequential;
   private boolean _serializeIndependent;
   private String _serializeIndependentPath;
   private boolean _serializeToText;
   private boolean _serializeVendor;
   private String _serializeVendorPath;
   private String _serviceHost;
   private String _serviceLogicBloxHostname;
   private int _servicePort;
   private String _serviceUrl;
   private boolean _shuffleJobs;
   private boolean _simplify;
   private boolean _synthesizeTopology;
   private String _testRigPath;
   private boolean _throwOnLexerError;
   private boolean _throwOnParserError;
   private boolean _timestamp;
   private boolean _traceQuery;
   private boolean _unimplementedAsError;
   private boolean _unimplementedRecord;
   private boolean _update;
   private boolean _usePrecomputedAdvertisements;
   private boolean _usePrecomputedFacts;
   private boolean _usePrecomputedIbgpNeighbors;
   private boolean _usePrecomputedRoutes;
   private String _varSizeMapPath;
   private boolean _writeBgpAdvertisements;
   private boolean _writeIbgpNeighbors;
   private boolean _writeRoutes;
   private boolean _z3;
   private String _z3File;

   public Settings() throws ParseException {
      this(new String[] {});
   }

   public Settings(String[] args) throws ParseException {
      _diffEnvironmentSettings = new EnvironmentSettings();
      _baseEnvironmentSettings = new EnvironmentSettings();
      _activeEnvironmentSettings = _baseEnvironmentSettings;
      initOptions();
      parseCommandLine(args);
   }

   public boolean canExecute() {
      return _canExecute;
   }

   public boolean concretizeUnique() {
      return _concUnique;
   }

   public boolean createWorkspace() {
      return _createWorkspace;
   }

   public boolean dumpInterfaceDescriptions() {
      return _dumpInterfaceDescriptions;
   }

   public boolean duplicateRoleFlows() {
      return _duplicateRoleFlows;
   }

   public boolean flattenOnTheFly() {
      return _flattenOnTheFly;
   }

   public String getAcceptNode() {
      return _acceptNode;
   }

   public EnvironmentSettings getActiveEnvironmentSettings() {
      return _activeEnvironmentSettings;
   }

   public boolean getAnonymize() {
      return _anonymize;
   }

   public String getAnonymizeDir() {
      return _anonymizeDir;
   }

   public boolean getAnswer() {
      return _answer;
   }

   public String getAutoBaseDir() {
      return _autoBaseDir;
   }

   public EnvironmentSettings getBaseEnvironmentSettings() {
      return _baseEnvironmentSettings;
   }

   public String getBlackHoleQueryPath() {
      return _blackHolePath;
   }

   public String getBlacklistDstIpPath() {
      return _blacklistDstIpPath;
   }

   public String getBlacklistInterfaceString() {
      return _blacklistInterface;
   }

   public String getBlacklistNode() {
      return _blacklistNode;
   }

   public List<String> getBlockNames() {
      return _blockNames;
   }

   public String getBranchName() {
      return _revertBranchName;
   }

   public boolean getBuildPredicateInfo() {
      return _buildPredicateInfo;
   }

   public boolean getConcretize() {
      return _concretize;
   }

   public String[] getConcretizerInputFilePaths() {
      return _concretizerInputFilePaths;
   }

   public String getConcretizerOutputFilePath() {
      return _concretizerOutputFilePath;
   }

   public String getConnectBloxHost() {
      return _cbHost;
   }

   public int getConnectBloxPort() {
      return _cbPort;
   }

   public String getCoordinatorHost() {
      return _coordinatorHost;
   }

   public int getCoordinatorPoolPort() {
      return _coordinatorPoolPort;
   }

   public int getCoordinatorWorkPort() {
      return _coordinatorWorkPort;
   }

   public boolean getCountsOnly() {
      return _counts;
   }

   public boolean getDataPlane() {
      return _dataPlane;
   }

   public boolean getDeleteWorkspace() {
      return _deleteWorkspace;
   }

   public boolean getDiffActive() {
      return _diffActive;
   }

   public String getDiffEnvironmentName() {
      return _diffEnvironmentName;
   }

   public EnvironmentSettings getDiffEnvironmentSettings() {
      return _diffEnvironmentSettings;
   }

   public boolean getDifferentialHistory() {
      return _differentialHistory;
   }

   public Set<String> getDisabledFacts() {
      return _disabledFacts;
   }

   public boolean getDumpControlPlaneFacts() {
      return _dumpControlPlaneFacts;
   }

   public String getDumpIFDir() {
      return _dumpIFDir;
   }

   public String getDumpInterfaceDescriptionsPath() {
      return _dumpInterfaceDescriptionsPath;
   }

   public boolean getDumpTrafficFacts() {
      return _dumpTrafficFacts;
   }

   public String getEnvironmentName() {
      return _environmentName;
   }

   public boolean getExitOnFirstError() {
      return _exitOnFirstError;
   }

   public boolean getFacts() {
      return _facts;
   }

   public String getFailureInconsistencyQueryPath() {
      return _failureInconsistencyQueryPath;
   }

   public boolean getFlatten() {
      return _flatten;
   }

   public String getFlattenDestination() {
      return _flattenDestination;
   }

   public String getFlattenSource() {
      return _flattenSource;
   }

   public String getFlowPath() {
      return _flowPath;
   }

   public String getFlowSinkPath() {
      return _flowSinkPath;
   }

   public boolean getGenerateMultipathInconsistencyQuery() {
      return _genMultipath;
   }

   public String getGenerateOspfTopologyPath() {
      return _genOspfTopology;
   }

   public boolean getGenerateStubs() {
      return _generateStubs;
   }

   public String getGenerateStubsInputRole() {
      return _generateStubsInputRole;
   }

   public String getGenerateStubsInterfaceDescriptionRegex() {
      return _generateStubsInterfaceDescriptionRegex;
   }

   public int getGenerateStubsRemoteAs() {
      return _generateStubsRemoteAs;
   }

   public List<String> getHelpPredicates() {
      return _helpPredicates;
   }

   public boolean getHistogram() {
      return _histogram;
   }

   public boolean getHistory() {
      return _history;
   }

   public String getHSAInputPath() {
      return _hsaInputDir;
   }

   public String getHSAOutputPath() {
      return _hsaOutputDir;
   }

   public boolean getInterfaceFailureInconsistencyBlackHoleQuery() {
      return _blackHole;
   }

   public boolean getInterfaceFailureInconsistencyReachableQuery() {
      return _reach;
   }

   public String getInterfaceMapPath() {
      return _interfaceMapPath;
   }

   public int getJobs() {
      return _jobs;
   }

   public boolean getKeepBlocks() {
      return _keepBlocks;
   }

   public int getLbWebAdminPort() {
      return _lbWebAdminPort;
   }

   public int getLbWebPort() {
      return _lbWebPort;
   }

   public String getLogFile() {
      return _logFile;
   }

   public BatfishLogger getLogger() {
      return _logger;
   }

   public String getLogicDir() {
      return _logicDir;
   }

   public String getLogicSrcDir() {
      return _logicSrcDir;
   }

   public String getLogLevel() {
      return _logLevel;
   }

   public boolean getLogTee() {
      return _logTee;
   }

   public String getMultipathInconsistencyQueryPath() {
      return _mpiPath;
   }

   public String[] getNegatedConcretizerInputFilePaths() {
      return _negatedConcretizerInputFilePaths;
   }

   public String getNodeRolesPath() {
      return _nodeRolesPath;
   }

   public String getNodeSetPath() {
      return _nodeSetPath;
   }

   public boolean getNoOutput() {
      return _noOutput;
   }

   public boolean getNoTraffic() {
      return _noTraffic;
   }

   public String getOutputEnvironmentName() {
      return _outputEnvironmentName;
   }

   public boolean getPedanticAsError() {
      return _pedanticAsError;
   }

   public boolean getPedanticRecord() {
      return _pedanticRecord;
   }

   public boolean getPostDifferentialFlows() {
      return _postDifferentialFlows;
   }

   public boolean getPostFlows() {
      return _postFlows;
   }

   public String getPrecomputedBgpAdvertisementsPath() {
      return _precomputedBgpAdvertisementsPath;
   }

   public String getPrecomputedFactsPath() {
      return _precomputedFactsPath;
   }

   public String getPrecomputedIbgpNeighborsPath() {
      return _precomputedIbgpNeighborsPath;
   }

   public String getPrecomputedRoutesPath() {
      return _precomputedRoutesPath;
   }

   public Set<String> getPrecomputedRoutesPaths() {
      return _precomputedRoutesPaths;
   }

   public List<String> getPredicates() {
      return _predicates;
   }

   public boolean getPrintSemantics() {
      return _printSemantics;
   }

   public boolean getPrintSymmetricEdgePairs() {
      return _printSymmetricEdges;
   }

   public boolean getQuery() {
      return _query;
   }

   public boolean getQueryAll() {
      return _queryAll;
   }

   public String getQueryDumpDir() {
      return _queryDumpDir;
   }

   public String getQuestionName() {
      return _questionName;
   }

   public String getQuestionParametersPath() {
      return _questionParametersPath;
   }

   public String getQuestionPath() {
      return _questionPath;
   }

   public String getReachableQueryPath() {
      return _reachPath;
   }

   public boolean getRedFlagAsError() {
      return _redFlagAsError;
   }

   public boolean getRedFlagRecord() {
      return _redFlagRecord;
   }

   public boolean getRemoveBlocks() {
      return _removeBlocks;
   }

   public boolean getRemoveFacts() {
      return _removeFacts;
   }

   public boolean getRoleHeaders() {
      return _roleHeaders;
   }

   public String getRoleNodesPath() {
      return _roleNodesPath;
   }

   public boolean getRoleReachabilityQuery() {
      return _roleReachabilityQuery;
   }

   public String getRoleReachabilityQueryPath() {
      return _roleReachabilityQueryPath;
   }

   public String getRoleSetPath() {
      return _roleSetPath;
   }

   public boolean getRoleTransitQuery() {
      return _roleTransitQuery;
   }

   public String getRoleTransitQueryPath() {
      return _roleTransitQueryPath;
   }

   public boolean getSequential() {
      return _sequential;
   }

   public boolean getSerializeIndependent() {
      return _serializeIndependent;
   }

   public String getSerializeIndependentPath() {
      return _serializeIndependentPath;
   }

   public boolean getSerializeToText() {
      return _serializeToText;
   }

   public boolean getSerializeVendor() {
      return _serializeVendor;
   }

   public String getSerializeVendorPath() {
      return _serializeVendorPath;
   }

   public String getServiceHost() {
      return _serviceHost;
   }

   public String getServiceLogicBloxHostname() {
      return _serviceLogicBloxHostname;
   }

   public int getServicePort() {
      return _servicePort;
   }

   public String getServiceUrl() {
      return _serviceUrl;
   }

   public boolean getShuffleJobs() {
      return _shuffleJobs;
   }

   public boolean getSimplify() {
      return _simplify;
   }

   public boolean getSynthesizeTopology() {
      return _synthesizeTopology;
   }

   public String getTestRigPath() {
      return _testRigPath;
   }

   public boolean getThrowOnLexerError() {
      return _throwOnLexerError;
   }

   public boolean getThrowOnParserError() {
      return _throwOnParserError;
   }

   public boolean getTimestamp() {
      return _timestamp;
   }

   public boolean getTraceQuery() {
      return _traceQuery;
   }

   public boolean getUnimplementedAsError() {
      return _unimplementedAsError;
   }

   public boolean getUnimplementedRecord() {
      return _unimplementedRecord;
   }

   public boolean getUpdate() {
      return _update;
   }

   public boolean getUsePrecomputedBgpAdvertisements() {
      return _usePrecomputedAdvertisements;
   }

   public boolean getUsePrecomputedFacts() {
      return _usePrecomputedFacts;
   }

   public boolean getUsePrecomputedIbgpNeighbors() {
      return _usePrecomputedIbgpNeighbors;
   }

   public boolean getUsePrecomputedRoutes() {
      return _usePrecomputedRoutes;
   }

   public String getVarSizeMapPath() {
      return _varSizeMapPath;
   }

   public boolean getWriteBgpAdvertisements() {
      return _writeBgpAdvertisements;
   }

   public boolean getWriteIbgpNeighbors() {
      return _writeIbgpNeighbors;
   }

   public boolean getWriteRoutes() {
      return _writeRoutes;
   }

   public boolean getZ3() {
      return _z3;
   }

   public String getZ3File() {
      return _z3File;
   }

   public boolean ignoreUnsupported() {
      return _ignoreUnsupported;
   }

   private void initOptions() {
      _options = new Options();
      _options.addOption(Option
            .builder()
            .argName("predicates")
            .hasArgs()
            .desc("list of LogicBlox predicates to query (defaults to '"
                  + DEFAULT_PREDICATES.get(0) + "')").longOpt(ARG_PREDICATES)
            .build());
      _options.addOption(Option.builder().argName("predicates").hasArgs()
            .desc("list of LogicBlox fact predicates to suppress")
            .longOpt(ARG_DISABLED_FACTS).build());
      _options.addOption(Option
            .builder()
            .argName("path")
            .hasArg()
            .desc("path to test rig directory (defaults to \""
                  + DEFAULT_TEST_RIG_PATH + "\")").longOpt(ARG_TEST_RIG_PATH)
            .build());
      _options
            .addOption(Option.builder().argName("name").hasArg()
                  .desc("name of LogicBlox workspace").longOpt(ARG_WORKSPACE)
                  .build());
      _options.addOption(Option.builder().argName("hostname").hasArg()
            .desc("hostname of ConnectBlox server for regular session")
            .longOpt(ARG_CB_HOST).build());
      _options.addOption(Option.builder().argName("port_number").hasArg()
            .desc("port of ConnectBlox server for regular session")
            .longOpt(ARG_CB_PORT).build());
      _options.addOption(Option.builder().argName(ARGNAME_LB_WEB_PORT).hasArg()
            .desc("port of lb-web server").longOpt(ARG_LB_WEB_PORT).build());
      _options.addOption(Option.builder().argName(ARGNAME_LB_WEB_ADMIN_PORT)
            .hasArg().desc("admin port lb-web server")
            .longOpt(ARG_LB_WEB_ADMIN_PORT).build());
      _options
            .addOption(Option
                  .builder()
                  .argName("predicates")
                  .optionalArg(true)
                  .hasArgs()
                  .desc("print semantics for all predicates, or for predicates supplied as optional arguments")
                  .longOpt(ARG_PREDHELP).build());
      _options.addOption(Option.builder().desc("print this message")
            .longOpt(ARG_HELP).build());
      _options.addOption(Option.builder().desc("query workspace")
            .longOpt(ARG_QUERY).build());
      _options.addOption(Option.builder()
            .desc("return predicate cardinalities instead of contents")
            .longOpt(ARG_COUNT).build());
      _options.addOption(Option.builder().desc("query ALL predicates")
            .longOpt(ARG_QUERY_ALL).build());
      _options.addOption(Option.builder()
            .desc("create workspace and add project logic")
            .longOpt(BfConsts.COMMAND_CREATE_WORKSPACE).build());
      _options.addOption(Option.builder().desc("add facts to workspace")
            .longOpt(ARG_FACTS).build());
      _options.addOption(Option.builder()
            .desc("remove facts instead of adding them")
            .longOpt(ARG_REMOVE_FACTS).build());
      _options.addOption(Option.builder().desc("display results in GUI")
            .longOpt(ARG_GUI).build());
      _options.addOption(Option.builder()
            .desc("differentially update test rig workspace")
            .longOpt(ARG_UPDATE).build());
      _options.addOption(Option.builder()
            .desc("do not add injected traffic facts").longOpt(ARG_NO_TRAFFIC)
            .build());
      _options
            .addOption(Option
                  .builder()
                  .desc("exit on first parse error (otherwise will exit on last parse error)")
                  .longOpt(ARG_EXIT_ON_FIRST_ERROR).build());
      _options.addOption(Option.builder().desc("generate z3 data plane logic")
            .longOpt(ARG_Z3).build());
      _options.addOption(Option.builder().argName(ARGNAME_Z3_OUTPUT).hasArg()
            .desc("set z3 data plane logic output file").longOpt(ARG_Z3_OUTPUT)
            .build());
      _options.addOption(Option.builder()
            .argName(ARGNAME_Z3_CONCRETIZER_INPUT_FILES).hasArgs()
            .desc("set z3 concretizer input file(s)")
            .longOpt(ARG_Z3_CONCRETIZER_INPUT_FILES).build());
      _options.addOption(Option.builder()
            .argName(ARGNAME_Z3_CONCRETIZER_NEGATED_INPUT_FILES).hasArgs()
            .desc("set z3 negated concretizer input file(s)")
            .longOpt(ARG_Z3_CONCRETIZER_NEGATED_INPUT_FILES).build());
      _options.addOption(Option.builder()
            .argName(ARGNAME_Z3_CONCRETIZER_OUTPUT_FILE).hasArg()
            .desc("set z3 concretizer output file")
            .longOpt(ARG_Z3_CONCRETIZER_OUTPUT_FILE).build());
      _options.addOption(Option.builder()
            .desc("create z3 logic to concretize data plane constraints")
            .longOpt(ARG_Z3_CONCRETIZE).build());
      _options.addOption(Option.builder()
            .desc("push concrete flows into logicblox databse")
            .longOpt(ARG_FLOWS).build());
      _options.addOption(Option.builder().argName(ARGNAME_FLOW_PATH).hasArg()
            .desc("path to concrete flows").longOpt(ARG_FLOW_PATH).build());
      _options.addOption(Option.builder().argName(ARGNAME_FLOW_SINK_PATH)
            .hasArg().desc("path to flow sinks").longOpt(ARG_FLOW_SINK_PATH)
            .build());
      _options.addOption(Option.builder()
            .desc("dump intermediate format of configurations")
            .longOpt(ARG_DUMP_IF).build());
      _options.addOption(Option.builder().argName(ARGNAME_DUMP_IF_DIR).hasArg()
            .desc("directory to dump intermediate format files")
            .longOpt(ARG_DUMP_IF_DIR).build());
      _options.addOption(Option.builder().desc("dump control plane facts")
            .longOpt(ARG_DUMP_CONTROL_PLANE_FACTS).build());
      _options.addOption(Option.builder().desc("dump traffic facts")
            .longOpt(ARG_DUMP_TRAFFIC_FACTS).build());
      _options.addOption(Option.builder().hasArg()
            .argName(ARGNAME_DUMP_FACTS_DIR)
            .desc("directory to dump LogicBlox facts")
            .longOpt(ARG_DUMP_FACTS_DIR).build());
      _options.addOption(Option.builder().hasArg().argName(ARGNAME_REVERT)
            .desc("revert test rig workspace to specified branch")
            .longOpt(ARG_REVERT).build());
      _options.addOption(Option.builder().hasArg().argName(ARGNAME_ANONYMIZE)
            .desc("created anonymized versions of configs in test rig")
            .longOpt(ARG_ANONYMIZE).build());
      _options
            .addOption(Option
                  .builder()
                  .hasArg()
                  .argName(ARGNAME_LOGICDIR)
                  .desc("set logic dir with respect to filesystem of machine running LogicBlox")
                  .longOpt(ARG_LOGICDIR).build());
      _options.addOption(Option.builder().desc("disable z3 simplification")
            .longOpt(ARG_DISABLE_Z3_SIMPLIFICATION).build());
      _options.addOption(Option.builder().desc("serialize vendor configs")
            .longOpt(ARG_SERIALIZE_VENDOR).build());
      _options.addOption(Option.builder().hasArg()
            .argName(ARGNAME_SERIALIZE_VENDOR_PATH)
            .desc("path to read or write serialized vendor configs")
            .longOpt(ARG_SERIALIZE_VENDOR_PATH).build());
      _options.addOption(Option.builder()
            .desc("serialize vendor-independent configs")
            .longOpt(ARG_SERIALIZE_INDEPENDENT).build());
      _options
            .addOption(Option
                  .builder()
                  .hasArg()
                  .argName(ARGNAME_SERIALIZE_INDEPENDENT_PATH)
                  .desc("path to read or write serialized vendor-independent configs")
                  .longOpt(ARG_SERIALIZE_INDEPENDENT_PATH).build());
      _options.addOption(Option.builder()
            .desc("compute and serialize data plane (requires logicblox)")
            .longOpt(ARG_DATA_PLANE).build());
      _options.addOption(Option.builder().hasArg()
            .argName(ARGNAME_DATA_PLANE_DIR)
            .desc("path to read or write serialized data plane")
            .longOpt(ARG_DATA_PLANE_PATH).build());
      _options.addOption(Option.builder().desc("print parse trees")
            .longOpt(ARG_PRINT_PARSE_TREES).build());
      _options.addOption(Option.builder().desc("dump interface descriptions")
            .longOpt(ARG_DUMP_INTERFACE_DESCRIPTIONS).build());
      _options.addOption(Option.builder().hasArg()
            .argName(ARGNAME_DUMP_INTERFACE_DESCRIPTIONS_PATH)
            .desc("path to read or write interface descriptions")
            .longOpt(ARG_DUMP_INTERFACE_DESCRIPTIONS_PATH).build());
      _options.addOption(Option.builder().hasArg()
            .argName(ARGNAME_NODE_SET_PATH)
            .desc("path to read or write node set").longOpt(ARG_NODE_SET_PATH)
            .build());
      _options.addOption(Option.builder().hasArg()
            .argName(ARGNAME_INTERFACE_MAP_PATH)
            .desc("path to read or write interface-number mappings")
            .longOpt(ARG_INTERFACE_MAP_PATH).build());
      _options.addOption(Option.builder().hasArg()
            .argName(ARGNAME_VAR_SIZE_MAP_PATH)
            .desc("path to read or write var-size mappings")
            .longOpt(ARG_VAR_SIZE_MAP_PATH).build());
      _options.addOption(Option.builder()
            .desc("generate multipath-inconsistency query").longOpt(ARG_MPI)
            .build());
      _options.addOption(Option.builder().hasArg().argName(ARGNAME_MPI_PATH)
            .desc("path to read or write multipath-inconsistency query")
            .longOpt(ARG_MPI_PATH).build());
      _options.addOption(Option.builder().desc("serialize to text")
            .longOpt(ARG_SERIALIZE_TO_TEXT).build());
      _options.addOption(Option.builder().desc("run in service mode")
            .longOpt(ARG_SERVICE_MODE).build());
      _options
            .addOption(Option.builder().argName("port_number").hasArg()
                  .desc("port for batfish service").longOpt(ARG_SERVICE_PORT)
                  .build());
      _options.addOption(Option.builder().argName("base_url").hasArg()
            .desc("base url for batfish service").longOpt(ARG_SERVICE_URL)
            .build());
      _options
            .addOption(Option
                  .builder()
                  .hasArg()
                  .argName(ARGNAME_BUILD_PREDICATE_INFO)
                  .desc("build predicate info (should only be called by ant build script) with provided input logic dir")
                  .longOpt(ARG_BUILD_PREDICATE_INFO).build());
      _options.addOption(Option.builder().hasArg()
            .argName(ARGNAME_BLACKLIST_INTERFACE)
            .desc("interface to blacklist (force inactive) during analysis")
            .longOpt(ARG_BLACKLIST_INTERFACE).build());
      _options
            .addOption(Option
                  .builder()
                  .hasArg()
                  .argName(ARGNAME_BLACKLIST_NODE)
                  .desc("node to blacklist (remove from configuration structures) during analysis")
                  .longOpt(ARG_BLACKLIST_NODE).build());
      _options.addOption(Option.builder().hasArg().argName(ARGNAME_ACCEPT_NODE)
            .desc("accept node for reachability query")
            .longOpt(ARG_ACCEPT_NODE).build());
      _options
            .addOption(Option
                  .builder()
                  .desc("generate interface-failure-inconsistency reachable packet query")
                  .longOpt(ARG_REACH).build());
      _options
            .addOption(Option
                  .builder()
                  .hasArg()
                  .argName(ARGNAME_REACH_PATH)
                  .desc("path to read or write interface-failure-inconsistency reachable packet query")
                  .longOpt(ARG_REACH_PATH).build());
      _options
            .addOption(Option
                  .builder()
                  .desc("generate interface-failure-inconsistency black-hole packet query")
                  .longOpt(ARG_BLACK_HOLE).build());
      _options
            .addOption(Option
                  .builder()
                  .desc("only concretize single packet (do not break up disjunctions)")
                  .longOpt(ARG_CONC_UNIQUE).build());
      _options
            .addOption(Option
                  .builder()
                  .hasArg()
                  .argName(ARGNAME_BLACK_HOLE_PATH)
                  .desc("path to read or write interface-failure-inconsistency black-hole packet query")
                  .longOpt(ARG_BLACK_HOLE_PATH).build());
      _options.addOption(Option.builder().hasArg()
            .argName(ARGNAME_BLACKLIST_DST_IP)
            .desc("destination ip to blacklist for concretizer queries")
            .longOpt(ARG_BLACKLIST_DST_IP_PATH).build());
      _options.addOption(Option.builder().hasArg()
            .argName(ARGNAME_NODE_ROLES_PATH)
            .desc("path to read or write node-role mappings")
            .longOpt(ARG_NODE_ROLES_PATH).build());
      _options.addOption(Option.builder().hasArg()
            .argName(ARGNAME_ROLE_NODES_PATH)
            .desc("path to read or write role-node mappings")
            .longOpt(ARG_ROLE_NODES_PATH).build());
      _options.addOption(Option.builder().hasArg()
            .argName(ARGNAME_ROLE_REACHABILITY_QUERY_PATH)
            .desc("path to read or write role-reachability queries")
            .longOpt(ARG_ROLE_REACHABILITY_QUERY_PATH).build());
      _options.addOption(Option.builder()
            .desc("generate role-reachability queries")
            .longOpt(ARG_ROLE_REACHABILITY_QUERY).build());
      _options.addOption(Option.builder().hasArg()
            .argName(ARGNAME_ROLE_TRANSIT_QUERY_PATH)
            .desc("path to read or write role-transit queries")
            .longOpt(ARG_ROLE_TRANSIT_QUERY_PATH).build());
      _options.addOption(Option.builder().desc("generate role-transit queries")
            .longOpt(ARG_ROLE_TRANSIT_QUERY).build());
      _options.addOption(Option.builder().hasArg()
            .argName(ARGNAME_ROLE_SET_PATH)
            .desc("path to read or write role set").longOpt(ARG_ROLE_SET_PATH)
            .build());
      _options.addOption(Option.builder()
            .desc("duplicate flows across all nodes in same role")
            .longOpt(ARG_DUPLICATE_ROLE_FLOWS).build());
      _options.addOption(Option.builder().hasArg().argName(ARGNAME_LOG_LEVEL)
            .desc("log level").longOpt(BfConsts.ARG_LOG_LEVEL).build());
      _options.addOption(Option.builder()
            .desc("header of concretized z3 output refers to role, not node")
            .longOpt(ARG_ROLE_HEADERS).build());
      _options.addOption(Option.builder()
            .desc("throw exception immediately on parser error")
            .longOpt(ARG_THROW_ON_PARSER_ERROR).build());
      _options.addOption(Option.builder()
            .desc("throw exception immediately on lexer error")
            .longOpt(ARG_THROW_ON_LEXER_ERROR).build());
      _options.addOption(Option.builder()
            .desc("flatten hierarchical juniper configuration files")
            .longOpt(ARG_FLATTEN).build());
      _options
            .addOption(Option
                  .builder()
                  .hasArg()
                  .argName(ARGNAME_FLATTEN_DESTINATION)
                  .desc("output path to test rig in which flat juniper (and all other) configurations will be placed")
                  .longOpt(ARG_FLATTEN_DESTINATION).build());
      _options
            .addOption(Option
                  .builder()
                  .desc("flatten hierarchical juniper configuration files on-the-fly (line number references will be spurious)")
                  .longOpt(ARG_FLATTEN_ON_THE_FLY).build());
      _options
            .addOption(Option
                  .builder()
                  .desc("throws "
                        + PedanticBatfishException.class.getSimpleName()
                        + " for likely harmless warnings (e.g. deviation from good configuration style), instead of emitting warning and continuing")
                  .longOpt(BfConsts.ARG_PEDANTIC_AS_ERROR).build());
      _options.addOption(Option.builder().desc("suppresses pedantic warnings")
            .longOpt(BfConsts.ARG_PEDANTIC_SUPPRESS).build());
      _options
            .addOption(Option
                  .builder()
                  .desc("throws "
                        + RedFlagBatfishException.class.getSimpleName()
                        + " on some recoverable errors (e.g. bad config lines), instead of emitting warning and attempting to recover")
                  .longOpt(BfConsts.ARG_RED_FLAG_AS_ERROR).build());
      _options.addOption(Option.builder().desc("suppresses red-flag warnings")
            .longOpt(BfConsts.ARG_RED_FLAG_SUPPRESS).build());
      _options
            .addOption(Option
                  .builder()
                  .desc("throws "
                        + UnimplementedBatfishException.class.getSimpleName()
                        + " when encountering unimplemented configuration directives, instead of emitting warning and ignoring")
                  .longOpt(BfConsts.ARG_UNIMPLEMENTED_AS_ERROR).build());
      _options.addOption(Option.builder()
            .desc("suppresses unimplemented-configuration-directive warnings")
            .longOpt(BfConsts.ARG_UNIMPLEMENTED_SUPPRESS).build());
      _options.addOption(Option.builder()
            .desc("build histogram of unimplemented features")
            .longOpt(ARG_HISTOGRAM).build());
      _options.addOption(Option.builder().desc("generate stubs")
            .longOpt(ARG_GENERATE_STUBS).build());
      _options.addOption(Option.builder().hasArg()
            .argName(ARGNAME_GENERATE_STUBS_INPUT_ROLE)
            .desc("input role for which to generate stubs")
            .longOpt(ARG_GENERATE_STUBS_INPUT_ROLE).build());
      _options
            .addOption(Option
                  .builder()
                  .hasArg()
                  .argName(ARGNAME_GENERATE_STUBS_INTERFACE_DESCRIPTION_REGEX)
                  .desc("java regex to extract hostname of generated stub from description of adjacent interface")
                  .longOpt(ARG_GENERATE_STUBS_INTERFACE_DESCRIPTION_REGEX)
                  .build());
      _options.addOption(Option.builder().hasArg()
            .argName(ARGNAME_GENERATE_STUBS_REMOTE_AS)
            .desc("autonomous system number of stubs to be generated")
            .longOpt(ARG_GENERATE_STUBS_REMOTE_AS).build());
      _options.addOption(Option.builder().hasArg().argName(ARGNAME_LOG_FILE)
            .desc("path to main log file").longOpt(ARG_LOG_FILE).build());
      _options.addOption(Option.builder().hasArg().argName(ARGNAME_GEN_OSPF)
            .desc("generate ospf configs from specified topology")
            .longOpt(ARG_GEN_OSPF).build());
      _options.addOption(Option.builder()
            .desc("print timestamps in log messages").longOpt(ARG_TIMESTAMP)
            .build());
      _options
            .addOption(Option
                  .builder()
                  .desc("ignore configuration files with unsupported format instead of crashing")
                  .longOpt(ARG_IGNORE_UNSUPPORTED).build());
      _options.addOption(Option.builder().hasArg()
            .argName(ARGNAME_AUTO_BASE_DIR)
            .desc("path to base dir for automatic i/o path selection")
            .longOpt(ARG_AUTO_BASE_DIR).build());
      _options.addOption(Option.builder().hasArg().argName("name")
            .desc("name of environment to use")
            .longOpt(BfConsts.ARG_ENVIRONMENT_NAME).build());
      _options
            .addOption(Option
                  .builder()
                  .hasArg()
                  .argName(ARGNAME_SERVICE_LOGICBLOX_HOSTNAME)
                  .desc("hostname of LogicBlox server to be used by batfish service when creating workspaces")
                  .longOpt(ARG_SERVICE_LOGICBLOX_HOSTNAME).build());
      _options.addOption(Option.builder().hasArg()
            .argName(ARGNAME_QUESTION_NAME).desc("name of question")
            .longOpt(BfConsts.ARG_QUESTION_NAME).build());
      _options.addOption(Option.builder().desc("answer provided question")
            .longOpt(BfConsts.COMMAND_ANSWER).build());
      _options.addOption(Option.builder()
            .desc("post dumped flows to logicblox")
            .longOpt(BfConsts.COMMAND_POST_FLOWS).build());
      _options.addOption(Option.builder().desc("force sequential operation")
            .longOpt(ARG_SEQUENTIAL).build());
      _options.addOption(Option.builder().argName("port_number").hasArg()
            .desc("coordinator work manager listening port")
            .longOpt(ARG_COORDINATOR_WORK_PORT).build());
      _options.addOption(Option.builder().argName("port_number").hasArg()
            .desc("coordinator pool manager listening port")
            .longOpt(ARG_COORDINATOR_POOL_PORT).build());
      _options.addOption(Option.builder().hasArg()
            .argName(ARGNAME_SERVICE_HOST)
            .desc("local hostname to report to coordinator")
            .longOpt(ARG_SERVICE_HOST).build());
      _options.addOption(Option
            .builder()
            .hasArg()
            .argName(ARGNAME_COORDINATOR_HOST)
            .desc("hostname of coordinator for registration with -"
                  + ARG_SERVICE_MODE).longOpt(ARG_COORDINATOR_HOST).build());
      _options.addOption(Option.builder().desc("do not produce output files")
            .longOpt(ARG_NO_OUTPUT).build());
      _options.addOption(Option.builder()
            .desc("print output to both logfile and standard out")
            .longOpt(ARG_LOG_TEE).build());
      _options.addOption(Option.builder().hasArg()
            .argName(ARGNAME_QUESTION_PATH).desc("path to question file")
            .longOpt(ARG_QUESTION_PATH).build());
      _options.addOption(Option.builder()
            .desc("synthesize topology from interface ip subnet information")
            .longOpt(ARG_SYNTHESIZE_TOPOLOGY).build());
      _options.addOption(Option.builder()
            .desc("write routes from LogicBlox to disk")
            .longOpt(BfConsts.COMMAND_WRITE_ROUTES).build());
      _options.addOption(Option.builder().hasArg()
            .argName(ARGNAME_PRECOMPUTED_ROUTES_PATH)
            .desc("path to precomputed routes")
            .longOpt(ARG_PRECOMPUTED_ROUTES_PATH).build());
      _options.addOption(Option.builder().hasArg().argName("paths")
            .desc("paths to precomputed routes")
            .longOpt(ARG_PRECOMPUTED_ROUTES_PATHS).build());
      _options.addOption(Option.builder().hasArg().argName("name")
            .desc("name of output environment")
            .longOpt(BfConsts.ARG_OUTPUT_ENV).build());
      _options.addOption(Option.builder()
            .desc("remove selected blocks from LogicBlox workspace")
            .longOpt(BfConsts.COMMAND_REMOVE_BLOCKS).build());
      _options.addOption(Option.builder()
            .desc("keep only selected blocks in LogicBlox workspace")
            .longOpt(BfConsts.COMMAND_KEEP_BLOCKS).build());
      _options.addOption(Option.builder().argName("blocknames").hasArgs()
            .desc("list of LogicBlox blocks to add or remove")
            .longOpt(BfConsts.ARG_BLOCK_NAMES).build());
      _options.addOption(Option.builder()
            .desc("add precomputed routes to workspace")
            .longOpt(BfConsts.ARG_USE_PRECOMPUTED_ROUTES).build());
      _options.addOption(Option.builder()
            .desc("add precomputed ibgp neighborsto workspace")
            .longOpt(BfConsts.ARG_USE_PRECOMPUTED_IBGP_NEIGHBORS).build());
      _options.addOption(Option.builder()
            .desc("add precomputed bgp advertisements to workspace")
            .longOpt(BfConsts.ARG_USE_PRECOMPUTED_ADVERTISEMENTS).build());
      _options.addOption(Option.builder().hasArg().argName("path")
            .desc("path to precomputed bgp advertisements")
            .longOpt(ARG_PRECOMPUTED_ADVERTISEMENTS_PATH).build());
      _options.addOption(Option.builder().hasArg().argName("path")
            .desc("path to precomputed ibgp neighbors")
            .longOpt(ARG_PRECOMPUTED_IBGP_NEIGHBORS_PATH).build());
      _options.addOption(Option.builder()
            .desc("write ibgp neighbors from LogicBlox to disk")
            .longOpt(BfConsts.COMMAND_WRITE_IBGP_NEIGHBORS).build());
      _options.addOption(Option.builder()
            .desc("write bgp advertisements from LogicBlox to disk")
            .longOpt(BfConsts.COMMAND_WRITE_ADVERTISEMENTS).build());
      _options.addOption(Option.builder().hasArg().argName("path")
            .desc("path to precomputed facts")
            .longOpt(ARG_PRECOMPUTED_FACTS_PATH).build());
      _options.addOption(Option.builder()
            .desc("add precomputed facts to workspace")
            .longOpt(ARG_USE_PRECOMPUTED_FACTS).build());
      _options.addOption(Option.builder().hasArg().argName("name")
            .desc("name of delta environment to use")
            .longOpt(BfConsts.ARG_DIFF_ENVIRONMENT_NAME).build());
      _options
            .addOption(Option
                  .builder()
                  .desc("post dumped differential flows to base and differential logicblox workspaces")
                  .longOpt(BfConsts.COMMAND_POST_DIFFERENTIAL_FLOWS).build());
      _options.addOption(Option.builder()
            .desc("retrieve differential flow history")
            .longOpt(BfConsts.COMMAND_GET_DIFFERENTIAL_HISTORY).build());
      _options.addOption(Option.builder().desc("retrieve flow history")
            .longOpt(BfConsts.COMMAND_GET_HISTORY).build());
      _options.addOption(Option.builder()
            .desc("get per-trace versions of relations during query")
            .longOpt(ARG_TRACE_QUERY).build());
      _options.addOption(Option.builder().desc("delete LogicBlox workspace")
            .longOpt(ARG_DELETE_WORKSPACE).build());
      _options.addOption(Option.builder()
            .desc("print topology with symmetric edges adjacent in listing")
            .longOpt(ARG_PRINT_SYMMETRIC_EDGES).build());
      _options.addOption(Option.builder().hasArg().argName("number")
            .desc("number of threads used by parallel jobs executor")
            .longOpt(ARG_JOBS).build());
      _options.addOption(Option.builder().desc("do not shuffle parallel jobs")
            .longOpt(ARG_NO_SHUFFLE).build());
      _options
            .addOption(Option
                  .builder()
                  .desc("make differential environment the active one for questions about a single environment")
                  .longOpt(BfConsts.ARG_DIFF_ACTIVE).build());
   }

   private void parseCommandLine(String[] args) throws ParseException {
      _canExecute = true;
      _runInServiceMode = false;
      _printSemantics = false;
      CommandLine line = null;
      CommandLineParser parser = new DefaultParser();

      // parse the command line arguments
      line = parser.parse(_options, args);

      _logLevel = line
            .getOptionValue(BfConsts.ARG_LOG_LEVEL, DEFAULT_LOG_LEVEL);
      _logFile = line.getOptionValue(ARG_LOG_FILE);
      if (line.hasOption(ARG_HELP)) {
         _canExecute = false;
         // automatically generate the help statement
         HelpFormatter formatter = new HelpFormatter();
         formatter.setLongOptPrefix("-");
         formatter.printHelp(EXECUTABLE_NAME, _options);
         return;
      }
      _runInServiceMode = line.hasOption(ARG_SERVICE_MODE);
      _servicePort = Integer.parseInt(line.getOptionValue(ARG_SERVICE_PORT,
            DEFAULT_SERVICE_PORT));
      _serviceUrl = line.getOptionValue(ARG_SERVICE_URL, DEFAULT_SERVICE_URL);
      _counts = line.hasOption(ARG_COUNT);
      _queryAll = line.hasOption(ARG_QUERY_ALL);
      _query = line.hasOption(ARG_QUERY);
      if (line.hasOption(ARG_PREDHELP)) {
         _printSemantics = true;
         String[] optionValues = line.getOptionValues(ARG_PREDHELP);
         if (optionValues != null) {
            _helpPredicates = Arrays.asList(optionValues);
         }
      }
      _cbHost = line.getOptionValue(ARG_CB_HOST, DEFAULT_CONNECTBLOX_HOST);
      _cbPort = Integer.parseInt(line.getOptionValue(ARG_CB_PORT,
            DEFAULT_CONNECTBLOX_REGULAR_PORT));

      _testRigPath = line.getOptionValue(ARG_TEST_RIG_PATH,
            DEFAULT_TEST_RIG_PATH);

      _baseEnvironmentSettings.setWorkspaceName(line.getOptionValue(
            ARG_WORKSPACE, null));
      _disabledFacts = new HashSet<String>();
      if (line.hasOption(ARG_DISABLED_FACTS)) {
         _disabledFacts.addAll(Arrays.asList(line
               .getOptionValues(ARG_DISABLED_FACTS)));
      }
      if (line.hasOption(ARG_PREDICATES)) {
         _predicates = Arrays.asList(line.getOptionValues(ARG_PREDICATES));
      }
      else {
         _predicates = DEFAULT_PREDICATES;
      }
      if (line.hasOption(BfConsts.ARG_BLOCK_NAMES)) {
         _blockNames = Arrays.asList(line
               .getOptionValues(BfConsts.ARG_BLOCK_NAMES));
      }
      else {
         _blockNames = Collections.<String> emptyList();
      }
      _removeFacts = line.hasOption(ARG_REMOVE_FACTS);
      _createWorkspace = line.hasOption(BfConsts.COMMAND_CREATE_WORKSPACE);
      _facts = line.hasOption(ARG_FACTS);
      _update = line.hasOption(ARG_UPDATE);
      _noTraffic = line.hasOption(ARG_NO_TRAFFIC);
      _exitOnFirstError = line.hasOption(ARG_EXIT_ON_FIRST_ERROR);
      _z3 = line.hasOption(ARG_Z3);
      if (_z3) {
         _z3File = line.getOptionValue(ARG_Z3_OUTPUT);
      }
      _concretize = line.hasOption(ARG_Z3_CONCRETIZE);
      if (_concretize) {
         _concretizerInputFilePaths = line
               .getOptionValues(ARG_Z3_CONCRETIZER_INPUT_FILES);
         _negatedConcretizerInputFilePaths = line
               .getOptionValues(ARG_Z3_CONCRETIZER_NEGATED_INPUT_FILES);
         _concretizerOutputFilePath = line
               .getOptionValue(ARG_Z3_CONCRETIZER_OUTPUT_FILE);
      }
      _flows = line.hasOption(ARG_FLOWS);
      if (_flows) {
         _flowPath = line.getOptionValue(ARG_FLOW_PATH, DEFAULT_FLOW_PATH);
      }
      _flowSinkPath = line.getOptionValue(ARG_FLOW_SINK_PATH);
      _dumpIF = line.hasOption(ARG_DUMP_IF);
      if (_dumpIF) {
         _dumpIFDir = line.getOptionValue(ARG_DUMP_IF_DIR, DEFAULT_DUMP_IF_DIR);
      }
      _dumpControlPlaneFacts = line.hasOption(ARG_DUMP_CONTROL_PLANE_FACTS);
      _dumpTrafficFacts = line.hasOption(ARG_DUMP_TRAFFIC_FACTS);
      _baseEnvironmentSettings.setDumpFactsDir(line
            .getOptionValue(ARG_DUMP_FACTS_DIR));
      _revertBranchName = line.getOptionValue(ARG_REVERT);
      _revert = (_revertBranchName != null);
      _anonymize = line.hasOption(ARG_ANONYMIZE);
      if (_anonymize) {
         _anonymizeDir = line.getOptionValue(ARG_ANONYMIZE);
      }
      _logicDir = line.getOptionValue(ARG_LOGICDIR, null);
      _simplify = DEFAULT_Z3_SIMPLIFY;
      if (line.hasOption(ARG_DISABLE_Z3_SIMPLIFICATION)) {
         _simplify = false;
      }
      _serializeVendor = line.hasOption(ARG_SERIALIZE_VENDOR);
      _serializeVendorPath = line.getOptionValue(ARG_SERIALIZE_VENDOR_PATH,
            DEFAULT_SERIALIZE_VENDOR_PATH);
      _serializeIndependent = line.hasOption(ARG_SERIALIZE_INDEPENDENT);
      _serializeIndependentPath = line.getOptionValue(
            ARG_SERIALIZE_INDEPENDENT_PATH, DEFAULT_SERIALIZE_INDEPENDENT_PATH);
      _dataPlane = line.hasOption(ARG_DATA_PLANE);
      _baseEnvironmentSettings.setDataPlanePath(line
            .getOptionValue(ARG_DATA_PLANE_PATH));
      _printParseTree = line.hasOption(ARG_PRINT_PARSE_TREES);
      _dumpInterfaceDescriptions = line
            .hasOption(ARG_DUMP_INTERFACE_DESCRIPTIONS);
      _dumpInterfaceDescriptionsPath = line.getOptionValue(
            ARG_DUMP_INTERFACE_DESCRIPTIONS_PATH,
            DEFAULT_DUMP_INTERFACE_DESCRIPTIONS_PATH);
      _nodeSetPath = line.getOptionValue(ARG_NODE_SET_PATH);
      _interfaceMapPath = line.getOptionValue(ARG_INTERFACE_MAP_PATH);
      _varSizeMapPath = line.getOptionValue(ARG_VAR_SIZE_MAP_PATH);
      _genMultipath = line.hasOption(ARG_MPI);
      _mpiPath = line.getOptionValue(ARG_MPI_PATH);
      _serializeToText = line.hasOption(ARG_SERIALIZE_TO_TEXT);
      _lbWebPort = Integer.parseInt(line.getOptionValue(ARG_LB_WEB_PORT,
            DEFAULT_LB_WEB_PORT));
      _lbWebAdminPort = Integer.parseInt(line.getOptionValue(
            ARG_LB_WEB_ADMIN_PORT, DEFAULT_LB_WEB_ADMIN_PORT));
      _buildPredicateInfo = line.hasOption(ARG_BUILD_PREDICATE_INFO);
      if (_buildPredicateInfo) {
         _logicSrcDir = line.getOptionValue(ARG_BUILD_PREDICATE_INFO);
      }
      _blacklistInterface = line.getOptionValue(ARG_BLACKLIST_INTERFACE);
      _blacklistNode = line.getOptionValue(ARG_BLACKLIST_NODE);
      _reach = line.hasOption(ARG_REACH);
      _reachPath = line.getOptionValue(ARG_REACH_PATH);
      _blackHole = line.hasOption(ARG_BLACK_HOLE);
      _blackHolePath = line.getOptionValue(ARG_BLACK_HOLE_PATH);
      _blacklistDstIpPath = line.getOptionValue(ARG_BLACKLIST_DST_IP_PATH);
      _concUnique = line.hasOption(ARG_CONC_UNIQUE);
      _acceptNode = line.getOptionValue(ARG_ACCEPT_NODE);
      _nodeRolesPath = line.getOptionValue(ARG_NODE_ROLES_PATH);
      _roleNodesPath = line.getOptionValue(ARG_ROLE_NODES_PATH);
      _roleReachabilityQueryPath = line
            .getOptionValue(ARG_ROLE_REACHABILITY_QUERY_PATH);
      _roleReachabilityQuery = line.hasOption(ARG_ROLE_REACHABILITY_QUERY);
      _roleTransitQueryPath = line.getOptionValue(ARG_ROLE_TRANSIT_QUERY_PATH);
      _roleTransitQuery = line.hasOption(ARG_ROLE_TRANSIT_QUERY);
      _roleSetPath = line.getOptionValue(ARG_ROLE_SET_PATH);
      _duplicateRoleFlows = line.hasOption(ARG_DUPLICATE_ROLE_FLOWS);
      _roleHeaders = line.hasOption(ARG_ROLE_HEADERS);
      _throwOnParserError = line.hasOption(ARG_THROW_ON_PARSER_ERROR);
      _throwOnLexerError = line.hasOption(ARG_THROW_ON_LEXER_ERROR);
      _flatten = line.hasOption(ARG_FLATTEN);
      _flattenDestination = line.getOptionValue(ARG_FLATTEN_DESTINATION);
      _flattenOnTheFly = line.hasOption(ARG_FLATTEN_ON_THE_FLY);
      _pedanticAsError = line.hasOption(BfConsts.ARG_PEDANTIC_AS_ERROR);
      _pedanticRecord = !line.hasOption(BfConsts.ARG_PEDANTIC_SUPPRESS);
      _redFlagAsError = line.hasOption(BfConsts.ARG_RED_FLAG_AS_ERROR);
      _redFlagRecord = !line.hasOption(BfConsts.ARG_RED_FLAG_SUPPRESS);
      _unimplementedAsError = line
            .hasOption(BfConsts.ARG_UNIMPLEMENTED_AS_ERROR);
      _unimplementedRecord = !line
            .hasOption(BfConsts.ARG_UNIMPLEMENTED_SUPPRESS);
      _histogram = line.hasOption(ARG_HISTOGRAM);
      _generateStubs = line.hasOption(ARG_GENERATE_STUBS);
      _generateStubsInputRole = line
            .getOptionValue(ARG_GENERATE_STUBS_INPUT_ROLE);
      _generateStubsInterfaceDescriptionRegex = line
            .getOptionValue(ARG_GENERATE_STUBS_INTERFACE_DESCRIPTION_REGEX);
      if (line.hasOption(ARG_GENERATE_STUBS_REMOTE_AS)) {
         _generateStubsRemoteAs = Integer.parseInt(line
               .getOptionValue(ARG_GENERATE_STUBS_REMOTE_AS));
      }
      _genOspfTopology = line.getOptionValue(ARG_GEN_OSPF);
      _timestamp = line.hasOption(ARG_TIMESTAMP);
      _ignoreUnsupported = line.hasOption(ARG_IGNORE_UNSUPPORTED);
      _autoBaseDir = line.getOptionValue(ARG_AUTO_BASE_DIR);
      _environmentName = line.getOptionValue(BfConsts.ARG_ENVIRONMENT_NAME);
      _questionName = line.getOptionValue(BfConsts.ARG_QUESTION_NAME);
      _answer = line.hasOption(BfConsts.COMMAND_ANSWER);
      _postFlows = line.hasOption(BfConsts.COMMAND_POST_FLOWS);
      _sequential = line.hasOption(ARG_SEQUENTIAL);
      _coordinatorHost = line.getOptionValue(ARG_COORDINATOR_HOST);
      _coordinatorPoolPort = Integer.parseInt(line.getOptionValue(
            ARG_COORDINATOR_POOL_PORT, CoordConsts.SVC_POOL_PORT.toString()));
      _coordinatorWorkPort = Integer.parseInt(line.getOptionValue(
            ARG_COORDINATOR_WORK_PORT, CoordConsts.SVC_WORK_PORT.toString()));
      _serviceHost = line.getOptionValue(ARG_SERVICE_HOST);
      // set service logicblox hostname to service hostname unless set
      // explicitly
      _serviceLogicBloxHostname = line.getOptionValue(
            ARG_SERVICE_LOGICBLOX_HOSTNAME, _serviceHost);
      _noOutput = line.hasOption(ARG_NO_OUTPUT);
      _logTee = line.hasOption(ARG_LOG_TEE);
      _questionPath = line.getOptionValue(ARG_QUESTION_PATH);
      _synthesizeTopology = line.hasOption(ARG_SYNTHESIZE_TOPOLOGY);
      _writeRoutes = line.hasOption(BfConsts.COMMAND_WRITE_ROUTES);
      String[] precomputedRoutesPathsAsArray = line
            .getOptionValues(ARG_PRECOMPUTED_ROUTES_PATHS);
      if (precomputedRoutesPathsAsArray != null) {
         _precomputedRoutesPaths = new TreeSet<String>();
         _precomputedRoutesPaths.addAll(Arrays
               .asList(precomputedRoutesPathsAsArray));
      }
      _precomputedRoutesPath = line.getOptionValue(ARG_PRECOMPUTED_ROUTES_PATH);
      _precomputedBgpAdvertisementsPath = line
            .getOptionValue(ARG_PRECOMPUTED_ADVERTISEMENTS_PATH);
      _precomputedIbgpNeighborsPath = line
            .getOptionValue(ARG_PRECOMPUTED_IBGP_NEIGHBORS_PATH);
      _outputEnvironmentName = line.getOptionValue(BfConsts.ARG_OUTPUT_ENV);
      _removeBlocks = line.hasOption(BfConsts.COMMAND_REMOVE_BLOCKS);
      _keepBlocks = line.hasOption(BfConsts.COMMAND_KEEP_BLOCKS);
      _usePrecomputedRoutes = line
            .hasOption(BfConsts.ARG_USE_PRECOMPUTED_ROUTES);
      _usePrecomputedAdvertisements = line
            .hasOption(BfConsts.ARG_USE_PRECOMPUTED_ADVERTISEMENTS);
      _writeBgpAdvertisements = line
            .hasOption(BfConsts.COMMAND_WRITE_ADVERTISEMENTS);
      _writeIbgpNeighbors = line
            .hasOption(BfConsts.COMMAND_WRITE_IBGP_NEIGHBORS);
      _usePrecomputedIbgpNeighbors = line
            .hasOption(BfConsts.ARG_USE_PRECOMPUTED_IBGP_NEIGHBORS);
      _usePrecomputedAdvertisements = line
            .hasOption(BfConsts.ARG_USE_PRECOMPUTED_ADVERTISEMENTS);
      _usePrecomputedFacts = line.hasOption(ARG_USE_PRECOMPUTED_FACTS);
      _precomputedFactsPath = line.getOptionValue(ARG_PRECOMPUTED_FACTS_PATH);
      _diffEnvironmentName = line
            .getOptionValue(BfConsts.ARG_DIFF_ENVIRONMENT_NAME);
      _postDifferentialFlows = line
            .hasOption(BfConsts.COMMAND_POST_DIFFERENTIAL_FLOWS);
      _differentialHistory = line
            .hasOption(BfConsts.COMMAND_GET_DIFFERENTIAL_HISTORY);
      _history = line.hasOption(BfConsts.COMMAND_GET_HISTORY);
      _traceQuery = line.hasOption(ARG_TRACE_QUERY);
      _deleteWorkspace = line.hasOption(ARG_DELETE_WORKSPACE);
      _printSymmetricEdges = line.hasOption(ARG_PRINT_SYMMETRIC_EDGES);
      String jobsStr = line.getOptionValue(ARG_JOBS, DEFAULT_JOBS);
      _jobs = Integer.parseInt(jobsStr);
      _shuffleJobs = !line.hasOption(ARG_NO_SHUFFLE);
      _diffActive = line.hasOption(BfConsts.ARG_DIFF_ACTIVE);
   }

   public boolean printParseTree() {
      return _printParseTree;
   }

   public boolean revert() {
      return _revert;
   }

   public boolean runInServiceMode() {
      return _runInServiceMode;
   }

   public void setActiveEnvironmentSettings(EnvironmentSettings envSettings) {
      _activeEnvironmentSettings = envSettings;
   }

   public void setConnectBloxHost(String hostname) {
      _cbHost = hostname;
   }

   public void setDiffEnvironmentName(String diffEnvironmentName) {
      _diffEnvironmentName = diffEnvironmentName;
   }

   public void setDifferentialHistory(boolean differentialHistory) {
      _differentialHistory = differentialHistory;
   }

   public void setEnvironmentName(String envName) {
      _environmentName = envName;
   }

   public void setFailureInconsistencyQueryPath(
         String failureInconsistencyQueryPath) {
      _failureInconsistencyQueryPath = failureInconsistencyQueryPath;
   }

   public void setHistory(boolean history) {
      _history = history;
   }

   public void setLogger(BatfishLogger logger) {
      _logger = logger;
   }

   public void setLogicDir(String logicDir) {
      _logicDir = logicDir;
   }

   public void setMultipathInconsistencyQueryPath(String path) {
      _mpiPath = path;
   }

   public void setNodeSetPath(String nodeSetPath) {
      _nodeSetPath = nodeSetPath;
   }

   public void setPostDifferentialFlows(boolean postDifferentialFlows) {
      _postDifferentialFlows = postDifferentialFlows;
   }

   public void setPostFlows(boolean postFlows) {
      _postFlows = postFlows;
   }

   public void setPrecomputedRoutesPath(String writeRoutesPath) {
      _precomputedRoutesPath = writeRoutesPath;
   }

   public void setQueryDumpDir(String path) {
      _queryDumpDir = path;
   }

   public void setQuestionParametersPath(String questionParametersPath) {
      _questionParametersPath = questionParametersPath;
   }

   public void setQuestionPath(String questionPath) {
      _questionPath = questionPath;
   }

   public void setSerializeIndependentPath(String path) {
      _serializeIndependentPath = path;
   }

   public void setSerializeVendorPath(String path) {
      _serializeVendorPath = path;
   }

   public void setServiceLogicBloxHostname(String hostname) {
      _serviceLogicBloxHostname = hostname;
   }

   public void setTestRigPath(String path) {
      _testRigPath = path;
   }

   public void setZ3DataPlaneFile(String path) {
      _z3File = path;
   }

}
