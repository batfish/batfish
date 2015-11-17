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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.batfish.collections.AdvertisementSet;
import org.batfish.collections.EdgeSet;
import org.batfish.collections.FibMap;
import org.batfish.collections.FibRow;
import org.batfish.collections.FibSet;
import org.batfish.collections.InterfaceSet;
import org.batfish.collections.FunctionSet;
import org.batfish.collections.IbgpTopology;
import org.batfish.collections.IpEdge;
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
import org.batfish.collections.RouteSet;
import org.batfish.collections.TreeMultiSet;
import org.batfish.common.BatfishLogger;
import org.batfish.common.BfConsts;
import org.batfish.common.BatfishException;
import org.batfish.common.CleanBatfishException;
import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.ParseTreePrettyPrinter;
import org.batfish.grammar.juniper.JuniperCombinedParser;
import org.batfish.grammar.juniper.JuniperFlattener;
import org.batfish.grammar.logicblox.LogQLPredicateInfoExtractor;
import org.batfish.grammar.logicblox.LogiQLCombinedParser;
import org.batfish.grammar.logicblox.LogiQLPredicateInfoResolver;
import org.batfish.grammar.question.QuestionCombinedParser;
import org.batfish.grammar.question.QuestionExtractor;
import org.batfish.grammar.question.QuestionParametersCombinedParser;
import org.batfish.grammar.question.QuestionParametersExtractor;
import org.batfish.grammar.topology.BatfishTopologyCombinedParser;
import org.batfish.grammar.topology.BatfishTopologyExtractor;
import org.batfish.grammar.topology.GNS3TopologyCombinedParser;
import org.batfish.grammar.topology.GNS3TopologyExtractor;
import org.batfish.grammar.topology.RoleCombinedParser;
import org.batfish.grammar.topology.RoleExtractor;
import org.batfish.grammar.topology.TopologyExtractor;
import org.batfish.job.BatfishJobExecutor;
import org.batfish.job.ConvertConfigurationJob;
import org.batfish.job.ConvertConfigurationResult;
import org.batfish.job.FlattenVendorConfigurationJob;
import org.batfish.job.FlattenVendorConfigurationResult;
import org.batfish.job.ParseVendorConfigurationJob;
import org.batfish.job.ParseVendorConfigurationResult;
import org.batfish.logic.LogicResourceLocator;
import org.batfish.logicblox.Block;
import org.batfish.logicblox.ConfigurationFactExtractor;
import org.batfish.logicblox.Facts;
import org.batfish.logicblox.LBValueType;
import org.batfish.logicblox.LogicBloxFrontend;
import org.batfish.logicblox.LogicBloxFrontendManager;
import org.batfish.logicblox.PredicateInfo;
import org.batfish.logicblox.ProjectFile;
import org.batfish.logicblox.QueryException;
import org.batfish.logicblox.TopologyFactExtractor;
import org.batfish.main.Settings.EnvironmentSettings;
import org.batfish.question.DestinationQuestion;
import org.batfish.question.FailureQuestion;
import org.batfish.question.IngressPathQuestion;
import org.batfish.question.LocalPathQuestion;
import org.batfish.question.MultipathQuestion;
import org.batfish.question.Question;
import org.batfish.question.QuestionParameters;
import org.batfish.question.ReachabilityQuestion;
import org.batfish.question.TracerouteQuestion;
import org.batfish.question.VerifyProgram;
import org.batfish.question.VerifyQuestion;
import org.batfish.representation.AsPath;
import org.batfish.representation.AsSet;
import org.batfish.representation.BgpAdvertisement;
import org.batfish.representation.BgpNeighbor;
import org.batfish.representation.BgpProcess;
import org.batfish.representation.Configuration;
import org.batfish.representation.DataPlane;
import org.batfish.representation.Edge;
import org.batfish.representation.Flow;
import org.batfish.representation.FlowHistory;
import org.batfish.representation.FlowTrace;
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
import org.batfish.representation.PrecomputedRoute;
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
import org.batfish.z3.BlacklistDstIpQuerySynthesizer;
import org.batfish.z3.CompositeNodJob;
import org.batfish.z3.ConcretizerQuery;
import org.batfish.z3.DropQuerySynthesizer;
import org.batfish.z3.MultipathInconsistencyQuerySynthesizer;
import org.batfish.z3.NodJob;
import org.batfish.z3.NodJobResult;
import org.batfish.z3.QuerySynthesizer;
import org.batfish.z3.ReachEdgeQuerySynthesizer;
import org.batfish.z3.ReachabilityQuerySynthesizer;
import org.batfish.z3.ReachableQuerySynthesizer;
import org.batfish.z3.RoleReachabilityQuerySynthesizer;
import org.batfish.z3.RoleTransitQuerySynthesizer;
import org.batfish.z3.Synthesizer;

