package org.batfish.main;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.batfish.bdp.BdpDataPlanePlugin;
import org.batfish.bgp.JsonExternalBgpAdvertisementPlugin;
import org.batfish.common.Answerer;
import org.batfish.common.BatfishException;
import org.batfish.common.BatfishException.BatfishStackTrace;
import org.batfish.common.BatfishLogger;
import org.batfish.common.BfConsts;
import org.batfish.common.CleanBatfishException;
import org.batfish.common.Directory;
import org.batfish.common.Pair;
import org.batfish.common.Version;
import org.batfish.common.Warning;
import org.batfish.common.Warnings;
import org.batfish.common.plugin.BgpTablePlugin;
import org.batfish.common.plugin.DataPlanePlugin;
import org.batfish.common.plugin.ExternalBgpAdvertisementPlugin;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.PluginClientType;
import org.batfish.common.plugin.PluginConsumer;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.common.util.CommonUtil;
import org.batfish.config.Settings;
import org.batfish.config.Settings.EnvironmentSettings;
import org.batfish.config.Settings.TestrigSettings;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.BgpAdvertisement;
import org.batfish.datamodel.BgpAdvertisement.BgpAdvertisementType;
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
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.IpsecVpn;
import org.batfish.datamodel.OspfArea;
import org.batfish.datamodel.OspfNeighbor;
import org.batfish.datamodel.OspfProcess;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Route;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.VrrpGroup;
import org.batfish.datamodel.answers.AclLinesAnswerElement;
import org.batfish.datamodel.answers.AclLinesAnswerElement.AclReachabilityEntry;
import org.batfish.datamodel.answers.Answer;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.answers.AnswerStatus;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.datamodel.answers.DataPlaneAnswerElement;
import org.batfish.datamodel.answers.EnvironmentCreationAnswerElement;
import org.batfish.datamodel.answers.FlattenVendorConfigurationAnswerElement;
import org.batfish.datamodel.answers.InitInfoAnswerElement;
import org.batfish.datamodel.answers.NodAnswerElement;
import org.batfish.datamodel.answers.NodFirstUnsatAnswerElement;
import org.batfish.datamodel.answers.NodSatAnswerElement;
import org.batfish.datamodel.answers.ParseEnvironmentBgpTablesAnswerElement;
import org.batfish.datamodel.answers.ParseEnvironmentRoutingTablesAnswerElement;
import org.batfish.datamodel.answers.ParseStatus;
import org.batfish.datamodel.answers.ParseVendorConfigurationAnswerElement;
import org.batfish.datamodel.answers.ReportAnswerElement;
import org.batfish.datamodel.answers.RunAnalysisAnswerElement;
import org.batfish.datamodel.answers.StringAnswerElement;
import org.batfish.datamodel.assertion.AssertionAst;
import org.batfish.datamodel.collections.AdvertisementSet;
import org.batfish.datamodel.collections.BgpAdvertisementsByVrf;
import org.batfish.datamodel.collections.EdgeSet;
import org.batfish.datamodel.collections.InterfaceSet;
import org.batfish.datamodel.collections.MultiSet;
import org.batfish.datamodel.collections.NamedStructureEquivalenceSet;
import org.batfish.datamodel.collections.NamedStructureEquivalenceSets;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.collections.NodeRoleMap;
import org.batfish.datamodel.collections.NodeSet;
import org.batfish.datamodel.collections.NodeVrfSet;
import org.batfish.datamodel.collections.RoleSet;
import org.batfish.datamodel.collections.RoutesByVrf;
import org.batfish.datamodel.collections.TreeMultiSet;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.questions.Question.InstanceData;
import org.batfish.datamodel.questions.Question.InstanceData.Variable;
import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.BgpTableFormat;
import org.batfish.grammar.GrammarSettings;
import org.batfish.grammar.ParseTreePrettyPrinter;
import org.batfish.grammar.assertion.AssertionCombinedParser;
import org.batfish.grammar.assertion.AssertionExtractor;
import org.batfish.grammar.assertion.AssertionParser.AssertionContext;
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
import org.batfish.job.ParseEnvironmentBgpTableJob;
import org.batfish.job.ParseEnvironmentBgpTableResult;
import org.batfish.job.ParseEnvironmentRoutingTableJob;
import org.batfish.job.ParseEnvironmentRoutingTableResult;
import org.batfish.job.ParseVendorConfigurationJob;
import org.batfish.job.ParseVendorConfigurationResult;
import org.batfish.representation.aws_vpcs.AwsVpcConfiguration;
import org.batfish.representation.host.HostConfiguration;
import org.batfish.representation.iptables.IptablesVendorConfiguration;
import org.batfish.vendor.VendorConfiguration;
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

/**
 * This class encapsulates the main control logic for Batfish.
 */
public class Batfish extends PluginConsumer implements AutoCloseable, IBatfish {

   private static final String BASE_TESTRIG_TAG = "BASE";

   private static final String DELTA_TESTRIG_TAG = "DELTA";

   private static final String DIFFERENTIAL_FLOW_TAG = "DIFFERENTIAL";

   private static final String GEN_OSPF_STARTING_IP = "10.0.0.0";

   /**
    * The name of the [optional] topology file within a test-rig
    */
   private static final String TOPOLOGY_FILENAME = "topology.net";

