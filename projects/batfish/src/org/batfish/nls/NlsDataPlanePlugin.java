package org.batfish.nls;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.batfish.common.BatfishException;
import org.batfish.common.BatfishLogger;
import org.batfish.common.CleanBatfishException;
import org.batfish.common.util.CommonUtil;
import org.batfish.common.util.StringFilter;
import org.batfish.common.util.UrlZipExplorer;
import org.batfish.datamodel.AsPath;
import org.batfish.datamodel.AsSet;
import org.batfish.datamodel.BgpAdvertisement;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowTrace;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.LBValueType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Route;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.answers.Answer;
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
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.collections.NodeIpPair;
import org.batfish.datamodel.collections.PolicyRouteFibIpMap;
import org.batfish.datamodel.collections.PolicyRouteFibNodeMap;
import org.batfish.datamodel.collections.PredicateSemantics;
import org.batfish.datamodel.collections.PredicateValueTypeMap;
import org.batfish.datamodel.collections.QualifiedNameMap;
import org.batfish.datamodel.collections.RouteSet;
import org.batfish.grammar.logicblox.LogQLPredicateInfoExtractor;
import org.batfish.grammar.logicblox.LogiQLCombinedParser;
import org.batfish.grammar.logicblox.LogiQLPredicateInfoResolver;
import org.batfish.logic.LogicResourceLocator;
import org.batfish.main.Batfish;
import org.batfish.main.Warnings;
import org.batfish.main.Settings.EnvironmentSettings;
import org.batfish.main.Settings.TestrigSettings;
import org.batfish.plugin.DataPlanePlugin;

public final class NlsDataPlanePlugin extends DataPlanePlugin {

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

   private static final String IBGP_NEIGHBORS_PREDICATE_NAME = "IbgpNeighbors";

   private static final String INSTALLED_ROUTE_PREDICATE_NAME = "InstalledRoute";

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

   public static void initTrafficFactBins(Map<String, StringBuilder> factBins) {
      initFactBins(Facts.TRAFFIC_FACT_COLUMN_HEADERS, factBins);
   }

   private final Map<TestrigSettings, EntityTable> _entityTables;

   private PredicateInfo _predicateInfo;

   private File _tmpLogicDir;

   public NlsDataPlanePlugin(Batfish batfish) {
      super(batfish);
      _entityTables = new HashMap<>();
   }

   /**
    * This function extracts predicate type information from the logic files. It
    * is meant only to be called during the build process, and should never be
    * executed from a jar
    */
   public void buildPredicateInfo() {
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
         logicBinDirPath = Paths.get(LogicResourceLocator.class.getClassLoader()
               .getResource(logicPackageResourceName).toURI());
      }
      catch (URISyntaxException e) {
         throw new BatfishException("Failed to resolve logic output directory",
               e);
      }
      Path logicSrcDirPath = Paths.get(_settings.getLogicSrcDir());
      final Set<Path> logicFiles = new TreeSet<>();
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
      List<ParserRuleContext> trees = new ArrayList<>();
      for (Path logicFilePath : logicFiles) {
         String input = CommonUtil.readFile(logicFilePath);
         LogiQLCombinedParser parser = new LogiQLCombinedParser(input,
               _settings);
         ParserRuleContext tree = _batfish.parse(parser,
               logicFilePath.toString());
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
      Path predicateInfoFile = logicBinDirPath.resolve(PREDICATE_INFO_FILENAME);
      _batfish.serializeObject(predicateInfo, predicateInfoFile);
   }

   private void checkComputeControlPlaneFacts() {
      _batfish.checkConfigurations();
      _batfish.checkEnvironmentExists(_batfish.getBaseTestrigSettings());
      if (_settings.getDiffActive()) {
         _batfish.checkDataPlane(_batfish.getBaseTestrigSettings());
         _batfish.checkDiffEnvironmentExists();
      }
   }

   private void checkComputeNlsRelations(TestrigSettings testrigSettings) {
      checkControlPlaneFacts(testrigSettings);
   }

