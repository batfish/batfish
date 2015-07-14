package org.batfish.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.batfish.collections.EdgeSet;
import org.batfish.collections.FibMap;
import org.batfish.collections.FibRow;
import org.batfish.collections.FibSet;
import org.batfish.collections.FlowSinkInterface;
import org.batfish.collections.FlowSinkSet;
import org.batfish.collections.FunctionSet;
import org.batfish.collections.MultiSet;
import org.batfish.collections.NodeInterfacePair;
import org.batfish.collections.NodeRoleMap;
import org.batfish.collections.NodeSet;
import org.batfish.collections.PolicyRouteFibIpMap;
import org.batfish.collections.PolicyRouteFibNodeMap;
import org.batfish.collections.PredicateSemantics;
import org.batfish.collections.PredicateValueTypeMap;
import org.batfish.collections.QualifiedNameMap;
import org.batfish.collections.RoleNodeMap;
import org.batfish.collections.RoleSet;
import org.batfish.collections.TreeMultiSet;
import org.batfish.common.BfConsts;
import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.ParseTreePrettyPrinter;
import org.batfish.grammar.juniper.JuniperCombinedParser;
import org.batfish.grammar.juniper.JuniperFlattener;
import org.batfish.grammar.logicblox.LogQLPredicateInfoExtractor;
import org.batfish.grammar.logicblox.LogiQLCombinedParser;
import org.batfish.grammar.logicblox.LogiQLPredicateInfoResolver;
import org.batfish.grammar.question.QuestionCombinedParser;
import org.batfish.grammar.question.QuestionExtractor;
import org.batfish.grammar.topology.BatfishTopologyCombinedParser;
import org.batfish.grammar.topology.BatfishTopologyExtractor;
import org.batfish.grammar.topology.GNS3TopologyCombinedParser;
import org.batfish.grammar.topology.GNS3TopologyExtractor;
import org.batfish.grammar.topology.RoleCombinedParser;
import org.batfish.grammar.topology.RoleExtractor;
import org.batfish.grammar.topology.TopologyExtractor;
import org.batfish.grammar.z3.ConcretizerQueryResultCombinedParser;
import org.batfish.grammar.z3.ConcretizerQueryResultExtractor;
import org.batfish.grammar.z3.DatalogQueryResultCombinedParser;
import org.batfish.grammar.z3.DatalogQueryResultExtractor;
import org.batfish.job.FlattenVendorConfigurationJob;
import org.batfish.job.FlattenVendorConfigurationResult;
import org.batfish.job.ParseVendorConfigurationJob;
import org.batfish.job.ParseVendorConfigurationResult;
import org.batfish.logic.LogicResourceLocator;
import org.batfish.logicblox.ConfigurationFactExtractor;
import org.batfish.logicblox.Facts;
import org.batfish.logicblox.LBInitializationException;
import org.batfish.logicblox.LBValueType;
import org.batfish.logicblox.LogicBloxFrontend;
import org.batfish.logicblox.PredicateInfo;
import org.batfish.logicblox.ProjectFile;
import org.batfish.logicblox.QueryException;
import org.batfish.logicblox.TopologyFactExtractor;
import org.batfish.question.MultipathQuestion;
import org.batfish.question.Question;
import org.batfish.question.VerifyProgram;
import org.batfish.question.VerifyQuestion;
import org.batfish.representation.BgpNeighbor;
import org.batfish.representation.BgpProcess;
import org.batfish.representation.Configuration;
import org.batfish.representation.Edge;
import org.batfish.representation.Interface;
import org.batfish.representation.Ip;
import org.batfish.representation.IpProtocol;
import org.batfish.representation.LineAction;
import org.batfish.representation.OspfArea;
import org.batfish.representation.OspfProcess;
import org.batfish.representation.PolicyMap;
import org.batfish.representation.PolicyMapAction;
import org.batfish.representation.PolicyMapClause;
import org.batfish.representation.PolicyMapMatchRouteFilterListLine;
import org.batfish.representation.Prefix;
import org.batfish.representation.RouteFilterLine;
import org.batfish.representation.RouteFilterList;
import org.batfish.representation.Topology;
import org.batfish.representation.VendorConfiguration;
import org.batfish.representation.cisco.CiscoVendorConfiguration;
import org.batfish.util.StringFilter;
import org.batfish.util.SubRange;
import org.batfish.util.UrlZipExplorer;
import org.batfish.util.Util;
import org.batfish.z3.ConcretizerQuery;
import org.batfish.z3.FailureInconsistencyBlackHoleQuerySynthesizer;
import org.batfish.z3.MultipathInconsistencyQuerySynthesizer;
import org.batfish.z3.NodJob;
import org.batfish.z3.NodJobResult;
import org.batfish.z3.QuerySynthesizer;
import org.batfish.z3.ReachableQuerySynthesizer;
import org.batfish.z3.RoleReachabilityQuerySynthesizer;
import org.batfish.z3.RoleTransitQuerySynthesizer;
import org.batfish.z3.Synthesizer;

import com.logicblox.bloxweb.client.ServiceClientException;
import com.logicblox.connect.Workspace.Relation;
import com.microsoft.z3.Context;
import com.microsoft.z3.Z3Exception;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * This class encapsulates the main control logic for Batfish.
 */
public class Batfish implements AutoCloseable {

   /**
    * Name of the LogiQL executable block containing basic facts that are true
    * for any network
    */
   private static final String BASIC_FACTS_BLOCKNAME = "BaseFacts";

   /**
    * Name of the file in which the topology of a network is serialized
    */
   private static final String EDGES_FILENAME = "edges";

   /**
    * Name of the LogiQL data-plane predicate containing next hop information
    * for policy-routing
    */
   private static final String FIB_POLICY_ROUTE_NEXT_HOP_PREDICATE_NAME = "FibForwardPolicyRouteNextHopIp";

   /**
    * Name of the LogiQL data-plane predicate containing next hop information
    * for destination-based routing
    */
   private static final String FIB_PREDICATE_NAME = "FibNetwork";

   /**
    * Name of the file in which the destination-routing FIBs are serialized
    */
   private static final String FIBS_FILENAME = "fibs";

   /**
    * Name of the file in which the policy-routing FIBs are serialized
    */
   private static final String FIBS_POLICY_ROUTE_NEXT_HOP_FILENAME = "fibs-policy-route";

   /**
    * Name of the LogiQL predicate containing flow-sink interface tags
    */
   private static final String FLOW_SINK_PREDICATE_NAME = "FlowSinkInterface";

   /**
    * Name of the file in which derived flow-sink interface tags are serialized
    */
   private static final String FLOW_SINKS_FILENAME = "flow-sinks";

   private static final String GEN_OSPF_STARTING_IP = "10.0.0.0";

   /**
    * A byte-array containing the first 4 bytes comprising the header for a file
    * that is the output of java serialization
    */
   private static final byte[] JAVA_SERIALIZED_OBJECT_HEADER = { (byte) 0xac,
         (byte) 0xed, (byte) 0x00, (byte) 0x05 };

   private static final long JOB_POLLING_PERIOD_MS = 1000l;

   /**
    * The name of the LogiQL library for org.batfish
    */
   private static final String LB_BATFISH_LIBRARY_NAME = "libbatfish";

   /**
    * The name of the file in which LogiQL predicate type-information and
    * documentation is serialized
    */
   private static final String PREDICATE_INFO_FILENAME = "predicateInfo.object";

   /**
    * A string containing the system-specific path separator character
    */
   private static final String SEPARATOR = System.getProperty("file.separator");

   /**
    * Role name for generated stubs
    */
   private static final String STUB_ROLE = "generated_stubs";

   private static final String TESTRIG_CONFIGURATION_DIRECTORY = "configs";

   /**
    * The name of the [optional] topology file within a test-rig
    */
   private static final String TOPOLOGY_FILENAME = "topology.net";

   /**
    * The name of the LogiQL predicate containing pairs of interfaces in the
    * same LAN segment
    */
   private static final String TOPOLOGY_PREDICATE_NAME = "LanAdjacent";

   public static String flatten(String input, BatfishLogger logger,
         Settings settings) {
      JuniperCombinedParser jparser = new JuniperCombinedParser(input,
            settings.getThrowOnParserError(), settings.getThrowOnLexerError());
      ParserRuleContext jtree = parse(jparser, logger, settings);
      JuniperFlattener flattener = new JuniperFlattener();
      ParseTreeWalker walker = new ParseTreeWalker();
      walker.walk(flattener, jtree);
      return flattener.getFlattenedConfigurationText();
   }

   private static void initControlPlaneFactBins(
         Map<String, StringBuilder> factBins) {
      initFactBins(Facts.CONTROL_PLANE_FACT_COLUMN_HEADERS, factBins);
   }

   private static void initFactBins(Map<String, String> columnHeaderMap,
         Map<String, StringBuilder> factBins) {
      for (String factPredicate : columnHeaderMap.keySet()) {
         String columnHeaders = columnHeaderMap.get(factPredicate);
         String initialText = columnHeaders + "\n";
         factBins.put(factPredicate, new StringBuilder(initialText));
      }

   }

   private static void initTrafficFactBins(Map<String, StringBuilder> factBins) {
      initFactBins(Facts.TRAFFIC_FACT_COLUMN_HEADERS, factBins);
   }

   public static ParserRuleContext parse(BatfishCombinedParser<?, ?> parser,
         BatfishLogger logger, Settings settings) {
      ParserRuleContext tree;
      try {
         tree = parser.parse();
      }
      catch (BatfishException e) {
         throw new ParserBatfishException("Parser error", e);
      }
      List<String> errors = parser.getErrors();
      int numErrors = errors.size();
      if (numErrors > 0) {
         logger.error(numErrors + " ERROR(S)\n");
         for (int i = 0; i < numErrors; i++) {
            String prefix = "ERROR " + (i + 1) + ": ";
            String msg = errors.get(i);
            String prefixedMsg = Util.applyPrefix(prefix, msg);
            logger.error(prefixedMsg + "\n");
         }
         throw new ParserBatfishException("Parser error(s)");
      }
      else if (!settings.printParseTree()) {
         logger.info("OK\n");
      }
      else {
         logger.info("OK, PRINTING PARSE TREE:\n");
         logger.info(ParseTreePrettyPrinter.print(tree, parser) + "\n\n");
      }
      return tree;
   }

   public static ParserRuleContext parse(BatfishCombinedParser<?, ?> parser,
         String filename, BatfishLogger logger, Settings settings) {
      logger.info("Parsing: \"" + filename + "\"...");
      return parse(parser, logger, settings);
   }

   private List<LogicBloxFrontend> _lbFrontends;

   private BatfishLogger _logger;

   private PredicateInfo _predicateInfo;

   private Settings _settings;

   private long _timerCount;

   private File _tmpLogicDir;

   public Batfish(Settings settings) {
      _settings = settings;
      _logger = _settings.getLogger();
      _lbFrontends = new ArrayList<LogicBloxFrontend>();
      _tmpLogicDir = null;
   }

   private void addProject(LogicBloxFrontend lbFrontend) {
      _logger.info("\n*** ADDING PROJECT ***\n");
      resetTimer();
      String settingsLogicDir = _settings.getLogicDir();
      File logicDir;
      if (settingsLogicDir != null) {
         logicDir = new ProjectFile(settingsLogicDir);
      }
      else {
         logicDir = retrieveLogicDir().getAbsoluteFile();
      }
      String result = lbFrontend.addProject(logicDir, "");
      cleanupLogicDir();
      if (result != null) {
         throw new BatfishException(result + "\n");
      }
      _logger.info("SUCCESS\n");
      printElapsedTime();
   }

   private void addStaticFacts(LogicBloxFrontend lbFrontend, String blockName) {
      _logger.info("\n*** ADDING STATIC FACTS ***\n");
      resetTimer();
      _logger.info("Adding " + blockName + "....");
      String output = lbFrontend.execNamedBlock(LB_BATFISH_LIBRARY_NAME + ":"
            + blockName);
      if (output == null) {
         _logger.info("OK\n");
      }
      else {
         throw new BatfishException(output + "\n");
      }
      _logger.info("SUCCESS\n");
      printElapsedTime();
   }

   private void anonymizeConfigurations() {
      // TODO Auto-generated method stub

   }

   private void answer(String questionPath) {
      Question question = parseQuestion(questionPath);
      switch (question.getType()) {
      case MULTIPATH:
         answerMultipath((MultipathQuestion) question);
         break;
      case VERIFY:
         answerVerify((VerifyQuestion) question);
         break;
      default:
         throw new BatfishException("Unknown question type");
      }
   }

   private void answerMultipath(MultipathQuestion question) {
      String environmentName = question.getMasterEnvironment();
      Path envPath = Paths.get(_settings.getAutoBaseDir(),
            BfConsts.RELPATH_ENVIRONMENTS_DIR, environmentName);
      Path queryDir = Paths.get(_settings.getAutoBaseDir(),
            BfConsts.RELPATH_QUESTIONS_DIR, _settings.getQuestionName(),
            BfConsts.RELPATH_QUERIES_DIR);
      _settings.setMultipathInconsistencyQueryPath(queryDir.resolve(
            BfConsts.RELPATH_MULTIPATH_QUERY_PREFIX).toString());
      _settings.setNodeSetPath(envPath.resolve(BfConsts.RELPATH_ENV_NODE_SET)
            .toString());
      _settings.setDataPlaneDir(envPath
            .resolve(BfConsts.RELPATH_DATA_PLANE_DIR).toString());
      _settings.setDumpFactsDir(_settings.getTrafficFactDumpDir());
      genMultipathQueries();
      List<QuerySynthesizer> queries = new ArrayList<QuerySynthesizer>();
      Map<String, Configuration> configurations = deserializeConfigurations(_settings
            .getSerializeIndependentPath());
      Set<String> flowLines = null;
      try {
         Context ctx = new Context();
         Synthesizer dataPlane = synthesizeDataPlane(configurations, ctx);
         Map<QuerySynthesizer, NodeSet> queryNodes = new HashMap<QuerySynthesizer, NodeSet>();
         for (String node : configurations.keySet()) {
            MultipathInconsistencyQuerySynthesizer query = new MultipathInconsistencyQuerySynthesizer(
                  node);
            queries.add(query);
            NodeSet nodes = new NodeSet();
            nodes.add(node);
            queryNodes.put(query, nodes);
         }
         flowLines = computeNodOutput(dataPlane, queries, queryNodes);
      }
      catch (Z3Exception e) {
         throw new BatfishException("Error creating nod programs", e);
      }
      Map<String, StringBuilder> trafficFactBins = new LinkedHashMap<String, StringBuilder>();
      initTrafficFactBins(trafficFactBins);
      StringBuilder wSetFlowOriginate = trafficFactBins.get("SetFlowOriginate");
      for (String flowLine : flowLines) {
         wSetFlowOriginate.append(flowLine);
      }
      dumpFacts(trafficFactBins);
   }

