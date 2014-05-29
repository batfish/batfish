package batfish.ucla;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.apache.commons.io.FileUtils;

import batfish.grammar.ConfigurationLexer;
import batfish.grammar.cisco.CiscoGrammarLexer;
import batfish.grammar.cisco.CiscoGrammarParser;
import batfish.main.Batfish;
import batfish.main.Settings;
import batfish.representation.Ip;
import batfish.representation.cisco.BgpPeerGroup;
import batfish.representation.cisco.BgpProcess;
import batfish.representation.cisco.CiscoVendorConfiguration;
import batfish.representation.cisco.Interface;
import batfish.representation.cisco.PrefixList;
import batfish.util.Util;

public class DeptGenerator {
   public static final String FAKE_INTERFACE_PREFIX = "TenGigabitEthernet200/";
   public static final String FLOW_SINK_INTERFACE_PREFIX = "TenGigabitEthernet100/";
   private Batfish _batfish;
   private Map<String, DeptRouter> _deptRouters;
   private String _separator;
   private Settings _settings;

   private List<Set<DeptRouter>> _subgroups;

   public DeptGenerator(Batfish batfish, Settings settings, String separator) {
      _batfish = batfish;
      _settings = settings;
      _separator = separator;
      _deptRouters = new HashMap<String, DeptRouter>();
      _subgroups = new ArrayList<Set<DeptRouter>>();
   }

   public void createSubgroupTestRigs() {
      for (int i = 0; i < _subgroups.size(); i++) {
         Set<DeptRouter> subgroup = _subgroups.get(i);
         Path testRigPath = Paths.get(_settings.getTestRigPath());
         String testRigName = testRigPath.getFileName().toString();
         String subgroupTestRigName = testRigName + "-subgroup-"
               + String.format("%02d", i);
         Path subgroupTestRigPath = Paths.get(_settings.getTestRigPath(),
               subgroupTestRigName);
         Path subgroupConfigPath = Paths.get(subgroupTestRigPath.toString(),
               "configs");
         try {
            // FileUtils.copyDirectory(configPath.toFile(),
            // subgroupConfigPath.toFile());
            Files.createDirectories(subgroupConfigPath);
            writeDeptRouters(subgroup, subgroupConfigPath.toString());
            writeFlowFile(subgroup, subgroupTestRigPath.toString());
         }
         catch (IOException e) {
            e.printStackTrace();
            quit(1);
         }
      }

   }

   private void error(int logLevel, String text) {
      _batfish.error(logLevel, text);
   }

   public void generateDeptRouters() {
      List<CiscoVendorConfiguration> configs = parseDistributionRouters(_settings
            .getTestRigPath());
      if (configs == null) {
         error(0, "quitting due to parse error\n");
         quit(1);
      }
      for (CiscoVendorConfiguration config : configs) {
         BgpProcess proc = config.getBgpProcess();
         BgpPeerGroup department = proc.getPeerGroup("department");
         if (department == null) {
            continue;
         }
         for (String neighborName : department.getNeighborAddresses()) {
            if (!proc.getActivatedNeighbors().contains(neighborName)) {
               continue;
            }
            BgpPeerGroup pg = proc.getPeerGroup(neighborName);
            int remoteAs = pg.getRemoteAS();
            String deptName = "dpt_" + remoteAs;
            DeptRouter deptRouter = _deptRouters.get(deptName);
            if (deptRouter == null) {
               deptRouter = new DeptRouter(deptName, remoteAs);
               _deptRouters.put(deptName, deptRouter);
            }
            DistDeptPeering peering = new DistDeptPeering();
            deptRouter.getPeerings().add(peering);
            long deptIntIpLong = Util.ipToLong(neighborName);
            String distrIp = null;
            String distrSubnet = null;
            for (Interface i : config.getInterfaces()) {
               if (i.getIP() == null) {
                  continue;
               }
               long intIpLong = Util.ipToLong(i.getIP());
               long intSubLong = Util.ipToLong(i.getSubnetMask());
               int intSubBits = Util.numSubnetBits(i.getSubnetMask());
               long network_start = intIpLong & intSubLong;
               long network_end = Util.getNetworkEnd(network_start, intSubBits);
               if (deptIntIpLong >= network_start
                     && deptIntIpLong <= network_end) {
                  // we found the distribution interface
                  distrIp = i.getIP();
                  distrSubnet = i.getSubnetMask();
                  break;
               }
            }
            if (distrIp == null) {
               throw new Error("could not find interface for peering");
            }
            peering.setDistIp(distrIp);
            peering.setDistName(config.getHostname());
            peering.setIp(neighborName);
            peering.setSubnet(distrSubnet);
            String distrPrefixListName = pg.getInboundPrefixList();
            if (distrPrefixListName != null) {
               PrefixList distrPrefixList = config.getRouteFilter().get(
                     distrPrefixListName);
               if (distrPrefixList != null) {
                  peering.setPrefixList(distrPrefixList);
               }
            }
         }
      }

      // compute department networks and flow ip equivalence classes
      for (DeptRouter router : _deptRouters.values()) {
         router.computeDeptNetworks();
      }

      // compute subgroups and subgroup networks
      int maxSubgroupSize = _settings.getMaxSubgroupSize();
      Set<DeptRouter> currentSubgroup = new TreeSet<DeptRouter>();
      _subgroups.add(currentSubgroup);
      Map<Ip, Ip> subgroupNetworks = new TreeMap<Ip, Ip>();
      for (DeptRouter router : _deptRouters.values()) {
         if (currentSubgroup.size() == maxSubgroupSize) {
            currentSubgroup = new TreeSet<DeptRouter>();
            _subgroups.add(currentSubgroup);
            subgroupNetworks = new TreeMap<Ip, Ip>();
         }
         currentSubgroup.add(router);
         subgroupNetworks.putAll(router.getDeptNetworks());
         router.setSubgroupNetworks(subgroupNetworks);
      }

   }