   private void checkControlPlaneFacts(TestrigSettings testrigSettings) {
      EnvironmentSettings envSettings = testrigSettings
            .getEnvironmentSettings();
      if (!Files.exists(envSettings.getControlPlaneFactsDir())) {
         throw new CleanBatfishException(
               "Missing control plane facts for testrig: \""
                     + testrigSettings.getName() + "\", environment: \""
                     + envSettings.getName() + "\"\n");
      }
   }

   private void checkDataPlaneFacts(TestrigSettings testrigSettings) {
      EnvironmentSettings envSettings = testrigSettings
            .getEnvironmentSettings();
      _batfish.checkEnvironmentExists(testrigSettings);
      if (!Files.exists(envSettings.getNlsDataPlaneOutputDir())) {
         throw new CleanBatfishException(
               "Missing computed data plane facts for environment: "
                     + envSettings.getName() + "\n");
      }
   }

   private void checkQuery(TestrigSettings testrigSettings,
         Set<String> predicateNames) {
      Set<String> dpIntersect = new HashSet<>();
      dpIntersect.addAll(predicateNames);
      dpIntersect.retainAll(getNlsDataPlaneOutputSymbols());
      if (dpIntersect.size() > 0) {
         checkDataPlaneFacts(testrigSettings);
      }
      Set<String> trafficIntersect = new HashSet<>();
      trafficIntersect.addAll(predicateNames);
      trafficIntersect.retainAll(getNlsTrafficOutputSymbols());
      if (trafficIntersect.size() > 0) {
         checkTrafficFacts(testrigSettings);
      }
   }

   private void checkTrafficFacts(TestrigSettings testrigSettings) {
      _batfish.checkEnvironmentExists(testrigSettings);
      EnvironmentSettings envSettings = testrigSettings
            .getEnvironmentSettings();
      if (!Files.exists(envSettings.getNlsTrafficOutputDir())) {
         throw new CleanBatfishException(
               "Missing computed traffic facts for environment: "
                     + envSettings.getName() + "\n");
      }
   }

