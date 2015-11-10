package org.batfish.main;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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

      private String _name;

      private String _nodeBlacklistPath;

      private String _nxtnetDataPlaneInputFile;

      private String _nxtnetDataPlaneOutputDir;

      private String _nxtnetTrafficInputFile;

      private String _nxtnetTrafficOutputDir;

      private String _serializedTopologyPath;

      private String _trafficFactsDir;

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

   }

   private static final String ARG_ANONYMIZE = "anonymize";

   private static final String ARG_AUTO_BASE_DIR = "autobasedir";

   private static final String ARG_BUILD_PREDICATE_INFO = "bpi";

   private static final String ARG_COORDINATOR_HOST = "coordinatorhost";

   private static final String ARG_COORDINATOR_POOL_PORT = "coordinatorpoolport";

   private static final String ARG_COORDINATOR_REGISTER = "register";

   private static final String ARG_COORDINATOR_USE_SSL = "coordinator.UseSsl";

   private static final String ARG_COORDINATOR_WORK_PORT = "coordinatorworkport";

   private static final String ARG_DIFF_QUESTION = "diffquestion";

   private static final String ARG_DISABLE_Z3_SIMPLIFICATION = "nosimplify";

   private static final String ARG_EXIT_ON_FIRST_ERROR = "ee";

   private static final String ARG_FLATTEN = "flatten";

   private static final String ARG_FLATTEN_DESTINATION = "flattendst";

   private static final String ARG_FLATTEN_ON_THE_FLY = "flattenonthefly";

   private static final String ARG_GEN_OSPF_TOPLOGY_PATH = "genospf";

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

   private static final String ARG_SEQUENTIAL = "sequential";

   private static final String ARG_SERIALIZE_TO_TEXT = "stext";

   private static final String ARG_SERVICE_HOST = "servicehost";

   private static final String ARG_SERVICE_MODE = "servicemode";

   private static final String ARG_SERVICE_PORT = "serviceport";

   private static final String ARG_SYNTHESIZE_TOPOLOGY = "synthesizetopology";

   private static final String ARG_THROW_ON_LEXER_ERROR = "throwlexer";

   private static final String ARG_THROW_ON_PARSER_ERROR = "throwparser";

   private static final String ARG_TIMESTAMP = "timestamp";

   /**
    * (not wired to command line)
    */
   private static final String ARG_TRUST_ALL_SSL_CERTS = "batfish.TrustAllSslCerts";

   private static final String ARG_USE_PRECOMPUTED_FACTS = "useprecomputedfacts";

   private static final String ARGNAME_AS = "as";

   private static final String ARGNAME_HOSTNAME = "hostname";

   private static final String ARGNAME_JAVA_REGEX = "java-regex";

   private static final String ARGNAME_LOG_LEVEL = "level-name";

   private static final String ARGNAME_NAME = "name";

   private static final String ARGNAME_PATH = "path";

   private static final String ARGNAME_PATHS = "path..";

   private static final String ARGNAME_PORT = "port";

   private static final String ARGNAME_ROLE = "role";

   private static final String EXECUTABLE_NAME = "batfish";

   private static final int HELP_WIDTH = 80;

   private EnvironmentSettings _activeEnvironmentSettings;

   private boolean _anonymize;

   private boolean _answer;

   private String _autoBaseDir;

   private EnvironmentSettings _baseEnvironmentSettings;

   private List<String> _blockNames;

   private boolean _buildPredicateInfo;

   private boolean _canExecute;

   private FileConfiguration _config;

   private String _coordinatorHost;

   private int _coordinatorPoolPort;

   private boolean _coordinatorRegister;

   private boolean _coordinatorUseSsl;

   private int _coordinatorWorkPort;

   private boolean _dataPlane;

   private boolean _diffActive;

   private String _diffEnvironmentName;

   private EnvironmentSettings _diffEnvironmentSettings;

   private boolean _diffQuestion;

   private String _environmentName;

   private boolean _exitOnFirstError;

   private boolean _flatten;

   private String _flattenDestination;

   private boolean _flattenOnTheFly;

   private boolean _generateStubs;

   private String _generateStubsInputRole;

   private String _generateStubsInterfaceDescriptionRegex;

   private Integer _generateStubsRemoteAs;

   private String _genOspfTopologyPath;

   private List<String> _helpPredicates;

   private boolean _histogram;

   private boolean _history;

   private boolean _ignoreUnsupported;

   private int _jobs;

   private boolean _keepBlocks;

   private CommandLine _line;

   private String _logFile;

   private BatfishLogger _logger;

   private String _logicDir;

   private String _logicSrcDir;

   private String _logLevel;

   private boolean _logTee;

   private String _nodeRolesPath;

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

   private List<String> _precomputedRoutesPaths;

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

   private boolean _unimplementedAsError;

   private boolean _unimplementedRecord;

   private boolean _usePrecomputedAdvertisements;

   private boolean _usePrecomputedFacts;

   private boolean _usePrecomputedIbgpNeighbors;

   private boolean _usePrecomputedRoutes;

   private boolean _writeBgpAdvertisements;

   private boolean _writeControlPlaneFacts;

   private boolean _writeIbgpNeighbors;

   private boolean _writeRoutes;

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
      initConfigDefaults();

      initOptions();
      parseCommandLine(args);
   }

   public boolean canExecute() {
      return _canExecute;
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

   private boolean getBooleanOptionValue(String key) {
      boolean value = _line.hasOption(key);
      if (!value) {
         value = _config.getBoolean(key);
      }
      return value;
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

   public boolean getCoordinatorRegister() {
      return _coordinatorRegister;
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
      return _writeControlPlaneFacts;
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
      return _genOspfTopologyPath;
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

   private Integer getIntegerOptionValue(String key) {
      String valueStr = _line.getOptionValue(key);
      if (valueStr == null) {
         return _config.getInteger(key, null);
      }
      else {
         return Integer.parseInt(valueStr);
      }
   }

   private int getIntOptionValue(String key) {
      String valueStr = _line.getOptionValue(key);
      if (valueStr == null) {
         return _config.getInt(key);
      }
      else {
         return Integer.parseInt(valueStr);
      }
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

   public List<String> getPrecomputedRoutesPaths() {
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

   private List<String> getStringListOptionValue(String key) {
      if (_line.hasOption(ARG_PREDICATES)) {
         String[] optionValues = _line.getOptionValues(ARG_PREDICATES);
         if (optionValues == null) {
            return Collections.<String> emptyList();
         }
         else {
            return Arrays.asList(optionValues);
         }
      }
      else {
         return Arrays.asList(_config.getStringArray(key));
      }
   }

   private String getStringOptionValue(String key) {
      String value = _line.getOptionValue(key, _config.getString(key));
      return value;
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

   public String getZ3File() {
      return _z3File;
   }

   public boolean ignoreUnsupported() {
      return _ignoreUnsupported;
   }

   private void initConfigDefaults() {
      setDefaultProperty(ARG_ANONYMIZE, false);
      setDefaultProperty(BfConsts.ARG_BLOCK_NAMES, new String[] {});
      setDefaultProperty(ARG_COORDINATOR_REGISTER, false);
      setDefaultProperty(ARG_COORDINATOR_HOST, "localhost");
      setDefaultProperty(ARG_COORDINATOR_POOL_PORT, CoordConsts.SVC_POOL_PORT);
      setDefaultProperty(ARG_COORDINATOR_USE_SSL, false);
      setDefaultProperty(ARG_COORDINATOR_WORK_PORT, CoordConsts.SVC_WORK_PORT);
      setDefaultProperty(BfConsts.ARG_DIFF_ACTIVE, false);
      setDefaultProperty(BfConsts.ARG_DIFF_ENVIRONMENT_NAME, null);
      setDefaultProperty(ARG_DIFF_QUESTION, false);
      setDefaultProperty(ARG_DISABLE_Z3_SIMPLIFICATION, false);
      setDefaultProperty(BfConsts.ARG_ENVIRONMENT_NAME, null);
      setDefaultProperty(ARG_EXIT_ON_FIRST_ERROR, false);
      setDefaultProperty(ARG_FLATTEN, false);
      setDefaultProperty(ARG_FLATTEN_DESTINATION, null);
      setDefaultProperty(ARG_FLATTEN_ON_THE_FLY, false);
      setDefaultProperty(ARG_GEN_OSPF_TOPLOGY_PATH, null);
      setDefaultProperty(ARG_GENERATE_STUBS, false);
      setDefaultProperty(ARG_GENERATE_STUBS_INPUT_ROLE, null);
      setDefaultProperty(ARG_GENERATE_STUBS_INTERFACE_DESCRIPTION_REGEX, null);
      setDefaultProperty(ARG_GENERATE_STUBS_REMOTE_AS, null);
      setDefaultProperty(ARG_HISTOGRAM, false);
      setDefaultProperty(ARG_IGNORE_UNSUPPORTED, false);
      setDefaultProperty(ARG_JOBS, Integer.MAX_VALUE);
      setDefaultProperty(ARG_LOG_FILE, null);
      setDefaultProperty(ARG_LOG_TEE, false);
      setDefaultProperty(BfConsts.ARG_LOG_LEVEL, "debug");
      setDefaultProperty(ARG_NO_OUTPUT, false);
      setDefaultProperty(ARG_NO_SHUFFLE, false);
      setDefaultProperty(BfConsts.ARG_OUTPUT_ENV, null);
      setDefaultProperty(BfConsts.ARG_PEDANTIC_AS_ERROR, false);
      setDefaultProperty(BfConsts.ARG_PEDANTIC_SUPPRESS, false);
      setDefaultProperty(ARG_PRECOMPUTED_ADVERTISEMENTS_PATH, null);
      setDefaultProperty(ARG_PRECOMPUTED_FACTS_PATH, null);
      setDefaultProperty(ARG_PRECOMPUTED_IBGP_NEIGHBORS_PATH, null);
      setDefaultProperty(ARG_PRECOMPUTED_ROUTES_PATH, null);
      setDefaultProperty(ARG_PRECOMPUTED_ROUTES_PATHS, new String[] {});
      setDefaultProperty(ARG_PREDHELP, new String[] {});
      setDefaultProperty(ARG_PREDICATES, new String[] {});
      setDefaultProperty(ARG_PRINT_PARSE_TREES, false);
      setDefaultProperty(ARG_PRINT_SYMMETRIC_EDGES, false);
      setDefaultProperty(ARG_QUERY, false);
      setDefaultProperty(ARG_QUERY_ALL, false);
      setDefaultProperty(BfConsts.ARG_QUESTION_NAME, null);
      setDefaultProperty(BfConsts.ARG_RED_FLAG_AS_ERROR, false);
      setDefaultProperty(BfConsts.ARG_RED_FLAG_SUPPRESS, false);
      setDefaultProperty(ARG_SEQUENTIAL, false);
      setDefaultProperty(ARG_SERIALIZE_TO_TEXT, false);
      setDefaultProperty(ARG_SERVICE_HOST, "localhost");
      setDefaultProperty(ARG_SERVICE_MODE, false);
      setDefaultProperty(ARG_SERVICE_PORT, BfConsts.SVC_PORT);
      setDefaultProperty(ARG_SYNTHESIZE_TOPOLOGY, false);
      setDefaultProperty(ARG_THROW_ON_LEXER_ERROR, false);
      setDefaultProperty(ARG_THROW_ON_PARSER_ERROR, false);
      setDefaultProperty(ARG_TIMESTAMP, false);
      setDefaultProperty(BfConsts.ARG_USE_PRECOMPUTED_ADVERTISEMENTS, false);
      setDefaultProperty(ARG_USE_PRECOMPUTED_FACTS, false);
      setDefaultProperty(BfConsts.ARG_USE_PRECOMPUTED_IBGP_NEIGHBORS, false);
      setDefaultProperty(BfConsts.ARG_USE_PRECOMPUTED_ROUTES, false);
      setDefaultProperty(BfConsts.ARG_UNIMPLEMENTED_AS_ERROR, false);
      setDefaultProperty(BfConsts.ARG_UNIMPLEMENTED_SUPPRESS, true);
      setDefaultProperty(BfConsts.COMMAND_ANSWER, false);
      setDefaultProperty(BfConsts.COMMAND_DUMP_DP, false);
      setDefaultProperty(BfConsts.COMMAND_GET_HISTORY, false);
      setDefaultProperty(BfConsts.COMMAND_KEEP_BLOCKS, false);
      setDefaultProperty(BfConsts.COMMAND_NXTNET_DATA_PLANE, false);
      setDefaultProperty(BfConsts.COMMAND_NXTNET_TRAFFIC, false);
      setDefaultProperty(BfConsts.COMMAND_PARSE_VENDOR_INDEPENDENT, false);
      setDefaultProperty(BfConsts.COMMAND_PARSE_VENDOR_SPECIFIC, false);
      setDefaultProperty(BfConsts.COMMAND_REMOVE_BLOCKS, false);
      setDefaultProperty(BfConsts.COMMAND_WRITE_CP_FACTS, false);
      setDefaultProperty(BfConsts.COMMAND_WRITE_ADVERTISEMENTS, false);
      setDefaultProperty(BfConsts.COMMAND_WRITE_IBGP_NEIGHBORS, false);
      setDefaultProperty(BfConsts.COMMAND_WRITE_ROUTES, false);
   }

   private void initOptions() {
      _options = new Options();

      _options.addOption(Option.builder()
            .desc("created anonymized versions of configs in test rig")
            .longOpt(ARG_ANONYMIZE).build());

      _options.addOption(Option.builder().hasArg().argName(ARGNAME_PATH)
            .desc("path to base dir for automatic i/o path selection")
            .longOpt(ARG_AUTO_BASE_DIR).build());

      _options.addOption(Option.builder().argName("blocknames").hasArgs()
            .desc("list of blocks of logic rules to add or remove")
            .longOpt(BfConsts.ARG_BLOCK_NAMES).build());

      _options
            .addOption(Option
                  .builder()
                  .hasArg()
                  .argName(ARGNAME_PATH)
                  .desc("build predicate info (should only be called by ant build script) with provided input logic dir")
                  .longOpt(ARG_BUILD_PREDICATE_INFO).build());

      _options.addOption(Option
            .builder()
            .hasArg()
            .argName(ARGNAME_HOSTNAME)
            .desc("hostname of coordinator for registration with -"
                  + ARG_SERVICE_MODE).longOpt(ARG_COORDINATOR_HOST).build());

      _options.addOption(Option.builder().argName(ARGNAME_PORT).hasArg()
            .desc("coordinator pool manager listening port")
            .longOpt(ARG_COORDINATOR_POOL_PORT).build());

      _options.addOption(Option.builder()
            .desc("register service with coordinator on startup")
            .longOpt(ARG_COORDINATOR_REGISTER).build());

      _options.addOption(Option.builder().desc("whether coordinator uses ssl")
            .longOpt(ARG_COORDINATOR_USE_SSL).build());

      _options.addOption(Option.builder().argName("port_number").hasArg()
            .desc("coordinator work manager listening port")
            .longOpt(ARG_COORDINATOR_WORK_PORT).build());

      _options
            .addOption(Option
                  .builder()
                  .desc("make differential environment the active one for questions about a single environment")
                  .longOpt(BfConsts.ARG_DIFF_ACTIVE).build());

      _options.addOption(Option.builder().hasArg().argName("name")
            .desc("name of delta environment to use")
            .longOpt(BfConsts.ARG_DIFF_ENVIRONMENT_NAME).build());

      _options
            .addOption(Option
                  .builder()
                  .desc("force treatment of question as differential (to be used when not answering question)")
                  .longOpt(ARG_DIFF_QUESTION).build());

      _options.addOption(Option.builder().desc("disable z3 simplification")
            .longOpt(ARG_DISABLE_Z3_SIMPLIFICATION).build());

      _options.addOption(Option.builder().hasArg().argName("name")
            .desc("name of environment to use")
            .longOpt(BfConsts.ARG_ENVIRONMENT_NAME).build());

      _options
            .addOption(Option
                  .builder()
                  .desc("exit on first parse error (otherwise will exit on last parse error)")
                  .longOpt(ARG_EXIT_ON_FIRST_ERROR).build());

      _options.addOption(Option.builder()
            .desc("flatten hierarchical juniper configuration files")
            .longOpt(ARG_FLATTEN).build());

      _options
            .addOption(Option
                  .builder()
                  .hasArg()
                  .argName(ARGNAME_PATH)
                  .desc("output path to test rig in which flat juniper (and all other) configurations will be placed")
                  .longOpt(ARG_FLATTEN_DESTINATION).build());

      _options
            .addOption(Option
                  .builder()
                  .desc("flatten hierarchical juniper configuration files on-the-fly (line number references will be spurious)")
                  .longOpt(ARG_FLATTEN_ON_THE_FLY).build());

      _options.addOption(Option.builder().hasArg().argName(ARGNAME_PATH)
            .desc("generate ospf configs from specified topology")
            .longOpt(ARG_GEN_OSPF_TOPLOGY_PATH).build());

      _options.addOption(Option.builder().desc("generate stubs")
            .longOpt(ARG_GENERATE_STUBS).build());

      _options.addOption(Option.builder().hasArg().argName(ARGNAME_ROLE)
            .desc("input role for which to generate stubs")
            .longOpt(ARG_GENERATE_STUBS_INPUT_ROLE).build());

      _options
            .addOption(Option
                  .builder()
                  .hasArg()
                  .argName(ARGNAME_JAVA_REGEX)
                  .desc("java regex to extract hostname of generated stub from description of adjacent interface")
                  .longOpt(ARG_GENERATE_STUBS_INTERFACE_DESCRIPTION_REGEX)
                  .build());

      _options.addOption(Option.builder().hasArg().argName(ARGNAME_AS)
            .desc("autonomous system number of stubs to be generated")
            .longOpt(ARG_GENERATE_STUBS_REMOTE_AS).build());

      _options.addOption(Option.builder().desc("print this message")
            .longOpt(ARG_HELP).build());

      _options
            .addOption(Option
                  .builder()
                  .desc("ignore configuration files with unsupported format instead of crashing")
                  .longOpt(ARG_IGNORE_UNSUPPORTED).build());

      _options.addOption(Option.builder().hasArg().argName("number")
            .desc("number of threads used by parallel jobs executor")
            .longOpt(ARG_JOBS).build());

      _options.addOption(Option.builder().hasArg().argName(ARGNAME_LOG_LEVEL)
            .desc("log level").longOpt(BfConsts.ARG_LOG_LEVEL).build());

      _options.addOption(Option.builder()
            .desc("build histogram of unimplemented features")
            .longOpt(ARG_HISTOGRAM).build());

      _options.addOption(Option.builder().hasArg().argName(ARGNAME_PATH)
            .desc("path to main log file").longOpt(ARG_LOG_FILE).build());

      _options.addOption(Option.builder()
            .desc("print output to both logfile and standard out")
            .longOpt(ARG_LOG_TEE).build());

      _options.addOption(Option.builder().desc("do not produce output files")
            .longOpt(ARG_NO_OUTPUT).build());

      _options.addOption(Option.builder().desc("do not shuffle parallel jobs")
            .longOpt(ARG_NO_SHUFFLE).build());

      _options.addOption(Option.builder().hasArg().argName("name")
            .desc("name of output environment")
            .longOpt(BfConsts.ARG_OUTPUT_ENV).build());

      _options
            .addOption(Option
                  .builder()
                  .desc("throws "
                        + PedanticBatfishException.class.getSimpleName()
                        + " for likely harmless warnings (e.g. deviation from good configuration style), instead of emitting warning and continuing")
                  .longOpt(BfConsts.ARG_PEDANTIC_AS_ERROR).build());

      _options.addOption(Option.builder().desc("suppresses pedantic warnings")
            .longOpt(BfConsts.ARG_PEDANTIC_SUPPRESS).build());

      _options.addOption(Option.builder().hasArg().argName("path")
            .desc("path to precomputed bgp advertisements")
            .longOpt(ARG_PRECOMPUTED_ADVERTISEMENTS_PATH).build());

      _options.addOption(Option.builder().hasArg().argName("path")
            .desc("path to precomputed facts")
            .longOpt(ARG_PRECOMPUTED_FACTS_PATH).build());

      _options.addOption(Option.builder().hasArg().argName("path")
            .desc("path to precomputed ibgp neighbors")
            .longOpt(ARG_PRECOMPUTED_IBGP_NEIGHBORS_PATH).build());

      _options.addOption(Option.builder().hasArg().argName(ARGNAME_PATH)
            .desc("output path to precomputed routes")
            .longOpt(ARG_PRECOMPUTED_ROUTES_PATH).build());

      _options.addOption(Option.builder().hasArg().argName(ARGNAME_PATHS)
            .desc("input paths to precomputed routes")
            .longOpt(ARG_PRECOMPUTED_ROUTES_PATHS).build());

      _options
            .addOption(Option
                  .builder()
                  .argName("predicates")
                  .optionalArg(true)
                  .hasArgs()
                  .desc("print semantics for all predicates, or for predicates supplied as optional arguments")
                  .longOpt(ARG_PREDHELP).build());

      _options.addOption(Option.builder().argName("predicates").hasArgs()
            .desc("list of predicates to query").longOpt(ARG_PREDICATES)
            .build());

      _options.addOption(Option.builder().desc("print parse trees")
            .longOpt(ARG_PRINT_PARSE_TREES).build());

      _options.addOption(Option.builder()
            .desc("print topology with symmetric edges adjacent in listing")
            .longOpt(ARG_PRINT_SYMMETRIC_EDGES).build());

      _options.addOption(Option.builder()
            .desc("query one or more nxtnet relations").longOpt(ARG_QUERY)
            .build());

      _options.addOption(Option.builder().desc("query ALL predicates")
            .longOpt(ARG_QUERY_ALL).build());

      _options.addOption(Option.builder().hasArg().argName(ARGNAME_NAME)
            .desc("name of question").longOpt(BfConsts.ARG_QUESTION_NAME)
            .build());

      _options
            .addOption(Option
                  .builder()
                  .desc("throws "
                        + RedFlagBatfishException.class.getSimpleName()
                        + " on some recoverable errors (e.g. bad config lines), instead of emitting warning and attempting to recover")
                  .longOpt(BfConsts.ARG_RED_FLAG_AS_ERROR).build());

      _options.addOption(Option.builder().desc("suppresses red-flag warnings")
            .longOpt(BfConsts.ARG_RED_FLAG_SUPPRESS).build());

      _options.addOption(Option.builder().desc("force sequential operation")
            .longOpt(ARG_SEQUENTIAL).build());

      _options.addOption(Option.builder().desc("serialize to text")
            .longOpt(ARG_SERIALIZE_TO_TEXT).build());

      _options.addOption(Option.builder().hasArg().argName(ARGNAME_HOSTNAME)
            .desc("local hostname to report to coordinator")
            .longOpt(ARG_SERVICE_HOST).build());

      _options.addOption(Option.builder().desc("run in service mode")
            .longOpt(ARG_SERVICE_MODE).build());

      _options
            .addOption(Option.builder().argName("port_number").hasArg()
                  .desc("port for batfish service").longOpt(ARG_SERVICE_PORT)
                  .build());

      _options.addOption(Option.builder()
            .desc("synthesize topology from interface ip subnet information")
            .longOpt(ARG_SYNTHESIZE_TOPOLOGY).build());

      _options.addOption(Option.builder()
            .desc("throw exception immediately on lexer error")
            .longOpt(ARG_THROW_ON_LEXER_ERROR).build());

      _options.addOption(Option.builder()
            .desc("throw exception immediately on parser error")
            .longOpt(ARG_THROW_ON_PARSER_ERROR).build());

      _options.addOption(Option.builder()
            .desc("print timestamps in log messages").longOpt(ARG_TIMESTAMP)
            .build());

      _options
            .addOption(Option
                  .builder()
                  .desc("throws "
                        + UnimplementedBatfishException.class.getSimpleName()
                        + " when encountering unimplemented configuration directives, instead of emitting warning and ignoring")
                  .longOpt(BfConsts.ARG_UNIMPLEMENTED_AS_ERROR).build());

      _options
            .addOption(Option
                  .builder()
                  .desc("suppresses warnings about unimplemented configuration directives")
                  .longOpt(BfConsts.ARG_UNIMPLEMENTED_SUPPRESS).build());

      _options.addOption(Option.builder()
            .desc("add precomputed bgp advertisements to data plane model")
            .longOpt(BfConsts.ARG_USE_PRECOMPUTED_ADVERTISEMENTS).build());

      _options.addOption(Option.builder()
            .desc("add precomputed facts to data plane model")
            .longOpt(ARG_USE_PRECOMPUTED_FACTS).build());

      _options.addOption(Option.builder()
            .desc("add precomputed ibgp neighbors to data plane model")
            .longOpt(BfConsts.ARG_USE_PRECOMPUTED_IBGP_NEIGHBORS).build());

      _options.addOption(Option.builder()
            .desc("add precomputed routes to data plane model")
            .longOpt(BfConsts.ARG_USE_PRECOMPUTED_ROUTES).build());

      _options.addOption(Option.builder().desc("answer provided question")
            .longOpt(BfConsts.COMMAND_ANSWER).build());

      _options.addOption(Option.builder()
            .desc("compute and serialize data plane")
            .longOpt(BfConsts.COMMAND_DUMP_DP).build());

      _options.addOption(Option.builder().desc("retrieve flow history")
            .longOpt(BfConsts.COMMAND_GET_HISTORY).build());

      _options.addOption(Option.builder()
            .desc("activate only selected blocks of logic rules")
            .longOpt(BfConsts.COMMAND_KEEP_BLOCKS).build());

      _options.addOption(Option.builder()
            .desc("compute data plane with nxtnet")
            .longOpt(BfConsts.COMMAND_NXTNET_DATA_PLANE).build());

      _options
            .addOption(Option
                  .builder()
                  .desc("compute traffic information from provided flows with nxtnet")
                  .longOpt(BfConsts.COMMAND_NXTNET_TRAFFIC).build());

      _options.addOption(Option.builder()
            .desc("serialize vendor-independent configs")
            .longOpt(BfConsts.COMMAND_PARSE_VENDOR_INDEPENDENT).build());

      _options.addOption(Option.builder().desc("serialize vendor configs")
            .longOpt(BfConsts.COMMAND_PARSE_VENDOR_SPECIFIC).build());

      _options.addOption(Option.builder()
            .desc("remove selected blocks of logic rules")
            .longOpt(BfConsts.COMMAND_REMOVE_BLOCKS).build());

      _options
            .addOption(Option
                  .builder()
                  .desc("write bgp advertisements from nxtnet data plane model to disk")
                  .longOpt(BfConsts.COMMAND_WRITE_ADVERTISEMENTS).build());

      _options.addOption(Option.builder().desc("write control plane facts")
            .longOpt(BfConsts.COMMAND_WRITE_CP_FACTS).build());

      _options.addOption(Option.builder()
            .desc("write ibgp neighbors from nxtnet data plane model to disk")
            .longOpt(BfConsts.COMMAND_WRITE_IBGP_NEIGHBORS).build());

      _options.addOption(Option.builder()
            .desc("write routes from nxtnet data plane model to disk")
            .longOpt(BfConsts.COMMAND_WRITE_ROUTES).build());

   }

   private void parseCommandLine(String[] args) throws ParseException {
      _canExecute = true;
      _runInServiceMode = false;
      _printSemantics = false;
      CommandLineParser parser = new DefaultParser();

      // parse the command line arguments
      _line = parser.parse(_options, args);

      // SPECIAL OPTIONS
      _logFile = getStringOptionValue(ARG_LOG_FILE);
      _logLevel = getStringOptionValue(BfConsts.ARG_LOG_LEVEL);
      if (_line.hasOption(ARG_HELP)) {
         _canExecute = false;
         // automatically generate the help statement
         HelpFormatter formatter = new HelpFormatter();
         formatter.setLongOptPrefix("-");
         formatter.setWidth(HELP_WIDTH);
         formatter.printHelp(EXECUTABLE_NAME, _options);
         return;
      }
      _buildPredicateInfo = _line.hasOption(ARG_BUILD_PREDICATE_INFO);
      if (_buildPredicateInfo) {
         _logicSrcDir = _line.getOptionValue(ARG_BUILD_PREDICATE_INFO);
         return;
      }
      _printSemantics = _line.hasOption(ARG_PREDHELP);

      // REGULAR OPTIONS
      _anonymize = getBooleanOptionValue(ARG_ANONYMIZE);
      _answer = getBooleanOptionValue(BfConsts.COMMAND_ANSWER);
      _autoBaseDir = getStringOptionValue(ARG_AUTO_BASE_DIR);
      _blockNames = getStringListOptionValue(BfConsts.ARG_BLOCK_NAMES);
      _coordinatorHost = getStringOptionValue(ARG_COORDINATOR_HOST);
      _coordinatorPoolPort = getIntOptionValue(ARG_COORDINATOR_POOL_PORT);
      _coordinatorRegister = getBooleanOptionValue(ARG_COORDINATOR_REGISTER);
      _coordinatorUseSsl = getBooleanOptionValue(ARG_COORDINATOR_USE_SSL);
      _coordinatorWorkPort = getIntOptionValue(ARG_COORDINATOR_WORK_PORT);
      _dataPlane = getBooleanOptionValue(BfConsts.COMMAND_DUMP_DP);
      _diffActive = getBooleanOptionValue(BfConsts.ARG_DIFF_ACTIVE);
      _diffEnvironmentName = getStringOptionValue(BfConsts.ARG_DIFF_ENVIRONMENT_NAME);
      _diffQuestion = getBooleanOptionValue(ARG_DIFF_QUESTION);
      _environmentName = getStringOptionValue(BfConsts.ARG_ENVIRONMENT_NAME);
      _exitOnFirstError = getBooleanOptionValue(ARG_EXIT_ON_FIRST_ERROR);
      _flatten = getBooleanOptionValue(ARG_FLATTEN);
      _flattenDestination = getStringOptionValue(ARG_FLATTEN_DESTINATION);
      _flattenOnTheFly = getBooleanOptionValue(ARG_FLATTEN_ON_THE_FLY);
      _generateStubs = getBooleanOptionValue(ARG_GENERATE_STUBS);
      _generateStubsInputRole = getStringOptionValue(ARG_GENERATE_STUBS_INPUT_ROLE);
      _generateStubsInterfaceDescriptionRegex = getStringOptionValue(ARG_GENERATE_STUBS_INTERFACE_DESCRIPTION_REGEX);
      _generateStubsRemoteAs = getIntegerOptionValue(ARG_GENERATE_STUBS_REMOTE_AS);
      _genOspfTopologyPath = getStringOptionValue(ARG_GEN_OSPF_TOPLOGY_PATH);
      _helpPredicates = getStringListOptionValue(ARG_PREDHELP);
      _histogram = getBooleanOptionValue(ARG_HISTOGRAM);
      _history = getBooleanOptionValue(BfConsts.COMMAND_GET_HISTORY);
      _ignoreUnsupported = getBooleanOptionValue(ARG_IGNORE_UNSUPPORTED);
      _jobs = getIntOptionValue(ARG_JOBS);
      _keepBlocks = getBooleanOptionValue(BfConsts.COMMAND_KEEP_BLOCKS);
      _logTee = getBooleanOptionValue(ARG_LOG_TEE);
      _noOutput = getBooleanOptionValue(ARG_NO_OUTPUT);
      _nxtnetDataPlane = getBooleanOptionValue(BfConsts.COMMAND_NXTNET_DATA_PLANE);
      _nxtnetTraffic = getBooleanOptionValue(BfConsts.COMMAND_NXTNET_TRAFFIC);
      _outputEnvironmentName = getStringOptionValue(BfConsts.ARG_OUTPUT_ENV);
      _pedanticAsError = getBooleanOptionValue(BfConsts.ARG_PEDANTIC_AS_ERROR);
      _pedanticRecord = !getBooleanOptionValue(BfConsts.ARG_PEDANTIC_SUPPRESS);
      _precomputedBgpAdvertisementsPath = getStringOptionValue(ARG_PRECOMPUTED_ADVERTISEMENTS_PATH);
      _precomputedFactsPath = getStringOptionValue(ARG_PRECOMPUTED_FACTS_PATH);
      _precomputedIbgpNeighborsPath = getStringOptionValue(ARG_PRECOMPUTED_IBGP_NEIGHBORS_PATH);
      _precomputedRoutesPath = getStringOptionValue(ARG_PRECOMPUTED_ROUTES_PATH);
      _precomputedRoutesPaths = getStringListOptionValue(ARG_PRECOMPUTED_ROUTES_PATHS);
      _predicates = getStringListOptionValue(ARG_PREDICATES);
      _printParseTree = getBooleanOptionValue(ARG_PRINT_PARSE_TREES);
      _printSymmetricEdges = getBooleanOptionValue(ARG_PRINT_SYMMETRIC_EDGES);
      _query = getBooleanOptionValue(ARG_QUERY);
      _queryAll = getBooleanOptionValue(ARG_QUERY_ALL);
      _questionName = getStringOptionValue(BfConsts.ARG_QUESTION_NAME);
      _redFlagAsError = getBooleanOptionValue(BfConsts.ARG_RED_FLAG_AS_ERROR);
      _redFlagRecord = !getBooleanOptionValue(BfConsts.ARG_RED_FLAG_SUPPRESS);
      _removeBlocks = getBooleanOptionValue(BfConsts.COMMAND_REMOVE_BLOCKS);
      _runInServiceMode = getBooleanOptionValue(ARG_SERVICE_MODE);
      _sequential = getBooleanOptionValue(ARG_SEQUENTIAL);
      _serializeIndependent = getBooleanOptionValue(BfConsts.COMMAND_PARSE_VENDOR_INDEPENDENT);
      _serializeToText = getBooleanOptionValue(ARG_SERIALIZE_TO_TEXT);
      _serializeVendor = getBooleanOptionValue(BfConsts.COMMAND_PARSE_VENDOR_SPECIFIC);
      _serviceHost = getStringOptionValue(ARG_SERVICE_HOST);
      _servicePort = getIntOptionValue(ARG_SERVICE_PORT);
      _shuffleJobs = !getBooleanOptionValue(ARG_NO_SHUFFLE);
      _simplify = !getBooleanOptionValue(ARG_DISABLE_Z3_SIMPLIFICATION);
      _synthesizeTopology = getBooleanOptionValue(ARG_SYNTHESIZE_TOPOLOGY);
      _throwOnLexerError = getBooleanOptionValue(ARG_THROW_ON_LEXER_ERROR);
      _throwOnParserError = getBooleanOptionValue(ARG_THROW_ON_PARSER_ERROR);
      _timestamp = getBooleanOptionValue(ARG_TIMESTAMP);
      _unimplementedAsError = getBooleanOptionValue(BfConsts.ARG_UNIMPLEMENTED_AS_ERROR);
      _unimplementedRecord = !getBooleanOptionValue(BfConsts.ARG_UNIMPLEMENTED_SUPPRESS);
      _usePrecomputedAdvertisements = getBooleanOptionValue(BfConsts.ARG_USE_PRECOMPUTED_ADVERTISEMENTS);
      _usePrecomputedFacts = getBooleanOptionValue(ARG_USE_PRECOMPUTED_FACTS);
      _usePrecomputedIbgpNeighbors = getBooleanOptionValue(BfConsts.ARG_USE_PRECOMPUTED_IBGP_NEIGHBORS);
      _usePrecomputedRoutes = getBooleanOptionValue(BfConsts.ARG_USE_PRECOMPUTED_ROUTES);
      _writeBgpAdvertisements = getBooleanOptionValue(BfConsts.COMMAND_WRITE_ADVERTISEMENTS);
      _writeControlPlaneFacts = getBooleanOptionValue(BfConsts.COMMAND_WRITE_CP_FACTS);
      _writeIbgpNeighbors = getBooleanOptionValue(BfConsts.COMMAND_WRITE_IBGP_NEIGHBORS);
      _writeRoutes = getBooleanOptionValue(BfConsts.COMMAND_WRITE_ROUTES);
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

   private void setDefaultProperty(String key, Object value) {
      if (_config.getProperty(key) == null) {
         _config.setProperty(key, value);
      }
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

   public void setNodeRolesPath(String nodeRolesPath) {
      _nodeRolesPath = nodeRolesPath;
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