   private void answerVerify(VerifyQuestion question) {
      Map<String, Configuration> configurations = deserializeConfigurations(_settings
            .getSerializeIndependentPath());
      VerifyProgram program = question.getProgram();
      program.execute(configurations, _logger, _settings);
      if (program.getAssertions() && !program.getUnsafe()) {
         _logger.output("No violations detected\n");
      }
   }

   /**
    * This function extracts predicate type information from the logic files. It
    * is meant only to be called during the build process, and should never be
    * executed from a jar
    */
   private void buildPredicateInfo() {
      Path logicBinDirPath = null;
      URL logicSourceURL = LogicResourceLocator.class.getProtectionDomain()
            .getCodeSource().getLocation();
      String logicSourceString = logicSourceURL.toString();
      if (logicSourceString.startsWith("onejar:")) {
         throw new BatfishException(
               "buildPredicateInfo() should never be called from within a jar");
      }
      String logicPackageResourceName = LogicResourceLocator.class.getPackage()
            .getName().replace('.', SEPARATOR.charAt(0));
      try {
         logicBinDirPath = Paths.get(LogicResourceLocator.class
               .getClassLoader().getResource(logicPackageResourceName).toURI());
      }
      catch (URISyntaxException e) {
         throw new BatfishException("Failed to resolve logic output directory",
               e);
      }
      Path logicSrcDirPath = Paths.get(_settings.getLogicSrcDir());
      final Set<Path> logicFiles = new TreeSet<Path>();
      try {
         Files.walkFileTree(logicSrcDirPath,
               new java.nio.file.SimpleFileVisitor<Path>() {
                  @Override
                  public FileVisitResult visitFile(Path file,
                        BasicFileAttributes attrs) throws IOException {
                     String name = file.getFileName().toString();
                     if (!name.equals("BaseFacts.logic")
                           && !name.endsWith("_rules.logic")
                           && !name.startsWith("service_")
                           && name.endsWith(".logic")) {
                        logicFiles.add(file);
                     }
                     return super.visitFile(file, attrs);
                  }
               });
      }
      catch (IOException e) {
         throw new BatfishException("Could not make list of logic files", e);
      }
      PredicateValueTypeMap predicateValueTypes = new PredicateValueTypeMap();
      QualifiedNameMap qualifiedNameMap = new QualifiedNameMap();
      FunctionSet functions = new FunctionSet();
      PredicateSemantics predicateSemantics = new PredicateSemantics();
      List<ParserRuleContext> trees = new ArrayList<ParserRuleContext>();
      for (Path logicFilePath : logicFiles) {
         String input = readFile(logicFilePath.toFile());
         LogiQLCombinedParser parser = new LogiQLCombinedParser(input,
               _settings.getThrowOnParserError(),
               _settings.getThrowOnLexerError());
         ParserRuleContext tree = parse(parser, logicFilePath.toString());
         trees.add(tree);
      }
      ParseTreeWalker walker = new ParseTreeWalker();
      for (ParserRuleContext tree : trees) {
         LogQLPredicateInfoExtractor extractor = new LogQLPredicateInfoExtractor(
               predicateValueTypes);
         walker.walk(extractor, tree);
      }
      for (ParserRuleContext tree : trees) {
         LogiQLPredicateInfoResolver resolver = new LogiQLPredicateInfoResolver(
               predicateValueTypes, qualifiedNameMap, functions,
               predicateSemantics);
         walker.walk(resolver, tree);
      }
      PredicateInfo predicateInfo = new PredicateInfo(predicateSemantics,
            predicateValueTypes, functions, qualifiedNameMap);
      File predicateInfoFile = logicBinDirPath.resolve(PREDICATE_INFO_FILENAME)
            .toFile();
      serializeObject(predicateInfo, predicateInfoFile);
   }

   private void cleanupLogicDir() {
      if (_tmpLogicDir != null) {
         try {
            FileUtils.deleteDirectory(_tmpLogicDir);
         }
         catch (IOException e) {
            throw new BatfishException(
                  "Error cleaning up temporary logic directory", e);
         }
         _tmpLogicDir = null;
      }
   }

   @Override
   public void close() throws Exception {
      for (LogicBloxFrontend lbFrontend : _lbFrontends) {
         // Close backend threads
         if (lbFrontend != null && lbFrontend.connected()) {
            lbFrontend.close();
         }
      }
   }

   private void computeDataPlane(LogicBloxFrontend lbFrontend) {
      _logger.info("\n*** COMPUTING DATA PLANE STRUCTURES ***\n");
      resetTimer();

      lbFrontend.initEntityTable();

      _logger.info("Retrieving flow sink information from LogicBlox...");
      FlowSinkSet flowSinks = getFlowSinkSet(lbFrontend);
      _logger.info("OK\n");

      _logger.info("Retrieving topology information from LogicBlox...");
      EdgeSet topologyEdges = getTopologyEdges(lbFrontend);
      _logger.info("OK\n");

      String fibQualifiedName = _predicateInfo.getPredicateNames().get(
            FIB_PREDICATE_NAME);
      _logger
            .info("Retrieving destination-routing FIB information from LogicBlox...");
      Relation fibNetwork = lbFrontend.queryPredicate(fibQualifiedName);
      _logger.info("OK\n");

      String fibPolicyRouteNextHopQualifiedName = _predicateInfo
            .getPredicateNames().get(FIB_POLICY_ROUTE_NEXT_HOP_PREDICATE_NAME);
      _logger
            .info("Retrieving policy-routing  FIB information from LogicBlox...");
      Relation fibPolicyRouteNextHops = lbFrontend
            .queryPredicate(fibPolicyRouteNextHopQualifiedName);
      _logger.info("OK\n");

      _logger.info("Caclulating forwarding rules...");
      FibMap fibs = getRouteForwardingRules(fibNetwork, lbFrontend);
      PolicyRouteFibNodeMap policyRouteFibNodeMap = getPolicyRouteFibNodeMap(
            fibPolicyRouteNextHops, lbFrontend);
      _logger.info("OK\n");

      Path flowSinksPath = Paths.get(_settings.getDataPlaneDir(),
            FLOW_SINKS_FILENAME);
      new File(_settings.getDataPlaneDir()).mkdirs();
      Path fibsPath = Paths.get(_settings.getDataPlaneDir(), FIBS_FILENAME);
      Path fibsPolicyRoutePath = Paths.get(_settings.getDataPlaneDir(),
            FIBS_POLICY_ROUTE_NEXT_HOP_FILENAME);
      Path edgesPath = Paths.get(_settings.getDataPlaneDir(), EDGES_FILENAME);
      _logger.info("Serializing flow sink set...");
      serializeObject(flowSinks, flowSinksPath.toFile());
      _logger.info("OK\n");
      _logger.info("Serializing fibs...");
      serializeObject(fibs, fibsPath.toFile());
      _logger.info("OK\n");
      _logger.info("Serializing policy route next hop interface map...");
      serializeObject(policyRouteFibNodeMap, fibsPolicyRoutePath.toFile());
      _logger.info("OK\n");
      _logger.info("Serializing toplogy edges...");
      serializeObject(topologyEdges, edgesPath.toFile());
      _logger.info("OK\n");

      printElapsedTime();
   }

   private Set<String> computeNodOutput(Synthesizer dataPlane,
         List<QuerySynthesizer> queries,
         Map<QuerySynthesizer, NodeSet> queryNodes) {
      Set<String> facts = new TreeSet<String>();
      int numConcurrentThreads = Runtime.getRuntime().availableProcessors();
      ExecutorService pool = Executors.newFixedThreadPool(numConcurrentThreads);
      // ExecutorService pool = Executors.newSingleThreadExecutor();
      Set<NodJob> jobs = new HashSet<NodJob>();
      for (final QuerySynthesizer query : queries) {
         NodeSet nodes = queryNodes.get(query);
         NodJob job = new NodJob(dataPlane, query, nodes);
         jobs.add(job);
      }
      List<Future<NodJobResult>> results;
      try {
         results = pool.invokeAll(jobs);
      }
      catch (InterruptedException e) {
         throw new BatfishException("Nod executor service interrupted", e);
      }
      for (Future<NodJobResult> future : results) {
         try {
            NodJobResult result = future.get();
            if (result.terminatedSuccessfully()) {
               facts.addAll(result.getFlowLines());
            }
            else {
               Throwable failureCause = result.getFailureCause();
               if (failureCause != null) {
                  throw new BatfishException("Failure running nod job",
                        failureCause);
               }
               else {
                  throw new BatfishException("Unknown failure running nod job");
               }
            }
         }
         catch (InterruptedException e) {
            throw new BatfishException("Nod job interrupted", e);
         }
         catch (ExecutionException e) {
            throw new BatfishException("Could not execute nod job", e);
         }
      }
      pool.shutdown();
      return facts;
   }

   private void concretize() {
      _logger.info("\n*** GENERATING Z3 CONCRETIZER QUERIES ***\n");
      resetTimer();
      String[] concInPaths = _settings.getConcretizerInputFilePaths();
      String[] negConcInPaths = _settings.getNegatedConcretizerInputFilePaths();
      List<ConcretizerQuery> concretizerQueries = new ArrayList<ConcretizerQuery>();
      String blacklistDstIpPath = _settings.getBlacklistDstIpPath();
      if (blacklistDstIpPath != null) {
         String blacklistDstIpFileText = readFile(new File(blacklistDstIpPath));
         String[] blacklistDstpIpStrs = blacklistDstIpFileText.split("\n");
         Set<Ip> blacklistDstIps = new TreeSet<Ip>();
         for (String blacklistDstIpStr : blacklistDstpIpStrs) {
            Ip blacklistDstIp = new Ip(blacklistDstIpStr);
            blacklistDstIps.add(blacklistDstIp);
         }
         if (blacklistDstIps.size() == 0) {
            _logger.warn("Warning: empty set of blacklisted destination ips\n");
         }
         ConcretizerQuery blacklistIpQuery = ConcretizerQuery
               .blacklistDstIpQuery(blacklistDstIps);
         concretizerQueries.add(blacklistIpQuery);
      }
      for (String concInPath : concInPaths) {
         _logger.info("Reading z3 datalog query output file: \"" + concInPath
               + "\"...");
         File queryOutputFile = new File(concInPath);
         String queryOutputStr = readFile(queryOutputFile);
         _logger.info("OK\n");

         DatalogQueryResultCombinedParser parser = new DatalogQueryResultCombinedParser(
               queryOutputStr, _settings.getThrowOnParserError(),
               _settings.getThrowOnLexerError());
         ParserRuleContext tree = parse(parser, concInPath);

         _logger.info("Computing concretizer queries...");
         ParseTreeWalker walker = new ParseTreeWalker();
         DatalogQueryResultExtractor extractor = new DatalogQueryResultExtractor(
               _settings.concretizeUnique(), false);
         walker.walk(extractor, tree);
         _logger.info("OK\n");

         List<ConcretizerQuery> currentQueries = extractor
               .getConcretizerQueries();
         if (concretizerQueries.size() == 0) {
            concretizerQueries.addAll(currentQueries);
         }
         else {
            concretizerQueries = ConcretizerQuery.crossProduct(
                  concretizerQueries, currentQueries);
         }
      }
      if (negConcInPaths != null) {
         for (String negConcInPath : negConcInPaths) {
            _logger
                  .info("Reading z3 datalog query output file (to be negated): \""
                        + negConcInPath + "\"...");
            File queryOutputFile = new File(negConcInPath);
            String queryOutputStr = readFile(queryOutputFile);
            _logger.info("OK\n");

            DatalogQueryResultCombinedParser parser = new DatalogQueryResultCombinedParser(
                  queryOutputStr, _settings.getThrowOnParserError(),
                  _settings.getThrowOnLexerError());
            ParserRuleContext tree = parse(parser, negConcInPath);

            _logger.info("Computing concretizer queries...");
            ParseTreeWalker walker = new ParseTreeWalker();
            DatalogQueryResultExtractor extractor = new DatalogQueryResultExtractor(
                  _settings.concretizeUnique(), true);
            walker.walk(extractor, tree);
            _logger.info("OK\n");

            List<ConcretizerQuery> currentQueries = extractor
                  .getConcretizerQueries();
            if (concretizerQueries.size() == 0) {
               concretizerQueries.addAll(currentQueries);
            }
            else {
               concretizerQueries = ConcretizerQuery.crossProduct(
                     concretizerQueries, currentQueries);
            }
         }
      }
      for (int i = 0; i < concretizerQueries.size(); i++) {
         ConcretizerQuery cq = concretizerQueries.get(i);
         String concQueryPath = _settings.getConcretizerOutputFilePath() + "-"
               + i + ".smt2";
         _logger.info("Writing concretizer query file: \"" + concQueryPath
               + "\"...");
         writeFile(concQueryPath, cq.getText());
         _logger.info("OK\n");
      }
      printElapsedTime();
   }

   private LogicBloxFrontend connect() {
      boolean assumedToExist = !_settings.createWorkspace();
      String workspaceMaster = _settings.getWorkspaceName();
      if (assumedToExist) {
         String jobLogicBloxHostnamePath = _settings
               .getJobLogicBloxHostnamePath();
         if (jobLogicBloxHostnamePath != null) {
            String lbHostname = readFile(new File(jobLogicBloxHostnamePath));
            _settings.setConnectBloxHost(lbHostname);
         }
      }
      else {
         String serviceLogicBloxHostname = _settings
               .getServiceLogicBloxHostname();
         if (serviceLogicBloxHostname != null) {
            _settings.setConnectBloxHost(serviceLogicBloxHostname);
         }
      }
      LogicBloxFrontend lbFrontend = null;
      try {
         lbFrontend = initFrontend(assumedToExist, workspaceMaster);
      }
      catch (LBInitializationException e) {
         throw new BatfishException("Failed to connect to LogicBlox", e);
      }
      return lbFrontend;
   }