   public void cleanupLogicDir() {
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

   public void clearEntityTables() {
      _entityTables.clear();
   }

   private void computeControlPlaneFacts(Map<String, StringBuilder> cpFactBins,
         boolean differentialContext, TestrigSettings testrigSettings) {
      checkComputeControlPlaneFacts();
      if (_settings.getUsePrecomputedRoutes()) {
         List<Path> precomputedRoutesPaths = _settings
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
      Map<String, Configuration> configurations = _batfish
            .loadConfigurations(testrigSettings);
      CommunitySet allCommunities = new CommunitySet();
      AdvertisementSet advertSet = _batfish.processExternalBgpAnnouncements(
            configurations, testrigSettings, allCommunities);
      populatePrecomputedBgpAdvertisements(advertSet, cpFactBins);
      Topology topology = _batfish.computeTopology(configurations,
            testrigSettings);
      InterfaceSet flowSinks = _batfish.computeFlowSinks(configurations,
            testrigSettings, differentialContext, topology);
      writeTopologyFacts(topology, cpFactBins);
      populateConfigurationFactBins(configurations.values(), allCommunities,
            cpFactBins);
      writeFlowSinkFacts(flowSinks, cpFactBins);
      if (!_logger.isActive(BatfishLogger.LEVEL_INFO)) {
         _logger.output("Facts generated successfully.\n");
      }
      dumpControlPlaneFacts(testrigSettings, cpFactBins);
      // serialize topology
      Path serializedTopologyPath = testrigSettings.getEnvironmentSettings()
            .getSerializedTopologyPath();
      _logger.info("Serializing topology...");
      _batfish.serializeObject(topology, serializedTopologyPath);
      _logger.info("OK\n");
   }

   @Override
   public Answer computeDataPlane(TestrigSettings testrigSettings,
         boolean differentialContext) {
      Map<String, StringBuilder> cpFactBins = new LinkedHashMap<>();
      initControlPlaneFactBins(cpFactBins);
      computeControlPlaneFacts(cpFactBins, differentialContext,
            testrigSettings);
      nlsDataPlane(testrigSettings);
      checkDataPlaneFacts(testrigSettings);
      EnvironmentSettings envSettings = testrigSettings
            .getEnvironmentSettings();
      Path dataPlanePath = envSettings.getDataPlanePath();
      if (dataPlanePath == null) {
         throw new BatfishException("Missing path to data plane");
      }
      _batfish.writeDataPlane(dataPlanePath, testrigSettings);
      // TODO: possibly change line below
      return new Answer();
   }

   private void dumpControlPlaneFacts(TestrigSettings testrigSettings,
         Map<String, StringBuilder> factBins) {
      _logger.info("\n*** DUMPING CONTROL PLANE FACTS ***\n");
      dumpFacts(factBins,
            testrigSettings.getEnvironmentSettings().getControlPlaneFactsDir());
   }

   private void dumpFacts(Map<String, StringBuilder> factBins, Path factsDir) {
      _batfish.resetTimer();
      try {
         CommonUtil.createDirectories(factsDir);
         for (String factsFilename : factBins.keySet()) {
            String[] factsLines = factBins.get(factsFilename).toString()
                  .split("\n");
            Set<String> uniqueFacts = new TreeSet<>();
            for (int i = 1; i < factsLines.length; i++) {
               uniqueFacts.add(factsLines[i]);
            }
            StringBuilder factsBuilder = new StringBuilder();
            factsBuilder.append(factsLines[0] + "\n");
            for (String factsLine : uniqueFacts) {
               factsBuilder.append(factsLine + "\n");
            }
            String facts = factsBuilder.toString();
            Path factsFilePath = factsDir.resolve(factsFilename);
            _logger.info("Writing: \""
                  + factsFilePath.toAbsolutePath().toString() + "\"\n");
            FileUtils.write(factsFilePath.toFile(), facts);
         }
      }
      catch (IOException e) {
         throw new BatfishException("Failed to write fact dump file(s)", e);
      }
      _batfish.printElapsedTime();
   }

   public void dumpTrafficFacts(Map<String, StringBuilder> factBins) {
      dumpTrafficFacts(factBins, _batfish.getTestrigSettings());
   }

   public void dumpTrafficFacts(Map<String, StringBuilder> factBins,
         TestrigSettings testrigSettings) {
      _logger.info("\n*** DUMPING TRAFFIC FACTS ***\n");
      dumpFacts(factBins,
            testrigSettings.getEnvironmentSettings().getTrafficFactsDir());
   }

   @Override
   public AdvertisementSet getAdvertisements(TestrigSettings testrigSettings) {
      checkDataPlaneFacts(testrigSettings);
      AdvertisementSet adverts = new AdvertisementSet();
      EntityTable entityTable = initEntityTable(testrigSettings);
      Relation relation = getRelation(testrigSettings,
            BGP_ADVERTISEMENT_PREDICATE_NAME);
      List<BgpAdvertisement> advertList = relation.getColumns().get(0)
            .asBgpAdvertisementList(entityTable);
      adverts.addAll(advertList);
      return adverts;
   }

   @Override
   public InterfaceSet getFlowSinkSet(TestrigSettings testrigSettings) {
      InterfaceSet flowSinks = new InterfaceSet();
      Relation relation = getRelation(testrigSettings,
            FLOW_SINK_PREDICATE_NAME);
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

   private List<String> getHelpPredicates(
         Map<String, String> predicateSemantics) {
      Set<String> helpPredicateSet = new LinkedHashSet<>();
      _settings.getHelpPredicates();
      if (_settings.getHelpPredicates() == null) {
         helpPredicateSet.addAll(predicateSemantics.keySet());
      }
      else {
         helpPredicateSet.addAll(_settings.getHelpPredicates());
      }
      List<String> helpPredicates = new ArrayList<>();
      helpPredicates.addAll(helpPredicateSet);
      Collections.sort(helpPredicates);
      return helpPredicates;
   }

   @Override
   public List<Flow> getHistoryFlows(TestrigSettings testrigSettings) {
      checkTrafficFacts(testrigSettings);
      EntityTable entityTable = initEntityTable(testrigSettings);
      Relation relation = getRelation(testrigSettings,
            FLOW_HISTORY_PREDICATE_NAME);
      List<Flow> flows = relation.getColumns().get(0).asFlowList(entityTable);
      return flows;
   }

   @Override
   public List<FlowTrace> getHistoryFlowTraces(
         TestrigSettings testrigSettings) {
      Relation relation = getRelation(testrigSettings,
            FLOW_HISTORY_PREDICATE_NAME);
      List<String> historyLines = relation.getColumns().get(1).asStringList();
      List<FlowTrace> flowTraces = historyLines.stream()
            .map(historyLine -> new FlowTrace(historyLine))
            .collect(Collectors.toList());
      return flowTraces;
   }

   @Override
   public IbgpTopology getIbgpNeighbors(TestrigSettings testrigSettings) {
      checkDataPlaneFacts(testrigSettings);
      IbgpTopology topology = new IbgpTopology();
      Relation relation = getRelation(testrigSettings,
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

   private Set<String> getNlsDataPlaneOutputSymbols() {
      Set<String> symbols = new HashSet<>();
      symbols.addAll(NlsConstants.NLS_DATA_PLANE_OUTPUT_SYMBOLS);
      if (_settings.getNlsDebugSymbols()) {
         symbols.addAll(NlsConstants.NLS_DATA_PLANE_OUTPUT_DEBUG_SYMBOLS);
      }
      return symbols;
   }

   private String[] getNlsLogicFilenames(File logicDir) {
      final Set<String> filenames = new TreeSet<>();
      Path logicDirPath = Paths.get(logicDir.toString());
      FileVisitor<Path> nlsLogicFileCollector = new SimpleFileVisitor<Path>() {
         @Override
         public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
               throws IOException {

            String fileStr;
            if (SystemUtils.IS_OS_WINDOWS) {
               fileStr = "\\\\?\\" + file.toString();
            }
            else {
               fileStr = file.toString();
            }
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

   private String getNlsText(Path nlsOutputDir, String relationName) {
      Path relationFile = nlsOutputDir.resolve(relationName);
      String content = CommonUtil.readFile(relationFile);
      return content;
   }

   private String getNlsText(TestrigSettings testrigSettings,
         String relationName) {
      Path nlsOutputDir;
      EnvironmentSettings envSettings = testrigSettings
            .getEnvironmentSettings();
      if (getNlsDataPlaneOutputSymbols().contains(relationName)) {
         nlsOutputDir = envSettings.getNlsDataPlaneOutputDir();
      }
      else if (getNlsTrafficOutputSymbols().contains(relationName)) {
         nlsOutputDir = envSettings.getNlsTrafficOutputDir();
      }
      else {
         throw new BatfishException(
               "Predicate: \"" + relationName + "\" not an output symbol");
      }
      return getNlsText(nlsOutputDir, relationName);
   }

   private Set<String> getNlsTrafficOutputSymbols() {
      Set<String> symbols = new HashSet<>();
      symbols.addAll(NlsConstants.NLS_TRAFFIC_OUTPUT_SYMBOLS);
      if (_settings.getNlsDebugSymbols()) {
         symbols.addAll(NlsConstants.NLS_TRAFFIC_OUTPUT_DEBUG_SYMBOLS);
      }
      return symbols;
   }

   @Override
   public PolicyRouteFibNodeMap getPolicyRouteFibNodeMap(
         TestrigSettings testrigSettings) {
      PolicyRouteFibNodeMap nodeMap = new PolicyRouteFibNodeMap();
      Relation relation = getRelation(testrigSettings,
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

   public PredicateInfo getPredicateInfo() {
      return _predicateInfo;
   }

   private Path getPredicateInfoPath() {
      File logicDir = retrieveLogicDir();
      return Paths.get(logicDir.toString(), PREDICATE_INFO_FILENAME);
   }

   private Relation getRelation(TestrigSettings testrigSettings,
         String predicateName) {
      String nlsText = getNlsText(testrigSettings, predicateName);
      Relation relation = new Relation.Builder(predicateName)
            .build(_predicateInfo, nlsText);
      return relation;
   }

   @Override
   public FibMap getRouteForwardingRules(TestrigSettings testrigSettings) {
      FibMap fibs = new FibMap();
      Relation relation = getRelation(testrigSettings, FIB_PREDICATE_NAME);
      EntityTable entityTable = initEntityTable(testrigSettings);
      List<String> nameList = relation.getColumns().get(0).asStringList();
      List<Prefix> networkList = relation.getColumns().get(1)
            .asPrefixList(entityTable);
      List<String> interfaceList = relation.getColumns().get(2).asStringList();
      List<String> nextHopList = relation.getColumns().get(3).asStringList();
      List<String> nextHopIntList = relation.getColumns().get(4).asStringList();

      String currentHostname = "";
      Map<String, Integer> startIndices = new HashMap<>();
      Map<String, Integer> endIndices = new HashMap<>();
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

   @Override
   public RouteSet getRoutes(TestrigSettings testrigSettings) {
      checkDataPlaneFacts(testrigSettings);
      RouteSet routes = new RouteSet();
      EntityTable entityTable = initEntityTable(testrigSettings);
      Relation relation = getRelation(testrigSettings,
            INSTALLED_ROUTE_PREDICATE_NAME);
      List<Route> routeList = relation.getColumns().get(0)
            .asRouteList(entityTable);
      routes.addAll(routeList);
      return routes;
   }

   private EntityTable initEntityTable(TestrigSettings testrigSettings) {
      EntityTable entityTable = _entityTables.get(testrigSettings);
      if (entityTable == null) {
         EnvironmentSettings envSettings = testrigSettings
               .getEnvironmentSettings();
         Map<String, String> nlsPredicateContents = new HashMap<>();
         Path nlsDataPlaneOutputDir = envSettings.getNlsDataPlaneOutputDir();
         Path nlsTrafficOutputDir = envSettings.getNlsTrafficOutputDir();
         if (nlsDataPlaneOutputDir != null
               && Files.exists(nlsDataPlaneOutputDir)) {
            nlsPredicateContents.putAll(readFacts(nlsDataPlaneOutputDir,
                  NlsConstants.NLS_DATA_PLANE_ENTITY_SYMBOLS));
         }
         if (nlsTrafficOutputDir != null && Files.exists(nlsTrafficOutputDir)) {
            nlsPredicateContents.putAll(readFacts(nlsTrafficOutputDir,
                  NlsConstants.NLS_TRAFFIC_ENTITY_SYMBOLS));
         }
         entityTable = new EntityTable(nlsPredicateContents, _predicateInfo);
         _entityTables.put(testrigSettings, entityTable);
      }
      return entityTable;
   }

   @Override
   public void initialize() {
      throw new BatfishException(
            "This plugin is not meant to be initialized dynamically");
   }

   public void initPredicateInfo() {
      _predicateInfo = loadPredicateInfo();
   }

   public PredicateInfo loadPredicateInfo() {
      // Get predicate semantics from rules file
      _logger.info("\n*** PARSING PREDICATE INFO ***\n");
      _batfish.resetTimer();
      Path predicateInfoPath = getPredicateInfoPath();
      PredicateInfo predicateInfo = (PredicateInfo) _batfish
            .deserializeObject(predicateInfoPath);
      _batfish.printElapsedTime();
      return predicateInfo;
   }

   private Answer nlsDataPlane(TestrigSettings testrigSettings) {
      EnvironmentSettings envSettings = testrigSettings
            .getEnvironmentSettings();
      Map<String, String> inputFacts = readFacts(
            envSettings.getControlPlaneFactsDir(),
            NlsConstants.NLS_DATA_PLANE_COMPUTATION_FACTS);
      writeNlsInput(getNlsDataPlaneOutputSymbols(), inputFacts,
            envSettings.getNlsDataPlaneInputFile(), testrigSettings);
      Answer answer = runNls(envSettings.getNlsDataPlaneInputFile(),
            envSettings.getNlsDataPlaneOutputDir());
      if (!_settings.getNlsDry()) {
         _batfish.writeRoutes(envSettings.getPrecomputedRoutesPath(),
               testrigSettings);
      }
      return answer;
   }

   public void nlsTraffic() {
      if (_settings.getDiffQuestion()) {
         nlsTraffic(_batfish.getBaseTestrigSettings());
         nlsTraffic(_batfish.getDeltaTestrigSettings());
      }
      else {
         nlsTraffic(_batfish.getTestrigSettings());
      }
   }

   public Answer nlsTraffic(TestrigSettings testrigSettings) {
      EnvironmentSettings envSettings = testrigSettings
            .getEnvironmentSettings();
      writeNlsPrecomputedRoutes(testrigSettings);
      Map<String, String> inputControlPlaneFacts = readFacts(
            envSettings.getControlPlaneFactsDir(),
            NlsConstants.NLS_TRAFFIC_COMPUTATION_CONTROL_PLANE_FACTS);
      Map<String, String> inputFlowFacts = readFacts(
            envSettings.getTrafficFactsDir(),
            NlsConstants.NLS_TRAFFIC_COMPUTATION_FLOW_FACTS);
      Map<String, String> inputFacts = new TreeMap<>();
      inputFacts.putAll(inputControlPlaneFacts);
      inputFacts.putAll(inputFlowFacts);
      writeNlsInput(getNlsTrafficOutputSymbols(), inputFacts,
            envSettings.getNlsTrafficInputFile(), testrigSettings);
      Answer answer = runNls(envSettings.getNlsTrafficInputFile(),
            envSettings.getNlsTrafficOutputDir());
      return answer;
   }

   private void populateConfigurationFactBins(
         Collection<Configuration> configurations, CommunitySet allCommunities,
         Map<String, StringBuilder> factBins) {
      _logger.info("\n*** EXTRACTING FACTS FROM CONFIGURATIONS ***\n");
      _batfish.resetTimer();
      for (Configuration c : configurations) {
         allCommunities.addAll(c.getCommunities());
      }
      Set<Ip> interfaceIps = new HashSet<>();
      Set<Ip> externalBgpRemoteIps = new TreeSet<>();
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
               if (neighborPrefix
                     .getPrefixLength() == Prefix.MAX_PREFIX_LENGTH) {
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
            Batfish.logWarnings(_logger, warnings);
         }
      }
      if (processingError) {
         throw new BatfishException(
               "Failed to extract facts from vendor-indpendent configuration structures");
      }
      _batfish.printElapsedTime();
   }

   private void populatePrecomputedBgpAdvertisements(AdvertisementSet advertSet,
         Map<String, StringBuilder> cpFactBins) {
      StringBuilder adverts = cpFactBins
            .get(PRECOMPUTED_BGP_ADVERTISEMENTS_PREDICATE_NAME);
      StringBuilder advertCommunities = cpFactBins
            .get(PRECOMPUTED_BGP_ADVERTISEMENT_COMMUNITY_PREDICATE_NAME);
      StringBuilder advertPaths = cpFactBins
            .get(PRECOMPUTED_BGP_ADVERTISEMENT_AS_PATH_PREDICATE_NAME);
      StringBuilder advertPathLengths = cpFactBins
            .get(PRECOMPUTED_BGP_ADVERTISEMENT_AS_PATH_LENGTH_PREDICATE_NAME);
      StringBuilder wNetworks = cpFactBins.get(NETWORKS_PREDICATE_NAME);
      Set<Prefix> networks = new HashSet<>();
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
         Path precomputedBgpAdvertisementsPath,
         Map<String, StringBuilder> cpFactBins) {
      AdvertisementSet rawAdvertSet = (AdvertisementSet) _batfish
            .deserializeObject(precomputedBgpAdvertisementsPath);
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

   private void populatePrecomputedIbgpNeighbors(
         Path precomputedIbgpNeighborsPath,
         Map<String, StringBuilder> cpFactBins) {
      StringBuilder sb = cpFactBins
            .get(PRECOMPUTED_IBGP_NEIGHBORS_PREDICATE_NAME);
      IbgpTopology topology = (IbgpTopology) _batfish
            .deserializeObject(precomputedIbgpNeighborsPath);
      for (IpEdge edge : topology) {
         String node1 = edge.getNode1();
         long ip1 = edge.getIp1().asLong();
         String node2 = edge.getNode2();
         long ip2 = edge.getIp2().asLong();
         sb.append(node1 + "|" + ip1 + "|" + node2 + "|" + ip2 + "\n");
      }
   }

   private void populatePrecomputedRoutes(List<Path> precomputedRoutesPaths,
         Map<String, StringBuilder> cpFactBins) {
      StringBuilder sb = cpFactBins.get(PRECOMPUTED_ROUTES_PREDICATE_NAME);
      StringBuilder wNetworks = cpFactBins.get(NETWORKS_PREDICATE_NAME);
      Set<Prefix> networks = new HashSet<>();
      for (Path precomputedRoutesPath : precomputedRoutesPaths) {
         RouteSet routes = (RouteSet) _batfish
               .deserializeObject(precomputedRoutesPath);
         for (Route route : routes) {
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

   public void printAllPredicateSemantics(
         Map<String, String> predicateSemantics) {
      // Get predicate semantics from rules file
      _logger.info("\n*** PRINTING PREDICATE SEMANTICS ***\n");
      List<String> helpPredicates = getHelpPredicates(predicateSemantics);
      for (String predicate : helpPredicates) {
         printPredicateSemantics(predicate);
         _logger.info("\n");
      }
   }

   private void printPredicate(TestrigSettings testrigSettings,
         String predicateName) {
      boolean function = _predicateInfo.isFunction(predicateName);
      StringBuilder sb = new StringBuilder();
      EntityTable entityTable = initEntityTable(testrigSettings);
      Relation relation = getRelation(testrigSettings, predicateName);
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

   public void printPredicates(TestrigSettings testrigSettings,
         Set<String> predicateNames) {
      // Print predicate(s) here
      _logger.info("\n*** SUBMITTING QUERY(IES) ***\n");
      _batfish.resetTimer();
      for (String predicateName : predicateNames) {
         printPredicate(testrigSettings, predicateName);
      }
      _batfish.printElapsedTime();
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

   @Override
   public void processFlows(Set<Flow> flows, TestrigSettings testrigSettings) {
      Map<String, StringBuilder> trafficFactBins = new LinkedHashMap<>();
      initTrafficFactBins(trafficFactBins);
      StringBuilder wSetFlowOriginate = trafficFactBins.get("SetFlowOriginate");
      for (Flow flow : flows) {
         wSetFlowOriginate.append(flow.toLBLine());
      }
      dumpTrafficFacts(trafficFactBins, testrigSettings);
      nlsTraffic(testrigSettings);
   }

   public void query() {
      Map<String, String> allPredicateNames = _predicateInfo
            .getPredicateNames();
      Set<String> predicateNames = new TreeSet<>();
      if (_settings.getQueryAll()) {
         predicateNames.addAll(allPredicateNames.keySet());
      }
      else {
         predicateNames.addAll(_settings.getPredicates());
      }
      checkQuery(_batfish.getTestrigSettings(), predicateNames);
      printPredicates(_batfish.getTestrigSettings(), predicateNames);
   }

   private Map<String, String> readFacts(Path factsDir, Set<String> factNames) {
      Map<String, String> inputFacts = new TreeMap<>();
      for (String factName : factNames) {
         Path factFile = factsDir.resolve(factName);
         String contents = CommonUtil.readFile(factFile);
         inputFacts.put(factName, contents);
      }
      return inputFacts;
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

   private Answer runNls(Path nlsInputFile, Path nlsOutputDir) {
      Answer answer = new Answer();
      _logger.info("\n*** RUNNING NLS ***\n");
      _batfish.resetTimer();
      File logicDir = retrieveLogicDir();
      String[] logicFilenames = getNlsLogicFilenames(logicDir);
      String nlsOutputDirStr;
      String nlsInputFileStr;
      if (SystemUtils.IS_OS_WINDOWS) {
         nlsOutputDirStr = "\\\\?\\" + nlsOutputDir.toString();
         nlsInputFileStr = "\\\\?\\" + nlsInputFile.toString();
      }
      else {
         nlsOutputDirStr = nlsOutputDir.toString();
         nlsInputFileStr = nlsInputFile.toString();
      }
      DefaultExecutor executor = new DefaultExecutor();
      ByteArrayOutputStream outStream = new ByteArrayOutputStream();
      ByteArrayOutputStream errStream = new ByteArrayOutputStream();
      executor.setStreamHandler(new PumpStreamHandler(outStream, errStream));
      executor.setExitValue(0);
      CommandLine cmdLine = new CommandLine(NLS_COMMAND);
      cmdLine.addArgument("-dir");
      cmdLine.addArgument(nlsOutputDirStr);
      cmdLine.addArgument("-rev-lookup");
      cmdLine.addArgument("-mcc");
      cmdLine.addArgument(nlsInputFileStr);
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
      _batfish.printElapsedTime();
      return answer;
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

   private void writeNlsInput(Set<String> outputSymbols,
         Map<String, String> inputFacts, Path nlsInputFile,
         TestrigSettings testrigSettings) {
      checkComputeNlsRelations(testrigSettings);
      StringBuilder sb = new StringBuilder();
      sb.append("output_symbols([");
      List<String> outputSymbolsList = new ArrayList<>();
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
         int numValueTypes = valueTypes.size();
         String[] lines = contents.split("\n");
         for (int i = 1; i < lines.length; i++) {
            sb.append("'" + predicateName + "'(");
            String line = lines[i];
            String[] parts = line.split(lineDelimiter);
            int numParts = parts.length;
            if (numParts != numValueTypes) {
               throw new BatfishException(
                     "Input to predicate '" + predicateName + "' has "
                           + numParts + " parts, but schema indicates it is "
                           + numValueTypes + "-ary");
            }
            for (int j = 0; j < numParts; j++) {
               String part = parts[j];
               boolean isNum;
               LBValueType currentValueType = valueTypes.get(j);
               if (currentValueType == null) {
                  throw new BatfishException("In predicate '" + predicateName
                        + "', missing type for argument in (0-based) position "
                        + j);
               }
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

   private void writeNlsPrecomputedRoutes(TestrigSettings testrigSettings) {
      EnvironmentSettings envSettings = testrigSettings
            .getEnvironmentSettings();
      Path precomputedRoutesPath = envSettings.getPrecomputedRoutesPath();
      Map<String, StringBuilder> prFactBins = new HashMap<>();
      initControlPlaneFactBins(prFactBins);
      Set<String> prPredicates = new HashSet<>();
      prPredicates.add(PRECOMPUTED_ROUTES_PREDICATE_NAME);
      prPredicates.add(NETWORKS_PREDICATE_NAME);
      prFactBins.keySet().retainAll(prPredicates);
      populatePrecomputedRoutes(
            Collections.singletonList(precomputedRoutesPath), prFactBins);
      dumpFacts(prFactBins, envSettings.getTrafficFactsDir());
   }

   private void writeTopologyFacts(Topology topology,
         Map<String, StringBuilder> factBins) {
      TopologyFactExtractor tfe = new TopologyFactExtractor(topology);
      tfe.writeFacts(factBins);
   }

}