   public static void applyBaseDir(TestrigSettings settings, Path containerDir,
         String testrig, String envName) {
      Path testrigDir = containerDir.resolve(testrig);
      settings.setName(testrig);
      settings.setBasePath(testrigDir);
      EnvironmentSettings envSettings = settings.getEnvironmentSettings();
      settings.setSerializeIndependentPath(
            testrigDir.resolve(BfConsts.RELPATH_VENDOR_INDEPENDENT_CONFIG_DIR));
      settings.setSerializeVendorPath(
            testrigDir.resolve(BfConsts.RELPATH_VENDOR_SPECIFIC_CONFIG_DIR));
      settings
            .setTestRigPath(testrigDir.resolve(BfConsts.RELPATH_TEST_RIG_DIR));
      settings.setParseAnswerPath(
            testrigDir.resolve(BfConsts.RELPATH_PARSE_ANSWER_PATH));
      settings.setConvertAnswerPath(
            testrigDir.resolve(BfConsts.RELPATH_CONVERT_ANSWER_PATH));
      if (envName != null) {
         envSettings.setName(envName);
         Path envPath = testrigDir.resolve(BfConsts.RELPATH_ENVIRONMENTS_DIR)
               .resolve(envName);
         envSettings.setEnvironmentBasePath(envPath);
         envSettings
               .setDataPlanePath(envPath.resolve(BfConsts.RELPATH_DATA_PLANE));
         envSettings.setDataPlaneAnswerPath(
               envPath.resolve(BfConsts.RELPATH_DATA_PLANE_ANSWER_PATH));
         envSettings.setParseEnvironmentBgpTablesAnswerPath(
               envPath.resolve(BfConsts.RELPATH_ENVIRONMENT_BGP_TABLES_ANSWER));
         envSettings.setParseEnvironmentRoutingTablesAnswerPath(envPath
               .resolve(BfConsts.RELPATH_ENVIRONMENT_ROUTING_TABLES_ANSWER));
         envSettings.setSerializeEnvironmentBgpTablesPath(envPath
               .resolve(BfConsts.RELPATH_SERIALIZED_ENVIRONMENT_BGP_TABLES));
         envSettings.setSerializeEnvironmentRoutingTablesPath(envPath.resolve(
               BfConsts.RELPATH_SERIALIZED_ENVIRONMENT_ROUTING_TABLES));
         Path envDirPath = envPath.resolve(BfConsts.RELPATH_ENV_DIR);
         envSettings.setEnvPath(envDirPath);
         envSettings.setNodeBlacklistPath(
               envDirPath.resolve(BfConsts.RELPATH_NODE_BLACKLIST_FILE));
         envSettings.setInterfaceBlacklistPath(
               envDirPath.resolve(BfConsts.RELPATH_INTERFACE_BLACKLIST_FILE));
         envSettings.setEdgeBlacklistPath(
               envDirPath.resolve(BfConsts.RELPATH_EDGE_BLACKLIST_FILE));
         envSettings.setSerializedTopologyPath(
               envDirPath.resolve(BfConsts.RELPATH_TOPOLOGY_FILE));
         envSettings.setDeltaConfigurationsDir(
               envDirPath.resolve(BfConsts.RELPATH_CONFIGURATIONS_DIR));
         envSettings.setExternalBgpAnnouncementsPath(
               envDirPath.resolve(BfConsts.RELPATH_EXTERNAL_BGP_ANNOUNCEMENTS));
         envSettings.setEnvironmentBgpTablesPath(
               envDirPath.resolve(BfConsts.RELPATH_ENVIRONMENT_BGP_TABLES));
         envSettings.setEnvironmentRoutingTablesPath(
               envDirPath.resolve(BfConsts.RELPATH_ENVIRONMENT_ROUTING_TABLES));
         envSettings.setPrecomputedRoutesPath(
               envPath.resolve(BfConsts.RELPATH_PRECOMPUTED_ROUTES));
         envSettings.setDeltaCompiledConfigurationsDir(
               envPath.resolve(BfConsts.RELPATH_VENDOR_INDEPENDENT_CONFIG_DIR));
         envSettings.setDeltaVendorConfigurationsDir(
               envPath.resolve(BfConsts.RELPATH_VENDOR_SPECIFIC_CONFIG_DIR));
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
      Path testrigDir = settings.getActiveTestrigSettings().getBasePath();
      if (questionName != null) {
         Path questionPath = testrigDir.resolve(BfConsts.RELPATH_QUESTIONS_DIR)
               .resolve(questionName);
         settings.setQuestionPath(
               questionPath.resolve(BfConsts.RELPATH_QUESTION_FILE));
      }
   }

   public static void initTestrigSettings(Settings settings) {
      String testrig = settings.getTestrig();
      String envName = settings.getEnvironmentName();
      Path containerDir = settings.getContainerDir();
      if (testrig != null) {
         applyBaseDir(settings.getBaseTestrigSettings(), containerDir, testrig,
               envName);
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
                  deltaEnvName);
         }
         if (settings.getDiffActive()) {
            settings
                  .setActiveTestrigSettings(settings.getDeltaTestrigSettings());
         }
         else {
            settings
                  .setActiveTestrigSettings(settings.getBaseTestrigSettings());
         }
         initQuestionSettings(settings);
      }
      else if (containerDir != null) {
         throw new CleanBatfishException(
               "Must supply argument to -" + BfConsts.ARG_TESTRIG);
      }
   }

   /**
    * Returns a sorted list of {@link Path paths} contains all files under the
    * directory indicated by {@code configsPath}. Directories under
    * {@code configsPath} are recursively expanded but not included in the
    * returned list.
    *
    * <p>
    * Temporary files(files start with {@code .} are omitted from the returned
    * list.
    * </p>
    *
    * <p>
    * This method follows all symbolic links.
    * </p>
    */
   static List<Path> listAllFiles(Path configsPath) {
      List<Path> configFilePaths;
      try (Stream<Path> allFiles = Files.walk(configsPath,
            FileVisitOption.FOLLOW_LINKS)) {
         configFilePaths = allFiles
               .filter(path -> !path.getFileName().toString().startsWith(".")
                     && Files.isRegularFile(path))
               .sorted().collect(Collectors.toList());
      }
      catch (IOException e) {
         throw new BatfishException("Failed to walk path: " + configsPath, e);
      }
      return configFilePaths;
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

   private final Map<String, BiFunction<Question, IBatfish, Answerer>> _answererCreators;

   private TestrigSettings _baseTestrigSettings;

   private SortedMap<BgpTableFormat, BgpTablePlugin> _bgpTablePlugins;

   private final Map<TestrigSettings, SortedMap<String, Configuration>> _cachedConfigurations;

   private final Map<TestrigSettings, DataPlane> _cachedDataPlanes;

   private final Map<EnvironmentSettings, SortedMap<String, BgpAdvertisementsByVrf>> _cachedEnvironmentBgpTables;

   private final Map<EnvironmentSettings, SortedMap<String, RoutesByVrf>> _cachedEnvironmentRoutingTables;

   private DataPlanePlugin _dataPlanePlugin;

   private TestrigSettings _deltaTestrigSettings;

   private Set<ExternalBgpAdvertisementPlugin> _externalBgpAdvertisementPlugins;

   private BatfishLogger _logger;

   private Settings _settings;

   // this variable is used communicate with parent thread on how the job
   // finished
   private boolean _terminatedWithException;

   private TestrigSettings _testrigSettings;

   private final List<TestrigSettings> _testrigSettingsStack;

   private long _timerCount;

   public Batfish(Settings settings,
         Map<TestrigSettings, SortedMap<String, Configuration>> cachedConfigurations,
         Map<TestrigSettings, DataPlane> cachedDataPlanes,
         Map<EnvironmentSettings, SortedMap<String, BgpAdvertisementsByVrf>> cachedEnvironmentBgpTables,
         Map<EnvironmentSettings, SortedMap<String, RoutesByVrf>> cachedEnvironmentRoutingTables) {
      super(settings.getSerializeToText(), settings.getPluginDirs());
      _settings = settings;
      _bgpTablePlugins = new TreeMap<>();
      _cachedConfigurations = cachedConfigurations;
      _cachedEnvironmentBgpTables = cachedEnvironmentBgpTables;
      _cachedEnvironmentRoutingTables = cachedEnvironmentRoutingTables;
      _cachedDataPlanes = cachedDataPlanes;
      _externalBgpAdvertisementPlugins = new TreeSet<>();
      _testrigSettings = settings.getActiveTestrigSettings();
      _baseTestrigSettings = settings.getBaseTestrigSettings();
      _deltaTestrigSettings = settings.getDeltaTestrigSettings();
      _logger = _settings.getLogger();
      _terminatedWithException = false;
      _answererCreators = new HashMap<>();
      _testrigSettingsStack = new ArrayList<>();
   }

   private Answer analyze() {
      Answer answer = new Answer();
      String analysisName = _settings.getAnalysisName();
      Path analysisQuestionsDir = _settings.getContainerDir()
            .resolve(Paths.get(BfConsts.RELPATH_ANALYSES_DIR, analysisName,
                  BfConsts.RELPATH_QUESTIONS_DIR).toString());
      if (!Files.exists(analysisQuestionsDir)) {
         throw new BatfishException("Analysis questions dir does not exist: '"
               + analysisQuestionsDir.toString() + "'");
      }
      RunAnalysisAnswerElement ae = new RunAnalysisAnswerElement();
      try (Stream<Path> questions = CommonUtil.list(analysisQuestionsDir)) {
         questions.forEach(analysisQuestionDir -> {
            String questionName = analysisQuestionDir.getFileName().toString();
            Path analysisQuestionPath = analysisQuestionDir
                  .resolve(BfConsts.RELPATH_QUESTION_FILE);
            _settings.setQuestionPath(analysisQuestionPath);
            Answer currentAnswer = answer();
            initAnalysisQuestionPath(analysisName, questionName);
            outputAnswer(currentAnswer);
            ae.getAnswers().put(questionName, currentAnswer);
         });
      }
      answer.addAnswerElement(ae);
      return answer;
   }

   private void anonymizeConfigurations() {
      // TODO Auto-generated method stub

   }

   private Answer answer() {
      Question question = parseQuestion();
      if (_settings.getDifferential()) {
         question.setDifferential(true);
      }
      boolean dp = question.getDataPlane();
      boolean diff = question.getDifferential();
      boolean diffActive = _settings.getDiffActive() && !diff;
      _settings.setDiffActive(diffActive);
      _settings.setDiffQuestion(diff);

      // Ensures configurations are parsed and ready
      loadConfigurations();

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
         answer.addAnswerElement(exception.getBatfishStackTrace());
      }
      return answer;
   }

   @Override
   public AnswerElement answerAclReachability(String aclNameRegexStr,
         NamedStructureEquivalenceSets<?> aclEqSets) {
      if (SystemUtils.IS_OS_MAC_OSX) {
         // TODO: remove when z3 parallelism bug on OSX is fixed
         _settings.setSequential(true);
      }
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
         Map<String, List<AclLine>> byAclName = arrangedAclLines
               .computeIfAbsent(hostname, k -> new TreeMap<>());
         String aclName = line.getAclName();
         List<AclLine> aclLines = byAclName.computeIfAbsent(aclName,
               k -> new ArrayList<>());
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

   private void checkBaseDirExists() {
      Path baseDir = _testrigSettings.getBasePath();
      if (baseDir == null) {
         throw new BatfishException("Test rig directory not set");
      }
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
      else {
         try (Stream<Path> paths = CommonUtil.list(path)) {
            if (!paths.iterator().hasNext()) {
               throw new CleanBatfishException(
                     "Nothing to do: Set of vendor-independent configurations for this test-rig is empty\n");
            }
         }
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
      if (!environmentExists(testrigSettings)) {
         throw new CleanBatfishException("Environment not initialized: \""
               + testrigSettings.getEnvironmentSettings().getName() + "\"");
      }
   }

   private void checkQuestionsDirExists() {
      checkBaseDirExists();
      Path questionsDir = _testrigSettings.getBasePath()
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
            _settings, _logger, true, "Composite NOD");
      executor.executeJobs(jobs, flows, answerElement);
      printElapsedTime();
      return flows;
   }

   private Answer computeDataPlane(boolean differentialContext) {
      checkEnvironmentExists();
      return _dataPlanePlugin.computeDataPlane(differentialContext);
   }

   private void computeEnvironmentBgpTables() {
      EnvironmentSettings envSettings = _testrigSettings
            .getEnvironmentSettings();
      Path outputPath = envSettings.getSerializeEnvironmentBgpTablesPath();
      Path inputPath = envSettings.getEnvironmentBgpTablesPath();
      serializeEnvironmentBgpTables(inputPath, outputPath);
   }

   private void computeEnvironmentRoutingTables() {
      EnvironmentSettings envSettings = _testrigSettings
            .getEnvironmentSettings();
      Path outputPath = envSettings.getSerializeEnvironmentRoutingTablesPath();
      Path inputPath = envSettings.getEnvironmentRoutingTablesPath();
      serializeEnvironmentRoutingTables(inputPath, outputPath);
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
      // TODO: confirm VRFs are handled correctly
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
         Map<String, Configuration> configurations, boolean excludeInactive) {
      // TODO: confirm VRFs are handled correctly
      Map<Ip, Set<String>> ipOwners = new HashMap<>();
      Map<Pair<Prefix, Integer>, Set<Interface>> vrrpGroups = new HashMap<>();
      configurations.forEach((hostname, c) -> {
         for (Interface i : c.getInterfaces().values()) {
            if (i.getActive() || (!excludeInactive && i.getBlacklisted())) {
               // collect vrrp info
               i.getVrrpGroups().forEach((groupNum, vrrpGroup) -> {
                  Prefix prefix = vrrpGroup.getVirtualAddress();
                  Pair<Prefix, Integer> key = new Pair<>(prefix, groupNum);
                  Set<Interface> candidates = vrrpGroups.computeIfAbsent(key,
                        k -> Collections
                              .newSetFromMap(new IdentityHashMap<>()));
                  candidates.add(i);
               });
               // collect prefixes
               i.getAllPrefixes().stream().map(p -> p.getAddress())
                     .forEach(ip -> {
                        Set<String> owners = ipOwners.computeIfAbsent(ip,
                              k -> new HashSet<>());
                        owners.add(hostname);
                     });
            }
         }
      });
      vrrpGroups.forEach((p, candidates) -> {
         int groupNum = p.getSecond();
         Prefix prefix = p.getFirst();
         Ip ip = prefix.getAddress();
         int lowestPriority = Integer.MAX_VALUE;
         String bestCandidate = null;
         Set<String> bestCandidates = new HashSet<>();
         for (Interface candidate : candidates) {
            VrrpGroup group = candidate.getVrrpGroups().get(groupNum);
            int currentPriority = group.getPriority();
            if (currentPriority < lowestPriority) {
               lowestPriority = currentPriority;
               bestCandidates.clear();
               bestCandidate = candidate.getOwner().getHostname();
            }
            if (currentPriority == lowestPriority) {
               bestCandidates.add(candidate.getOwner().getHostname());
            }
         }
         if (bestCandidates.size() != 1) {
            throw new BatfishException(
                  "multiple best vrrp candidates:" + bestCandidates);
         }
         Set<String> owners = ipOwners.computeIfAbsent(ip,
               k -> new HashSet<>());
         owners.add(bestCandidate);
      });
      return ipOwners;
   }

   @Override
   public Map<Ip, String> computeIpOwnersSimple(Map<Ip, Set<String>> ipOwners) {
      Map<Ip, String> ipOwnersSimple = new HashMap<>();
      ipOwners.forEach((ip, owners) -> {
         String hostname = owners.size() == 1 ? owners.iterator().next()
               : Route.AMBIGUOUS_NEXT_HOP;
         ipOwnersSimple.put(ip, hostname);
      });
      return ipOwnersSimple;
   }

   public <Key, Result> void computeNodFirstUnsatOutput(
         List<NodFirstUnsatJob<Key, Result>> jobs, Map<Key, Result> output) {
      _logger.info("\n*** EXECUTING NOD UNSAT JOBS ***\n");
      resetTimer();
      BatfishJobExecutor<NodFirstUnsatJob<Key, Result>, NodFirstUnsatAnswerElement, NodFirstUnsatResult<Key, Result>, Map<Key, Result>> executor = new BatfishJobExecutor<>(
            _settings, _logger, true, "NOD First-UNSAT");
      executor.executeJobs(jobs, output, new NodFirstUnsatAnswerElement());
      printElapsedTime();
   }

   public Set<Flow> computeNodOutput(List<NodJob> jobs) {
      _logger.info("\n*** EXECUTING NOD JOBS ***\n");
      resetTimer();
      Set<Flow> flows = new TreeSet<>();
      BatfishJobExecutor<NodJob, NodAnswerElement, NodJobResult, Set<Flow>> executor = new BatfishJobExecutor<>(
            _settings, _logger, true, "NOD");
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
            _settings, _logger, true, "NOD SAT");
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
            _settings, _logger, _settings.getHaltOnConvertError(),
            "Convert configurations to vendor-independent format");
      executor.executeJobs(jobs, configurations, answerElement);
      printElapsedTime();
      return configurations;
   }

   @Override
   public EnvironmentCreationAnswerElement createEnvironment(String newEnvName,
         SortedSet<String> nodeBlacklist,
         SortedSet<NodeInterfacePair> interfaceBlacklist,
         SortedSet<Edge> edgeBlacklist, boolean dp) {
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
      applyBaseDir(_testrigSettings, _settings.getContainerDir(),
            _testrigSettings.getName(), newEnvName);
      EnvironmentSettings newEnvSettings = _testrigSettings
            .getEnvironmentSettings();
      Path newEnvPath = newEnvSettings.getEnvPath();
      if (Files.exists(newEnvPath)) {
         throw new BatfishException("Cannot create new environment '"
               + newEnvName + "': environment with same name already exists");
      }
      newEnvPath.toFile().mkdirs();
      try {
         FileUtils.copyDirectory(oldEnvPath.toFile(), newEnvPath.toFile());
      }
      catch (IOException e) {
         throw new BatfishException(
               "Failed to intialize new environment from old environment", e);
      }

      // write node blacklist from question
      String nodeBlacklistStr;
      if (nodeBlacklist != null && !nodeBlacklist.isEmpty()) {
         try {
            nodeBlacklistStr = new BatfishObjectMapper()
                  .writeValueAsString(nodeBlacklist);
         }
         catch (JsonProcessingException e) {
            throw new BatfishException("Could not serialize node blacklist", e);
         }
         CommonUtil.writeFile(newEnvSettings.getNodeBlacklistPath(),
               nodeBlacklistStr);
      }
      // write interface blacklist from question
      if (interfaceBlacklist != null && !interfaceBlacklist.isEmpty()) {
         String interfaceBlacklistStr;
         try {
            interfaceBlacklistStr = new BatfishObjectMapper()
                  .writeValueAsString(interfaceBlacklist);
         }
         catch (JsonProcessingException e) {
            throw new BatfishException(
                  "Could not serialize interface blacklist", e);
         }
         CommonUtil.writeFile(newEnvSettings.getInterfaceBlacklistPath(),
               interfaceBlacklistStr);
      }

      // write edge blacklist from question
      if (edgeBlacklist != null) {
         String edgeBlacklistStr;
         try {
            edgeBlacklistStr = new BatfishObjectMapper()
                  .writeValueAsString(edgeBlacklist);
         }
         catch (JsonProcessingException e) {
            throw new BatfishException("Could not serialize edge blacklist", e);
         }
         CommonUtil.writeFile(newEnvSettings.getEdgeBlacklistPath(),
               edgeBlacklistStr);
      }

      if (dp && !dataPlaneDependenciesExist(_testrigSettings)) {
         computeDataPlane(true);
      }
      return answerElement;
   }

   private boolean dataPlaneDependenciesExist(TestrigSettings testrigSettings) {
      checkConfigurations();
      Path dpPath = testrigSettings.getEnvironmentSettings()
            .getDataPlaneAnswerPath();
      return Files.exists(dpPath);
   }

   public SortedMap<String, Configuration> deserializeConfigurations(
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
      SortedMap<String, Configuration> configurations = deserializeObjects(
            namesByPath, Configuration.class);
      printElapsedTime();
      return configurations;
   }

   private SortedMap<String, BgpAdvertisementsByVrf> deserializeEnvironmentBgpTables(
         Path serializeEnvironmentBgpTablesPath) {
      _logger.info("\n*** DESERIALIZING ENVIRONMENT BGP TABLES ***\n");
      resetTimer();
      Map<Path, String> namesByPath = new TreeMap<>();
      try (DirectoryStream<Path> serializedBgpTables = Files
            .newDirectoryStream(serializeEnvironmentBgpTablesPath)) {
         for (Path serializedBgpTable : serializedBgpTables) {
            String name = serializedBgpTable.getFileName().toString();
            namesByPath.put(serializedBgpTable, name);
         }
      }
      catch (IOException e) {
         throw new BatfishException(
               "Error reading serialized BGP tables directory", e);
      }
      SortedMap<String, BgpAdvertisementsByVrf> bgpTables = deserializeObjects(
            namesByPath, BgpAdvertisementsByVrf.class);
      printElapsedTime();
      return bgpTables;
   }

   private SortedMap<String, RoutesByVrf> deserializeEnvironmentRoutingTables(
         Path serializeEnvironmentRoutingTablesPath) {
      _logger.info("\n*** DESERIALIZING ENVIRONMENT ROUTING TABLES ***\n");
      resetTimer();
      Map<Path, String> namesByPath = new TreeMap<>();
      try (DirectoryStream<Path> serializedRoutingTables = Files
            .newDirectoryStream(serializeEnvironmentRoutingTablesPath)) {
         for (Path serializedRoutingTable : serializedRoutingTables) {
            String name = serializedRoutingTable.getFileName().toString();
            namesByPath.put(serializedRoutingTable, name);
         }
      }
      catch (IOException e) {
         throw new BatfishException(
               "Error reading serialized routing tables directory", e);
      }
      SortedMap<String, RoutesByVrf> routingTables = deserializeObjects(
            namesByPath, RoutesByVrf.class);
      printElapsedTime();
      return routingTables;
   }

   public <S extends Serializable> SortedMap<String, S> deserializeObjects(
         Map<Path, String> namesByPath, Class<S> outputClass) {
      String outputClassName = outputClass.getName();
      BatfishLogger logger = getLogger();
      Map<String, byte[]> dataByName = new TreeMap<>();
      AtomicInteger readCompleted = newBatch(
            "Reading and unpacking files containg '" + outputClassName
                  + "' instances",
            namesByPath.size());
      namesByPath.forEach((inputPath, name) -> {
         logger.debug("Reading and gunzipping: " + outputClassName + " '" + name
               + "' from '" + inputPath.toString() + "'");
         byte[] data = fromGzipFile(inputPath);
         logger.debug(" ...OK\n");
         dataByName.put(name, data);
         readCompleted.incrementAndGet();
      });
      Map<String, S> unsortedOutput = new ConcurrentHashMap<>();
      AtomicInteger deserializeCompleted = newBatch(
            "Deserializing '" + outputClassName + "' instances",
            dataByName.size());
      dataByName.keySet().parallelStream().forEach(name -> {
         byte[] data = dataByName.get(name);
         S object = deserializeObject(data, outputClass);
         unsortedOutput.put(name, object);
         deserializeCompleted.incrementAndGet();
      });
      SortedMap<String, S> output = new TreeMap<>(unsortedOutput);
      return output;
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

   private void disableUnusableVlanInterfaces(
         Map<String, Configuration> configurations) {
      for (Configuration c : configurations.values()) {
         Map<Integer, Interface> vlanInterfaces = new HashMap<>();
         Map<Integer, Integer> vlanMemberCounts = new HashMap<>();
         Set<Interface> nonVlanInterfaces = new HashSet<>();
         Integer vlanNumber = null;
         // Populate vlanInterface and nonVlanInterfaces, and initialize
         // vlanMemberCounts:
         for (Interface iface : c.getInterfaces().values()) {
            if ((iface.getInterfaceType() == InterfaceType.VLAN)
                  && ((vlanNumber = CommonUtil
                        .getInterfaceVlanNumber(iface.getName())) != null)) {
               vlanInterfaces.put(vlanNumber, iface);
               vlanMemberCounts.put(vlanNumber, 0);
            }
            else {
               nonVlanInterfaces.add(iface);
            }
         }
         // Update vlanMemberCounts:
         for (Interface iface : nonVlanInterfaces) {
            List<SubRange> vlans = new ArrayList<>();
            vlanNumber = iface.getAccessVlan();
            if (vlanNumber == 0) { // vlan trunked interface
               vlans.addAll(iface.getAllowedVlans());
               vlanNumber = iface.getNativeVlan();
            }
            vlans.add(new SubRange(vlanNumber, vlanNumber));

            for (SubRange sr : vlans) {
               for (int vlanId = sr.getStart(); vlanId <= sr
                     .getEnd(); ++vlanId) {
                  vlanMemberCounts.compute(vlanId,
                        (k, v) -> (v == null) ? 1 : (v + 1));
               }
            }
         }
         // Disable all "normal" vlan interfaces with zero member counts:
         String hostname = c.getHostname();
         SubRange normalVlanRange = c.getNormalVlanRange();
         for (Map.Entry<Integer, Integer> entry : vlanMemberCounts.entrySet()) {
            if (entry.getValue() == 0) {
               vlanNumber = entry.getKey();
               if ((vlanNumber >= normalVlanRange.getStart())
                     && (vlanNumber <= normalVlanRange.getEnd())) {
                  Interface iface = vlanInterfaces.get(vlanNumber);
                  if ((iface != null) && iface.getAutoState()) {
                     _logger.warnf(
                           "WARNING: Disabling unusable vlan interface because no switch port is assigned to it: \"%s:%d\"\n",
                           hostname, vlanNumber);
                     iface.setActive(false);
                     iface.setBlacklisted(true);
                  }
               }
            }
         }
      }
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
                  bindInterface.setBlacklisted(true);
                  String bindInterfaceName = bindInterface.getName();
                  _logger.warnf(
                        "WARNING: Disabling unusable vpn interface because we cannot determine remote endpoint: \"%s:%s\"\n",
                        hostname, bindInterfaceName);
               }
            }
         }
      }
   }

   private boolean environmentBgpTablesExist(EnvironmentSettings envSettings) {
      checkConfigurations();
      Path answerPath = envSettings.getParseEnvironmentBgpTablesAnswerPath();
      return Files.exists(answerPath);
   }

   private boolean environmentExists(TestrigSettings testrigSettings) {
      checkBaseDirExists();
      Path envPath = testrigSettings.getEnvironmentSettings().getEnvPath();
      if (envPath == null) {
         throw new CleanBatfishException(
               "No environment specified for testrig: "
                     + testrigSettings.getName());
      }
      return Files.exists(envPath);
   }

   private boolean environmentRoutingTablesExist(
         EnvironmentSettings envSettings) {
      checkConfigurations();
      Path answerPath = envSettings
            .getParseEnvironmentRoutingTablesAnswerPath();
      return Files.exists(answerPath);
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
            _settings, _logger,
            _settings.getFlatten() || _settings.getHaltOnParseError(),
            "Flatten configurations");
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
         config.setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
         OspfProcess proc = new OspfProcess();
         config.getDefaultVrf().setOspfProcess(proc);
         proc.setReferenceBandwidth(
               org.batfish.representation.cisco.OspfProcess.DEFAULT_REFERENCE_BANDWIDTH);
         long backboneArea = 0;
         OspfArea area = new OspfArea(backboneArea);
         proc.getAreas().put(backboneArea, area);
         area.getInterfaces()
               .addAll(config.getDefaultVrf().getInterfaces().values());
      }

      serializeIndependentConfigs(configs, outputPath);
   }

   private void generateStubs(String inputRole, int stubAs,
         String interfaceDescriptionRegex) {
      // Map<String, Configuration> configs = loadConfigurations();
      // Pattern pattern = Pattern.compile(interfaceDescriptionRegex);
      // Map<String, Configuration> stubConfigurations = new TreeMap<>();
      //
      // _logger.info("\n*** GENERATING STUBS ***\n");
      // resetTimer();
      //
      // // load old node-roles to be updated at end
      // RoleSet stubRoles = new RoleSet();
      // stubRoles.add(STUB_ROLE);
      // Path nodeRolesPath = _settings.getNodeRolesPath();
      // _logger.info("Deserializing old node-roles mappings: \"" +
      // nodeRolesPath
      // + "\" ...");
      // NodeRoleMap nodeRoles = deserializeObject(nodeRolesPath,
      // NodeRoleMap.class);
      // _logger.info("OK\n");
      //
      // // create origination policy common to all stubs
      // String stubOriginationPolicyName = "~STUB_ORIGINATION_POLICY~";
      // PolicyMap stubOriginationPolicy = new PolicyMap(
      // stubOriginationPolicyName);
      // PolicyMapClause clause = new PolicyMapClause();
      // stubOriginationPolicy.getClauses().add(clause);
      // String stubOriginationRouteFilterListName =
      // "~STUB_ORIGINATION_ROUTE_FILTER~";
      // RouteFilterList rf = new RouteFilterList(
      // stubOriginationRouteFilterListName);
      // RouteFilterLine rfl = new RouteFilterLine(LineAction.ACCEPT,
      // Prefix.ZERO,
      // new SubRange(0, 0));
      // rf.addLine(rfl);
      // PolicyMapMatchRouteFilterListLine matchLine = new
      // PolicyMapMatchRouteFilterListLine(
      // Collections.singleton(rf));
      // clause.getMatchLines().add(matchLine);
      // clause.setAction(PolicyMapAction.PERMIT);
      //
      // Set<String> skipWarningNodes = new HashSet<>();
      //
      // for (Configuration config : configs.values()) {
      // if (!config.getRoles().contains(inputRole)) {
      // continue;
      // }
      // for (BgpNeighbor neighbor : config.getBgpProcess().getNeighbors()
      // .values()) {
      // if (!neighbor.getRemoteAs().equals(stubAs)) {
      // continue;
      // }
      // Prefix neighborPrefix = neighbor.getPrefix();
      // if (neighborPrefix.getPrefixLength() != 32) {
      // throw new BatfishException(
      // "do not currently handle generating stubs based on dynamic bgp
      // sessions");
      // }
      // Ip neighborAddress = neighborPrefix.getAddress();
      // int edgeAs = neighbor.getLocalAs();
      // /*
      // * Now that we have the ip address of the stub, we want to find the
      // * interface that connects to it. We will extract the hostname for
      // * the stub from the description of this interface using the
      // * supplied regex.
      // */
      // boolean found = false;
      // for (Interface iface : config.getInterfaces().values()) {
      // Prefix prefix = iface.getPrefix();
      // if (prefix == null || !prefix.contains(neighborAddress)) {
      // continue;
      // }
      // // the neighbor address falls within the network assigned to this
      // // interface, so now we check the description
      // String description = iface.getDescription();
      // Matcher matcher = pattern.matcher(description);
      // if (matcher.find()) {
      // String hostname = matcher.group(1);
      // if (configs.containsKey(hostname)) {
      // Configuration duplicateConfig = configs.get(hostname);
      // if (!duplicateConfig.getRoles().contains(STUB_ROLE)
      // || duplicateConfig.getRoles().size() != 1) {
      // throw new BatfishException(
      // "A non-generated node with hostname: \""
      // + hostname
      // + "\" already exists in network under analysis");
      // }
      // else {
      // if (!skipWarningNodes.contains(hostname)) {
      // _logger
      // .warn("WARNING: Overwriting previously generated node: \""
      // + hostname + "\"\n");
      // skipWarningNodes.add(hostname);
      // }
      // }
      // }
      // found = true;
      // Configuration stub = stubConfigurations.get(hostname);
      //
      // // create stub if it doesn't exist yet
      // if (stub == null) {
      // stub = new Configuration(hostname);
      // stubConfigurations.put(hostname, stub);
      // // create flow sink interface for stub with common deatils
      // String flowSinkName = "TenGibabitEthernet100/100";
      // Interface flowSink = new Interface(flowSinkName, stub);
      // flowSink.setPrefix(Prefix.ZERO);
      // flowSink.setActive(true);
      // flowSink.setBandwidth(10E9d);
      //
      // stub.getInterfaces().put(flowSinkName, flowSink);
      // stub.setBgpProcess(new BgpProcess());
      // stub.getPolicyMaps().put(stubOriginationPolicyName,
      // stubOriginationPolicy);
      // stub.getRouteFilterLists()
      // .put(stubOriginationRouteFilterListName, rf);
      // stub.setConfigurationFormat(ConfigurationFormat.CISCO);
      // stub.setRoles(stubRoles);
      // nodeRoles.put(hostname, stubRoles);
      // }
      //
      // // create interface that will on which peering will occur
      // Map<String, Interface> stubInterfaces = stub.getInterfaces();
      // String stubInterfaceName = "TenGigabitEthernet0/"
      // + (stubInterfaces.size() - 1);
      // Interface stubInterface = new Interface(stubInterfaceName,
      // stub);
      // stubInterfaces.put(stubInterfaceName, stubInterface);
      // stubInterface.setPrefix(
      // new Prefix(neighborAddress, prefix.getPrefixLength()));
      // stubInterface.setActive(true);
      // stubInterface.setBandwidth(10E9d);
      //
      // // create neighbor within bgp process
      // BgpNeighbor edgeNeighbor = new BgpNeighbor(prefix, stub);
      // edgeNeighbor.getOriginationPolicies()
      // .add(stubOriginationPolicy);
      // edgeNeighbor.setRemoteAs(edgeAs);
      // edgeNeighbor.setLocalAs(stubAs);
      // edgeNeighbor.setSendCommunity(true);
      // edgeNeighbor.setDefaultMetric(0);
      // stub.getBgpProcess().getNeighbors()
      // .put(edgeNeighbor.getPrefix(), edgeNeighbor);
      // break;
      // }
      // else {
      // throw new BatfishException(
      // "Unable to derive stub hostname from interface description: \""
      // + description + "\" using regex: \""
      // + interfaceDescriptionRegex + "\"");
      // }
      // }
      // if (!found) {
      // throw new BatfishException(
      // "Could not determine stub hostname corresponding to ip: \""
      // + neighborAddress.toString()
      // + "\" listed as neighbor on router: \""
      // + config.getHostname() + "\"");
      // }
      // }
      // }
      // // write updated node-roles mappings to disk
      // _logger.info("Serializing updated node-roles mappings: \"" +
      // nodeRolesPath
      // + "\" ...");
      // serializeObject(nodeRoles, nodeRolesPath);
      // _logger.info("OK\n");
      // printElapsedTime();
      //
      // // write stubs to disk
      // serializeIndependentConfigs(stubConfigurations,
      // _testrigSettings.getSerializeIndependentPath());
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
      postProcessConfigurations(configurations.values());
      return configurations;
   }

   public DataPlanePlugin getDataPlanePlugin() {
      return _dataPlanePlugin;
   }

   private Map<String, Configuration> getDeltaConfigurations() {
      EnvironmentSettings envSettings = _testrigSettings
            .getEnvironmentSettings();
      Path deltaDir = envSettings.getDeltaConfigurationsDir();
      if (deltaDir != null && Files.exists(deltaDir)) {
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
            blacklistEdges = parseEdgeBlacklist(edgeBlacklistPath);
         }
      }
      return blacklistEdges;
   }

   private double getElapsedTime(long beforeTime) {
      long difference = System.currentTimeMillis() - beforeTime;
      double seconds = difference / 1000d;
      return seconds;
   }

   private SortedMap<String, BgpAdvertisementsByVrf> getEnvironmentBgpTables(
         Path inputPath, ParseEnvironmentBgpTablesAnswerElement answerElement) {
      if (Files.exists(inputPath.getParent()) && !Files.exists(inputPath)) {
         return new TreeMap<>();
      }
      SortedMap<Path, String> inputData = readFiles(inputPath,
            "Environment BGP Tables");
      SortedMap<String, BgpAdvertisementsByVrf> bgpTables = parseEnvironmentBgpTables(
            inputData, answerElement);
      return bgpTables;
   }

   public String getEnvironmentName() {
      return _testrigSettings.getEnvironmentSettings().getName();
   }

   private SortedMap<String, RoutesByVrf> getEnvironmentRoutingTables(
         Path inputPath,
         ParseEnvironmentRoutingTablesAnswerElement answerElement) {
      if (Files.exists(inputPath.getParent()) && !Files.exists(inputPath)) {
         return new TreeMap<>();
      }
      SortedMap<Path, String> inputData = readFiles(inputPath,
            "Environment Routing Tables");
      SortedMap<String, RoutesByVrf> routingTables = parseEnvironmentRoutingTables(
            inputData, answerElement);
      return routingTables;
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
   public GrammarSettings getGrammarSettings() {
      return _settings;
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

   @Override
   public SortedMap<String, SortedMap<String, SortedSet<AbstractRoute>>> getRoutes() {
      return _dataPlanePlugin.getRoutes();
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

   @Override
   public Directory getTestrigFileTree() {
      Path trPath = _testrigSettings.getTestRigPath();
      Directory dir = new Directory(trPath);
      return dir;
   }

   public String getTestrigName() {
      return _testrigSettings.getName();
   }

   public TestrigSettings getTestrigSettings() {
      return _testrigSettings;
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

   private void initAnalysisQuestionPath(String analysisName,
         String questionName) {
      Path questionDir = _testrigSettings.getBasePath()
            .resolve(Paths
                  .get(BfConsts.RELPATH_ANALYSES_DIR, analysisName,
                        BfConsts.RELPATH_QUESTIONS_DIR, questionName)
                  .toString());
      questionDir.toFile().mkdirs();
      Path questionPath = questionDir.resolve(BfConsts.RELPATH_QUESTION_FILE);
      _settings.setQuestionPath(questionPath);
   }

   @Override
   public void initBgpAdvertisements(
         Map<String, Configuration> configurations) {
      AdvertisementSet globalBgpAdvertisements = _dataPlanePlugin
            .getAdvertisements();
      for (Configuration node : configurations.values()) {
         node.initBgpAdvertisements();
         for (Vrf vrf : node.getVrfs().values()) {
            vrf.initBgpAdvertisements();
         }
      }
      for (BgpAdvertisement bgpAdvertisement : globalBgpAdvertisements) {
         BgpAdvertisementType type = bgpAdvertisement.getType();
         String srcVrf = bgpAdvertisement.getSrcVrf();
         String dstVrf = bgpAdvertisement.getDstVrf();
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
               Vrf originationVrf = originationNode.getVrfs().get(srcVrf);
               originationVrf.getBgpAdvertisements().add(bgpAdvertisement);
               originationVrf.getOriginatedAdvertisements()
                     .add(bgpAdvertisement);
               originationVrf.getOriginatedEbgpAdvertisements()
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
               originationNode.getOriginatedEbgpAdvertisements()
                     .add(bgpAdvertisement);
               Vrf originationVrf = originationNode.getVrfs().get(srcVrf);
               originationVrf.getBgpAdvertisements().add(bgpAdvertisement);
               originationVrf.getOriginatedAdvertisements()
                     .add(bgpAdvertisement);
               originationVrf.getOriginatedIbgpAdvertisements()
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
               Vrf receivingVrf = receivingNode.getVrfs().get(dstVrf);
               receivingVrf.getBgpAdvertisements().add(bgpAdvertisement);
               receivingVrf.getReceivedAdvertisements().add(bgpAdvertisement);
               receivingVrf.getReceivedEbgpAdvertisements()
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
               receivingNode.getReceivedEbgpAdvertisements()
                     .add(bgpAdvertisement);
               Vrf receivingVrf = receivingNode.getVrfs().get(dstVrf);
               receivingVrf.getBgpAdvertisements().add(bgpAdvertisement);
               receivingVrf.getReceivedAdvertisements().add(bgpAdvertisement);
               receivingVrf.getReceivedIbgpAdvertisements()
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
               Vrf sendingVrf = sendingNode.getVrfs().get(srcVrf);
               sendingVrf.getBgpAdvertisements().add(bgpAdvertisement);
               sendingVrf.getSentAdvertisements().add(bgpAdvertisement);
               sendingVrf.getSentEbgpAdvertisements().add(bgpAdvertisement);
            }
            break;
         }

         case IBGP_SENT: {
            String sendingNodeName = bgpAdvertisement.getSrcNode();
            Configuration sendingNode = configurations.get(sendingNodeName);
            if (sendingNode != null) {
               sendingNode.getBgpAdvertisements().add(bgpAdvertisement);
               sendingNode.getSentAdvertisements().add(bgpAdvertisement);
               sendingNode.getSentEbgpAdvertisements().add(bgpAdvertisement);
               Vrf sendingVrf = sendingNode.getVrfs().get(srcVrf);
               sendingVrf.getBgpAdvertisements().add(bgpAdvertisement);
               sendingVrf.getSentAdvertisements().add(bgpAdvertisement);
               sendingVrf.getSentIbgpAdvertisements().add(bgpAdvertisement);
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
      // ProtocolDependencyAnalysis protocolDependencyAnalysis = new
      // ProtocolDependencyAnalysis(
      // configurations);
      // DependencyDatabase database = protocolDependencyAnalysis
      // .getDependencyDatabase();
      //
      // for (Entry<String, Configuration> e : configurations.entrySet()) {
      // PrefixSpace ebgpExportSpace = new PrefixSpace();
      // String name = e.getKey();
      // Configuration node = e.getValue();
      // BgpProcess proc = node.getBgpProcess();
      // if (proc != null) {
      // Set<PotentialExport> bgpExports = database.getPotentialExports(name,
      // RoutingProtocol.BGP);
      // for (PotentialExport export : bgpExports) {
      // DependentRoute exportSourceRoute = export.getDependency();
      // if (!exportSourceRoute.dependsOn(RoutingProtocol.BGP)
      // && !exportSourceRoute.dependsOn(RoutingProtocol.IBGP)) {
      // Prefix prefix = export.getPrefix();
      // ebgpExportSpace.addPrefix(prefix);
      // }
      // }
      // proc.setOriginationSpace(ebgpExportSpace);
      // }
      // }
   }

   @Override
   public InitInfoAnswerElement initInfo(
         boolean summary,
         boolean verboseError,
         boolean environmentRoutes) {
      checkConfigurations();
      InitInfoAnswerElement answerElement = new InitInfoAnswerElement();
      if (environmentRoutes) {
         ParseEnvironmentRoutingTablesAnswerElement parseAnswer = loadParseEnvironmentRoutingTablesAnswerElement();
         if (!summary) {
            SortedMap<String, org.batfish.common.Warnings> warnings = answerElement
                  .getWarnings();
            warnings.putAll(parseAnswer.getWarnings());
         }
         answerElement.setParseStatus(parseAnswer.getParseStatus());
      }
      else {
         ParseVendorConfigurationAnswerElement parseAnswer = loadParseVendorConfigurationAnswerElement();
         ConvertConfigurationAnswerElement convertAnswer = loadConvertConfigurationAnswerElement();
         if (!summary) {
            if (verboseError) {
               SortedMap<String, Set<BatfishStackTrace>> errors = answerElement.getErrors();
               parseAnswer.getErrors().forEach((hostname, parseErrors) -> {
                  errors.computeIfAbsent(hostname, k -> new HashSet<>()).add(parseErrors);
               });
               convertAnswer.getErrors().forEach((hostname, convertErrors) -> {
                  errors.computeIfAbsent(hostname, k -> new HashSet<>()).add(convertErrors);
               });
            }
            SortedMap<String, org.batfish.common.Warnings> warnings = answerElement
                  .getWarnings();
            warnings.putAll(parseAnswer.getWarnings());
            convertAnswer.getWarnings().forEach((hostname, convertWarnings) -> {
               org.batfish.common.Warnings combined = warnings.get(hostname);
               if (combined == null) {
                  warnings.put(hostname, convertWarnings);
               }
               else {
                  combined.getPedanticWarnings()
                        .addAll(convertWarnings.getPedanticWarnings());
                  combined.getRedFlagWarnings()
                        .addAll(convertWarnings.getRedFlagWarnings());
                  combined.getUnimplementedWarnings()
                        .addAll(convertWarnings.getUnimplementedWarnings());
               }
            });
         }
         answerElement.setParseStatus(parseAnswer.getParseStatus());
         for (String failed : convertAnswer.getFailed()) {
            answerElement.getParseStatus().put(failed, ParseStatus.FAILED);
         }
      }
      _logger.info(answerElement.prettyPrint());
      return answerElement;
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
      if (!environmentBgpTablesExist(envSettings)) {
         computeEnvironmentBgpTables();
      }
      if (!environmentRoutingTablesExist(envSettings)) {
         computeEnvironmentRoutingTables();
      }
      if (dp && !dataPlaneDependenciesExist(_testrigSettings)) {
         computeDataPlane(differentialContext);
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
      // TODO: handle duplicate ips on different vrfs
      Map<BgpNeighbor, Ip> remoteAddresses = new IdentityHashMap<>();
      Map<Ip, Set<BgpNeighbor>> localAddresses = new HashMap<>();
      for (Configuration node : configurations.values()) {
         String hostname = node.getHostname();
         for (Vrf vrf : node.getVrfs().values()) {
            BgpProcess proc = vrf.getBgpProcess();
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
                  if (localAddress == null
                        || !ipOwners.containsKey(localAddress)
                        || !ipOwners.get(localAddress).contains(hostname)) {
                     continue;
                  }
                  remoteAddresses.put(bgpNeighbor, remoteAddress);
                  Set<BgpNeighbor> localAddressOwners = localAddresses
                        .computeIfAbsent(localAddress, k -> Collections
                              .newSetFromMap(new IdentityHashMap<>()));
                  localAddressOwners.add(bgpNeighbor);
               }
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
            Ip remoteAddress = ipsecVpn.getIkeGateway().getAddress();
            remoteAddresses.put(ipsecVpn, remoteAddress);
            Set<Prefix> externalPrefixes = ipsecVpn.getIkeGateway()
                  .getExternalInterface().getAllPrefixes();
            for (Prefix externalPrefix : externalPrefixes) {
               Ip externalAddress = externalPrefix.getAddress();
               Set<IpsecVpn> vpnsUsingExternalAddress = externalAddresses
                     .computeIfAbsent(externalAddress, k -> new HashSet<>());
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
                     .getIkeGateway().getLocalAddress();
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
   public void initRemoteOspfNeighbors(
         Map<String, Configuration> configurations,
         Map<Ip, Set<String>> ipOwners, Topology topology) {
      for (Entry<String, Configuration> e : configurations.entrySet()) {
         String hostname = e.getKey();
         Configuration c = e.getValue();
         for (Entry<String, Vrf> e2 : c.getVrfs().entrySet()) {
            Vrf vrf = e2.getValue();
            OspfProcess proc = vrf.getOspfProcess();
            if (proc != null) {
               proc.setOspfNeighbors(new TreeMap<>());
               if (proc != null) {
                  String vrfName = e2.getKey();
                  for (Entry<Long, OspfArea> e3 : proc.getAreas().entrySet()) {
                     long areaNum = e3.getKey();
                     OspfArea area = e3.getValue();
                     for (Interface iface : area.getInterfaces()) {
                        String ifaceName = iface.getName();
                        EdgeSet ifaceEdges = topology.getInterfaceEdges()
                              .get(new NodeInterfacePair(hostname, ifaceName));
                        boolean hasNeighbor = false;
                        Ip localIp = iface.getPrefix().getAddress();
                        if (ifaceEdges != null) {
                           for (Edge edge : ifaceEdges) {
                              if (edge.getNode1().equals(hostname)) {
                                 String remoteHostname = edge.getNode2();
                                 String remoteIfaceName = edge.getInt2();
                                 Configuration remoteNode = configurations
                                       .get(remoteHostname);
                                 Interface remoteIface = remoteNode
                                       .getInterfaces().get(remoteIfaceName);
                                 Vrf remoteVrf = remoteIface.getVrf();
                                 String remoteVrfName = remoteVrf.getName();
                                 OspfProcess remoteProc = remoteVrf
                                       .getOspfProcess();
                                 if (remoteProc.getOspfNeighbors() == null) {
                                    remoteProc
                                          .setOspfNeighbors(new TreeMap<>());
                                 }
                                 if (remoteProc != null) {
                                    OspfArea remoteArea = remoteProc.getAreas()
                                          .get(areaNum);
                                    if (remoteArea != null
                                          && remoteArea.getInterfaceNames()
                                                .contains(remoteIfaceName)) {
                                       Ip remoteIp = remoteIface.getPrefix()
                                             .getAddress();
                                       Pair<Ip, Ip> localKey = new Pair<>(
                                             localIp, remoteIp);
                                       OspfNeighbor neighbor = proc
                                             .getOspfNeighbors().get(localKey);
                                       if (neighbor == null) {
                                          hasNeighbor = true;

                                          // initialize local neighbor
                                          neighbor = new OspfNeighbor(localKey);
                                          neighbor.setArea(areaNum);
                                          neighbor.setVrf(vrfName);
                                          neighbor.setOwner(c);
                                          neighbor.setInterface(iface);
                                          proc.getOspfNeighbors().put(localKey,
                                                neighbor);

                                          // initialize remote neighbor
                                          Pair<Ip, Ip> remoteKey = new Pair<>(
                                                remoteIp, localIp);
                                          OspfNeighbor remoteNeighbor = new OspfNeighbor(
                                                remoteKey);
                                          remoteNeighbor.setArea(areaNum);
                                          remoteNeighbor.setVrf(remoteVrfName);
                                          remoteNeighbor.setOwner(remoteNode);
                                          remoteNeighbor
                                                .setInterface(remoteIface);
                                          remoteProc.getOspfNeighbors()
                                                .put(remoteKey, remoteNeighbor);

                                          // link neighbors
                                          neighbor.setRemoteOspfNeighbor(
                                                remoteNeighbor);
                                          remoteNeighbor.setRemoteOspfNeighbor(
                                                neighbor);
                                       }
                                    }
                                 }
                              }
                           }
                        }
                        if (!hasNeighbor) {
                           Pair<Ip, Ip> key = new Pair<>(localIp, Ip.ZERO);
                           OspfNeighbor neighbor = new OspfNeighbor(key);
                           neighbor.setArea(areaNum);
                           neighbor.setVrf(vrfName);
                           neighbor.setOwner(c);
                           neighbor.setInterface(iface);
                           proc.getOspfNeighbors().put(key, neighbor);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   @Override
   public SortedMap<String, Configuration> loadConfigurations() {
      SortedMap<String, Configuration> configurations = _cachedConfigurations
            .get(_testrigSettings);
      if (configurations == null) {
         ConvertConfigurationAnswerElement ccae = loadConvertConfigurationAnswerElement();
         if (!Version.isCompatibleVersion("Service",
               "Old processed configurations", ccae.getVersion())) {
            repairConfigurations();
         }
         configurations = deserializeConfigurations(
               _testrigSettings.getSerializeIndependentPath());
         _cachedConfigurations.put(_testrigSettings, configurations);
      }
      processNodeBlacklist(configurations);
      processInterfaceBlacklist(configurations);
      processDeltaConfigurations(configurations);
      disableUnusableVlanInterfaces(configurations);
      disableUnusableVpnInterfaces(configurations);
      return configurations;
   }

   @Override
   public ConvertConfigurationAnswerElement loadConvertConfigurationAnswerElement() {
      return loadConvertConfigurationAnswerElement(true);
   }

   private ConvertConfigurationAnswerElement loadConvertConfigurationAnswerElement(
         boolean firstAttempt) {
      if (Files.exists(_testrigSettings.getConvertAnswerPath())) {
         ConvertConfigurationAnswerElement ccae = deserializeObject(
               _testrigSettings.getConvertAnswerPath(),
               ConvertConfigurationAnswerElement.class);
         if (Version.isCompatibleVersion("Service",
               "Old processed configurations", ccae.getVersion())) {
            return ccae;
         }
      }
      if (firstAttempt) {
         repairConfigurations();
         return loadConvertConfigurationAnswerElement(false);
      }
      else {
         throw new BatfishException(
               "Version error repairing configurations for convert configuration answer element");
      }
   }

   @Override
   public DataPlane loadDataPlane() {
      DataPlane dp = _cachedDataPlanes.get(_testrigSettings);
      if (dp == null) {
         /*
          * Data plane should exist after loading answer element, as it triggers
          * repair if necessary. However, it might not be cached if it was not
          * repaired, so we still might need to load it from disk.
          */
         loadDataPlaneAnswerElement();
         dp = _cachedDataPlanes.get(_testrigSettings);
         if (dp == null) {
            newBatch("Loading data plane from disk", 0);
            dp = deserializeObject(
                  _testrigSettings.getEnvironmentSettings().getDataPlanePath(),
                  DataPlane.class);
            _cachedDataPlanes.put(_testrigSettings, dp);
         }
      }
      return dp;
   }

   private DataPlaneAnswerElement loadDataPlaneAnswerElement() {
      return loadDataPlaneAnswerElement(true);
   }

   private DataPlaneAnswerElement loadDataPlaneAnswerElement(
         boolean firstAttempt) {
      DataPlaneAnswerElement bae = deserializeObject(
            _testrigSettings.getEnvironmentSettings().getDataPlaneAnswerPath(),
            DataPlaneAnswerElement.class);
      if (!Version.isCompatibleVersion("Service", "Old data plane",
            bae.getVersion())) {
         if (firstAttempt) {
            repairDataPlane();
            return loadDataPlaneAnswerElement(false);
         }
         else {
            throw new BatfishException(
                  "Version error repairing data plane for data plane answer element");
         }
      }
      else {
         return bae;
      }
   }

   @Override
   public SortedMap<String, BgpAdvertisementsByVrf> loadEnvironmentBgpTables() {
      EnvironmentSettings envSettings = _testrigSettings
            .getEnvironmentSettings();
      SortedMap<String, BgpAdvertisementsByVrf> environmentBgpTables = _cachedEnvironmentBgpTables
            .get(envSettings);
      if (environmentBgpTables == null) {
         ParseEnvironmentBgpTablesAnswerElement ae = loadParseEnvironmentBgpTablesAnswerElement();
         if (!Version.isCompatibleVersion("Service",
               "Old processed environment BGP tables", ae.getVersion())) {
            repairEnvironmentBgpTables();
         }
         environmentBgpTables = deserializeEnvironmentBgpTables(
               envSettings.getSerializeEnvironmentBgpTablesPath());
         _cachedEnvironmentBgpTables.put(envSettings, environmentBgpTables);
      }
      return environmentBgpTables;
   }

   @Override
   public SortedMap<String, RoutesByVrf> loadEnvironmentRoutingTables() {
      EnvironmentSettings envSettings = _testrigSettings
            .getEnvironmentSettings();
      SortedMap<String, RoutesByVrf> environmentRoutingTables = _cachedEnvironmentRoutingTables
            .get(envSettings);
      if (environmentRoutingTables == null) {
         ParseEnvironmentRoutingTablesAnswerElement pertae = loadParseEnvironmentRoutingTablesAnswerElement();
         if (!Version.isCompatibleVersion("Service",
               "Old processed environment routing tables",
               pertae.getVersion())) {
            repairEnvironmentRoutingTables();
         }
         environmentRoutingTables = deserializeEnvironmentRoutingTables(
               envSettings.getSerializeEnvironmentRoutingTablesPath());
         _cachedEnvironmentRoutingTables.put(envSettings,
               environmentRoutingTables);
      }
      return environmentRoutingTables;
   }

   @Override
   public ParseEnvironmentBgpTablesAnswerElement loadParseEnvironmentBgpTablesAnswerElement() {
      return loadParseEnvironmentBgpTablesAnswerElement(true);
   }

   private ParseEnvironmentBgpTablesAnswerElement loadParseEnvironmentBgpTablesAnswerElement(
         boolean firstAttempt) {
      Path answerPath = _testrigSettings.getEnvironmentSettings()
            .getParseEnvironmentBgpTablesAnswerPath();
      if (!Files.exists(answerPath)) {
         repairEnvironmentBgpTables();
      }
      ParseEnvironmentBgpTablesAnswerElement ae = deserializeObject(answerPath,
            ParseEnvironmentBgpTablesAnswerElement.class);
      if (!Version.isCompatibleVersion("Service",
            "Old processed environment BGP tables", ae.getVersion())) {
         if (firstAttempt) {
            repairEnvironmentRoutingTables();
            return loadParseEnvironmentBgpTablesAnswerElement(false);
         }
         else {
            throw new BatfishException(
                  "Version error repairing environment BGP tables for parse environment BGP tables answer element");
         }
      }
      else {
         return ae;
      }
   }

   @Override
   public ParseEnvironmentRoutingTablesAnswerElement loadParseEnvironmentRoutingTablesAnswerElement() {
      return loadParseEnvironmentRoutingTablesAnswerElement(true);
   }

   private ParseEnvironmentRoutingTablesAnswerElement loadParseEnvironmentRoutingTablesAnswerElement(
         boolean firstAttempt) {
      Path answerPath = _testrigSettings.getEnvironmentSettings()
            .getParseEnvironmentRoutingTablesAnswerPath();
      if (!Files.exists(answerPath)) {
         repairEnvironmentRoutingTables();
      }
      ParseEnvironmentRoutingTablesAnswerElement pertae = deserializeObject(
            answerPath, ParseEnvironmentRoutingTablesAnswerElement.class);
      if (!Version.isCompatibleVersion("Service",
            "Old processed environment routing tables", pertae.getVersion())) {
         if (firstAttempt) {
            repairEnvironmentRoutingTables();
            return loadParseEnvironmentRoutingTablesAnswerElement(false);
         }
         else {
            throw new BatfishException(
                  "Version error repairing environment routing tables for parse environment routing tables answer element");
         }
      }
      else {
         return pertae;
      }
   }

   @Override
   public ParseVendorConfigurationAnswerElement loadParseVendorConfigurationAnswerElement() {
      return loadParseVendorConfigurationAnswerElement(true);
   }

   private ParseVendorConfigurationAnswerElement loadParseVendorConfigurationAnswerElement(
         boolean firstAttempt) {
      if (Files.exists(_testrigSettings.getParseAnswerPath())) {
         ParseVendorConfigurationAnswerElement pvcae = deserializeObject(
               _testrigSettings.getParseAnswerPath(),
               ParseVendorConfigurationAnswerElement.class);
         if (Version.isCompatibleVersion("Service",
               "Old processed configurations", pvcae.getVersion())) {
            return pvcae;
         }
      }
      if (firstAttempt) {
         repairVendorConfigurations();
         return loadParseVendorConfigurationAnswerElement(false);
      }
      else {
         throw new BatfishException(
               "Version error repairing vendor configurations for parse configuration answer element");
      }
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
      if (SystemUtils.IS_OS_MAC_OSX) {
         // TODO: remove when z3 parallelism bug on OSX is fixed
         _settings.setSequential(true);
      }
      Settings settings = getSettings();
      String tag = getFlowTag(_testrigSettings);
      Map<String, Configuration> configurations = loadConfigurations();
      Set<Flow> flows = null;
      Synthesizer dataPlaneSynthesizer = synthesizeDataPlane();
      List<NodJob> jobs = new ArrayList<>();
      configurations.forEach((node, configuration) -> {
         for (String vrf : configuration.getVrfs().keySet()) {
            MultipathInconsistencyQuerySynthesizer query = new MultipathInconsistencyQuerySynthesizer(
                  node, vrf, headerSpace);
            NodeVrfSet nodes = new NodeVrfSet();
            nodes.add(new Pair<>(node, vrf));
            NodJob job = new NodJob(settings, dataPlaneSynthesizer, query,
                  nodes, tag);
            jobs.add(job);
         }
      });

      flows = computeNodOutput(jobs);

      getDataPlanePlugin().processFlows(flows);

      AnswerElement answerElement = getHistory();
      return answerElement;
   }

   @Override
   public AtomicInteger newBatch(String description, int jobs) {
      return Driver.newBatch(_settings, description, jobs);
   }

   void outputAnswer(Answer answer) {
      ObjectMapper mapper = new BatfishObjectMapper();
      try {
         Answer structuredAnswer = answer;
         Answer prettyAnswer = structuredAnswer.prettyPrintAnswer();
         StringBuilder structuredAnswerSb = new StringBuilder();
         String structuredAnswerRawString = mapper
               .writeValueAsString(structuredAnswer);
         structuredAnswerSb.append(structuredAnswerRawString);
         structuredAnswerSb.append("\n");
         String structuredAnswerString = structuredAnswerSb.toString();
         StringBuilder prettyAnswerSb = new StringBuilder();
         String prettyAnswerRawString = mapper.writeValueAsString(prettyAnswer);
         prettyAnswerSb.append(prettyAnswerRawString);
         prettyAnswerSb.append("\n");
         String answerString;
         String prettyAnswerString = prettyAnswerSb.toString();
         if (_settings.prettyPrintAnswer()) {
            answerString = prettyAnswerString;
         }
         else {
            answerString = structuredAnswerString;
         }
         _logger.debug(answerString);
         writeJsonAnswer(structuredAnswerString, prettyAnswerString);
      }
      catch (Exception e) {
         BatfishException be = new BatfishException("Error in sending answer",
               e);
         try {
            Answer failureAnswer = Answer.failureAnswer(e.toString(),
                  answer.getQuestion());
            failureAnswer.addAnswerElement(be.getBatfishStackTrace());
            Answer structuredAnswer = failureAnswer;
            Answer prettyAnswer = structuredAnswer.prettyPrintAnswer();
            StringBuilder structuredAnswerSb = new StringBuilder();
            String structuredAnswerRawString = mapper
                  .writeValueAsString(structuredAnswer);
            structuredAnswerSb.append(structuredAnswerRawString);
            structuredAnswerSb.append("\n");
            String structuredAnswerString = structuredAnswerSb.toString();
            StringBuilder prettyAnswerSb = new StringBuilder();
            String prettyAnswerRawString = mapper
                  .writeValueAsString(prettyAnswer);
            prettyAnswerSb.append(prettyAnswerRawString);
            prettyAnswerSb.append("\n");
            String answerString;
            String prettyAnswerString = prettyAnswerSb.toString();
            if (_settings.prettyPrintAnswer()) {
               answerString = prettyAnswerString;
            }
            else {
               answerString = structuredAnswerString;
            }
            _logger.error(answerString);
            writeJsonAnswer(structuredAnswerString, prettyAnswerString);
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

   void outputAnswerWithLog(Answer answer) {
      ObjectMapper mapper = new BatfishObjectMapper();
      try {
         Answer structuredAnswer = answer;
         Answer prettyAnswer = structuredAnswer.prettyPrintAnswer();
         StringBuilder structuredAnswerSb = new StringBuilder();
         String structuredAnswerRawString = mapper
               .writeValueAsString(structuredAnswer);
         structuredAnswerSb.append(structuredAnswerRawString);
         structuredAnswerSb.append("\n");
         String structuredAnswerString = structuredAnswerSb.toString();
         StringBuilder prettyAnswerSb = new StringBuilder();
         String prettyAnswerRawString = mapper.writeValueAsString(prettyAnswer);
         prettyAnswerSb.append(prettyAnswerRawString);
         prettyAnswerSb.append("\n");
         String answerString;
         String prettyAnswerString = prettyAnswerSb.toString();
         if (_settings.prettyPrintAnswer()) {
            answerString = prettyAnswerString;
         }
         else {
            answerString = structuredAnswerString;
         }
         _logger.debug(answerString);
         writeJsonAnswerWithLog(answerString, structuredAnswerString,
               prettyAnswerString);
      }
      catch (Exception e) {
         BatfishException be = new BatfishException("Error in sending answer",
               e);
         try {
            Answer failureAnswer = Answer.failureAnswer(e.toString(),
                  answer.getQuestion());
            failureAnswer.addAnswerElement(be.getBatfishStackTrace());
            Answer structuredAnswer = failureAnswer;
            Answer prettyAnswer = structuredAnswer.prettyPrintAnswer();
            StringBuilder structuredAnswerSb = new StringBuilder();
            String structuredAnswerRawString = mapper
                  .writeValueAsString(structuredAnswer);
            structuredAnswerSb.append(structuredAnswerRawString);
            structuredAnswerSb.append("\n");
            String structuredAnswerString = structuredAnswerSb.toString();
            StringBuilder prettyAnswerSb = new StringBuilder();
            String prettyAnswerRawString = mapper
                  .writeValueAsString(prettyAnswer);
            prettyAnswerSb.append(prettyAnswerRawString);
            prettyAnswerSb.append("\n");
            String answerString;
            String prettyAnswerString = prettyAnswerSb.toString();
            if (_settings.prettyPrintAnswer()) {
               answerString = prettyAnswerString;
            }
            else {
               answerString = structuredAnswerString;
            }
            _logger.error(answerString);
            writeJsonAnswerWithLog(answerString, structuredAnswerString,
                  prettyAnswerString);
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

   @Override
   public AssertionAst parseAssertion(String text) {
      AssertionCombinedParser parser = new AssertionCombinedParser(text,
            _settings);
      AssertionContext tree = (AssertionContext) parse(parser);
      ParseTreeWalker walker = new ParseTreeWalker();
      AssertionExtractor extractor = new AssertionExtractor(text,
            parser.getParser());
      walker.walk(extractor, tree);
      AssertionAst ast = extractor.getAst();
      return ast;
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

   private EdgeSet parseEdgeBlacklist(Path edgeBlacklistPath) {
      String edgeBlacklistText = CommonUtil.readFile(edgeBlacklistPath);
      SortedSet<Edge> edges;
      try {
         edges = new BatfishObjectMapper().<SortedSet<Edge>> readValue(
               edgeBlacklistText, new TypeReference<SortedSet<Edge>>() {
               });
      }
      catch (IOException e) {
         throw new BatfishException("Failed to parse edge blacklist", e);
      }
      return new EdgeSet(edges);
   }

   private SortedMap<String, BgpAdvertisementsByVrf> parseEnvironmentBgpTables(
         SortedMap<Path, String> inputData,
         ParseEnvironmentBgpTablesAnswerElement answerElement) {
      _logger.info("\n*** PARSING ENVIRONMENT BGP TABLES ***\n");
      resetTimer();
      SortedMap<String, BgpAdvertisementsByVrf> bgpTables = new TreeMap<>();
      List<ParseEnvironmentBgpTableJob> jobs = new ArrayList<>();
      SortedMap<String, Configuration> configurations = loadConfigurations();
      for (Path currentFile : inputData.keySet()) {
         String hostname = currentFile.getFileName().toString();
         String optionalSuffix = ".bgp";
         if (hostname.endsWith(optionalSuffix)) {
            hostname = hostname.substring(0,
                  hostname.length() - optionalSuffix.length());
         }
         if (!configurations.containsKey(hostname)) {
            continue;
         }
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
         String fileText = inputData.get(currentFile);
         ParseEnvironmentBgpTableJob job = new ParseEnvironmentBgpTableJob(
               _settings, fileText, hostname, currentFile, warnings,
               _bgpTablePlugins);
         jobs.add(job);
      }
      BatfishJobExecutor<ParseEnvironmentBgpTableJob, ParseEnvironmentBgpTablesAnswerElement, ParseEnvironmentBgpTableResult, SortedMap<String, BgpAdvertisementsByVrf>> executor = new BatfishJobExecutor<>(
            _settings, _logger, _settings.getHaltOnParseError(),
            "Parse environment BGP tables");
      executor.executeJobs(jobs, bgpTables, answerElement);
      printElapsedTime();
      return bgpTables;
   }

   private SortedMap<String, RoutesByVrf> parseEnvironmentRoutingTables(
         SortedMap<Path, String> inputData,
         ParseEnvironmentRoutingTablesAnswerElement answerElement) {
      _logger.info("\n*** PARSING ENVIRONMENT ROUTING TABLES ***\n");
      resetTimer();
      SortedMap<String, RoutesByVrf> routingTables = new TreeMap<>();
      List<ParseEnvironmentRoutingTableJob> jobs = new ArrayList<>();
      SortedMap<String, Configuration> configurations = loadConfigurations();
      for (Path currentFile : inputData.keySet()) {
         String hostname = currentFile.getFileName().toString();
         if (!configurations.containsKey(hostname)) {
            continue;
         }
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
         String fileText = inputData.get(currentFile);
         ParseEnvironmentRoutingTableJob job = new ParseEnvironmentRoutingTableJob(
               _settings, fileText, currentFile, warnings, this);
         jobs.add(job);
      }
      BatfishJobExecutor<ParseEnvironmentRoutingTableJob, ParseEnvironmentRoutingTablesAnswerElement, ParseEnvironmentRoutingTableResult, SortedMap<String, RoutesByVrf>> executor = new BatfishJobExecutor<>(
            _settings, _logger, _settings.getHaltOnParseError(),
            "Parse environment routing tables");
      executor.executeJobs(jobs, routingTables, answerElement);
      printElapsedTime();
      return routingTables;
   }

   private Set<NodeInterfacePair> parseInterfaceBlacklist(
         Path interfaceBlacklistPath) {
      String interfaceBlacklistText = CommonUtil
            .readFile(interfaceBlacklistPath);
      SortedSet<NodeInterfacePair> ifaces;
      try {
         ifaces = new BatfishObjectMapper()
               .<SortedSet<NodeInterfacePair>> readValue(interfaceBlacklistText,
                     new TypeReference<SortedSet<NodeInterfacePair>>() {
                     });
      }
      catch (IOException e) {
         throw new BatfishException("Failed to parse interface blacklist", e);
      }
      return ifaces;
   }

   private NodeSet parseNodeBlacklist(Path nodeBlacklistPath) {
      String nodeBlacklistText = CommonUtil.readFile(nodeBlacklistPath);
      SortedSet<String> nodes;
      try {
         nodes = new BatfishObjectMapper().<SortedSet<String>> readValue(
               nodeBlacklistText, new TypeReference<SortedSet<String>>() {
               });
      }
      catch (IOException e) {
         throw new BatfishException("Failed to parse node blacklist", e);
      }
      return new NodeSet(nodes);
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
      String rawQuestionText = CommonUtil.readFile(questionPath);
      _logger.info("OK\n");
      String questionText = preprocessQuestion(rawQuestionText);
      try {
         ObjectMapper mapper = new BatfishObjectMapper(getCurrentClassLoader());
         Question question = mapper.readValue(questionText, Question.class);
         return question;
      }
      catch (IOException e) {
         throw new BatfishException("Could not parse JSON question", e);
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
            _settings, _logger, _settings.getHaltOnParseError(),
            "Parse configurations");
      executor.executeJobs(jobs, vendorConfigurations, answerElement);
      printElapsedTime();
      return vendorConfigurations;
   }

   @Override
   public AnswerElement pathDiff(HeaderSpace headerSpace) {
      if (SystemUtils.IS_OS_MAC_OSX) {
         // TODO: remove when z3 parallelism bug on OSX is fixed
         _settings.setSequential(true);
      }
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
         String outInterface = edge.getInt1();
         String vrf = diffConfigurations.get(ingressNode).getInterfaces()
               .get(outInterface).getVrf().getName();
         ReachEdgeQuerySynthesizer reachQuery = new ReachEdgeQuerySynthesizer(
               ingressNode, vrf, edge, true, headerSpace);
         ReachEdgeQuerySynthesizer noReachQuery = new ReachEdgeQuerySynthesizer(
               ingressNode, vrf, edge, true, new HeaderSpace());
         noReachQuery.setNegate(true);
         List<QuerySynthesizer> queries = new ArrayList<>();
         queries.add(reachQuery);
         queries.add(noReachQuery);
         queries.add(blacklistQuery);
         NodeVrfSet nodes = new NodeVrfSet();
         nodes.add(new Pair<>(ingressNode, vrf));
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
         String outInterface = missingEdge.getInt1();
         String vrf = diffConfigurations.get(ingressNode).getInterfaces()
               .get(outInterface).getVrf().getName();
         if (diffConfigurations.containsKey(ingressNode)) {
            ReachEdgeQuerySynthesizer reachQuery = new ReachEdgeQuerySynthesizer(
                  ingressNode, vrf, missingEdge, true, headerSpace);
            List<QuerySynthesizer> queries = new ArrayList<>();
            queries.add(reachQuery);
            queries.add(blacklistQuery);
            NodeVrfSet nodes = new NodeVrfSet();
            nodes.add(new Pair<>(ingressNode, vrf));
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

   private void postProcessConfigurations(
         Collection<Configuration> configurations) {
      // ComputeOSPF interface costs where they are missing
      for (Configuration c : configurations) {
         for (Vrf vrf : c.getVrfs().values()) {
            OspfProcess proc = vrf.getOspfProcess();
            if (proc != null) {
               proc.initInterfaceCosts();
            }
         }
      }
   }

   private String preprocessQuestion(String rawQuestionText) {
      try {
         JSONObject jobj = new JSONObject(rawQuestionText);
         if (jobj.has(BfConsts.INSTANCE_VAR)
               && !jobj.isNull(BfConsts.INSTANCE_VAR)) {
            String instanceDataStr = jobj.getString(BfConsts.INSTANCE_VAR);
            BatfishObjectMapper mapper = new BatfishObjectMapper();
            InstanceData instanceData = mapper.<InstanceData> readValue(
                  instanceDataStr, new TypeReference<InstanceData>() {
                  });
            for (Entry<String, Variable> e : instanceData.getVariables()
                  .entrySet()) {
               String varName = e.getKey();
               Variable variable = e.getValue();
               JsonNode value = variable.getValue();
               if (value == null) {
                  if (variable.getOptional()) {
                     /*
                      * For now we assume optional values are top-level
                      * variables and single-line. Otherwise it's not really
                      * clear what to do.
                      */
                     jobj.remove(varName);
                  }
                  else {
                     // What to do here? For now, do nothing and assume that
                     // later validation will handle it.
                  }
                  continue;
               }
               if (variable.getType() == Variable.Type.QUESTION) {
                  if (variable.getMinElements() != null) {
                     if (!value.isArray()) {
                        throw new IllegalArgumentException(
                              "Expecting JSON array for array type");
                     }
                     JSONArray arr = new JSONArray();
                     for (int i = 0; i < value.size(); i++) {
                        String valueJsonString = new ObjectMapper()
                              .writeValueAsString(value.get(i));
                        arr.put(i, new JSONObject(
                              preprocessQuestion(valueJsonString)));
                     }
                     jobj.put(varName, arr);
                  }
                  else {
                     String valueJsonString = new ObjectMapper()
                           .writeValueAsString(value);
                     jobj.put(varName,
                           new JSONObject(preprocessQuestion(valueJsonString)));
                  }
               }
            }
            String questionText = jobj.toString();
            for (Entry<String, Variable> e : instanceData.getVariables()
                  .entrySet()) {
               String varName = e.getKey();
               Variable variable = e.getValue();
               JsonNode value = variable.getValue();
               String valueJsonString = new ObjectMapper()
                     .writeValueAsString(value);
               boolean stringType = variable.getType().getStringType();
               boolean setType = variable.getMinElements() != null;
               if (value != null) {
                  String topLevelVarNameRegex = Pattern
                        .quote("\"${" + varName + "}\"");
                  String inlineVarNameRegex = Pattern
                        .quote("${" + varName + "}");
                  String topLevelReplacement = valueJsonString;
                  String inlineReplacement;
                  if (stringType && !setType) {
                     inlineReplacement = valueJsonString.substring(1,
                           valueJsonString.length() - 1);
                  }
                  else {
                     String quotedValueJsonString = JSONObject
                           .quote(valueJsonString);
                     inlineReplacement = quotedValueJsonString.substring(1,
                           quotedValueJsonString.length() - 1);
                  }
                  String inlineReplacementRegex = Matcher
                        .quoteReplacement(inlineReplacement);
                  String topLevelReplacementRegex = Matcher
                        .quoteReplacement(topLevelReplacement);
                  questionText = questionText.replaceAll(topLevelVarNameRegex,
                        topLevelReplacementRegex);
                  questionText = questionText.replaceAll(inlineVarNameRegex,
                        inlineReplacementRegex);
               }
            }
            return questionText;
         }
         return rawQuestionText;
      }
      catch (JSONException | IOException e) {
         throw new BatfishException(
               String.format("Could not convert raw question text [%s] to JSON",
                     rawQuestionText),
               e);
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
      for (ExternalBgpAdvertisementPlugin plugin : _externalBgpAdvertisementPlugins) {
         AdvertisementSet currentAdvertisements = plugin
               .loadExternalBgpAdvertisements();
         advertSet.addAll(currentAdvertisements);
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
    */
   public AdvertisementSet processExternalBgpAnnouncements(
         Map<String, Configuration> configurations,
         SortedSet<Long> allCommunities) {
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
            String ifaceName = p.getInterface();
            Configuration node = configurations.get(hostname);
            Interface iface = node.getInterfaces().get(ifaceName);
            if (iface == null) {
               throw new BatfishException(
                     "Cannot disable non-existent interface '" + ifaceName
                           + "' on node '" + hostname + "'\n");
            }
            else {
               iface.setActive(false);
               iface.setBlacklisted(true);
            }
         }
      }
   }

   private void processNodeBlacklist(
         Map<String, Configuration> configurations) {
      NodeSet blacklistNodes = getNodeBlacklist();
      if (blacklistNodes != null) {
         for (String hostname : blacklistNodes) {
            Configuration node = configurations.get(hostname);
            if (node != null) {
               for (Interface iface : node.getInterfaces().values()) {
                  iface.setActive(false);
                  iface.setBlacklisted(true);
               }
            }
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
      List<Path> configFilePaths = listAllFiles(configsPath);
      AtomicInteger completed = newBatch("Reading network configuration files",
            configFilePaths.size());
      for (Path file : configFilePaths) {
         _logger.debug("Reading: \"" + file.toString() + "\"\n");
         String fileTextRaw = CommonUtil.readFile(file.toAbsolutePath());
         String fileText = fileTextRaw
               + ((fileTextRaw.length() != 0) ? "\n" : "");
         configurationData.put(file, fileText);
         completed.incrementAndGet();
      }
      printElapsedTime();
      return configurationData;
   }

   @Override
   public String readExternalBgpAnnouncementsFile() {
      Path externalBgpAnnouncementsPath = _testrigSettings
            .getEnvironmentSettings().getExternalBgpAnnouncementsPath();
      if (Files.exists(externalBgpAnnouncementsPath)) {
         String externalBgpAnnouncementsFileContents = CommonUtil
               .readFile(externalBgpAnnouncementsPath);
         return externalBgpAnnouncementsFileContents;
      }
      else {
         return null;
      }
   }

   private SortedMap<Path, String> readFiles(Path directory,
         String description) {
      _logger.infof("\n*** READING FILES: %s ***\n", description);
      resetTimer();
      SortedMap<Path, String> fileData = new TreeMap<>();
      List<Path> filePaths;
      try (Stream<Path> paths = CommonUtil.list(directory)) {
         filePaths = paths
               .filter(path -> !path.getFileName().toString().startsWith("."))
               .sorted().collect(Collectors.toList());
      }
      AtomicInteger completed = newBatch("Reading files: " + description,
            filePaths.size());
      for (Path file : filePaths) {
         _logger.debug("Reading: \"" + file.toString() + "\"\n");
         String fileTextRaw = CommonUtil.readFile(file.toAbsolutePath());
         String fileText = fileTextRaw
               + ((fileTextRaw.length() != 0) ? "\n" : "");
         fileData.put(file, fileText);
         completed.incrementAndGet();
      }
      printElapsedTime();
      return fileData;
   }

   @Override
   public AnswerElement reducedReachability(HeaderSpace headerSpace) {
      if (SystemUtils.IS_OS_MAC_OSX) {
         // TODO: remove when z3 parallelism bug on OSX is fixed
         _settings.setSequential(true);
      }
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
         for (String vrf : baseConfigurations.get(node).getVrfs().keySet()) {
            Map<String, Set<String>> nodeVrfs = new TreeMap<>();
            nodeVrfs.put(node, Collections.singleton(vrf));
            ReachabilityQuerySynthesizer acceptQuery = new ReachabilityQuerySynthesizer(
                  Collections.singleton(ForwardingAction.ACCEPT), headerSpace,
                  Collections.<String> emptySet(), nodeVrfs);
            ReachabilityQuerySynthesizer notAcceptQuery = new ReachabilityQuerySynthesizer(
                  Collections.singleton(ForwardingAction.ACCEPT),
                  new HeaderSpace(), Collections.<String> emptySet(), nodeVrfs);
            notAcceptQuery.setNegate(true);
            NodeVrfSet nodes = new NodeVrfSet();
            nodes.add(new Pair<>(node, vrf));
            List<QuerySynthesizer> queries = new ArrayList<>();
            queries.add(acceptQuery);
            queries.add(notAcceptQuery);
            queries.add(blacklistQuery);
            CompositeNodJob job = new CompositeNodJob(settings, synthesizers,
                  queries, nodes, tag);
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
   public void registerAnswerer(String questionName, String questionClassName,
         BiFunction<Question, IBatfish, Answerer> answererCreator) {
      _answererCreators.put(questionName, answererCreator);
   }

   @Override
   public void registerBgpTablePlugin(BgpTableFormat format,
         BgpTablePlugin bgpTablePlugin) {
      _bgpTablePlugins.put(format, bgpTablePlugin);
   }

   @Override
   public void registerExternalBgpAdvertisementPlugin(
         ExternalBgpAdvertisementPlugin externalBgpAdvertisementPlugin) {
      _externalBgpAdvertisementPlugins.add(externalBgpAdvertisementPlugin);
   }

   private void repairConfigurations() {
      Path outputPath = _testrigSettings.getSerializeIndependentPath();
      CommonUtil.deleteDirectory(outputPath);
      ParseVendorConfigurationAnswerElement pvcae = loadParseVendorConfigurationAnswerElement();
      if (!Version.isCompatibleVersion("Service", "Old parsed configurations",
            pvcae.getVersion())) {
         repairVendorConfigurations();
      }
      Path inputPath = _testrigSettings.getSerializeVendorPath();
      serializeIndependentConfigs(inputPath, outputPath);
   }

   private void repairDataPlane() {
      Path dataPlanePath = _testrigSettings.getEnvironmentSettings()
            .getDataPlanePath();
      Path dataPlaneAnswerPath = _testrigSettings.getEnvironmentSettings()
            .getDataPlaneAnswerPath();
      CommonUtil.deleteIfExists(dataPlanePath);
      CommonUtil.deleteIfExists(dataPlaneAnswerPath);
      computeDataPlane(false);
   }

   private void repairEnvironmentBgpTables() {
      EnvironmentSettings envSettings = _testrigSettings
            .getEnvironmentSettings();
      Path answerPath = envSettings.getParseEnvironmentBgpTablesAnswerPath();
      Path bgpTablesOutputPath = envSettings
            .getSerializeEnvironmentBgpTablesPath();
      CommonUtil.deleteIfExists(answerPath);
      CommonUtil.deleteDirectory(bgpTablesOutputPath);
      computeEnvironmentBgpTables();
   }

   private void repairEnvironmentRoutingTables() {
      EnvironmentSettings envSettings = _testrigSettings
            .getEnvironmentSettings();
      Path answerPath = envSettings
            .getParseEnvironmentRoutingTablesAnswerPath();
      Path rtOutputPath = envSettings
            .getSerializeEnvironmentRoutingTablesPath();
      CommonUtil.deleteIfExists(answerPath);
      CommonUtil.deleteDirectory(rtOutputPath);
      computeEnvironmentRoutingTables();
   }

   private void repairVendorConfigurations() {
      Path outputPath = _testrigSettings.getSerializeVendorPath();
      CommonUtil.deleteDirectory(outputPath);
      Path testRigPath = _testrigSettings.getTestRigPath();
      serializeVendorConfigs(testRigPath, outputPath);
   }

   private AnswerElement report() {
      ReportAnswerElement answerElement = new ReportAnswerElement();
      checkQuestionsDirExists();
      Path questionsDir = _settings.getActiveTestrigSettings().getBasePath()
            .resolve(BfConsts.RELPATH_QUESTIONS_DIR);
      ConcurrentMap<Path, String> answers = new ConcurrentHashMap<>();
      try (DirectoryStream<Path> questions = Files
            .newDirectoryStream(questionsDir)) {
         questions.forEach(questionDirPath -> answers.put(
               questionDirPath.resolve(BfConsts.RELPATH_ANSWER_JSON),
               !questionDirPath.getFileName().startsWith(".") && Files.exists(
                     questionDirPath.resolve(BfConsts.RELPATH_ANSWER_JSON))
                           ? CommonUtil.readFile(questionDirPath
                                 .resolve(BfConsts.RELPATH_ANSWER_JSON))
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
      newBatch("Begin job", 0);
      loadPlugins();
      if (_dataPlanePlugin == null) {
         _dataPlanePlugin = new BdpDataPlanePlugin();
         _dataPlanePlugin.initialize(this);
      }
      JsonExternalBgpAdvertisementPlugin jsonExternalBgpAdvertisementsPlugin = new JsonExternalBgpAdvertisementPlugin();
      jsonExternalBgpAdvertisementsPlugin.initialize(this);
      _externalBgpAdvertisementPlugins.add(jsonExternalBgpAdvertisementsPlugin);
      boolean action = false;
      Answer answer = new Answer();

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

      if (_settings.getInitInfo()) {
         answer.addAnswerElement(initInfo(true, false, false));
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

      if (_settings.getAnalyze()) {
         answer.append(analyze());
         action = true;
      }

      if (_settings.getDataPlane()) {
         answer.append(computeDataPlane(_settings.getDiffActive()));
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

      _logger.info("\n*** SERIALIZING AWS CONFIGURATION STRUCTURES ***\n");
      resetTimer();
      outputPath.toFile().mkdirs();
      Path currentOutputPath = outputPath
            .resolve(BfConsts.RELPATH_AWS_VPC_CONFIGS_FILE);
      _logger.debug("Serializing AWS VPCs to " + currentOutputPath.toString()
            + "\"...");
      serializeObject(config, currentOutputPath);
      _logger.debug("OK\n");
      printElapsedTime();
      return answer;
   }

   private Answer serializeEnvironmentBgpTables(Path inputPath,
         Path outputPath) {
      Answer answer = new Answer();
      ParseEnvironmentBgpTablesAnswerElement answerElement = new ParseEnvironmentBgpTablesAnswerElement();
      answerElement.setVersion(Version.getVersion());
      answer.addAnswerElement(answerElement);
      SortedMap<String, BgpAdvertisementsByVrf> bgpTables = getEnvironmentBgpTables(
            inputPath, answerElement);
      serializeEnvironmentBgpTables(bgpTables, outputPath);
      serializeObject(answerElement, _testrigSettings.getEnvironmentSettings()
            .getParseEnvironmentBgpTablesAnswerPath());
      return answer;
   }

   private void serializeEnvironmentBgpTables(
         SortedMap<String, BgpAdvertisementsByVrf> bgpTables, Path outputPath) {
      if (bgpTables == null) {
         throw new BatfishException("Exiting due to parsing error(s)");
      }
      _logger.info("\n*** SERIALIZING ENVIRONMENT BGP TABLES ***\n");
      resetTimer();
      outputPath.toFile().mkdirs();
      SortedMap<Path, BgpAdvertisementsByVrf> output = new TreeMap<>();
      bgpTables.forEach((name, rt) -> {
         Path currentOutputPath = outputPath.resolve(name);
         output.put(currentOutputPath, rt);
      });
      serializeObjects(output);
      printElapsedTime();
   }

   private Answer serializeEnvironmentRoutingTables(Path inputPath,
         Path outputPath) {
      Answer answer = new Answer();
      ParseEnvironmentRoutingTablesAnswerElement answerElement = new ParseEnvironmentRoutingTablesAnswerElement();
      answerElement.setVersion(Version.getVersion());
      answer.addAnswerElement(answerElement);
      SortedMap<String, RoutesByVrf> routingTables = getEnvironmentRoutingTables(
            inputPath, answerElement);
      serializeEnvironmentRoutingTables(routingTables, outputPath);
      serializeObject(answerElement, _testrigSettings.getEnvironmentSettings()
            .getParseEnvironmentRoutingTablesAnswerPath());
      return answer;
   }

   private void serializeEnvironmentRoutingTables(
         SortedMap<String, RoutesByVrf> routingTables, Path outputPath) {
      if (routingTables == null) {
         throw new BatfishException("Exiting due to parsing error(s)");
      }
      _logger.info("\n*** SERIALIZING ENVIRONMENT ROUTING TABLES ***\n");
      resetTimer();
      outputPath.toFile().mkdirs();
      SortedMap<Path, RoutesByVrf> output = new TreeMap<>();
      routingTables.forEach((name, rt) -> {
         Path currentOutputPath = outputPath.resolve(name);
         output.put(currentOutputPath, rt);
      });
      serializeObjects(output);
      printElapsedTime();
   }

   private void serializeHostConfigs(Path testRigPath, Path outputPath,
         ParseVendorConfigurationAnswerElement answerElement) {
      Map<Path, String> configurationData = readConfigurationFiles(testRigPath,
            BfConsts.RELPATH_HOST_CONFIGS_DIR);
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
         _logger.info(
               "Serializing node-roles mappings: \"" + nodeRolesPath + "\"...");
         serializeObject(nodeRoles, nodeRolesPath);
         _logger.info("OK\n");
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
            String relativePathStr = _testrigSettings.getBasePath()
                  .relativize(path).toString();
            if (!iptablesConfigurations.containsKey(relativePathStr)) {
               for (String key : iptablesConfigurations.keySet()) {
                  _logger.errorf("key : %s\n", key);
               }
               throw new BatfishException(
                     "Key not found for iptables: " + relativePathStr);
            }
            hostConfig.setIptablesConfig(
                  (IptablesVendorConfiguration) iptablesConfigurations
                        .get(relativePathStr));
         }
      }

      // now, serialize
      _logger.info("\n*** SERIALIZING VENDOR CONFIGURATION STRUCTURES ***\n");
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

   private void serializeIndependentConfigs(
         Map<String, Configuration> configurations, Path outputPath) {
      if (configurations == null) {
         throw new BatfishException("Exiting due to conversion error(s)");
      }
      _logger.info(
            "\n*** SERIALIZING VENDOR-INDEPENDENT CONFIGURATION STRUCTURES ***\n");
      resetTimer();
      outputPath.toFile().mkdirs();
      Map<Path, Configuration> output = new TreeMap<>();
      configurations.forEach((name, c) -> {
         Path currentOutputPath = outputPath.resolve(name);
         output.put(currentOutputPath, c);
      });
      serializeObjects(output);
      printElapsedTime();
   }

   private Answer serializeIndependentConfigs(Path vendorConfigPath,
         Path outputPath) {
      Answer answer = new Answer();
      ConvertConfigurationAnswerElement answerElement = new ConvertConfigurationAnswerElement();
      answerElement.setVersion(Version.getVersion());
      if (_settings.getVerboseParse()) {
         answer.addAnswerElement(answerElement);
      }
      Map<String, Configuration> configurations = getConfigurations(
            vendorConfigPath, answerElement);
      serializeIndependentConfigs(configurations, outputPath);
      serializeObject(answerElement, _testrigSettings.getConvertAnswerPath());
      return answer;
   }

   private void serializeNetworkConfigs(Path testRigPath, Path outputPath,
         ParseVendorConfigurationAnswerElement answerElement) {
      Map<Path, String> configurationData = readConfigurationFiles(testRigPath,
            BfConsts.RELPATH_CONFIGURATIONS_DIR);
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
         _logger.info(
               "Serializing node-roles mappings: \"" + nodeRolesPath + "\"...");
         serializeObject(nodeRoles, nodeRolesPath);
         _logger.info("OK\n");
      }
      _logger.info("\n*** SERIALIZING VENDOR CONFIGURATION STRUCTURES ***\n");
      resetTimer();
      CommonUtil.createDirectories(outputPath);
      Map<Path, VendorConfiguration> output = new TreeMap<>();
      vendorConfigurations.forEach((name, vc) -> {
         if (name.contains(File.separator)) {
            // iptables will get a hostname like configs/iptables-save if they
            // are not set up correctly using host files
            _logger.errorf("Cannot serialize configuration with hostname %s\n",
                  name);
            answerElement
                  .addRedFlagWarning(name,
                        new Warning(
                              "Cannot serialize network config. Bad hostname "
                                    + name.replace("\\", "/"),
                              "MISCELLANEOUS"));
         }
         else {
            Path currentOutputPath = outputPath.resolve(name);
            output.put(currentOutputPath, vc);
         }
      });
      serializeObjects(output);
      printElapsedTime();
   }

   public <S extends Serializable> void serializeObjects(
         Map<Path, S> objectsByPath) {
      if (objectsByPath.isEmpty()) {
         return;
      }
      BatfishLogger logger = getLogger();
      Map<Path, byte[]> dataByPath = new ConcurrentHashMap<>();
      int size = objectsByPath.size();
      String className = objectsByPath.values().iterator().next().getClass()
            .getName();
      AtomicInteger serializeCompleted = newBatch(
            "Serializing '" + className + "' instances", size);
      objectsByPath.keySet().parallelStream().forEach(outputPath -> {
         S object = objectsByPath.get(outputPath);
         byte[] gzipData = toGzipData(object);
         dataByPath.put(outputPath, gzipData);
         serializeCompleted.incrementAndGet();
      });
      AtomicInteger writeCompleted = newBatch(
            "Packing and writing '" + className + "' instances to disk", size);
      dataByPath.forEach((outputPath, data) -> {
         logger.debug("Writing: \"" + outputPath.toString() + "\"...");
         try {
            Files.write(outputPath, data);
         }
         catch (IOException e) {
            throw new BatfishException(
                  "Failed to write: '" + outputPath.toString() + "'");
         }
         logger.debug("OK\n");
         writeCompleted.incrementAndGet();
      });
   }

   private Answer serializeVendorConfigs(Path testRigPath, Path outputPath) {
      Answer answer = new Answer();
      boolean configsFound = false;

      // look for network configs
      Path networkConfigsPath = testRigPath
            .resolve(BfConsts.RELPATH_CONFIGURATIONS_DIR);
      ParseVendorConfigurationAnswerElement answerElement = new ParseVendorConfigurationAnswerElement();
      answerElement.setVersion(Version.getVersion());
      if (_settings.getVerboseParse()) {
         answer.addAnswerElement(answerElement);
      }
      if (Files.exists(networkConfigsPath)) {
         serializeNetworkConfigs(testRigPath, outputPath, answerElement);
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
         serializeHostConfigs(testRigPath, outputPath, answerElement);
         configsFound = true;
      }

      if (!configsFound) {
         throw new BatfishException("No valid configurations found");
      }

      // serialize warnings
      serializeObject(answerElement, _testrigSettings.getParseAnswerPath());

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
      if (SystemUtils.IS_OS_MAC_OSX) {
         // TODO: remove when z3 parallelism bug on OSX is fixed
         _settings.setSequential(true);
      }
      Settings settings = getSettings();
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
         for (String ingressVrf : configurations.get(ingressNode).getVrfs()
               .keySet()) {
            Map<String, Set<String>> nodeVrfs = new TreeMap<>();
            nodeVrfs.put(ingressNode, Collections.singleton(ingressVrf));
            ReachabilityQuerySynthesizer query = new ReachabilityQuerySynthesizer(
                  actions, headerSpace, activeFinalNodes, nodeVrfs);
            NodeVrfSet nodes = new NodeVrfSet();
            nodes.add(new Pair<>(ingressNode, ingressVrf));
            NodJob job = new NodJob(settings, dataPlaneSynthesizer, query,
                  nodes, tag);
            jobs.add(job);
         }
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
         for (Entry<String, Interface> e : node.getInterfaces().entrySet()) {
            String ifaceName = e.getKey();
            Interface iface = e.getValue();
            if (!iface.isLoopback(node.getConfigurationFormat())
                  && iface.getActive()) {
               for (Prefix prefix : iface.getAllPrefixes()) {
                  if (prefix.getPrefixLength() < 32) {
                     Prefix network = new Prefix(prefix.getNetworkAddress(),
                           prefix.getPrefixLength());
                     NodeInterfacePair pair = new NodeInterfacePair(nodeName,
                           ifaceName);
                     Set<NodeInterfacePair> interfaceBucket = prefixInterfaces
                           .computeIfAbsent(network, k -> new HashSet<>());
                     interfaceBucket.add(pair);
                  }
               }
            }
         }
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

   @Override
   public void writeDataPlane(DataPlane dp, DataPlaneAnswerElement ae) {
      _cachedDataPlanes.put(_testrigSettings, dp);
      serializeObject(dp,
            _testrigSettings.getEnvironmentSettings().getDataPlanePath());
      serializeObject(ae,
            _testrigSettings.getEnvironmentSettings().getDataPlaneAnswerPath());
   }

   private void writeJsonAnswer(String structuredAnswerString,
         String prettyAnswerString) {
      Path questionPath = _settings.getQuestionPath();
      if (questionPath != null) {
         Path questionDir = questionPath.getParent();
         if (!Files.exists(questionDir)) {
            throw new BatfishException(
                  "Could not write JSON answer to question dir '"
                        + questionDir.toString()
                        + "' because it does not exist");
         }
         boolean diff = _settings.getDiffQuestion();
         String baseEnvName = _testrigSettings.getEnvironmentSettings()
               .getName();
         Path answerDir = questionDir.resolve(Paths
               .get(BfConsts.RELPATH_ENVIRONMENTS_DIR, baseEnvName).toString());
         if (diff) {
            String deltaTestrigName = _deltaTestrigSettings.getName();
            String deltaEnvName = _deltaTestrigSettings.getEnvironmentSettings()
                  .getName();
            answerDir = answerDir.resolve(Paths
                  .get(BfConsts.RELPATH_DELTA, deltaTestrigName, deltaEnvName)
                  .toString());
         }
         Path structuredAnswerPath = answerDir
               .resolve(BfConsts.RELPATH_ANSWER_JSON);
         Path prettyAnswerPath = answerDir
               .resolve(BfConsts.RELPATH_ANSWER_PRETTY_JSON);
         answerDir.toFile().mkdirs();
         CommonUtil.writeFile(structuredAnswerPath, structuredAnswerString);
         CommonUtil.writeFile(prettyAnswerPath, prettyAnswerString);
      }
   }

   private void writeJsonAnswerWithLog(String answerString,
         String structuredAnswerString, String prettyAnswerString) {
      Path jsonPath = _settings.getAnswerJsonPath();
      if (jsonPath != null) {
         CommonUtil.writeFile(jsonPath, answerString);
      }
      Path questionPath = _settings.getQuestionPath();
      if (questionPath != null) {
         Path questionDir = questionPath.getParent();
         if (!Files.exists(questionDir)) {
            throw new BatfishException(
                  "Could not write JSON answer to question dir '"
                        + questionDir.toString()
                        + "' because it does not exist");
         }
         boolean diff = _settings.getDiffQuestion();
         String baseEnvName = _testrigSettings.getEnvironmentSettings()
               .getName();
         Path answerDir = questionDir.resolve(Paths
               .get(BfConsts.RELPATH_ENVIRONMENTS_DIR, baseEnvName).toString());
         if (diff) {
            String deltaTestrigName = _deltaTestrigSettings.getName();
            String deltaEnvName = _deltaTestrigSettings.getEnvironmentSettings()
                  .getName();
            answerDir = answerDir.resolve(Paths
                  .get(BfConsts.RELPATH_DELTA, deltaTestrigName, deltaEnvName)
                  .toString());
         }
         Path structuredAnswerPath = answerDir
               .resolve(BfConsts.RELPATH_ANSWER_JSON);
         Path prettyAnswerPath = answerDir
               .resolve(BfConsts.RELPATH_ANSWER_PRETTY_JSON);
         answerDir.toFile().mkdirs();
         CommonUtil.writeFile(structuredAnswerPath, structuredAnswerString);
         CommonUtil.writeFile(prettyAnswerPath, prettyAnswerString);
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