   private Map<String, Configuration> convertConfigurations(
         Map<String, VendorConfiguration> vendorConfigurations) {
      boolean processingError = false;
      Map<String, Configuration> configurations = new TreeMap<String, Configuration>();
      _logger
            .info("\n*** CONVERTING VENDOR CONFIGURATIONS TO INDEPENDENT FORMAT ***\n");
      resetTimer();
      boolean pedanticAsError = _settings.getPedanticAsError();
      boolean pedanticRecord = _settings.getPedanticRecord();
      boolean redFlagAsError = _settings.getRedFlagAsError();
      boolean redFlagRecord = _settings.getRedFlagRecord();
      boolean unimplementedAsError = _settings.getUnimplementedAsError();
      boolean unimplementedRecord = _settings.getUnimplementedRecord();
      for (String name : vendorConfigurations.keySet()) {
         _logger.debug("Processing: \"" + name + "\"");
         VendorConfiguration vc = vendorConfigurations.get(name);
         Warnings warnings = new Warnings(pedanticAsError, pedanticRecord,
               redFlagAsError, redFlagRecord, unimplementedAsError,
               unimplementedRecord, false);
         try {
            Configuration config = vc
                  .toVendorIndependentConfiguration(warnings);
            configurations.put(name, config);
            _logger.debug(" ...OK\n");
         }
         catch (BatfishException e) {
            _logger.fatal("...CONVERSION ERROR\n");
            _logger.fatal(ExceptionUtils.getStackTrace(e));
            processingError = true;
            if (_settings.getExitOnFirstError()) {
               break;
            }
            else {
               continue;
            }
         }
         finally {
            for (String warning : warnings.getRedFlagWarnings()) {
               _logger.redflag(warning);
            }
            for (String warning : warnings.getUnimplementedWarnings()) {
               _logger.unimplemented(warning);
            }
            for (String warning : warnings.getPedanticWarnings()) {
               _logger.pedantic(warning);
            }
         }
      }
      if (processingError) {
         throw new BatfishException("Vendor conversion error(s)");
      }
      else {
         printElapsedTime();
         return configurations;
      }
   }

   public Map<String, Configuration> deserializeConfigurations(
         String serializedConfigPath) {
      _logger
            .info("\n*** DESERIALIZING VENDOR-INDEPENDENT CONFIGURATION STRUCTURES ***\n");
      resetTimer();
      Map<String, Configuration> configurations = new TreeMap<String, Configuration>();
      File dir = new File(serializedConfigPath);
      File[] serializedConfigs = dir.listFiles();
      if (serializedConfigs == null) {
         throw new BatfishException(
               "Error reading vendor-independent configs directory: \""
                     + dir.toString() + "\"");
      }
      for (File serializedConfig : serializedConfigs) {
         String name = serializedConfig.getName();
         _logger.debug("Reading config: \"" + serializedConfig + "\"");
         Object object = deserializeObject(serializedConfig);
         Configuration c = (Configuration) object;
         configurations.put(name, c);
         _logger.debug(" ...OK\n");
      }
      disableBlacklistedInterface(configurations);
      disableBlacklistedNode(configurations);
      printElapsedTime();
      return configurations;
   }

   private Object deserializeObject(File inputFile) {
      FileInputStream fis;
      Object o = null;
      ObjectInputStream ois;
      try {
         fis = new FileInputStream(inputFile);
         if (!isJavaSerializationData(inputFile)) {
            XStream xstream = new XStream(new DomDriver("UTF-8"));
            ois = xstream.createObjectInputStream(fis);
         }
         else {
            ois = new ObjectInputStream(fis);
         }
         o = ois.readObject();
         ois.close();
      }
      catch (IOException | ClassNotFoundException e) {
         throw new BatfishException("Failed to deserialize object from file: "
               + inputFile.toString(), e);
      }
      return o;
   }

   public Map<String, VendorConfiguration> deserializeVendorConfigurations(
         String serializedVendorConfigPath) {
      _logger.info("\n*** DESERIALIZING VENDOR CONFIGURATION STRUCTURES ***\n");
      resetTimer();
      Map<String, VendorConfiguration> vendorConfigurations = new TreeMap<String, VendorConfiguration>();
      File dir = new File(serializedVendorConfigPath);
      File[] serializedConfigs = dir.listFiles();
      if (serializedConfigs == null) {
         throw new BatfishException("Error reading vendor configs directory");
      }
      for (File serializedConfig : serializedConfigs) {
         String name = serializedConfig.getName();
         _logger.debug("Reading vendor config: \"" + serializedConfig + "\"");
         Object object = deserializeObject(serializedConfig);
         VendorConfiguration vc = (VendorConfiguration) object;
         vendorConfigurations.put(name, vc);
         _logger.debug("...OK\n");
      }
      printElapsedTime();
      return vendorConfigurations;
   }

   private void disableBlacklistedInterface(
         Map<String, Configuration> configurations) {
      String blacklistInterfaceString = _settings.getBlacklistInterfaceString();
      if (blacklistInterfaceString != null) {
         String[] blacklistInterfaceStringParts = blacklistInterfaceString
               .split(",");
         String blacklistInterfaceNode = blacklistInterfaceStringParts[0];
         String blacklistInterfaceName = blacklistInterfaceStringParts[1];
         Configuration c = configurations.get(blacklistInterfaceNode);
         Interface i = c.getInterfaces().get(blacklistInterfaceName);
         i.setActive(false);
      }
   }

   private void disableBlacklistedNode(Map<String, Configuration> configurations) {
      String blacklistNode = _settings.getBlacklistNode();
      if (blacklistNode != null) {
         if (!configurations.containsKey(blacklistNode)) {
            throw new BatfishException("Cannot blacklist non-existent node: "
                  + blacklistNode);
         }
         Configuration configuration = configurations.get(blacklistNode);
         for (Interface iface : configuration.getInterfaces().values()) {
            iface.setActive(false);
         }
      }
   }

   private void dumpFacts(Map<String, StringBuilder> factBins) {
      _logger.info("\n*** DUMPING FACTS ***\n");
      resetTimer();
      Path factsDir = Paths.get(_settings.getDumpFactsDir());
      try {
         Files.createDirectories(factsDir);
         for (String factsFilename : factBins.keySet()) {
            String facts = factBins.get(factsFilename).toString();
            Path factsFilePath = factsDir.resolve(factsFilename);
            _logger.info("Writing: \""
                  + factsFilePath.toAbsolutePath().toString() + "\"\n");
            FileUtils.write(factsFilePath.toFile(), facts);
         }
      }
      catch (IOException e) {
         throw new BatfishException("Failed to write fact dump file", e);
      }
      printElapsedTime();
   }

   private void dumpInterfaceDescriptions(String testRigPath, String outputPath) {
      Map<File, String> configurationData = readConfigurationFiles(testRigPath);
      Map<String, VendorConfiguration> configs = parseVendorConfigurations(configurationData);
      Map<String, VendorConfiguration> sortedConfigs = new TreeMap<String, VendorConfiguration>();
      sortedConfigs.putAll(configs);
      StringBuilder sb = new StringBuilder();
      for (VendorConfiguration vconfig : sortedConfigs.values()) {
         String node = vconfig.getHostname();
         CiscoVendorConfiguration config = null;
         try {
            config = (CiscoVendorConfiguration) vconfig;
         }
         catch (ClassCastException e) {
            continue;
         }
         Map<String, org.batfish.representation.cisco.Interface> sortedInterfaces = new TreeMap<String, org.batfish.representation.cisco.Interface>();
         sortedInterfaces.putAll(config.getInterfaces());
         for (org.batfish.representation.cisco.Interface iface : sortedInterfaces
               .values()) {
            String iname = iface.getName();
            String description = iface.getDescription();
            sb.append(node + " " + iname);
            if (description != null) {
               sb.append(" \"" + description + "\"");
            }
            sb.append("\n");
         }
      }
      String output = sb.toString();
      writeFile(outputPath, output);
   }

   private void flatten(String inputPath, String outputPath) {
      Map<File, String> configurationData = readConfigurationFiles(inputPath);
      Map<File, String> outputConfigurationData = new TreeMap<File, String>();
      File inputFolder = new File(inputPath);
      File[] configs = inputFolder.listFiles();
      if (configs == null) {
         throw new BatfishException("Error reading configs from input test rig");
      }
      try {
         Files.createDirectories(Paths.get(outputPath));
      }
      catch (IOException e) {
         throw new BatfishException(
               "Could not create output testrig directory", e);
      }

      _logger.info("\n*** FLATTENING TEST RIG ***\n");
      resetTimer();

      ExecutorService pool;
      boolean shuffle;
      if (!_settings.getSequential()) {
         int numConcurrentThreads = Runtime.getRuntime().availableProcessors();
         pool = Executors.newFixedThreadPool(numConcurrentThreads);
         shuffle = true;
      }
      else {
         pool = Executors.newSingleThreadExecutor();
         shuffle = false;
      }

      List<FlattenVendorConfigurationJob> jobs = new ArrayList<FlattenVendorConfigurationJob>();

      boolean processingError = false;
      for (File inputFile : configurationData.keySet()) {
         Warnings warnings = new Warnings(_settings.getPedanticAsError(),
               _settings.getPedanticRecord()
                     && _logger.isActive(BatfishLogger.LEVEL_PEDANTIC),
               _settings.getRedFlagAsError(), _settings.getRedFlagRecord()
                     && _logger.isActive(BatfishLogger.LEVEL_REDFLAG),
               _settings.getUnimplementedAsError(),
               _settings.getUnimplementedRecord()
                     && _logger.isActive(BatfishLogger.LEVEL_UNIMPLEMENTED),
               _settings.printParseTree());
         String fileText = configurationData.get(inputFile);
         String name = inputFile.getName();
         File outputFile = Paths.get(outputPath,
               TESTRIG_CONFIGURATION_DIRECTORY, name).toFile();
         FlattenVendorConfigurationJob job = new FlattenVendorConfigurationJob(
               _settings, fileText, inputFile, outputFile, warnings);
         jobs.add(job);
      }
      if (shuffle) {
         Collections.shuffle(jobs);
      }
      List<Future<FlattenVendorConfigurationResult>> futures = new ArrayList<Future<FlattenVendorConfigurationResult>>();
      for (FlattenVendorConfigurationJob job : jobs) {
         Future<FlattenVendorConfigurationResult> future = pool.submit(job);
         futures.add(future);
      }
      while (!futures.isEmpty()) {
         List<Future<FlattenVendorConfigurationResult>> currentFutures = new ArrayList<Future<FlattenVendorConfigurationResult>>();
         currentFutures.addAll(futures);
         for (Future<FlattenVendorConfigurationResult> future : currentFutures) {
            if (future.isDone()) {
               futures.remove(future);
               FlattenVendorConfigurationResult result = null;
               try {
                  result = future.get();
               }
               catch (InterruptedException | ExecutionException e) {
                  throw new BatfishException("Error executing parse job", e);
               }
               _logger.append(result.getHistory());
               Throwable failureCause = result.getFailureCause();
               if (failureCause != null) {
                  if (_settings.getExitOnFirstError()) {
                     throw new BatfishException("Failed parse job",
                           failureCause);
                  }
                  else {
                     processingError = true;
                     _logger.error(ExceptionUtils.getStackTrace(failureCause));
                  }
               }
               else {
                  File outputFile = result.getOutputFile();
                  String flattenedText = result.getFlattenedText();
                  outputConfigurationData.put(outputFile, flattenedText);
               }
            }
            else {
               continue;
            }
         }
         if (!futures.isEmpty()) {
            try {
               Thread.sleep(JOB_POLLING_PERIOD_MS);
            }
            catch (InterruptedException e) {
               throw new BatfishException("interrupted while sleeping", e);
            }
         }
      }
      pool.shutdown();
      if (processingError) {
         throw new BatfishException("Error flattening vendor configurations");
      }
      else {
         printElapsedTime();
      }
      for (Entry<File, String> e : outputConfigurationData.entrySet()) {
         File outputFile = e.getKey();
         String flatConfigText = e.getValue();
         String outputFileAsString = outputFile.toString();
         _logger.debug("Writing config to \"" + outputFileAsString + "\"...");
         writeFile(outputFileAsString, flatConfigText);
         _logger.debug("OK\n");
      }
      Path inputTopologyPath = Paths.get(inputPath, TOPOLOGY_FILENAME);
      Path outputTopologyPath = Paths.get(outputPath, TOPOLOGY_FILENAME);
      if (Files.isRegularFile(inputTopologyPath)) {
         String topologyFileText = readFile(inputTopologyPath.toFile());
         writeFile(outputTopologyPath.toString(), topologyFileText);
      }
   }

   private void genBlackHoleQueries() {
      _logger.info("\n*** GENERATING BLACK-HOLE QUERIES ***\n");
      resetTimer();

      String fiQueryBasePath = _settings.getBlackHoleQueryPath();
      String nodeSetPath = _settings.getNodeSetPath();

      _logger.info("Reading node set from: \"" + nodeSetPath + "\"...");
      NodeSet nodes = (NodeSet) deserializeObject(new File(nodeSetPath));
      _logger.info("OK\n");

      for (String hostname : nodes) {
         QuerySynthesizer synth = new FailureInconsistencyBlackHoleQuerySynthesizer(
               hostname);
         String queryText = synth.getQueryText();
         String fiQueryPath;
         fiQueryPath = fiQueryBasePath + "-" + hostname + ".smt2";

         _logger.info("Writing query to: \"" + fiQueryPath + "\"...");
         writeFile(fiQueryPath, queryText);
         _logger.info("OK\n");
      }

      printElapsedTime();
   }

