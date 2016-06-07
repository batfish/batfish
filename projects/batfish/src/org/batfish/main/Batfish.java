package org.batfish.main;

import java.io.ByteArrayOutputStream;
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
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.batfish.common.BatfishLogger;
import org.batfish.common.BfConsts;
import org.batfish.common.BatfishException;
import org.batfish.common.CleanBatfishException;
import org.batfish.common.Warning;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.common.util.StringFilter;
import org.batfish.common.util.CommonUtil;
import org.batfish.common.util.UrlZipExplorer;
import org.batfish.datamodel.AsPath;
import org.batfish.datamodel.AsSet;
import org.batfish.datamodel.BgpAdvertisement;
import org.batfish.datamodel.BgpNeighbor;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowHistory;
import org.batfish.datamodel.FlowTrace;
import org.batfish.datamodel.GenericConfigObject;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpsecVpn;
import org.batfish.datamodel.LBValueType;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.OspfArea;
import org.batfish.datamodel.OspfProcess;
import org.batfish.datamodel.PolicyMap;
import org.batfish.datamodel.PolicyMapAction;
import org.batfish.datamodel.PolicyMapClause;
import org.batfish.datamodel.PolicyMapMatchRouteFilterListLine;
import org.batfish.datamodel.PrecomputedRoute;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixSpace;
import org.batfish.datamodel.RouteFilterLine;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.BgpAdvertisement.BgpAdvertisementType;
import org.batfish.datamodel.answers.Answer;
import org.batfish.datamodel.answers.AnswerStatus;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.datamodel.answers.FlattenVendorConfigurationAnswerElement;
import org.batfish.datamodel.answers.NodAnswerElement;
import org.batfish.datamodel.answers.NodSatAnswerElement;
import org.batfish.datamodel.answers.ParseVendorConfigurationAnswerElement;
import org.batfish.datamodel.collections.AdvertisementSet;
import org.batfish.datamodel.collections.CommunitySet;
import org.batfish.datamodel.collections.EdgeSet;
import org.batfish.datamodel.collections.FibMap;
import org.batfish.datamodel.collections.FibRow;
import org.batfish.datamodel.collections.FibSet;
import org.batfish.datamodel.collections.FunctionSet;
import org.batfish.datamodel.collections.IbgpTopology;
import org.batfish.datamodel.collections.InterfaceSet;
import org.batfish.datamodel.collections.IpEdge;
import org.batfish.datamodel.collections.LBValueTypeList;
import org.batfish.datamodel.collections.MultiSet;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.collections.NodeIpPair;
import org.batfish.datamodel.collections.NodeRoleMap;
import org.batfish.datamodel.collections.NodeSet;
import org.batfish.datamodel.collections.PolicyRouteFibIpMap;
import org.batfish.datamodel.collections.PolicyRouteFibNodeMap;
import org.batfish.datamodel.collections.PredicateSemantics;
import org.batfish.datamodel.collections.PredicateValueTypeMap;
import org.batfish.datamodel.collections.QualifiedNameMap;
import org.batfish.datamodel.collections.RoleSet;
import org.batfish.datamodel.collections.RouteSet;
import org.batfish.datamodel.collections.TreeMultiSet;
import org.batfish.datamodel.questions.AclReachabilityQuestion;
import org.batfish.datamodel.questions.BgpAdvertisementsQuestion;
import org.batfish.datamodel.questions.BgpSessionCheckQuestion;
import org.batfish.datamodel.questions.CompareSameNameQuestion;
import org.batfish.datamodel.questions.ErrorQuestion;
import org.batfish.datamodel.questions.IpsecVpnCheckQuestion;
import org.batfish.datamodel.questions.IsisLoopbacksQuestion;
import org.batfish.datamodel.questions.LocalPathQuestion;
import org.batfish.datamodel.questions.MultipathQuestion;
import org.batfish.datamodel.questions.NeighborsQuestion;
import org.batfish.datamodel.questions.NodesQuestion;
import org.batfish.datamodel.questions.OspfLoopbacksQuestion;
import org.batfish.datamodel.questions.PairwiseVpnConnectivityQuestion;
import org.batfish.datamodel.questions.ProtocolDependenciesQuestion;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.questions.ReachabilityQuestion;
import org.batfish.datamodel.questions.ReducedReachabilityQuestion;
import org.batfish.datamodel.questions.RoutesQuestion;
import org.batfish.datamodel.questions.SelfAdjacenciesQuestion;
import org.batfish.datamodel.questions.TracerouteQuestion;
import org.batfish.datamodel.questions.UndefinedReferencesQuestion;
import org.batfish.datamodel.questions.UniqueBgpPrefixOriginationQuestion;
import org.batfish.datamodel.questions.UniqueIpAssignmentsQuestion;
import org.batfish.datamodel.questions.UnusedStructuresQuestion;
import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.ParseTreePrettyPrinter;
import org.batfish.grammar.juniper.JuniperCombinedParser;
import org.batfish.grammar.juniper.JuniperFlattener;
import org.batfish.grammar.logicblox.LogQLPredicateInfoExtractor;
import org.batfish.grammar.logicblox.LogiQLCombinedParser;
import org.batfish.grammar.logicblox.LogiQLPredicateInfoResolver;
import org.batfish.grammar.topology.BatfishTopologyCombinedParser;
import org.batfish.grammar.topology.BatfishTopologyExtractor;
import org.batfish.grammar.topology.GNS3TopologyCombinedParser;
import org.batfish.grammar.topology.GNS3TopologyExtractor;
import org.batfish.grammar.topology.RoleCombinedParser;
import org.batfish.grammar.topology.RoleExtractor;
import org.batfish.grammar.topology.TopologyExtractor;
import org.batfish.grammar.vyos.VyosCombinedParser;
import org.batfish.grammar.vyos.VyosFlattener;
import org.batfish.job.BatfishJobExecutor;
import org.batfish.job.ConvertConfigurationJob;
import org.batfish.job.ConvertConfigurationResult;
import org.batfish.job.FlattenVendorConfigurationJob;
import org.batfish.job.FlattenVendorConfigurationResult;
import org.batfish.job.ParseVendorConfigurationJob;
import org.batfish.job.ParseVendorConfigurationResult;
import org.batfish.logic.LogicResourceLocator;
import org.batfish.main.Settings.EnvironmentSettings;
import org.batfish.nls.Block;
import org.batfish.nls.Column;
import org.batfish.nls.ConfigurationFactExtractor;
import org.batfish.nls.EntityTable;
import org.batfish.nls.Facts;
import org.batfish.nls.NlsConstants;
import org.batfish.nls.PredicateInfo;
import org.batfish.nls.Relation;
import org.batfish.nls.TopologyFactExtractor;
import org.batfish.protocoldependency.DependencyDatabase;
import org.batfish.protocoldependency.DependentRoute;
import org.batfish.protocoldependency.PotentialExport;
import org.batfish.protocoldependency.ProtocolDependencyAnalysis;
import org.batfish.question.AclReachabilityAnswer;
import org.batfish.question.BgpAdvertisementsAnswer;
import org.batfish.question.BgpSessionCheckAnswer;
import org.batfish.question.CompareSameNameAnswer;
import org.batfish.question.ErrorAnswer;
import org.batfish.question.IpsecVpnCheckAnswer;
import org.batfish.question.IsisLoopbacksAnswer;
import org.batfish.question.LocalPathAnswer;
import org.batfish.question.MultipathAnswer;
import org.batfish.question.NeighborsAnswer;
import org.batfish.question.NodesAnswer;
import org.batfish.question.OspfLoopbacksAnswer;
import org.batfish.question.PairwiseVpnConnectivityAnswer;
import org.batfish.question.ProtocolDependenciesAnswer;
import org.batfish.question.ReachabilityAnswer;
import org.batfish.question.ReducedReachabilityAnswer;
import org.batfish.question.RoutesAnswer;
import org.batfish.question.SelfAdjacenciesAnswer;
import org.batfish.question.TracerouteAnswer;
import org.batfish.question.UndefinedReferencesAnswer;
import org.batfish.question.UniqueBgpPrefixOriginationAnswer;
import org.batfish.question.UniqueIpAssignmentsAnswer;
import org.batfish.question.UnusedStructuresAnswer;
import org.batfish.representation.VendorConfiguration;
import org.batfish.representation.aws_vpcs.AwsVpcConfiguration;
import org.batfish.z3.CompositeNodJob;
import org.batfish.z3.NodJob;
import org.batfish.z3.NodJobResult;
import org.batfish.z3.NodSatJob;
import org.batfish.z3.NodSatResult;
import org.batfish.z3.Synthesizer;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * This class encapsulates the main control logic for Batfish.
 */
public class Batfish implements AutoCloseable {

   // private static final String BGP_ADVERTISEMENT_ROUTE_PREDICATE_NAME =
   // "BgpAdvertisementRoute";

   private static final String BGP_ADVERTISEMENT_PREDICATE_NAME = "BgpAdvertisement";

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
    * A byte-array containing the first 4 bytes of the header for a file that is
    * the output of java serialization
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

   private static final String NLS_COMMAND = "nls";

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

