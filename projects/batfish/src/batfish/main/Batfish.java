package batfish.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import com.logicblox.bloxweb.client.ServiceClientException;
import com.logicblox.connect.Workspace.Relation;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import batfish.collections.EdgeSet;
import batfish.collections.FibMap;
import batfish.collections.FibRow;
import batfish.collections.FibSet;
import batfish.collections.NodeSet;
import batfish.collections.PolicyRouteFibIpMap;
import batfish.collections.PolicyRouteFibNodeMap;
import batfish.grammar.BatfishCombinedParser;
import batfish.grammar.ConfigurationLexer;
import batfish.grammar.ConfigurationParser;
import batfish.grammar.ParseTreePrettyPrinter;
import batfish.grammar.cisco.CiscoCombinedParser;
import batfish.grammar.cisco.controlplane.CiscoControlPlaneExtractor;
import batfish.grammar.juniper.FlatJuniperGrammarLexer;
import batfish.grammar.juniper.FlatJuniperGrammarParser;
import batfish.grammar.juniper.JuniperGrammarLexer;
import batfish.grammar.juniper.JuniperGrammarParser;
import batfish.grammar.topology.BatfishTopologyCombinedParser;
import batfish.grammar.topology.BatfishTopologyExtractor;
import batfish.grammar.topology.GNS3TopologyCombinedParser;
import batfish.grammar.topology.GNS3TopologyExtractor;
import batfish.grammar.topology.TopologyExtractor;
import batfish.grammar.z3.ConcretizerQueryResultCombinedParser;
import batfish.grammar.z3.ConcretizerQueryResultExtractor;
import batfish.grammar.z3.DatalogQueryResultCombinedParser;
import batfish.grammar.z3.DatalogQueryResultExtractor;
import batfish.grammar.semantics.SemanticsLexer;
import batfish.grammar.semantics.SemanticsParser;
import batfish.logic.LogicResourceLocator;
import batfish.logicblox.ConfigurationFactExtractor;
import batfish.logicblox.Facts;
import batfish.logicblox.LBInitializationException;
import batfish.logicblox.LBValueType;
import batfish.logicblox.LogicBloxFrontend;
import batfish.logicblox.PredicateInfo;
import batfish.logicblox.ProjectFile;
import batfish.logicblox.QueryException;
import batfish.logicblox.TopologyFactExtractor;
import batfish.representation.Configuration;
import batfish.representation.Edge;
import batfish.representation.Ip;
import batfish.representation.Topology;
import batfish.representation.VendorConfiguration;
import batfish.representation.VendorConversionException;
import batfish.representation.cisco.CiscoVendorConfiguration;
import batfish.representation.cisco.Interface;
import batfish.util.UrlZipExplorer;
import batfish.util.StringFilter;
import batfish.util.Util;
import batfish.z3.ConcretizerQuery;
import batfish.z3.MultipathInconsistencyQuerySynthesizer;
import batfish.z3.QuerySynthesizer;
import batfish.z3.Synthesizer;

public class Batfish {
   private static final String BASIC_FACTS_BLOCKNAME = "BaseFacts";
   private static final String EDGES_FILENAME = "edges";
   private static final String FIB_POLICY_ROUTE_NEXT_HOP_PREDICATE_NAME = "FibForwardPolicyRouteNextHopIp";
   private static final String FIB_PREDICATE_NAME = "FibNetworkForward";
   private static final String FIBS_FILENAME = "fibs";
   private static final String FIBS_POLICY_ROUTE_NEXT_HOP_FILENAME = "fibs-policy-route";
   private static final String SEPARATOR = System.getProperty("file.separator");
   private static final String STATIC_FACT_BLOCK_PREFIX = "libbatfish:";
   private static final String TOPOLOGY_FILENAME = "topology.net";
   private static final String TOPOLOGY_PREDICATE_NAME = "LanAdjacent";

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

   private List<LogicBloxFrontend> _lbFrontends;

   private PredicateInfo _predicateInfo;
   private Settings _settings;

   private long _timerCount;

   private File _tmpLogicDir;

   public Batfish(Settings settings) {
      _settings = settings;
      _lbFrontends = new ArrayList<LogicBloxFrontend>();
      _tmpLogicDir = null;
   }

