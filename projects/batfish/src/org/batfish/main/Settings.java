package org.batfish.main;

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

public class Settings {

   private static final String ARG_ACCEPT_NODE = "acceptnode";
   private static final String ARG_ANONYMIZE = "anonymize";
   private static final String ARG_BLACK_HOLE = "blackhole";
   private static final String ARG_BLACK_HOLE_PATH = "blackholepath";
   private static final String ARG_BLACKLIST_DST_IP_PATH = "blacklistdstippath";
   private static final String ARG_BLACKLIST_INTERFACE = "blint";
   private static final String ARG_BLACKLIST_NODE = "blnode";
   private static final String ARG_BUILD_PREDICATE_INFO = "bpi";
   private static final String ARG_CB_HOST = "lbhost";
   private static final String ARG_CB_PORT = "lbport";
   private static final String ARG_COMPILE = "compile";
   private static final String ARG_CONC_UNIQUE = "concunique";
   private static final String ARG_COUNT = "count";
   private static final String ARG_DATA_PLANE = "dp";
   private static final String ARG_DATA_PLANE_DIR = "dpdir";
   private static final String ARG_DIFF = "diff";
   private static final String ARG_DISABLE_Z3_SIMPLIFICATION = "nosimplify";
   private static final String ARG_DUMP_CONTROL_PLANE_FACTS = "dumpcp";
   private static final String ARG_DUMP_FACTS_DIR = "dumpdir";
   private static final String ARG_DUMP_IF = "dumpif";
   private static final String ARG_DUMP_IF_DIR = "dumpifdir";
   private static final String ARG_DUMP_INTERFACE_DESCRIPTIONS = "id";
   private static final String ARG_DUMP_INTERFACE_DESCRIPTIONS_PATH = "idpath";
   private static final String ARG_DUMP_TRAFFIC_FACTS = "dumptraffic";
   private static final String ARG_DUPLICATE_ROLE_FLOWS = "drf";
   private static final String ARG_EXIT_ON_PARSE_ERROR = "ee";
   private static final String ARG_FACTS = "facts";
   private static final String ARG_FLATTEN = "flatten";
   private static final String ARG_FLATTEN_DESTINATION = "flattendst";
   private static final String ARG_FLATTEN_ON_THE_FLY = "flattenonthefly";
   private static final String ARG_FLATTEN_SOURCE = "flattensrc";
   private static final String ARG_FLOW_PATH = "flowpath";
   private static final String ARG_FLOW_SINK_PATH = "flowsink";
   private static final String ARG_FLOWS = "flow";
   private static final String ARG_GENERATE_STUBS = "gs";
   private static final String ARG_GENERATE_STUBS_INPUT_ROLE = "gsinputrole";
   private static final String ARG_GENERATE_STUBS_INTERFACE_DESCRIPTION_REGEX = "gsidregex";
   private static final String ARG_GENERATE_STUBS_REMOTE_AS = "gsremoteas";
   private static final String ARG_GUI = "gui";
   private static final String ARG_HELP = "help";
   private static final String ARG_HISTOGRAM = "histogram";
   private static final String ARG_INTERFACE_MAP_PATH = "impath";
   private static final String ARG_LB_WEB_ADMIN_PORT = "lbwebadminport";
   private static final String ARG_LB_WEB_PORT = "lbwebport";
   private static final String ARG_LOG_LEVEL = "log";
   private static final String ARG_LOGICDIR = "logicdir";
   private static final String ARG_MPI = "mpi";
   private static final String ARG_MPI_PATH = "mpipath";
   private static final String ARG_NO_TRAFFIC = "notraffic";
   private static final String ARG_NODE_ROLES_PATH = "nrpath";
   private static final String ARG_NODE_SET_PATH = "nodes";
   private static final String ARG_PEDANTIC_AS_ERROR = "pedanticerror";
   private static final String ARG_PEDANTIC_SUPPRESS = "pedanticsuppress";
   private static final String ARG_PREDHELP = "predhelp";
   private static final String ARG_PREDICATES = "predicates";
   private static final String ARG_PRINT_PARSE_TREES = "ppt";
   private static final String ARG_QUERY = "query";
   private static final String ARG_QUERY_ALL = "all";
   private static final String ARG_REACH = "reach";
   private static final String ARG_REACH_PATH = "reachpath";
   private static final String ARG_RED_FLAG_AS_ERROR = "redflagerror";
   private static final String ARG_RED_FLAG_SUPPRESS = "redflagsuppress";
   private static final String ARG_REDIRECT_STDERR = "redirect";
   private static final String ARG_REMOVE_FACTS = "remove";
   private static final String ARG_REVERT = "revert";
   private static final String ARG_ROLE_HEADERS = "rh";
   private static final String ARG_ROLE_NODES_PATH = "rnpath";
   private static final String ARG_ROLE_REACHABILITY_QUERY = "rr";
   private static final String ARG_ROLE_REACHABILITY_QUERY_PATH = "rrpath";
   private static final String ARG_ROLE_SET_PATH = "rspath";
   private static final String ARG_ROLE_TRANSIT_QUERY = "rt";
   private static final String ARG_ROLE_TRANSIT_QUERY_PATH = "rtpath";
   private static final String ARG_SERIALIZE_INDEPENDENT = "si";
   private static final String ARG_SERIALIZE_INDEPENDENT_PATH = "sipath";
   private static final String ARG_SERIALIZE_TO_TEXT = "stext";
   private static final String ARG_SERIALIZE_VENDOR = "sv";
   private static final String ARG_SERIALIZE_VENDOR_PATH = "svpath";
   private static final String ARG_SERVICE_MODE = "servicemode";
   private static final String ARG_SERVICE_PORT = "serviceport";
   private static final String ARG_SERVICE_URL = "serviceurl";
   private static final String ARG_TEST_RIG_PATH = "testrig";
   private static final String ARG_THROW_ON_LEXER_ERROR = "throwlexer";
   private static final String ARG_THROW_ON_PARSER_ERROR = "throwparser";
   private static final String ARG_UNIMPLEMENTED_AS_ERROR = "unimplementederror";
   private static final String ARG_UNIMPLEMENTED_SUPPRESS = "unimplementedsuppress";
   private static final String ARG_UPDATE = "update";
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
   private static final String ARGNAME_BLACK_HOLE_PATH = "path";
   private static final String ARGNAME_BLACKLIST_DST_IP = "ip";
   private static final String ARGNAME_BLACKLIST_INTERFACE = "node,interface";
   private static final String ARGNAME_BLACKLIST_NODE = "node";
   private static final String ARGNAME_BUILD_PREDICATE_INFO = "path";
   private static final String ARGNAME_DATA_PLANE_DIR = "path";
   private static final String ARGNAME_DUMP_FACTS_DIR = "path";
   private static final String ARGNAME_DUMP_IF_DIR = "path";
   private static final String ARGNAME_DUMP_INTERFACE_DESCRIPTIONS_PATH = "path";
   private static final String ARGNAME_FLATTEN_DESTINATION = "path";
   private static final String ARGNAME_FLATTEN_SOURCE = "path";
   private static final String ARGNAME_FLOW_PATH = "path";
   private static final String ARGNAME_FLOW_SINK_PATH = "path";
   private static final String ARGNAME_GENERATE_STUBS_INPUT_ROLE = "role";
   private static final String ARGNAME_GENERATE_STUBS_INTERFACE_DESCRIPTION_REGEX = "java-regex";
   private static final String ARGNAME_GENERATE_STUBS_REMOTE_AS = "as";
   private static final String ARGNAME_INTERFACE_MAP_PATH = "path";
   private static final String ARGNAME_LB_WEB_ADMIN_PORT = "port";
   private static final String ARGNAME_LB_WEB_PORT = "port";
   private static final String ARGNAME_LOG_LEVEL = "level";
   private static final String ARGNAME_LOGICDIR = "path";
   private static final String ARGNAME_MPI_PATH = "path";
   private static final String ARGNAME_NODE_ROLES_PATH = "path";
   private static final String ARGNAME_NODE_SET_PATH = "path";
   private static final String ARGNAME_REACH_PATH = "path";
   private static final String ARGNAME_REVERT = "branch-name";
   private static final String ARGNAME_ROLE_NODES_PATH = "path";
   private static final String ARGNAME_ROLE_REACHABILITY_QUERY_PATH = "path";
   private static final String ARGNAME_ROLE_SET_PATH = "path";
   private static final String ARGNAME_ROLE_TRANSIT_QUERY_PATH = "path";
   private static final String ARGNAME_SERIALIZE_INDEPENDENT_PATH = "path";
   private static final String ARGNAME_SERIALIZE_VENDOR_PATH = "path";
   private static final String ARGNAME_VAR_SIZE_MAP_PATH = "path";
   private static final String ARGNAME_Z3_CONCRETIZER_INPUT_FILES = "paths";
   private static final String ARGNAME_Z3_CONCRETIZER_NEGATED_INPUT_FILES = "paths";
   private static final String ARGNAME_Z3_CONCRETIZER_OUTPUT_FILE = "path";
   private static final String ARGNAME_Z3_OUTPUT = "path";
   public static final String DEFAULT_CONNECTBLOX_ADMIN_PORT = "55181";
   public static final String DEFAULT_CONNECTBLOX_HOST = "localhost";
   public static final String DEFAULT_CONNECTBLOX_REGULAR_PORT = "55179";
   private static final String DEFAULT_DATA_PLANE_DIR = "dp";
   private static final String DEFAULT_DUMP_FACTS_DIR = "facts";
   private static final String DEFAULT_DUMP_IF_DIR = "if";
   private static final String DEFAULT_DUMP_INTERFACE_DESCRIPTIONS_PATH = "interface_descriptions";
   private static final String DEFAULT_FLOW_PATH = "flows";
   private static final String DEFAULT_LB_WEB_ADMIN_PORT = "55183";
   private static final String DEFAULT_LB_WEB_PORT = "8080";
   private static final List<String> DEFAULT_PREDICATES = Collections
         .singletonList("InstalledRoute");
   private static final String DEFAULT_SERIALIZE_INDEPENDENT_PATH = "serialized-independent-configs";
   private static final String DEFAULT_SERIALIZE_VENDOR_PATH = "serialized-vendor-configs";
   private static final String DEFAULT_SERVICE_PORT = "9999";
   private static final String DEFAULT_SERVICE_URL = "http://localhost";
   private static final String DEFAULT_TEST_RIG_PATH = "default_test_rig";
   private static final String DEFAULT_Z3_OUTPUT = "z3-dataplane-output.smt2";
   private static final boolean DEFAULT_Z3_SIMPLIFY = true;
   private static final String EXECUTABLE_NAME = "batfish";

