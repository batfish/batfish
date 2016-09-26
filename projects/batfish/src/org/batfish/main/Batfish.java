package org.batfish.main;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.batfish.common.BatfishLogger;
import org.batfish.common.BfConsts;
import org.batfish.common.Answerer;
import org.batfish.common.BatfishException;
import org.batfish.common.CleanBatfishException;
import org.batfish.common.Pair;
import org.batfish.common.Warning;
import org.batfish.common.plugin.DataPlanePlugin;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.PluginClientType;
import org.batfish.common.plugin.PluginConsumer;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.common.util.CommonUtil;
import org.batfish.common.util.ZipUtility;
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
import org.batfish.datamodel.ForwardingAction;
import org.batfish.datamodel.GenericConfigObject;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.IpsecVpn;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.OspfArea;
import org.batfish.datamodel.OspfProcess;
import org.batfish.datamodel.PolicyMap;
import org.batfish.datamodel.PolicyMapAction;
import org.batfish.datamodel.PolicyMapClause;
import org.batfish.datamodel.PolicyMapMatchRouteFilterListLine;
import org.batfish.datamodel.Route;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixSpace;
import org.batfish.datamodel.RouteFilterLine;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.BgpAdvertisement.BgpAdvertisementType;
import org.batfish.datamodel.answers.AclLinesAnswerElement;
import org.batfish.datamodel.answers.Answer;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.answers.AnswerStatus;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.datamodel.answers.EnvironmentCreationAnswerElement;
import org.batfish.datamodel.answers.FlattenVendorConfigurationAnswerElement;
import org.batfish.datamodel.answers.NodAnswerElement;
import org.batfish.datamodel.answers.NodFirstUnsatAnswerElement;
import org.batfish.datamodel.answers.NodSatAnswerElement;
import org.batfish.datamodel.answers.ParseVendorConfigurationAnswerElement;
import org.batfish.datamodel.answers.ReportAnswerElement;
import org.batfish.datamodel.answers.StringAnswerElement;
import org.batfish.datamodel.answers.AclLinesAnswerElement.AclReachabilityEntry;
import org.batfish.datamodel.collections.AdvertisementSet;
import org.batfish.datamodel.collections.CommunitySet;
import org.batfish.datamodel.collections.EdgeSet;
import org.batfish.datamodel.collections.IbgpTopology;
import org.batfish.datamodel.collections.InterfaceSet;
import org.batfish.datamodel.collections.MultiSet;
import org.batfish.datamodel.collections.NamedStructureEquivalenceSet;
import org.batfish.datamodel.collections.NamedStructureEquivalenceSets;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.collections.NodeRoleMap;
import org.batfish.datamodel.collections.NodeSet;
import org.batfish.datamodel.collections.RoleSet;
import org.batfish.datamodel.collections.RouteSet;
import org.batfish.datamodel.collections.TreeMultiSet;
import org.batfish.datamodel.questions.Question;
import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.ParseTreePrettyPrinter;
import org.batfish.grammar.juniper.JuniperCombinedParser;
import org.batfish.grammar.juniper.JuniperFlattener;
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
import org.batfish.main.Settings.EnvironmentSettings;
import org.batfish.main.Settings.TestrigSettings;
import org.batfish.nls.NlsDataPlanePlugin;
import org.batfish.protocoldependency.DependencyDatabase;
import org.batfish.protocoldependency.DependentRoute;
import org.batfish.protocoldependency.PotentialExport;
import org.batfish.protocoldependency.ProtocolDependencyAnalysis;
import org.batfish.representation.VendorConfiguration;
import org.batfish.representation.aws_vpcs.AwsVpcConfiguration;
import org.batfish.representation.host.HostConfiguration;
import org.batfish.representation.iptables.IptablesVendorConfiguration;
import org.batfish.z3.AclLine;
import org.batfish.z3.AclReachabilityQuerySynthesizer;
import org.batfish.z3.BlacklistDstIpQuerySynthesizer;
import org.batfish.z3.CompositeNodJob;
import org.batfish.z3.EarliestMoreGeneralReachableLineQuerySynthesizer;
import org.batfish.z3.MultipathInconsistencyQuerySynthesizer;
import org.batfish.z3.NodFirstUnsatJob;
import org.batfish.z3.NodFirstUnsatResult;
import org.batfish.z3.NodJob;
import org.batfish.z3.NodJobResult;
import org.batfish.z3.NodSatJob;
import org.batfish.z3.NodSatResult;
import org.batfish.z3.QuerySynthesizer;
import org.batfish.z3.ReachEdgeQuerySynthesizer;
import org.batfish.z3.ReachabilityQuerySynthesizer;
import org.batfish.z3.Synthesizer;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This class encapsulates the main control logic for Batfish.
 */
public class Batfish extends PluginConsumer implements AutoCloseable, IBatfish {

   private static final String BASE_TESTRIG_TAG = "BASE";

   private static final String DELTA_TESTRIG_TAG = "DELTA";

   private static final String DIFFERENTIAL_FLOW_TAG = "DIFFERENTIAL";

   private static final String GEN_OSPF_STARTING_IP = "10.0.0.0";

   /**
    * Role name for generated stubs
    */
   private static final String STUB_ROLE = "generated_stubs";

   /**
    * The name of the [optional] topology file within a test-rig
    */
   private static final String TOPOLOGY_FILENAME = "topology.net";