   private void generateOspfConfigs(String topologyPath, String outputPath) {
      File topologyFilePath = new File(topologyPath);
      Topology topology = parseTopology(topologyFilePath);
      Map<String, Configuration> configs = new TreeMap<String, Configuration>();
      NodeSet allNodes = new NodeSet();
      Map<NodeInterfacePair, Set<NodeInterfacePair>> interfaceMap = new HashMap<NodeInterfacePair, Set<NodeInterfacePair>>();
      // first we collect set of all mentioned nodes, and build mapping from
      // each interface to the set of interfaces that connect to each other
      for (Edge edge : topology.getEdges()) {
         allNodes.add(edge.getNode1());
         allNodes.add(edge.getNode2());
         NodeInterfacePair interface1 = new NodeInterfacePair(edge.getNode1(),
               edge.getInt1());
         NodeInterfacePair interface2 = new NodeInterfacePair(edge.getNode2(),
               edge.getInt2());
         Set<NodeInterfacePair> interfaceSet = interfaceMap.get(interface1);
         if (interfaceSet == null) {
            interfaceSet = new HashSet<NodeInterfacePair>();
         }
         interfaceMap.put(interface1, interfaceSet);
         interfaceMap.put(interface2, interfaceSet);
         interfaceSet.add(interface1);
         interfaceSet.add(interface2);
      }
      // then we create configs for every mentioned node
      for (String hostname : allNodes) {
         Configuration config = new Configuration(hostname);
         configs.put(hostname, config);
      }
      // Now we create interfaces for each edge and record the number of
      // neighbors so we know how large to make the subnet
      long currentStartingIpAsLong = new Ip(GEN_OSPF_STARTING_IP).asLong();
      Set<Set<NodeInterfacePair>> interfaceSets = new HashSet<Set<NodeInterfacePair>>();
      interfaceSets.addAll(interfaceMap.values());
      for (Set<NodeInterfacePair> interfaceSet : interfaceSets) {
         int numInterfaces = interfaceSet.size();
         if (numInterfaces < 2) {
            throw new BatfishException(
                  "The following interface set contains less than two interfaces: "
                        + interfaceSet.toString());
         }
         int numHostBits = 0;
         for (int shiftedValue = numInterfaces - 1; shiftedValue != 0; shiftedValue >>= 1, numHostBits++) {
         }
         int subnetBits = 32 - numHostBits;
         int offset = 0;
         for (NodeInterfacePair currentPair : interfaceSet) {
            Ip ip = new Ip(currentStartingIpAsLong + offset);
            Prefix prefix = new Prefix(ip, subnetBits);
            String ifaceName = currentPair.getInterface();
            Interface iface = new Interface(ifaceName);
            iface.setPrefix(prefix);

            // dirty hack for setting bandwidth for now
            double ciscoBandwidth = org.batfish.representation.cisco.Interface
                  .getDefaultBandwidth(ifaceName);
            double juniperBandwidth = org.batfish.representation.juniper.Interface
                  .getDefaultBandwidthByName(ifaceName);
            double bandwidth = Math.min(ciscoBandwidth, juniperBandwidth);
            iface.setBandwidth(bandwidth);

            String hostname = currentPair.getHostname();
            Configuration config = configs.get(hostname);
            config.getInterfaces().put(ifaceName, iface);
            offset++;
         }
         currentStartingIpAsLong += (1 << numHostBits);
      }
      for (Configuration config : configs.values()) {
         // use cisco arbitrarily
         config.setVendor(ConfigurationFormat.CISCO);
         OspfProcess proc = new OspfProcess();
         config.setOspfProcess(proc);
         proc.setReferenceBandwidth(org.batfish.representation.cisco.OspfProcess.DEFAULT_REFERENCE_BANDWIDTH);
         long backboneArea = 0;
         OspfArea area = new OspfArea(backboneArea);
         proc.getAreas().put(backboneArea, area);
         area.getInterfaces().addAll(config.getInterfaces().values());
      }

      serializeIndependentConfigs(configs, outputPath);
   }

   private void generateStubs(String inputRole, int stubAs,
         String interfaceDescriptionRegex, String configPath) {
      Map<String, Configuration> configs = deserializeConfigurations(configPath);
      Pattern pattern = Pattern.compile(interfaceDescriptionRegex);
      Map<String, Configuration> stubConfigurations = new TreeMap<String, Configuration>();

      _logger.info("\n*** GENERATING STUBS ***\n");
      resetTimer();

      // load old node-roles to be updated at end
      RoleSet stubRoles = new RoleSet();
      stubRoles.add(STUB_ROLE);
      File nodeRolesPath = new File(_settings.getNodeRolesPath());
      _logger.info("Deserializing old node-roles mappings: \"" + nodeRolesPath
            + "\" ...");
      NodeRoleMap nodeRoles = (NodeRoleMap) deserializeObject(nodeRolesPath);
      _logger.info("OK\n");

      // create origination policy common to all stubs
      String stubOriginationPolicyName = "~STUB_ORIGINATION_POLICY~";
      PolicyMap stubOriginationPolicy = new PolicyMap(stubOriginationPolicyName);
      PolicyMapClause clause = new PolicyMapClause();
      stubOriginationPolicy.getClauses().add(clause);
      String stubOriginationRouteFilterListName = "~STUB_ORIGINATION_ROUTE_FILTER~";
      RouteFilterList rf = new RouteFilterList(
            stubOriginationRouteFilterListName);
      RouteFilterLine rfl = new RouteFilterLine(LineAction.ACCEPT, Prefix.ZERO,
            new SubRange(0, 0));
      rf.addLine(rfl);
      PolicyMapMatchRouteFilterListLine matchLine = new PolicyMapMatchRouteFilterListLine(
            Collections.singleton(rf));
      clause.getMatchLines().add(matchLine);
      clause.setAction(PolicyMapAction.PERMIT);

      // create flow sink interface common to all stubs
      String flowSinkName = "TenGibabitEthernet100/100";
      Interface flowSink = new Interface(flowSinkName);
      flowSink.setPrefix(Prefix.ZERO);
      flowSink.setActive(true);
      flowSink.setBandwidth(10E9d);

      Set<String> skipWarningNodes = new HashSet<String>();

      for (Configuration config : configs.values()) {
         if (!config.getRoles().contains(inputRole)) {
            continue;
         }
         for (BgpNeighbor neighbor : config.getBgpProcess().getNeighbors()
               .values()) {
            if (!neighbor.getRemoteAs().equals(stubAs)) {
               continue;
            }
            Prefix neighborPrefix = neighbor.getPrefix();
            if (neighborPrefix.getPrefixLength() != 32) {
               throw new BatfishException(
                     "do not currently handle generating stubs based on dynamic bgp sessions");
            }
            Ip neighborAddress = neighborPrefix.getAddress();
            int edgeAs = neighbor.getLocalAs();
            /*
             * Now that we have the ip address of the stub, we want to find the
             * interface that connects to it. We will extract the hostname for
             * the stub from the description of this interface using the
             * supplied regex.
             */
            boolean found = false;
            for (Interface iface : config.getInterfaces().values()) {
               Prefix prefix = iface.getPrefix();
               if (prefix == null || !prefix.contains(neighborAddress)) {
                  continue;
               }
               // the neighbor address falls within the network assigned to this
               // interface, so now we check the description
               String description = iface.getDescription();
               Matcher matcher = pattern.matcher(description);
               if (matcher.find()) {
                  String hostname = matcher.group(1);
                  if (configs.containsKey(hostname)) {
                     Configuration duplicateConfig = configs.get(hostname);
                     if (!duplicateConfig.getRoles().contains(STUB_ROLE)
                           || duplicateConfig.getRoles().size() != 1) {
                        throw new BatfishException(
                              "A non-generated node with hostname: \""
                                    + hostname
                                    + "\" already exists in network under analysis");
                     }
                     else {
                        if (!skipWarningNodes.contains(hostname)) {
                           _logger
                                 .warn("WARNING: Overwriting previously generated node: \""
                                       + hostname + "\"\n");
                           skipWarningNodes.add(hostname);
                        }
                     }
                  }
                  found = true;
                  Configuration stub = stubConfigurations.get(hostname);

                  // create stub if it doesn't exist yet
                  if (stub == null) {
                     stub = new Configuration(hostname);
                     stubConfigurations.put(hostname, stub);
                     stub.getInterfaces().put(flowSinkName, flowSink);
                     stub.setBgpProcess(new BgpProcess());
                     stub.getPolicyMaps().put(stubOriginationPolicyName,
                           stubOriginationPolicy);
                     stub.getRouteFilterLists().put(
                           stubOriginationRouteFilterListName, rf);
                     stub.setVendor(ConfigurationFormat.CISCO);
                     stub.setRoles(stubRoles);
                     nodeRoles.put(hostname, stubRoles);
                  }

                  // create interface that will on which peering will occur
                  Map<String, Interface> stubInterfaces = stub.getInterfaces();
                  String stubInterfaceName = "TenGigabitEthernet0/"
                        + (stubInterfaces.size() - 1);
                  Interface stubInterface = new Interface(stubInterfaceName);
                  stubInterfaces.put(stubInterfaceName, stubInterface);
                  stubInterface.setPrefix(new Prefix(neighborAddress, prefix
                        .getPrefixLength()));
                  stubInterface.setActive(true);
                  stubInterface.setBandwidth(10E9d);

                  // create neighbor within bgp process
                  BgpNeighbor edgeNeighbor = new BgpNeighbor(prefix);
                  edgeNeighbor.getOriginationPolicies().add(
                        stubOriginationPolicy);
                  edgeNeighbor.setRemoteAs(edgeAs);
                  edgeNeighbor.setLocalAs(stubAs);
                  edgeNeighbor.setSendCommunity(true);
                  edgeNeighbor.setDefaultMetric(0);
                  stub.getBgpProcess().getNeighbors()
                        .put(edgeNeighbor.getPrefix(), edgeNeighbor);
                  break;
               }
               else {
                  throw new BatfishException(
                        "Unable to derive stub hostname from interface description: \""
                              + description + "\" using regex: \""
                              + interfaceDescriptionRegex + "\"");
               }
            }
            if (!found) {
               throw new BatfishException(
                     "Could not determine stub hostname corresponding to ip: \""
                           + neighborAddress.toString()
                           + "\" listed as neighbor on router: \""
                           + config.getHostname() + "\"");
            }
         }
      }
      // write updated node-roles mappings to disk
      _logger.info("Serializing updated node-roles mappings: \""
            + nodeRolesPath + "\" ...");
      serializeObject(nodeRoles, nodeRolesPath);
      _logger.info("OK\n");
      printElapsedTime();

      // write stubs to disk
      serializeIndependentConfigs(stubConfigurations, configPath);

   }

   private void genMultipathQueries() {
      _logger.info("\n*** GENERATING MULTIPATH-INCONSISTENCY QUERIES ***\n");
      resetTimer();

      String mpiQueryBasePath = _settings.getMultipathInconsistencyQueryPath();
      String nodeSetPath = _settings.getNodeSetPath();
      String nodeSetTextPath = nodeSetPath + ".txt";

      _logger.info("Reading node set from: \"" + nodeSetPath + "\"...");
      NodeSet nodes = (NodeSet) deserializeObject(new File(nodeSetPath));
      _logger.info("OK\n");

      for (String hostname : nodes) {
         QuerySynthesizer synth = new MultipathInconsistencyQuerySynthesizer(
               hostname);
         String queryText = synth.getQueryText();
         String mpiQueryPath = mpiQueryBasePath + "-" + hostname + ".smt2";
         _logger.info("Writing query to: \"" + mpiQueryPath + "\"...");
         writeFile(mpiQueryPath, queryText);
         _logger.info("OK\n");
      }

      _logger.info("Writing node lines for next stage...");
      StringBuilder sb = new StringBuilder();
      for (String node : nodes) {
         sb.append(node + "\n");
      }
      writeFile(nodeSetTextPath, sb.toString());
      _logger.info("OK\n");

      printElapsedTime();
   }

   private void genReachableQueries() {
      _logger.info("\n*** GENERATING REACHABLE QUERIES ***\n");
      resetTimer();

      String queryBasePath = _settings.getReachableQueryPath();
      String nodeSetPath = _settings.getNodeSetPath();
      String acceptNode = _settings.getAcceptNode();
      String blacklistedNode = _settings.getBlacklistNode();
      _logger.info("Reading node set from: \"" + nodeSetPath + "\"...");
      NodeSet nodes = (NodeSet) deserializeObject(new File(nodeSetPath));
      _logger.info("OK\n");

      for (String hostname : nodes) {
         if (hostname.equals(acceptNode) || hostname.equals(blacklistedNode)) {
            continue;
         }
         QuerySynthesizer synth = new ReachableQuerySynthesizer(hostname,
               acceptNode);
         String queryText = synth.getQueryText();
         String queryPath;
         queryPath = queryBasePath + "-" + hostname + ".smt2";

         _logger.info("Writing query to: \"" + queryPath + "\"...");
         writeFile(queryPath, queryText);
         _logger.info("OK\n");
      }

      printElapsedTime();
   }

   private void genRoleReachabilityQueries() {
      _logger.info("\n*** GENERATING NODE-TO-ROLE QUERIES ***\n");
      resetTimer();

      String queryBasePath = _settings.getRoleReachabilityQueryPath();
      String nodeSetPath = _settings.getNodeSetPath();
      String nodeSetTextPath = nodeSetPath + ".txt";
      String roleSetTextPath = _settings.getRoleSetPath();
      String nodeRolesPath = _settings.getNodeRolesPath();
      String iterationsPath = nodeRolesPath + ".iterations";

      _logger.info("Reading node set from: \"" + nodeSetPath + "\"...");
      NodeSet nodes = (NodeSet) deserializeObject(new File(nodeSetPath));
      _logger.info("OK\n");

      _logger.info("Reading node roles from: \"" + nodeRolesPath + "\"...");
      NodeRoleMap nodeRoles = (NodeRoleMap) deserializeObject(new File(
            nodeRolesPath));
      _logger.info("OK\n");

      RoleNodeMap roleNodes = nodeRoles.toRoleNodeMap();

      for (String hostname : nodes) {
         for (String role : roleNodes.keySet()) {
            QuerySynthesizer synth = new RoleReachabilityQuerySynthesizer(
                  hostname, role);
            String queryText = synth.getQueryText();
            String queryPath = queryBasePath + "-" + hostname + "-" + role
                  + ".smt2";
            _logger.info("Writing query to: \"" + queryPath + "\"...");
            writeFile(queryPath, queryText);
            _logger.info("OK\n");
         }
      }

      _logger.info("Writing node lines for next stage...");
      StringBuilder sbNodes = new StringBuilder();
      for (String node : nodes) {
         sbNodes.append(node + "\n");
      }
      writeFile(nodeSetTextPath, sbNodes.toString());
      _logger.info("OK\n");

      StringBuilder sbRoles = new StringBuilder();
      _logger.info("Writing role lines for next stage...");
      sbRoles = new StringBuilder();
      for (String role : roleNodes.keySet()) {
         sbRoles.append(role + "\n");
      }
      writeFile(roleSetTextPath, sbRoles.toString());
      _logger.info("OK\n");

      _logger
            .info("Writing role-node-role iteration ordering lines for concretizer stage...");
      StringBuilder sbIterations = new StringBuilder();
      for (Entry<String, NodeSet> roleNodeEntry : roleNodes.entrySet()) {
         String transmittingRole = roleNodeEntry.getKey();
         NodeSet transmittingNodes = roleNodeEntry.getValue();
         if (transmittingNodes.size() < 2) {
            continue;
         }
         String[] tNodeArray = transmittingNodes.toArray(new String[] {});
         String masterNode = tNodeArray[0];
         for (int i = 1; i < tNodeArray.length; i++) {
            String slaveNode = tNodeArray[i];
            for (String receivingRole : roleNodes.keySet()) {
               String iterationLine = transmittingRole + ":" + masterNode + ":"
                     + slaveNode + ":" + receivingRole + "\n";
               sbIterations.append(iterationLine);
            }
         }
      }
      writeFile(iterationsPath, sbIterations.toString());
      _logger.info("OK\n");

      printElapsedTime();
   }