   private String _acceptNode;
   private boolean _anonymize;
   private String _anonymizeDir;
   private boolean _blackHole;
   private String _blackHolePath;
   private String _blacklistDstIpPath;
   private String _blacklistInterface;
   private String _blacklistNode;
   private boolean _buildPredicateInfo;
   private boolean _canExecute;
   private String _cbHost;
   private int _cbPort;
   private boolean _compile;
   private boolean _concretize;
   private String[] _concretizerInputFilePaths;
   private String _concretizerOutputFilePath;
   private boolean _concUnique;
   private boolean _counts;
   private boolean _dataPlane;
   private String _dataPlaneDir;
   private boolean _diff;
   private boolean _dumpControlPlaneFacts;
   private String _dumpFactsDir;
   private boolean _dumpIF;
   private String _dumpIFDir;
   private boolean _dumpInterfaceDescriptions;
   private String _dumpInterfaceDescriptionsPath;
   private boolean _dumpTrafficFacts;
   private boolean _duplicateRoleFlows;
   private boolean _exitOnParseError;
   private boolean _facts;
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
   private List<String> _helpPredicates;
   private boolean _histogram;
   private String _hsaInputDir;
   private String _hsaOutputDir;
   private String _interfaceMapPath;
   private int _lbWebAdminPort;
   private int _lbWebPort;
   private String _logicDir;
   private String _logicSrcDir;
   private String _logLevel;
   private String _mpiPath;
   private String[] _negatedConcretizerInputFilePaths;
   private String _nodeRolesPath;
   private String _nodeSetPath;
   private boolean _noTraffic;
   private Options _options;
   private boolean _pedanticAsError;
   private boolean _pedanticRecord;
   private List<String> _predicates;
   private boolean _printParseTree;
   private boolean _printSemantics;
   private boolean _query;
   private boolean _queryAll;
   private boolean _reach;
   private String _reachPath;
   private boolean _redFlagAsError;
   private boolean _redFlagRecord;
   private boolean _redirectStdErr;
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
   private String _secondTestRigPath;
   private boolean _serializeIndependent;
   private String _serializeIndependentPath;
   private boolean _serializeToText;
   private boolean _serializeVendor;
   private String _serializeVendorPath;
   private int _servicePort;
   private String _serviceUrl;
   private boolean _simplify;
   private String _testRigPath;
   private boolean _throwOnLexerError;
   private boolean _throwOnParserError;
   private boolean _unimplementedAsError;
   private boolean _unimplementedRecord;
   private boolean _update;
   private String _varSizeMapPath;
   private String _workspaceName;
   private boolean _z3;
   private String _z3File;

