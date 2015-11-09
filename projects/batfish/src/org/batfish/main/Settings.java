package org.batfish.main;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
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
import org.apache.commons.configuration.FileConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.batfish.common.BatfishLogger;
import org.batfish.common.BfConsts;
import org.batfish.common.CoordConsts;

public final class Settings {

   public final class EnvironmentSettings {

      private String _controlPlaneFactsDir;

      private String _dataPlanePath;

      private String _deltaConfigurationsDir;

      private String _edgeBlacklistPath;

      private String _interfaceBlacklistPath;

      private String _jobLogicBloxHostnamePath;

      private String _name;

      private String _nodeBlacklistPath;

      private String _nxtnetDataPlaneInputFile;

      private String _nxtnetDataPlaneOutputDir;

      private String _nxtnetTrafficInputFile;

      private String _nxtnetTrafficOutputDir;

      private String _serializedTopologyPath;

      private String _trafficFactsDir;

      private String _workspaceName;

      public String getControlPlaneFactsDir() {
         return _controlPlaneFactsDir;
      }

      public String getDataPlanePath() {
         return _dataPlanePath;
      }

      public String getDeltaConfigurationsDir() {
         return _deltaConfigurationsDir;
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

      public String getNxtnetDataPlaneInputFile() {
         return _nxtnetDataPlaneInputFile;
      }

      public String getNxtnetDataPlaneOutputDir() {
         return _nxtnetDataPlaneOutputDir;
      }

      public String getNxtnetTrafficInputFile() {
         return _nxtnetTrafficInputFile;
      }

      public String getNxtnetTrafficOutputDir() {
         return _nxtnetTrafficOutputDir;
      }

      public String getSerializedTopologyPath() {
         return _serializedTopologyPath;
      }

      public String getTrafficFactsDir() {
         return _trafficFactsDir;
      }

      public String getWorkspaceName() {
         return _workspaceName;
      }

      public void setControlPlaneFactsDir(String path) {
         _controlPlaneFactsDir = path;
      }

      public void setDataPlanePath(String path) {
         _dataPlanePath = path;
      }

      public void setDeltaConfigurationsDir(String deltaConfigurationsDir) {
         _deltaConfigurationsDir = deltaConfigurationsDir;
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

      public void setNxtnetDataPlaneInputFile(String nxtnetDataPlaneInputFile) {
         _nxtnetDataPlaneInputFile = nxtnetDataPlaneInputFile;
      }

      public void setNxtnetDataPlaneOutputDir(String nxtnetDataPlaneOutputDir) {
         _nxtnetDataPlaneOutputDir = nxtnetDataPlaneOutputDir;
      }

      public void setNxtnetTrafficInputFile(String nxtnetTrafficInputFile) {
         _nxtnetTrafficInputFile = nxtnetTrafficInputFile;
      }

      public void setNxtnetTrafficOutputDir(String nxtnetTrafficOutputDir) {
         _nxtnetTrafficOutputDir = nxtnetTrafficOutputDir;
      }

      public void setSerializedTopologyPath(String serializedTopologyPath) {
         _serializedTopologyPath = serializedTopologyPath;
      }

      public void setTrafficFactDumpDir(String trafficFactDumpDir) {
         _trafficFactsDir = trafficFactDumpDir;
      }

      public void setWorkspaceName(String name) {
         _workspaceName = name;
      }

   }

   private static final String ARG_ANONYMIZE = "anonymize";
   private static final String ARG_AUTO_BASE_DIR = "autobasedir";
   private static final String ARG_BUILD_PREDICATE_INFO = "bpi";
   private static final String ARG_COORDINATOR_HOST = "coordinatorhost";
   private static final String ARG_COORDINATOR_POOL_PORT = "coordinatorpoolport";
   private static final String ARG_COORDINATOR_USE_SSL = "coordinator.UseSsl";
   private static final String ARG_COORDINATOR_WORK_PORT = "coordinatorworkport";
   private static final String ARG_DATA_PLANE = "dp";
   private static final String ARG_DIFF_QUESTION = "diffquestion";
   private static final String ARG_DISABLE_Z3_SIMPLIFICATION = "nosimplify";
   private static final String ARG_DUPLICATE_ROLE_FLOWS = "drf";
   private static final String ARG_EXIT_ON_FIRST_ERROR = "ee";
   private static final String ARG_FLATTEN = "flatten";
   private static final String ARG_FLATTEN_DESTINATION = "flattendst";
   private static final String ARG_FLATTEN_ON_THE_FLY = "flattenonthefly";
   private static final String ARG_GEN_OSPF = "genospf";
   private static final String ARG_GENERATE_STUBS = "gs";
   private static final String ARG_GENERATE_STUBS_INPUT_ROLE = "gsinputrole";
   private static final String ARG_GENERATE_STUBS_INTERFACE_DESCRIPTION_REGEX = "gsidregex";
   private static final String ARG_GENERATE_STUBS_REMOTE_AS = "gsremoteas";
   private static final String ARG_HELP = "help";
   private static final String ARG_HISTOGRAM = "histogram";
   private static final String ARG_IGNORE_UNSUPPORTED = "ignoreunsupported";
   private static final String ARG_JOBS = "jobs";
   private static final String ARG_LOG_FILE = "logfile";
   private static final String ARG_LOG_TEE = "logtee";
   private static final String ARG_NO_OUTPUT = "nooutput";
   private static final String ARG_NO_SHUFFLE = "noshuffle";
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
   private static final String ARG_ROLE_NODES_PATH = "rnpath";
   private static final String ARG_ROLE_SET_PATH = "rspath";
   private static final String ARG_ROLE_TRANSIT_QUERY = "rt";
   private static final String ARG_ROLE_TRANSIT_QUERY_PATH = "rtpath";
   private static final String ARG_SEQUENTIAL = "sequential";
   private static final String ARG_SERIALIZE_INDEPENDENT = "si";
   private static final String ARG_SERIALIZE_TO_TEXT = "stext";
   private static final String ARG_SERIALIZE_VENDOR = "sv";
   private static final String ARG_SERVICE_HOST = "servicehost";
   private static final String ARG_SERVICE_MODE = "servicemode";
   private static final String ARG_SERVICE_PORT = "serviceport";
   private static final String ARG_SYNTHESIZE_TOPOLOGY = "synthesizetopology";
   private static final String ARG_THROW_ON_LEXER_ERROR = "throwlexer";
   private static final String ARG_THROW_ON_PARSER_ERROR = "throwparser";
   private static final String ARG_TIMESTAMP = "timestamp";
   private static final String ARG_TRACE_QUERY = "tracequery";
   private static final String ARG_TRUST_ALL_SSL_CERTS = "batfish.TrustAllSslCerts"; // not
                                                                                     // wired
                                                                                     // to
                                                                                     // command
                                                                                     // line
   private static final String ARG_USE_PRECOMPUTED_FACTS = "useprecomputedfacts";
   private static final String ARG_WORKSPACE = "workspace";
   private static final String ARGNAME_ANONYMIZE = "path";
   private static final String ARGNAME_AUTO_BASE_DIR = "path";
   private static final String ARGNAME_BUILD_PREDICATE_INFO = "path";
   private static final String ARGNAME_COORDINATOR_HOST = "hostname";
   private static final String ARGNAME_FLATTEN_DESTINATION = "path";
   private static final String ARGNAME_GEN_OSPF = "path";
   private static final String ARGNAME_GENERATE_STUBS_INPUT_ROLE = "role";
   private static final String ARGNAME_GENERATE_STUBS_INTERFACE_DESCRIPTION_REGEX = "java-regex";
   private static final String ARGNAME_GENERATE_STUBS_REMOTE_AS = "as";
   private static final String ARGNAME_LOG_FILE = "path";
   private static final String ARGNAME_LOG_LEVEL = "level";
   private static final String ARGNAME_NODE_ROLES_PATH = "path";
   private static final String ARGNAME_NODE_SET_PATH = "path";
   private static final String ARGNAME_PRECOMPUTED_ROUTES_PATH = "path";
   private static final String ARGNAME_QUESTION_NAME = "name";
   private static final String ARGNAME_ROLE_NODES_PATH = "path";
   private static final String ARGNAME_ROLE_SET_PATH = "path";
   private static final String ARGNAME_ROLE_TRANSIT_QUERY_PATH = "path";
   private static final String ARGNAME_SERVICE_HOST = "hostname";
   private static final String DEFAULT_JOBS = Integer
         .toString(Integer.MAX_VALUE);
   private static final String DEFAULT_LOG_LEVEL = "debug";
   private static final List<String> DEFAULT_PREDICATES = Collections
         .singletonList("InstalledRoute");
   private static final String DEFAULT_SERVICE_PORT = BfConsts.SVC_PORT
         .toString();
   private static final boolean DEFAULT_Z3_SIMPLIFY = true;
   private static final String EXECUTABLE_NAME = "batfish";

   private EnvironmentSettings _activeEnvironmentSettings;
   private boolean _anonymize;
   private String _anonymizeDir;
   private boolean _answer;
   private String _autoBaseDir;
   private EnvironmentSettings _baseEnvironmentSettings;
   private List<String> _blockNames;
   private boolean _buildPredicateInfo;
   private boolean _canExecute;
   private FileConfiguration _config;
   private String _coordinatorHost;
   private int _coordinatorPoolPort;
   private boolean _coordinatorUseSsl;
   private int _coordinatorWorkPort;
   private boolean _dataPlane;
   private boolean _diffActive;
   private String _diffEnvironmentName;
   private EnvironmentSettings _diffEnvironmentSettings;
   private boolean _diffQuestion;
   private boolean _dumpControlPlaneFacts;
   private boolean _duplicateRoleFlows;
   private String _environmentName;
   private boolean _exitOnFirstError;
   private boolean _flatten;
   private String _flattenDestination;
   private boolean _flattenOnTheFly;
   private boolean _generateStubs;
   private String _generateStubsInputRole;
   private String _generateStubsInterfaceDescriptionRegex;
   private Integer _generateStubsRemoteAs;
   private String _genOspfTopology;
   private List<String> _helpPredicates;
   private boolean _histogram;
   private boolean _history;
   private boolean _ignoreUnsupported;
   private int _jobs;
   private boolean _keepBlocks;
   private String _logFile;
   private BatfishLogger _logger;
   private String _logicDir;
   private String _logicSrcDir;
   private String _logLevel;
   private boolean _logTee;
   private String _nodeRolesPath;
   private String _nodeSetPath;
   private boolean _noOutput;
   private boolean _nxtnetDataPlane;
   private boolean _nxtnetTraffic;
   private Options _options;
   private String _outputEnvironmentName;
   private boolean _pedanticAsError;
   private boolean _pedanticRecord;
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
   private String _questionName;
   private String _questionParametersPath;
   private String _questionPath;
   private boolean _redFlagAsError;
   private boolean _redFlagRecord;
   private boolean _removeBlocks;
   private String _roleNodesPath;
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
   private int _servicePort;
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
   private boolean _usePrecomputedAdvertisements;
   private boolean _usePrecomputedFacts;
   private boolean _usePrecomputedIbgpNeighbors;
   private boolean _usePrecomputedRoutes;
   private boolean _writeBgpAdvertisements;
   private boolean _writeIbgpNeighbors;
   private boolean _writeRoutes;
   private boolean _z3;
   private String _z3File;

   public Settings() throws Exception {
      this(new String[] {});
   }

   public Settings(String[] args) throws Exception {
      _diffEnvironmentSettings = new EnvironmentSettings();
      _baseEnvironmentSettings = new EnvironmentSettings();
      _activeEnvironmentSettings = _baseEnvironmentSettings;

      _config = new PropertiesConfiguration();
      File configFile = org.batfish.common.Util
            .getConfigProperties(org.batfish.config.ConfigurationLocator.class);
      _config.setFile(configFile);
      _config.load();

      initOptions();
      parseCommandLine(args);
   }

   public boolean canExecute() {
      return _canExecute;
   }

   public boolean duplicateRoleFlows() {
      return _duplicateRoleFlows;
   }

   public boolean flattenOnTheFly() {
      return _flattenOnTheFly;
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

   public List<String> getBlockNames() {
      return _blockNames;
   }

   public boolean getBuildPredicateInfo() {
      return _buildPredicateInfo;
   }

   public String getCoordinatorHost() {
      return _coordinatorHost;
   }

   public int getCoordinatorPoolPort() {
      return _coordinatorPoolPort;
   }

   public boolean getCoordinatorUseSsl() {
      return _coordinatorUseSsl;
   }

   public int getCoordinatorWorkPort() {
      return _coordinatorWorkPort;
   }

   public boolean getDataPlane() {
      return _dataPlane;
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

   public boolean getDiffQuestion() {
      return _diffQuestion;
   }

   public boolean getDumpControlPlaneFacts() {
      return _dumpControlPlaneFacts;
   }

   public String getEnvironmentName() {
      return _environmentName;
   }

   public boolean getExitOnFirstError() {
      return _exitOnFirstError;
   }

   public boolean getFlatten() {
      return _flatten;
   }

   public String getFlattenDestination() {
      return _flattenDestination;
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

   public int getJobs() {
      return _jobs;
   }

   public boolean getKeepBlocks() {
      return _keepBlocks;
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

   public String getNodeRolesPath() {
      return _nodeRolesPath;
   }

   public String getNodeSetPath() {
      return _nodeSetPath;
   }

   public boolean getNoOutput() {
      return _noOutput;
   }

   public boolean getNxtnetDataPlane() {
      return _nxtnetDataPlane;
   }

   public boolean getNxtnetTraffic() {
      return _nxtnetTraffic;
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

   public String getQuestionName() {
      return _questionName;
   }

   public String getQuestionParametersPath() {
      return _questionParametersPath;
   }

   public String getQuestionPath() {
      return _questionPath;
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

   public String getRoleNodesPath() {
      return _roleNodesPath;
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

   public int getServicePort() {
      return _servicePort;
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

   public boolean getTrustAllSslCerts() {
      return _config.getBoolean(ARG_TRUST_ALL_SSL_CERTS);
   }

   public boolean getUnimplementedAsError() {
      return _unimplementedAsError;
   }

   public boolean getUnimplementedRecord() {
      return _unimplementedRecord;
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
      _options.addOption(Option.builder().desc("query ALL predicates")
            .longOpt(ARG_QUERY_ALL).build());
      _options
            .addOption(Option
                  .builder()
                  .desc("exit on first parse error (otherwise will exit on last parse error)")
                  .longOpt(ARG_EXIT_ON_FIRST_ERROR).build());
      _options.addOption(Option.builder().desc("write control plane facts")
            .longOpt(BfConsts.COMMAND_WRITE_CP_FACTS).build());
      _options.addOption(Option.builder().hasArg().argName(ARGNAME_ANONYMIZE)
            .desc("created anonymized versions of configs in test rig")
            .longOpt(ARG_ANONYMIZE).build());
      _options.addOption(Option.builder().desc("disable z3 simplification")
            .longOpt(ARG_DISABLE_Z3_SIMPLIFICATION).build());
      _options.addOption(Option.builder().desc("serialize vendor configs")
            .longOpt(ARG_SERIALIZE_VENDOR).build());
      _options.addOption(Option.builder()
            .desc("serialize vendor-independent configs")
            .longOpt(ARG_SERIALIZE_INDEPENDENT).build());
      _options.addOption(Option.builder()
            .desc("compute and serialize data plane").longOpt(ARG_DATA_PLANE)
            .build());
      _options.addOption(Option.builder().desc("print parse trees")
            .longOpt(ARG_PRINT_PARSE_TREES).build());
      _options.addOption(Option.builder().hasArg()
            .argName(ARGNAME_NODE_SET_PATH)
            .desc("path to read or write node set").longOpt(ARG_NODE_SET_PATH)
            .build());
      _options.addOption(Option.builder().desc("serialize to text")
            .longOpt(ARG_SERIALIZE_TO_TEXT).build());
      _options.addOption(Option.builder().desc("run in service mode")
            .longOpt(ARG_SERVICE_MODE).build());
      _options
            .addOption(Option.builder().argName("port_number").hasArg()
                  .desc("port for batfish service").longOpt(ARG_SERVICE_PORT)
                  .build());
      _options
            .addOption(Option
                  .builder()
                  .hasArg()
                  .argName(ARGNAME_BUILD_PREDICATE_INFO)
                  .desc("build predicate info (should only be called by ant build script) with provided input logic dir")
                  .longOpt(ARG_BUILD_PREDICATE_INFO).build());
      _options.addOption(Option.builder().hasArg()
            .argName(ARGNAME_NODE_ROLES_PATH)
            .desc("path to read or write node-role mappings")
            .longOpt(ARG_NODE_ROLES_PATH).build());
      _options.addOption(Option.builder().hasArg()
            .argName(ARGNAME_ROLE_NODES_PATH)
            .desc("path to read or write role-node mappings")
            .longOpt(ARG_ROLE_NODES_PATH).build());
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
      _options.addOption(Option.builder().hasArg()
            .argName(ARGNAME_QUESTION_NAME).desc("name of question")
            .longOpt(BfConsts.ARG_QUESTION_NAME).build());
      _options.addOption(Option.builder().desc("answer provided question")
            .longOpt(BfConsts.COMMAND_ANSWER).build());
      _options.addOption(Option.builder().desc("force sequential operation")
            .longOpt(ARG_SEQUENTIAL).build());
      _options.addOption(Option.builder().argName("port_number").hasArg()
            .desc("coordinator work manager listening port")
            .longOpt(ARG_COORDINATOR_WORK_PORT).build());
      _options.addOption(Option.builder().argName("port_number").hasArg()
            .desc("coordinator pool manager listening port")
            .longOpt(ARG_COORDINATOR_POOL_PORT).build());
      _options.addOption(Option.builder().argName("coordinator_use_ssl")
            .hasArg().desc("whether coordinator uses ssl")
            .longOpt(ARG_COORDINATOR_USE_SSL).build());
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
      _options.addOption(Option.builder().desc("retrieve flow history")
            .longOpt(BfConsts.COMMAND_GET_HISTORY).build());
      _options.addOption(Option.builder()
            .desc("get per-trace versions of relations during query")
            .longOpt(ARG_TRACE_QUERY).build());
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
      _options.addOption(Option.builder()
            .desc("compute data plane with nxtnet")
            .longOpt(BfConsts.COMMAND_NXTNET_DATA_PLANE).build());
      _options
            .addOption(Option
                  .builder()
                  .desc("compute traffic information from provided flows with nxtnet")
                  .longOpt(BfConsts.COMMAND_NXTNET_TRAFFIC).build());
      _options
            .addOption(Option
                  .builder()
                  .desc("force treatment of question as differential (to be used when not answering question)")
                  .longOpt(ARG_DIFF_QUESTION).build());
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
      _queryAll = line.hasOption(ARG_QUERY_ALL);
      _query = line.hasOption(ARG_QUERY);
      if (line.hasOption(ARG_PREDHELP)) {
         _printSemantics = true;
         String[] optionValues = line.getOptionValues(ARG_PREDHELP);
         if (optionValues != null) {
            _helpPredicates = Arrays.asList(optionValues);
         }
      }
      _baseEnvironmentSettings.setWorkspaceName(line.getOptionValue(
            ARG_WORKSPACE, null));
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
      _exitOnFirstError = line.hasOption(ARG_EXIT_ON_FIRST_ERROR);
      _dumpControlPlaneFacts = line.hasOption(BfConsts.COMMAND_WRITE_CP_FACTS);
      _anonymize = line.hasOption(ARG_ANONYMIZE);
      if (_anonymize) {
         _anonymizeDir = line.getOptionValue(ARG_ANONYMIZE);
      }
      _simplify = DEFAULT_Z3_SIMPLIFY;
      if (line.hasOption(ARG_DISABLE_Z3_SIMPLIFICATION)) {
         _simplify = false;
      }
      _serializeVendor = line.hasOption(ARG_SERIALIZE_VENDOR);
      _serializeIndependent = line.hasOption(ARG_SERIALIZE_INDEPENDENT);
      _dataPlane = line.hasOption(ARG_DATA_PLANE);
      _printParseTree = line.hasOption(ARG_PRINT_PARSE_TREES);
      _nodeSetPath = line.getOptionValue(ARG_NODE_SET_PATH);
      _serializeToText = line.hasOption(ARG_SERIALIZE_TO_TEXT);
      _buildPredicateInfo = line.hasOption(ARG_BUILD_PREDICATE_INFO);
      if (_buildPredicateInfo) {
         _logicSrcDir = line.getOptionValue(ARG_BUILD_PREDICATE_INFO);
      }
      _nodeRolesPath = line.getOptionValue(ARG_NODE_ROLES_PATH);
      _roleNodesPath = line.getOptionValue(ARG_ROLE_NODES_PATH);
      _roleTransitQueryPath = line.getOptionValue(ARG_ROLE_TRANSIT_QUERY_PATH);
      _roleTransitQuery = line.hasOption(ARG_ROLE_TRANSIT_QUERY);
      _roleSetPath = line.getOptionValue(ARG_ROLE_SET_PATH);
      _duplicateRoleFlows = line.hasOption(ARG_DUPLICATE_ROLE_FLOWS);
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
      _nxtnetTraffic = line.hasOption(BfConsts.COMMAND_NXTNET_TRAFFIC);
      _sequential = line.hasOption(ARG_SEQUENTIAL);
      _coordinatorHost = line.getOptionValue(ARG_COORDINATOR_HOST);
      _coordinatorPoolPort = Integer.parseInt(line.getOptionValue(
            ARG_COORDINATOR_POOL_PORT, CoordConsts.SVC_POOL_PORT.toString()));
      _coordinatorWorkPort = Integer.parseInt(line.getOptionValue(
            ARG_COORDINATOR_WORK_PORT, CoordConsts.SVC_WORK_PORT.toString()));
      _coordinatorUseSsl = Boolean
            .parseBoolean(line.getOptionValue(ARG_COORDINATOR_USE_SSL,
                  _config.getString(ARG_COORDINATOR_USE_SSL)));
      _serviceHost = line.getOptionValue(ARG_SERVICE_HOST);
      _noOutput = line.hasOption(ARG_NO_OUTPUT);
      _logTee = line.hasOption(ARG_LOG_TEE);
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
      _history = line.hasOption(BfConsts.COMMAND_GET_HISTORY);
      _traceQuery = line.hasOption(ARG_TRACE_QUERY);
      _printSymmetricEdges = line.hasOption(ARG_PRINT_SYMMETRIC_EDGES);
      String jobsStr = line.getOptionValue(ARG_JOBS, DEFAULT_JOBS);
      _jobs = Integer.parseInt(jobsStr);
      _shuffleJobs = !line.hasOption(ARG_NO_SHUFFLE);
      _diffActive = line.hasOption(BfConsts.ARG_DIFF_ACTIVE);
      _nxtnetDataPlane = line.hasOption(BfConsts.COMMAND_NXTNET_DATA_PLANE);
      _diffQuestion = line.hasOption(ARG_DIFF_QUESTION);
   }

   public boolean printParseTree() {
      return _printParseTree;
   }

   public boolean runInServiceMode() {
      return _runInServiceMode;
   }

   public void setActiveEnvironmentSettings(EnvironmentSettings envSettings) {
      _activeEnvironmentSettings = envSettings;
   }

   public void setDiffEnvironmentName(String diffEnvironmentName) {
      _diffEnvironmentName = diffEnvironmentName;
   }

   public void setDiffQuestion(boolean diffQuestion) {
      _diffQuestion = diffQuestion;
   }

   public void setEnvironmentName(String envName) {
      _environmentName = envName;
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

   public void setNodeSetPath(String nodeSetPath) {
      _nodeSetPath = nodeSetPath;
   }

   public void setNxtnetTraffic(boolean postFlows) {
      _nxtnetTraffic = postFlows;
   }

   public void setPrecomputedRoutesPath(String writeRoutesPath) {
      _precomputedRoutesPath = writeRoutesPath;
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

   public void setTestRigPath(String path) {
      _testRigPath = path;
   }

   public void setZ3DataPlaneFile(String path) {
      _z3File = path;
   }

}