   private void genRoleTransitQueries() {
      _logger.info("\n*** GENERATING ROLE-TO-NODE QUERIES ***\n");
      resetTimer();

      String queryBasePath = _settings.getRoleTransitQueryPath();
      String nodeSetPath = _settings.getNodeSetPath();
      String nodeSetTextPath = nodeSetPath + ".txt";
      String roleSetTextPath = _settings.getRoleSetPath();
      String nodeRolesPath = _settings.getNodeRolesPath();
      String roleNodesPath = _settings.getRoleNodesPath();
      String iterationsPath = nodeRolesPath + ".rtiterations";
      String constraintsIterationsPath = nodeRolesPath
            + ".rtconstraintsiterations";

      _logger.info("Reading node set from: \"" + nodeSetPath + "\"...");
      NodeSet nodes = (NodeSet) deserializeObject(new File(nodeSetPath));
      _logger.info("OK\n");

      _logger.info("Reading node roles from: \"" + nodeRolesPath + "\"...");
      NodeRoleMap nodeRoles = (NodeRoleMap) deserializeObject(new File(
            nodeRolesPath));
      _logger.info("OK\n");

      RoleNodeMap roleNodes = nodeRoles.toRoleNodeMap();

      for (Entry<String, NodeSet> sourceEntry : roleNodes.entrySet()) {
         String sourceRole = sourceEntry.getKey();
         for (Entry<String, NodeSet> transitEntry : roleNodes.entrySet()) {
            String transitRole = transitEntry.getKey();
            if (transitRole.equals(sourceRole)) {
               continue;
            }
            NodeSet transitNodes = transitEntry.getValue();
            for (String transitNode : transitNodes) {
               QuerySynthesizer synth = new RoleTransitQuerySynthesizer(
                     sourceRole, transitNode);
               String queryText = synth.getQueryText();
               String queryPath = queryBasePath + "-" + transitNode + "-"
                     + sourceRole + ".smt2";
               _logger.info("Writing query to: \"" + queryPath + "\"...");
               writeFile(queryPath, queryText);
               _logger.info("OK\n");
            }
         }
      }

      _logger.info("Writing node lines for next stage...");
      StringBuilder sbNodes = new StringBuilder();
      for (String node : nodes) {
         sbNodes.append(node + "\n");
      }
      writeFile(nodeSetTextPath, sbNodes.toString());
      _logger.info("OK\n");

      StringBuilder sbRoles = new StringBuilder();
      _logger.info("Writing role lines for next stage...");
      sbRoles = new StringBuilder();
      for (String role : roleNodes.keySet()) {
         sbRoles.append(role + "\n");
      }
      writeFile(roleSetTextPath, sbRoles.toString());
      _logger.info("OK\n");

      // not actually sure if this is necessary
      StringBuilder sbRoleNodes = new StringBuilder();
      _logger.info("Writing role-node mappings for concretizer stage...");
      sbRoleNodes = new StringBuilder();
      for (Entry<String, NodeSet> e : roleNodes.entrySet()) {
         String role = e.getKey();
         NodeSet currentNodes = e.getValue();
         sbRoleNodes.append(role + ":");
         for (String node : currentNodes) {
            sbRoleNodes.append(node + ",");
         }
         sbRoleNodes.append(role + "\n");
      }
      writeFile(roleNodesPath, sbRoleNodes.toString());

      _logger
            .info("Writing transitrole-transitnode-sourcerole iteration ordering lines for constraints stage...");
      StringBuilder sbConstraintsIterations = new StringBuilder();
      for (Entry<String, NodeSet> roleNodeEntry : roleNodes.entrySet()) {
         String transitRole = roleNodeEntry.getKey();
         NodeSet transitNodes = roleNodeEntry.getValue();
         if (transitNodes.size() < 2) {
            continue;
         }
         for (String sourceRole : roleNodes.keySet()) {
            if (sourceRole.equals(transitRole)) {
               continue;
            }
            for (String transitNode : transitNodes) {
               String iterationLine = transitRole + ":" + transitNode + ":"
                     + sourceRole + "\n";
               sbConstraintsIterations.append(iterationLine);
            }
         }
      }
      writeFile(constraintsIterationsPath, sbConstraintsIterations.toString());
      _logger.info("OK\n");

      _logger
            .info("Writing transitrole-master-slave-sourcerole iteration ordering lines for concretizer stage...");
      StringBuilder sbIterations = new StringBuilder();
      for (Entry<String, NodeSet> roleNodeEntry : roleNodes.entrySet()) {
         String transitRole = roleNodeEntry.getKey();
         NodeSet transitNodes = roleNodeEntry.getValue();
         if (transitNodes.size() < 2) {
            continue;
         }
         String[] tNodeArray = transitNodes.toArray(new String[] {});
         String masterNode = tNodeArray[0];
         for (int i = 1; i < tNodeArray.length; i++) {
            String slaveNode = tNodeArray[i];
            for (String sourceRole : roleNodes.keySet()) {
               if (sourceRole.equals(transitRole)) {
                  continue;
               }
               String iterationLine = transitRole + ":" + masterNode + ":"
                     + slaveNode + ":" + sourceRole + "\n";
               sbIterations.append(iterationLine);
            }
         }
      }
      writeFile(iterationsPath, sbIterations.toString());
      _logger.info("OK\n");

      printElapsedTime();
   }

   private void genZ3(Map<String, Configuration> configurations) {
      _logger.info("\n*** GENERATING Z3 LOGIC ***\n");
      resetTimer();

      String outputPath = _settings.getZ3File();
      if (outputPath == null) {
         throw new BatfishException("Need to specify output path for z3 logic");
      }
      String nodeSetPath = _settings.getNodeSetPath();
      if (nodeSetPath == null) {
         throw new BatfishException(
               "Need to specify output path for serialized set of nodes in environment");
      }

      Path flowSinkSetPath = Paths.get(_settings.getDataPlaneDir(),
            FLOW_SINKS_FILENAME);
      Path fibsPath = Paths.get(_settings.getDataPlaneDir(), FIBS_FILENAME);
      Path prFibsPath = Paths.get(_settings.getDataPlaneDir(),
            FIBS_POLICY_ROUTE_NEXT_HOP_FILENAME);
      Path edgesPath = Paths.get(_settings.getDataPlaneDir(), EDGES_FILENAME);

      _logger.info("Deserializing flow sink interface set: \""
            + flowSinkSetPath.toString() + "\"...");
      FlowSinkSet flowSinks = (FlowSinkSet) deserializeObject(flowSinkSetPath
            .toFile());
      _logger.info("OK\n");

      _logger.info("Deserializing destination route fibs: \""
            + fibsPath.toString() + "\"...");
      FibMap fibs = (FibMap) deserializeObject(fibsPath.toFile());
      _logger.info("OK\n");

      _logger.info("Deserializing policy route fibs: \""
            + prFibsPath.toString() + "\"...");
      PolicyRouteFibNodeMap prFibs = (PolicyRouteFibNodeMap) deserializeObject(prFibsPath
            .toFile());
      _logger.info("OK\n");

      _logger.info("Deserializing toplogy edges: \"" + edgesPath.toString()
            + "\"...");
      EdgeSet topologyEdges = (EdgeSet) deserializeObject(edgesPath.toFile());
      _logger.info("OK\n");

      _logger.info("Synthesizing Z3 logic...");
      Synthesizer s = new Synthesizer(configurations, fibs, prFibs,
            topologyEdges, _settings.getSimplify(), flowSinks);
      String result = s.synthesize();
      List<String> warnings = s.getWarnings();
      int numWarnings = warnings.size();
      if (numWarnings == 0) {
         _logger.info("OK\n");
      }
      else {
         for (String warning : warnings) {
            _logger.warn(warning);
         }
      }

      _logger.info("Writing Z3 logic: \"" + outputPath + "\"...");
      File z3Out = new File(outputPath);
      z3Out.delete();
      writeFile(outputPath, result);
      _logger.info("OK\n");

      _logger.info("Serializing node set: \"" + nodeSetPath + "\"...");
      NodeSet nodeSet = s.getNodeSet();
      serializeObject(nodeSet, new File(nodeSetPath));
      _logger.info("OK\n");

      printElapsedTime();
   }

   public Map<String, Configuration> getConfigurations(
         String serializedVendorConfigPath) {
      Map<String, VendorConfiguration> vendorConfigurations = deserializeVendorConfigurations(serializedVendorConfigPath);
      Map<String, Configuration> configurations = convertConfigurations(vendorConfigurations);
      return configurations;
   }

   private double getElapsedTime(long beforeTime) {
      long difference = System.currentTimeMillis() - beforeTime;
      double seconds = difference / 1000d;
      return seconds;
   }

   // private Set<Path> getMultipathQueryPaths(Path directory) {
   // Set<Path> queryPaths = new TreeSet<Path>();
   // try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(
   // directory, new DirectoryStream.Filter<Path>() {
   // @Override
   // public boolean accept(Path path) throws IOException {
   // String filename = path.getFileName().toString();
   // return filename
   // .startsWith(BfConsts.RELPATH_MULTIPATH_QUERY_PREFIX)
   // && filename.endsWith(".smt2");
   // }
   // })) {
   // for (Path path : directoryStream) {
   // queryPaths.add(path);
   // }
   // }
   // catch (IOException ex) {
   // throw new BatfishException(
   // "Could not list files in queries directory", ex);
   // }
   // return queryPaths;
   // }

   private FlowSinkSet getFlowSinkSet(LogicBloxFrontend lbFrontend) {
      FlowSinkSet flowSinks = new FlowSinkSet();
      String qualifiedName = _predicateInfo.getPredicateNames().get(
            FLOW_SINK_PREDICATE_NAME);
      Relation flowSinkRelation = lbFrontend.queryPredicate(qualifiedName);
      List<String> nodes = new ArrayList<String>();
      lbFrontend.fillColumn(LBValueType.ENTITY_REF_STRING, nodes,
            flowSinkRelation.getColumns().get(0));
      List<String> interfaces = new ArrayList<String>();
      lbFrontend.fillColumn(LBValueType.ENTITY_REF_STRING, interfaces,
            flowSinkRelation.getColumns().get(1));
      for (int i = 0; i < nodes.size(); i++) {
         String node = nodes.get(i);
         String iface = interfaces.get(i);
         FlowSinkInterface f = new FlowSinkInterface(node, iface);
         flowSinks.add(f);
      }
      return flowSinks;
   }

   private List<String> getHelpPredicates(Map<String, String> predicateSemantics) {
      Set<String> helpPredicateSet = new LinkedHashSet<String>();
      _settings.getHelpPredicates();
      if (_settings.getHelpPredicates() == null) {
         helpPredicateSet.addAll(predicateSemantics.keySet());
      }
      else {
         helpPredicateSet.addAll(_settings.getHelpPredicates());
      }
      List<String> helpPredicates = new ArrayList<String>();
      helpPredicates.addAll(helpPredicateSet);
      Collections.sort(helpPredicates);
      return helpPredicates;
   }

   private PolicyRouteFibNodeMap getPolicyRouteFibNodeMap(
         Relation fibPolicyRouteNextHops, LogicBloxFrontend lbFrontend) {
      PolicyRouteFibNodeMap nodeMap = new PolicyRouteFibNodeMap();
      List<String> nodeList = new ArrayList<String>();
      lbFrontend.fillColumn(LBValueType.ENTITY_REF_STRING, nodeList,
            fibPolicyRouteNextHops.getColumns().get(0));
      List<String> ipList = new ArrayList<String>();
      lbFrontend.fillColumn(LBValueType.ENTITY_REF_IP, ipList,
            fibPolicyRouteNextHops.getColumns().get(1));
      List<String> outInterfaces = new ArrayList<String>();
      lbFrontend.fillColumn(LBValueType.ENTITY_REF_STRING, outInterfaces,
            fibPolicyRouteNextHops.getColumns().get(2));
      List<String> inNodes = new ArrayList<String>();
      lbFrontend.fillColumn(LBValueType.ENTITY_REF_STRING, inNodes,
            fibPolicyRouteNextHops.getColumns().get(3));
      List<String> inInterfaces = new ArrayList<String>();
      lbFrontend.fillColumn(LBValueType.ENTITY_REF_STRING, inInterfaces,
            fibPolicyRouteNextHops.getColumns().get(4));
      int size = nodeList.size();
      for (int i = 0; i < size; i++) {
         String nodeOut = nodeList.get(i);
         String nodeIn = inNodes.get(i);
         Ip ip = new Ip(ipList.get(i));
         String ifaceOut = outInterfaces.get(i);
         String ifaceIn = inInterfaces.get(i);
         PolicyRouteFibIpMap ipMap = nodeMap.get(nodeOut);
         if (ipMap == null) {
            ipMap = new PolicyRouteFibIpMap();
            nodeMap.put(nodeOut, ipMap);
         }
         EdgeSet edges = ipMap.get(ip);
         if (edges == null) {
            edges = new EdgeSet();
            ipMap.put(ip, edges);
         }
         Edge newEdge = new Edge(nodeOut, ifaceOut, nodeIn, ifaceIn);
         edges.add(newEdge);
      }
      return nodeMap;
   }

   public PredicateInfo getPredicateInfo(Map<String, String> logicFiles) {
      // Get predicate semantics from rules file
      _logger.info("\n*** PARSING PREDICATE INFO ***\n");
      resetTimer();
      String predicateInfoPath = getPredicateInfoPath();
      PredicateInfo predicateInfo = (PredicateInfo) deserializeObject(new File(
            predicateInfoPath));
      printElapsedTime();
      return predicateInfo;
   }

   private String getPredicateInfoPath() {
      File logicDir = retrieveLogicDir();
      return Paths.get(logicDir.toString(), PREDICATE_INFO_FILENAME).toString();
   }