   private void addProject(LogicBloxFrontend lbFrontend) {
      print(0, "\n*** ADDING PROJECT ***\n");
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
         error(0, result + "\n");
         quit(1);
      }
      print(1, "SUCCESS\n");
      printElapsedTime();
   }

   private void addStaticFacts(LogicBloxFrontend lbFrontend,
         List<String> blockNames) {
      print(0, "\n*** ADDING STATIC FACTS ***\n");
      resetTimer();
      for (String blockName : blockNames) {
         print(1, "Adding " + blockName + "...");
         String output = lbFrontend.execNamedBlock(STATIC_FACT_BLOCK_PREFIX
               + blockName);
         if (output == null) {
            print(1, "OK\n");
         }
         else {
            error(0, output + "\n");
            quit(1);
         }
      }
      print(1, "SUCCESS\n");
      printElapsedTime();
   }

   private void anonymizeConfigurations() {
      // TODO Auto-generated method stub

   }

   private void cleanupLogicDir() {
      if (_tmpLogicDir != null) {
         try {
            FileUtils.deleteDirectory(_tmpLogicDir);
         }
         catch (IOException e) {
            e.printStackTrace();
         }
         _tmpLogicDir = null;
      }
   }

   private void computeDataPlane(LogicBloxFrontend lbFrontend) {
      print(0, "\n*** COMPUTING DATA PLANE STRUCTURES ***\n");
      resetTimer();

      lbFrontend.initEntityTable();

      print(1, "Retrieving topology information from LogicBlox..");
      EdgeSet topologyEdges = getTopologyEdges(lbFrontend);
      print(1, "OK\n");

      String fibQualifiedName = _predicateInfo.getPredicateNames().get(
            FIB_PREDICATE_NAME);
      print(1, "Retrieving network FIB information from LogicBlox..");
      Relation fibNetwork = lbFrontend.queryPredicate(fibQualifiedName);
      print(1, "OK\n");

      String fibPolicyRouteNextHopQualifiedName = _predicateInfo
            .getPredicateNames().get(FIB_POLICY_ROUTE_NEXT_HOP_PREDICATE_NAME);
      print(1,
            "Retrieving ip FIB information from LogicBlox for policy-routing next-hop-ips..");
      Relation fibPolicyRouteNextHops = lbFrontend
            .queryPredicate(fibPolicyRouteNextHopQualifiedName);
      print(1, "OK\n");

      print(1, "Caclulating forwarding rules..");
      FibMap fibs = getRouteForwardingRules(fibNetwork, lbFrontend);
      PolicyRouteFibNodeMap policyRouteFibNodeMap = getPolicyRouteFibNodeMap(
            fibPolicyRouteNextHops, lbFrontend);
      print(1, "OK\n");

      Path fibsPath = Paths.get(_settings.getDataPlaneDir(), FIBS_FILENAME);
      Path fibsPolicyRoutePath = Paths.get(_settings.getDataPlaneDir(),
            FIBS_POLICY_ROUTE_NEXT_HOP_FILENAME);
      Path edgesPath = Paths.get(_settings.getDataPlaneDir(), EDGES_FILENAME);
      print(1, "Serializing fibs..");
      serializeObject(fibs, fibsPath.toFile());
      print(1, "OK\n");
      print(1, "Serializing policy route next hop interface map..");
      serializeObject(policyRouteFibNodeMap, fibsPolicyRoutePath.toFile());
      print(1, "OK\n");
      print(1, "Serializing toplogy edges..");
      serializeObject(topologyEdges, edgesPath.toFile());
      print(1, "OK\n");

      printElapsedTime();
   }

   private void concretize() {
      print(0, "\n*** GENERATING Z3 CONCRETIZER QUERIES ***\n");
      resetTimer();
      String concInPath = _settings.getConcretizerInputFilePath();

      print(1, "Reading z3 datalog query output file: \"" + concInPath + "\"..");
      File queryOutputFile = new File(concInPath);
      String queryOutputStr = readFile(queryOutputFile);
      print(1, "OK\n");

      DatalogQueryResultCombinedParser parser = new DatalogQueryResultCombinedParser(
            queryOutputStr);
      ParserRuleContext tree = parse(parser, concInPath);

      print(1, "Computing concretizer queries..");
      ParseTreeWalker walker = new ParseTreeWalker();
      DatalogQueryResultExtractor extractor = new DatalogQueryResultExtractor();
      walker.walk(extractor, tree);
      print(1, "OK\n");

      List<ConcretizerQuery> concretizerQueries = extractor
            .getConcretizerQueries();

      for (int i = 0; i < concretizerQueries.size(); i++) {
         ConcretizerQuery cq = concretizerQueries.get(i);
         String concQueryPath = _settings.getConcretizerOutputFilePath() + "-"
               + i + ".smt2";
         print(1, "Writing concretizer query file: \"" + concQueryPath + "\"..");
         writeFile(concQueryPath, cq.getText());
         print(1, "OK\n");
      }

      printElapsedTime();
   }

   private LogicBloxFrontend connect() {
      boolean assumedToExist = !_settings.createWorkspace();
      String workspaceMaster = _settings.getWorkspaceName();
      LogicBloxFrontend lbFrontend = null;
      try {
         lbFrontend = initFrontend(assumedToExist, workspaceMaster);
      }
      catch (LBInitializationException e) {
         error(0, ExceptionUtils.getStackTrace(e));
         quit(1);
      }
      return lbFrontend;
   }

   public Map<String, Configuration> deserializeConfigurations(
         String serializedConfigPath) {
      print(1,
            "\n*** DESERIALIZING VENDOR-INDEPENDENT CONFIGURATION STRUCTURES ***\n");
      resetTimer();
      Map<String, Configuration> configurations = new TreeMap<String, Configuration>();
      File dir = new File(serializedConfigPath);
      File[] serializedConfigs = dir.listFiles();
      for (File serializedConfig : serializedConfigs) {
         String name = serializedConfig.getName();
         print(2, "Reading config: \"" + serializedConfig + "\"");
         Object object = deserializeObject(serializedConfig);
         Configuration c = (Configuration) object;
         configurations.put(name, c);
         print(2, "...OK\n");
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
         if (_settings.getSerializeToText()) {
            XStream xstream = new XStream(new DomDriver("UTF-8"));
            ois = xstream.createObjectInputStream(fis);
         }
         else {
            ois = new ObjectInputStream(fis);
         }
         o = ois.readObject();
         ois.close();
      }
      catch (FileNotFoundException e) {
         e.printStackTrace();
         quit(1);
      }
      catch (IOException e) {
         e.printStackTrace();
         quit(1);
      }
      catch (ClassNotFoundException e) {
         e.printStackTrace();
         quit(1);
      }
      return o;
   }

   public Map<String, VendorConfiguration> deserializeVendorConfigurations(
         String serializedVendorConfigPath) {
      print(1, "\n*** DESERIALIZING VENDOR CONFIGURATION STRUCTURES ***\n");
      resetTimer();
      Map<String, VendorConfiguration> vendorConfigurations = new TreeMap<String, VendorConfiguration>();
      File dir = new File(serializedVendorConfigPath);
      File[] serializedConfigs = dir.listFiles();
      for (File serializedConfig : serializedConfigs) {
         String name = serializedConfig.getName();
         print(2, "Reading vendor config: \"" + serializedConfig + "\"");
         Object object = deserializeObject(serializedConfig);
         VendorConfiguration vc = (VendorConfiguration) object;
         vendorConfigurations.put(name, vc);
         print(2, "...OK\n");
      }
      printElapsedTime();
      return vendorConfigurations;
   }

   private void dumpFacts(Map<String, StringBuilder> factBins) {
      print(0, "\n*** DUMPING FACTS ***\n");
      resetTimer();
      Path factsDir = Paths.get(_settings.getDumpFactsDir());
      try {
         Files.createDirectories(factsDir);
         for (String factsFilename : factBins.keySet()) {
            String facts = factBins.get(factsFilename).toString();
            Path factsFilePath = factsDir.resolve(factsFilename);
            print(1, "Writing: \"" + factsFilePath.toAbsolutePath().toString()
                  + "\"\n");
            FileUtils.write(factsFilePath.toFile(), facts);
         }
      }
      catch (IOException e) {
         e.printStackTrace();
         quit(1);
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
         Map<String, Interface> sortedInterfaces = new TreeMap<String, Interface>();
         sortedInterfaces.putAll(config.getInterfaces());
         for (Interface iface : sortedInterfaces.values()) {
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

   public void error(int logLevel, String text) {
      if (_settings.getLogLevel() >= logLevel) {
         System.err.print(text);
         System.err.flush();
      }
   }

   private Map<String, String> extractPredicateSemantics(
         Map<String, String> logicFiles) {
      Map<String, String> predicateSemantics = new HashMap<String, String>();
      for (String absolutePath : logicFiles.keySet()) {
         String currentRules = logicFiles.get(absolutePath);
         ANTLRStringStream in = new ANTLRStringStream(currentRules);
         SemanticsLexer lexer = new SemanticsLexer(in);
         CommonTokenStream tokens = new CommonTokenStream(lexer);
         SemanticsParser parser = new SemanticsParser(tokens);
         print(2, "Parsing: \"" + absolutePath + "\"");
         try {
            predicateSemantics.putAll(parser.predicate_semantics());
         }
         catch (RecognitionException e) {
            print(2, " ...ERROR\n");
            e.printStackTrace();
            return null;
         }
         int numErrors = parser.getErrors().size() + lexer.getErrors().size();
         if (numErrors > 0) {
            error(0, " ..." + numErrors + " ERROR(S)\n");
            for (String msg : lexer.getErrors()) {
               error(2, "\tlexer: " + msg + "\n");
            }
            for (String msg : parser.getErrors()) {
               error(2, "\tparser: " + msg + "\n");
            }
            return null;
         }
         print(2, " ...OK\n");

      }
      return predicateSemantics;
   }

   private void genMultipathQueries() {
      print(0, "\n*** GENERATING MULTIPATH-INCONSISTENCY QUERIES ***\n");
      resetTimer();

      String mpiQueryBasePath = _settings.getMultipathInconsistencyQueryPath();
      String nodeSetPath = _settings.getNodeSetPath();
      String nodeSetTextPath = nodeSetPath + ".txt";

      print(1, "Reading node set from : \"" + nodeSetPath + "\"..");
      NodeSet nodes = (NodeSet) deserializeObject(new File(nodeSetPath));
      print(1, "OK\n");

      for (String hostname : nodes) {
         QuerySynthesizer synth = new MultipathInconsistencyQuerySynthesizer(
               hostname);
         String queryText = synth.getQueryText();
         String mpiQueryPath = mpiQueryBasePath + "-" + hostname + ".smt2";
         print(1, "Writing query to: \"" + mpiQueryPath + "\"..");
         writeFile(mpiQueryPath, queryText);
         print(1, "OK\n");
      }

      print(1, "Writing node lines for next stage..");
      StringBuilder sb = new StringBuilder();
      for (String node : nodes) {
         sb.append(node + "\n");
      }
      writeFile(nodeSetTextPath, sb.toString());
      print(1, "OK\n");

      printElapsedTime();
   }

   private void genZ3(Map<String, Configuration> configurations) {
      print(0, "\n*** GENERATING Z3 LOGIC ***\n");
      resetTimer();

      Path fibsPath = Paths.get(_settings.getDataPlaneDir(), FIBS_FILENAME);
      Path prFibsPath = Paths.get(_settings.getDataPlaneDir(),
            FIBS_POLICY_ROUTE_NEXT_HOP_FILENAME);
      Path edgesPath = Paths.get(_settings.getDataPlaneDir(), EDGES_FILENAME);

      print(1, "Deserializing destination route fibs..");
      FibMap fibs = (FibMap) deserializeObject(fibsPath.toFile());
      print(1, "OK\n");

      print(1, "Deserializing policy route fibs..");
      PolicyRouteFibNodeMap prFibs = (PolicyRouteFibNodeMap) deserializeObject(prFibsPath
            .toFile());
      print(1, "OK\n");

      print(1, "Deserializing toplogy edges..");
      EdgeSet topologyEdges = (EdgeSet) deserializeObject(edgesPath.toFile());
      print(1, "OK\n");

      print(1, "Synthesizing Z3 logic..");
      Synthesizer s = new Synthesizer(configurations, fibs, prFibs,
            topologyEdges, _settings.getSimplify());
      try {
         s.synthesize(_settings.getZ3File());
      }
      catch (IOException e) {
         error(1, "ERROR\n");
         e.printStackTrace();
         quit(1);
      }
      print(1, "OK\n");

      print(1, "Serializing node set..");
      NodeSet nodeSet = s.getNodeSet();
      serializeObject(nodeSet, new File(_settings.getNodeSetPath()));
      print(1, "OK\n");

      printElapsedTime();
   }

   public Map<String, Configuration> getConfigurations(
         String serializedVendorConfigPath) {
      Map<String, VendorConfiguration> vendorConfigurations = deserializeVendorConfigurations(serializedVendorConfigPath);
      Map<String, Configuration> configurations = parseConfigurations(vendorConfigurations);
      return configurations;
   }

   public void getDiff() {
      // Map<File, String> configurationData1 = readConfigurationFiles(_settings
      // .getTestRigPath());
      // Map<File, String> configurationData2 = readConfigurationFiles(_settings
      // .getSecondTestRigPath());
      //
      // List<Configuration> firstConfigurations =
      // parseConfigFiles(configurationData1);
      // if (firstConfigurations == null) {
      // quit(1);
      // }
      // List<Configuration> secondConfigurations =
      // parseConfigFiles(configurationData2);
      // if (secondConfigurations == null) {
      // quit(1);
      // }
      // if (firstConfigurations.size() != secondConfigurations.size()) {
      // System.out.println("Size MISMATCH");
      // quit(1);
      // }
      // Collections.sort(firstConfigurations);
      // Collections.sort(secondConfigurations);
      // boolean finalRes = true;
      // for (int i = 0; i < firstConfigurations.size(); i++) {
      // boolean res = (firstConfigurations.get(i).sameParseTree(
      // secondConfigurations.get(i), firstConfigurations.get(i)
      // .getName() + " MISMATCH"));
      // if (res == false) {
      // finalRes = false;
      // }
      // }
      // if (finalRes == true) {
      // System.out.println("MATCH");
      // }
   }

   private double getElapsedTime(long beforeTime) {
      long difference = System.currentTimeMillis() - beforeTime;
      double seconds = difference / 1000d;
      return seconds;
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
      lbFrontend.fillColumn(LBValueType.ENTITY_INDEX_IP, ipList,
            fibPolicyRouteNextHops.getColumns().get(1));
      List<String> interfaces = new ArrayList<String>();
      lbFrontend.fillColumn(LBValueType.ENTITY_REF_STRING, interfaces,
            fibPolicyRouteNextHops.getColumns().get(2));
      int size = nodeList.size();
      for (int i = 0; i < size; i++) {
         String node = nodeList.get(i);
         Ip ip = new Ip(ipList.get(i));
         String iface = interfaces.get(i);
         PolicyRouteFibIpMap ipMap = nodeMap.get(node);
         if (ipMap == null) {
            ipMap = new PolicyRouteFibIpMap();
            nodeMap.put(node, ipMap);
         }
         ipMap.put(ip, iface);
      }
      return nodeMap;
   }

   public PredicateInfo getPredicateInfo(Map<String, String> logicFiles) {
      // Get predicate semantics from rules file
      print(1, "\n*** PARSING PREDICATE SEMANTICS ***\n");
      resetTimer();
      Map<String, String> predicateSemantics = extractPredicateSemantics(logicFiles);
      if (predicateSemantics == null) {
         quit(1);
      }
      PredicateInfo predicateInfo = new PredicateInfo(predicateSemantics);
      printElapsedTime();
      return predicateInfo;
   }

   private FibMap getRouteForwardingRules(Relation installedRoutes,
         LogicBloxFrontend lbFrontend) {
      FibMap fibs = new FibMap();
      List<String> nameList = new ArrayList<String>();
      lbFrontend.fillColumn(LBValueType.ENTITY_REF_STRING, nameList,
            installedRoutes.getColumns().get(0));
      List<String> networkList = new ArrayList<String>();
      lbFrontend.fillColumn(LBValueType.ENTITY_INDEX_NETWORK, networkList,
            installedRoutes.getColumns().get(1));
      List<String> interfaces = new ArrayList<String>();
      lbFrontend.fillColumn(LBValueType.ENTITY_REF_STRING, interfaces,
            installedRoutes.getColumns().get(2));

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
            String networkString = networkList.get(i);
            Ip networkAddress = new Ip(
                  Util.getIpFromIpSubnetPair(networkString));
            int prefixLength = Util
                  .getPrefixLengthFromIpSubnetPair(networkString);
            String iface = interfaces.get(i);
            fibRows.add(new FibRow(networkAddress, prefixLength, iface));
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

   /**
    * Generates a topology object from inferred edges encoded in interface
    * descriptions.
    * 
    * @param configurations
    *           The vendor specific configurations.
    * @param includeExternal
    *           Whether to include edges to nodes for which configuration files
    *           were not supplied (used for debugging).
    * @return The inferred topology.
    */
   private Topology inferTopologyFromInterfaceDescriptions(
         Map<String, Configuration> configurations, boolean IncludeExternal) {
      // TODO Auto-generated method stub
      return null;
   }

   public LogicBloxFrontend initFrontend(boolean assumedToExist,
         String workspace) throws LBInitializationException {
      print(1, "\n*** STARTING CONNECTBLOX SESSION ***\n");
      resetTimer();
      LogicBloxFrontend lbFrontend = new LogicBloxFrontend(
            _settings.getConnectBloxHost(), _settings.getConnectBloxPort(),
            _settings.getSshPort(), workspace, assumedToExist);
      lbFrontend.initialize();
      if (!lbFrontend.connected()) {
         error(0,
               "Error connecting to ConnectBlox service. Please make sure service is running and try again.\n\n");
         quit(1);
      }
      print(1, "SUCCESS\n");
      printElapsedTime();
      _lbFrontends.add(lbFrontend);
      return lbFrontend;

   }

   private ParserRuleContext parse(BatfishCombinedParser<?, ?> parser) {
      ParserRuleContext tree = parser.parse();
      List<String> errors = parser.getErrors();
      int numErrors = errors.size();
      if (numErrors > 0) {
         error(1, numErrors + " ERROR(S)\n");
         for (int i = 0; i < numErrors; i++) {
            String prefix = "ERROR " + (i + 1) + ": ";
            String msg = errors.get(i);
            String prefixedMsg = Util.applyPrefix(prefix, msg);
            error(1, prefixedMsg + "\n");
         }
         quit(1);
      }
      else if (!_settings.printParseTree()) {
         print(1, "OK\n");
      }
      else {
         print(0, "OK, PRINTING PARSE TREE:\n");
         print(0, ParseTreePrettyPrinter.print(tree, parser.getParser())
               + "\n\n");
      }
      return tree;
   }

   private ParserRuleContext parse(BatfishCombinedParser<?, ?> parser,
         String filename) {
      print(1, "Parsing: \"" + filename + "\"..");
      return parse(parser);
   }

   private Map<String, Configuration> parseConfigurations(
         Map<String, VendorConfiguration> vendorConfigurations) {
      boolean processingError = false;
      Map<String, Configuration> configurations = new TreeMap<String, Configuration>();
      print(1,
            "\n*** CONVERTING VENDOR CONFIGURATIONS TO INDEPENDENT FORMAT ***\n");
      resetTimer();
      for (String name : vendorConfigurations.keySet()) {
         print(2, "Processing: \"" + name + "\"");
         VendorConfiguration vc = vendorConfigurations.get(name);
         try {
            Configuration config = vc.toVendorIndependentConfiguration();
            configurations.put(name, config);
         }
         catch (VendorConversionException e) {
            error(0, "...CONVERSION ERROR\n");
            error(0, ExceptionUtils.getStackTrace(e));
            if (_settings.exitOnParseError()) {
               return null;
            }
            else {
               processingError = true;
               continue;
            }
         }

         List<String> conversionWarnings = vc.getConversionWarnings();
         int numWarnings = conversionWarnings.size();
         if (numWarnings > 0) {
            print(2, "..." + numWarnings + " WARNING(S)\n");
            for (String warning : conversionWarnings) {
               print(2, "\tconverter: " + warning + "\n");
            }
         }
         else {
            print(2, " ...OK\n");
         }
      }
      if (processingError) {
         return null;
      }
      else {
         printElapsedTime();
         return configurations;
      }
   }

   private void parseFlowsFromConstraints(StringBuilder sb) {
      Path flowConstraintsDir = Paths.get(_settings.getFlowPath());
      File[] constraintsFiles = flowConstraintsDir.toFile().listFiles(
            new FilenameFilter() {
               @Override
               public boolean accept(File dir, String filename) {
                  return filename.matches(".*-concrete-.*.smt2.out");
               }
            });
      for (File constraintsFile : constraintsFiles) {
         String flowConstraintsText = readFile(constraintsFile);
         print(1, "Parsing: \"" + constraintsFile.toString() + "\"");
         ConcretizerQueryResultCombinedParser parser = new ConcretizerQueryResultCombinedParser(
               flowConstraintsText);
         ParserRuleContext tree = parse(parser, constraintsFile.toString());
         ParseTreeWalker walker = new ParseTreeWalker();
         ConcretizerQueryResultExtractor extractor = new ConcretizerQueryResultExtractor();
         walker.walk(extractor, tree);
         String node = extractor.getNode();
         if (node == null) {
            continue;
         }
         Map<String, Long> constraints = extractor.getConstraints();
         long src_ip = 0;
         long dst_ip = 0;
         long src_port = 0;
         long dst_port = 0;
         long protocol = 0;
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
         String line = node + "|" + src_ip + "|" + dst_ip + "|" + src_port
               + "|" + dst_port + "|" + protocol + "\n";
         sb.append(line);
      }
   }

   private void parseTopology(String testRigPath, String topologyFileText,
         Map<String, StringBuilder> factBins) {
      BatfishCombinedParser<?, ?> parser = null;
      TopologyExtractor extractor = null;
      Topology topology = null;
      File topologyPath = Paths.get(testRigPath, "topology.net").toFile();
      print(1, "Parsing: \"" + topologyPath.getAbsolutePath() + "\"");
      if (topologyFileText.startsWith("autostart")) {
         parser = new GNS3TopologyCombinedParser(topologyFileText);
         extractor = new GNS3TopologyExtractor();
      }
      else if (topologyFileText.startsWith("CONFIGPARSER_TOPOLOGY")) {
         parser = new BatfishTopologyCombinedParser(topologyFileText);
         extractor = new BatfishTopologyExtractor();
      }
      else if (topologyFileText.equals("")) {
         error(1, "...WARNING: empty topology\n");
         return;
      }
      else {
         error(0, "...ERROR\n");
         throw new Error("Topology format error");
      }
      ParserRuleContext tree = parse(parser);
      ParseTreeWalker walker = new ParseTreeWalker();
      walker.walk(extractor, tree);
      topology = extractor.getTopology();
      TopologyFactExtractor tfe = new TopologyFactExtractor(topology);
      tfe.writeFacts(factBins);
   }

   private Map<String, VendorConfiguration> parseVendorConfigurations(
         Map<File, String> configurationData) {
      print(1, "\n*** PARSING VENDOR CONFIGURATION FILES ***\n");
      resetTimer();
      Map<String, VendorConfiguration> vendorConfigurations = new TreeMap<String, VendorConfiguration>();

      boolean processingError = false;
      for (File currentFile : configurationData.keySet()) {
         String fileText = configurationData.get(currentFile);
         String currentPath = currentFile.getAbsolutePath();
         ConfigurationParser parser = null;
         ConfigurationLexer lexer = null;
         VendorConfiguration vc = null;
         ANTLRStringStream in = new ANTLRStringStream(fileText);
         CommonTokenStream tokens;
         if (fileText.length() == 0) {
            continue;
         }
         CiscoControlPlaneExtractor extractor = null;
         boolean antlr4 = false;
         if (fileText.charAt(0) == '!') {
            // antlr 4 stuff
            antlr4 = true;
            BatfishCombinedParser<?, ?> combinedParser = new CiscoCombinedParser(
                  fileText);
            ParserRuleContext tree = parse(combinedParser, currentPath);
            extractor = new CiscoControlPlaneExtractor(fileText,
                  combinedParser.getParser());
            ParseTreeWalker walker = new ParseTreeWalker();
            walker.walk(extractor, tree);
            for (String warning : extractor.getWarnings()) {
               error(2, warning);
            }
            vc = extractor.getVendorConfiguration();
            assert Boolean.TRUE;
         }
         else if ((fileText.indexOf("set version") >= 0)
               && ((fileText.indexOf("set version") == 0) || (fileText
                     .charAt(fileText.indexOf("set version") - 1) == '\n'))) {
            lexer = new FlatJuniperGrammarLexer(in);
            tokens = new CommonTokenStream(lexer);
            parser = new FlatJuniperGrammarParser(tokens);
         }
         else if (fileText.charAt(0) == '#') {
            lexer = new JuniperGrammarLexer(in);
            tokens = new CommonTokenStream(lexer);
            parser = new JuniperGrammarParser(tokens);
         }
         else {
            continue;
         }
         if (!antlr4) {
            print(2, "Parsing: \"" + currentPath + "\"");
            try {
               vc = parser.parse_configuration();
            }
            catch (Exception e) {
               error(0, " ...ERROR\n");
               e.printStackTrace();
            }
            List<String> parserErrors = parser.getErrors();
            List<String> lexerErrors = lexer.getErrors();
            int numErrors = parserErrors.size() + lexerErrors.size();
            if (numErrors > 0) {
               error(0, " ..." + numErrors + " ERROR(S)\n");
               for (String msg : lexer.getErrors()) {
                  error(2, "\tlexer: " + msg + "\n");
               }
               for (String msg : parser.getErrors()) {
                  error(2, "\tparser: " + msg + "\n");
               }
               if (_settings.exitOnParseError()) {
                  return null;
               }
               else {
                  processingError = true;
                  continue;
               }
            }
            else {
               print(2, "...OK\n");
            }
         }

         // at this point we should have a VendorConfiguration vc
         vendorConfigurations.put(vc.getHostname(), vc);
      }
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
      print(1, "\n*** EXTRACTING LOGICBLOX FACTS FROM CONFIGURATIONS ***\n");
      resetTimer();
      Set<Long> communities = new LinkedHashSet<Long>();
      for (Configuration c : configurations) {
         communities.addAll(c.getCommunities());
      }
      for (Configuration c : configurations) {
         ConfigurationFactExtractor cfe = new ConfigurationFactExtractor(c,
               communities, factBins);
         cfe.writeFacts();
      }
      printElapsedTime();
   }

   private void postFacts(LogicBloxFrontend lbFrontend,
         Map<String, StringBuilder> factBins) {
      print(1, "\n*** POSTING FACTS TO BLOXWEB SERVICES ***\n");
      resetTimer();
      String ret = lbFrontend.startBloxWebServices();
      if (ret != null) {
         error(0, ret + "\n");
         quit(1);
      }
      try {
         lbFrontend.postFacts(factBins);
      }
      catch (ServiceClientException e) {
         e.printStackTrace();
         quit(1);
      }
      ret = lbFrontend.stopBloxWebServices();
      if (ret != null) {
         error(0, ret + "\n");
         quit(1);
      }
      print(1, "SUCCESS\n");
      printElapsedTime();
   }

   public void print(int logLevel, String text) {
      if (_settings.getLogLevel() >= logLevel) {
         System.out.print(text);
         System.out.flush();
      }
   }

   private void printAllPredicateSemantics(
         Map<String, String> predicateSemantics) {
      // Get predicate semantics from rules file
      print(1, "\n*** PRINTING PREDICATE SEMANTICS ***\n");
      List<String> helpPredicates = getHelpPredicates(predicateSemantics);
      for (String predicate : helpPredicates) {
         printPredicateSemantics(predicate);
         print(0, "\n");
      }
   }

   private void printElapsedTime() {
      double seconds = getElapsedTime(_timerCount);
      print(1, "Time taken for this task: " + seconds + " seconds\n");
   }

   private void printPredicate(LogicBloxFrontend lbFrontend,
         String predicateName) {
      List<String> output;
      printPredicateSemantics(predicateName);
      String qualifiedName = _predicateInfo.getPredicateNames().get(
            predicateName);
      if (qualifiedName == null) { // predicate not found
         error(0, "ERROR: No information for predicate: " + predicateName
               + "\n");
         return;
      }
      Relation relation = lbFrontend.queryPredicate(qualifiedName);
      try {
         output = lbFrontend.getPredicate(_predicateInfo, relation,
               predicateName);
         for (String match : output) {
            print(0, match);
         }
      }
      catch (QueryException q) {
         error(0, q.getMessage() + "\n");
      }
   }

   private void printPredicateCount(LogicBloxFrontend lbFrontend,
         String predicateName) {
      int numRows = lbFrontend.queryPredicate(predicateName).getColumns()
            .get(0).size();
      String output = "|" + predicateName + "| = " + numRows + "\n";
      print(0, output);
   }

   public void printPredicateCounts(LogicBloxFrontend lbFrontend,
         Set<String> predicateNames) {
      // Print predicate(s) here
      print(0, "\n*** SUBMITTING QUERY(IES) ***\n");
      resetTimer();
      for (String predicateName : predicateNames) {
         printPredicateCount(lbFrontend, predicateName);
         // print(0, "\n");
      }
      printElapsedTime();
   }

   public void printPredicates(LogicBloxFrontend lbFrontend,
         Set<String> predicateNames) {
      // Print predicate(s) here
      print(0, "\n*** SUBMITTING QUERY(IES) ***\n");
      resetTimer();
      for (String predicateName : predicateNames) {
         printPredicate(lbFrontend, predicateName);
         print(0, "\n");
      }
      printElapsedTime();
   }

   private void printPredicateSemantics(String predicateName) {
      String semantics = _predicateInfo.getPredicateSemantics(predicateName);
      if (semantics == null) {
         semantics = "<missing>";
      }
      print(0, "Predicate: " + predicateName + "\n");
      print(0, "Semantics: " + semantics + "\n");
   }

   public void quit(int exitCode) {
      for (LogicBloxFrontend lbFrontend : _lbFrontends) {
         // Close backend threads
         if (lbFrontend != null && lbFrontend.connected()) {
            lbFrontend.close();
         }
      }
      System.exit(exitCode);
   }

   private Map<File, String> readConfigurationFiles(String testRigPath) {
      print(1, "\n*** READING CONFIGURATION FILES ***\n");
      resetTimer();
      Map<File, String> configurationData = new TreeMap<File, String>();
      File configsPath = Paths.get(testRigPath, "configs").toFile();
      File[] configFilePaths = configsPath.listFiles(new FilenameFilter() {
         @Override
         public boolean accept(File dir, String name) {
            return !name.startsWith(".");
         }
      });
      for (File file : configFilePaths) {
         print(2, "Reading: \"" + file.toString() + "\"\n");
         String fileText = readFile(file.getAbsoluteFile());
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
         e.printStackTrace();
         quit(1);
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
                  || filename.endsWith(locatorFilename);
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
            e.printStackTrace();
            quit(1);
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
            e.printStackTrace();
            quit(1);
         }
         return logicDirFile;
      }
   }

   private void revert(LogicBloxFrontend lbFrontend) {
      print(1, "\n*** REVERTING WORKSPACE ***\n");
      String workspaceName = new File(_settings.getTestRigPath()).getName();
      String branchName = _settings.getBranchName();
      print(2, "Reverting workspace: \"" + workspaceName + "\" to branch: \""
            + branchName + "\n");
      String errorResult = lbFrontend.revertDatabase(branchName);
      if (errorResult != null) {
         error(0, errorResult + "\n");
         quit(1);
      }
   }

   public void run() {
      if (_settings.redirectStdErr()) {
         System.setErr(System.out);
      }

      if (_settings.getZ3()) {
         Map<String, Configuration> configurations = deserializeConfigurations(_settings
               .getSerializeIndependentPath());
         genZ3(configurations);
         quit(0);
      }

      if (_settings.getAnonymize()) {
         anonymizeConfigurations();
         quit(0);
      }

      if (_settings.getGenerateMultipathInconsistencyQuery()) {
         genMultipathQueries();
         quit(0);
      }

      if (_settings.getSerializeVendor()) {
         String testRigPath = _settings.getTestRigPath();
         String outputPath = _settings.getSerializeVendorPath();
         serializeVendorConfigs(testRigPath, outputPath);
         quit(0);
      }

      if (_settings.dumpInterfaceDescriptions()) {
         String testRigPath = _settings.getTestRigPath();
         String outputPath = _settings.getDumpInterfaceDescriptionsPath();
         dumpInterfaceDescriptions(testRigPath, outputPath);
         quit(0);
      }

      if (_settings.getSerializeIndependent()) {
         String inputPath = _settings.getSerializeVendorPath();
         String outputPath = _settings.getSerializeIndependentPath();
         serializeIndependentConfigs(inputPath, outputPath);
         quit(0);
      }

      if (_settings.getDiff()) {
         getDiff();
         quit(0);
      }

      if (_settings.getConcretize()) {
         concretize();
         quit(0);
      }

      if (_settings.getQuery() || _settings.getPrintSemantics()
            || _settings.getDataPlane()) {
         Map<String, String> logicFiles = getSemanticsFiles();
         _predicateInfo = getPredicateInfo(logicFiles);
         // Print predicate semantics and quit if requested
         if (_settings.getPrintSemantics()) {
            printAllPredicateSemantics(_predicateInfo.getPredicateSemantics());
            quit(0);
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
         if (_settings.getDumpControlPlaneFacts()) {
            dumpFacts(cpFactBins);
         }
         if (!(_settings.getFacts() || _settings.createWorkspace())) {
            quit(0);
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
         quit(0);
      }

      // Create new workspace (will overwrite existing) if requested
      if (_settings.createWorkspace()) {
         addProject(lbFrontend);
         if (!_settings.getFacts()) {
            quit(0);
         }
      }

      // Post facts if requested
      if (_settings.getFacts()) {
         addStaticFacts(lbFrontend,
               Collections.singletonList(BASIC_FACTS_BLOCKNAME));
         postFacts(lbFrontend, cpFactBins);
         quit(0);
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
         quit(0);
      }

      if (_settings.getDataPlane()) {
         computeDataPlane(lbFrontend);
         quit(0);
      }

      Map<String, StringBuilder> trafficFactBins = null;
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
            quit(0);
         }
      }

      error(0, "No task performed! Run with -help flag to see usage\n");
      quit(1);
   }

   private void serializeIndependentConfigs(String vendorConfigPath,
         String outputPath) {
      Map<String, Configuration> configurations = getConfigurations(vendorConfigPath);
      print(1,
            "\n*** SERIALIZING VENDOR-INDEPENDENT CONFIGURATION STRUCTURES ***\n");
      resetTimer();
      for (String name : configurations.keySet()) {
         Configuration c = configurations.get(name);
         Path currentOutputPath = Paths.get(outputPath, name);
         print(2,
               "Serializing: \"" + name + "\" ==> \""
                     + currentOutputPath.toString() + "\"");
         serializeObject(c, currentOutputPath.toFile());
         print(2, " ...OK\n");
      }
      printElapsedTime();
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
      catch (FileNotFoundException e) {
         e.printStackTrace();
         quit(1);
      }
      catch (IOException e) {
         e.printStackTrace();
         quit(1);
      }
   }

   private void serializeVendorConfigs(String testRigPath, String outputPath) {
      Map<File, String> configurationData = readConfigurationFiles(testRigPath);
      Map<String, VendorConfiguration> vendorConfigurations = parseVendorConfigurations(configurationData);
      if (vendorConfigurations == null) {
         error(0, "Exiting due to parser errors\n");
         quit(1);
      }
      print(1, "\n*** SERIALIZING VENDOR CONFIGURATION STRUCTURES ***\n");
      resetTimer();
      new File(outputPath).mkdirs();
      for (String name : vendorConfigurations.keySet()) {
         VendorConfiguration vc = vendorConfigurations.get(name);
         Path currentOutputPath = Paths.get(outputPath, name);
         print(2,
               "Serializing: \"" + name + "\" ==> \""
                     + currentOutputPath.toString() + "\"");
         serializeObject(vc, currentOutputPath.toFile());
         print(2, " ...OK\n");
      }
      printElapsedTime();
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
         e.printStackTrace();
         quit(1);
      }
   }

   public void writeTopologyFacts(String testRigPath,
         Map<String, Configuration> configurations,
         Map<String, StringBuilder> factBins) {
      // TODO: Use flag to extract topology from interface descriptions.
      if (Boolean.FALSE) {
         Topology topology = inferTopologyFromInterfaceDescriptions(
               configurations, /* Include external nodes (debug) */true);
         // TODO: Get from flag.
         String topologyDotFile = "/home/david/Projects/usc-configs/topology/topology.dot";
         if (!topologyDotFile.isEmpty()) {
            try {
               FileOutputStream out = new FileOutputStream(topologyDotFile);
               topology.dumpDot(out);
               out.close();
            }
            catch (FileNotFoundException e) {
               error(0, "Unable to write topology dot-file.");
               e.printStackTrace();
               quit(1);
            }
            catch (IOException e) {
               // TODO Auto-generated catch block
               e.printStackTrace();
            }
         }
      }
      else {
         Path topologyFilePath = Paths.get(testRigPath, TOPOLOGY_FILENAME);
         // Get generated facts from topology file
         String topologyFileText = null;
         boolean guess = false;
         print(1, "*** PARSING TOPOLOGY ***\n");
         resetTimer();
         try {
            topologyFileText = FileUtils.readFileToString(topologyFilePath
                  .toFile());
         }
         catch (FileNotFoundException e) {
            // tell logicblox to guess adjacencies based on interface
            // subnetworks
            print(1,
                  "*** (GUESSING TOPOLOGY IN ABSENCE OF EXPLICIT FILE) ***\n");
            StringBuilder wGuessTopology = factBins.get("GuessTopology");
            wGuessTopology.append("1\n");
            guess = true;
         }
         catch (IOException e) {
            e.printStackTrace();
            quit(1);
         }
         if (!guess) {
            parseTopology(testRigPath, topologyFileText, factBins);
         }
      }
      printElapsedTime();
      /*
       * // flow sinks Path flowSinkPath = Paths.get(_settings.getTestRigPath(),
       * FLOW_SINK_FILENAME);
       * 
       * StringBuilder wSetFlowSinkInterface = factBins
       * .get("SetFlowSinkInterface"); if (Files.exists(flowSinkPath)) { try {
       * String flowSinkInterface = FileUtils.readFileToString(flowSinkPath
       * .toFile()); wSetFlowSinkInterface.append(flowSinkInterface); } catch
       * (IOException e) { e.printStackTrace(); quit(1); } }
       */
   }

   private void writeTrafficFacts(Map<String, StringBuilder> factBins) {
      StringBuilder wSetFlowOriginate = factBins.get("SetFlowOriginate");
      parseFlowsFromConstraints(wSetFlowOriginate);
   }
}