   private List<CiscoVendorConfiguration> parseDistributionRouters(
         String testRigPath) {
      List<CiscoVendorConfiguration> configs = new ArrayList<CiscoVendorConfiguration>();
      List<String> configFiles = new ArrayList<String>();
      File configsPath = new File(testRigPath + _separator + "configs");
      File[] configFilePaths = configsPath.listFiles(new FilenameFilter() {
         @Override
         public boolean accept(File dir, String name) {
            return name.startsWith("dr");
         }
      });
      for (File file : configFilePaths) {
         configFiles.add(readFile(file.getAbsoluteFile()));
      }

      boolean processingError = false;
      for (int currentPathIndex = 0; currentPathIndex < configFiles.size(); currentPathIndex++) {
         String fileText = configFiles.get(currentPathIndex);
         ANTLRStringStream in = new ANTLRStringStream(fileText);
         ConfigurationLexer lexer = new CiscoGrammarLexer(in);
         CommonTokenStream tokens = new CommonTokenStream(lexer);
         CiscoGrammarParser parser = new CiscoGrammarParser(tokens);
         CiscoVendorConfiguration vc = null;
         if (fileText.length() == 0) {
            continue;
         }
         String currentPath = configFilePaths[currentPathIndex]
               .getAbsolutePath();
         print(2, "Parsing: \"" + currentPath + "\"");
         try {
            vc = (CiscoVendorConfiguration) parser.parse_configuration();
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
         print(2, "...OK\n");
         configs.add(vc);
      }
      if (processingError) {
         return null;
      }
      else {
         return configs;
      }
   }

   private void print(int logLevel, String text) {
      _batfish.print(logLevel, text);
   }

   private void quit(int code) {
      _batfish.quit(code);
   }

   private String readFile(File file) {
      return _batfish.readFile(file);
   }

   private void writeDeptRouters(Set<DeptRouter> routers, String directory)
         throws IOException {
      for (DeptRouter router : routers) {
         String routerName = router.getName();
         String fileName = routerName + ".conf";
         Path filePath = Paths.get(directory, fileName);
         String routerConfig = router.toConfigString();
         print(2, "Writing: \"" + filePath.toAbsolutePath().toString() + "\"\n");
         FileUtils.writeStringToFile(filePath.toFile(), routerConfig);
      }
   }

   private void writeFlowFile(Set<DeptRouter> routers, String testRigPath)
         throws IOException {
      StringBuilder flowSinks = new StringBuilder();
      flowSinks.append("dc_stub" + "|" + FLOW_SINK_INTERFACE_PREFIX + "0\n");
      flowSinks.append("hpr_stub" + "|" + FLOW_SINK_INTERFACE_PREFIX + "0\n");
      for (DeptRouter router : routers) {
         String routerName = router.getName();
         for (int i = 0; i < router.getNumFlowSinkInterfaces(); i++) {
            String flowSinkInterface = FLOW_SINK_INTERFACE_PREFIX + i;
            flowSinks.append(routerName + "|" + flowSinkInterface + "\n");
         }
      }

      // Remove trailing newline
      int flen = flowSinks.length();
      flowSinks.delete(flen - 1, flen);

      String fileName = "flow_sinks";
      Path filePath = Paths.get(testRigPath, fileName);

      print(2, "Writing: \"" + filePath.toAbsolutePath().toString() + "\"\n");
      FileUtils.writeStringToFile(filePath.toFile(), flowSinks.toString());
   }

}