   private FibMap getRouteForwardingRules(Relation fibNetworkForward,
         LogicBloxFrontend lbFrontend) {
      FibMap fibs = new FibMap();
      List<String> nameList = new ArrayList<String>();
      lbFrontend.fillColumn(LBValueType.ENTITY_REF_STRING, nameList,
            fibNetworkForward.getColumns().get(0));
      List<String> networkList = new ArrayList<String>();
      lbFrontend.fillColumn(LBValueType.ENTITY_INDEX_NETWORK, networkList,
            fibNetworkForward.getColumns().get(1));
      List<String> interfaceList = new ArrayList<String>();
      lbFrontend.fillColumn(LBValueType.ENTITY_REF_STRING, interfaceList,
            fibNetworkForward.getColumns().get(2));
      List<String> nextHopList = new ArrayList<String>();
      lbFrontend.fillColumn(LBValueType.ENTITY_REF_STRING, nextHopList,
            fibNetworkForward.getColumns().get(3));
      List<String> nextHopIntList = new ArrayList<String>();
      lbFrontend.fillColumn(LBValueType.ENTITY_REF_STRING, nextHopIntList,
            fibNetworkForward.getColumns().get(4));

      String currentHostname = "";
      Map<String, Integer> startIndices = new HashMap<String, Integer>();
      Map<String, Integer> endIndices = new HashMap<String, Integer>();
      for (int i = 0; i < nameList.size(); i++) {
         String currentRowHostname = nameList.get(i);
         if (!currentHostname.equals(currentRowHostname)) {
            if (i > 0) {
               endIndices.put(currentHostname, i - 1);
            }
            currentHostname = currentRowHostname;
            startIndices.put(currentHostname, i);
         }
      }
      endIndices.put(currentHostname, nameList.size() - 1);
      for (String hostname : startIndices.keySet()) {
         FibSet fibRows = new FibSet();
         fibs.put(hostname, fibRows);
         int startIndex = startIndices.get(hostname);
         int endIndex = endIndices.get(hostname);
         for (int i = startIndex; i <= endIndex; i++) {
            String networkStr = networkList.get(i);
            Prefix prefix = new Prefix(networkStr);
            String iface = interfaceList.get(i);
            String nextHop = nextHopList.get(i);
            String nextHopInt = nextHopIntList.get(i);
            fibRows.add(new FibRow(prefix, iface, nextHop, nextHopInt));
         }
      }
      return fibs;
   }

   private Map<String, String> getSemanticsFiles() {
      final Map<String, String> semanticsFiles = new HashMap<String, String>();
      File logicDirFile = retrieveLogicDir();
      FileVisitor<Path> visitor = new SimpleFileVisitor<Path>() {
         @Override
         public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
               throws IOException {
            String pathString = file.toString();
            if (pathString.endsWith(".semantics")) {
               String contents = FileUtils.readFileToString(file.toFile());
               semanticsFiles.put(pathString, contents);
            }
            return super.visitFile(file, attrs);
         }
      };

      try {
         Files.walkFileTree(Paths.get(logicDirFile.getAbsolutePath()), visitor);
      }
      catch (IOException e) {
         e.printStackTrace();
      }
      cleanupLogicDir();
      return semanticsFiles;
   }

   public EdgeSet getTopologyEdges(LogicBloxFrontend lbFrontend) {
      EdgeSet edges = new EdgeSet();
      String qualifiedName = _predicateInfo.getPredicateNames().get(
            TOPOLOGY_PREDICATE_NAME);
      Relation topologyRelation = lbFrontend.queryPredicate(qualifiedName);
      List<String> fromRouters = new ArrayList<String>();
      lbFrontend.fillColumn(LBValueType.ENTITY_REF_STRING, fromRouters,
            topologyRelation.getColumns().get(0));
      List<String> fromInterfaces = new ArrayList<String>();
      lbFrontend.fillColumn(LBValueType.ENTITY_REF_STRING, fromInterfaces,
            topologyRelation.getColumns().get(1));
      List<String> toRouters = new ArrayList<String>();
      lbFrontend.fillColumn(LBValueType.ENTITY_REF_STRING, toRouters,
            topologyRelation.getColumns().get(2));
      List<String> toInterfaces = new ArrayList<String>();
      lbFrontend.fillColumn(LBValueType.ENTITY_REF_STRING, toInterfaces,
            topologyRelation.getColumns().get(3));
      for (int i = 0; i < fromRouters.size(); i++) {
         if (Util.isLoopback(fromInterfaces.get(i))
               || Util.isLoopback(toInterfaces.get(i))) {
            continue;
         }
         Edge newEdge = new Edge(fromRouters.get(i), fromInterfaces.get(i),
               toRouters.get(i), toInterfaces.get(i));
         edges.add(newEdge);
      }
      return edges;
   }

   private void histogram(String testRigPath) {
      Map<File, String> configurationData = readConfigurationFiles(testRigPath);
      Map<String, VendorConfiguration> vendorConfigurations = parseVendorConfigurations(configurationData);
      _logger.info("Building feature histogram...");
      MultiSet<String> histogram = new TreeMultiSet<String>();
      for (VendorConfiguration vc : vendorConfigurations.values()) {
         Set<String> unimplementedFeatures = vc.getUnimplementedFeatures();
         histogram.add(unimplementedFeatures);
      }
      _logger.info("OK\n");
      for (String feature : histogram.elements()) {
         int count = histogram.count(feature);
         _logger.output(feature + ": " + count + "\n");
      }
   }

   public LogicBloxFrontend initFrontend(boolean assumedToExist,
         String workspace) throws LBInitializationException {
      _logger.info("\n*** STARTING CONNECTBLOX SESSION ***\n");
      resetTimer();
      LogicBloxFrontend lbFrontend = new LogicBloxFrontend(
            _settings.getConnectBloxHost(), _settings.getConnectBloxPort(),
            _settings.getLbWebPort(), _settings.getLbWebAdminPort(), workspace,
            assumedToExist, _logger);
      lbFrontend.initialize();
      if (!lbFrontend.connected()) {
         throw new BatfishException(
               "Error connecting to ConnectBlox service. Please make sure service is running and try again.");
      }
      _logger.info("SUCCESS\n");
      printElapsedTime();
      _lbFrontends.add(lbFrontend);
      return lbFrontend;

   }

   private boolean isJavaSerializationData(File inputFile) {
      try (FileInputStream i = new FileInputStream(inputFile)) {
         int headerLength = JAVA_SERIALIZED_OBJECT_HEADER.length;
         byte[] headerBytes = new byte[headerLength];
         int result = i.read(headerBytes, 0, headerLength);
         if (result != headerLength) {
            throw new BatfishException("Read wrong number of bytes");
         }
         return Arrays.equals(headerBytes, JAVA_SERIALIZED_OBJECT_HEADER);
      }
      catch (IOException e) {
         throw new BatfishException("Could not read header from file: "
               + inputFile.toString(), e);
      }
   }

   private ParserRuleContext parse(BatfishCombinedParser<?, ?> parser) {
      return parse(parser, _logger, _settings);
   }

   private ParserRuleContext parse(BatfishCombinedParser<?, ?> parser,
         String filename) {
      _logger.info("Parsing: \"" + filename + "\"...");
      return parse(parser);
   }

   private void parseFlowsFromConstraints(StringBuilder sb,
         RoleNodeMap roleNodes) {
      Path flowConstraintsDir = Paths.get(_settings.getFlowPath());
      File[] constraintsFiles = flowConstraintsDir.toFile().listFiles(
            new FilenameFilter() {
               @Override
               public boolean accept(File dir, String filename) {
                  return filename.matches(".*-concrete-.*.smt2.out");
               }
            });
      if (constraintsFiles == null) {
         throw new BatfishException("Error reading flow constraints directory");
      }
      for (File constraintsFile : constraintsFiles) {
         String flowConstraintsText = readFile(constraintsFile);
         ConcretizerQueryResultCombinedParser parser = new ConcretizerQueryResultCombinedParser(
               flowConstraintsText, _settings.getThrowOnParserError(),
               _settings.getThrowOnLexerError());
         ParserRuleContext tree = parse(parser, constraintsFile.toString());
         ParseTreeWalker walker = new ParseTreeWalker();
         ConcretizerQueryResultExtractor extractor = new ConcretizerQueryResultExtractor();
         walker.walk(extractor, tree);
         String id = extractor.getId();
         if (id == null) {
            continue;
         }
         Map<String, Long> constraints = extractor.getConstraints();
         long src_ip = 0;
         long dst_ip = 0;
         long src_port = 0;
         long dst_port = 0;
         long protocol = IpProtocol.IP.number();
         for (String varName : constraints.keySet()) {
            Long value = constraints.get(varName);
            switch (varName) {
            case Synthesizer.SRC_IP_VAR:
               src_ip = value;
               break;

            case Synthesizer.DST_IP_VAR:
               dst_ip = value;
               break;

            case Synthesizer.SRC_PORT_VAR:
               src_port = value;
               break;

            case Synthesizer.DST_PORT_VAR:
               dst_port = value;
               break;

            case Synthesizer.IP_PROTOCOL_VAR:
               protocol = value;
               break;

            default:
               throw new Error("invalid variable name");
            }
         }
         // TODO: cleanup dirty hack
         if (roleNodes != null) {
            // id is role
            NodeSet nodes = roleNodes.get(id);
            for (String node : nodes) {
               String line = node + "|" + src_ip + "|" + dst_ip + "|"
                     + src_port + "|" + dst_port + "|" + protocol + "\n";
               sb.append(line);
            }
         }
         else {
            String node = id;
            String line = node + "|" + src_ip + "|" + dst_ip + "|" + src_port
                  + "|" + dst_port + "|" + protocol + "\n";
            sb.append(line);
         }
      }
   }

   private NodeRoleMap parseNodeRoles(String testRigPath) {
      Path rolePath = Paths.get(testRigPath, "node_roles");
      String roleFileText = readFile(rolePath.toFile());
      _logger.info("Parsing: \"" + rolePath.toAbsolutePath().toString() + "\"");
      BatfishCombinedParser<?, ?> parser = new RoleCombinedParser(roleFileText,
            _settings.getThrowOnParserError(), _settings.getThrowOnLexerError());
      RoleExtractor extractor = new RoleExtractor();
      ParserRuleContext tree = parse(parser);
      ParseTreeWalker walker = new ParseTreeWalker();
      walker.walk(extractor, tree);
      NodeRoleMap nodeRoles = extractor.getRoleMap();
      return nodeRoles;
   }

   private Question parseQuestion(String questionPath) {
      File questionFile = new File(questionPath);
      _logger.info("Reading question file: \"" + questionPath + "\"...");
      String questionText = readFile(questionFile);
      _logger.info("OK\n");
      QuestionCombinedParser parser = new QuestionCombinedParser(questionText,
            _settings.getThrowOnParserError(), _settings.getThrowOnLexerError());
      QuestionExtractor extractor = new QuestionExtractor();
      try {
         ParserRuleContext tree = parse(parser, questionPath);
         _logger.info("\tPost-processing...");
         extractor.processParseTree(tree);
         _logger.info("OK\n");
      }
      catch (ParserBatfishException e) {
         String error = "Error parsing question: \"" + questionPath + "\"";
         throw new BatfishException(error, e);
      }
      catch (Exception e) {
         String error = "Error post-processing parse tree of question file: \""
               + questionPath + "\"";
         throw new BatfishException(error, e);
      }
      return extractor.getQuestion();
   }

   private Topology parseTopology(File topologyFilePath) {
      _logger.info("*** PARSING TOPOLOGY ***\n");
      resetTimer();
      String topologyFileText = readFile(topologyFilePath);
      BatfishCombinedParser<?, ?> parser = null;
      TopologyExtractor extractor = null;
      _logger.info("Parsing: \""
            + topologyFilePath.getAbsolutePath().toString() + "\"");
      if (topologyFileText.startsWith("autostart")) {
         parser = new GNS3TopologyCombinedParser(topologyFileText,
               _settings.getThrowOnParserError(),
               _settings.getThrowOnLexerError());
         extractor = new GNS3TopologyExtractor();
      }
      else if (topologyFileText.startsWith("CONFIGPARSER_TOPOLOGY")) {
         parser = new BatfishTopologyCombinedParser(topologyFileText,
               _settings.getThrowOnParserError(),
               _settings.getThrowOnLexerError());
         extractor = new BatfishTopologyExtractor();
      }
      else if (topologyFileText.equals("")) {
         throw new BatfishException("...ERROR: empty topology\n");
      }
      else {
         _logger.fatal("...ERROR\n");
         throw new BatfishException("Topology format error");
      }
      ParserRuleContext tree = parse(parser);
      ParseTreeWalker walker = new ParseTreeWalker();
      walker.walk(extractor, tree);
      Topology topology = extractor.getTopology();
      printElapsedTime();
      return topology;
   }