   public Settings() throws ParseException {
      this(new String[] {});
   }

   public Settings(String[] args) throws ParseException {
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
      return _compile;
   }

   public boolean dumpInterfaceDescriptions() {
      return _dumpInterfaceDescriptions;
   }

   public boolean duplicateRoleFlows() {
      return _duplicateRoleFlows;
   }

   public boolean exitOnParseError() {
      return _exitOnParseError;
   }

   public boolean flattenOnTheFly() {
      return _flattenOnTheFly;
   }

   public String getAcceptNode() {
      return _acceptNode;
   }

   public boolean getAnonymize() {
      return _anonymize;
   }

   public String getAnonymizeDir() {
      return _anonymizeDir;
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

   public boolean getCountsOnly() {
      return _counts;
   }

   public boolean getDataPlane() {
      return _dataPlane;
   }

   public String getDataPlaneDir() {
      return _dataPlaneDir;
   }

   public boolean getDiff() {
      return _diff;
   }

   public boolean getDumpControlPlaneFacts() {
      return _dumpControlPlaneFacts;
   }

   public String getDumpFactsDir() {
      return _dumpFactsDir;
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

   public boolean getFacts() {
      return _facts;
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

   public boolean getFlows() {
      return _flows;
   }

   public String getFlowSinkPath() {
      return _flowSinkPath;
   }

   public boolean getGenerateMultipathInconsistencyQuery() {
      return _genMultipath;
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

   public int getLbWebAdminPort() {
      return _lbWebAdminPort;
   }

   public int getLbWebPort() {
      return _lbWebPort;
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

   public boolean getNoTraffic() {
      return _noTraffic;
   }

   public boolean getPedanticAsError() {
      return _pedanticAsError;
   }

   public boolean getPedanticRecord() {
      return _pedanticRecord;
   }

   public List<String> getPredicates() {
      return _predicates;
   }

   public boolean getPrintSemantics() {
      return _printSemantics;
   }

   public boolean getQuery() {
      return _query;
   }

   public boolean getQueryAll() {
      return _queryAll;
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

   public String getSecondTestRigPath() {
      return _secondTestRigPath;
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

   public int getServicePort() {
      return _servicePort;
   }

   public String getServiceUrl() {
      return _serviceUrl;
   }

   public boolean getSimplify() {
      return _simplify;
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

   public boolean getUnimplementedAsError() {
      return _unimplementedAsError;
   }

   public boolean getUnimplementedRecord() {
      return _unimplementedRecord;
   }

   public boolean getUpdate() {
      return _update;
   }

   public String getVarSizeMapPath() {
      return _varSizeMapPath;
   }

   public String getWorkspaceName() {
      return _workspaceName;
   }

   public boolean getZ3() {
      return _z3;
   }

   public String getZ3File() {
      return _z3File;
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
            .longOpt(ARG_COMPILE).build());
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
                  .longOpt(ARG_EXIT_ON_PARSE_ERROR).build());
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
      _options.addOption(Option.builder().argName("secondPath").hasArg()
            .desc("path to test rig directory to diff with").longOpt(ARG_DIFF)
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
      _options.addOption(Option.builder().desc("redirect stderr to stdout")
            .longOpt(ARG_REDIRECT_STDERR).build());
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
            .longOpt(ARG_DATA_PLANE_DIR).build());
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
            .desc("log4j2 log level").longOpt(ARG_LOG_LEVEL).build());
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
                  .argName(ARGNAME_FLATTEN_SOURCE)
                  .desc("path to test rig containing hierarchical juniper configurations to be flattened")
                  .longOpt(ARG_FLATTEN_SOURCE).build());
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
                  .longOpt(ARG_PEDANTIC_AS_ERROR).build());
      _options.addOption(Option.builder().desc("suppresses pedantic warnings")
            .longOpt(ARG_PEDANTIC_SUPPRESS).build());
      _options
            .addOption(Option
                  .builder()
                  .desc("throws "
                        + RedFlagBatfishException.class.getSimpleName()
                        + " on some recoverable errors (e.g. bad config lines), instead of emitting warning and attempting to recover")
                  .longOpt(ARG_RED_FLAG_AS_ERROR).build());
      _options.addOption(Option.builder().desc("suppresses red-flag warnings")
            .longOpt(ARG_RED_FLAG_SUPPRESS).build());
      _options
            .addOption(Option
                  .builder()
                  .desc("throws "
                        + UnimplementedBatfishException.class.getSimpleName()
                        + " when encountering unimplemented configuration directives, instead of emitting warning and ignoring")
                  .longOpt(ARG_UNIMPLEMENTED_AS_ERROR).build());
      _options.addOption(Option.builder()
            .desc("suppresses unimplemented-configuration-directive warnings")
            .longOpt(ARG_UNIMPLEMENTED_SUPPRESS).build());
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
   }

   private void parseCommandLine(String[] args) throws ParseException {
      _canExecute = true;
      _runInServiceMode = false;
      _printSemantics = false;
      CommandLine line = null;
      CommandLineParser parser = new DefaultParser();

      // parse the command line arguments
      line = parser.parse(_options, args);

      if (line.hasOption(ARG_HELP)) {
         _canExecute = false;
         // automatically generate the help statement
         HelpFormatter formatter = new HelpFormatter();
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

      _workspaceName = line.getOptionValue(ARG_WORKSPACE, null);
      if (line.hasOption(ARG_PREDICATES)) {
         _predicates = Arrays.asList(line.getOptionValues(ARG_PREDICATES));
      }
      else {
         _predicates = DEFAULT_PREDICATES;
      }
      _removeFacts = line.hasOption(ARG_REMOVE_FACTS);
      _compile = line.hasOption(ARG_COMPILE);
      _facts = line.hasOption(ARG_FACTS);
      _update = line.hasOption(ARG_UPDATE);
      _noTraffic = line.hasOption(ARG_NO_TRAFFIC);
      _exitOnParseError = line.hasOption(ARG_EXIT_ON_PARSE_ERROR);
      _z3 = line.hasOption(ARG_Z3);
      if (_z3) {
         _z3File = line.getOptionValue(ARG_Z3_OUTPUT, DEFAULT_Z3_OUTPUT);
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
      _secondTestRigPath = line.getOptionValue(ARG_DIFF);
      _diff = line.hasOption(ARG_DIFF);
      _dumpIF = line.hasOption(ARG_DUMP_IF);
      if (_dumpIF) {
         _dumpIFDir = line.getOptionValue(ARG_DUMP_IF_DIR, DEFAULT_DUMP_IF_DIR);
      }
      _dumpControlPlaneFacts = line.hasOption(ARG_DUMP_CONTROL_PLANE_FACTS);
      _dumpTrafficFacts = line.hasOption(ARG_DUMP_TRAFFIC_FACTS);
      _dumpFactsDir = line.getOptionValue(ARG_DUMP_FACTS_DIR,
            DEFAULT_DUMP_FACTS_DIR);

      _revertBranchName = line.getOptionValue(ARG_REVERT);
      _revert = (_revertBranchName != null);
      _redirectStdErr = line.hasOption(ARG_REDIRECT_STDERR);
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
      _dataPlaneDir = line.getOptionValue(ARG_DATA_PLANE_DIR,
            DEFAULT_DATA_PLANE_DIR);
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
      _logLevel = line.getOptionValue(ARG_LOG_LEVEL);
      _roleHeaders = line.hasOption(ARG_ROLE_HEADERS);
      _throwOnParserError = line.hasOption(ARG_THROW_ON_PARSER_ERROR);
      _throwOnLexerError = line.hasOption(ARG_THROW_ON_LEXER_ERROR);
      _flatten = line.hasOption(ARG_FLATTEN);
      _flattenSource = line.getOptionValue(ARG_FLATTEN_SOURCE);
      _flattenDestination = line.getOptionValue(ARG_FLATTEN_DESTINATION);
      _flattenOnTheFly = line.hasOption(ARG_FLATTEN_ON_THE_FLY);
      _pedanticAsError = line.hasOption(ARG_PEDANTIC_AS_ERROR);
      _pedanticRecord = !line.hasOption(ARG_PEDANTIC_SUPPRESS);
      _redFlagAsError = line.hasOption(ARG_RED_FLAG_AS_ERROR);
      _redFlagRecord = !line.hasOption(ARG_RED_FLAG_SUPPRESS);
      _unimplementedAsError = line.hasOption(ARG_UNIMPLEMENTED_AS_ERROR);
      _unimplementedRecord = !line.hasOption(ARG_UNIMPLEMENTED_SUPPRESS);
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
   }

   public boolean printParseTree() {
      return _printParseTree;
   }

   public boolean redirectStdErr() {
      return _redirectStdErr;
   }

   public boolean revert() {
      return _revert;
   }

   public boolean runInServiceMode() {
      return _runInServiceMode;
   }

}