   public static void applyAutoBaseDir(final Settings settings) {
      String baseDir = settings.getAutoBaseDir();
      if (baseDir != null) {
         EnvironmentSettings envSettings = settings
               .getBaseEnvironmentSettings();
         EnvironmentSettings diffEnvSettings = settings
               .getDiffEnvironmentSettings();
         settings.setSerializeIndependentPath(Paths.get(baseDir,
               BfConsts.RELPATH_VENDOR_INDEPENDENT_CONFIG_DIR).toString());
         settings.setSerializeVendorPath(Paths.get(baseDir,
               BfConsts.RELPATH_VENDOR_SPECIFIC_CONFIG_DIR).toString());
         settings.setTestRigPath(Paths.get(baseDir,
               BfConsts.RELPATH_TEST_RIG_DIR).toString());
         settings.setProtocolDependencyGraphPath(Paths.get(baseDir,
               BfConsts.RELPATH_PROTOCOL_DEPENDENCY_GRAPH).toString());
         settings.setProtocolDependencyGraphZipPath(Paths.get(baseDir,
               BfConsts.RELPATH_PROTOCOL_DEPENDENCY_GRAPH_ZIP).toString());
         settings.setParseAnswerPath(Paths.get(baseDir,
               BfConsts.RELPATH_PARSE_ANSWER_PATH).toString());
         settings.setConvertAnswerPath(Paths.get(baseDir,
               BfConsts.RELPATH_CONVERT_ANSWER_PATH).toString());
         String envName = settings.getEnvironmentName();
         if (envName != null) {
            envSettings.setName(envName);
            Path envPath = Paths.get(baseDir,
                  BfConsts.RELPATH_ENVIRONMENTS_DIR, envName);
            envSettings.setControlPlaneFactsDir(envPath.resolve(
                  BfConsts.RELPATH_CONTROL_PLANE_FACTS_DIR).toString());
            envSettings.setNlsDataPlaneInputFile(envPath.resolve(
                  BfConsts.RELPATH_NLS_INPUT_FILE).toString());
            envSettings.setNlsDataPlaneOutputDir(envPath.resolve(
                  BfConsts.RELPATH_NLS_OUTPUT_DIR).toString());
            envSettings.setDataPlanePath(envPath.resolve(
                  BfConsts.RELPATH_DATA_PLANE_DIR).toString());
            settings.setZ3DataPlaneFile(envPath.resolve(
                  BfConsts.RELPATH_Z3_DATA_PLANE_FILE).toString());
            Path envDirPath = envPath.resolve(BfConsts.RELPATH_ENV_DIR);
            envSettings.setEnvPath(envDirPath.toString());
            envSettings.setNodeBlacklistPath(envDirPath.resolve(
                  BfConsts.RELPATH_NODE_BLACKLIST_FILE).toString());
            envSettings.setInterfaceBlacklistPath(envDirPath.resolve(
                  BfConsts.RELPATH_INTERFACE_BLACKLIST_FILE).toString());
            envSettings.setEdgeBlacklistPath(envDirPath.resolve(
                  BfConsts.RELPATH_EDGE_BLACKLIST_FILE).toString());
            envSettings.setSerializedTopologyPath(envDirPath.resolve(
                  BfConsts.RELPATH_TOPOLOGY_FILE).toString());
            envSettings.setDeltaConfigurationsDir(envDirPath.resolve(
                  BfConsts.RELPATH_CONFIGURATIONS_DIR).toString());
            envSettings.setExternalBgpAnnouncementsPath(envDirPath.resolve(
                  BfConsts.RELPATH_EXTERNAL_BGP_ANNOUNCEMENTS).toString());
            envSettings.setPrecomputedRoutesPath(envPath.resolve(
                  BfConsts.RELPATH_PRECOMPUTED_ROUTES).toString());
         }
         String diffEnvName = settings.getDiffEnvironmentName();
         if (diffEnvName != null) {
            diffEnvSettings.setName(diffEnvName);
            Path diffEnvPath = Paths.get(baseDir,
                  BfConsts.RELPATH_ENVIRONMENTS_DIR, diffEnvName);
            diffEnvSettings.setControlPlaneFactsDir(diffEnvPath.resolve(
                  BfConsts.RELPATH_CONTROL_PLANE_FACTS_DIR).toString());
            diffEnvSettings.setNlsDataPlaneInputFile(diffEnvPath.resolve(
                  BfConsts.RELPATH_NLS_INPUT_FILE).toString());
            diffEnvSettings.setNlsDataPlaneOutputDir(diffEnvPath.resolve(
                  BfConsts.RELPATH_NLS_OUTPUT_DIR).toString());
            diffEnvSettings.setDataPlanePath(diffEnvPath.resolve(
                  BfConsts.RELPATH_DATA_PLANE_DIR).toString());
            Path diffEnvDirPath = diffEnvPath.resolve(BfConsts.RELPATH_ENV_DIR);
            diffEnvSettings.setEnvPath(diffEnvDirPath.toString());
            diffEnvSettings.setNodeBlacklistPath(diffEnvDirPath.resolve(
                  BfConsts.RELPATH_NODE_BLACKLIST_FILE).toString());
            diffEnvSettings.setInterfaceBlacklistPath(diffEnvDirPath.resolve(
                  BfConsts.RELPATH_INTERFACE_BLACKLIST_FILE).toString());
            diffEnvSettings.setEdgeBlacklistPath(diffEnvDirPath.resolve(
                  BfConsts.RELPATH_EDGE_BLACKLIST_FILE).toString());
            diffEnvSettings.setSerializedTopologyPath(diffEnvDirPath.resolve(
                  BfConsts.RELPATH_TOPOLOGY_FILE).toString());
            diffEnvSettings.setDeltaConfigurationsDir(diffEnvDirPath.resolve(
                  BfConsts.RELPATH_CONFIGURATIONS_DIR).toString());
            diffEnvSettings.setExternalBgpAnnouncementsPath(diffEnvDirPath
                  .resolve(BfConsts.RELPATH_EXTERNAL_BGP_ANNOUNCEMENTS)
                  .toString());
            diffEnvSettings.setPrecomputedRoutesPath(diffEnvPath.resolve(
                  BfConsts.RELPATH_PRECOMPUTED_ROUTES).toString());
            if (settings.getDiffActive()) {
               settings.setActiveEnvironmentSettings(diffEnvSettings);
            }
         }
         String outputEnvName = settings.getOutputEnvironmentName();
         if (outputEnvName != null) {
            Path outputEnvPath = Paths.get(baseDir,
                  BfConsts.RELPATH_ENVIRONMENTS_DIR, outputEnvName);
            envSettings.setPrecomputedRoutesPath(outputEnvPath.resolve(
                  BfConsts.RELPATH_PRECOMPUTED_ROUTES).toString());
         }
         String questionName = settings.getQuestionName();
         if (questionName != null) {
            Path questionPath = Paths.get(baseDir,
                  BfConsts.RELPATH_QUESTIONS_DIR, questionName);
            settings.setQuestionPath(questionPath.resolve(
                  BfConsts.RELPATH_QUESTION_FILE).toString());
            settings.setQuestionParametersPath(questionPath.resolve(
                  BfConsts.RELPATH_QUESTION_PARAM_FILE).toString());
            if (diffEnvName != null) {
               diffEnvSettings.setTrafficFactDumpDir(questionPath
                     .resolve(
                           Paths.get(BfConsts.RELPATH_DIFF, envName,
                                 diffEnvName,
                                 BfConsts.RELPATH_CONTROL_PLANE_FACTS_DIR)
                                 .toString()).toString());
               diffEnvSettings.setNlsTrafficInputFile(questionPath.resolve(
                     Paths.get(BfConsts.RELPATH_DIFF, envName, diffEnvName,
                           BfConsts.RELPATH_NLS_INPUT_FILE).toString())
                     .toString());
               diffEnvSettings.setNlsTrafficOutputDir(questionPath.resolve(
                     Paths.get(BfConsts.RELPATH_DIFF, envName, diffEnvName,
                           BfConsts.RELPATH_NLS_OUTPUT_DIR).toString())
                     .toString());
               envSettings.setTrafficFactDumpDir(questionPath
                     .resolve(
                           Paths.get(BfConsts.RELPATH_BASE, envName,
                                 diffEnvName,
                                 BfConsts.RELPATH_CONTROL_PLANE_FACTS_DIR)
                                 .toString()).toString());
               envSettings.setNlsTrafficInputFile(questionPath.resolve(
                     Paths.get(BfConsts.RELPATH_BASE, envName, diffEnvName,
                           BfConsts.RELPATH_NLS_INPUT_FILE).toString())
                     .toString());
               envSettings.setNlsTrafficOutputDir(questionPath.resolve(
                     Paths.get(BfConsts.RELPATH_BASE, envName, diffEnvName,
                           BfConsts.RELPATH_NLS_OUTPUT_DIR).toString())
                     .toString());
            }
            else {
               envSettings.setTrafficFactDumpDir(questionPath
                     .resolve(
                           Paths.get(BfConsts.RELPATH_BASE, envName,
                                 BfConsts.RELPATH_CONTROL_PLANE_FACTS_DIR)
                                 .toString()).toString());
               envSettings.setNlsTrafficInputFile(questionPath.resolve(
                     Paths.get(BfConsts.RELPATH_BASE, envName,
                           BfConsts.RELPATH_NLS_INPUT_FILE).toString())
                     .toString());
               envSettings.setNlsTrafficOutputDir(questionPath.resolve(
                     Paths.get(BfConsts.RELPATH_BASE, envName,
                           BfConsts.RELPATH_NLS_OUTPUT_DIR).toString())
                     .toString());
            }
         }
      }
   }