   private Map<String, VendorConfiguration> parseVendorConfigurations(
         Map<File, String> configurationData) {
      _logger.info("\n*** PARSING VENDOR CONFIGURATION FILES ***\n");
      resetTimer();

      ExecutorService pool;
      boolean shuffle;
      if (!_settings.getSequential()) {
         int numConcurrentThreads = Runtime.getRuntime().availableProcessors();
         pool = Executors.newFixedThreadPool(numConcurrentThreads);
         shuffle = true;
      }
      else {
         pool = Executors.newSingleThreadExecutor();
         shuffle = false;
      }

      Map<String, VendorConfiguration> vendorConfigurations = new TreeMap<String, VendorConfiguration>();
      List<ParseVendorConfigurationJob> jobs = new ArrayList<ParseVendorConfigurationJob>();

      boolean processingError = false;
      for (File currentFile : configurationData.keySet()) {
         Warnings warnings = new Warnings(_settings.getPedanticAsError(),
               _settings.getPedanticRecord()
                     && _logger.isActive(BatfishLogger.LEVEL_PEDANTIC),
               _settings.getRedFlagAsError(), _settings.getRedFlagRecord()
                     && _logger.isActive(BatfishLogger.LEVEL_REDFLAG),
               _settings.getUnimplementedAsError(),
               _settings.getUnimplementedRecord()
                     && _logger.isActive(BatfishLogger.LEVEL_UNIMPLEMENTED),
               _settings.printParseTree());
         String fileText = configurationData.get(currentFile);
         ParseVendorConfigurationJob job = new ParseVendorConfigurationJob(
               _settings, fileText, currentFile, warnings);
         jobs.add(job);
      }
      if (shuffle) {
         Collections.shuffle(jobs);
      }
      List<Future<ParseVendorConfigurationResult>> futures = new ArrayList<Future<ParseVendorConfigurationResult>>();
      for (ParseVendorConfigurationJob job : jobs) {
         Future<ParseVendorConfigurationResult> future = pool.submit(job);
         futures.add(future);
      }
      // try {
      // futures = pool.invokeAll(jobs);
      // }
      // catch (InterruptedException e) {
      // throw new BatfishException("Error invoking parse jobs", e);
      // }
      while (!futures.isEmpty()) {
         List<Future<ParseVendorConfigurationResult>> currentFutures = new ArrayList<Future<ParseVendorConfigurationResult>>();
         currentFutures.addAll(futures);
         for (Future<ParseVendorConfigurationResult> future : currentFutures) {
            if (future.isDone()) {
               futures.remove(future);
               ParseVendorConfigurationResult result = null;
               try {
                  result = future.get();
               }
               catch (InterruptedException | ExecutionException e) {
                  throw new BatfishException("Error executing parse job", e);
               }
               String terseLogLevelPrefix;
               if (_logger.isActive(BatfishLogger.LEVEL_INFO)) {
                  terseLogLevelPrefix = "";
               }
               else {
                  terseLogLevelPrefix = result.getFile().toString() + ": ";
               }
               _logger.append(result.getHistory(), terseLogLevelPrefix);
               Throwable failureCause = result.getFailureCause();
               if (failureCause != null) {
                  if (_settings.getExitOnFirstError()) {
                     throw new BatfishException("Failed parse job",
                           failureCause);
                  }
                  else {
                     processingError = true;
                     _logger.error(ExceptionUtils.getStackTrace(failureCause));
                  }
               }
               else {
                  VendorConfiguration vc = result.getVendorConfiguration();
                  if (vc != null) {
                     String hostname = vc.getHostname();
                     if (vendorConfigurations.containsKey(hostname)) {
                        throw new BatfishException("Duplicate hostname: "
                              + hostname);
                     }
                     else {
                        vendorConfigurations.put(hostname, vc);
                     }
                  }
               }
            }
            else {
               continue;
            }
         }
         if (!futures.isEmpty()) {
            try {
               Thread.sleep(JOB_POLLING_PERIOD_MS);
            }
            catch (InterruptedException e) {
               throw new BatfishException("interrupted while sleeping", e);
            }
         }
      }
      pool.shutdown();
      if (processingError) {
         return null;
      }
      else {
         printElapsedTime();
         return vendorConfigurations;
      }
   }

   private void populateConfigurationFactBins(
         Collection<Configuration> configurations,
         Map<String, StringBuilder> factBins) {
      _logger
            .info("\n*** EXTRACTING LOGICBLOX FACTS FROM CONFIGURATIONS ***\n");
      resetTimer();
      Set<Long> communities = new LinkedHashSet<Long>();
      for (Configuration c : configurations) {
         communities.addAll(c.getCommunities());
      }
      boolean pedanticAsError = _settings.getPedanticAsError();
      boolean pedanticRecord = _settings.getPedanticRecord();
      boolean redFlagAsError = _settings.getRedFlagAsError();
      boolean redFlagRecord = _settings.getRedFlagRecord();
      boolean unimplementedAsError = _settings.getUnimplementedAsError();
      boolean unimplementedRecord = _settings.getUnimplementedRecord();
      boolean processingError = false;
      for (Configuration c : configurations) {
         String hostname = c.getHostname();
         _logger.debug("Extracting facts from: \"" + hostname + "\"");
         Warnings warnings = new Warnings(pedanticAsError, pedanticRecord,
               redFlagAsError, redFlagRecord, unimplementedAsError,
               unimplementedRecord, false);
         try {
            ConfigurationFactExtractor cfe = new ConfigurationFactExtractor(c,
                  communities, factBins, warnings);
            cfe.writeFacts();
            _logger.debug("...OK\n");
         }
         catch (BatfishException e) {
            _logger.fatal("...EXTRACTION ERROR\n");
            _logger.fatal(ExceptionUtils.getStackTrace(e));
            processingError = true;
            if (_settings.getExitOnFirstError()) {
               break;
            }
            else {
               continue;
            }
         }
         finally {
            for (String warning : warnings.getRedFlagWarnings()) {
               _logger.redflag(warning);
            }
            for (String warning : warnings.getUnimplementedWarnings()) {
               _logger.unimplemented(warning);
            }
            for (String warning : warnings.getPedanticWarnings()) {
               _logger.pedantic(warning);
            }
         }
      }
      if (processingError) {
         throw new BatfishException(
               "Failed to extract facts from vendor-indpendent configuration structures");
      }
      printElapsedTime();
   }

   private void postFacts(LogicBloxFrontend lbFrontend,
         Map<String, StringBuilder> factBins) {
      Map<String, StringBuilder> enabledFacts = new HashMap<String, StringBuilder>();
      enabledFacts.putAll(factBins);
      enabledFacts.keySet().removeAll(_settings.getDisabledFacts());
      _logger.info("\n*** POSTING FACTS TO BLOXWEB SERVICES ***\n");
      resetTimer();
      _logger.info("Starting bloxweb services...");
      lbFrontend.startLbWebServices();
      _logger.info("OK\n");
      _logger.info("Posting facts...");
      try {
         lbFrontend.postFacts(enabledFacts);
      }
      catch (ServiceClientException e) {
         throw new BatfishException("Failed to post facts to bloxweb services",
               e);
      }
      _logger.info("OK\n");
      _logger.info("Stopping bloxweb services...");
      lbFrontend.stopLbWebServices();
      _logger.info("OK\n");
      _logger.info("SUCCESS\n");
      printElapsedTime();
   }

   private void printAllPredicateSemantics(
         Map<String, String> predicateSemantics) {
      // Get predicate semantics from rules file
      _logger.info("\n*** PRINTING PREDICATE SEMANTICS ***\n");
      List<String> helpPredicates = getHelpPredicates(predicateSemantics);
      for (String predicate : helpPredicates) {
         printPredicateSemantics(predicate);
         _logger.info("\n");
      }
   }

   private void printElapsedTime() {
      double seconds = getElapsedTime(_timerCount);
      _logger.info("Time taken for this task: " + seconds + " seconds\n");
   }

   private void printPredicate(LogicBloxFrontend lbFrontend,
         String predicateName) {
      List<String> output;
      printPredicateSemantics(predicateName);
      String qualifiedName = _predicateInfo.getPredicateNames().get(
            predicateName);
      if (qualifiedName == null) { // predicate not found
         _logger.error("ERROR: No information for predicate: " + predicateName
               + "\n");
         return;
      }
      Relation relation = lbFrontend.queryPredicate(qualifiedName);
      try {
         output = lbFrontend.getPredicate(_predicateInfo, relation,
               predicateName);
         for (String match : output) {
            _logger.output(match + "\n");
         }
      }
      catch (QueryException q) {
         _logger.fatal(q.getMessage() + "\n");
      }
   }

   private void printPredicateCount(LogicBloxFrontend lbFrontend,
         String predicateName) {
      int numRows = lbFrontend.queryPredicate(predicateName).getColumns()
            .get(0).size();
      String output = "|" + predicateName + "| = " + numRows + "\n";
      _logger.info(output);
   }

   public void printPredicateCounts(LogicBloxFrontend lbFrontend,
         Set<String> predicateNames) {
      // Print predicate(s) here
      _logger.info("\n*** SUBMITTING QUERY(IES) ***\n");
      resetTimer();
      for (String predicateName : predicateNames) {
         printPredicateCount(lbFrontend, predicateName);
         // _logger.info("\n");
      }
      printElapsedTime();
   }

   public void printPredicates(LogicBloxFrontend lbFrontend,
         Set<String> predicateNames) {
      // Print predicate(s) here
      _logger.info("\n*** SUBMITTING QUERY(IES) ***\n");
      resetTimer();
      String queryDumpDirStr = _settings.getQueryDumpDir();
      if (queryDumpDirStr == null) {
         for (String predicateName : predicateNames) {
            printPredicate(lbFrontend, predicateName);
         }
      }
      else {
         Path queryDumpDir = Paths.get(queryDumpDirStr);
         queryDumpDir.toFile().mkdirs();
         for (String predicateName : predicateNames) {
            String outputPath = queryDumpDir.resolve(predicateName).toString();
            printPredicateToFile(lbFrontend, predicateName, outputPath);
         }
      }
      printElapsedTime();
   }

   private void printPredicateSemantics(String predicateName) {
      String semantics = _predicateInfo.getPredicateSemantics(predicateName);
      if (semantics == null) {
         semantics = "<missing>";
      }
      _logger.info("\n");
      _logger.info("Predicate: " + predicateName + "\n");
      _logger.info("Semantics: " + semantics + "\n");
   }

   private void printPredicateToFile(LogicBloxFrontend lbFrontend,
         String predicateName, String outputPath) {
      List<String> output;
      printPredicateSemantics(predicateName);
      StringBuilder sb = new StringBuilder();
      String qualifiedName = _predicateInfo.getPredicateNames().get(
            predicateName);
      if (qualifiedName == null) { // predicate not found
         _logger.error("ERROR: No information for predicate: " + predicateName
               + "\n");
         return;
      }
      Relation relation = lbFrontend.queryPredicate(qualifiedName);
      try {
         output = lbFrontend.getPredicate(_predicateInfo, relation,
               predicateName);
         for (String match : output) {
            sb.append(match + "\n");
         }
      }
      catch (QueryException q) {
         _logger.fatal(q.getMessage() + "\n");
      }
      String outputString = sb.toString();
      writeFile(outputPath, outputString);
   }

   private void processTopology(File topologyFilePath,
         Map<String, StringBuilder> factBins) {
      Topology topology = null;
      topology = parseTopology(topologyFilePath);
      TopologyFactExtractor tfe = new TopologyFactExtractor(topology);
      tfe.writeFacts(factBins);
   }

   private Map<File, String> readConfigurationFiles(String testRigPath) {
      _logger.info("\n*** READING CONFIGURATION FILES ***\n");
      resetTimer();
      Map<File, String> configurationData = new TreeMap<File, String>();
      File configsPath = Paths
            .get(testRigPath, TESTRIG_CONFIGURATION_DIRECTORY).toFile();
      File[] configFilePaths = configsPath.listFiles(new FilenameFilter() {
         @Override
         public boolean accept(File dir, String name) {
            return !name.startsWith(".");
         }
      });
      if (configFilePaths == null) {
         throw new BatfishException("Error reading test rig configs directory");
      }
      for (File file : configFilePaths) {
         _logger.debug("Reading: \"" + file.toString() + "\"\n");
         String fileText = readFile(file.getAbsoluteFile()) + "\n";
         configurationData.put(file, fileText);
      }
      printElapsedTime();
      return configurationData;
   }

   public String readFile(File file) {
      String text = null;
      try {
         text = FileUtils.readFileToString(file);
      }
      catch (IOException e) {
         throw new BatfishException("Failed to read file: " + file.toString(),
               e);
      }
      return text;
   }

   private void resetTimer() {
      _timerCount = System.currentTimeMillis();
   }

   private File retrieveLogicDir() {
      File logicDirFile = null;
      final String locatorFilename = LogicResourceLocator.class.getSimpleName()
            + ".class";
      URL logicSourceURL = LogicResourceLocator.class.getProtectionDomain()
            .getCodeSource().getLocation();
      String logicSourceString = logicSourceURL.toString();
      UrlZipExplorer zip = null;
      StringFilter lbFilter = new StringFilter() {
         @Override
         public boolean accept(String filename) {
            return filename.endsWith(".lbb") || filename.endsWith(".lbp")
                  || filename.endsWith(".semantics")
                  || filename.endsWith(locatorFilename)
                  || filename.endsWith(PREDICATE_INFO_FILENAME);
         }
      };
      if (logicSourceString.startsWith("onejar:")) {
         FileVisitor<Path> visitor = null;
         try {
            zip = new UrlZipExplorer(logicSourceURL);
            Path destinationDir = Files.createTempDirectory("lbtmpproject");
            File destinationDirAsFile = destinationDir.toFile();
            zip.extractFiles(lbFilter, destinationDirAsFile);
            visitor = new SimpleFileVisitor<Path>() {
               private String _projectDirectory;

               @Override
               public String toString() {
                  return _projectDirectory;
               }

               @Override
               public FileVisitResult visitFile(Path aFile,
                     BasicFileAttributes aAttrs) throws IOException {
                  if (aFile.endsWith(locatorFilename)) {
                     _projectDirectory = aFile.getParent().toString();
                     return FileVisitResult.TERMINATE;
                  }
                  return FileVisitResult.CONTINUE;
               }
            };
            Files.walkFileTree(destinationDir, visitor);
            _tmpLogicDir = destinationDirAsFile;
         }
         catch (IOException e) {
            throw new BatfishException(
                  "Failed to retrieve logic dir from onejar archive", e);
         }
         String fileString = visitor.toString();
         return new File(fileString);
      }
      else {
         String logicPackageResourceName = LogicResourceLocator.class
               .getPackage().getName().replace('.', SEPARATOR.charAt(0));
         try {
            logicDirFile = new File(LogicResourceLocator.class.getClassLoader()
                  .getResource(logicPackageResourceName).toURI());
         }
         catch (URISyntaxException e) {
            throw new BatfishException("Failed to resolve logic directory", e);
         }
         return logicDirFile;
      }
   }

   private void revert(LogicBloxFrontend lbFrontend) {
      _logger.info("\n*** REVERTING WORKSPACE ***\n");
      String workspaceName = new File(_settings.getTestRigPath()).getName();
      String branchName = _settings.getBranchName();
      _logger.debug("Reverting workspace: \"" + workspaceName
            + "\" to branch: \"" + branchName + "\n");
      String errorResult = lbFrontend.revertDatabase(branchName);
      if (errorResult != null) {
         throw new BatfishException("Failed to revert database: " + errorResult);
      }
   }