   public static void applyBaseDir(TestrigSettings settings, Path containerDir,
         String testrig, String envName, String questionName) {
      Path testrigDir = containerDir.resolve(testrig);
      settings.setName(testrig);
      settings.setBasePath(testrigDir);
      if (containerDir != null) {
         EnvironmentSettings envSettings = settings.getEnvironmentSettings();
         settings.setSerializeIndependentPath(testrigDir
               .resolve(BfConsts.RELPATH_VENDOR_INDEPENDENT_CONFIG_DIR));
         settings.setSerializeVendorPath(
               testrigDir.resolve(BfConsts.RELPATH_VENDOR_SPECIFIC_CONFIG_DIR));
         settings.setTestRigPath(
               testrigDir.resolve(BfConsts.RELPATH_TEST_RIG_DIR));
         settings.setProtocolDependencyGraphPath(
               testrigDir.resolve(BfConsts.RELPATH_PROTOCOL_DEPENDENCY_GRAPH));
         settings.setProtocolDependencyGraphZipPath(testrigDir
               .resolve(BfConsts.RELPATH_PROTOCOL_DEPENDENCY_GRAPH_ZIP));
         settings.setParseAnswerPath(
               testrigDir.resolve(BfConsts.RELPATH_PARSE_ANSWER_PATH));
         settings.setConvertAnswerPath(
               testrigDir.resolve(BfConsts.RELPATH_CONVERT_ANSWER_PATH));
         if (envName != null) {
            envSettings.setName(envName);
            Path envPath = testrigDir.resolve(BfConsts.RELPATH_ENVIRONMENTS_DIR)
                  .resolve(envName);
            envSettings.setEnvironmentBasePath(envPath);
            envSettings.setControlPlaneFactsDir(
                  envPath.resolve(BfConsts.RELPATH_CONTROL_PLANE_FACTS_DIR));
            envSettings.setNlsDataPlaneInputFile(
                  envPath.resolve(BfConsts.RELPATH_NLS_INPUT_FILE));
            envSettings.setNlsDataPlaneOutputDir(
                  envPath.resolve(BfConsts.RELPATH_NLS_OUTPUT_DIR));
            envSettings.setDataPlanePath(
                  envPath.resolve(BfConsts.RELPATH_DATA_PLANE_DIR));
            envSettings.setZ3DataPlaneFile(
                  envPath.resolve(BfConsts.RELPATH_Z3_DATA_PLANE_FILE));
            Path envDirPath = envPath.resolve(BfConsts.RELPATH_ENV_DIR);
            envSettings.setEnvPath(envDirPath);
            envSettings.setNodeBlacklistPath(
                  envDirPath.resolve(BfConsts.RELPATH_NODE_BLACKLIST_FILE));
            envSettings.setInterfaceBlacklistPath(envDirPath
                  .resolve(BfConsts.RELPATH_INTERFACE_BLACKLIST_FILE));
            envSettings.setEdgeBlacklistPath(
                  envDirPath.resolve(BfConsts.RELPATH_EDGE_BLACKLIST_FILE));
            envSettings.setSerializedTopologyPath(
                  envDirPath.resolve(BfConsts.RELPATH_TOPOLOGY_FILE));
            envSettings.setDeltaConfigurationsDir(
                  envDirPath.resolve(BfConsts.RELPATH_CONFIGURATIONS_DIR));
            envSettings.setExternalBgpAnnouncementsPath(envDirPath
                  .resolve(BfConsts.RELPATH_EXTERNAL_BGP_ANNOUNCEMENTS));
            envSettings.setPrecomputedRoutesPath(
                  envPath.resolve(BfConsts.RELPATH_PRECOMPUTED_ROUTES));
            envSettings.setDeltaCompiledConfigurationsDir(envPath
                  .resolve(BfConsts.RELPATH_VENDOR_INDEPENDENT_CONFIG_DIR));
            envSettings.setDeltaVendorConfigurationsDir(
                  envPath.resolve(BfConsts.RELPATH_VENDOR_SPECIFIC_CONFIG_DIR));
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

   public static void initQuestionSettings(Settings settings) {
      String questionName = settings.getQuestionName();
      Path testrigDir = settings.getTestrigSettings().getBasePath();
      TestrigSettings testrigSettings = settings.getTestrigSettings();
      TestrigSettings deltaTestrigSettings = settings.getDeltaTestrigSettings();
      EnvironmentSettings deltaEnvSettings = deltaTestrigSettings
            .getEnvironmentSettings();
      String deltaEnvName = deltaEnvSettings.getName();
      boolean delta = settings.getDeltaTestrig() != null
            || deltaEnvName != null;
      if (questionName != null) {
         Path questionPath = testrigDir.resolve(BfConsts.RELPATH_QUESTIONS_DIR)
               .resolve(questionName);
         settings.setQuestionPath(
               questionPath.resolve(BfConsts.RELPATH_QUESTION_FILE));
         settings.setQuestionParametersPath(
               questionPath.resolve(BfConsts.RELPATH_QUESTION_PARAM_FILE));
         EnvironmentSettings envSettings = testrigSettings
               .getEnvironmentSettings();
         String envName = envSettings.getName();
         if (delta) {
            deltaEnvSettings.setTrafficFactsDir(questionPath.resolve(
                  Paths.get(BfConsts.RELPATH_DIFF, envName, deltaEnvName,
                        BfConsts.RELPATH_CONTROL_PLANE_FACTS_DIR)));
            deltaEnvSettings.setNlsTrafficInputFile(
                  questionPath.resolve(Paths.get(BfConsts.RELPATH_DIFF, envName,
                        deltaEnvName, BfConsts.RELPATH_NLS_INPUT_FILE)));
            deltaEnvSettings.setNlsTrafficOutputDir(
                  questionPath.resolve(Paths.get(BfConsts.RELPATH_DIFF, envName,
                        deltaEnvName, BfConsts.RELPATH_NLS_OUTPUT_DIR)));
            envSettings.setTrafficFactsDir(questionPath.resolve(
                  Paths.get(BfConsts.RELPATH_BASE, envName, deltaEnvName,
                        BfConsts.RELPATH_CONTROL_PLANE_FACTS_DIR)));
            envSettings.setNlsTrafficInputFile(
                  questionPath.resolve(Paths.get(BfConsts.RELPATH_BASE, envName,
                        deltaEnvName, BfConsts.RELPATH_NLS_INPUT_FILE)));
            envSettings.setNlsTrafficOutputDir(
                  questionPath.resolve(Paths.get(BfConsts.RELPATH_BASE, envName,
                        deltaEnvName, BfConsts.RELPATH_NLS_OUTPUT_DIR)));
         }
         else {
            envSettings.setTrafficFactsDir(
                  questionPath.resolve(Paths.get(BfConsts.RELPATH_BASE, envName,
                        BfConsts.RELPATH_CONTROL_PLANE_FACTS_DIR)));
            envSettings.setNlsTrafficInputFile(
                  questionPath.resolve(Paths.get(BfConsts.RELPATH_BASE, envName,
                        BfConsts.RELPATH_NLS_INPUT_FILE)));
            envSettings.setNlsTrafficOutputDir(
                  questionPath.resolve(Paths.get(BfConsts.RELPATH_BASE, envName,
                        BfConsts.RELPATH_NLS_OUTPUT_DIR)));
         }
      }
   }

   public static void initTestrigSettings(Settings settings) {
      String testrig = settings.getTestrig();
      String envName = settings.getEnvironmentName();
      String questionName = settings.getQuestionName();
      Path containerDir = settings.getContainerDir();
      if (testrig != null) {
         applyBaseDir(settings.getTestrigSettings(), containerDir, testrig,
               envName, questionName);
         String deltaTestrig = settings.getDeltaTestrig();
         String deltaEnvName = settings.getDeltaEnvironmentName();
         TestrigSettings deltaTestrigSettings = settings
               .getDeltaTestrigSettings();
         if (deltaTestrig != null && deltaEnvName == null) {
            deltaEnvName = envName;
            settings.setDeltaEnvironmentName(envName);
         }
         else if (deltaTestrig == null && deltaEnvName != null) {
            deltaTestrig = testrig;
            settings.setDeltaTestrig(testrig);
         }
         if (deltaTestrig != null) {
            applyBaseDir(deltaTestrigSettings, containerDir, deltaTestrig,
                  deltaEnvName, questionName);
         }
         if (settings.getDiffActive()) {
            settings
                  .setActiveTestrigSettings(settings.getDeltaTestrigSettings());
         }
         else {
            settings.setActiveTestrigSettings(settings.getTestrigSettings());
         }
         initQuestionSettings(settings);
      }
      else if (!settings.getBuildPredicateInfo() && containerDir != null) {
         throw new CleanBatfishException(
               "Must supply argument to -" + BfConsts.ARG_TESTRIG);
      }
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

   private final Map<String, BiFunction<Question, IBatfish, Answerer>> _answererCreators;

   private TestrigSettings _baseTestrigSettings;

   private final Map<TestrigSettings, Map<String, Configuration>> _cachedConfigurations;

   private DataPlanePlugin _dataPlanePlugin;

   private final Map<TestrigSettings, DataPlane> _dataPlanes;

   private TestrigSettings _deltaTestrigSettings;

   private BatfishLogger _logger;

   private NlsDataPlanePlugin _nls;

   private Settings _settings;

   // this variable is used communicate with parent thread on how the job
   // finished
   private boolean _terminatedWithException;

   private TestrigSettings _testrigSettings;

   private final List<TestrigSettings> _testrigSettingsStack;

   private long _timerCount;

   public Batfish(Settings settings) {
      super(settings.getSerializeToText(), settings.getPluginDirs());
      _settings = settings;
      _cachedConfigurations = new HashMap<>();
      _dataPlanes = new HashMap<>();
      _testrigSettings = settings.getActiveTestrigSettings();
      _baseTestrigSettings = settings.getTestrigSettings();
      _deltaTestrigSettings = settings.getDeltaTestrigSettings();
      _logger = _settings.getLogger();
      _terminatedWithException = false;
      _answererCreators = new HashMap<>();
      _testrigSettingsStack = new ArrayList<>();
   }

   private void anonymizeConfigurations() {
      // TODO Auto-generated method stub

   }

   private Answer answer() {
      Question question = parseQuestion();
      boolean dp = question.getDataPlane();
      boolean diff = question.getDifferential();
      boolean diffActive = _settings.getDiffActive() && !diff;
      _settings.setDiffActive(diffActive);
      _settings.setDiffQuestion(diff);
      initQuestionEnvironments(question, diff, diffActive, dp);
      AnswerElement answerElement = null;
      BatfishException exception = null;
      try {
         if (question.getDifferential() == true) {
            answerElement = Answerer.create(question, this).answerDiff();
         }
         else {
            answerElement = Answerer.create(question, this).answer();
         }
      }
      catch (Exception e) {
         exception = new BatfishException("Failed to answer question", e);
      }

      Answer answer = new Answer();
      answer.setQuestion(question);

      if (exception == null) {
         // success
         answer.setStatus(AnswerStatus.SUCCESS);
         answer.addAnswerElement(answerElement);
      }
      else {
         // failure
         answer.setStatus(AnswerStatus.FAILURE);
         answer.addAnswerElement(exception);
      }
      return answer;
   }

   @Override
   public AnswerElement answerAclReachability(String aclNameRegexStr,
         NamedStructureEquivalenceSets<?> aclEqSets) {
      AclLinesAnswerElement answerElement = new AclLinesAnswerElement();

      Pattern aclNameRegex;
      try {
         aclNameRegex = Pattern.compile(aclNameRegexStr);
      }
      catch (PatternSyntaxException e) {
         throw new BatfishException(
               "Supplied regex for nodes is not a valid java regex: \""
                     + aclNameRegexStr + "\"",
               e);
      }

      checkConfigurations();
      Map<String, Configuration> configurations = loadConfigurations();

      List<NodSatJob<AclLine>> jobs = new ArrayList<>();

      for (Entry<String, ?> e : aclEqSets.getSameNamedStructures().entrySet()) {
         String aclName = e.getKey();
         if (!aclNameRegex.matcher(aclName).matches()) {
            continue;
         }
         // skip juniper srx inbound filters, as they can't really contain
         // operator error
         if (aclName.contains("~ZONE_INTERFACE_FILTER~")
               || aclName.contains("~INBOUND_ZONE_FILTER~")) {
            continue;
         }
         SortedSet<?> s = (SortedSet<?>) e.getValue();
         for (Object o : s) {
            NamedStructureEquivalenceSet<?> aclEqSet = (NamedStructureEquivalenceSet<?>) o;
            String hostname = aclEqSet.getRepresentativeElement();
            SortedSet<String> eqClassNodes = aclEqSet.getNodes();
            answerElement.addEquivalenceClass(aclName, hostname, eqClassNodes);
            Configuration c = configurations.get(hostname);
            IpAccessList acl = c.getIpAccessLists().get(aclName);
            int numLines = acl.getLines().size();
            if (numLines == 0) {
               _logger.redflag("RED_FLAG: Acl \"" + hostname + ":" + aclName
                     + "\" contains no lines\n");
               continue;
            }
            AclReachabilityQuerySynthesizer query = new AclReachabilityQuerySynthesizer(
                  hostname, aclName, numLines);
            Synthesizer aclSynthesizer = synthesizeAcls(
                  Collections.singletonMap(hostname, c));
            NodSatJob<AclLine> job = new NodSatJob<>(_settings, aclSynthesizer,
                  query);
            jobs.add(job);
         }
      }

      Map<AclLine, Boolean> output = new TreeMap<>();
      computeNodSatOutput(jobs, output);

      // rearrange output for next step
      Map<String, Map<String, List<AclLine>>> arrangedAclLines = new TreeMap<>();
      for (Entry<AclLine, Boolean> e : output.entrySet()) {
         AclLine line = e.getKey();
         String hostname = line.getHostname();
         Map<String, List<AclLine>> byAclName = arrangedAclLines.get(hostname);
         if (byAclName == null) {
            byAclName = new TreeMap<>();
            arrangedAclLines.put(hostname, byAclName);
         }
         String aclName = line.getAclName();
         List<AclLine> aclLines = byAclName.get(aclName);
         if (aclLines == null) {
            aclLines = new ArrayList<>();
            byAclName.put(aclName, aclLines);
         }
         aclLines.add(line);
      }

      // now get earliest more general lines
      List<NodFirstUnsatJob<AclLine, Integer>> step2Jobs = new ArrayList<>();
      for (Entry<String, Map<String, List<AclLine>>> e : arrangedAclLines
            .entrySet()) {
         String hostname = e.getKey();
         Configuration c = configurations.get(hostname);
         Synthesizer aclSynthesizer = synthesizeAcls(
               Collections.singletonMap(hostname, c));
         Map<String, List<AclLine>> byAclName = e.getValue();
         for (Entry<String, List<AclLine>> e2 : byAclName.entrySet()) {
            String aclName = e2.getKey();
            IpAccessList ipAccessList = c.getIpAccessLists().get(aclName);
            List<AclLine> lines = e2.getValue();
            for (int i = 0; i < lines.size(); i++) {
               AclLine line = lines.get(i);
               boolean reachable = output.get(line);
               if (!reachable) {
                  List<AclLine> toCheck = new ArrayList<>();
                  for (int j = 0; j < i; j++) {
                     AclLine earlierLine = lines.get(j);
                     boolean earlierIsReachable = output.get(earlierLine);
                     if (earlierIsReachable) {
                        toCheck.add(earlierLine);
                     }
                  }
                  EarliestMoreGeneralReachableLineQuerySynthesizer query = new EarliestMoreGeneralReachableLineQuerySynthesizer(
                        line, toCheck, ipAccessList);
                  NodFirstUnsatJob<AclLine, Integer> job = new NodFirstUnsatJob<>(
                        _settings, aclSynthesizer, query);
                  step2Jobs.add(job);
               }
            }
         }
      }
      Map<AclLine, Integer> step2Output = new TreeMap<>();
      computeNodFirstUnsatOutput(step2Jobs, step2Output);
      for (AclLine line : output.keySet()) {
         Integer earliestMoreGeneralReachableLine = step2Output.get(line);
         line.setEarliestMoreGeneralReachableLine(
               earliestMoreGeneralReachableLine);
      }

      Set<Pair<String, String>> aclsWithUnreachableLines = new TreeSet<>();
      Set<Pair<String, String>> allAcls = new TreeSet<>();
      int numUnreachableLines = 0;
      int numLines = output.entrySet().size();
      for (Entry<AclLine, Boolean> e : output.entrySet()) {
         AclLine aclLine = e.getKey();
         boolean sat = e.getValue();
         String hostname = aclLine.getHostname();
         String aclName = aclLine.getAclName();
         Pair<String, String> qualifiedAclName = new Pair<>(hostname, aclName);
         allAcls.add(qualifiedAclName);
         if (!sat) {
            numUnreachableLines++;
            aclsWithUnreachableLines.add(qualifiedAclName);
         }
      }
      for (Entry<AclLine, Boolean> e : output.entrySet()) {
         AclLine aclLine = e.getKey();
         int index = aclLine.getLine();
         boolean sat = e.getValue();
         String hostname = aclLine.getHostname();
         String aclName = aclLine.getAclName();
         Pair<String, String> qualifiedAclName = new Pair<>(hostname, aclName);
         IpAccessList ipAccessList = configurations.get(hostname)
               .getIpAccessLists().get(aclName);
         IpAccessListLine ipAccessListLine = ipAccessList.getLines().get(index);
         AclReachabilityEntry line = new AclReachabilityEntry(index,
               ipAccessListLine.getName());
         if (aclsWithUnreachableLines.contains(qualifiedAclName)) {
            if (sat) {
               _logger.debugf("%s:%s:%d:'%s' is REACHABLE\n", hostname, aclName,
                     line.getIndex(), line.getName());
               answerElement.addReachableLine(hostname, ipAccessList, line);
            }
            else {
               _logger.debugf("%s:%s:%d:'%s' is UNREACHABLE\n\t%s\n", hostname,
                     aclName, line.getIndex(), line.getName(),
                     ipAccessListLine.toString());
               Integer earliestMoreGeneralLineIndex = aclLine
                     .getEarliestMoreGeneralReachableLine();
               if (earliestMoreGeneralLineIndex != null) {
                  IpAccessListLine earliestMoreGeneralLine = ipAccessList
                        .getLines().get(earliestMoreGeneralLineIndex);
                  line.setEarliestMoreGeneralLineIndex(
                        earliestMoreGeneralLineIndex);
                  line.setEarliestMoreGeneralLineName(
                        earliestMoreGeneralLine.getName());
                  if (!earliestMoreGeneralLine.getAction()
                        .equals(ipAccessListLine.getAction())) {
                     line.setDifferentAction(true);
                  }
               }
               answerElement.addUnreachableLine(hostname, ipAccessList, line);
               aclsWithUnreachableLines.add(qualifiedAclName);
            }
         }
         else {
            answerElement.addReachableLine(hostname, ipAccessList, line);
         }
      }
      for (Pair<String, String> qualfiedAcl : aclsWithUnreachableLines) {
         String hostname = qualfiedAcl.getFirst();
         String aclName = qualfiedAcl.getSecond();
         _logger.debugf("%s:%s has at least 1 unreachable line\n", hostname,
               aclName);
      }
      int numAclsWithUnreachableLines = aclsWithUnreachableLines.size();
      int numAcls = allAcls.size();
      double percentUnreachableAcls = 100d * numAclsWithUnreachableLines
            / numAcls;
      double percentUnreachableLines = 100d * numUnreachableLines / numLines;
      _logger.debugf("SUMMARY:\n");
      _logger.debugf("\t%d/%d (%.1f%%) acls have unreachable lines\n",
            numAclsWithUnreachableLines, numAcls, percentUnreachableAcls);
      _logger.debugf("\t%d/%d (%.1f%%) acl lines are unreachable\n",
            numUnreachableLines, numLines, percentUnreachableLines);

      return answerElement;
   }

   @Override
   public String answerProtocolDependencies() {
      checkConfigurations();
      Map<String, Configuration> configurations = loadConfigurations();

      ProtocolDependencyAnalysis analysis = new ProtocolDependencyAnalysis(
            configurations);
      analysis.printDependencies(_logger);
      analysis.writeGraphs(this, _logger);
      Path protocolDependencyGraphPath = _testrigSettings
            .getProtocolDependencyGraphPath();
      Path protocolDependencyGraphZipPath = _testrigSettings
            .getProtocolDependencyGraphZipPath();
      ZipUtility.zipFiles(protocolDependencyGraphPath.toString(),
            protocolDependencyGraphZipPath.toString());
      byte[] zipBytes;
      try {
         zipBytes = Files.readAllBytes(protocolDependencyGraphZipPath);
      }
      catch (IOException e) {
         throw new BatfishException("Could not read zip", e);
      }
      String zipBase64 = Base64.encodeBase64String(zipBytes);
      return zipBase64;

   }

   private void checkBaseDirExists() {
      Path baseDir = _testrigSettings.getBasePath();
      if (!Files.exists(baseDir)) {
         throw new CleanBatfishException("Test rig does not exist: \""
               + baseDir.getFileName().toString() + "\"");
      }
   }

   @Override
   public void checkConfigurations() {
      checkConfigurations(_testrigSettings);
   }

   public void checkConfigurations(TestrigSettings testrigSettings) {
      Path path = testrigSettings.getSerializeIndependentPath();
      if (!Files.exists(path)) {
         throw new CleanBatfishException(
               "Missing compiled vendor-independent configurations for this test-rig\n");
      }
      else if (CommonUtil.list(path).count() == 0) {
         throw new CleanBatfishException(
               "Nothing to do: Set of vendor-independent configurations for this test-rig is empty\n");
      }
   }

   @Override
   public void checkDataPlane() {
      checkDataPlane(_testrigSettings);
   }

   public void checkDataPlane(TestrigSettings testrigSettings) {
      EnvironmentSettings envSettings = testrigSettings
            .getEnvironmentSettings();
      if (!Files.exists(envSettings.getDataPlanePath())) {
         throw new CleanBatfishException(
               "Missing data plane for testrig: \"" + testrigSettings.getName()
                     + "\", environment: \"" + envSettings.getName() + "\"\n");
      }
   }

   @Override
   public void checkDataPlaneQuestionDependencies() {
      checkDataPlaneQuestionDependencies(_testrigSettings);
   }

   public void checkDataPlaneQuestionDependencies(
         TestrigSettings testrigSettings) {
      checkConfigurations(testrigSettings);
      checkDataPlane(testrigSettings);
   }

   public void checkDiffEnvironmentExists() {
      checkDiffEnvironmentSpecified();
      checkEnvironmentExists(_deltaTestrigSettings);
   }

   private void checkDiffEnvironmentSpecified() {
      if (_settings.getDeltaEnvironmentName() == null) {
         throw new CleanBatfishException(
               "No differential environment specified for differential question");
      }
   }

   public void checkDifferentialDataPlaneQuestionDependencies() {
      checkDiffEnvironmentSpecified();
      checkConfigurations();
      checkDataPlane(_baseTestrigSettings);
      checkDataPlane(_deltaTestrigSettings);
   }

   @Override
   public void checkEnvironmentExists() {
      checkEnvironmentExists(_testrigSettings);
   }

   public void checkEnvironmentExists(TestrigSettings testrigSettings) {
      checkBaseDirExists();
      EnvironmentSettings envSettings = testrigSettings
            .getEnvironmentSettings();
      if (!Files.exists(envSettings.getEnvPath())) {
         throw new CleanBatfishException("Environment not initialized: \""
               + envSettings.getName() + "\"");
      }
   }

   private void checkQuestionsDirExists() {
      checkBaseDirExists();
      Path questionsDir = _settings.getTestrigSettings().getBasePath()
            .resolve(BfConsts.RELPATH_QUESTIONS_DIR);
      if (!Files.exists(questionsDir)) {
         throw new CleanBatfishException("questions dir does not exist: \""
               + questionsDir.getFileName().toString() + "\"");
      }
   }

   @Override
   public void close() throws Exception {
   }

   private Answer compileEnvironmentConfigurations(
         TestrigSettings testrigSettings) {
      Answer answer = new Answer();
      EnvironmentSettings envSettings = testrigSettings
            .getEnvironmentSettings();
      Path deltaConfigurationsDir = envSettings.getDeltaConfigurationsDir();
      Path vendorConfigsDir = envSettings.getDeltaVendorConfigurationsDir();
      Path indepConfigsDir = envSettings.getDeltaCompiledConfigurationsDir();
      if (deltaConfigurationsDir != null) {
         if (Files.exists(deltaConfigurationsDir)) {
            answer.append(serializeVendorConfigs(envSettings.getEnvPath(),
                  vendorConfigsDir));
            answer.append(serializeIndependentConfigs(vendorConfigsDir,
                  indepConfigsDir));
         }
         return answer;
      }
      else {
         throw new BatfishException(
               "Delta configurations directory cannot be null");
      }
   }

   public Set<Flow> computeCompositeNodOutput(List<CompositeNodJob> jobs,
         NodAnswerElement answerElement) {
      _logger.info("\n*** EXECUTING COMPOSITE NOD JOBS ***\n");
      resetTimer();
      Set<Flow> flows = new TreeSet<>();
      BatfishJobExecutor<CompositeNodJob, NodAnswerElement, NodJobResult, Set<Flow>> executor = new BatfishJobExecutor<>(
            _settings, _logger);
      executor.executeJobs(jobs, flows, answerElement);
      printElapsedTime();
      return flows;
   }

   private Answer computeDataPlane(boolean differentialContext) {
      return _dataPlanePlugin.computeDataPlane(differentialContext);
   }

   @Override
   public InterfaceSet computeFlowSinks(
         Map<String, Configuration> configurations, boolean differentialContext,
         Topology topology) {
      InterfaceSet flowSinks = null;
      if (differentialContext) {
         pushBaseEnvironment();
         flowSinks = loadDataPlane().getFlowSinks();
         popEnvironment();
      }
      NodeSet blacklistNodes = getNodeBlacklist();
      if (blacklistNodes != null) {
         if (differentialContext) {
            flowSinks.removeNodes(blacklistNodes);
         }
      }
      Set<NodeInterfacePair> blacklistInterfaces = getInterfaceBlacklist();
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
            if (iface.getActive()
                  && !iface.isLoopback(node.getConfigurationFormat())
                  && !topologyInterfaces.contains(p)) {
               flowSinks.add(p);
            }
         }
      }
      return flowSinks;
   }

   @Override
   public Map<Ip, Set<String>> computeIpOwners(
         Map<String, Configuration> configurations) {
      Map<Ip, Set<String>> ipOwners = new HashMap<>();
      configurations.forEach((hostname, c) -> {
         for (Interface i : c.getInterfaces().values()) {
            if (i.getActive()) {
               i.getAllPrefixes().stream().map(p -> p.getAddress())
                     .forEach(ip -> {
                        Set<String> owners = ipOwners.get(ip);
                        if (owners == null) {
                           owners = new HashSet<>();
                           ipOwners.put(ip, owners);
                        }
                        owners.add(hostname);
                     });
            }
         }
      });
      return ipOwners;
   }

   public <Key, Result> void computeNodFirstUnsatOutput(
         List<NodFirstUnsatJob<Key, Result>> jobs, Map<Key, Result> output) {
      _logger.info("\n*** EXECUTING NOD UNSAT JOBS ***\n");
      resetTimer();
      BatfishJobExecutor<NodFirstUnsatJob<Key, Result>, NodFirstUnsatAnswerElement, NodFirstUnsatResult<Key, Result>, Map<Key, Result>> executor = new BatfishJobExecutor<>(
            _settings, _logger);
      executor.executeJobs(jobs, output, new NodFirstUnsatAnswerElement());
      printElapsedTime();
   }

   public Set<Flow> computeNodOutput(List<NodJob> jobs) {
      _logger.info("\n*** EXECUTING NOD JOBS ***\n");
      resetTimer();
      Set<Flow> flows = new TreeSet<>();
      BatfishJobExecutor<NodJob, NodAnswerElement, NodJobResult, Set<Flow>> executor = new BatfishJobExecutor<>(
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
      BatfishJobExecutor<NodSatJob<Key>, NodSatAnswerElement, NodSatResult<Key>, Map<Key, Boolean>> executor = new BatfishJobExecutor<>(
            _settings, _logger);
      executor.executeJobs(jobs, output, new NodSatAnswerElement());
      printElapsedTime();
   }

   @Override
   public Topology computeTopology(Map<String, Configuration> configurations) {
      resetTimer();
      Topology topology = computeTopology(_testrigSettings.getTestRigPath(),
            configurations);
      EdgeSet blacklistEdges = getEdgeBlacklist();
      if (blacklistEdges != null) {
         EdgeSet edges = topology.getEdges();
         edges.removeAll(blacklistEdges);
         if (blacklistEdges.size() > 0) {
         }
      }
      NodeSet blacklistNodes = getNodeBlacklist();
      if (blacklistNodes != null) {
         for (String blacklistNode : blacklistNodes) {
            topology.removeNode(blacklistNode);
         }
      }
      Set<NodeInterfacePair> blacklistInterfaces = getInterfaceBlacklist();
      if (blacklistInterfaces != null) {
         for (NodeInterfacePair blacklistInterface : blacklistInterfaces) {
            topology.removeInterface(blacklistInterface);
         }
      }
      Topology prunedTopology = new Topology(topology.getEdges());
      printElapsedTime();
      return prunedTopology;
   }

   private Topology computeTopology(Path testRigPath,
         Map<String, Configuration> configurations) {
      Path topologyFilePath = testRigPath.resolve(TOPOLOGY_FILENAME);
      Topology topology;
      // Get generated facts from topology file
      if (Files.exists(topologyFilePath)) {
         topology = processTopologyFile(topologyFilePath);
      }
      else {
         // guess adjacencies based on interface subnetworks
         _logger.info(
               "*** (GUESSING TOPOLOGY IN ABSENCE OF EXPLICIT FILE) ***\n");
         topology = synthesizeTopology(configurations);
      }
      return topology;
   }

   private Map<String, Configuration> convertConfigurations(
         Map<String, GenericConfigObject> vendorConfigurations,
         ConvertConfigurationAnswerElement answerElement) {
      _logger.info(
            "\n*** CONVERTING VENDOR CONFIGURATIONS TO INDEPENDENT FORMAT ***\n");
      resetTimer();
      Map<String, Configuration> configurations = new TreeMap<>();
      List<ConvertConfigurationJob> jobs = new ArrayList<>();
      for (String hostname : vendorConfigurations.keySet()) {
         Warnings warnings = new Warnings(_settings.getPedanticAsError(),
               _settings.getPedanticRecord()
                     && _logger.isActive(BatfishLogger.LEVEL_PEDANTIC),
               _settings.getRedFlagAsError(),
               _settings.getRedFlagRecord()
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
      BatfishJobExecutor<ConvertConfigurationJob, ConvertConfigurationAnswerElement, ConvertConfigurationResult, Map<String, Configuration>> executor = new BatfishJobExecutor<>(
            _settings, _logger);
      executor.executeJobs(jobs, configurations, answerElement);
      printElapsedTime();
      return configurations;
   }

   @Override
   public EnvironmentCreationAnswerElement createEnvironment(String newEnvName,
         NodeSet nodeBlacklist, Set<NodeInterfacePair> interfaceBlacklist,
         boolean dp) {
      EnvironmentCreationAnswerElement answerElement = new EnvironmentCreationAnswerElement();
      EnvironmentSettings envSettings = _testrigSettings
            .getEnvironmentSettings();
      String oldEnvName = envSettings.getName();
      if (oldEnvName.equals(newEnvName)) {
         throw new BatfishException(
               "Cannot create new environment: name of environment is same as that of old");
      }
      answerElement.setNewEnvironmentName(newEnvName);
      answerElement.setOldEnvironmentName(oldEnvName);
      Path oldEnvPath = envSettings.getEnvPath();
      applyBaseDir(_settings.getTestrigSettings(), _settings.getContainerDir(),
            _settings.getTestrig(), newEnvName, _settings.getQuestionName());
      EnvironmentSettings newEnvSettings = _testrigSettings
            .getEnvironmentSettings();
      Path newEnvPath = newEnvSettings.getEnvPath();
      newEnvPath.toFile().mkdirs();
      try {
         FileUtils.copyDirectory(oldEnvPath.toFile(), newEnvPath.toFile());
      }
      catch (IOException e) {
         throw new BatfishException(
               "Failed to intialize new environment from old environment", e);
      }

      // write node blacklist from question
      if (!nodeBlacklist.isEmpty()) {
         StringBuilder nodeBlacklistSb = new StringBuilder();
         for (String node : nodeBlacklist) {
            nodeBlacklistSb.append(node + "\n");
         }
         String nodeBlacklistStr = nodeBlacklistSb.toString();
         CommonUtil.writeFile(newEnvSettings.getNodeBlacklistPath(),
               nodeBlacklistStr);
      }
      // write interface blacklist from question
      if (!interfaceBlacklist.isEmpty()) {
         StringBuilder interfaceBlacklistSb = new StringBuilder();
         for (NodeInterfacePair pair : interfaceBlacklist) {
            interfaceBlacklistSb.append(
                  pair.getHostname() + ":" + pair.getInterface() + "\n");
         }
         String interfaceBlacklistStr = interfaceBlacklistSb.toString();
         CommonUtil.writeFile(newEnvSettings.getInterfaceBlacklistPath(),
               interfaceBlacklistStr);
      }

      if (dp && !dataPlaneDependenciesExist(_testrigSettings)) {
         computeDataPlane(true);
         if (_nls != null) {
            _nls.clearEntityTables();
         }
      }
      return answerElement;
   }

   private boolean dataPlaneDependenciesExist(TestrigSettings testrigSettings) {
      checkConfigurations();
      Path dpPath = testrigSettings.getEnvironmentSettings().getDataPlanePath();
      return Files.exists(dpPath);
   }

   public Map<String, Configuration> deserializeConfigurations(
         Path serializedConfigPath) {
      _logger.info(
            "\n*** DESERIALIZING VENDOR-INDEPENDENT CONFIGURATION STRUCTURES ***\n");
      resetTimer();
      if (!Files.exists(serializedConfigPath)) {
         throw new BatfishException(
               "Missing vendor-independent configs directory: '"
                     + serializedConfigPath.toString() + "'");
      }
      Map<Path, String> namesByPath = new TreeMap<>();
      try (DirectoryStream<Path> stream = Files
            .newDirectoryStream(serializedConfigPath)) {
         for (Path serializedConfig : stream) {
            String name = serializedConfig.getFileName().toString();
            namesByPath.put(serializedConfig, name);
         }
      }
      catch (IOException e) {
         throw new BatfishException(
               "Error reading vendor-independent configs directory: '"
                     + serializedConfigPath.toString() + "'",
               e);
      }
      Map<String, Configuration> configurations = deserializeObjects(
            namesByPath, Configuration.class);
      printElapsedTime();
      return configurations;
   }

   public Map<String, GenericConfigObject> deserializeVendorConfigurations(
         Path serializedVendorConfigPath) {
      _logger.info("\n*** DESERIALIZING VENDOR CONFIGURATION STRUCTURES ***\n");
      resetTimer();
      Map<Path, String> namesByPath = new TreeMap<>();
      try (DirectoryStream<Path> serializedConfigs = Files
            .newDirectoryStream(serializedVendorConfigPath)) {
         for (Path serializedConfig : serializedConfigs) {
            String name = serializedConfig.getFileName().toString();
            namesByPath.put(serializedConfig, name);
         }
      }
      catch (IOException e) {
         throw new BatfishException("Error reading vendor configs directory",
               e);
      }
      Map<String, GenericConfigObject> vendorConfigurations = deserializeObjects(
            namesByPath, GenericConfigObject.class);
      printElapsedTime();
      return vendorConfigurations;
   }

   private void disableUnusableVpnInterfaces(
         Map<String, Configuration> configurations) {
      initRemoteIpsecVpns(configurations);
      for (Configuration c : configurations.values()) {
         for (IpsecVpn vpn : c.getIpsecVpns().values()) {
            if (vpn.getRemoteIpsecVpn() == null) {
               String hostname = c.getHostname();
               Interface bindInterface = vpn.getBindInterface();
               if (bindInterface != null) {
                  bindInterface.setActive(false);
                  String bindInterfaceName = bindInterface.getName();
                  _logger.warnf(
                        "WARNING: Disabling unusable vpn interface because we cannot determine remote endpoint: \"%s:%s\"\n",
                        hostname, bindInterfaceName);
               }
            }
         }
      }
   }

   private boolean environmentExists(TestrigSettings testrigSettings) {
      checkBaseDirExists();
      return Files
            .exists(testrigSettings.getEnvironmentSettings().getEnvPath());
   }

   private void flatten(Path inputPath, Path outputPath) {
      Map<Path, String> configurationData = readConfigurationFiles(inputPath,
            BfConsts.RELPATH_CONFIGURATIONS_DIR);
      Map<Path, String> outputConfigurationData = new TreeMap<>();
      Path outputConfigDir = outputPath
            .resolve(BfConsts.RELPATH_CONFIGURATIONS_DIR);
      CommonUtil.createDirectories(outputConfigDir);
      _logger.info("\n*** FLATTENING TEST RIG ***\n");
      resetTimer();
      List<FlattenVendorConfigurationJob> jobs = new ArrayList<>();
      for (Path inputFile : configurationData.keySet()) {
         Warnings warnings = new Warnings(_settings.getPedanticAsError(),
               _settings.getPedanticRecord()
                     && _logger.isActive(BatfishLogger.LEVEL_PEDANTIC),
               _settings.getRedFlagAsError(),
               _settings.getRedFlagRecord()
                     && _logger.isActive(BatfishLogger.LEVEL_REDFLAG),
               _settings.getUnimplementedAsError(),
               _settings.getUnimplementedRecord()
                     && _logger.isActive(BatfishLogger.LEVEL_UNIMPLEMENTED),
               _settings.printParseTree());
         String fileText = configurationData.get(inputFile);
         String name = inputFile.getFileName().toString();
         Path outputFile = outputConfigDir.resolve(name);
         FlattenVendorConfigurationJob job = new FlattenVendorConfigurationJob(
               _settings, fileText, inputFile, outputFile, warnings);
         jobs.add(job);
      }
      BatfishJobExecutor<FlattenVendorConfigurationJob, FlattenVendorConfigurationAnswerElement, FlattenVendorConfigurationResult, Map<Path, String>> executor = new BatfishJobExecutor<>(
            _settings, _logger);
      // todo: do something with answer element
      executor.executeJobs(jobs, outputConfigurationData,
            new FlattenVendorConfigurationAnswerElement());
      printElapsedTime();
      for (Entry<Path, String> e : outputConfigurationData.entrySet()) {
         Path outputFile = e.getKey();
         String flatConfigText = e.getValue();
         String outputFileAsString = outputFile.toString();
         _logger.debug("Writing config to \"" + outputFileAsString + "\"...");
         CommonUtil.writeFile(outputFile, flatConfigText);
         _logger.debug("OK\n");
      }
      Path inputTopologyPath = inputPath.resolve(TOPOLOGY_FILENAME);
      Path outputTopologyPath = outputPath.resolve(TOPOLOGY_FILENAME);
      if (Files.isRegularFile(inputTopologyPath)) {
         String topologyFileText = CommonUtil.readFile(inputTopologyPath);
         CommonUtil.writeFile(outputTopologyPath, topologyFileText);
      }
   }

   private void generateOspfConfigs(Path topologyPath, Path outputPath) {
      Topology topology = parseTopology(topologyPath);
      Map<String, Configuration> configs = new TreeMap<>();
      NodeSet allNodes = new NodeSet();
      Map<NodeInterfacePair, Set<NodeInterfacePair>> interfaceMap = new HashMap<>();
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
            interfaceSet = new HashSet<>();
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
      Set<Set<NodeInterfacePair>> interfaceSets = new HashSet<>();
      interfaceSets.addAll(interfaceMap.values());
      for (Set<NodeInterfacePair> interfaceSet : interfaceSets) {
         int numInterfaces = interfaceSet.size();
         if (numInterfaces < 2) {
            throw new BatfishException(
                  "The following interface set contains less than two interfaces: "
                        + interfaceSet.toString());
         }
         int numHostBits = 0;
         for (int shiftedValue = numInterfaces
               - 1; shiftedValue != 0; shiftedValue >>= 1, numHostBits++) {
         }
         int subnetBits = 32 - numHostBits;
         int offset = 0;
         for (NodeInterfacePair currentPair : interfaceSet) {
            Ip ip = new Ip(currentStartingIpAsLong + offset);
            Prefix prefix = new Prefix(ip, subnetBits);
            String ifaceName = currentPair.getInterface();
            Interface iface = new Interface(ifaceName,
                  configs.get(currentPair.getHostname()));
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
         proc.setReferenceBandwidth(
               org.batfish.representation.cisco.OspfProcess.DEFAULT_REFERENCE_BANDWIDTH);
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
      Map<String, Configuration> stubConfigurations = new TreeMap<>();

      _logger.info("\n*** GENERATING STUBS ***\n");
      resetTimer();

      // load old node-roles to be updated at end
      RoleSet stubRoles = new RoleSet();
      stubRoles.add(STUB_ROLE);
      Path nodeRolesPath = _settings.getNodeRolesPath();
      _logger.info("Deserializing old node-roles mappings: \"" + nodeRolesPath
            + "\" ...");
      NodeRoleMap nodeRoles = deserializeObject(nodeRolesPath,
            NodeRoleMap.class);
      _logger.info("OK\n");

      // create origination policy common to all stubs
      String stubOriginationPolicyName = "~STUB_ORIGINATION_POLICY~";
      PolicyMap stubOriginationPolicy = new PolicyMap(
            stubOriginationPolicyName);
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

      Set<String> skipWarningNodes = new HashSet<>();

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
                     stub.getRouteFilterLists()
                           .put(stubOriginationRouteFilterListName, rf);
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
                  stubInterface.setPrefix(
                        new Prefix(neighborAddress, prefix.getPrefixLength()));
                  stubInterface.setActive(true);
                  stubInterface.setBandwidth(10E9d);

                  // create neighbor within bgp process
                  BgpNeighbor edgeNeighbor = new BgpNeighbor(prefix, stub);
                  edgeNeighbor.getOriginationPolicies()
                        .add(stubOriginationPolicy);
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
      _logger.info("Serializing updated node-roles mappings: \"" + nodeRolesPath
            + "\" ...");
      serializeObject(nodeRoles, nodeRolesPath);
      _logger.info("OK\n");
      printElapsedTime();

      // write stubs to disk
      serializeIndependentConfigs(stubConfigurations,
            _testrigSettings.getSerializeIndependentPath());
   }

   @Override
   public Map<String, BiFunction<Question, IBatfish, Answerer>> getAnswererCreators() {
      return _answererCreators;
   }

   public TestrigSettings getBaseTestrigSettings() {
      return _baseTestrigSettings;
   }

   public Map<String, Configuration> getConfigurations(
         Path serializedVendorConfigPath,
         ConvertConfigurationAnswerElement answerElement) {
      Map<String, GenericConfigObject> vendorConfigurations = deserializeVendorConfigurations(
            serializedVendorConfigPath);
      Map<String, Configuration> configurations = convertConfigurations(
            vendorConfigurations, answerElement);
      return configurations;
   }

   public Path getControlPlaneFactsDir() {
      return _testrigSettings.getEnvironmentSettings()
            .getControlPlaneFactsDir();
   }

   @Override
   public ConvertConfigurationAnswerElement getConvertConfigurationAnswerElement() {
      return deserializeObject(_testrigSettings.getConvertAnswerPath(),
            ConvertConfigurationAnswerElement.class);
   }

   public DataPlanePlugin getDataPlanePlugin() {
      return _dataPlanePlugin;
   }

   private Map<String, Configuration> getDeltaConfigurations() {
      EnvironmentSettings envSettings = _testrigSettings
            .getEnvironmentSettings();
      if (Files.exists(envSettings.getDeltaConfigurationsDir())) {
         if (Files.exists(envSettings.getDeltaCompiledConfigurationsDir())) {
            return deserializeConfigurations(
                  envSettings.getDeltaCompiledConfigurationsDir());
         }
         else {
            throw new BatfishException("Missing compiled delta configurations");
         }
      }
      else {
         return Collections.emptyMap();
      }
   }

   public TestrigSettings getDeltaTestrigSettings() {
      return _deltaTestrigSettings;
   }

   @Override
   public String getDifferentialFlowTag() {
      // return _settings.getQuestionName() + ":" +
      // _baseTestrigSettings.getName()
      // + ":" + _baseTestrigSettings.getEnvironmentSettings().getName()
      // + ":" + _deltaTestrigSettings.getName() + ":"
      // + _deltaTestrigSettings.getEnvironmentSettings().getName();
      return DIFFERENTIAL_FLOW_TAG;
   }

   public EdgeSet getEdgeBlacklist() {
      EdgeSet blacklistEdges = null;
      Path edgeBlacklistPath = _testrigSettings.getEnvironmentSettings()
            .getEdgeBlacklistPath();
      if (edgeBlacklistPath != null) {
         if (Files.exists(edgeBlacklistPath)) {
            Topology blacklistTopology = parseTopology(edgeBlacklistPath);
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

   public String getEnvironmentName() {
      return _testrigSettings.getEnvironmentSettings().getName();
   }

   @Override
   public String getFlowTag() {
      return getFlowTag(_testrigSettings);
   }

   public String getFlowTag(TestrigSettings testrigSettings) {
      // return _settings.getQuestionName() + ":" + testrigSettings.getName() +
      // ":"
      // + testrigSettings.getEnvironmentSettings().getName();
      if (testrigSettings == _deltaTestrigSettings) {
         return DELTA_TESTRIG_TAG;
      }
      else if (testrigSettings == _baseTestrigSettings) {
         return BASE_TESTRIG_TAG;
      }
      else {
         throw new BatfishException("Could not determine flow tag");
      }
   }

   @Override
   public FlowHistory getHistory() {
      FlowHistory flowHistory = new FlowHistory();
      if (_settings.getDiffQuestion()) {
         String tag = getDifferentialFlowTag();
         // String baseName = _baseTestrigSettings.getName() + ":"
         // + _baseTestrigSettings.getEnvironmentSettings().getName();
         String baseName = getFlowTag(_baseTestrigSettings);
         // String deltaName = _deltaTestrigSettings.getName() + ":"
         // + _deltaTestrigSettings.getEnvironmentSettings().getName();
         String deltaName = getFlowTag(_deltaTestrigSettings);
         pushBaseEnvironment();
         populateFlowHistory(flowHistory, baseName, tag);
         popEnvironment();
         pushDeltaEnvironment();
         populateFlowHistory(flowHistory, deltaName, tag);
         popEnvironment();
      }
      else {
         String tag = getFlowTag();
         // String name = testrigSettings.getName() + ":"
         // + testrigSettings.getEnvironmentSettings().getName();
         String envName = tag;
         populateFlowHistory(flowHistory, envName, tag);
      }
      _logger.debug(flowHistory.toString());
      return flowHistory;
   }

   private IbgpTopology getIbgpNeighbors() {
      return _dataPlanePlugin.getIbgpNeighbors();
   }

   public Set<NodeInterfacePair> getInterfaceBlacklist() {
      Set<NodeInterfacePair> blacklistInterfaces = null;
      Path interfaceBlacklistPath = _testrigSettings.getEnvironmentSettings()
            .getInterfaceBlacklistPath();
      if (interfaceBlacklistPath != null) {
         if (Files.exists(interfaceBlacklistPath)) {
            blacklistInterfaces = parseInterfaceBlacklist(
                  interfaceBlacklistPath);
         }
      }
      return blacklistInterfaces;
   }

   @Override
   public BatfishLogger getLogger() {
      return _logger;
   }

   public Path getNlsDataPlaneInputFile() {
      return _testrigSettings.getEnvironmentSettings()
            .getNlsDataPlaneInputFile();
   }

   public Path getNlsDataPlaneOutputDir() {
      return _testrigSettings.getEnvironmentSettings()
            .getNlsDataPlaneOutputDir();
   }

   public Path getNlsTrafficInputFile() {
      return _testrigSettings.getEnvironmentSettings().getNlsTrafficInputFile();
   }

   public Path getNlsTrafficOutputDir() {
      return _testrigSettings.getEnvironmentSettings().getNlsTrafficOutputDir();
   }

   public NodeSet getNodeBlacklist() {
      NodeSet blacklistNodes = null;
      Path nodeBlacklistPath = _testrigSettings.getEnvironmentSettings()
            .getNodeBlacklistPath();
      if (nodeBlacklistPath != null) {
         if (Files.exists(nodeBlacklistPath)) {
            blacklistNodes = parseNodeBlacklist(nodeBlacklistPath);
         }
      }
      return blacklistNodes;
   }

   public Path getPrecomputedRoutesPath() {
      return _testrigSettings.getEnvironmentSettings()
            .getPrecomputedRoutesPath();
   }

   public Path getSerializedTopologyPath() {
      return _testrigSettings.getEnvironmentSettings()
            .getSerializedTopologyPath();
   }

   public Settings getSettings() {
      return _settings;
   }

   private Set<Edge> getSymmetricEdgePairs(EdgeSet edges) {
      LinkedHashSet<Edge> consumedEdges = new LinkedHashSet<>();
      for (Edge edge : edges) {
         if (consumedEdges.contains(edge)) {
            continue;
         }
         Edge reverseEdge = new Edge(edge.getInterface2(),
               edge.getInterface1());
         consumedEdges.add(edge);
         consumedEdges.add(reverseEdge);
      }
      return consumedEdges;
   }

   public boolean getTerminatedWithException() {
      return _terminatedWithException;
   }

   public String getTestrigName() {
      return _testrigSettings.getName();
   }

   public TestrigSettings getTestrigSettings() {
      return _testrigSettings;
   }

   public Path getTrafficFactsDir() {
      return _testrigSettings.getEnvironmentSettings().getTrafficFactsDir();
   }

   @Override
   public PluginClientType getType() {
      return PluginClientType.BATFISH;
   }

   private void histogram(Path testRigPath) {
      Map<Path, String> configurationData = readConfigurationFiles(testRigPath,
            BfConsts.RELPATH_CONFIGURATIONS_DIR);
      // todo: either remove histogram function or do something userful with
      // answer
      Map<String, VendorConfiguration> vendorConfigurations = parseVendorConfigurations(
            configurationData, new ParseVendorConfigurationAnswerElement(),
            ConfigurationFormat.UNKNOWN);
      _logger.info("Building feature histogram...");
      MultiSet<String> histogram = new TreeMultiSet<>();
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

   @Override
   public void initBgpAdvertisements(
         Map<String, Configuration> configurations) {
      AdvertisementSet globalBgpAdvertisements = _dataPlanePlugin
            .getAdvertisements();
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
               originationNode.getOriginatedAdvertisements()
                     .add(bgpAdvertisement);
               originationNode.getOriginatedEbgpAdvertisements()
                     .add(bgpAdvertisement);
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
               originationNode.getOriginatedAdvertisements()
                     .add(bgpAdvertisement);
               originationNode.getOriginatedIbgpAdvertisements()
                     .add(bgpAdvertisement);
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
               receivingNode.getReceivedEbgpAdvertisements()
                     .add(bgpAdvertisement);
            }
            break;
         }

         case IBGP_RECEIVED: {
            String recevingNodeName = bgpAdvertisement.getDstNode();
            Configuration receivingNode = configurations.get(recevingNodeName);
            if (receivingNode != null) {
               receivingNode.getBgpAdvertisements().add(bgpAdvertisement);
               receivingNode.getReceivedAdvertisements().add(bgpAdvertisement);
               receivingNode.getReceivedIbgpAdvertisements()
                     .add(bgpAdvertisement);
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

   @Override
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
            Set<PotentialExport> bgpExports = database.getPotentialExports(name,
                  RoutingProtocol.BGP);
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

   private void initQuestionEnvironment(Question question, boolean dp,
         boolean differentialContext) {
      EnvironmentSettings envSettings = _testrigSettings
            .getEnvironmentSettings();
      if (!environmentExists(_testrigSettings)) {
         Path envPath = envSettings.getEnvPath();
         // create environment required folders
         CommonUtil.createDirectories(envPath);
      }
      if (dp && !dataPlaneDependenciesExist(_testrigSettings)) {
         computeDataPlane(differentialContext);
         if (_nls != null) {
            _nls.clearEntityTables();
         }
      }
   }

   private void initQuestionEnvironments(Question question, boolean diff,
         boolean diffActive, boolean dp) {
      if (diff || !diffActive) {
         pushBaseEnvironment();
         initQuestionEnvironment(question, dp, false);
         popEnvironment();
      }
      if (diff || diffActive) {
         pushDeltaEnvironment();
         initQuestionEnvironment(question, dp, true);
         popEnvironment();
      }
   }

   @Override
   public void initRemoteBgpNeighbors(Map<String, Configuration> configurations,
         Map<Ip, Set<String>> ipOwners) {
      Map<BgpNeighbor, Ip> remoteAddresses = new IdentityHashMap<>();
      Map<Ip, Set<BgpNeighbor>> localAddresses = new HashMap<>();
      for (Configuration node : configurations.values()) {
         String hostname = node.getHostname();
         BgpProcess proc = node.getBgpProcess();
         if (proc != null) {
            for (BgpNeighbor bgpNeighbor : proc.getNeighbors().values()) {
               bgpNeighbor.initCandidateRemoteBgpNeighbors();
               if (bgpNeighbor.getPrefix().getPrefixLength() < 32) {
                  throw new BatfishException(hostname
                        + ": Do not support dynamic bgp sessions at this time: "
                        + bgpNeighbor.getPrefix());
               }
               Ip remoteAddress = bgpNeighbor.getAddress();
               if (remoteAddress == null) {
                  throw new BatfishException(hostname
                        + ": Could not determine remote address of bgp neighbor: "
                        + bgpNeighbor);
               }
               Ip localAddress = bgpNeighbor.getLocalIp();
               if (localAddress == null || !ipOwners.containsKey(localAddress)
                     || !ipOwners.get(localAddress).contains(hostname)) {
                  continue;
               }
               remoteAddresses.put(bgpNeighbor, remoteAddress);
               Set<BgpNeighbor> localAddressOwners = localAddresses
                     .get(localAddress);
               if (localAddressOwners == null) {
                  localAddressOwners = Collections.newSetFromMap(
                        new IdentityHashMap<BgpNeighbor, Boolean>());
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
                  bgpNeighbor.getCandidateRemoteBgpNeighbors()
                        .add(remoteBgpNeighborCandidate);
                  bgpNeighbor.setRemoteBgpNeighbor(remoteBgpNeighborCandidate);
               }
            }
         }
      }
   }

   @Override
   public void initRemoteIpsecVpns(Map<String, Configuration> configurations) {
      Map<IpsecVpn, Ip> remoteAddresses = new HashMap<>();
      Map<Ip, Set<IpsecVpn>> externalAddresses = new HashMap<>();
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
                  vpnsUsingExternalAddress = new HashSet<>();
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
               if (reciprocalVpns != null
                     && reciprocalVpns.contains(ipsecVpn)) {
                  ipsecVpn.setRemoteIpsecVpn(remoteIpsecVpnCandidate);
                  ipsecVpn.getCandidateRemoteIpsecVpns()
                        .add(remoteIpsecVpnCandidate);
               }
            }
         }
      }
   }

   @Override
   public void initRoutes(Map<String, Configuration> configurations) {
      Set<Route> globalRoutes = _dataPlanePlugin.getRoutes();
      for (Configuration node : configurations.values()) {
         node.initRoutes();
      }
      for (Route route : globalRoutes) {
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

   @Override
   public Map<String, Configuration> loadConfigurations() {
      Map<String, Configuration> configurations = _cachedConfigurations
            .get(_testrigSettings);
      if (configurations == null) {
         configurations = deserializeConfigurations(
               _testrigSettings.getSerializeIndependentPath());
         _cachedConfigurations.put(_testrigSettings, configurations);
      }
      processNodeBlacklist(configurations);
      processInterfaceBlacklist(configurations);
      processDeltaConfigurations(configurations);
      disableUnusableVpnInterfaces(configurations);
      return configurations;
   }

   @Override
   public DataPlane loadDataPlane() {
      DataPlane dp = _dataPlanes.get(_testrigSettings);
      if (dp == null) {
         dp = deserializeObject(
               _testrigSettings.getEnvironmentSettings().getDataPlanePath(),
               DataPlane.class);
         _dataPlanes.put(_testrigSettings, dp);
      }
      return dp;
   }

   public Topology loadTopology() {
      Path topologyPath = _testrigSettings.getEnvironmentSettings()
            .getSerializedTopologyPath();
      _logger.info("Deserializing topology...");
      Topology topology = deserializeObject(topologyPath, Topology.class);
      _logger.info("OK\n");
      return topology;
   }

   @Override
   public AnswerElement multipath(HeaderSpace headerSpace) {
      Settings settings = getSettings();
      checkDataPlaneQuestionDependencies();
      String tag = getFlowTag(_testrigSettings);
      Map<String, Configuration> configurations = loadConfigurations();
      Set<Flow> flows = null;
      Synthesizer dataPlaneSynthesizer = synthesizeDataPlane();
      List<NodJob> jobs = new ArrayList<>();
      for (String node : configurations.keySet()) {
         MultipathInconsistencyQuerySynthesizer query = new MultipathInconsistencyQuerySynthesizer(
               node, headerSpace);
         NodeSet nodes = new NodeSet();
         nodes.add(node);
         NodJob job = new NodJob(settings, dataPlaneSynthesizer, query, nodes,
               tag);
         jobs.add(job);
      }

      flows = computeNodOutput(jobs);

      getDataPlanePlugin().processFlows(flows);

      AnswerElement answerElement = getHistory();
      return answerElement;
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

   public ParserRuleContext parse(BatfishCombinedParser<?, ?> parser,
         String filename) {
      _logger.info("Parsing: \"" + filename + "\"...");
      return parse(parser);
   }

   private AwsVpcConfiguration parseAwsVpcConfigurations(
         Map<Path, String> configurationData) {
      AwsVpcConfiguration config = new AwsVpcConfiguration();
      for (Path file : configurationData.keySet()) {

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
               throw new BatfishException(
                     "Problems parsing JSON in " + file.toString(), e);
            }
         }
      }
      return config;
   }

   private Set<NodeInterfacePair> parseInterfaceBlacklist(
         Path interfaceBlacklistPath) {
      Set<NodeInterfacePair> ifaces = new TreeSet<>();
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

   private NodeSet parseNodeBlacklist(Path nodeBlacklistPath) {
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

   private NodeRoleMap parseNodeRoles(Path testRigPath) {
      Path rolePath = testRigPath.resolve("node_roles");
      String roleFileText = CommonUtil.readFile(rolePath);
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
      Path questionPath = _settings.getQuestionPath();
      _logger.info("Reading question file: \"" + questionPath + "\"...");
      String questionText = CommonUtil.readFile(questionPath);
      _logger.info("OK\n");

      try {
         ObjectMapper mapper = new BatfishObjectMapper(getCurrentClassLoader());
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
      Path questionParametersPath = _settings.getQuestionParametersPath();
      if (!Files.exists(questionParametersPath)) {
         throw new BatfishException("Missing question parameters file: \""
               + questionParametersPath + "\"");
      }
      _logger.info("Reading question parameters file: \""
            + questionParametersPath + "\"...");
      String questionText = CommonUtil.readFile(questionParametersPath);
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

   private Topology parseTopology(Path topologyFilePath) {
      _logger.info("*** PARSING TOPOLOGY ***\n");
      resetTimer();
      String topologyFileText = CommonUtil.readFile(topologyFilePath);
      BatfishCombinedParser<?, ?> parser = null;
      TopologyExtractor extractor = null;
      _logger.info("Parsing: \"" + topologyFilePath.toAbsolutePath().toString()
            + "\" ...");
      if (topologyFileText.startsWith("autostart")) {
         parser = new GNS3TopologyCombinedParser(topologyFileText, _settings);
         extractor = new GNS3TopologyExtractor();
      }
      else if (topologyFileText
            .startsWith(BatfishTopologyCombinedParser.HEADER)) {
         parser = new BatfishTopologyCombinedParser(topologyFileText,
               _settings);
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
         Map<Path, String> configurationData,
         ParseVendorConfigurationAnswerElement answerElement,
         ConfigurationFormat configurationFormat) {
      _logger.info("\n*** PARSING VENDOR CONFIGURATION FILES ***\n");
      resetTimer();
      Map<String, VendorConfiguration> vendorConfigurations = new TreeMap<>();
      List<ParseVendorConfigurationJob> jobs = new ArrayList<>();
      for (Path currentFile : configurationData.keySet()) {
         Warnings warnings = new Warnings(_settings.getPedanticAsError(),
               _settings.getPedanticRecord()
                     && _logger.isActive(BatfishLogger.LEVEL_PEDANTIC),
               _settings.getRedFlagAsError(),
               _settings.getRedFlagRecord()
                     && _logger.isActive(BatfishLogger.LEVEL_REDFLAG),
               _settings.getUnimplementedAsError(),
               _settings.getUnimplementedRecord()
                     && _logger.isActive(BatfishLogger.LEVEL_UNIMPLEMENTED),
               _settings.printParseTree());
         String fileText = configurationData.get(currentFile);
         ParseVendorConfigurationJob job = new ParseVendorConfigurationJob(
               _settings, fileText, currentFile, warnings, configurationFormat);
         jobs.add(job);
      }
      BatfishJobExecutor<ParseVendorConfigurationJob, ParseVendorConfigurationAnswerElement, ParseVendorConfigurationResult, Map<String, VendorConfiguration>> executor = new BatfishJobExecutor<>(
            _settings, _logger);

      executor.executeJobs(jobs, vendorConfigurations, answerElement);
      printElapsedTime();
      return vendorConfigurations;
   }

   @Override
   public AnswerElement pathDiff(HeaderSpace headerSpace) {
      Settings settings = getSettings();
      checkDifferentialDataPlaneQuestionDependencies();
      String tag = getDifferentialFlowTag();

      // load base configurations and generate base data plane
      pushBaseEnvironment();
      Map<String, Configuration> baseConfigurations = loadConfigurations();
      Synthesizer baseDataPlaneSynthesizer = synthesizeDataPlane();
      popEnvironment();

      // load diff configurations and generate diff data plane
      pushDeltaEnvironment();
      Map<String, Configuration> diffConfigurations = loadConfigurations();
      Synthesizer diffDataPlaneSynthesizer = synthesizeDataPlane();
      popEnvironment();

      Set<String> commonNodes = new TreeSet<>();
      commonNodes.addAll(baseConfigurations.keySet());
      commonNodes.retainAll(diffConfigurations.keySet());

      pushDeltaEnvironment();
      NodeSet blacklistNodes = getNodeBlacklist();
      Set<NodeInterfacePair> blacklistInterfaces = getInterfaceBlacklist();
      EdgeSet blacklistEdges = getEdgeBlacklist();
      popEnvironment();

      BlacklistDstIpQuerySynthesizer blacklistQuery = new BlacklistDstIpQuerySynthesizer(
            null, blacklistNodes, blacklistInterfaces, blacklistEdges,
            baseConfigurations);

      // compute composite program and flows
      List<Synthesizer> commonEdgeSynthesizers = new ArrayList<>();
      commonEdgeSynthesizers.add(baseDataPlaneSynthesizer);
      commonEdgeSynthesizers.add(diffDataPlaneSynthesizer);
      commonEdgeSynthesizers.add(baseDataPlaneSynthesizer);

      List<CompositeNodJob> jobs = new ArrayList<>();

      // generate local edge reachability and black hole queries
      pushDeltaEnvironment();
      Topology diffTopology = loadTopology();
      popEnvironment();
      EdgeSet diffEdges = diffTopology.getEdges();
      for (Edge edge : diffEdges) {
         String ingressNode = edge.getNode1();
         ReachEdgeQuerySynthesizer reachQuery = new ReachEdgeQuerySynthesizer(
               ingressNode, edge, true, headerSpace);
         ReachEdgeQuerySynthesizer noReachQuery = new ReachEdgeQuerySynthesizer(
               ingressNode, edge, true, new HeaderSpace());
         noReachQuery.setNegate(true);
         List<QuerySynthesizer> queries = new ArrayList<>();
         queries.add(reachQuery);
         queries.add(noReachQuery);
         queries.add(blacklistQuery);
         NodeSet nodes = new NodeSet();
         nodes.add(ingressNode);
         CompositeNodJob job = new CompositeNodJob(settings,
               commonEdgeSynthesizers, queries, nodes, tag);
         jobs.add(job);
      }

      // we also need queries for nodes next to edges that are now missing,
      // in the case that those nodes still exist
      List<Synthesizer> missingEdgeSynthesizers = new ArrayList<>();
      missingEdgeSynthesizers.add(baseDataPlaneSynthesizer);
      missingEdgeSynthesizers.add(baseDataPlaneSynthesizer);
      pushBaseEnvironment();
      Topology baseTopology = loadTopology();
      popEnvironment();
      EdgeSet baseEdges = baseTopology.getEdges();
      EdgeSet missingEdges = new EdgeSet();
      missingEdges.addAll(baseEdges);
      missingEdges.removeAll(diffEdges);
      for (Edge missingEdge : missingEdges) {
         String ingressNode = missingEdge.getNode1();
         if (diffConfigurations.containsKey(ingressNode)) {
            ReachEdgeQuerySynthesizer reachQuery = new ReachEdgeQuerySynthesizer(
                  ingressNode, missingEdge, true, headerSpace);
            List<QuerySynthesizer> queries = new ArrayList<>();
            queries.add(reachQuery);
            queries.add(blacklistQuery);
            NodeSet nodes = new NodeSet();
            nodes.add(ingressNode);
            CompositeNodJob job = new CompositeNodJob(settings,
                  missingEdgeSynthesizers, queries, nodes, tag);
            jobs.add(job);
         }

      }

      // TODO: maybe do something with nod answer element
      Set<Flow> flows = computeCompositeNodOutput(jobs, new NodAnswerElement());
      pushBaseEnvironment();
      getDataPlanePlugin().processFlows(flows);
      popEnvironment();
      pushDeltaEnvironment();
      getDataPlanePlugin().processFlows(flows);
      popEnvironment();

      AnswerElement answerElement = getHistory();
      return answerElement;
   }

   @Override
   public void popEnvironment() {
      int lastIndex = _testrigSettingsStack.size() - 1;
      _testrigSettings = _testrigSettingsStack.get(lastIndex);
      _testrigSettingsStack.remove(lastIndex);
   }

   private void populateFlowHistory(FlowHistory flowHistory,
         String environmentName, String tag) {
      List<Flow> flows = _dataPlanePlugin.getHistoryFlows();
      List<FlowTrace> flowTraces = _dataPlanePlugin.getHistoryFlowTraces();
      int numEntries = flows.size();
      for (int i = 0; i < numEntries; i++) {
         Flow flow = flows.get(i);
         if (flow.getTag().equals(tag)) {
            FlowTrace flowTrace = flowTraces.get(i);
            flowHistory.addFlowTrace(flow, environmentName, flowTrace);
         }
      }
   }

   @Override
   public void printElapsedTime() {
      double seconds = getElapsedTime(_timerCount);
      _logger.info("Time taken for this task: " + seconds + " seconds\n");
   }

   private void printSymmetricEdgePairs() {
      Map<String, Configuration> configs = loadConfigurations();
      EdgeSet edges = synthesizeTopology(configs).getEdges();
      Set<Edge> symmetricEdgePairs = getSymmetricEdgePairs(edges);
      List<Edge> edgeList = new ArrayList<>();
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
         Map<String, Configuration> configurations) {
      Map<String, Configuration> deltaConfigurations = getDeltaConfigurations();
      configurations.putAll(deltaConfigurations);
      // TODO: deal with topological changes
   }

   @Override
   public AdvertisementSet processExternalBgpAnnouncements(
         Map<String, Configuration> configurations) {
      AdvertisementSet advertSet = new AdvertisementSet();
      Path externalBgpAnnouncementsPath = _testrigSettings
            .getEnvironmentSettings().getExternalBgpAnnouncementsPath();
      if (Files.exists(externalBgpAnnouncementsPath)) {
         String externalBgpAnnouncementsFileContents = CommonUtil
               .readFile(externalBgpAnnouncementsPath);
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
               JSONObject announcement = new JSONObject();
               announcement.put("@id", index);
               JSONObject announcementSrc = announcements.getJSONObject(index);
               for (Iterator<?> i = announcementSrc.keys(); i.hasNext();) {
                  String key = (String) i.next();
                  if (!key.equals("@id")) {
                     announcement.put(key, announcementSrc.get(key));
                  }
               }
               BgpAdvertisement bgpAdvertisement = mapper.readValue(
                     announcement.toString(), BgpAdvertisement.class);
               advertSet.add(bgpAdvertisement);
            }

         }
         catch (JSONException | IOException e) {
            throw new BatfishException("Problems parsing JSON in "
                  + externalBgpAnnouncementsPath.toString(), e);
         }
      }
      return advertSet;
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
    * @param allCommunities
    */
   public AdvertisementSet processExternalBgpAnnouncements(
         Map<String, Configuration> configurations,
         CommunitySet allCommunities) {
      AdvertisementSet advertSet = new AdvertisementSet();
      Path externalBgpAnnouncementsPath = _testrigSettings
            .getEnvironmentSettings().getExternalBgpAnnouncementsPath();
      if (Files.exists(externalBgpAnnouncementsPath)) {
         String externalBgpAnnouncementsFileContents = CommonUtil
               .readFile(externalBgpAnnouncementsPath);
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
               JSONObject announcement = new JSONObject();
               announcement.put("@id", index);
               JSONObject announcementSrc = announcements.getJSONObject(index);
               for (Iterator<?> i = announcementSrc.keys(); i.hasNext();) {
                  String key = (String) i.next();
                  if (!key.equals("@id")) {
                     announcement.put(key, announcementSrc.get(key));
                  }
               }
               BgpAdvertisement bgpAdvertisement = mapper.readValue(
                     announcement.toString(), BgpAdvertisement.class);
               allCommunities.addAll(bgpAdvertisement.getCommunities());
               advertSet.add(bgpAdvertisement);
            }

         }
         catch (JSONException | IOException e) {
            throw new BatfishException("Problems parsing JSON in "
                  + externalBgpAnnouncementsPath.toString(), e);
         }
      }
      return advertSet;
   }

   @Override
   public void processFlows(Set<Flow> flows) {
      _dataPlanePlugin.processFlows(flows);
   }

   private void processInterfaceBlacklist(
         Map<String, Configuration> configurations) {
      Set<NodeInterfacePair> blacklistInterfaces = getInterfaceBlacklist();
      if (blacklistInterfaces != null) {
         for (NodeInterfacePair p : blacklistInterfaces) {
            String hostname = p.getHostname();
            String iface = p.getInterface();
            Configuration node = configurations.get(hostname);
            node.getInterfaces().get(iface).setActive(false);
         }
      }
   }

   private void processNodeBlacklist(
         Map<String, Configuration> configurations) {
      NodeSet blacklistNodes = getNodeBlacklist();
      if (blacklistNodes != null) {
         for (String hostname : blacklistNodes) {
            configurations.remove(hostname);
         }
      }
   }

   private Topology processTopologyFile(Path topologyFilePath) {
      Topology topology = parseTopology(topologyFilePath);
      return topology;
   }

   @Override
   public void pushBaseEnvironment() {
      _testrigSettingsStack.add(_testrigSettings);
      _testrigSettings = _baseTestrigSettings;
   }

   @Override
   public void pushDeltaEnvironment() {
      _testrigSettingsStack.add(_testrigSettings);
      _testrigSettings = _deltaTestrigSettings;
   }

   private Map<Path, String> readConfigurationFiles(Path testRigPath,
         String configsType) {
      _logger.infof("\n*** READING %s FILES ***\n", configsType);
      resetTimer();
      Map<Path, String> configurationData = new TreeMap<>();
      Path configsPath = testRigPath.resolve(configsType);
      Path[] configFilePaths = CommonUtil.list(configsPath)
            .filter(path -> !path.getFileName().toString().startsWith("."))
            .collect(Collectors.toList()).toArray(new Path[] {});
      Arrays.sort(configFilePaths);
      for (Path file : configFilePaths) {
         _logger.debug("Reading: \"" + file.toString() + "\"\n");
         String fileTextRaw = CommonUtil.readFile(file.toAbsolutePath());
         String fileText = fileTextRaw
               + ((fileTextRaw.length() != 0) ? "\n" : "");
         configurationData.put(file, fileText);
      }
      printElapsedTime();
      return configurationData;
   }

   @Override
   public AnswerElement reducedReachability(HeaderSpace headerSpace) {
      Settings settings = getSettings();
      checkDifferentialDataPlaneQuestionDependencies();
      String tag = getDifferentialFlowTag();

      // load base configurations and generate base data plane
      pushBaseEnvironment();
      Map<String, Configuration> baseConfigurations = loadConfigurations();
      Synthesizer baseDataPlaneSynthesizer = synthesizeDataPlane();
      popEnvironment();

      // load diff configurations and generate diff data plane
      pushDeltaEnvironment();
      Map<String, Configuration> diffConfigurations = loadConfigurations();
      Synthesizer diffDataPlaneSynthesizer = synthesizeDataPlane();
      popEnvironment();

      Set<String> commonNodes = new TreeSet<>();
      commonNodes.addAll(baseConfigurations.keySet());
      commonNodes.retainAll(diffConfigurations.keySet());

      pushDeltaEnvironment();
      NodeSet blacklistNodes = getNodeBlacklist();
      Set<NodeInterfacePair> blacklistInterfaces = getInterfaceBlacklist();
      EdgeSet blacklistEdges = getEdgeBlacklist();
      popEnvironment();

      BlacklistDstIpQuerySynthesizer blacklistQuery = new BlacklistDstIpQuerySynthesizer(
            null, blacklistNodes, blacklistInterfaces, blacklistEdges,
            baseConfigurations);

      // compute composite program and flows
      List<Synthesizer> synthesizers = new ArrayList<>();
      synthesizers.add(baseDataPlaneSynthesizer);
      synthesizers.add(diffDataPlaneSynthesizer);
      synthesizers.add(baseDataPlaneSynthesizer);

      List<CompositeNodJob> jobs = new ArrayList<>();

      // generate base reachability and diff blackhole and blacklist queries
      for (String node : commonNodes) {
         ReachabilityQuerySynthesizer acceptQuery = new ReachabilityQuerySynthesizer(
               Collections.singleton(ForwardingAction.ACCEPT), headerSpace,
               Collections.<String> emptySet(), Collections.singleton(node));
         ReachabilityQuerySynthesizer notAcceptQuery = new ReachabilityQuerySynthesizer(
               Collections.singleton(ForwardingAction.ACCEPT),
               new HeaderSpace(), Collections.<String> emptySet(),
               Collections.singleton(node));
         notAcceptQuery.setNegate(true);
         NodeSet nodes = new NodeSet();
         nodes.add(node);
         List<QuerySynthesizer> queries = new ArrayList<>();
         queries.add(acceptQuery);
         queries.add(notAcceptQuery);
         queries.add(blacklistQuery);
         CompositeNodJob job = new CompositeNodJob(settings, synthesizers,
               queries, nodes, tag);
         jobs.add(job);
      }

      // TODO: maybe do something with nod answer element
      Set<Flow> flows = computeCompositeNodOutput(jobs, new NodAnswerElement());
      pushBaseEnvironment();
      getDataPlanePlugin().processFlows(flows);
      popEnvironment();
      pushDeltaEnvironment();
      getDataPlanePlugin().processFlows(flows);
      popEnvironment();

      AnswerElement answerElement = getHistory();
      return answerElement;
   }

   @Override
   public void registerAnswerer(String questionClassName,
         BiFunction<Question, IBatfish, Answerer> answererCreator) {
      _answererCreators.put(questionClassName, answererCreator);
   }

   private AnswerElement report() {
      ReportAnswerElement answerElement = new ReportAnswerElement();
      checkQuestionsDirExists();
      Path questionsDir = _settings.getTestrigSettings().getBasePath()
            .resolve(BfConsts.RELPATH_QUESTIONS_DIR);
      ConcurrentMap<Path, String> answers = new ConcurrentHashMap<>();
      try {
         Files.newDirectoryStream(questionsDir)
               .forEach(questionDirPath -> answers.put(
                     questionDirPath.resolve(BfConsts.RELPATH_ANSWER_JSON),
                     !questionDirPath.getFileName().startsWith(".")
                           && Files.exists(questionDirPath
                                 .resolve(BfConsts.RELPATH_ANSWER_JSON))
                                       ? CommonUtil
                                             .readFile(questionDirPath.resolve(
                                                   BfConsts.RELPATH_ANSWER_JSON))
                                       : ""));
      }
      catch (IOException e1) {
         throw new BatfishException("Could not create directory stream for '"
               + questionsDir.toString() + "'", e1);
      }
      ObjectMapper mapper = new BatfishObjectMapper();
      for (Entry<Path, String> entry : answers.entrySet()) {
         Path answerPath = entry.getKey();
         String answerText = entry.getValue();
         if (!answerText.equals("")) {
            try {
               answerElement.getJsonAnswers().add(mapper.readTree(answerText));
            }
            catch (IOException e) {
               throw new BatfishException("Error mapping JSON content of '"
                     + answerPath.toString() + "' to object", e);
            }
         }
      }
      return answerElement;
   }

   @Override
   public void resetTimer() {
      _timerCount = System.currentTimeMillis();
   }

   public Answer run() {
      loadPlugins();
      if (_dataPlanePlugin == null) {
         _nls = new NlsDataPlanePlugin();
         _nls.initialize(this);
         _dataPlanePlugin = _nls;
      }

      boolean action = false;
      Answer answer = new Answer();

      if (_settings.getPrintSemantics()) {
         _nls.printAllPredicateSemantics();
         return answer;
      }

      if (_settings.getPrintSymmetricEdgePairs()) {
         printSymmetricEdgePairs();
         return answer;
      }

      if (_settings.getReport()) {
         answer.addAnswerElement(report());
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
         _nls.buildPredicateInfo();
         return answer;
      }

      if (_settings.getHistogram()) {
         histogram(_testrigSettings.getTestRigPath());
         return answer;
      }

      if (_settings.getGenerateOspfTopologyPath() != null) {
         generateOspfConfigs(_settings.getGenerateOspfTopologyPath(),
               _testrigSettings.getSerializeIndependentPath());
         return answer;
      }

      if (_settings.getFlatten()) {
         Path flattenSource = _testrigSettings.getTestRigPath();
         Path flattenDestination = _settings.getFlattenDestination();
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
         Path testRigPath = _testrigSettings.getTestRigPath();
         Path outputPath = _testrigSettings.getSerializeVendorPath();
         answer.append(serializeVendorConfigs(testRigPath, outputPath));
         action = true;
      }

      if (_settings.getSerializeIndependent()) {
         Path inputPath = _testrigSettings.getSerializeVendorPath();
         Path outputPath = _testrigSettings.getSerializeIndependentPath();
         answer.append(serializeIndependentConfigs(inputPath, outputPath));
         action = true;
      }

      if (_settings.getCompileEnvironment()) {
         answer.append(compileEnvironmentConfigurations(_testrigSettings));
         action = true;
      }

      if (_settings.getAnswer()) {
         answer.append(answer());
         action = true;
      }

      if (_settings.getQuery()) {
         _nls.query();
         return answer;
      }

      if (_settings.getDataPlane()) {
         answer.append(computeDataPlane(_settings.getDiffActive()));
         action = true;
      }

      if (_settings.getWriteRoutes()) {
         writeRoutes(_settings.getPrecomputedRoutesPath());
         action = true;
      }

      if (_settings.getWriteBgpAdvertisements()) {
         writeBgpAdvertisements(
               _settings.getPrecomputedBgpAdvertisementsPath());
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

   private Answer serializeAwsVpcConfigs(Path testRigPath, Path outputPath) {
      Answer answer = new Answer();
      Map<Path, String> configurationData = readConfigurationFiles(testRigPath,
            BfConsts.RELPATH_AWS_VPC_CONFIGS_DIR);
      AwsVpcConfiguration config = parseAwsVpcConfigurations(configurationData);

      if (!_settings.getNoOutput()) {
         _logger.info("\n*** SERIALIZING AWS CONFIGURATION STRUCTURES ***\n");
         resetTimer();
         outputPath.toFile().mkdirs();
         Path currentOutputPath = outputPath
               .resolve(BfConsts.RELPATH_AWS_VPC_CONFIGS_FILE);
         _logger.debug("Serializing AWS VPCs to " + currentOutputPath.toString()
               + "\"...");
         serializeObject(config, currentOutputPath);
         _logger.debug("OK\n");
      }
      printElapsedTime();
      return answer;
   }

   private Answer serializeHostConfigs(Path testRigPath, Path outputPath) {
      Answer answer = new Answer();
      Map<Path, String> configurationData = readConfigurationFiles(testRigPath,
            BfConsts.RELPATH_HOST_CONFIGS_DIR);
      ParseVendorConfigurationAnswerElement answerElement = new ParseVendorConfigurationAnswerElement();
      answer.addAnswerElement(answerElement);

      // read the host files
      Map<String, VendorConfiguration> hostConfigurations = parseVendorConfigurations(
            configurationData, answerElement, ConfigurationFormat.HOST);
      if (hostConfigurations == null) {
         throw new BatfishException("Exiting due to parser errors");
      }

      // assign roles if that file exists
      Path nodeRolesPath = _settings.getNodeRolesPath();
      if (nodeRolesPath != null) {
         NodeRoleMap nodeRoles = parseNodeRoles(testRigPath);
         for (Entry<String, RoleSet> nodeRolesEntry : nodeRoles.entrySet()) {
            String hostname = nodeRolesEntry.getKey();
            VendorConfiguration config = hostConfigurations.get(hostname);
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
            serializeObject(nodeRoles, nodeRolesPath);
            _logger.info("OK\n");
         }
      }

      // read and associate iptables files for specified hosts
      Map<Path, String> iptablesData = new TreeMap<>();
      for (VendorConfiguration vc : hostConfigurations.values()) {
         HostConfiguration hostConfig = (HostConfiguration) vc;
         if (hostConfig.getIptablesFile() != null) {
            Path path = Paths.get(testRigPath.toString(),
                  hostConfig.getIptablesFile());

            // ensure that the iptables file is not taking us outside of the
            // testrig
            try {
               if (testRigPath.toFile().getCanonicalPath()
                     .contains(path.toFile().getCanonicalPath())) {
                  throw new BatfishException(
                        "Iptables file " + hostConfig.getIptablesFile()
                              + " for host " + hostConfig.getHostname()
                              + "is not contained within the testrig");
               }
            }
            catch (IOException e) {
               throw new BatfishException("Could not get canonical path", e);
            }

            String fileText = CommonUtil.readFile(path);
            iptablesData.put(path, fileText);
         }
      }

      Map<String, VendorConfiguration> iptablesConfigurations = parseVendorConfigurations(
            iptablesData, answerElement, ConfigurationFormat.IPTABLES);
      for (VendorConfiguration vc : hostConfigurations.values()) {
         HostConfiguration hostConfig = (HostConfiguration) vc;
         if (hostConfig.getIptablesFile() != null) {
            Path path = Paths.get(testRigPath.toString(),
                  hostConfig.getIptablesFile());
            if (!iptablesConfigurations.containsKey(path.toString())) {
               throw new BatfishException("Key not found for iptables!");
            }
            hostConfig.setIptablesConfig(
                  (IptablesVendorConfiguration) iptablesConfigurations
                        .get(path.toString()));
         }
      }

      // now, serialize
      if (!_settings.getNoOutput()) {
         _logger
               .info("\n*** SERIALIZING VENDOR CONFIGURATION STRUCTURES ***\n");
         resetTimer();
         CommonUtil.createDirectories(outputPath);

         Map<Path, VendorConfiguration> output = new TreeMap<>();
         hostConfigurations.forEach((name, vc) -> {
            Path currentOutputPath = outputPath.resolve(name);
            output.put(currentOutputPath, vc);
         });
         serializeObjects(output);
         // serialize warnings
         serializeObject(answerElement, _testrigSettings.getParseAnswerPath());
         printElapsedTime();

      }
      return answer;
   }

   private void serializeIndependentConfigs(
         Map<String, Configuration> configurations, Path outputPath) {
      if (configurations == null) {
         throw new BatfishException("Exiting due to conversion error(s)");
      }
      if (!_settings.getNoOutput()) {
         _logger.info(
               "\n*** SERIALIZING VENDOR-INDEPENDENT CONFIGURATION STRUCTURES ***\n");
         resetTimer();
         outputPath.toFile().mkdirs();
         Map<Path, Configuration> output = new TreeMap<>();
         configurations.forEach((name, vc) -> {
            Path currentOutputPath = outputPath.resolve(name);
            output.put(currentOutputPath, vc);
         });
         serializeObjects(output);
         printElapsedTime();
      }
   }

   private Answer serializeIndependentConfigs(Path vendorConfigPath,
         Path outputPath) {
      Answer answer = new Answer();
      ConvertConfigurationAnswerElement answerElement = new ConvertConfigurationAnswerElement();
      answer.addAnswerElement(answerElement);
      Map<String, Configuration> configurations = getConfigurations(
            vendorConfigPath, answerElement);
      serializeIndependentConfigs(configurations, outputPath);
      serializeObject(answerElement, _testrigSettings.getConvertAnswerPath());
      return answer;
   }

   private Answer serializeNetworkConfigs(Path testRigPath, Path outputPath) {
      Answer answer = new Answer();
      Map<Path, String> configurationData = readConfigurationFiles(testRigPath,
            BfConsts.RELPATH_CONFIGURATIONS_DIR);
      ParseVendorConfigurationAnswerElement answerElement = new ParseVendorConfigurationAnswerElement();
      answer.addAnswerElement(answerElement);
      Map<String, VendorConfiguration> vendorConfigurations = parseVendorConfigurations(
            configurationData, answerElement, ConfigurationFormat.UNKNOWN);
      if (vendorConfigurations == null) {
         throw new BatfishException("Exiting due to parser errors");
      }
      Path nodeRolesPath = _settings.getNodeRolesPath();
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
            serializeObject(nodeRoles, nodeRolesPath);
            _logger.info("OK\n");
         }
      }
      if (!_settings.getNoOutput()) {
         _logger
               .info("\n*** SERIALIZING VENDOR CONFIGURATION STRUCTURES ***\n");
         resetTimer();
         CommonUtil.createDirectories(outputPath);
         Map<Path, VendorConfiguration> output = new TreeMap<>();
         vendorConfigurations.forEach((name, vc) -> {
            Path currentOutputPath = outputPath.resolve(name);
            output.put(currentOutputPath, vc);
         });
         serializeObjects(output);
         // serialize warnings
         serializeObject(answerElement, _testrigSettings.getParseAnswerPath());
         printElapsedTime();

      }
      return answer;
   }

   private Answer serializeVendorConfigs(Path testRigPath, Path outputPath) {
      Answer answer = new Answer();
      boolean configsFound = false;

      // look for network configs
      Path networkConfigsPath = testRigPath
            .resolve(BfConsts.RELPATH_CONFIGURATIONS_DIR);
      if (Files.exists(networkConfigsPath)) {
         answer.append(serializeNetworkConfigs(testRigPath, outputPath));
         configsFound = true;
      }

      // look for AWS VPC configs
      Path awsVpcConfigsPath = testRigPath
            .resolve(BfConsts.RELPATH_AWS_VPC_CONFIGS_DIR);
      if (Files.exists(awsVpcConfigsPath)) {
         answer.append(serializeAwsVpcConfigs(testRigPath, outputPath));
         configsFound = true;
      }

      // look for host configs
      Path hostConfigsPath = testRigPath
            .resolve(BfConsts.RELPATH_HOST_CONFIGS_DIR);
      if (Files.exists(hostConfigsPath)) {
         answer.append(serializeHostConfigs(testRigPath, outputPath));
         configsFound = true;
      }

      if (!configsFound) {
         throw new BatfishException("No valid configurations found");
      }
      return answer;
   }

   @Override
   public void setDataPlanePlugin(DataPlanePlugin dataPlanePlugin) {
      _dataPlanePlugin = dataPlanePlugin;
   }

   public void setTerminatedWithException(boolean terminatedWithException) {
      _terminatedWithException = terminatedWithException;
   }

   @Override
   public AnswerElement standard(HeaderSpace headerSpace,
         Set<ForwardingAction> actions, String ingressNodeRegexStr,
         String notIngressNodeRegexStr, String finalNodeRegexStr,
         String notFinalNodeRegexStr) {
      Settings settings = getSettings();
      checkDataPlaneQuestionDependencies(_testrigSettings);
      String tag = getFlowTag(_testrigSettings);
      Map<String, Configuration> configurations = loadConfigurations();
      Set<Flow> flows = null;
      Synthesizer dataPlaneSynthesizer = synthesizeDataPlane();

      // collect ingress nodes
      Pattern ingressNodeRegex = Pattern.compile(ingressNodeRegexStr);
      Pattern notIngressNodeRegex = Pattern.compile(notIngressNodeRegexStr);
      Set<String> activeIngressNodes = new TreeSet<>();
      for (String node : configurations.keySet()) {
         Matcher ingressNodeMatcher = ingressNodeRegex.matcher(node);
         Matcher notIngressNodeMatcher = notIngressNodeRegex.matcher(node);
         if (ingressNodeMatcher.matches() && !notIngressNodeMatcher.matches()) {
            activeIngressNodes.add(node);
         }
      }
      if (activeIngressNodes.isEmpty()) {
         return new StringAnswerElement(
               "NOTHING TO DO: No nodes both match ingressNodeRegex: '"
                     + ingressNodeRegexStr
                     + "' and fail to match notIngressNodeRegex: '"
                     + notIngressNodeRegexStr + "'");
      }

      // collect final nodes
      Pattern finalNodeRegex = Pattern.compile(finalNodeRegexStr);
      Pattern notFinalNodeRegex = Pattern.compile(notFinalNodeRegexStr);
      Set<String> activeFinalNodes = new TreeSet<>();
      for (String node : configurations.keySet()) {
         Matcher finalNodeMatcher = finalNodeRegex.matcher(node);
         Matcher notFinalNodeMatcher = notFinalNodeRegex.matcher(node);
         if (finalNodeMatcher.matches() && !notFinalNodeMatcher.matches()) {
            activeFinalNodes.add(node);
         }
      }
      if (activeFinalNodes.isEmpty()) {
         return new StringAnswerElement(
               "NOTHING TO DO: No nodes both match finalNodeRegex: '"
                     + finalNodeRegexStr
                     + "' and fail to match notFinalNodeRegex: '"
                     + notFinalNodeRegexStr + "'");
      }

      // build query jobs
      List<NodJob> jobs = new ArrayList<>();
      for (String ingressNode : activeIngressNodes) {
         ReachabilityQuerySynthesizer query = new ReachabilityQuerySynthesizer(
               actions, headerSpace, activeFinalNodes,
               Collections.singleton(ingressNode));
         NodeSet nodes = new NodeSet();
         nodes.add(ingressNode);
         NodJob job = new NodJob(settings, dataPlaneSynthesizer, query, nodes,
               tag);
         jobs.add(job);
      }

      // run jobs and get resulting flows
      flows = computeNodOutput(jobs);

      getDataPlanePlugin().processFlows(flows);

      AnswerElement answerElement = getHistory();
      return answerElement;

   }

   private Synthesizer synthesizeAcls(
         Map<String, Configuration> configurations) {
      _logger.info("\n*** GENERATING Z3 LOGIC ***\n");
      resetTimer();

      _logger.info("Synthesizing Z3 ACL logic...");
      Synthesizer s = new Synthesizer(configurations, _settings.getSimplify());

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

   public Synthesizer synthesizeDataPlane() {

      _logger.info("\n*** GENERATING Z3 LOGIC ***\n");
      resetTimer();

      DataPlane dataPlane = loadDataPlane();

      _logger.info("Synthesizing Z3 logic...");
      Map<String, Configuration> configurations = loadConfigurations();
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

   private Topology synthesizeTopology(
         Map<String, Configuration> configurations) {
      _logger.info(
            "\n*** SYNTHESIZING TOPOLOGY FROM INTERFACE SUBNET INFORMATION ***\n");
      resetTimer();
      EdgeSet edges = new EdgeSet();
      Map<Prefix, Set<NodeInterfacePair>> prefixInterfaces = new HashMap<>();
      configurations.forEach((nodeName, node) -> {
         node.getInterfaces().forEach((ifaceName, iface) -> {
            if (!iface.isLoopback(node.getConfigurationFormat())
                  && iface.getActive()) {
               for (Prefix prefix : iface.getAllPrefixes()) {
                  if (prefix.getPrefixLength() < 32) {
                     Prefix network = new Prefix(prefix.getNetworkAddress(),
                           prefix.getPrefixLength());
                     NodeInterfacePair pair = new NodeInterfacePair(nodeName,
                           ifaceName);
                     Set<NodeInterfacePair> interfaceBucket = prefixInterfaces
                           .get(network);
                     if (interfaceBucket == null) {
                        interfaceBucket = new HashSet<>();
                        prefixInterfaces.put(network, interfaceBucket);
                     }
                     interfaceBucket.add(pair);
                  }
               }
            }
         });
      });
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
      return new Topology(edges);
   }

   private void writeBgpAdvertisements(Path writeAdvertsPath) {
      AdvertisementSet adverts = _dataPlanePlugin.getAdvertisements();
      CommonUtil.createDirectories(writeAdvertsPath.getParent());
      _logger.info("Serializing: BGP advertisements => \"" + writeAdvertsPath
            + "\"...");
      serializeObject(adverts, writeAdvertsPath);
      _logger.info("OK\n");
   }

   @Override
   public void writeDataPlane(DataPlane dp) {
      serializeObject(dp,
            _testrigSettings.getEnvironmentSettings().getDataPlanePath());
   }

   private void writeIbgpNeighbors(Path ibgpTopologyPath) {
      IbgpTopology topology = getIbgpNeighbors();
      CommonUtil.createDirectories(ibgpTopologyPath.getParent());
      _logger.info(
            "Serializing: IBGP neighbors => \"" + ibgpTopologyPath + "\"...");
      serializeObject(topology, ibgpTopologyPath);
      _logger.info("OK\n");
   }

   private void writeJsonAnswer(String jsonAnswer) {
      Path jsonPath = _settings.getAnswerJsonPath();
      if (jsonPath != null) {
         CommonUtil.writeFile(jsonPath, jsonAnswer);
      }
      Path questionPath = _settings.getQuestionPath();
      if (questionPath != null) {
         if (!Files.exists(questionPath)) {
            throw new BatfishException(
                  "Could not write JSON answer to question dir '"
                        + questionPath.toString()
                        + "' because it does not exist");
         }
         Path answerPath = questionPath.getParent()
               .resolve(BfConsts.RELPATH_ANSWER_JSON);
         CommonUtil.writeFile(answerPath, jsonAnswer);
      }
   }

   private void writeJsonTopology() {
      try {
         Map<String, Configuration> configs = loadConfigurations();
         EdgeSet textEdges = synthesizeTopology(configs).getEdges();
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

   public void writeRoutes(Path writeRoutesPath) {
      RouteSet routes = _dataPlanePlugin.getRoutes();
      CommonUtil.createDirectories(writeRoutesPath.getParent());
      _logger.info("Serializing: routes => \"" + writeRoutesPath + "\"...");
      serializeObject(routes, writeRoutesPath);
      _logger.info("OK\n");
   }

   private void writeSynthesizedTopology() {
      Map<String, Configuration> configs = loadConfigurations();
      EdgeSet edges = synthesizeTopology(configs).getEdges();
      _logger.output(BatfishTopologyCombinedParser.HEADER + "\n");
      for (Edge edge : edges) {
         _logger.output(edge.getNode1() + ":" + edge.getInt1() + ","
               + edge.getNode2() + ":" + edge.getInt2() + "\n");
      }
      printElapsedTime();
   }

}