import com.logicblox.bloxweb.client.ServiceClientException;
import com.logicblox.connect.Workspace.Relation;
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

   private static final String BGP_ADVERTISEMENT_ROUTE_PREDICATE_NAME = "BgpAdvertisementRoute";

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

   private static final String FLOW_HISTORY_PREDICATE_NAME = "FlowPathHistory";

   /**
    * Name of the LogiQL predicate containing flow-sink interface tags
    */
   private static final String FLOW_SINK_PREDICATE_NAME = "SetFlowSinkInterface";

   private static final String GEN_OSPF_STARTING_IP = "10.0.0.0";

   private static final String IBGP_NEIGHBORS_PREDICATE_NAME = "IbgpNeighbors";

   private static final String INSTALLED_ROUTE_PREDICATE_NAME = "InstalledRoute";

   /**
    * A byte-array containing the first 4 bytes comprising the header for a file
    * that is the output of java serialization
    */
   private static final byte[] JAVA_SERIALIZED_OBJECT_HEADER = {
         (byte) 0xac,
         (byte) 0xed,
         (byte) 0x00,
         (byte) 0x05 };

   /**
    * The name of the LogiQL library for org.batfish
    */
   private static final String LB_BATFISH_LIBRARY_NAME = "libbatfish";

   private static final String NETWORKS_PREDICATE_NAME = "SetNetwork";

   private static final String PRECOMPUTED_BGP_ADVERTISEMENT_AS_PATH_LENGTH_PREDICATE_NAME = "SetBgpAdvertisementPathSize";

   private static final String PRECOMPUTED_BGP_ADVERTISEMENT_AS_PATH_PREDICATE_NAME = "SetBgpAdvertisementPath";

   private static final String PRECOMPUTED_BGP_ADVERTISEMENT_COMMUNITY_PREDICATE_NAME = "SetBgpAdvertisementCommunity";

   private static final String PRECOMPUTED_BGP_ADVERTISEMENTS_PREDICATE_NAME = "SetBgpAdvertisement_flat";

   private static final String PRECOMPUTED_IBGP_NEIGHBORS_PREDICATE_NAME = "SetIbgpNeighbors";

   private static final String PRECOMPUTED_ROUTES_PREDICATE_NAME = "SetPrecomputedRoute_flat";

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

   /**
    * The name of the [optional] topology file within a test-rig
    */
   private static final String TOPOLOGY_FILENAME = "topology.net";

   private static final String TRACE_PREFIX = "trace:";

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
         Map<String, StringBuilder> factBins, boolean addHeaders) {
      initFactBins(Facts.CONTROL_PLANE_FACT_COLUMN_HEADERS, factBins,
            addHeaders);
   }

   private static void initFactBins(Map<String, String> columnHeaderMap,
         Map<String, StringBuilder> factBins, boolean addHeaders) {
      for (String factPredicate : columnHeaderMap.keySet()) {
         if (addHeaders) {
            String columnHeaders = columnHeaderMap.get(factPredicate);
            String initialText = columnHeaders + "\n";
            factBins.put(factPredicate, new StringBuilder(initialText));
         }
         else {
            factBins.put(factPredicate, new StringBuilder());
         }
      }
   }

   private static void initTrafficFactBins(Map<String, StringBuilder> factBins) {
      initFactBins(Facts.TRAFFIC_FACT_COLUMN_HEADERS, factBins, true);
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
      logger.info("Parsing: \"" + filename + "\" ...");
      return parse(parser, logger, settings);
   }

   private EnvironmentSettings _baseEnvSettings;

   private EnvironmentSettings _diffEnvSettings;

   private EnvironmentSettings _envSettings;

   private BatfishLogger _logger;

   private LogicBloxFrontendManager _manager;

   private PredicateInfo _predicateInfo;

   private Settings _settings;

   private long _timerCount;

   private File _tmpLogicDir;

   public Batfish(Settings settings) {
      _settings = settings;
      _envSettings = settings.getActiveEnvironmentSettings();
      _baseEnvSettings = settings.getBaseEnvironmentSettings();
      _diffEnvSettings = settings.getDiffEnvironmentSettings();
      _logger = _settings.getLogger();
      _manager = new LogicBloxFrontendManager(settings, _logger, _envSettings);
      _tmpLogicDir = null;
   }

   private void addProject() {
      LogicBloxFrontend lbFrontend = _manager.connect();
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

   private void addStaticFacts(String blockName) {
      LogicBloxFrontend lbFrontend = _manager.connect();
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

   private void answer() {
      boolean dp = false;
      boolean diff = false;
      Question question = parseQuestion();
      switch (question.getType()) {
      case DESTINATION:
         answerDestination((DestinationQuestion) question);
         dp = true;
         diff = true;
         break;

      case FAILURE:
         answerFailure((FailureQuestion) question);
         dp = true;
         diff = true;
         break;

      case INGRESS_PATH:
         answerIngressPath((IngressPathQuestion) question);
         dp = true;
         diff = true;
         break;

      case LOCAL_PATH:
         answerLocalPath((LocalPathQuestion) question);
         dp = true;
         diff = true;
         break;

      case MULTIPATH:
         answerMultipath((MultipathQuestion) question);
         dp = true;
         break;

      case REACHABILITY:
         answerReachability((ReachabilityQuestion) question);
         dp = true;
         break;

      case TRACEROUTE:
         answerTraceroute((TracerouteQuestion) question);
         dp = true;
         break;

      case VERIFY:
         answerVerify((VerifyQuestion) question);
         break;

      default:
         throw new BatfishException("Unknown question type");
      }
      if (diff) {
         _settings.setPostFlows(false);
         _settings.setHistory(false);
      }
      else {
         _settings.setPostDifferentialFlows(false);
         _settings.setDifferentialHistory(false);
      }
      if (!dp) {
         _settings.setPostDifferentialFlows(false);
         _settings.setDifferentialHistory(false);
         _settings.setPostFlows(false);
         _settings.setHistory(false);
      }
   }

   private void answerDestination(DestinationQuestion question) {
      checkDifferentialDataPlaneQuestionDependencies();
      throw new UnsupportedOperationException(
            "no implementation for generated method"); // TODO Auto-generated
                                                       // method stub
   }

   private void answerFailure(FailureQuestion question) {
      checkDifferentialDataPlaneQuestionDependencies();
      String tag = getDifferentialFlowTag();
      _baseEnvSettings
            .setDumpFactsDir(_baseEnvSettings.getTrafficFactDumpDir());
      _diffEnvSettings
            .setDumpFactsDir(_diffEnvSettings.getTrafficFactDumpDir());

      // load base configurations and generate base data plane
      Map<String, Configuration> baseConfigurations = loadConfigurations(_baseEnvSettings);
      File baseDataPlanePath = new File(_baseEnvSettings.getDataPlanePath());
      Synthesizer baseDataPlaneSynthesizer = synthesizeDataPlane(
            baseConfigurations, baseDataPlanePath);

      // load diff configurations and generate diff data plane
      Map<String, Configuration> diffConfigurations = loadConfigurations(_diffEnvSettings);
      File diffDataPlanePath = new File(_diffEnvSettings.getDataPlanePath());
      Synthesizer diffDataPlaneSynthesizer = synthesizeDataPlane(
            diffConfigurations, diffDataPlanePath);

      Set<String> commonNodes = new TreeSet<String>();
      commonNodes.addAll(baseConfigurations.keySet());
      commonNodes.retainAll(diffConfigurations.keySet());

      NodeSet blacklistNodes = getNodeBlacklist(_diffEnvSettings);
      Set<NodeInterfacePair> blacklistInterfaces = getInterfaceBlacklist(_diffEnvSettings);
      EdgeSet blacklistEdges = getEdgeBlacklist(_diffEnvSettings);

      BlacklistDstIpQuerySynthesizer blacklistQuery = new BlacklistDstIpQuerySynthesizer(
            null, blacklistNodes, blacklistInterfaces, blacklistEdges,
            baseConfigurations);

      // compute composite program and flows
      List<Synthesizer> synthesizers = new ArrayList<Synthesizer>();
      synthesizers.add(baseDataPlaneSynthesizer);
      synthesizers.add(diffDataPlaneSynthesizer);
      synthesizers.add(baseDataPlaneSynthesizer);

      List<CompositeNodJob> jobs = new ArrayList<CompositeNodJob>();

      // generate base reachability and diff blackhole and blacklist queries
      for (String node : commonNodes) {
         ReachableQuerySynthesizer reachableQuery = new ReachableQuerySynthesizer(
               node, null);
         ReachableQuerySynthesizer blackHoleQuery = new ReachableQuerySynthesizer(
               node, null);
         blackHoleQuery.setNegate(true);
         NodeSet nodes = new NodeSet();
         nodes.add(node);
         List<QuerySynthesizer> queries = new ArrayList<QuerySynthesizer>();
         queries.add(reachableQuery);
         queries.add(blackHoleQuery);
         queries.add(blacklistQuery);
         CompositeNodJob job = new CompositeNodJob(synthesizers, queries,
               nodes, tag);
         jobs.add(job);
      }

      Set<Flow> flows = computeCompositeNodOutput(jobs);

      Map<String, StringBuilder> trafficFactBins = new LinkedHashMap<String, StringBuilder>();
      initTrafficFactBins(trafficFactBins);
      StringBuilder wSetFlowOriginate = trafficFactBins.get("SetFlowOriginate");
      for (Flow flow : flows) {
         wSetFlowOriginate.append(flow.toLBLine());
         _logger.debug("Found: " + flow.toString() + "\n");
      }
      dumpFacts(trafficFactBins, _baseEnvSettings);
      dumpFacts(trafficFactBins, _diffEnvSettings);
   }

   private void answerIngressPath(IngressPathQuestion question) {
      checkDifferentialDataPlaneQuestionDependencies();
      throw new UnsupportedOperationException(
            "no implementation for generated method"); // TODO Auto-generated
                                                       // method stub
   }

   private void answerLocalPath(LocalPathQuestion question) {
      checkDifferentialDataPlaneQuestionDependencies();
      String tag = getDifferentialFlowTag();
      _baseEnvSettings
            .setDumpFactsDir(_baseEnvSettings.getTrafficFactDumpDir());
      _diffEnvSettings
            .setDumpFactsDir(_diffEnvSettings.getTrafficFactDumpDir());

      // load base configurations and generate base data plane
      Map<String, Configuration> baseConfigurations = loadConfigurations(_baseEnvSettings);
      File baseDataPlanePath = new File(_baseEnvSettings.getDataPlanePath());
      Synthesizer baseDataPlaneSynthesizer = synthesizeDataPlane(
            baseConfigurations, baseDataPlanePath);

      // load diff configurations and generate diff data plane
      Map<String, Configuration> diffConfigurations = loadConfigurations(_diffEnvSettings);
      File diffDataPlanePath = new File(_diffEnvSettings.getDataPlanePath());
      Synthesizer diffDataPlaneSynthesizer = synthesizeDataPlane(
            diffConfigurations, diffDataPlanePath);

      Set<String> commonNodes = new TreeSet<String>();
      commonNodes.addAll(baseConfigurations.keySet());
      commonNodes.retainAll(diffConfigurations.keySet());

      NodeSet blacklistNodes = getNodeBlacklist(_diffEnvSettings);
      Set<NodeInterfacePair> blacklistInterfaces = getInterfaceBlacklist(_diffEnvSettings);
      EdgeSet blacklistEdges = getEdgeBlacklist(_diffEnvSettings);

      BlacklistDstIpQuerySynthesizer blacklistQuery = new BlacklistDstIpQuerySynthesizer(
            null, blacklistNodes, blacklistInterfaces, blacklistEdges,
            baseConfigurations);

      // compute composite program and flows
      List<Synthesizer> commonEdgeSynthesizers = new ArrayList<Synthesizer>();
      commonEdgeSynthesizers.add(baseDataPlaneSynthesizer);
      commonEdgeSynthesizers.add(diffDataPlaneSynthesizer);
      commonEdgeSynthesizers.add(baseDataPlaneSynthesizer);

      List<CompositeNodJob> jobs = new ArrayList<CompositeNodJob>();

      // generate local edge reachability and black hole queries
      Topology diffTopology = loadTopology(_diffEnvSettings);
      EdgeSet diffEdges = diffTopology.getEdges();
      for (Edge edge : diffEdges) {
         String ingressNode = edge.getNode1();
         ReachEdgeQuerySynthesizer reachQuery = new ReachEdgeQuerySynthesizer(
               ingressNode, edge, true);
         ReachEdgeQuerySynthesizer noReachQuery = new ReachEdgeQuerySynthesizer(
               ingressNode, edge, false);
         noReachQuery.setNegate(true);
         List<QuerySynthesizer> queries = new ArrayList<QuerySynthesizer>();
         queries.add(reachQuery);
         queries.add(noReachQuery);
         queries.add(blacklistQuery);
         NodeSet nodes = new NodeSet();
         nodes.add(ingressNode);
         CompositeNodJob job = new CompositeNodJob(commonEdgeSynthesizers,
               queries, nodes, tag);
         jobs.add(job);
      }

      // we also need queries for nodes next to edges that are now missing, in
      // the case that those nodes still exist
      List<Synthesizer> missingEdgeSynthesizers = new ArrayList<Synthesizer>();
      missingEdgeSynthesizers.add(baseDataPlaneSynthesizer);
      missingEdgeSynthesizers.add(baseDataPlaneSynthesizer);
      Topology baseTopology = loadTopology(_baseEnvSettings);
      EdgeSet baseEdges = baseTopology.getEdges();
      EdgeSet missingEdges = new EdgeSet();
      missingEdges.addAll(baseEdges);
      missingEdges.removeAll(diffEdges);
      for (Edge missingEdge : missingEdges) {
         String ingressNode = missingEdge.getNode1();
         if (diffConfigurations.containsKey(ingressNode)) {
            ReachEdgeQuerySynthesizer reachQuery = new ReachEdgeQuerySynthesizer(
                  ingressNode, missingEdge, true);
            List<QuerySynthesizer> queries = new ArrayList<QuerySynthesizer>();
            queries.add(reachQuery);
            queries.add(blacklistQuery);
            NodeSet nodes = new NodeSet();
            nodes.add(ingressNode);
            CompositeNodJob job = new CompositeNodJob(missingEdgeSynthesizers,
                  queries, nodes, tag);
            jobs.add(job);
         }

      }

      Set<Flow> flows = computeCompositeNodOutput(jobs);

      Map<String, StringBuilder> trafficFactBins = new LinkedHashMap<String, StringBuilder>();
      initTrafficFactBins(trafficFactBins);
      StringBuilder wSetFlowOriginate = trafficFactBins.get("SetFlowOriginate");
      for (Flow flow : flows) {
         wSetFlowOriginate.append(flow.toLBLine());
         _logger.output(flow.toString() + "\n");
      }
      dumpFacts(trafficFactBins, _baseEnvSettings);
      dumpFacts(trafficFactBins, _diffEnvSettings);
   }

   private void answerMultipath(MultipathQuestion question) {
      checkDataPlaneQuestionDependencies();
      String tag = getFlowTag();
      _envSettings.setDumpFactsDir(_envSettings.getTrafficFactDumpDir());
      Map<String, Configuration> configurations = loadConfigurations();
      File dataPlanePath = new File(_envSettings.getDataPlanePath());
      Set<Flow> flows = null;
      Synthesizer dataPlaneSynthesizer = synthesizeDataPlane(configurations,
            dataPlanePath);
      List<NodJob> jobs = new ArrayList<NodJob>();
      for (String node : configurations.keySet()) {
         MultipathInconsistencyQuerySynthesizer query = new MultipathInconsistencyQuerySynthesizer(
               node);
         NodeSet nodes = new NodeSet();
         nodes.add(node);
         NodJob job = new NodJob(dataPlaneSynthesizer, query, nodes, tag);
         jobs.add(job);
      }

      flows = computeNodOutput(jobs);
      Map<String, StringBuilder> trafficFactBins = new LinkedHashMap<String, StringBuilder>();
      initTrafficFactBins(trafficFactBins);
      StringBuilder wSetFlowOriginate = trafficFactBins.get("SetFlowOriginate");
      for (Flow flow : flows) {
         wSetFlowOriginate.append(flow.toLBLine());
      }
      dumpFacts(trafficFactBins);
   }

   private void answerReachability(ReachabilityQuestion question) {
      checkDataPlaneQuestionDependencies();
      String tag = getFlowTag();
      _envSettings.setDumpFactsDir(_envSettings.getTrafficFactDumpDir());
      Map<String, Configuration> configurations = loadConfigurations();
      File dataPlanePath = new File(_envSettings.getDataPlanePath());
      Set<Flow> flows = null;
      Synthesizer dataPlaneSynthesizer = synthesizeDataPlane(configurations,
            dataPlanePath);

      // collect ingress nodes
      Pattern ingressNodeRegex = question.getIngressNodeRegex();
      Set<String> activeIngressNodes = new TreeSet<String>();
      if (ingressNodeRegex != null) {
         for (String node : configurations.keySet()) {
            Matcher ingressNodeMatcher = ingressNodeRegex.matcher(node);
            if (ingressNodeMatcher.matches()) {
               activeIngressNodes.add(node);
            }
         }
      }
      else {
         activeIngressNodes.addAll(configurations.keySet());
      }

      // collect final nodes
      Pattern finalNodeRegex = question.getFinalNodeRegex();
      Set<String> activeFinalNodes = new TreeSet<String>();
      if (finalNodeRegex != null) {
         for (String node : configurations.keySet()) {
            Matcher finalNodeMatcher = finalNodeRegex.matcher(node);
            if (finalNodeMatcher.matches()) {
               activeFinalNodes.add(node);
            }
         }
      }
      else {
         activeFinalNodes.addAll(configurations.keySet());
      }

      // build query jobs
      List<NodJob> jobs = new ArrayList<NodJob>();
      for (String ingressNode : activeIngressNodes) {
         ReachabilityQuerySynthesizer query = new ReachabilityQuerySynthesizer(
               question.getActions(), question.getDstPrefixes(),
               question.getDstPortRange(), activeFinalNodes,
               Collections.singleton(ingressNode),
               question.getIpProtocolRange(), question.getSrcPrefixes(),
               question.getSrcPortRange());
         NodeSet nodes = new NodeSet();
         nodes.add(ingressNode);
         NodJob job = new NodJob(dataPlaneSynthesizer, query, nodes, tag);
         jobs.add(job);
      }

      // run jobs and get resulting flows
      flows = computeNodOutput(jobs);

      // dump flows to disk
      Map<String, StringBuilder> trafficFactBins = new LinkedHashMap<String, StringBuilder>();
      initTrafficFactBins(trafficFactBins);
      StringBuilder wSetFlowOriginate = trafficFactBins.get("SetFlowOriginate");
      for (Flow flow : flows) {
         wSetFlowOriginate.append(flow.toLBLine());
      }
      dumpFacts(trafficFactBins);
   }

   private void answerTraceroute(TracerouteQuestion question) {
      checkDataPlaneQuestionDependencies();
      _envSettings.setDumpFactsDir(_envSettings.getTrafficFactDumpDir());
      Set<Flow> flows = question.getFlows();
      Map<String, StringBuilder> trafficFactBins = new LinkedHashMap<String, StringBuilder>();
      initTrafficFactBins(trafficFactBins);
      StringBuilder wSetFlowOriginate = trafficFactBins.get("SetFlowOriginate");
      for (Flow flow : flows) {
         wSetFlowOriginate.append(flow.toLBLine());
      }
      dumpFacts(trafficFactBins);
   }

   private void answerVerify(VerifyQuestion question) {
      checkConfigurations();
      Map<String, Configuration> configurations = loadConfigurations();
      VerifyProgram program = question.getProgram();
      program.execute(configurations, _logger, _settings);
      if (program.getAssertions()) {
         int totalAssertions = program.getTotalAssertions();
         int failedAssertions = program.getFailedAssertions();
         int passedAssertions = totalAssertions - failedAssertions;
         double percentPassed = 100 * ((double) passedAssertions)
               / totalAssertions;
         _logger.outputf("%d/%d (%.1f%%) assertions passed.\n",
               passedAssertions, totalAssertions, percentPassed);
         if (!program.getUnsafe()) {
            _logger.output("No violations detected\n");
         }
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
                           && !name.equals("pedantic.logic")
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
         String input = Util.readFile(logicFilePath.toFile());
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

   private void checkComputeControlPlaneFacts() {
      checkConfigurations();
      checkEnvironmentExists(_baseEnvSettings);
      if (_settings.getDiffActive()) {
         checkDataPlane(_baseEnvSettings);
         checkDiffEnvironmentExists();
      }
   }

   private void checkConfigurations() {
      String serializedConfigPath = _settings.getSerializeIndependentPath();
      File dir = new File(serializedConfigPath);
      File[] serializedConfigs = dir.listFiles();
      if (serializedConfigs == null) {
         throw new CleanBatfishException(
               "Missing compiled vendor-independent configurations for this test-rig\n");
      }
      else if (serializedConfigs.length == 0) {
         throw new CleanBatfishException(
               "Nothing to do: Set of vendor-independent configurations for this test-rig is empty\n");
      }
   }

   private void checkDataPlane(EnvironmentSettings envSettings) {
      String dpPath = envSettings.getDataPlanePath();
      File dp = new File(dpPath);
      if (!dp.exists()) {
         throw new CleanBatfishException("Missing data plane for environment: "
               + envSettings.getName() + "\n");
      }
   }

   private void checkDataPlaneQuestionDependencies() {
      checkDataPlaneQuestionDependencies(_envSettings);
   }

   private void checkDataPlaneQuestionDependencies(
         EnvironmentSettings envSettings) {
      checkConfigurations();
      checkDataPlane(envSettings);
   }

   private void checkDiffEnvironmentExists() {
      checkDiffEnvironmentSpecified();
      checkEnvironmentExists(_diffEnvSettings);
   }

   private void checkDiffEnvironmentSpecified() {
      if (_settings.getDiffEnvironmentName() == null) {
         throw new CleanBatfishException(
               "No differential environment specified for differential question");
      }
   }

   private void checkDifferentialDataPlaneQuestionDependencies() {
      checkDiffEnvironmentSpecified();
      checkConfigurations();
      checkDataPlane(_baseEnvSettings);
      checkDataPlane(_diffEnvSettings);
   }

   private void checkEnvironmentExists(EnvironmentSettings envSettings) {
      if (!new File(envSettings.getDataPlanePath()).getParentFile().exists()) {
         throw new CleanBatfishException("Environment not initialized: \""
               + envSettings.getName() + "\"");
      }
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
      if (_manager != null) {
         _manager.close();
      }
   }

   private Set<Flow> computeCompositeNodOutput(List<CompositeNodJob> jobs) {
      _logger.info("\n*** EXECUTING COMPOSITE NOD JOBS ***\n");
      resetTimer();
      Set<Flow> flows = new TreeSet<Flow>();
      BatfishJobExecutor<CompositeNodJob, NodJobResult, Set<Flow>> executor = new BatfishJobExecutor<CompositeNodJob, NodJobResult, Set<Flow>>(
            _settings, _logger);
      executor.executeJobs(jobs, flows);
      printElapsedTime();
      return flows;
   }

   private void computeControlPlaneFacts(Map<String, StringBuilder> cpFactBins) {
      checkComputeControlPlaneFacts();
      if (_settings.getUsePrecomputedRoutes()) {
         Set<String> precomputedRoutesPaths = _settings
               .getPrecomputedRoutesPaths();
         String precomputedRoutesPath = _settings.getPrecomputedRoutesPath();
         if (precomputedRoutesPaths == null) {
            if (precomputedRoutesPath == null) {
               throw new BatfishException(
                     "Must specify path(s) to precomputed routes");
            }
            else {
               precomputedRoutesPaths = Collections
                     .singleton(precomputedRoutesPath);
            }
         }
         populatePrecomputedRoutes(precomputedRoutesPaths, cpFactBins);
      }
      if (_settings.getUsePrecomputedIbgpNeighbors()) {
         populatePrecomputedIbgpNeighbors(
               _settings.getPrecomputedIbgpNeighborsPath(), cpFactBins);
      }
      if (_settings.getUsePrecomputedBgpAdvertisements()) {
         populatePrecomputedBgpAdvertisements(
               _settings.getPrecomputedBgpAdvertisementsPath(), cpFactBins);
      }
      boolean differentialContext = _settings.getDiffActive();
      Map<String, Configuration> configurations = loadConfigurations();
      Topology topology = computeTopology(_settings.getTestRigPath(),
            configurations, cpFactBins);
      String edgeBlacklistPath = _envSettings.getEdgeBlacklistPath();
      String serializedTopologyPath = _envSettings.getSerializedTopologyPath();
      InterfaceSet flowSinks = null;
      if (differentialContext) {
         flowSinks = getFlowSinkSet(_baseEnvSettings.getDataPlanePath());
      }
      EdgeSet blacklistEdges = getEdgeBlacklist(_envSettings);
      if (edgeBlacklistPath != null) {
         File edgeBlacklistPathAsFile = new File(edgeBlacklistPath);
         if (edgeBlacklistPathAsFile.exists()) {
            EdgeSet edges = topology.getEdges();
            edges.removeAll(blacklistEdges);
         }
      }
      NodeSet blacklistNodes = getNodeBlacklist(_envSettings);
      if (blacklistNodes != null) {
         if (differentialContext) {
            flowSinks.removeNodes(blacklistNodes);
         }
         for (String blacklistNode : blacklistNodes) {
            topology.removeNode(blacklistNode);
         }
      }
      Set<NodeInterfacePair> blacklistInterfaces = getInterfaceBlacklist(_envSettings);
      if (blacklistInterfaces != null) {
         for (NodeInterfacePair blacklistInterface : blacklistInterfaces) {
            topology.removeInterface(blacklistInterface);
            if (differentialContext) {
               flowSinks.remove(blacklistInterface);
            }
         }
      }
      if (!differentialContext) {
         flowSinks = computeFlowSinks(configurations, topology);
      }
      writeTopologyFacts(topology, cpFactBins);
      writeConfigurationFacts(configurations, cpFactBins);
      writeFlowSinkFacts(flowSinks, cpFactBins);
      if (!_logger.isActive(BatfishLogger.LEVEL_INFO)) {
         _logger.output("LogicBlox facts generated successfully.\n");
      }
      if (_settings.getDumpControlPlaneFacts()) {
         dumpFacts(cpFactBins);
      }
      if (_settings.getFacts()) {
         // serialize topology
         File topologyPath = new File(serializedTopologyPath);
         _logger.info("Serializing topology...");
         serializeObject(topology, topologyPath);
         _logger.info("OK\n");
      }
   }

   private void computeDataPlane() {
      String dataPlanePath = _envSettings.getDataPlanePath();
      if (dataPlanePath == null) {
         throw new BatfishException("Missing path to data plane");
      }
      File dataPlanePathAsFile = new File(dataPlanePath);
      computeDataPlane(dataPlanePathAsFile);
   }

   private void computeDataPlane(File dataPlanePath) {
      LogicBloxFrontend lbFrontend = _manager.connect();
      _logger.info("\n*** COMPUTING DATA PLANE STRUCTURES ***\n");
      resetTimer();

      lbFrontend.initEntityTable();

      _logger.info("Retrieving flow sink information from LogicBlox...");
      InterfaceSet flowSinks = getFlowSinkSet(lbFrontend);
      _logger.info("OK\n");

      Topology topology = loadTopology();
      EdgeSet topologyEdges = topology.getEdges();

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
      DataPlane dataPlane = new DataPlane(flowSinks, topologyEdges, fibs,
            policyRouteFibNodeMap);
      _logger.info("Serializing data plane...");
      serializeObject(dataPlane, dataPlanePath);
      _logger.info("OK\n");

      printElapsedTime();
   }

   private InterfaceSet computeFlowSinks(
         Map<String, Configuration> configurations, Topology topology) {
      InterfaceSet flowSinks = new InterfaceSet();
      InterfaceSet topologyInterfaces = new InterfaceSet();
      for (Edge edge : topology.getEdges()) {
         topologyInterfaces.add(edge.getInterface1());
         topologyInterfaces.add(edge.getInterface2());
      }
      for (Configuration node : configurations.values()) {
         String hostname = node.getHostname();
         for (Interface iface : node.getInterfaces().values()) {
            String ifaceName = iface.getName();
            NodeInterfacePair p = new NodeInterfacePair(hostname, ifaceName);
            if (iface.getActive() && !iface.isLoopback(node.getVendor())
                  && !topologyInterfaces.contains(p)) {
               flowSinks.add(p);
            }
         }
      }
      return flowSinks;
   }

   private Set<Flow> computeNodOutput(List<NodJob> jobs) {
      _logger.info("\n*** EXECUTING NOD JOBS ***\n");
      resetTimer();
      Set<Flow> flows = new TreeSet<Flow>();
      BatfishJobExecutor<NodJob, NodJobResult, Set<Flow>> executor = new BatfishJobExecutor<NodJob, NodJobResult, Set<Flow>>(
            _settings, _logger);
      executor.executeJobs(jobs, flows);
      printElapsedTime();
      return flows;
   }

   public Topology computeTopology(String testRigPath,
         Map<String, Configuration> configurations,
         Map<String, StringBuilder> factBins) {
      Path topologyFilePath = Paths.get(testRigPath, TOPOLOGY_FILENAME);
      Topology topology;
      // Get generated facts from topology file
      if (Files.exists(topologyFilePath)) {
         topology = processTopologyFile(topologyFilePath.toFile(), factBins);
      }
      else {
         // guess adjacencies based on interface subnetworks
         _logger
               .info("*** (GUESSING TOPOLOGY IN ABSENCE OF EXPLICIT FILE) ***\n");
         EdgeSet edges = synthesizeTopology(configurations);
         topology = new Topology(edges);
      }
      return topology;
   }

   private Map<String, Configuration> convertConfigurations(
         Map<String, VendorConfiguration> vendorConfigurations) {
      _logger
            .info("\n*** CONVERTING VENDOR CONFIGURATIONS TO INDEPENDENT FORMAT ***\n");
      resetTimer();
      Map<String, Configuration> configurations = new TreeMap<String, Configuration>();
      List<ConvertConfigurationJob> jobs = new ArrayList<ConvertConfigurationJob>();
      for (String hostname : vendorConfigurations.keySet()) {
         Warnings warnings = new Warnings(_settings.getPedanticAsError(),
               _settings.getPedanticRecord()
                     && _logger.isActive(BatfishLogger.LEVEL_PEDANTIC),
               _settings.getRedFlagAsError(), _settings.getRedFlagRecord()
                     && _logger.isActive(BatfishLogger.LEVEL_REDFLAG),
               _settings.getUnimplementedAsError(),
               _settings.getUnimplementedRecord()
                     && _logger.isActive(BatfishLogger.LEVEL_UNIMPLEMENTED),
               _settings.printParseTree());
         VendorConfiguration vc = vendorConfigurations.get(hostname);
         ConvertConfigurationJob job = new ConvertConfigurationJob(_settings,
               vc, hostname, warnings);
         jobs.add(job);
      }
      BatfishJobExecutor<ConvertConfigurationJob, ConvertConfigurationResult, Map<String, Configuration>> executor = new BatfishJobExecutor<ConvertConfigurationJob, ConvertConfigurationResult, Map<String, Configuration>>(
            _settings, _logger);
      executor.executeJobs(jobs, configurations);
      printElapsedTime();
      return configurations;
   }

   private void createWorkspace() {
      addProject();
      String lbHostnamePath = _envSettings.getJobLogicBloxHostnamePath();
      String lbHostname = _settings.getServiceLogicBloxHostname();
      if (lbHostname == null) {
         lbHostname = "localhost";
      }
      if (lbHostnamePath != null) {
         writeFile(lbHostnamePath, lbHostname);
      }
   }

   private void deleteWorkspace() {
      LogicBloxFrontend lbFrontend = _manager.connect();
      lbFrontend.deleteWorkspace();
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
         _logger.info("Reading vendor config: \"" + serializedConfig + "\"");
         Object object = deserializeObject(serializedConfig);
         VendorConfiguration vc = (VendorConfiguration) object;
         vendorConfigurations.put(name, vc);
         _logger.info("...OK\n");
      }
      printElapsedTime();
      return vendorConfigurations;
   }

   private void dumpFacts(Map<String, StringBuilder> factBins) {
      dumpFacts(factBins, _envSettings);
   }

   private void dumpFacts(Map<String, StringBuilder> factBins,
         EnvironmentSettings envSettings) {
      _logger.info("\n*** DUMPING FACTS ***\n");
      resetTimer();
      Path factsDir = Paths.get(envSettings.getDumpFactsDir());
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
      List<FlattenVendorConfigurationJob> jobs = new ArrayList<FlattenVendorConfigurationJob>();
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
               BfConsts.RELPATH_CONFIGURATIONS_DIR, name).toFile();
         FlattenVendorConfigurationJob job = new FlattenVendorConfigurationJob(
               _settings, fileText, inputFile, outputFile, warnings);
         jobs.add(job);
      }
      BatfishJobExecutor<FlattenVendorConfigurationJob, FlattenVendorConfigurationResult, Map<File, String>> executor = new BatfishJobExecutor<FlattenVendorConfigurationJob, FlattenVendorConfigurationResult, Map<File, String>>(
            _settings, _logger);
      executor.executeJobs(jobs, outputConfigurationData);
      printElapsedTime();
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
         String topologyFileText = Util.readFile(inputTopologyPath.toFile());
         writeFile(outputTopologyPath.toString(), topologyFileText);
      }
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
         String interfaceDescriptionRegex) {
      Map<String, Configuration> configs = loadConfigurations();
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
      serializeIndependentConfigs(stubConfigurations,
            _settings.getSerializeIndependentPath());

   }

   private AdvertisementSet getAdvertisements(LogicBloxFrontend lbFrontend) {
      AdvertisementSet adverts = new AdvertisementSet();
      String qualifiedName = _predicateInfo.getPredicateNames().get(
            BGP_ADVERTISEMENT_ROUTE_PREDICATE_NAME);
      Relation bgpAdvertisementRouteRelation = lbFrontend
            .queryPredicate(qualifiedName);
      lbFrontend.fillBgpAdvertisementColumn(adverts,
            bgpAdvertisementRouteRelation.getColumns().get(0));
      return adverts;
   }

   public Map<String, Configuration> getConfigurations(
         String serializedVendorConfigPath) {
      Map<String, VendorConfiguration> vendorConfigurations = deserializeVendorConfigurations(serializedVendorConfigPath);
      Map<String, Configuration> configurations = convertConfigurations(vendorConfigurations);
      return configurations;
   }

   private Map<String, Configuration> getDeltaConfigurations(
         EnvironmentSettings envSettings) {
      String deltaConfigurationsDir = envSettings.getDeltaConfigurationsDir();
      if (deltaConfigurationsDir != null) {
         File deltaConfigurationsDirAsFile = new File(deltaConfigurationsDir);
         if (deltaConfigurationsDirAsFile.exists()) {
            File configParentDir = deltaConfigurationsDirAsFile.getParentFile();
            Map<File, String> deltaConfigsText = readConfigurationFiles(configParentDir
                  .toString());
            Map<String, VendorConfiguration> vendorDeltaConfigs = parseVendorConfigurations(deltaConfigsText);
            Map<String, Configuration> deltaConfigs = convertConfigurations(vendorDeltaConfigs);
            return deltaConfigs;
         }
      }
      return Collections.<String, Configuration> emptyMap();
   }

   private String getDifferentialFlowTag() {
      return _settings.getQuestionName() + ":" + _baseEnvSettings.getName()
            + ":" + _diffEnvSettings.getName();
   }

   private void getDifferentialHistory() {
      LogicBloxFrontend baseLbFrontend = _manager.connect(_baseEnvSettings);
      LogicBloxFrontend diffLbFrontend = _manager.connect(_diffEnvSettings);
      baseLbFrontend.initEntityTable();
      diffLbFrontend.initEntityTable();
      String tag = getDifferentialFlowTag();
      FlowHistory flowHistory = new FlowHistory();
      populateFlowHistory(flowHistory, baseLbFrontend,
            _baseEnvSettings.getName(), tag);
      populateFlowHistory(flowHistory, diffLbFrontend,
            _diffEnvSettings.getName(), tag);
      _logger.output(flowHistory.toString());
   }

   private EdgeSet getEdgeBlacklist(EnvironmentSettings envSettings) {
      EdgeSet blacklistEdges = null;
      String edgeBlacklistPath = envSettings.getEdgeBlacklistPath();
      if (edgeBlacklistPath != null) {
         File edgeBlacklistPathAsFile = new File(edgeBlacklistPath);
         if (edgeBlacklistPathAsFile.exists()) {
            Topology blacklistTopology = parseTopology(edgeBlacklistPathAsFile);
            blacklistEdges = blacklistTopology.getEdges();
         }
      }
      return blacklistEdges;
   }

   private double getElapsedTime(long beforeTime) {
      long difference = System.currentTimeMillis() - beforeTime;
      double seconds = difference / 1000d;
      return seconds;
   }

   private InterfaceSet getFlowSinkSet(LogicBloxFrontend lbFrontend) {
      InterfaceSet flowSinks = new InterfaceSet();
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
         NodeInterfacePair f = new NodeInterfacePair(node, iface);
         flowSinks.add(f);
      }
      return flowSinks;
   }

   private InterfaceSet getFlowSinkSet(String dataPlanePath) {
      _logger.info("Deserializing data plane: \"" + dataPlanePath + "\"...");
      File dataPlanePathAsFile = new File(dataPlanePath);
      DataPlane dataPlane = (DataPlane) deserializeObject(dataPlanePathAsFile);
      _logger.info("OK\n");
      return dataPlane.getFlowSinks();
   }

   private String getFlowTag() {
      return _settings.getQuestionName() + ":" + _envSettings.getName();
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

   private void getHistory() {
      LogicBloxFrontend lbFrontend = _manager.connect();
      lbFrontend.initEntityTable();
      String tag = getFlowTag();
      FlowHistory flowHistory = new FlowHistory();
      populateFlowHistory(flowHistory, lbFrontend, _envSettings.getName(), tag);
      _logger.output(flowHistory.toString());
   }

   private Set<NodeInterfacePair> getInterfaceBlacklist(
         EnvironmentSettings envSettings) {
      Set<NodeInterfacePair> blacklistInterfaces = null;
      String interfaceBlacklistPath = envSettings.getInterfaceBlacklistPath();
      if (interfaceBlacklistPath != null) {
         File interfaceBlacklistPathAsFile = new File(interfaceBlacklistPath);
         if (interfaceBlacklistPathAsFile.exists()) {
            blacklistInterfaces = parseInterfaceBlacklist(interfaceBlacklistPathAsFile);
         }
      }
      return blacklistInterfaces;
   }

   private NodeSet getNodeBlacklist(EnvironmentSettings envSettings) {
      NodeSet blacklistNodes = null;
      String nodeBlacklistPath = envSettings.getNodeBlacklistPath();
      if (nodeBlacklistPath != null) {
         File nodeBlacklistPathAsFile = new File(nodeBlacklistPath);
         if (nodeBlacklistPathAsFile.exists()) {
            blacklistNodes = parseNodeBlacklist(nodeBlacklistPathAsFile);
         }
      }
      return blacklistNodes;
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

   private RouteSet getRoutes(LogicBloxFrontend lbFrontend) {
      RouteSet routes = new RouteSet();
      String qualifiedName = _predicateInfo.getPredicateNames().get(
            INSTALLED_ROUTE_PREDICATE_NAME);
      Relation installedRoutesRelation = lbFrontend
            .queryPredicate(qualifiedName);
      lbFrontend.fillRouteColumn(routes, installedRoutesRelation.getColumns()
            .get(0));
      return routes;
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
               String contents = Util.readFile(file.toFile());
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

   private Set<Edge> getSymmetricEdgePairs(EdgeSet edges) {
      LinkedHashSet<Edge> consumedEdges = new LinkedHashSet<Edge>();
      for (Edge edge : edges) {
         if (consumedEdges.contains(edge)) {
            continue;
         }
         Edge reverseEdge = new Edge(edge.getInterface2(), edge.getInterface1());
         consumedEdges.add(edge);
         consumedEdges.add(reverseEdge);
      }
      return consumedEdges;
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

   private void keepBlocks(List<String> blockNames) {
      LogicBloxFrontend lbFrontend = _manager.connect();
      Set<String> allBlockNames = new LinkedHashSet<String>();
      allBlockNames.addAll(Block.BLOCKS.keySet());
      for (String blockName : blockNames) {
         Block block = Block.BLOCKS.get(blockName);
         if (block == null) {
            throw new BatfishException("Invalid block name: \"" + blockName
                  + "\"");
         }
         Set<Block> dependencies = block.getDependencies();
         for (Block dependency : dependencies) {
            allBlockNames.remove(dependency.getName());
         }
         allBlockNames.remove(blockName);
      }
      List<String> qualifiedBlockNames = new ArrayList<String>();
      for (String blockName : allBlockNames) {
         String qualifiedBlockName = LB_BATFISH_LIBRARY_NAME + ":" + blockName
               + "_rules";
         qualifiedBlockNames.add(qualifiedBlockName);
      }
      lbFrontend.removeBlocks(qualifiedBlockNames);
   }

   public Map<String, Configuration> loadConfigurations() {
      return loadConfigurations(_envSettings);
   }

   public Map<String, Configuration> loadConfigurations(
         EnvironmentSettings envSettings) {
      Map<String, Configuration> configurations = deserializeConfigurations(_settings
            .getSerializeIndependentPath());
      processNodeBlacklist(configurations, envSettings);
      processInterfaceBlacklist(configurations, envSettings);
      processDeltaConfigurations(configurations, envSettings);
      return configurations;
   }

   private Topology loadTopology() {
      return loadTopology(_envSettings);
   }

   private Topology loadTopology(EnvironmentSettings envSettings) {
      String topologyPath = envSettings.getSerializedTopologyPath();
      File topologyPathFile = new File(topologyPath);
      _logger.info("Deserializing topology...");
      Topology topology = (Topology) deserializeObject(topologyPathFile);
      _logger.info("OK\n");
      return topology;
   }

   private ParserRuleContext parse(BatfishCombinedParser<?, ?> parser) {
      return parse(parser, _logger, _settings);
   }

   private ParserRuleContext parse(BatfishCombinedParser<?, ?> parser,
         String filename) {
      _logger.info("Parsing: \"" + filename + "\"...");
      return parse(parser);
   }

   private Set<NodeInterfacePair> parseInterfaceBlacklist(
         File interfaceBlacklistPath) {
      Set<NodeInterfacePair> ifaces = new TreeSet<NodeInterfacePair>();
      String interfaceBlacklistText = Util.readFile(interfaceBlacklistPath);
      String[] interfaceBlacklistLines = interfaceBlacklistText.split("\n");
      for (String interfaceBlacklistLine : interfaceBlacklistLines) {
         String trimmedLine = interfaceBlacklistLine.trim();
         if (trimmedLine.length() > 0) {
            String[] parts = trimmedLine.split(":");
            if (parts.length != 2) {
               throw new BatfishException(
                     "Invalid node-interface pair format: " + trimmedLine);
            }
            String hostname = parts[0];
            String iface = parts[1];
            NodeInterfacePair p = new NodeInterfacePair(hostname, iface);
            ifaces.add(p);
         }
      }
      return ifaces;
   }

   private NodeSet parseNodeBlacklist(File nodeBlacklistPath) {
      NodeSet nodeSet = new NodeSet();
      String nodeBlacklistText = Util.readFile(nodeBlacklistPath);
      String[] nodeBlacklistLines = nodeBlacklistText.split("\n");
      for (String nodeBlacklistLine : nodeBlacklistLines) {
         String hostname = nodeBlacklistLine.trim();
         if (hostname.length() > 0) {
            nodeSet.add(hostname);
         }
      }
      return nodeSet;
   }

   private NodeRoleMap parseNodeRoles(String testRigPath) {
      Path rolePath = Paths.get(testRigPath, "node_roles");
      String roleFileText = Util.readFile(rolePath.toFile());
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

   private Question parseQuestion() {
      String questionPath = _settings.getQuestionPath();
      File questionFile = new File(questionPath);
      _logger.info("Reading question file: \"" + questionPath + "\"...");
      String questionText = Util.readFile(questionFile);
      _logger.info("OK\n");
      QuestionParameters parameters = parseQuestionParameters();
      QuestionCombinedParser parser = new QuestionCombinedParser(questionText,
            _settings.getThrowOnParserError(), _settings.getThrowOnLexerError());
      QuestionExtractor extractor = new QuestionExtractor(parser, getFlowTag(),
            parameters);
      try {
         ParserRuleContext tree = parse(parser, questionPath);
         _logger.info("\tPost-processing...");
         extractor.processParseTree(tree);
         _logger.info("OK\n");
      }
      catch (CleanBatfishException e) {
         throw e;
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

   private QuestionParameters parseQuestionParameters() {
      String questionParametersPath = _settings.getQuestionParametersPath();
      File questionParametersFile = new File(questionParametersPath);
      if (!questionParametersFile.exists()) {
         return null;
      }
      _logger.info("Reading question parametersfile: \""
            + questionParametersPath + "\"...");
      String questionText = Util.readFile(questionParametersFile);
      _logger.info("OK\n");
      QuestionParametersCombinedParser parser = new QuestionParametersCombinedParser(
            questionText, _settings.getThrowOnParserError(),
            _settings.getThrowOnLexerError());
      QuestionParametersExtractor extractor = new QuestionParametersExtractor();
      try {
         ParserRuleContext tree = parse(parser, questionParametersPath);
         _logger.info("\tPost-processing...");
         extractor.processParseTree(tree);
         _logger.info("OK\n");
      }
      catch (ParserBatfishException e) {
         String error = "Error parsing question parameters: \""
               + questionParametersPath + "\"";
         throw new BatfishException(error, e);
      }
      catch (Exception e) {
         String error = "Error post-processing parse tree of question parameters file: \""
               + questionParametersPath + "\"";
         throw new BatfishException(error, e);
      }
      return extractor.getParameters();
   }

   private Topology parseTopology(File topologyFilePath) {
      _logger.info("*** PARSING TOPOLOGY ***\n");
      resetTimer();
      String topologyFileText = Util.readFile(topologyFilePath);
      BatfishCombinedParser<?, ?> parser = null;
      TopologyExtractor extractor = null;
      _logger.info("Parsing: \""
            + topologyFilePath.getAbsolutePath().toString() + "\" ...");
      if (topologyFileText.startsWith("autostart")) {
         parser = new GNS3TopologyCombinedParser(topologyFileText,
               _settings.getThrowOnParserError(),
               _settings.getThrowOnLexerError());
         extractor = new GNS3TopologyExtractor();
      }
      else if (topologyFileText
            .startsWith(BatfishTopologyCombinedParser.HEADER)) {
         parser = new BatfishTopologyCombinedParser(topologyFileText,
               _settings.getThrowOnParserError(),
               _settings.getThrowOnLexerError());
         extractor = new BatfishTopologyExtractor();
      }
      else if (topologyFileText.equals("")) {
         throw new BatfishException("ERROR: empty topology\n");
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
      Map<String, VendorConfiguration> vendorConfigurations = new TreeMap<String, VendorConfiguration>();
      List<ParseVendorConfigurationJob> jobs = new ArrayList<ParseVendorConfigurationJob>();
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
      BatfishJobExecutor<ParseVendorConfigurationJob, ParseVendorConfigurationResult, Map<String, VendorConfiguration>> executor = new BatfishJobExecutor<ParseVendorConfigurationJob, ParseVendorConfigurationResult, Map<String, VendorConfiguration>>(
            _settings, _logger);
      executor.executeJobs(jobs, vendorConfigurations);

      printElapsedTime();
      return vendorConfigurations;
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

   private void populateFlowHistory(FlowHistory flowHistory,
         LogicBloxFrontend lbFrontend, String environmentName, String tag) {
      String qualifiedName = _predicateInfo.getPredicateNames().get(
            FLOW_HISTORY_PREDICATE_NAME);
      Relation relation = lbFrontend.queryPredicate(qualifiedName);
      List<Flow> flows = new ArrayList<Flow>();
      List<String> historyLines = new ArrayList<String>();
      lbFrontend.fillFlowColumn(flows, relation.getColumns().get(0));
      lbFrontend.fillColumn(LBValueType.STRING, historyLines, relation
            .getColumns().get(1));
      int numEntries = flows.size();
      for (int i = 0; i < numEntries; i++) {
         Flow flow = flows.get(i);
         if (flow.getTag().equals(tag)) {
            String historyLine = historyLines.get(i);
            FlowTrace flowTrace = new FlowTrace(historyLine);
            flowHistory.addFlowTrace(flow, environmentName, flowTrace);
         }
      }
   }

   private void populatePrecomputedBgpAdvertisements(
         String precomputedBgpAdvertisementsPath,
         Map<String, StringBuilder> cpFactBins) {
      File inputFile = new File(precomputedBgpAdvertisementsPath);
      StringBuilder adverts = cpFactBins
            .get(PRECOMPUTED_BGP_ADVERTISEMENTS_PREDICATE_NAME);
      StringBuilder advertCommunities = cpFactBins
            .get(PRECOMPUTED_BGP_ADVERTISEMENT_COMMUNITY_PREDICATE_NAME);
      StringBuilder advertPaths = cpFactBins
            .get(PRECOMPUTED_BGP_ADVERTISEMENT_AS_PATH_PREDICATE_NAME);
      StringBuilder advertPathLengths = cpFactBins
            .get(PRECOMPUTED_BGP_ADVERTISEMENT_AS_PATH_LENGTH_PREDICATE_NAME);
      AdvertisementSet advertSet = (AdvertisementSet) deserializeObject(inputFile);
      StringBuilder wNetworks = cpFactBins.get(NETWORKS_PREDICATE_NAME);
      Set<Prefix> networks = new HashSet<Prefix>();
      int pcIndex = 0;
      for (BgpAdvertisement advert : advertSet) {
         String type = advert.getType();
         switch (type) {
         case "ibgp_ti":
         case "bgp_ti":
            break;

         default:
            continue;
         }

         Prefix network = advert.getNetwork();
         networks.add(network);
         long networkStart = network.getAddress().asLong();
         long networkEnd = network.getEndAddress().asLong();
         int prefixLength = network.getPrefixLength();
         long nextHopIp = advert.getNextHopIp().asLong();
         String srcNode = advert.getSrcNode();
         long srcIp = advert.getSrcIp().asLong();
         String dstNode = advert.getDstNode();
         long dstIp = advert.getDstIp().asLong();
         String srcProtocol = advert.getSrcProtocol().protocolName();
         String originType = advert.getOriginType().toString();
         int localPref = advert.getLocalPreference();
         int med = advert.getMed();
         long originatorIp = advert.getOriginatorIp().asLong();
         adverts.append(pcIndex + "|" + type + "|" + networkStart + "|"
               + networkEnd + "|" + prefixLength + "|" + nextHopIp + "|"
               + srcNode + "|" + srcIp + "|" + dstNode + "|" + dstIp + "|"
               + srcProtocol + "|" + originType + "|" + localPref + "|" + med
               + "|" + originatorIp + "\n");
         for (Long community : advert.getCommunities()) {
            advertCommunities.append(pcIndex + "|" + community + "\n");
         }
         AsPath asPath = advert.getAsPath();
         int asPathLength = asPath.size();
         for (int i = 0; i < asPathLength; i++) {
            AsSet asSet = asPath.get(i);
            for (Integer as : asSet) {
               advertPaths.append(pcIndex + "|" + i + "|" + as + "\n");
            }
         }
         advertPathLengths.append(pcIndex + "|" + asPathLength + "\n");
         pcIndex++;
      }
      for (Prefix network : networks) {
         long networkStart = network.getNetworkAddress().asLong();
         long networkEnd = network.getEndAddress().asLong();
         int prefixLength = network.getPrefixLength();
         wNetworks.append(networkStart + "|" + networkStart + "|" + networkEnd
               + "|" + prefixLength + "\n");
      }
   }

   private void populatePrecomputedFacts(String precomputedFactsPath,
         Map<String, StringBuilder> cpFactBins) {
      File precomputedFactsDir = new File(precomputedFactsPath);
      String[] filenames = precomputedFactsDir.list();
      for (String filename : filenames) {
         File file = Paths.get(precomputedFactsPath, filename).toFile();
         StringBuilder sb = cpFactBins.get(filename);
         if (sb == null) {
            throw new BatfishException("File: \"" + filename
                  + "\" does not correspond to a fact");
         }
         String contents = Util.readFile(file);
         sb.append(contents);
      }
      Set<Map.Entry<String, StringBuilder>> cpEntries = cpFactBins.entrySet();
      Set<Map.Entry<String, StringBuilder>> cpEntriesToRemove = new HashSet<Map.Entry<String, StringBuilder>>();
      for (Entry<String, StringBuilder> e : cpEntries) {
         StringBuilder sb = e.getValue();
         if (sb.toString().length() == 0) {
            cpEntriesToRemove.add(e);
         }
      }
      for (Entry<String, StringBuilder> e : cpEntriesToRemove) {
         cpEntries.remove(e);
      }
   }

   private void populatePrecomputedIbgpNeighbors(
         String precomputedIbgpNeighborsPath,
         Map<String, StringBuilder> cpFactBins) {
      File inputFile = new File(precomputedIbgpNeighborsPath);
      StringBuilder sb = cpFactBins
            .get(PRECOMPUTED_IBGP_NEIGHBORS_PREDICATE_NAME);
      IbgpTopology topology = (IbgpTopology) deserializeObject(inputFile);
      for (IpEdge edge : topology) {
         String node1 = edge.getNode1();
         long ip1 = edge.getIp1().asLong();
         String node2 = edge.getNode2();
         long ip2 = edge.getIp2().asLong();
         sb.append(node1 + "|" + ip1 + "|" + node2 + "|" + ip2 + "\n");
      }
   }

   private void populatePrecomputedRoutes(Set<String> precomputedRoutesPaths,
         Map<String, StringBuilder> cpFactBins) {
      StringBuilder sb = cpFactBins.get(PRECOMPUTED_ROUTES_PREDICATE_NAME);
      StringBuilder wNetworks = cpFactBins.get(NETWORKS_PREDICATE_NAME);
      Set<Prefix> networks = new HashSet<Prefix>();
      for (String precomputedRoutesPath : precomputedRoutesPaths) {
         File inputFile = new File(precomputedRoutesPath);
         RouteSet routes = (RouteSet) deserializeObject(inputFile);
         for (PrecomputedRoute route : routes) {
            String node = route.getNode();
            Prefix prefix = route.getPrefix();
            networks.add(prefix);
            long networkStart = prefix.getNetworkAddress().asLong();
            long networkEnd = prefix.getEndAddress().asLong();
            int prefixLength = prefix.getPrefixLength();
            long nextHopIp = route.getNextHopIp().asLong();
            int admin = route.getAdministrativeCost();
            int cost = route.getCost();
            String protocol = route.getProtocol().protocolName();
            int tag = route.getTag();
            sb.append(node + "|" + networkStart + "|" + networkEnd + "|"
                  + prefixLength + "|" + nextHopIp + "|" + admin + "|" + cost
                  + "|" + protocol + "|" + tag + "\n");
         }
         for (Prefix network : networks) {
            long networkStart = network.getNetworkAddress().asLong();
            long networkEnd = network.getEndAddress().asLong();
            int prefixLength = network.getPrefixLength();
            wNetworks.append(networkStart + "|" + networkStart + "|"
                  + networkEnd + "|" + prefixLength + "\n");
         }
      }
   }

   private void postDifferentialFlows() {
      LogicBloxFrontend baseLbFrontend = _manager.connect(_baseEnvSettings);
      LogicBloxFrontend diffLbFrontend = _manager.connect(_diffEnvSettings);
      Map<String, StringBuilder> baseTrafficFactBins = new LinkedHashMap<String, StringBuilder>();
      Map<String, StringBuilder> diffTrafficFactBins = new LinkedHashMap<String, StringBuilder>();
      Path baseDumpDir = Paths.get(_baseEnvSettings.getTrafficFactDumpDir());
      Path diffDumpDir = Paths.get(_diffEnvSettings.getTrafficFactDumpDir());
      for (String predicate : Facts.TRAFFIC_FACT_COLUMN_HEADERS.keySet()) {
         File factFile = baseDumpDir.resolve(predicate).toFile();
         String contents = Util.readFile(factFile);
         StringBuilder sb = new StringBuilder();
         baseTrafficFactBins.put(predicate, sb);
         sb.append(contents);
      }
      for (String predicate : Facts.TRAFFIC_FACT_COLUMN_HEADERS.keySet()) {
         File factFile = diffDumpDir.resolve(predicate).toFile();
         String contents = Util.readFile(factFile);
         StringBuilder sb = new StringBuilder();
         diffTrafficFactBins.put(predicate, sb);
         sb.append(contents);
      }
      postFacts(baseLbFrontend, baseTrafficFactBins);
      postFacts(diffLbFrontend, baseTrafficFactBins);
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

   private void postFacts(Map<String, StringBuilder> factBins) {
      LogicBloxFrontend lbFrontend = _manager.connect();
      postFacts(lbFrontend, factBins);
   }

   private void postFlows() {
      Map<String, StringBuilder> trafficFactBins = new LinkedHashMap<String, StringBuilder>();
      Path dumpDir = Paths.get(_envSettings.getTrafficFactDumpDir());
      for (String predicate : Facts.TRAFFIC_FACT_COLUMN_HEADERS.keySet()) {
         File factFile = dumpDir.resolve(predicate).toFile();
         String contents = Util.readFile(factFile);
         StringBuilder sb = new StringBuilder();
         trafficFactBins.put(predicate, sb);
         sb.append(contents);
      }
      postFacts(trafficFactBins);
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

   private void printSymmetricEdgePairs() {
      Map<String, Configuration> configs = loadConfigurations();
      EdgeSet edges = synthesizeTopology(configs);
      Set<Edge> symmetricEdgePairs = getSymmetricEdgePairs(edges);
      List<Edge> edgeList = new ArrayList<Edge>();
      edgeList.addAll(symmetricEdgePairs);
      for (int i = 0; i < edgeList.size() / 2; i++) {
         Edge edge1 = edgeList.get(2 * i);
         Edge edge2 = edgeList.get(2 * i + 1);
         _logger.output(edge1.getNode1() + ":" + edge1.getInt1() + ","
               + edge1.getNode2() + ":" + edge1.getInt2() + " "
               + edge2.getNode1() + ":" + edge2.getInt1() + ","
               + edge2.getNode2() + ":" + edge2.getInt2() + "\n");
      }
      printElapsedTime();
   }

   private void printTracePredicate(LogicBloxFrontend lbFrontend,
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
      Relation relation = lbFrontend.queryPredicate(TRACE_PREFIX
            + qualifiedName);
      try {
         output = lbFrontend.getTracePredicate(_predicateInfo, relation,
               predicateName);
         for (String match : output) {
            _logger.output(match + "\n");
         }
      }
      catch (QueryException q) {
         _logger.fatal(q.getMessage() + "\n");
      }
   }

   public void printTracePredicates(LogicBloxFrontend lbFrontend,
         Set<String> predicateNames) {
      // Print predicate(s) here
      _logger.info("\n*** SUBMITTING QUERY(IES) ***\n");
      resetTimer();
      for (String predicateName : predicateNames) {
         printTracePredicate(lbFrontend, predicateName);
      }
      printElapsedTime();
   }

   private void processDeltaConfigurations(
         Map<String, Configuration> configurations,
         EnvironmentSettings envSettings) {
      Map<String, Configuration> deltaConfigurations = getDeltaConfigurations(envSettings);
      configurations.putAll(deltaConfigurations);
      // TODO: deal with topological changes
   }

   private void processInterfaceBlacklist(
         Map<String, Configuration> configurations,
         EnvironmentSettings envSettings) {
      Set<NodeInterfacePair> blacklistInterfaces = getInterfaceBlacklist(envSettings);
      if (blacklistInterfaces != null) {
         for (NodeInterfacePair p : blacklistInterfaces) {
            String hostname = p.getHostname();
            String iface = p.getInterface();
            Configuration node = configurations.get(hostname);
            node.getInterfaces().get(iface).setActive(false);
         }
      }
   }

   private void processNodeBlacklist(Map<String, Configuration> configurations,
         EnvironmentSettings envSettings) {
      NodeSet blacklistNodes = getNodeBlacklist(envSettings);
      if (blacklistNodes != null) {
         for (String hostname : blacklistNodes) {
            configurations.remove(hostname);
         }
      }
   }

   private Topology processTopologyFile(File topologyFilePath,
         Map<String, StringBuilder> factBins) {
      Topology topology = parseTopology(topologyFilePath);
      return topology;
   }

   private void query() {
      LogicBloxFrontend lbFrontend = _manager.connect();
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
   }

   private Map<File, String> readConfigurationFiles(String testRigPath) {
      _logger.info("\n*** READING CONFIGURATION FILES ***\n");
      resetTimer();
      Map<File, String> configurationData = new TreeMap<File, String>();
      File configsPath = Paths.get(testRigPath,
            BfConsts.RELPATH_CONFIGURATIONS_DIR).toFile();
      File[] configFilePaths = configsPath.listFiles(new FilenameFilter() {
         @Override
         public boolean accept(File dir, String name) {
            return !name.startsWith(".");
         }
      });
      if (configFilePaths == null) {
         throw new BatfishException(
               "Error reading test rig configs directory: \""
                     + configsPath.toString() + "\"");
      }
      Arrays.sort(configFilePaths);
      for (File file : configFilePaths) {
         _logger.debug("Reading: \"" + file.toString() + "\"\n");
         String fileTextRaw = Util.readFile(file.getAbsoluteFile());
         String fileText = fileTextRaw
               + ((fileTextRaw.length() != 0) ? "\n" : "");
         configurationData.put(file, fileText);
      }
      printElapsedTime();
      return configurationData;
   }

   private void removeBlocks(List<String> blockNames) {
      LogicBloxFrontend lbFrontend = _manager.connect();
      Set<String> allBlockNames = new LinkedHashSet<String>();
      for (String blockName : blockNames) {
         Block block = Block.BLOCKS.get(blockName);
         if (block == null) {
            throw new BatfishException("Invalid block name: \"" + blockName
                  + "\"");
         }
         Set<Block> dependents = block.getDependents();
         for (Block dependent : dependents) {
            allBlockNames.add(dependent.getName());
         }
         allBlockNames.add(blockName);
      }
      List<String> qualifiedBlockNames = new ArrayList<String>();
      for (String blockName : allBlockNames) {
         String qualifiedBlockName = LB_BATFISH_LIBRARY_NAME + ":" + blockName
               + "_rules";
         qualifiedBlockNames.add(qualifiedBlockName);
      }
      lbFrontend.removeBlocks(qualifiedBlockNames);
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

   private void revert() {
      _logger.info("\n*** REVERTING WORKSPACE ***\n");
      LogicBloxFrontend lbFrontend = _manager.connect();
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
      boolean action = false;

      if (_settings.getQuery() || _settings.getPrintSemantics()
            || _settings.getDataPlane() || _settings.getWriteRoutes()
            || _settings.getWriteBgpAdvertisements()
            || _settings.getWriteIbgpNeighbors()
            || _settings.getDifferentialHistory() || _settings.getHistory()
            || _settings.getTraceQuery()) {
         Map<String, String> logicFiles = getSemanticsFiles();
         _predicateInfo = getPredicateInfo(logicFiles);
         // Print predicate semantics and quit if requested
         if (_settings.getPrintSemantics()) {
            printAllPredicateSemantics(_predicateInfo.getPredicateSemantics());
            return;
         }
      }

      if (_settings.getPrintSymmetricEdgePairs()) {
         printSymmetricEdgePairs();
         return;
      }

      if (_settings.getSynthesizeTopology()) {
         writeSynthesizedTopology();
         return;
      }

      if (_settings.getAnswer()) {
         answer();
         action = true;
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
         String inputRole = _settings.getGenerateStubsInputRole();
         String interfaceDescriptionRegex = _settings
               .getGenerateStubsInterfaceDescriptionRegex();
         int stubAs = _settings.getGenerateStubsRemoteAs();
         generateStubs(inputRole, stubAs, interfaceDescriptionRegex);
         return;
      }

      if (_settings.getAnonymize()) {
         anonymizeConfigurations();
         return;
      }

      if (_settings.getSerializeVendor()) {
         String testRigPath = _settings.getTestRigPath();
         String outputPath = _settings.getSerializeVendorPath();
         serializeVendorConfigs(testRigPath, outputPath);
         return;
      }

      if (_settings.getSerializeIndependent()) {
         String inputPath = _settings.getSerializeVendorPath();
         String outputPath = _settings.getSerializeIndependentPath();
         serializeIndependentConfigs(inputPath, outputPath);
         return;
      }

      Map<String, StringBuilder> cpFactBins = null;
      if (_settings.getFacts() || _settings.getDumpControlPlaneFacts()) {
         boolean usePrecomputedFacts = _settings.getUsePrecomputedFacts();
         cpFactBins = new LinkedHashMap<String, StringBuilder>();
         initControlPlaneFactBins(cpFactBins, !usePrecomputedFacts);
         if (!usePrecomputedFacts) {
            computeControlPlaneFacts(cpFactBins);
         }
         action = true;
      }

      if (_settings.getUsePrecomputedFacts()) {
         populatePrecomputedFacts(_settings.getPrecomputedFactsPath(),
               cpFactBins);
      }

      if (_settings.getWriteRoutes()) {
         writeRoutes(_settings.getPrecomputedRoutesPath());
         action = true;
      }
      if (_settings.getWriteBgpAdvertisements()) {
         writeBgpAdvertisements(_settings.getPrecomputedBgpAdvertisementsPath());
         action = true;
      }
      if (_settings.getWriteIbgpNeighbors()) {
         writeIbgpNeighbors(_settings.getPrecomputedIbgpNeighborsPath());
         action = true;
      }

      if (_settings.revert()) {
         revert();
         return;
      }

      if (_settings.getDeleteWorkspace()) {
         deleteWorkspace();
         return;
      }

      // Create new workspace (will overwrite existing) if requested
      if (_settings.createWorkspace()) {
         createWorkspace();
         action = true;
      }

      // Remove blocks if requested
      if (_settings.getRemoveBlocks() || _settings.getKeepBlocks()) {
         List<String> blockNames = _settings.getBlockNames();
         if (_settings.getRemoveBlocks()) {
            removeBlocks(blockNames);
         }
         if (_settings.getKeepBlocks()) {
            keepBlocks(blockNames);
         }
         action = true;
      }

      // Post facts if requested
      if (_settings.getFacts()) {
         addStaticFacts(BASIC_FACTS_BLOCKNAME);
         postFacts(cpFactBins);
         return;
      }

      if (_settings.getQuery()) {
         query();
         return;
      }

      if (_settings.getTraceQuery()) {
         traceQuery();
         return;
      }

      if (_settings.getDataPlane()) {
         computeDataPlane();
         return;
      }

      if (_settings.getPostFlows()) {
         postFlows();
         action = true;
      }

      if (_settings.getPostDifferentialFlows()) {
         postDifferentialFlows();
         action = true;
      }

      if (_settings.getHistory()) {
         getHistory();
         action = true;
      }

      if (_settings.getDifferentialHistory()) {
         getDifferentialHistory();
         action = true;
      }

      if (!action) {
         throw new CleanBatfishException(
               "No task performed! Run with -help flag to see usage\n");
      }
   }

   private void serializeIndependentConfigs(
         Map<String, Configuration> configurations, String outputPath) {
      if (configurations == null) {
         throw new BatfishException("Exiting due to conversion error(s)");
      }
      if (!_settings.getNoOutput()) {
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
            _logger.info(" ...OK\n");
         }
         printElapsedTime();
      }
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
         throw new BatfishException("Exiting due to parser errors");
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
         Map<String, Configuration> configurations, File dataPlanePath) {
      _logger.info("\n*** GENERATING Z3 LOGIC ***\n");
      resetTimer();

      _logger.info("Deserializing data plane: \"" + dataPlanePath.toString()
            + "\"...");
      DataPlane dataPlane = (DataPlane) deserializeObject(dataPlanePath);
      _logger.info("OK\n");

      _logger.info("Synthesizing Z3 logic...");
      Synthesizer s = new Synthesizer(configurations, dataPlane,
            _settings.getSimplify());

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

   private EdgeSet synthesizeTopology(Map<String, Configuration> configurations) {
      _logger
            .info("\n*** SYNTHESIZING TOPOLOGY FROM INTERFACE SUBNET INFORMATION ***\n");
      resetTimer();
      EdgeSet edges = new EdgeSet();
      Map<Prefix, Set<NodeInterfacePair>> prefixInterfaces = new HashMap<Prefix, Set<NodeInterfacePair>>();
      for (Entry<String, Configuration> e1 : configurations.entrySet()) {
         String nodeName = e1.getKey();
         Configuration node = e1.getValue();
         for (Entry<String, Interface> e2 : node.getInterfaces().entrySet()) {
            Interface iface = e2.getValue();
            String ifaceName = e2.getKey();
            Prefix prefix = e2.getValue().getPrefix();
            if (!iface.isLoopback(node.getVendor()) && iface.getActive()
                  && prefix != null && prefix.getPrefixLength() < 32) {
               Prefix network = new Prefix(prefix.getNetworkAddress(),
                     prefix.getPrefixLength());
               NodeInterfacePair pair = new NodeInterfacePair(nodeName,
                     ifaceName);
               Set<NodeInterfacePair> interfaceBucket = prefixInterfaces
                     .get(network);
               if (interfaceBucket == null) {
                  interfaceBucket = new HashSet<NodeInterfacePair>();
                  prefixInterfaces.put(network, interfaceBucket);
               }
               interfaceBucket.add(pair);
            }
         }
      }
      for (Set<NodeInterfacePair> bucket : prefixInterfaces.values()) {
         for (NodeInterfacePair p1 : bucket) {
            for (NodeInterfacePair p2 : bucket) {
               if (!p1.equals(p2)) {
                  Edge edge = new Edge(p1, p2);
                  edges.add(edge);
               }
            }
         }
      }
      return edges;
   }

   private void traceQuery() {
      LogicBloxFrontend lbFrontend = _manager.connect();
      lbFrontend.initEntityTable();
      lbFrontend.initTraceEntityTable();
      Map<String, String> allPredicateNames = _predicateInfo
            .getPredicateNames();
      Set<String> predicateNames = new TreeSet<String>();
      if (_settings.getQueryAll()) {
         predicateNames.addAll(allPredicateNames.keySet());
      }
      else {
         predicateNames.addAll(_settings.getPredicates());
      }
      printTracePredicates(lbFrontend, predicateNames);
      // Map<Integer, Map<Long, PrecomputedRoute>> routes = lbFrontend
      // .getTraceRoutes();
      // Map<Integer, Map<Long, BgpAdvertisement>> adverts = lbFrontend
      // .getTraceAdvertisements();
      // for (int traceNumber : routes.keySet()) {
      // Map<Long, PrecomputedRoute> currentRoutes = routes
      // .get(traceNumber);
      // for (long index : currentRoutes.keySet()) {
      // PrecomputedRoute route = currentRoutes.get(index);
      // _logger.output("Trace: " + traceNumber + ", " + route.toString()
      // + "\n");
      // }
      // }
      // for (int traceNumber : adverts.keySet()) {
      // Map<Long, BgpAdvertisement> currentAdverts = adverts
      // .get(traceNumber);
      // for (long index : currentAdverts.keySet()) {
      // BgpAdvertisement advert = currentAdverts.get(index);
      // _logger.output("Trace: " + traceNumber + ", "
      // + advert.toString() + "\n");
      // }
      // }
   }

   private void writeBgpAdvertisements(String writeAdvertsPath) {
      LogicBloxFrontend lbFrontend = _manager.connect();
      lbFrontend.initEntityTable();
      File advertsFile = new File(writeAdvertsPath);
      File parentDir = advertsFile.getParentFile();
      if (parentDir != null) {
         parentDir.mkdirs();
      }
      AdvertisementSet adverts = getAdvertisements(lbFrontend);
      _logger.info("Serializing: BGP advertisements => \"" + writeAdvertsPath
            + "\"...");
      serializeObject(adverts, advertsFile);
      _logger.info("OK\n");
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

   private void writeFlowSinkFacts(InterfaceSet flowSinks,
         Map<String, StringBuilder> cpFactBins) {
      StringBuilder sb = cpFactBins.get("SetFlowSinkInterface");
      for (NodeInterfacePair f : flowSinks) {
         String node = f.getHostname();
         String iface = f.getInterface();
         sb.append(node + "|" + iface + "\n");
      }
   }

   private void writeIbgpNeighbors(String ibgpTopologyPath) {
      LogicBloxFrontend lbFrontend = _manager.connect();
      lbFrontend.initEntityTable();
      File ibgpTopologyFile = new File(ibgpTopologyPath);
      File parentDir = ibgpTopologyFile.getParentFile();
      if (parentDir != null) {
         parentDir.mkdirs();
      }
      String qualifiedName = _predicateInfo.getPredicateNames().get(
            IBGP_NEIGHBORS_PREDICATE_NAME);
      IbgpTopology topology = lbFrontend.getIbgpNeighbors(qualifiedName);
      _logger.info("Serializing: IBGP neighbors => \"" + ibgpTopologyPath
            + "\"...");
      serializeObject(topology, ibgpTopologyFile);
      _logger.info("OK\n");
   }

   private void writeRoutes(String writeRoutesPath) {
      LogicBloxFrontend lbFrontend = _manager.connect();
      lbFrontend.initEntityTable();
      File routesFile = new File(writeRoutesPath);
      File parentDir = routesFile.getParentFile();
      if (parentDir != null) {
         parentDir.mkdirs();
      }
      RouteSet routes = getRoutes(lbFrontend);
      _logger.info("Serializing: routes => \"" + writeRoutesPath + "\"...");
      serializeObject(routes, routesFile);
      _logger.info("OK\n");
   }

   private void writeSynthesizedTopology() {
      Map<String, Configuration> configs = loadConfigurations();
      EdgeSet edges = synthesizeTopology(configs);
      _logger.output(BatfishTopologyCombinedParser.HEADER + "\n");
      for (Edge edge : edges) {
         _logger.output(edge.getNode1() + ":" + edge.getInt1() + ","
               + edge.getNode2() + ":" + edge.getInt2() + "\n");
      }
      printElapsedTime();
   }

   private void writeTopologyFacts(Topology topology,
         Map<String, StringBuilder> factBins) {
      TopologyFactExtractor tfe = new TopologyFactExtractor(topology);
      tfe.writeFacts(factBins);
   }

}