   public void run() {

      if (_settings.getAnswer()) {
         String questionPath = _settings.getQuestionPath();
         answer(questionPath);
         return;
      }

      if (_settings.getBuildPredicateInfo()) {
         buildPredicateInfo();
         return;
      }

      if (_settings.getHistogram()) {
         histogram(_settings.getTestRigPath());
         return;
      }

      if (_settings.getGenerateOspfTopologyPath() != null) {
         generateOspfConfigs(_settings.getGenerateOspfTopologyPath(),
               _settings.getSerializeIndependentPath());
         return;
      }

      if (_settings.getFlatten()) {
         String flattenSource = _settings.getTestRigPath();
         String flattenDestination = _settings.getFlattenDestination();
         flatten(flattenSource, flattenDestination);
         return;
      }

      if (_settings.getGenerateStubs()) {
         String configPath = _settings.getSerializeIndependentPath();
         String inputRole = _settings.getGenerateStubsInputRole();
         String interfaceDescriptionRegex = _settings
               .getGenerateStubsInterfaceDescriptionRegex();
         int stubAs = _settings.getGenerateStubsRemoteAs();
         generateStubs(inputRole, stubAs, interfaceDescriptionRegex, configPath);
         return;
      }

      if (_settings.getZ3()) {
         Map<String, Configuration> configurations = deserializeConfigurations(_settings
               .getSerializeIndependentPath());
         genZ3(configurations);
         return;
      }

      if (_settings.getAnonymize()) {
         anonymizeConfigurations();
         return;
      }

      if (_settings.getInterfaceFailureInconsistencyReachableQuery()) {
         genReachableQueries();
         return;
      }

      if (_settings.getRoleReachabilityQuery()) {
         genRoleReachabilityQueries();
         return;
      }

      if (_settings.getRoleTransitQuery()) {
         genRoleTransitQueries();
         return;
      }

      if (_settings.getInterfaceFailureInconsistencyBlackHoleQuery()) {
         genBlackHoleQueries();
         return;
      }

      if (_settings.getGenerateMultipathInconsistencyQuery()) {
         genMultipathQueries();
         return;
      }

      if (_settings.getSerializeVendor()) {
         String testRigPath = _settings.getTestRigPath();
         String outputPath = _settings.getSerializeVendorPath();
         serializeVendorConfigs(testRigPath, outputPath);
         return;
      }

      if (_settings.dumpInterfaceDescriptions()) {
         String testRigPath = _settings.getTestRigPath();
         String outputPath = _settings.getDumpInterfaceDescriptionsPath();
         dumpInterfaceDescriptions(testRigPath, outputPath);
         return;
      }

      if (_settings.getSerializeIndependent()) {
         String inputPath = _settings.getSerializeVendorPath();
         String outputPath = _settings.getSerializeIndependentPath();
         serializeIndependentConfigs(inputPath, outputPath);
         return;
      }

      if (_settings.getConcretize()) {
         concretize();
         return;
      }

      if (_settings.getQuery() || _settings.getPrintSemantics()
            || _settings.getDataPlane()) {
         Map<String, String> logicFiles = getSemanticsFiles();
         _predicateInfo = getPredicateInfo(logicFiles);
         // Print predicate semantics and quit if requested
         if (_settings.getPrintSemantics()) {
            printAllPredicateSemantics(_predicateInfo.getPredicateSemantics());
            return;
         }
      }

      Map<String, StringBuilder> cpFactBins = null;
      if (_settings.getFacts() || _settings.getDumpControlPlaneFacts()) {
         cpFactBins = new LinkedHashMap<String, StringBuilder>();
         initControlPlaneFactBins(cpFactBins);
         Map<String, Configuration> configurations = deserializeConfigurations(_settings
               .getSerializeIndependentPath());
         writeTopologyFacts(_settings.getTestRigPath(), configurations,
               cpFactBins);
         writeConfigurationFacts(configurations, cpFactBins);
         String flowSinkPath = _settings.getFlowSinkPath();
         if (flowSinkPath != null) {
            FlowSinkSet flowSinks = (FlowSinkSet) deserializeObject(new File(
                  flowSinkPath));
            writeFlowSinkFacts(flowSinks, cpFactBins);
         }
         if (_settings.getDumpControlPlaneFacts()) {
            dumpFacts(cpFactBins);
         }
         if (!(_settings.getFacts() || _settings.createWorkspace())) {
            return;
         }
      }

      // Start frontend
      LogicBloxFrontend lbFrontend = null;
      if (_settings.createWorkspace() || _settings.getFacts()
            || _settings.getQuery() || _settings.getDataPlane()
            || _settings.revert()) {
         lbFrontend = connect();
      }

      if (_settings.revert()) {
         revert(lbFrontend);
         return;
      }

      // Create new workspace (will overwrite existing) if requested
      if (_settings.createWorkspace()) {
         addProject(lbFrontend);
         String lbHostnamePath = _settings.getJobLogicBloxHostnamePath();
         String lbHostname = _settings.getServiceLogicBloxHostname();
         if (lbHostnamePath != null && lbHostname != null) {
            writeFile(lbHostnamePath, lbHostname);
         }
         if (!_settings.getFacts()) {
            return;
         }
      }

      // Post facts if requested
      if (_settings.getFacts()) {
         addStaticFacts(lbFrontend, BASIC_FACTS_BLOCKNAME);
         postFacts(lbFrontend, cpFactBins);
         return;
      }

      if (_settings.getQuery()) {
         lbFrontend.initEntityTable();
         Map<String, String> allPredicateNames = _predicateInfo
               .getPredicateNames();
         Set<String> predicateNames = new TreeSet<String>();
         if (_settings.getQueryAll()) {
            predicateNames.addAll(allPredicateNames.keySet());
         }
         else {
            predicateNames.addAll(_settings.getPredicates());
         }
         if (_settings.getCountsOnly()) {
            printPredicateCounts(lbFrontend, predicateNames);
         }
         else {
            printPredicates(lbFrontend, predicateNames);
         }
         return;
      }

      if (_settings.getDataPlane()) {
         computeDataPlane(lbFrontend);
         return;
      }

      Map<String, StringBuilder> trafficFactBins = null;
      if (_settings.getPostFlows()) {
         trafficFactBins = new LinkedHashMap<String, StringBuilder>();
         Path dumpDir = Paths.get(_settings.getTrafficFactDumpDir());
         for (String predicate : Facts.TRAFFIC_FACT_COLUMN_HEADERS.keySet()) {
            File factFile = dumpDir.resolve(predicate).toFile();
            String contents = readFile(factFile);
            StringBuilder sb = new StringBuilder();
            trafficFactBins.put(predicate, sb);
            sb.append(contents);
         }
         lbFrontend = connect();
         postFacts(lbFrontend, trafficFactBins);
         return;
      }

      if (_settings.getFlows() || _settings.getDumpTrafficFacts()) {
         trafficFactBins = new LinkedHashMap<String, StringBuilder>();
         initTrafficFactBins(trafficFactBins);
         writeTrafficFacts(trafficFactBins);
         if (_settings.getDumpTrafficFacts()) {
            dumpFacts(trafficFactBins);
         }
         if (_settings.getFlows()) {
            lbFrontend = connect();
            postFacts(lbFrontend, trafficFactBins);
            return;
         }
      }
      throw new BatfishException(
            "No task performed! Run with -help flag to see usage");
   }

   private void serializeIndependentConfigs(
         Map<String, Configuration> configurations, String outputPath) {
      _logger
            .info("\n*** SERIALIZING VENDOR-INDEPENDENT CONFIGURATION STRUCTURES ***\n");
      resetTimer();
      new File(outputPath).mkdirs();
      for (String name : configurations.keySet()) {
         Configuration c = configurations.get(name);
         Path currentOutputPath = Paths.get(outputPath, name);
         _logger.info("Serializing: \"" + name + "\" ==> \""
               + currentOutputPath.toString() + "\"");
         serializeObject(c, currentOutputPath.toFile());
         _logger.debug(" ...OK\n");
      }
      printElapsedTime();
   }

   private void serializeIndependentConfigs(String vendorConfigPath,
         String outputPath) {
      Map<String, Configuration> configurations = getConfigurations(vendorConfigPath);
      serializeIndependentConfigs(configurations, outputPath);
   }

   private void serializeObject(Object object, File outputFile) {
      FileOutputStream fos;
      ObjectOutputStream oos;
      try {
         fos = new FileOutputStream(outputFile);
         if (_settings.getSerializeToText()) {
            XStream xstream = new XStream(new DomDriver("UTF-8"));
            oos = xstream.createObjectOutputStream(fos);
         }
         else {
            oos = new ObjectOutputStream(fos);
         }
         oos.writeObject(object);
         oos.close();
      }
      catch (IOException e) {
         throw new BatfishException(
               "Failed to serialize object to output file: "
                     + outputFile.toString(), e);
      }
   }

   private void serializeVendorConfigs(String testRigPath, String outputPath) {
      Map<File, String> configurationData = readConfigurationFiles(testRigPath);
      Map<String, VendorConfiguration> vendorConfigurations = parseVendorConfigurations(configurationData);
      if (vendorConfigurations == null) {
         throw new BatfishException("Exiting due to parser errors\n");
      }
      String nodeRolesPath = _settings.getNodeRolesPath();
      if (nodeRolesPath != null) {
         NodeRoleMap nodeRoles = parseNodeRoles(testRigPath);
         for (Entry<String, RoleSet> nodeRolesEntry : nodeRoles.entrySet()) {
            String hostname = nodeRolesEntry.getKey();
            VendorConfiguration config = vendorConfigurations.get(hostname);
            if (config == null) {
               throw new BatfishException(
                     "role set assigned to non-existent node: \"" + hostname
                           + "\"");
            }
            RoleSet roles = nodeRolesEntry.getValue();
            config.setRoles(roles);
         }
         if (!_settings.getNoOutput()) {
            _logger.info("Serializing node-roles mappings: \"" + nodeRolesPath
                  + "\"...");
            serializeObject(nodeRoles, new File(nodeRolesPath));
            _logger.info("OK\n");
         }
      }
      if (!_settings.getNoOutput()) {
         _logger
               .info("\n*** SERIALIZING VENDOR CONFIGURATION STRUCTURES ***\n");
         resetTimer();
         new File(outputPath).mkdirs();
         for (String name : vendorConfigurations.keySet()) {
            VendorConfiguration vc = vendorConfigurations.get(name);
            Path currentOutputPath = Paths.get(outputPath, name);
            _logger.debug("Serializing: \"" + name + "\" ==> \""
                  + currentOutputPath.toString() + "\"...");
            serializeObject(vc, currentOutputPath.toFile());
            _logger.debug("OK\n");
         }
         printElapsedTime();
      }
   }

   private Synthesizer synthesizeDataPlane(
         Map<String, Configuration> configurations, Context ctx)
         throws Z3Exception {
      _logger.info("\n*** GENERATING Z3 LOGIC ***\n");
      resetTimer();

      String dataPlaneDir = _settings.getDataPlaneDir();
      if (dataPlaneDir == null) {
         throw new BatfishException("Data plane dir not set");
      }
      Path flowSinkSetPath = Paths.get(dataPlaneDir, FLOW_SINKS_FILENAME);
      Path fibsPath = Paths.get(dataPlaneDir, FIBS_FILENAME);
      Path prFibsPath = Paths.get(dataPlaneDir,
            FIBS_POLICY_ROUTE_NEXT_HOP_FILENAME);
      Path edgesPath = Paths.get(dataPlaneDir, EDGES_FILENAME);

      _logger.info("Deserializing flow sink interface set: \""
            + flowSinkSetPath.toString() + "\"...");
      FlowSinkSet flowSinks = (FlowSinkSet) deserializeObject(flowSinkSetPath
            .toFile());
      _logger.info("OK\n");

      _logger.info("Deserializing destination route fibs: \""
            + fibsPath.toString() + "\"...");
      FibMap fibs = (FibMap) deserializeObject(fibsPath.toFile());
      _logger.info("OK\n");

      _logger.info("Deserializing policy route fibs: \""
            + prFibsPath.toString() + "\"...");
      PolicyRouteFibNodeMap prFibs = (PolicyRouteFibNodeMap) deserializeObject(prFibsPath
            .toFile());
      _logger.info("OK\n");

      _logger.info("Deserializing toplogy edges: \"" + edgesPath.toString()
            + "\"...");
      EdgeSet topologyEdges = (EdgeSet) deserializeObject(edgesPath.toFile());
      _logger.info("OK\n");

      _logger.info("Synthesizing Z3 logic...");
      Synthesizer s = new Synthesizer(configurations, fibs, prFibs,
            topologyEdges, _settings.getSimplify(), flowSinks);

      List<String> warnings = s.getWarnings();
      int numWarnings = warnings.size();
      if (numWarnings == 0) {
         _logger.info("OK\n");
      }
      else {
         for (String warning : warnings) {
            _logger.warn(warning);
         }
      }
      printElapsedTime();
      return s;
   }

   public void writeConfigurationFacts(
         Map<String, Configuration> configurations,
         Map<String, StringBuilder> factBins) {
      populateConfigurationFactBins(configurations.values(), factBins);
   }

   private void writeFile(String outputPath, String output) {
      File outputFile = new File(outputPath);
      try {
         FileUtils.write(outputFile, output);
      }
      catch (IOException e) {
         throw new BatfishException("Failed to write file: " + outputPath, e);
      }
   }

   private void writeFlowSinkFacts(FlowSinkSet flowSinks,
         Map<String, StringBuilder> cpFactBins) {
      StringBuilder sb = cpFactBins.get("SetFlowSinkInterface");
      for (FlowSinkInterface f : flowSinks) {
         String node = f.getNode();
         String iface = f.getInterface();
         sb.append(node + "|" + iface + "\n");
      }
   }

   public void writeTopologyFacts(String testRigPath,
         Map<String, Configuration> configurations,
         Map<String, StringBuilder> factBins) {
      Path topologyFilePath = Paths.get(testRigPath, TOPOLOGY_FILENAME);
      // Get generated facts from topology file
      if (Files.exists(topologyFilePath)) {
         processTopology(topologyFilePath.toFile(), factBins);
      }
      else {
         // tell logicblox to guess adjacencies based on interface
         // subnetworks
         _logger
               .info("*** (GUESSING TOPOLOGY IN ABSENCE OF EXPLICIT FILE) ***\n");
         StringBuilder wGuessTopology = factBins.get("GuessTopology");
         wGuessTopology.append("1\n");
      }
   }

   private void writeTrafficFacts(Map<String, StringBuilder> factBins) {
      StringBuilder wSetFlowOriginate = factBins.get("SetFlowOriginate");
      RoleNodeMap roleNodes = null;
      if (_settings.getRoleHeaders()) {
         String nodeRolesPath = _settings.getNodeRolesPath();
         NodeRoleMap nodeRoles = (NodeRoleMap) deserializeObject(new File(
               nodeRolesPath));
         roleNodes = nodeRoles.toRoleNodeMap();
      }
      parseFlowsFromConstraints(wSetFlowOriginate, roleNodes);
      if (_settings.duplicateRoleFlows()) {
         StringBuilder wDuplicateRoleFlows = factBins.get("DuplicateRoleFlows");
         wDuplicateRoleFlows.append("1\n");
      }
   }

}