   public static String flatten(String input, BatfishLogger logger,
         Settings settings, ConfigurationFormat format, String header) {
      switch (format) {
      case JUNIPER: {
         JuniperCombinedParser parser = new JuniperCombinedParser(input,
               settings);
         ParserRuleContext tree = parse(parser, logger, settings);
         JuniperFlattener flattener = new JuniperFlattener(header);
         ParseTreeWalker walker = new ParseTreeWalker();
         walker.walk(flattener, tree);
         return flattener.getFlattenedConfigurationText();
      }

      case VYOS: {
         VyosCombinedParser parser = new VyosCombinedParser(input, settings);
         ParserRuleContext tree = parse(parser, logger, settings);
         VyosFlattener flattener = new VyosFlattener(header);
         ParseTreeWalker walker = new ParseTreeWalker();
         walker.walk(flattener, tree);
         return flattener.getFlattenedConfigurationText();
      }

      // $CASES-OMITTED$
      default:
         throw new BatfishException("Invalid format for flattening");
      }
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

   public static void initTrafficFactBins(Map<String, StringBuilder> factBins) {
      initFactBins(Facts.TRAFFIC_FACT_COLUMN_HEADERS, factBins, true);
   }

   public static void logWarnings(BatfishLogger logger, Warnings warnings) {
      for (Warning warning : warnings.getRedFlagWarnings()) {
         logger.redflag(logWarningsHelper(warning));
      }
      for (Warning warning : warnings.getUnimplementedWarnings()) {
         logger.unimplemented(logWarningsHelper(warning));
      }
      for (Warning warning : warnings.getPedanticWarnings()) {
         logger.pedantic(logWarningsHelper(warning));
      }
   }

   private static String logWarningsHelper(Warning warning) {
      return "   " + warning.getTag() + ": " + warning.getText() + "\n";
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
            String prefixedMsg = CommonUtil.applyPrefix(prefix, msg);
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

   private final Map<EnvironmentSettings, EntityTable> _entityTables;

   private EnvironmentSettings _envSettings;

   private BatfishLogger _logger;

   private PredicateInfo _predicateInfo;

   private Settings _settings;

   // this variable is used communicate with parent thread on how the job
   // finished
   private boolean _terminatedWithException;

   private long _timerCount;

   private File _tmpLogicDir;

   public Batfish(Settings settings) {
      _settings = settings;
      _envSettings = settings.getActiveEnvironmentSettings();
      _baseEnvSettings = settings.getBaseEnvironmentSettings();
      _diffEnvSettings = settings.getDiffEnvironmentSettings();
      _logger = _settings.getLogger();
      _tmpLogicDir = null;
      _entityTables = new HashMap<EnvironmentSettings, EntityTable>();
      _terminatedWithException = false;
   }

   private void anonymizeConfigurations() {
      // TODO Auto-generated method stub

   }

   private Answer answer() {
      Question question = parseQuestion();
      boolean dp = question.getDataPlane();
      boolean diff = question.getDifferential();
      boolean diffActive = (question.getDiffActive() || _settings
            .getDiffActive()) && !diff;
      _settings.setDiffActive(diffActive);
      _settings.setDiffQuestion(diff);
      initQuestionEnvironments(question, diff, diffActive, dp);
      Answer answer = null;
      BatfishException exception = null;
      try {
         switch (question.getType()) {
         case ACL_REACHABILITY:
            answer = new AclReachabilityAnswer(this,
                  (AclReachabilityQuestion) question);
            break;

         case BGP_ADVERTISEMENTS:
            answer = new BgpAdvertisementsAnswer(this,
                  (BgpAdvertisementsQuestion) question);
            break;

         case BGP_SESSION_CHECK:
            answer = new BgpSessionCheckAnswer(this,
                  (BgpSessionCheckQuestion) question);
            break;

         case COMPARE_SAME_NAME:
            answer = new CompareSameNameAnswer(this,
                  (CompareSameNameQuestion) question);
            break;

         case ERROR:
            answer = new ErrorAnswer(this, (ErrorQuestion) question);
            break;

         case IPSEC_VPN_CHECK:
            answer = new IpsecVpnCheckAnswer(this,
                  (IpsecVpnCheckQuestion) question);
            break;

         case ISIS_LOOPBACKS:
            answer = new IsisLoopbacksAnswer(this,
                  (IsisLoopbacksQuestion) question);
            break;

         case LOCAL_PATH:
            answer = new LocalPathAnswer(this, (LocalPathQuestion) question);
            break;

         case MULTIPATH:
            answer = new MultipathAnswer(this, (MultipathQuestion) question);
            break;

         case NEIGHBORS:
            answer = new NeighborsAnswer(this, (NeighborsQuestion) question);
            break;

         case NODES:
            answer = new NodesAnswer(this, (NodesQuestion) question);
            break;

         case OSPF_LOOPBACKS:
            answer = new OspfLoopbacksAnswer(this,
                  (OspfLoopbacksQuestion) question);
            break;

         case PAIRWISE_VPN_CONNECTIVITY:
            answer = new PairwiseVpnConnectivityAnswer(this,
                  (PairwiseVpnConnectivityQuestion) question);
            break;

         case PROTOCOL_DEPENDENCIES:
            answer = new ProtocolDependenciesAnswer(this,
                  (ProtocolDependenciesQuestion) question);
            break;

         case REACHABILITY:
            answer = new ReachabilityAnswer(this,
                  (ReachabilityQuestion) question);
            break;

         case REDUCED_REACHABILITY:
            answer = new ReducedReachabilityAnswer(this,
                  (ReducedReachabilityQuestion) question);
            break;

         case ROUTES:
            answer = new RoutesAnswer(this, (RoutesQuestion) question);
            break;

         case SELF_ADJACENCIES:
            answer = new SelfAdjacenciesAnswer(this,
                  (SelfAdjacenciesQuestion) question);
            break;

         case TRACEROUTE:
            answer = new TracerouteAnswer(this, (TracerouteQuestion) question);
            break;

         case UNDEFINED_REFERENCES:
            answer = new UndefinedReferencesAnswer(this,
                  (UndefinedReferencesQuestion) question);
            break;

         case UNIQUE_BGP_PREFIX_ORIGINATION:
            answer = new UniqueBgpPrefixOriginationAnswer(this,
                  (UniqueBgpPrefixOriginationQuestion) question);
            break;

         case UNIQUE_IP_ASSIGNMENTS:
            answer = new UniqueIpAssignmentsAnswer(this,
                  (UniqueIpAssignmentsQuestion) question);
            break;

         case UNUSED_STRUCTURES:
            answer = new UnusedStructuresAnswer(this,
                  (UnusedStructuresQuestion) question);
            break;

         default:
            throw new BatfishException("Unknown question type");
         }
      }
      catch (Exception e) {
         exception = new BatfishException("Failed to answer question", e);
      }
      if (exception == null) {
         // success
         answer.setStatus(AnswerStatus.SUCCESS);
      }
      else {
         // failure
         answer = new Answer();
         answer.setStatus(AnswerStatus.FAILURE);
         answer.addAnswerElement(exception);
      }
      answer.setQuestion(question);
      return answer;
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
         String input = CommonUtil.readFile(logicFilePath.toFile());
         LogiQLCombinedParser parser = new LogiQLCombinedParser(input,
               _settings);
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

   private void checkBaseDirExists() {
      File baseDir = new File(_settings.getAutoBaseDir());
      if (!baseDir.exists()) {
         throw new CleanBatfishException("Test rig does not exist: \""
               + baseDir.getName() + "\"");
      }
   }

   private void checkComputeControlPlaneFacts() {
      checkConfigurations();
      checkEnvironmentExists(_baseEnvSettings);
      if (_settings.getDiffActive()) {
         checkDataPlane(_baseEnvSettings);
         checkDiffEnvironmentExists();
      }
   }

   private void checkComputeNlsRelations(EnvironmentSettings envSettings) {
      checkControlPlaneFacts(envSettings);
   }

   public void checkConfigurations() {
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

   private void checkControlPlaneFacts(EnvironmentSettings envSettings) {
      File cpFactsDir = new File(envSettings.getControlPlaneFactsDir());
      if (!cpFactsDir.exists()) {
         throw new CleanBatfishException(
               "Missing control plane facts for environment: \""
                     + envSettings.getName() + "\"\n");
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

   private void checkDataPlaneFacts(EnvironmentSettings envSettings) {
      checkEnvironmentExists(envSettings);
      File dataPlaneFactDir = new File(envSettings.getNlsDataPlaneOutputDir());
      if (!dataPlaneFactDir.exists()) {
         throw new CleanBatfishException(
               "Missing computed data plane facts for environment: "
                     + envSettings.getName() + "\n");
      }
   }

   public void checkDataPlaneQuestionDependencies() {
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

   public void checkDifferentialDataPlaneQuestionDependencies() {
      checkDiffEnvironmentSpecified();
      checkConfigurations();
      checkDataPlane(_baseEnvSettings);
      checkDataPlane(_diffEnvSettings);
   }

   public void checkEnvironmentExists(EnvironmentSettings envSettings) {
      checkBaseDirExists();
      if (!new File(envSettings.getDataPlanePath()).getParentFile().exists()) {
         throw new CleanBatfishException("Environment not initialized: \""
               + envSettings.getName() + "\"");
      }
   }

   private void checkQuery(EnvironmentSettings envSettings,
         Set<String> predicateNames) {
      Set<String> dpIntersect = new HashSet<String>();
      dpIntersect.addAll(predicateNames);
      dpIntersect.retainAll(getNlsDataPlaneOutputSymbols());
      if (dpIntersect.size() > 0) {
         checkDataPlaneFacts(envSettings);
      }
      Set<String> trafficIntersect = new HashSet<String>();
      trafficIntersect.addAll(predicateNames);
      trafficIntersect.retainAll(getNlsTrafficOutputSymbols());
      if (trafficIntersect.size() > 0) {
         checkTrafficFacts(envSettings);
      }
   }

   private void checkTrafficFacts(EnvironmentSettings envSettings) {
      checkEnvironmentExists(envSettings);
      File trafficFactDir = new File(envSettings.getNlsTrafficOutputDir());
      if (!trafficFactDir.exists()) {
         throw new CleanBatfishException(
               "Missing computed traffic facts for environment: "
                     + envSettings.getName() + "\n");
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
   }

   public Set<Flow> computeCompositeNodOutput(List<CompositeNodJob> jobs,
         NodAnswerElement answerElement) {
      _logger.info("\n*** EXECUTING COMPOSITE NOD JOBS ***\n");
      resetTimer();
      Set<Flow> flows = new TreeSet<Flow>();
      BatfishJobExecutor<CompositeNodJob, NodAnswerElement, NodJobResult, Set<Flow>> executor = new BatfishJobExecutor<CompositeNodJob, NodAnswerElement, NodJobResult, Set<Flow>>(
            _settings, _logger);
      executor.executeJobs(jobs, flows, answerElement);
      printElapsedTime();
      return flows;
   }

   private void computeControlPlaneFacts(Map<String, StringBuilder> cpFactBins,
         boolean differentialContext, EnvironmentSettings envSettings) {
      checkComputeControlPlaneFacts();
      if (_settings.getUsePrecomputedRoutes()) {
         List<String> precomputedRoutesPaths = _settings
               .getPrecomputedRoutesPaths();
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
      Map<String, Configuration> configurations = loadConfigurations(envSettings);
      CommunitySet allCommunities = new CommunitySet();
      processExternalBgpAnnouncements(configurations, envSettings, cpFactBins,
            allCommunities);
      Topology topology = computeTopology(configurations, envSettings);
      InterfaceSet flowSinks = computeFlowSinks(configurations, envSettings,
            differentialContext, topology);
      writeTopologyFacts(topology, cpFactBins);
      populateConfigurationFactBins(configurations.values(), allCommunities,
            cpFactBins);
      writeFlowSinkFacts(flowSinks, cpFactBins);
      if (!_logger.isActive(BatfishLogger.LEVEL_INFO)) {
         _logger.output("Facts generated successfully.\n");
      }
      if (_settings.getDumpControlPlaneFacts()) {
         dumpControlPlaneFacts(envSettings, cpFactBins);
      }
      // serialize topology
      String serializedTopologyPath = envSettings.getSerializedTopologyPath();
      File topologyPath = new File(serializedTopologyPath);
      _logger.info("Serializing topology...");
      serializeObject(topology, topologyPath);
      _logger.info("OK\n");
   }

   private void computeDataPlane() {
      computeDataPlane(_envSettings);
   }

   private void computeDataPlane(EnvironmentSettings envSettings) {
      checkDataPlaneFacts(envSettings);
      String dataPlanePath = envSettings.getDataPlanePath();
      if (dataPlanePath == null) {
         throw new BatfishException("Missing path to data plane");
      }
      File dataPlanePathAsFile = new File(dataPlanePath);
      computeDataPlane(dataPlanePathAsFile, envSettings);
   }

   private void computeDataPlane(File dataPlanePath,
         EnvironmentSettings envSettings) {
      _logger.info("\n*** COMPUTING DATA PLANE STRUCTURES ***\n");
      resetTimer();

      _logger.info("Retrieving flow sink information...");
      InterfaceSet flowSinks = getFlowSinkSet(envSettings);
      _logger.info("OK\n");

      Topology topology = loadTopology(envSettings);
      EdgeSet topologyEdges = topology.getEdges();

      _logger.info("Caclulating forwarding rules...");
      FibMap fibs = getRouteForwardingRules(envSettings);
      PolicyRouteFibNodeMap policyRouteFibNodeMap = getPolicyRouteFibNodeMap(envSettings);
      _logger.info("OK\n");
      DataPlane dataPlane = new DataPlane(flowSinks, topologyEdges, fibs,
            policyRouteFibNodeMap);
      _logger.info("Serializing data plane...");
      serializeObject(dataPlane, dataPlanePath);
      _logger.info("OK\n");

      printElapsedTime();
   }

   private InterfaceSet computeFlowSinks(
         Map<String, Configuration> configurations,
         EnvironmentSettings envSettings, boolean differentialContext,
         Topology topology) {
      InterfaceSet flowSinks = null;
      if (differentialContext) {
         flowSinks = getFlowSinkSet(_baseEnvSettings.getDataPlanePath());
      }
      NodeSet blacklistNodes = getNodeBlacklist(envSettings);
      if (blacklistNodes != null) {
         if (differentialContext) {
            flowSinks.removeNodes(blacklistNodes);
         }
      }
      Set<NodeInterfacePair> blacklistInterfaces = getInterfaceBlacklist(envSettings);
      if (blacklistInterfaces != null) {
         for (NodeInterfacePair blacklistInterface : blacklistInterfaces) {
            if (differentialContext) {
               flowSinks.remove(blacklistInterface);
            }
         }
      }
      if (!differentialContext) {
         flowSinks = computeFlowSinks(configurations, topology);
      }
      return flowSinks;
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
            if (iface.getActive() && !iface.isLoopback(node.getConfigurationFormat())
                  && !topologyInterfaces.contains(p)) {
               flowSinks.add(p);
            }
         }
      }
      return flowSinks;
   }

   public Set<Flow> computeNodOutput(List<NodJob> jobs) {
      _logger.info("\n*** EXECUTING NOD JOBS ***\n");
      resetTimer();
      Set<Flow> flows = new TreeSet<Flow>();
      BatfishJobExecutor<NodJob, NodAnswerElement, NodJobResult, Set<Flow>> executor = new BatfishJobExecutor<NodJob, NodAnswerElement, NodJobResult, Set<Flow>>(
            _settings, _logger);
      // todo: do something with nod answer element
      executor.executeJobs(jobs, flows, new NodAnswerElement());
      printElapsedTime();
      return flows;
   }

   public <Key> void computeNodSatOutput(List<NodSatJob<Key>> jobs,
         Map<Key, Boolean> output) {
      _logger.info("\n*** EXECUTING NOD SAT JOBS ***\n");
      resetTimer();
      BatfishJobExecutor<NodSatJob<Key>, NodSatAnswerElement, NodSatResult<Key>, Map<Key, Boolean>> executor = new BatfishJobExecutor<NodSatJob<Key>, NodSatAnswerElement, NodSatResult<Key>, Map<Key, Boolean>>(
            _settings, _logger);
      executor.executeJobs(jobs, output, new NodSatAnswerElement());
      printElapsedTime();
   }

   public Topology computeTopology(Map<String, Configuration> configurations,
         EnvironmentSettings envSettings) {
      Topology topology = computeTopology(_settings.getTestRigPath(),
            configurations);
      String edgeBlacklistPath = envSettings.getEdgeBlacklistPath();
      EdgeSet blacklistEdges = getEdgeBlacklist(envSettings);
      if (edgeBlacklistPath != null) {
         File edgeBlacklistPathAsFile = new File(edgeBlacklistPath);
         if (edgeBlacklistPathAsFile.exists()) {
            EdgeSet edges = topology.getEdges();
            edges.removeAll(blacklistEdges);
         }
      }
      NodeSet blacklistNodes = getNodeBlacklist(envSettings);
      if (blacklistNodes != null) {
         for (String blacklistNode : blacklistNodes) {
            topology.removeNode(blacklistNode);
         }
      }
      Set<NodeInterfacePair> blacklistInterfaces = getInterfaceBlacklist(envSettings);
      if (blacklistInterfaces != null) {
         for (NodeInterfacePair blacklistInterface : blacklistInterfaces) {
            topology.removeInterface(blacklistInterface);
         }
      }
      return topology;
   }

   private Topology computeTopology(String testRigPath,
         Map<String, Configuration> configurations) {
      Path topologyFilePath = Paths.get(testRigPath, TOPOLOGY_FILENAME);
      Topology topology;
      // Get generated facts from topology file
      if (Files.exists(topologyFilePath)) {
         topology = processTopologyFile(topologyFilePath.toFile());
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
         Map<String, GenericConfigObject> vendorConfigurations,
         ConvertConfigurationAnswerElement answerElement) {
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
         GenericConfigObject vc = vendorConfigurations.get(hostname);
         ConvertConfigurationJob job = new ConvertConfigurationJob(_settings,
               vc, hostname, warnings);
         jobs.add(job);
      }
      BatfishJobExecutor<ConvertConfigurationJob, ConvertConfigurationAnswerElement, ConvertConfigurationResult, Map<String, Configuration>> executor = new BatfishJobExecutor<ConvertConfigurationJob, ConvertConfigurationAnswerElement, ConvertConfigurationResult, Map<String, Configuration>>(
            _settings, _logger);
      executor.executeJobs(jobs, configurations, answerElement);
      printElapsedTime();
      return configurations;
   }

   private boolean dataPlaneDependenciesExist(EnvironmentSettings envSettings) {
      checkConfigurations();
      String dpPath = envSettings.getDataPlanePath();
      File dp = new File(dpPath);
      return dp.exists();
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

   public Object deserializeObject(File inputFile) {
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

   public Map<String, GenericConfigObject> deserializeVendorConfigurations(
         String serializedVendorConfigPath) {
      _logger.info("\n*** DESERIALIZING VENDOR CONFIGURATION STRUCTURES ***\n");
      resetTimer();
      Map<String, GenericConfigObject> vendorConfigurations = new TreeMap<String, GenericConfigObject>();
      File dir = new File(serializedVendorConfigPath);
      File[] serializedConfigs = dir.listFiles();
      if (serializedConfigs == null) {
         throw new BatfishException("Error reading vendor configs directory");
      }
      for (File serializedConfig : serializedConfigs) {
         String name = serializedConfig.getName();
         _logger.info("Reading vendor config: \"" + serializedConfig + "\"");
         Object object = deserializeObject(serializedConfig);
         GenericConfigObject vc = (GenericConfigObject) object;
         vendorConfigurations.put(name, vc);
         _logger.info("...OK\n");
      }
      printElapsedTime();
      return vendorConfigurations;
   }

   private void disableUnusableVpnInterfaces(
         Map<String, Configuration> configurations,
         EnvironmentSettings envSettings) {
      initRemoteIpsecVpns(configurations);
      for (Configuration c : configurations.values()) {
         for (IpsecVpn vpn : c.getIpsecVpns().values()) {
            if (vpn.getRemoteIpsecVpn() == null) {
               String hostname = c.getHostname();
               Interface bindInterface = vpn.getBindInterface();
               if (bindInterface != null) {
                  bindInterface.setActive(false);
                  String bindInterfaceName = bindInterface.getName();
                  _logger
                        .warnf(
                              "WARNING: Disabling unusable vpn interface because we cannot determine remote endpoint: \"%s:%s\"\n",
                              hostname, bindInterfaceName);
               }
            }
         }
      }
   }

   private void dumpControlPlaneFacts(EnvironmentSettings envSettings,
         Map<String, StringBuilder> factBins) {
      _logger.info("\n*** DUMPING CONTROL PLANE FACTS ***\n");
      dumpFacts(factBins, envSettings.getControlPlaneFactsDir());
   }

   private void dumpFacts(Map<String, StringBuilder> factBins, String factsDir) {
      resetTimer();
      Path factsDirPath = Paths.get(factsDir);
      try {
         Files.createDirectories(factsDirPath);
         for (String factsFilename : factBins.keySet()) {
            String[] factsLines = factBins.get(factsFilename).toString()
                  .split("\n");
            Set<String> uniqueFacts = new TreeSet<String>();
            for (int i = 1; i < factsLines.length; i++) {
               uniqueFacts.add(factsLines[i]);
            }
            StringBuilder factsBuilder = new StringBuilder();
            factsBuilder.append(factsLines[0] + "\n");
            for (String factsLine : uniqueFacts) {
               factsBuilder.append(factsLine + "\n");
            }
            String facts = factsBuilder.toString();
            Path factsFilePath = factsDirPath.resolve(factsFilename);
            _logger.info("Writing: \""
                  + factsFilePath.toAbsolutePath().toString() + "\"\n");
            FileUtils.write(factsFilePath.toFile(), facts);
         }
      }
      catch (IOException e) {
         throw new BatfishException("Failed to write fact dump file(s)", e);
      }
      printElapsedTime();
   }

   public void dumpTrafficFacts(Map<String, StringBuilder> factBins) {
      dumpTrafficFacts(factBins, _envSettings);
   }

   public void dumpTrafficFacts(Map<String, StringBuilder> factBins,
         EnvironmentSettings envSettings) {
      _logger.info("\n*** DUMPING TRAFFIC FACTS ***\n");
      dumpFacts(factBins, envSettings.getTrafficFactsDir());
   }

   private boolean environmentExists(EnvironmentSettings envSettings) {
      checkBaseDirExists();
      return new File(envSettings.getDataPlanePath()).getParentFile().exists();
   }

   private void flatten(String inputPath, String outputPath) {
      Map<File, String> configurationData = readConfigurationFiles(inputPath,
            BfConsts.RELPATH_CONFIGURATIONS_DIR);
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
      BatfishJobExecutor<FlattenVendorConfigurationJob, FlattenVendorConfigurationAnswerElement, FlattenVendorConfigurationResult, Map<File, String>> executor = new BatfishJobExecutor<FlattenVendorConfigurationJob, FlattenVendorConfigurationAnswerElement, FlattenVendorConfigurationResult, Map<File, String>>(
            _settings, _logger);
      // todo: do something with answer element
      executor.executeJobs(jobs, outputConfigurationData,
            new FlattenVendorConfigurationAnswerElement());
      printElapsedTime();
      for (Entry<File, String> e : outputConfigurationData.entrySet()) {
         File outputFile = e.getKey();
         String flatConfigText = e.getValue();
         String outputFileAsString = outputFile.toString();
         _logger.debug("Writing config to \"" + outputFileAsString + "\"...");
         CommonUtil.writeFile(outputFileAsString, flatConfigText);
         _logger.debug("OK\n");
      }
      Path inputTopologyPath = Paths.get(inputPath, TOPOLOGY_FILENAME);
      Path outputTopologyPath = Paths.get(outputPath, TOPOLOGY_FILENAME);
      if (Files.isRegularFile(inputTopologyPath)) {
         String topologyFileText = CommonUtil.readFile(inputTopologyPath
               .toFile());
         CommonUtil.writeFile(outputTopologyPath.toString(), topologyFileText);
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
            Interface iface = new Interface(ifaceName, configs.get(currentPair
                  .getHostname()));
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
         config.setConfigurationFormat(ConfigurationFormat.CISCO);
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
                     // create flow sink interface for stub with common deatils
                     String flowSinkName = "TenGibabitEthernet100/100";
                     Interface flowSink = new Interface(flowSinkName, stub);
                     flowSink.setPrefix(Prefix.ZERO);
                     flowSink.setActive(true);
                     flowSink.setBandwidth(10E9d);

                     stub.getInterfaces().put(flowSinkName, flowSink);
                     stub.setBgpProcess(new BgpProcess());
                     stub.getPolicyMaps().put(stubOriginationPolicyName,
                           stubOriginationPolicy);
                     stub.getRouteFilterLists().put(
                           stubOriginationRouteFilterListName, rf);
                     stub.setConfigurationFormat(ConfigurationFormat.CISCO);
                     stub.setRoles(stubRoles);
                     nodeRoles.put(hostname, stubRoles);
                  }

                  // create interface that will on which peering will occur
                  Map<String, Interface> stubInterfaces = stub.getInterfaces();
                  String stubInterfaceName = "TenGigabitEthernet0/"
                        + (stubInterfaces.size() - 1);
                  Interface stubInterface = new Interface(stubInterfaceName,
                        stub);
                  stubInterfaces.put(stubInterfaceName, stubInterface);
                  stubInterface.setPrefix(new Prefix(neighborAddress, prefix
                        .getPrefixLength()));
                  stubInterface.setActive(true);
                  stubInterface.setBandwidth(10E9d);

                  // create neighbor within bgp process
                  BgpNeighbor edgeNeighbor = new BgpNeighbor(prefix, stub);
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

   private AdvertisementSet getAdvertisements(EnvironmentSettings envSettings) {
      checkDataPlaneFacts(_envSettings);
      AdvertisementSet adverts = new AdvertisementSet();
      EntityTable entityTable = initEntityTable(envSettings);
      Relation relation = getRelation(envSettings,
            BGP_ADVERTISEMENT_PREDICATE_NAME);
      List<BgpAdvertisement> advertList = relation.getColumns().get(0)
            .asBgpAdvertisementList(entityTable);
      adverts.addAll(advertList);
      return adverts;
   }

   public EnvironmentSettings getBaseEnvSettings() {
      return _baseEnvSettings;
   }

   public Map<String, Configuration> getConfigurations(
         String serializedVendorConfigPath,
         ConvertConfigurationAnswerElement answerElement) {
      Map<String, GenericConfigObject> vendorConfigurations = deserializeVendorConfigurations(serializedVendorConfigPath);
      Map<String, Configuration> configurations = convertConfigurations(
            vendorConfigurations, answerElement);
      return configurations;
   }

   private Map<String, Configuration> getDeltaConfigurations(
         EnvironmentSettings envSettings, Answer answer) {
      String deltaConfigurationsDir = envSettings.getDeltaConfigurationsDir();
      if (deltaConfigurationsDir != null) {
         File deltaConfigurationsDirAsFile = new File(deltaConfigurationsDir);
         if (deltaConfigurationsDirAsFile.exists()) {
            File configParentDir = deltaConfigurationsDirAsFile.getParentFile();
            Map<File, String> deltaConfigsText = readConfigurationFiles(
                  configParentDir.toString(),
                  BfConsts.RELPATH_CONFIGURATIONS_DIR);
            // todo: something about answer
            Map<String, VendorConfiguration> vendorDeltaConfigs = parseVendorConfigurations(
                  deltaConfigsText, new ParseVendorConfigurationAnswerElement());

            // convert the map to the right type
            Map<String, GenericConfigObject> castedConfigs = new HashMap<String, GenericConfigObject>();
            for (String name : vendorDeltaConfigs.keySet()) {
               castedConfigs.put(name, vendorDeltaConfigs.get(name));
            }
            // todo: something with answer
            Map<String, Configuration> deltaConfigs = convertConfigurations(
                  castedConfigs, new ConvertConfigurationAnswerElement());
            return deltaConfigs;
         }
      }
      return Collections.<String, Configuration> emptyMap();
   }

   public EnvironmentSettings getDiffEnvSettings() {
      return _diffEnvSettings;
   }

   public String getDifferentialFlowTag() {
      return _settings.getQuestionName() + ":" + _baseEnvSettings.getName()
            + ":" + _diffEnvSettings.getName();
   }

   public EdgeSet getEdgeBlacklist(EnvironmentSettings envSettings) {
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

   public EnvironmentSettings getEnvSettings() {
      return _envSettings;
   }

   private InterfaceSet getFlowSinkSet(EnvironmentSettings envSettings) {
      InterfaceSet flowSinks = new InterfaceSet();
      Relation relation = getRelation(envSettings, FLOW_SINK_PREDICATE_NAME);
      List<String> nodes = relation.getColumns().get(0).asStringList();
      List<String> interfaces = relation.getColumns().get(1).asStringList();
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

   public String getFlowTag() {
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

   public FlowHistory getHistory() {
      FlowHistory flowHistory = new FlowHistory();
      if (_settings.getDiffQuestion()) {
         checkTrafficFacts(_baseEnvSettings);
         checkTrafficFacts(_diffEnvSettings);
         String tag = getDifferentialFlowTag();
         populateFlowHistory(flowHistory, _baseEnvSettings,
               _baseEnvSettings.getName(), tag);
         populateFlowHistory(flowHistory, _diffEnvSettings,
               _diffEnvSettings.getName(), tag);
      }
      else {
         checkTrafficFacts(_envSettings);
         String tag = getFlowTag();
         populateFlowHistory(flowHistory, _envSettings, _envSettings.getName(),
               tag);
      }
      _logger.debug(flowHistory.toString());
      return flowHistory;
   }

   private IbgpTopology getIbgpNeighbors() {
      return getIbgpNeighbors(_envSettings);
   }

   private IbgpTopology getIbgpNeighbors(EnvironmentSettings envSettings) {
      checkDataPlaneFacts(_envSettings);
      IbgpTopology topology = new IbgpTopology();
      Relation relation = getRelation(envSettings,
            IBGP_NEIGHBORS_PREDICATE_NAME);
      List<String> node1List = relation.getColumns().get(0).asStringList();
      List<Ip> ip1List = relation.getColumns().get(1).asIpList();
      List<String> node2List = relation.getColumns().get(2).asStringList();
      List<Ip> ip2List = relation.getColumns().get(3).asIpList();
      int numEntries = node1List.size();
      for (int i = 0; i < numEntries; i++) {
         String node1 = node1List.get(i);
         String node2 = node2List.get(i);
         Ip ip1 = ip1List.get(i);
         Ip ip2 = ip2List.get(i);
         NodeIpPair p1 = new NodeIpPair(node1, ip1);
         NodeIpPair p2 = new NodeIpPair(node2, ip2);
         IpEdge edge = new IpEdge(p1, p2);
         topology.add(edge);
      }
      return topology;
   }

   public Set<NodeInterfacePair> getInterfaceBlacklist(
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

   public BatfishLogger getLogger() {
      return _logger;
   }

   private Set<String> getNlsDataPlaneOutputSymbols() {
      Set<String> symbols = new HashSet<String>();
      symbols.addAll(NlsConstants.NLS_DATA_PLANE_OUTPUT_SYMBOLS);
      if (_settings.getNlsDebugSymbols()) {
         symbols.addAll(NlsConstants.NLS_DATA_PLANE_OUTPUT_DEBUG_SYMBOLS);
      }
      return symbols;
   }

   private String[] getNlsLogicFilenames(File logicDir) {
      final Set<String> filenames = new TreeSet<String>();
      Path logicDirPath = Paths.get(logicDir.toString());
      FileVisitor<Path> nlsLogicFileCollector = new SimpleFileVisitor<Path>() {
         @Override
         public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
               throws IOException {
            String fileStr = file.toString();
            if (fileStr.endsWith(".pl")) {
               filenames.add(fileStr);
            }
            return FileVisitResult.CONTINUE;
         }
      };
      try {
         Files.walkFileTree(logicDirPath, nlsLogicFileCollector);
      }
      catch (IOException e) {
         throw new BatfishException("failed to retreive nls logic files", e);
      }
      return filenames.toArray(new String[] {});
   }

   private String getNlsText(EnvironmentSettings envSettings,
         String relationName) {
      String nlsOutputDir;
      if (getNlsDataPlaneOutputSymbols().contains(relationName)) {
         nlsOutputDir = envSettings.getNlsDataPlaneOutputDir();
      }
      else if (getNlsTrafficOutputSymbols().contains(relationName)) {
         nlsOutputDir = envSettings.getNlsTrafficOutputDir();
      }
      else {
         throw new BatfishException("Predicate: \"" + relationName
               + "\" not an output symbol");
      }
      return getNlsText(nlsOutputDir, relationName);
   }

   private String getNlsText(String nlsOutputDir, String relationName) {
      File relationFile = Paths.get(nlsOutputDir, relationName).toFile();
      String content = CommonUtil.readFile(relationFile);
      return content;
   }

   private Set<String> getNlsTrafficOutputSymbols() {
      Set<String> symbols = new HashSet<String>();
      symbols.addAll(NlsConstants.NLS_TRAFFIC_OUTPUT_SYMBOLS);
      if (_settings.getNlsDebugSymbols()) {
         symbols.addAll(NlsConstants.NLS_TRAFFIC_OUTPUT_DEBUG_SYMBOLS);
      }
      return symbols;
   }

   public NodeSet getNodeBlacklist(EnvironmentSettings envSettings) {
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
         EnvironmentSettings envSettings) {
      PolicyRouteFibNodeMap nodeMap = new PolicyRouteFibNodeMap();
      Relation relation = getRelation(envSettings,
            FIB_POLICY_ROUTE_NEXT_HOP_PREDICATE_NAME);
      List<String> nodeList = relation.getColumns().get(0).asStringList();
      List<Ip> ipList = relation.getColumns().get(1).asIpList();
      List<String> outInterfaces = relation.getColumns().get(2).asStringList();
      List<String> inNodes = relation.getColumns().get(3).asStringList();
      List<String> inInterfaces = relation.getColumns().get(4).asStringList();
      int size = nodeList.size();
      for (int i = 0; i < size; i++) {
         String nodeOut = nodeList.get(i);
         String nodeIn = inNodes.get(i);
         Ip ip = ipList.get(i);
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

   private Relation getRelation(EnvironmentSettings envSettings,
         String predicateName) {
      String nlsText = getNlsText(envSettings, predicateName);
      Relation relation = new Relation.Builder(predicateName).build(
            _predicateInfo, nlsText);
      return relation;
   }

   private FibMap getRouteForwardingRules(EnvironmentSettings envSettings) {
      FibMap fibs = new FibMap();
      Relation relation = getRelation(envSettings, FIB_PREDICATE_NAME);
      EntityTable entityTable = initEntityTable(envSettings);
      List<String> nameList = relation.getColumns().get(0).asStringList();
      List<Prefix> networkList = relation.getColumns().get(1)
            .asPrefixList(entityTable);
      List<String> interfaceList = relation.getColumns().get(2).asStringList();
      List<String> nextHopList = relation.getColumns().get(3).asStringList();
      List<String> nextHopIntList = relation.getColumns().get(4).asStringList();

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
            Prefix prefix = networkList.get(i);
            String iface = interfaceList.get(i);
            String nextHop = nextHopList.get(i);
            String nextHopInt = nextHopIntList.get(i);
            fibRows.add(new FibRow(prefix, iface, nextHop, nextHopInt));
         }
      }
      return fibs;
   }

   private RouteSet getRoutes(EnvironmentSettings envSettings) {
      checkDataPlaneFacts(envSettings);
      RouteSet routes = new RouteSet();
      EntityTable entityTable = initEntityTable(envSettings);
      Relation relation = getRelation(envSettings,
            INSTALLED_ROUTE_PREDICATE_NAME);
      List<PrecomputedRoute> routeList = relation.getColumns().get(0)
            .asRouteList(entityTable);
      routes.addAll(routeList);
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
               String contents = CommonUtil.readFile(file.toFile());
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

   public Settings getSettings() {
      return _settings;
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

   public boolean getTerminatedWithException() {
      return _terminatedWithException;
   }

   private void histogram(String testRigPath) {
      Map<File, String> configurationData = readConfigurationFiles(testRigPath,
            BfConsts.RELPATH_CONFIGURATIONS_DIR);
      // todo: either remove histogram function or do something userful with
      // answer
      Map<String, VendorConfiguration> vendorConfigurations = parseVendorConfigurations(
            configurationData, new ParseVendorConfigurationAnswerElement());
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

   public void initBgpAdvertisements(Map<String, Configuration> configurations) {
      AdvertisementSet globalBgpAdvertisements = getAdvertisements(_envSettings);
      for (Configuration node : configurations.values()) {
         node.initBgpAdvertisements();
      }
      for (BgpAdvertisement bgpAdvertisement : globalBgpAdvertisements) {
         BgpAdvertisementType type = BgpAdvertisementType
               .fromNlsTypeName(bgpAdvertisement.getType());
         switch (type) {
         case EBGP_ORIGINATED: {
            String originationNodeName = bgpAdvertisement.getSrcNode();
            Configuration originationNode = configurations
                  .get(originationNodeName);
            if (originationNode != null) {
               originationNode.getBgpAdvertisements().add(bgpAdvertisement);
               originationNode.getOriginatedAdvertisements().add(
                     bgpAdvertisement);
               originationNode.getOriginatedEbgpAdvertisements().add(
                     bgpAdvertisement);
            }
            else {
               throw new BatfishException(
                     "Originated bgp advertisement refers to missing node: \""
                           + originationNodeName + "\"");
            }
            break;
         }

         case IBGP_ORIGINATED: {
            String originationNodeName = bgpAdvertisement.getSrcNode();
            Configuration originationNode = configurations
                  .get(originationNodeName);
            if (originationNode != null) {
               originationNode.getBgpAdvertisements().add(bgpAdvertisement);
               originationNode.getOriginatedAdvertisements().add(
                     bgpAdvertisement);
               originationNode.getOriginatedIbgpAdvertisements().add(
                     bgpAdvertisement);
            }
            else {
               throw new BatfishException(
                     "Originated bgp advertisement refers to missing node: \""
                           + originationNodeName + "\"");
            }
            break;
         }

         case EBGP_RECEIVED: {
            String recevingNodeName = bgpAdvertisement.getDstNode();
            Configuration receivingNode = configurations.get(recevingNodeName);
            if (receivingNode != null) {
               receivingNode.getBgpAdvertisements().add(bgpAdvertisement);
               receivingNode.getReceivedAdvertisements().add(bgpAdvertisement);
               receivingNode.getReceivedEbgpAdvertisements().add(
                     bgpAdvertisement);
            }
            break;
         }

         case IBGP_RECEIVED: {
            String recevingNodeName = bgpAdvertisement.getDstNode();
            Configuration receivingNode = configurations.get(recevingNodeName);
            if (receivingNode != null) {
               receivingNode.getBgpAdvertisements().add(bgpAdvertisement);
               receivingNode.getReceivedAdvertisements().add(bgpAdvertisement);
               receivingNode.getReceivedIbgpAdvertisements().add(
                     bgpAdvertisement);
            }
            break;
         }

         case EBGP_SENT: {
            String sendingNodeName = bgpAdvertisement.getSrcNode();
            Configuration sendingNode = configurations.get(sendingNodeName);
            if (sendingNode != null) {
               sendingNode.getBgpAdvertisements().add(bgpAdvertisement);
               sendingNode.getSentAdvertisements().add(bgpAdvertisement);
               sendingNode.getSentEbgpAdvertisements().add(bgpAdvertisement);
            }
            break;
         }

         case IBGP_SENT: {
            String sendingNodeName = bgpAdvertisement.getSrcNode();
            Configuration sendingNode = configurations.get(sendingNodeName);
            if (sendingNode != null) {
               sendingNode.getBgpAdvertisements().add(bgpAdvertisement);
               sendingNode.getSentAdvertisements().add(bgpAdvertisement);
               sendingNode.getSentIbgpAdvertisements().add(bgpAdvertisement);
            }
            break;
         }

         default:
            throw new BatfishException("Invalid bgp advertisement type");
         }
      }
   }

   public void initBgpOriginationSpaceExplicit(
         Map<String, Configuration> configurations) {
      ProtocolDependencyAnalysis protocolDependencyAnalysis = new ProtocolDependencyAnalysis(
            configurations);
      DependencyDatabase database = protocolDependencyAnalysis
            .getDependencyDatabase();

      for (Entry<String, Configuration> e : configurations.entrySet()) {
         PrefixSpace ebgpExportSpace = new PrefixSpace();
         String name = e.getKey();
         Configuration node = e.getValue();
         BgpProcess proc = node.getBgpProcess();
         if (proc != null) {
            Set<PotentialExport> bgpExports = database.getPotentialExports(
                  name, RoutingProtocol.BGP);
            for (PotentialExport export : bgpExports) {
               DependentRoute exportSourceRoute = export.getDependency();
               if (!exportSourceRoute.dependsOn(RoutingProtocol.BGP)
                     && !exportSourceRoute.dependsOn(RoutingProtocol.IBGP)) {
                  Prefix prefix = export.getPrefix();
                  ebgpExportSpace.addPrefix(prefix);
               }
            }
            proc.setOriginationSpace(ebgpExportSpace);
         }
      }
   }

   private EntityTable initEntityTable(EnvironmentSettings envSettings) {
      EntityTable entityTable = _entityTables.get(envSettings);
      if (entityTable == null) {
         Map<String, String> nlsPredicateContents = new HashMap<String, String>();
         String nlsDataPlaneOutputDir = envSettings.getNlsDataPlaneOutputDir();
         String nlsTrafficOutputDir = envSettings.getNlsTrafficOutputDir();
         if (nlsDataPlaneOutputDir != null
               && new File(nlsDataPlaneOutputDir).exists()) {
            nlsPredicateContents.putAll(readFacts(nlsDataPlaneOutputDir,
                  NlsConstants.NLS_DATA_PLANE_ENTITY_SYMBOLS));
         }
         if (nlsTrafficOutputDir != null
               && new File(nlsTrafficOutputDir).exists()) {
            nlsPredicateContents.putAll(readFacts(nlsTrafficOutputDir,
                  NlsConstants.NLS_TRAFFIC_ENTITY_SYMBOLS));
         }
         entityTable = new EntityTable(nlsPredicateContents, _predicateInfo);
         _entityTables.put(envSettings, entityTable);
      }
      return entityTable;
   }

   private void initQuestionEnvironment(EnvironmentSettings envSettings,
         Question question, boolean dp, boolean differentialContext) {
      if (!environmentExists(envSettings)) {
         File envPath = new File(envSettings.getEnvPath());
         // create environment required folders
         envPath.mkdirs();
         // write node blacklist from question
         if (!question.getNodeBlacklist().isEmpty()) {
            StringBuilder nodeBlacklistSb = new StringBuilder();
            for (String node : question.getNodeBlacklist()) {
               nodeBlacklistSb.append(node + "\n");
            }
            String nodeBlacklist = nodeBlacklistSb.toString();
            CommonUtil.writeFile(envSettings.getNodeBlacklistPath(),
                  nodeBlacklist);
         }
         // write interface blacklist from question
         if (!question.getInterfaceBlacklist().isEmpty()) {
            StringBuilder interfaceBlacklistSb = new StringBuilder();
            for (NodeInterfacePair pair : question.getInterfaceBlacklist()) {
               interfaceBlacklistSb.append(pair.getHostname() + ":"
                     + pair.getInterface() + "\n");
            }
            String interfaceBlacklist = interfaceBlacklistSb.toString();
            CommonUtil.writeFile(envSettings.getInterfaceBlacklistPath(),
                  interfaceBlacklist);
         }
      }
      if (dp && !dataPlaneDependenciesExist(envSettings)) {
         _settings.setDumpControlPlaneFacts(true);
         boolean usePrecomputedFacts = _settings.getUsePrecomputedFacts();
         Map<String, StringBuilder> cpFactBins = new LinkedHashMap<String, StringBuilder>();
         initControlPlaneFactBins(cpFactBins, !usePrecomputedFacts);
         if (!usePrecomputedFacts) {
            computeControlPlaneFacts(cpFactBins, differentialContext,
                  envSettings);
         }
         nlsDataPlane(envSettings);
         computeDataPlane(envSettings);
         _entityTables.clear();
      }
   }

   private void initQuestionEnvironments(Question question, boolean diff,
         boolean diffActive, boolean dp) {
      if (diff || !diffActive) {
         initQuestionEnvironment(_baseEnvSettings, question, dp, false);
      }
      if (diff || diffActive) {
         if (_settings.getDiffEnvironmentName() == null
               || (diffActive && !_settings.getDiffActive())) {
            String diffEnvironmentName = UUID.randomUUID().toString();
            _settings.setDiffEnvironmentName(diffEnvironmentName);
            applyAutoBaseDir(_settings);
            _envSettings = _diffEnvSettings;
         }
         initQuestionEnvironment(_diffEnvSettings, question, dp, true);
      }
   }

   public void initRemoteBgpNeighbors(Map<String, Configuration> configurations) {
      Map<BgpNeighbor, Ip> remoteAddresses = new HashMap<BgpNeighbor, Ip>();
      Map<Ip, Set<BgpNeighbor>> localAddresses = new HashMap<Ip, Set<BgpNeighbor>>();
      for (Configuration node : configurations.values()) {
         String hostname = node.getHostname();
         BgpProcess proc = node.getBgpProcess();
         if (proc != null) {
            for (BgpNeighbor bgpNeighbor : proc.getNeighbors().values()) {
               bgpNeighbor.initCandidateRemoteBgpNeighbors();
               if (bgpNeighbor.getPrefix().getPrefixLength() < 32) {
                  throw new BatfishException(
                        hostname
                              + ": Do not support dynamic bgp sessions at this time: "
                              + bgpNeighbor.getPrefix());
               }
               Ip remoteAddress = bgpNeighbor.getAddress();
               if (remoteAddress == null) {
                  throw new BatfishException(
                        hostname
                              + ": Could not determine remote address of bgp neighbor: "
                              + bgpNeighbor);
               }
               Ip localAddress = bgpNeighbor.getLocalIp();
               if (localAddress == null) {
                  continue;
               }
               remoteAddresses.put(bgpNeighbor, remoteAddress);
               Set<BgpNeighbor> localAddressOwners = localAddresses
                     .get(localAddress);
               if (localAddressOwners == null) {
                  localAddressOwners = new HashSet<BgpNeighbor>();
                  localAddresses.put(localAddress, localAddressOwners);
               }
               localAddressOwners.add(bgpNeighbor);
            }
         }
      }
      for (Entry<BgpNeighbor, Ip> e : remoteAddresses.entrySet()) {
         BgpNeighbor bgpNeighbor = e.getKey();
         Ip remoteAddress = e.getValue();
         Ip localAddress = bgpNeighbor.getLocalIp();
         Set<BgpNeighbor> remoteBgpNeighborCandidates = localAddresses
               .get(remoteAddress);
         if (remoteBgpNeighborCandidates != null) {
            for (BgpNeighbor remoteBgpNeighborCandidate : remoteBgpNeighborCandidates) {
               Ip reciprocalRemoteIp = remoteBgpNeighborCandidate.getAddress();
               if (localAddress.equals(reciprocalRemoteIp)) {
                  bgpNeighbor.getCandidateRemoteBgpNeighbors().add(
                        remoteBgpNeighborCandidate);
                  bgpNeighbor.setRemoteBgpNeighbor(remoteBgpNeighborCandidate);
               }
            }
         }
      }
   }

   public void initRemoteIpsecVpns(Map<String, Configuration> configurations) {
      Map<IpsecVpn, Ip> remoteAddresses = new HashMap<IpsecVpn, Ip>();
      Map<Ip, Set<IpsecVpn>> externalAddresses = new HashMap<Ip, Set<IpsecVpn>>();
      for (Configuration c : configurations.values()) {
         for (IpsecVpn ipsecVpn : c.getIpsecVpns().values()) {
            Ip remoteAddress = ipsecVpn.getGateway().getAddress();
            remoteAddresses.put(ipsecVpn, remoteAddress);
            Set<Prefix> externalPrefixes = ipsecVpn.getGateway()
                  .getExternalInterface().getAllPrefixes();
            for (Prefix externalPrefix : externalPrefixes) {
               Ip externalAddress = externalPrefix.getAddress();
               Set<IpsecVpn> vpnsUsingExternalAddress = externalAddresses
                     .get(externalAddress);
               if (vpnsUsingExternalAddress == null) {
                  vpnsUsingExternalAddress = new HashSet<IpsecVpn>();
                  externalAddresses.put(externalAddress,
                        vpnsUsingExternalAddress);
               }
               vpnsUsingExternalAddress.add(ipsecVpn);
            }
         }
      }
      for (Entry<IpsecVpn, Ip> e : remoteAddresses.entrySet()) {
         IpsecVpn ipsecVpn = e.getKey();
         Ip remoteAddress = e.getValue();
         ipsecVpn.initCandidateRemoteVpns();
         Set<IpsecVpn> remoteIpsecVpnCandidates = externalAddresses
               .get(remoteAddress);
         if (remoteIpsecVpnCandidates != null) {
            for (IpsecVpn remoteIpsecVpnCandidate : remoteIpsecVpnCandidates) {
               Ip remoteIpsecVpnLocalAddress = remoteIpsecVpnCandidate
                     .getGateway().getLocalAddress();
               if (remoteIpsecVpnLocalAddress != null
                     && !remoteIpsecVpnLocalAddress.equals(remoteAddress)) {
                  continue;
               }
               Ip reciprocalRemoteAddress = remoteAddresses
                     .get(remoteIpsecVpnCandidate);
               Set<IpsecVpn> reciprocalVpns = externalAddresses
                     .get(reciprocalRemoteAddress);
               if (reciprocalVpns != null && reciprocalVpns.contains(ipsecVpn)) {
                  ipsecVpn.setRemoteIpsecVpn(remoteIpsecVpnCandidate);
                  ipsecVpn.getCandidateRemoteIpsecVpns().add(
                        remoteIpsecVpnCandidate);
               }
            }
         }
      }
   }

   public void initRoutes(Map<String, Configuration> configurations) {
      Set<PrecomputedRoute> globalRoutes = getRoutes(_envSettings);
      for (Configuration node : configurations.values()) {
         node.initRoutes();
      }
      for (PrecomputedRoute route : globalRoutes) {
         String nodeName = route.getNode();
         Configuration node = configurations.get(nodeName);
         if (node != null) {
            node.getRoutes().add(route);
         }
         else {
            throw new BatfishException(
                  "Precomputed route refers to missing node: \"" + nodeName
                        + "\"");
         }
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
      // lbFrontend.removeBlocks(qualifiedBlockNames);
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
      processDeltaConfigurations(configurations, envSettings, new Answer());
      disableUnusableVpnInterfaces(configurations, envSettings);
      return configurations;
   }

   public Topology loadTopology(EnvironmentSettings envSettings) {
      String topologyPath = envSettings.getSerializedTopologyPath();
      File topologyPathFile = new File(topologyPath);
      _logger.info("Deserializing topology...");
      Topology topology = (Topology) deserializeObject(topologyPathFile);
      _logger.info("OK\n");
      return topology;
   }

   private Answer nlsDataPlane(EnvironmentSettings envSettings) {
      Map<String, String> inputFacts = readFacts(
            envSettings.getControlPlaneFactsDir(),
            NlsConstants.NLS_DATA_PLANE_COMPUTATION_FACTS);
      writeNlsInput(getNlsDataPlaneOutputSymbols(), inputFacts,
            envSettings.getNlsDataPlaneInputFile(), envSettings);
      Answer answer = runNls(envSettings.getNlsDataPlaneInputFile(),
            envSettings.getNlsDataPlaneOutputDir());
      if (!_settings.getNlsDry()) {
         writeRoutes(envSettings.getPrecomputedRoutesPath(), envSettings);
      }
      return answer;
   }

   public void nlsTraffic() {
      if (_settings.getDiffQuestion()) {
         nlsTraffic(_baseEnvSettings);
         nlsTraffic(_diffEnvSettings);
      }
      else {
         nlsTraffic(_envSettings);
      }
   }

   private Answer nlsTraffic(EnvironmentSettings envSettings) {
      writeNlsPrecomputedRoutes(envSettings);
      Map<String, String> inputControlPlaneFacts = readFacts(
            envSettings.getControlPlaneFactsDir(),
            NlsConstants.NLS_TRAFFIC_COMPUTATION_CONTROL_PLANE_FACTS);
      Map<String, String> inputFlowFacts = readFacts(
            envSettings.getTrafficFactsDir(),
            NlsConstants.NLS_TRAFFIC_COMPUTATION_FLOW_FACTS);
      Map<String, String> inputFacts = new TreeMap<String, String>();
      inputFacts.putAll(inputControlPlaneFacts);
      inputFacts.putAll(inputFlowFacts);
      writeNlsInput(getNlsTrafficOutputSymbols(), inputFacts,
            envSettings.getNlsTrafficInputFile(), envSettings);
      Answer answer = runNls(envSettings.getNlsTrafficInputFile(),
            envSettings.getNlsTrafficOutputDir());
      return answer;
   }

   void outputAnswer(Answer answer) {
      ObjectMapper mapper = new BatfishObjectMapper();
      try {
         String jsonString = mapper.writeValueAsString(answer);
         _logger.debug(jsonString);
         writeJsonAnswer(jsonString);
      }
      catch (Exception e) {
         BatfishException be = new BatfishException("Error in sending answer",
               e);
         Answer failureAnswer = Answer.failureAnswer(e.getMessage());
         failureAnswer.addAnswerElement(be);
         try {
            String failureJsonString = mapper.writeValueAsString(failureAnswer);
            _logger.error(failureJsonString);
            writeJsonAnswer(failureJsonString);
         }
         catch (Exception e1) {
            String errorMessage = String.format(
                  "Could not serialize failure answer.",
                  ExceptionUtils.getStackTrace(e1));
            _logger.error(errorMessage);
         }
         throw be;
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

   private AwsVpcConfiguration parseAwsVpcConfigurations(
         Map<File, String> configurationData) {
      AwsVpcConfiguration config = new AwsVpcConfiguration();
      for (File file : configurationData.keySet()) {

         // we stop classic link processing here because it interferes with VPC
         // processing
         if (file.toString().contains("classic-link")) {
            _logger.errorf("%s has classic link configuration\n",
                  file.toString());
            continue;
         }

         JSONObject jsonObj = null;
         try {
            jsonObj = new JSONObject(configurationData.get(file));
         }
         catch (JSONException e) {
            _logger.errorf("%s does not have valid json\n", file.toString());
         }

         if (jsonObj != null) {
            try {
               config.addConfigElement(jsonObj, _logger);
            }
            catch (JSONException e) {
               throw new BatfishException("Problems parsing JSON in "
                     + file.toString(), e);
            }
         }
      }
      return config;
   }

   private Set<NodeInterfacePair> parseInterfaceBlacklist(
         File interfaceBlacklistPath) {
      Set<NodeInterfacePair> ifaces = new TreeSet<NodeInterfacePair>();
      String interfaceBlacklistText = CommonUtil
            .readFile(interfaceBlacklistPath);
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
      String nodeBlacklistText = CommonUtil.readFile(nodeBlacklistPath);
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
      String roleFileText = CommonUtil.readFile(rolePath.toFile());
      _logger.info("Parsing: \"" + rolePath.toAbsolutePath().toString() + "\"");
      BatfishCombinedParser<?, ?> parser = new RoleCombinedParser(roleFileText,
            _settings);
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
      String questionText = CommonUtil.readFile(questionFile);
      _logger.info("OK\n");

      try {
         ObjectMapper mapper = new ObjectMapper();
         Question question = mapper.readValue(questionText, Question.class);
         JSONObject parameters = (JSONObject) parseQuestionParameters();
         question.setJsonParameters(parameters);
         return question;
      }
      catch (IOException e) {
         throw new BatfishException("Could not parse JSON question", e);
      }
   }

   private Object parseQuestionParameters() {
      String questionParametersPath = _settings.getQuestionParametersPath();
      File questionParametersFile = new File(questionParametersPath);
      if (!questionParametersFile.exists()) {
         throw new BatfishException("Missing question parameters file: \""
               + questionParametersPath + "\"");
      }
      _logger.info("Reading question parameters file: \""
            + questionParametersPath + "\"...");
      String questionText = CommonUtil.readFile(questionParametersFile);
      _logger.info("OK\n");

      try {
         JSONObject jObj = (questionText.trim().isEmpty()) ? new JSONObject()
               : new JSONObject(questionText);
         return jObj;
      }
      catch (JSONException e) {
         throw new BatfishException("Could not parse JSON parameters", e);
      }
   }

   private Topology parseTopology(File topologyFilePath) {
      _logger.info("*** PARSING TOPOLOGY ***\n");
      resetTimer();
      String topologyFileText = CommonUtil.readFile(topologyFilePath);
      BatfishCombinedParser<?, ?> parser = null;
      TopologyExtractor extractor = null;
      _logger.info("Parsing: \""
            + topologyFilePath.getAbsolutePath().toString() + "\" ...");
      if (topologyFileText.startsWith("autostart")) {
         parser = new GNS3TopologyCombinedParser(topologyFileText, _settings);
         extractor = new GNS3TopologyExtractor();
      }
      else if (topologyFileText
            .startsWith(BatfishTopologyCombinedParser.HEADER)) {
         parser = new BatfishTopologyCombinedParser(topologyFileText, _settings);
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
         Map<File, String> configurationData,
         ParseVendorConfigurationAnswerElement answerElement) {
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
      BatfishJobExecutor<ParseVendorConfigurationJob, ParseVendorConfigurationAnswerElement, ParseVendorConfigurationResult, Map<String, VendorConfiguration>> executor = new BatfishJobExecutor<ParseVendorConfigurationJob, ParseVendorConfigurationAnswerElement, ParseVendorConfigurationResult, Map<String, VendorConfiguration>>(
            _settings, _logger);

      executor.executeJobs(jobs, vendorConfigurations, answerElement);
      printElapsedTime();
      return vendorConfigurations;
   }

   private void populateConfigurationFactBins(
         Collection<Configuration> configurations, CommunitySet allCommunities,
         Map<String, StringBuilder> factBins) {
      _logger.info("\n*** EXTRACTING FACTS FROM CONFIGURATIONS ***\n");
      resetTimer();
      for (Configuration c : configurations) {
         allCommunities.addAll(c.getCommunities());
      }
      Set<Ip> interfaceIps = new HashSet<Ip>();
      Set<Ip> externalBgpRemoteIps = new TreeSet<Ip>();
      for (Configuration c : configurations) {
         for (Interface i : c.getInterfaces().values()) {
            for (Prefix p : i.getAllPrefixes()) {
               Ip ip = p.getAddress();
               interfaceIps.add(ip);
            }
         }
         BgpProcess proc = c.getBgpProcess();
         if (proc != null) {
            for (Prefix neighborPrefix : proc.getNeighbors().keySet()) {
               if (neighborPrefix.getPrefixLength() == Prefix.MAX_PREFIX_LENGTH) {
                  Ip neighborAddress = neighborPrefix.getAddress();
                  externalBgpRemoteIps.add(neighborAddress);
               }
            }
         }
      }
      externalBgpRemoteIps.removeAll(interfaceIps);
      StringBuilder wSetExternalBgpRemoteIp = factBins
            .get("SetExternalBgpRemoteIp");
      StringBuilder wSetNetwork = factBins.get("SetNetwork");
      for (Ip ip : externalBgpRemoteIps) {
         String node = ip.toString();
         long ipAsLong = ip.asLong();
         wSetExternalBgpRemoteIp.append(node + "|" + ipAsLong + "\n");
         wSetNetwork.append(ipAsLong + "|" + ipAsLong + "|" + ipAsLong + "|"
               + Prefix.MAX_PREFIX_LENGTH + "\n");
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
                  allCommunities, factBins, warnings);
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
            logWarnings(_logger, warnings);
         }
      }
      if (processingError) {
         throw new BatfishException(
               "Failed to extract facts from vendor-indpendent configuration structures");
      }
      printElapsedTime();
   }

   private void populateFlowHistory(FlowHistory flowHistory,
         EnvironmentSettings envSettings, String environmentName, String tag) {
      EntityTable entityTable = initEntityTable(envSettings);
      Relation relation = getRelation(envSettings, FLOW_HISTORY_PREDICATE_NAME);
      List<Flow> flows = relation.getColumns().get(0).asFlowList(entityTable);
      List<String> historyLines = relation.getColumns().get(1).asStringList();
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
         AdvertisementSet advertSet, Map<String, StringBuilder> cpFactBins) {
      StringBuilder adverts = cpFactBins
            .get(PRECOMPUTED_BGP_ADVERTISEMENTS_PREDICATE_NAME);
      StringBuilder advertCommunities = cpFactBins
            .get(PRECOMPUTED_BGP_ADVERTISEMENT_COMMUNITY_PREDICATE_NAME);
      StringBuilder advertPaths = cpFactBins
            .get(PRECOMPUTED_BGP_ADVERTISEMENT_AS_PATH_PREDICATE_NAME);
      StringBuilder advertPathLengths = cpFactBins
            .get(PRECOMPUTED_BGP_ADVERTISEMENT_AS_PATH_LENGTH_PREDICATE_NAME);
      StringBuilder wNetworks = cpFactBins.get(NETWORKS_PREDICATE_NAME);
      Set<Prefix> networks = new HashSet<Prefix>();
      int pcIndex = 0;
      for (BgpAdvertisement advert : advertSet) {
         String type = advert.getType();
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

   private void populatePrecomputedBgpAdvertisements(
         String precomputedBgpAdvertisementsPath,
         Map<String, StringBuilder> cpFactBins) {
      File inputFile = new File(precomputedBgpAdvertisementsPath);
      AdvertisementSet rawAdvertSet = (AdvertisementSet) deserializeObject(inputFile);
      AdvertisementSet incomingAdvertSet = new AdvertisementSet();
      for (BgpAdvertisement advert : rawAdvertSet) {
         String type = advert.getType();
         switch (type) {
         case "ibgp_ti":
         case "bgp_ti":
            incomingAdvertSet.add(advert);
            break;

         default:
            continue;
         }
      }
      populatePrecomputedBgpAdvertisements(incomingAdvertSet, cpFactBins);
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
         String contents = CommonUtil.readFile(file);
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

   private void populatePrecomputedRoutes(List<String> precomputedRoutesPaths,
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

   public void printElapsedTime() {
      double seconds = getElapsedTime(_timerCount);
      _logger.info("Time taken for this task: " + seconds + " seconds\n");
   }

   private void printPredicate(EnvironmentSettings envSettings,
         String predicateName) {
      boolean function = _predicateInfo.isFunction(predicateName);
      StringBuilder sb = new StringBuilder();
      EntityTable entityTable = initEntityTable(envSettings);
      Relation relation = getRelation(envSettings, predicateName);
      List<Column> columns = relation.getColumns();
      List<LBValueType> valueTypes = _predicateInfo
            .getPredicateValueTypes(predicateName);
      int numColumns = columns.size();
      int numRows = relation.getNumRows();
      for (int i = 0; i < numRows; i++) {
         sb.append(predicateName);
         if (function) {
            sb.append("[");
         }
         else {
            sb.append("(");
         }
         for (int j = 0; j < numColumns; j++) {
            boolean last = (j == numColumns - 1);
            boolean penultimate = (j == numColumns - 2);
            String part = columns.get(j)
                  .getItem(i, entityTable, valueTypes.get(j)).toString();
            sb.append(part);
            if ((function && !last && !penultimate) || (!function && !last)) {
               sb.append(", ");
            }
            else if (function && penultimate) {
               sb.append("] = ");
            }
            else if (last) {
               if (!function) {
                  sb.append(")");
               }
               sb.append(".\n");
            }
         }
      }
      _logger.output(sb.toString());
   }

   public void printPredicates(EnvironmentSettings envSettings,
         Set<String> predicateNames) {
      // Print predicate(s) here
      _logger.info("\n*** SUBMITTING QUERY(IES) ***\n");
      resetTimer();
      for (String predicateName : predicateNames) {
         printPredicate(envSettings, predicateName);
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

   private void processDeltaConfigurations(
         Map<String, Configuration> configurations,
         EnvironmentSettings envSettings, Answer answer) {
      Map<String, Configuration> deltaConfigurations = getDeltaConfigurations(
            envSettings, answer);
      configurations.putAll(deltaConfigurations);
      // TODO: deal with topological changes
   }

   /**
    * Reads the external bgp announcement specified in the environment, and
    * populates the vendor-independent configurations with data about those
    * announcements
    *
    * @param configurations
    *           The vendor-independent configurations to be modified
    * @param envSettings
    *           The settings for the environment, containing e.g. the path to
    *           the external announcements file
    * @param cpFactBins
    *           The container for nls facts
    * @param allCommunities
    */
   private void processExternalBgpAnnouncements(
         Map<String, Configuration> configurations,
         EnvironmentSettings envSettings,
         Map<String, StringBuilder> cpFactBins, CommunitySet allCommunities) {
      AdvertisementSet advertSet = new AdvertisementSet();
      String externalBgpAnnouncementsPath = envSettings
            .getExternalBgpAnnouncementsPath();
      File externalBgpAnnouncementsFile = new File(externalBgpAnnouncementsPath);
      if (externalBgpAnnouncementsFile.exists()) {
         String externalBgpAnnouncementsFileContents = CommonUtil
               .readFile(externalBgpAnnouncementsFile);
         // Populate advertSet with BgpAdvertisements that
         // gets passed to populatePrecomputedBgpAdvertisements.
         // See populatePrecomputedBgpAdvertisements for the things that get
         // extracted from these advertisements.

         try {
            JSONObject jsonObj = new JSONObject(
                  externalBgpAnnouncementsFileContents);

            JSONArray announcements = jsonObj
                  .getJSONArray(BfConsts.KEY_BGP_ANNOUNCEMENTS);

            ObjectMapper mapper = new ObjectMapper();

            for (int index = 0; index < announcements.length(); index++) {
               JSONObject announcement = announcements.getJSONObject(index);
               BgpAdvertisement bgpAdvertisement = mapper.readValue(
                     announcement.toString(), BgpAdvertisement.class);
               allCommunities.addAll(bgpAdvertisement.getCommunities());
               advertSet.add(bgpAdvertisement);
            }

         }
         catch (JSONException | IOException e) {
            throw new BatfishException("Problems parsing JSON in "
                  + externalBgpAnnouncementsFile.toString(), e);
         }

         populatePrecomputedBgpAdvertisements(advertSet, cpFactBins);
      }
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

   private Topology processTopologyFile(File topologyFilePath) {
      Topology topology = parseTopology(topologyFilePath);
      return topology;
   }

   private void query() {
      Map<String, String> allPredicateNames = _predicateInfo
            .getPredicateNames();
      Set<String> predicateNames = new TreeSet<String>();
      if (_settings.getQueryAll()) {
         predicateNames.addAll(allPredicateNames.keySet());
      }
      else {
         predicateNames.addAll(_settings.getPredicates());
      }
      checkQuery(_envSettings, predicateNames);
      printPredicates(_envSettings, predicateNames);
   }

   private Map<File, String> readConfigurationFiles(String testRigPath,
         String configsType) {
      _logger.infof("\n*** READING %s FILES ***\n", configsType);
      resetTimer();
      Map<File, String> configurationData = new TreeMap<File, String>();
      File configsPath = Paths.get(testRigPath, configsType).toFile();
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
         String fileTextRaw = CommonUtil.readFile(file.getAbsoluteFile());
         String fileText = fileTextRaw
               + ((fileTextRaw.length() != 0) ? "\n" : "");
         configurationData.put(file, fileText);
      }
      printElapsedTime();
      return configurationData;
   }

   private Map<String, String> readFacts(String factsDir, Set<String> factNames) {
      Map<String, String> inputFacts = new TreeMap<String, String>();
      for (String factName : factNames) {
         File factFile = Paths.get(factsDir, factName).toFile();
         String contents = CommonUtil.readFile(factFile);
         inputFacts.put(factName, contents);
      }
      return inputFacts;
   }

   private void removeBlocks(List<String> blockNames) {
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
      // lbFrontend.removeBlocks(qualifiedBlockNames);
   }

   public void resetTimer() {
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
            return filename.endsWith(".semantics") || filename.endsWith(".pl")
                  || filename.endsWith(locatorFilename)
                  || filename.endsWith(PREDICATE_INFO_FILENAME);
         }
      };
      if (logicSourceString.startsWith("onejar:")) {
         FileVisitor<Path> visitor = null;
         try {
            zip = new UrlZipExplorer(logicSourceURL);
            Path destinationDir = Files
                  .createTempDirectory("batfish_tmp_logic");
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

   public Answer run() {
      boolean action = false;
      Answer answer = new Answer();

      if (_settings.getQuery() || _settings.getPrintSemantics()
            || _settings.getDataPlane() || _settings.getWriteRoutes()
            || _settings.getWriteBgpAdvertisements()
            || _settings.getWriteIbgpNeighbors() || _settings.getHistory()
            || _settings.getNlsDataPlane() || _settings.getNlsTraffic()
            || _settings.getAnswer()) {
         Map<String, String> logicFiles = getSemanticsFiles();
         _predicateInfo = getPredicateInfo(logicFiles);
         // Print predicate semantics and quit if requested
         if (_settings.getPrintSemantics()) {
            printAllPredicateSemantics(_predicateInfo.getPredicateSemantics());
            return answer;
         }
      }

      if (_settings.getPrintSymmetricEdgePairs()) {
         printSymmetricEdgePairs();
         return answer;
      }

      if (_settings.getSynthesizeTopology()) {
         writeSynthesizedTopology();
         return answer;
      }

      if (_settings.getSynthesizeJsonTopology()) {
         writeJsonTopology();
         return answer;
      }

      if (_settings.getBuildPredicateInfo()) {
         buildPredicateInfo();
         return answer;
      }

      if (_settings.getHistogram()) {
         histogram(_settings.getTestRigPath());
         return answer;
      }

      if (_settings.getGenerateOspfTopologyPath() != null) {
         generateOspfConfigs(_settings.getGenerateOspfTopologyPath(),
               _settings.getSerializeIndependentPath());
         return answer;
      }

      if (_settings.getFlatten()) {
         String flattenSource = _settings.getTestRigPath();
         String flattenDestination = _settings.getFlattenDestination();
         flatten(flattenSource, flattenDestination);
         return answer;
      }

      if (_settings.getGenerateStubs()) {
         String inputRole = _settings.getGenerateStubsInputRole();
         String interfaceDescriptionRegex = _settings
               .getGenerateStubsInterfaceDescriptionRegex();
         int stubAs = _settings.getGenerateStubsRemoteAs();
         generateStubs(inputRole, stubAs, interfaceDescriptionRegex);
         return answer;
      }

      // if (_settings.getZ3()) {
      // Map<String, Configuration> configurations = loadConfigurations();
      // String dataPlanePath = _envSettings.getDataPlanePath();
      // if (dataPlanePath == null) {
      // throw new BatfishException("Missing path to data plane");
      // }
      // File dataPlanePathAsFile = new File(dataPlanePath);
      // genZ3(configurations, dataPlanePathAsFile);
      // return answer;
      // }
      //
      if (_settings.getAnonymize()) {
         anonymizeConfigurations();
         return answer;
      }

      // if (_settings.getRoleTransitQuery()) {
      // genRoleTransitQueries();
      // return answer;
      // }

      if (_settings.getSerializeVendor()) {
         String testRigPath = _settings.getTestRigPath();
         String outputPath = _settings.getSerializeVendorPath();
         answer.append(serializeVendorConfigs(testRigPath, outputPath));
         action = true;
      }

      if (_settings.getSerializeIndependent()) {
         String inputPath = _settings.getSerializeVendorPath();
         String outputPath = _settings.getSerializeIndependentPath();
         answer.append(serializeIndependentConfigs(inputPath, outputPath));
         action = true;
      }

      Map<String, StringBuilder> cpFactBins = null;
      if (_settings.getDumpControlPlaneFacts()) {
         boolean usePrecomputedFacts = _settings.getUsePrecomputedFacts();
         cpFactBins = new LinkedHashMap<String, StringBuilder>();
         initControlPlaneFactBins(cpFactBins, !usePrecomputedFacts);
         if (!usePrecomputedFacts) {
            computeControlPlaneFacts(cpFactBins, _settings.getDiffActive(),
                  _envSettings);
         }
         action = true;
      }

      if (_settings.getNlsDataPlane()) {
         answer.append(nlsDataPlane(_envSettings));
         action = true;
      }

      if (_settings.getUsePrecomputedFacts()) {
         populatePrecomputedFacts(_settings.getPrecomputedFactsPath(),
               cpFactBins);
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

      if (_settings.getAnswer()) {
         answer.append(answer());
         action = true;
      }

      if (_settings.getQuery()) {
         query();
         return answer;
      }

      if (_settings.getDataPlane()) {
         computeDataPlane();
         action = true;
      }

      /*
       * if (_settings.getNlsTraffic()) { nlsTraffic(); action = true; } if
       * (_settings.getHistory()) { getHistory(); action = true; }
       */
      if (_settings.getWriteRoutes()) {
         writeRoutes(_settings.getPrecomputedRoutesPath(), _envSettings);
         action = true;
      }

      if (_settings.getWriteBgpAdvertisements()) {
         writeBgpAdvertisements(
               _settings.getPrecomputedBgpAdvertisementsPath(), _envSettings);
         action = true;
      }

      if (_settings.getWriteIbgpNeighbors()) {
         writeIbgpNeighbors(_settings.getPrecomputedIbgpNeighborsPath());
         action = true;
      }

      if (!action) {
         throw new CleanBatfishException(
               "No task performed! Run with -help flag to see usage\n");
      }
      return answer;
   }

   private Answer runNls(String nlsInputFile, String nlsOutputDir) {
      Answer answer = new Answer();
      _logger.info("\n*** RUNNING NLS ***\n");
      resetTimer();
      File logicDir = retrieveLogicDir();
      String[] logicFilenames = getNlsLogicFilenames(logicDir);
      DefaultExecutor executor = new DefaultExecutor();
      ByteArrayOutputStream outStream = new ByteArrayOutputStream();
      ByteArrayOutputStream errStream = new ByteArrayOutputStream();
      executor.setStreamHandler(new PumpStreamHandler(outStream, errStream));
      executor.setExitValue(0);
      CommandLine cmdLine = new CommandLine(NLS_COMMAND);
      cmdLine.addArgument("-dir");
      cmdLine.addArgument(nlsOutputDir);
      cmdLine.addArgument("-rev-lookup");
      cmdLine.addArgument("-mcc");
      cmdLine.addArgument(nlsInputFile);
      cmdLine.addArguments(logicFilenames);
      StringBuilder cmdLineSb = new StringBuilder();
      cmdLineSb.append(NLS_COMMAND + " ");
      cmdLineSb.append(CommonUtil.joinStrings(" ", cmdLine.getArguments()));
      String cmdLineString = cmdLineSb.toString();
      boolean failure = false;
      _logger.info("Command line: " + cmdLineString + " \n");
      if (!_settings.getNlsDry()) {
         try {
            executor.execute(cmdLine);
         }
         catch (ExecuteException e) {
            failure = true;
         }
         catch (IOException e) {
            throw new BatfishException("Unknown error running nls", e);
         }
         finally {
            cleanupLogicDir();
            byte[] outRaw = outStream.toByteArray();
            byte[] errRaw = errStream.toByteArray();
            String out = null;
            String err = null;
            try {
               out = new String(outRaw, "UTF-8");
               err = new String(errRaw, "UTF-8");
            }
            catch (IOException e) {
               throw new BatfishException("Error reading nxnet output", e);
            }
            StringBuilder sb = new StringBuilder();
            if (failure) {
               sb.append("nls terminated abnormally:\n");
               sb.append("nls command line: " + cmdLine.toString() + "\n");
               sb.append(err);
               throw new BatfishException(sb.toString());
            }
            else {
               sb.append("nls output:\n");
               sb.append(out);
               _logger.debug(sb.toString());
               _logger.info("nls completed successfully\n");
            }
         }
      }
      else {
         _logger.warn("NLS dry run - not executing\n");
      }
      printElapsedTime();
      return answer;
   }

   private Answer serializeAwsVpcConfigs(String testRigPath, String outputPath) {
      Answer answer = new Answer();
      Map<File, String> configurationData = readConfigurationFiles(testRigPath,
            BfConsts.RELPATH_AWS_VPC_CONFIGS_DIR);
      AwsVpcConfiguration config = parseAwsVpcConfigurations(configurationData);

      if (!_settings.getNoOutput()) {
         _logger.info("\n*** SERIALIZING AWS CONFIGURATION STRUCTURES ***\n");
         resetTimer();
         new File(outputPath).mkdirs();
         Path currentOutputPath = Paths.get(outputPath,
               BfConsts.RELPATH_AWS_VPC_CONFIGS_FILE);
         _logger.debug("Serializing AWS VPCs to "
               + currentOutputPath.toString() + "\"...");
         serializeObject(config, currentOutputPath.toFile());
         _logger.debug("OK\n");
      }
      printElapsedTime();
      return answer;
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

   private Answer serializeIndependentConfigs(String vendorConfigPath,
         String outputPath) {
      Answer answer = new Answer();
      ConvertConfigurationAnswerElement answerElement = new ConvertConfigurationAnswerElement();
      answer.addAnswerElement(answerElement);
      Map<String, Configuration> configurations = getConfigurations(
            vendorConfigPath, answerElement);
      serializeIndependentConfigs(configurations, outputPath);
      serializeObject(answerElement, new File(_settings.getConvertAnswerPath()));
      return answer;
   }

   private Answer serializeNetworkConfigs(String testRigPath, String outputPath) {
      Answer answer = new Answer();
      Map<File, String> configurationData = readConfigurationFiles(testRigPath,
            BfConsts.RELPATH_CONFIGURATIONS_DIR);
      ParseVendorConfigurationAnswerElement answerElement = new ParseVendorConfigurationAnswerElement();
      answer.addAnswerElement(answerElement);
      Map<String, VendorConfiguration> vendorConfigurations = parseVendorConfigurations(
            configurationData, answerElement);
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
         // serialize warnings
         serializeObject(answerElement,
               new File(_settings.getParseAnswerPath()));
         printElapsedTime();

      }
      return answer;
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

   private Answer serializeVendorConfigs(String testRigPath, String outputPath) {
      Answer answer = new Answer();
      boolean configsFound = false;

      // look for network configs
      File networkConfigsPath = Paths.get(testRigPath,
            BfConsts.RELPATH_CONFIGURATIONS_DIR).toFile();
      if (networkConfigsPath.exists()) {
         answer.append(serializeNetworkConfigs(testRigPath, outputPath));
         configsFound = true;
      }

      // look for AWS VPC configs
      File awsVpcConfigsPath = Paths.get(testRigPath,
            BfConsts.RELPATH_AWS_VPC_CONFIGS_DIR).toFile();
      if (awsVpcConfigsPath.exists()) {
         answer.append(serializeAwsVpcConfigs(testRigPath, outputPath));
         configsFound = true;
      }

      if (!configsFound) {
         throw new BatfishException("No valid configurations found");
      }
      return answer;
   }

   public void setTerminatedWithException(boolean terminatedWithException) {
      _terminatedWithException = terminatedWithException;
   }

   public Synthesizer synthesizeDataPlane(
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
            if (!iface.isLoopback(node.getConfigurationFormat()) && iface.getActive()
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

   private void writeBgpAdvertisements(String writeAdvertsPath,
         EnvironmentSettings envSettings) {
      AdvertisementSet adverts = getAdvertisements(envSettings);
      File advertsFile = new File(writeAdvertsPath);
      File parentDir = advertsFile.getParentFile();
      if (parentDir != null) {
         parentDir.mkdirs();
      }
      _logger.info("Serializing: BGP advertisements => \"" + writeAdvertsPath
            + "\"...");
      serializeObject(adverts, advertsFile);
      _logger.info("OK\n");
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
      IbgpTopology topology = getIbgpNeighbors();
      File ibgpTopologyFile = new File(ibgpTopologyPath);
      File parentDir = ibgpTopologyFile.getParentFile();
      if (parentDir != null) {
         parentDir.mkdirs();
      }
      _logger.info("Serializing: IBGP neighbors => \"" + ibgpTopologyPath
            + "\"...");
      serializeObject(topology, ibgpTopologyFile);
      _logger.info("OK\n");
   }

   private void writeJsonAnswer(String jsonAnswer) {
      String jsonPath = _settings.getAnswerJsonPath();
      if (jsonPath != null) {
         CommonUtil.writeFile(jsonPath, jsonAnswer);
      }
   }

   private void writeJsonTopology() {
      try {
         Map<String, Configuration> configs = loadConfigurations();
         EdgeSet textEdges = synthesizeTopology(configs);
         JSONArray jEdges = new JSONArray();
         for (Edge textEdge : textEdges) {
            Configuration node1 = configs.get(textEdge.getNode1());
            Configuration node2 = configs.get(textEdge.getNode2());
            Interface interface1 = node1.getInterfaces()
                  .get(textEdge.getInt1());
            Interface interface2 = node2.getInterfaces()
                  .get(textEdge.getInt2());
            JSONObject jEdge = new JSONObject();
            jEdge.put("interface1", interface1.toJSONObject());
            jEdge.put("interface2", interface2.toJSONObject());
            jEdges.put(jEdge);
         }
         JSONObject master = new JSONObject();
         JSONObject topology = new JSONObject();
         topology.put("edges", jEdges);
         master.put("topology", topology);
         String text = master.toString(3);
         _logger.output(text);
      }
      catch (JSONException e) {
         throw new BatfishException("Failed to synthesize JSON topology", e);
      }
   }

   private void writeNlsInput(Set<String> outputSymbols,
         Map<String, String> inputFacts, String nlsInputFile,
         EnvironmentSettings envSettings) {
      checkComputeNlsRelations(envSettings);
      StringBuilder sb = new StringBuilder();
      sb.append("output_symbols([");
      List<String> outputSymbolsList = new ArrayList<String>();
      outputSymbolsList.addAll(outputSymbols);
      int numOutputSymbols = outputSymbols.size();
      for (int i = 0; i < numOutputSymbols; i++) {
         String symbol = outputSymbolsList.get(i);
         sb.append("'" + symbol + "'");
         if (i < numOutputSymbols - 1) {
            sb.append(",");
         }
         else {
            sb.append("]).\n");
         }
      }
      String lineDelimiter = Pattern.quote("|");
      for (Entry<String, String> e : inputFacts.entrySet()) {
         String predicateName = e.getKey();
         String contents = e.getValue();
         LBValueTypeList valueTypes = _predicateInfo
               .getPredicateValueTypes(predicateName);
         String[] lines = contents.split("\n");
         for (int i = 1; i < lines.length; i++) {
            sb.append("'" + predicateName + "'(");
            String line = lines[i];
            String[] parts = line.split(lineDelimiter);
            for (int j = 0; j < parts.length; j++) {
               String part = parts[j];
               boolean isNum;
               LBValueType currentValueType = valueTypes.get(j);
               switch (currentValueType) {
               case ENTITY_INDEX_BGP_ADVERTISEMENT:
               case ENTITY_INDEX_FLOW:
               case ENTITY_INDEX_INT:
               case ENTITY_INDEX_NETWORK:
               case ENTITY_INDEX_ROUTE:
               case ENTITY_REF_AUTONOMOUS_SYSTEM:
               case ENTITY_REF_INT:
               case ENTITY_REF_IP:
               case FLOAT:
               case INT:
                  isNum = true;
                  break;

               case ENTITY_REF_ADVERTISEMENT_TYPE:
               case ENTITY_REF_AS_PATH:
               case ENTITY_REF_FLOW_TAG:
               case ENTITY_REF_INTERFACE:
               case ENTITY_REF_NODE:
               case ENTITY_REF_ORIGIN_TYPE:
               case ENTITY_REF_POLICY_MAP:
               case ENTITY_REF_ROUTING_PROTOCOL:
               case ENTITY_REF_STRING:
               case STRING:
                  isNum = false;
                  break;

               default:
                  throw new BatfishException("invalid value type");
               }
               if (!isNum) {
                  sb.append("'" + part + "'");
               }
               else {
                  sb.append(part);
               }
               if (j < parts.length - 1) {
                  sb.append(",");
               }
               else {
                  sb.append(").\n");
               }
            }
         }
      }
      String output = sb.toString();
      CommonUtil.writeFile(nlsInputFile, output);
   }

   private void writeNlsPrecomputedRoutes(EnvironmentSettings envSettings) {
      String precomputedRoutesPath = envSettings.getPrecomputedRoutesPath();
      Map<String, StringBuilder> prFactBins = new HashMap<String, StringBuilder>();
      initControlPlaneFactBins(prFactBins, true);
      Set<String> prPredicates = new HashSet<String>();
      prPredicates.add(PRECOMPUTED_ROUTES_PREDICATE_NAME);
      prPredicates.add(NETWORKS_PREDICATE_NAME);
      prFactBins.keySet().retainAll(prPredicates);
      populatePrecomputedRoutes(
            Collections.singletonList(precomputedRoutesPath), prFactBins);
      dumpFacts(prFactBins, envSettings.getTrafficFactsDir());
   }

   private void writeRoutes(String writeRoutesPath,
         EnvironmentSettings envSettings) {
      RouteSet routes = getRoutes(envSettings);
      File routesFile = new File(writeRoutesPath);
      File parentDir = routesFile.getParentFile();
      if (parentDir != null) {
         parentDir.mkdirs();
      }
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
