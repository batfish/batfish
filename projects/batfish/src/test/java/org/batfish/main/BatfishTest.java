package org.batfish.main;

import static org.batfish.main.Batfish.postProcessInterfaceDependencies;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import org.batfish.common.Answerer;
import org.batfish.common.BatfishException;
import org.batfish.common.BfConsts;
import org.batfish.common.topology.Layer1Edge;
import org.batfish.common.topology.Layer1Node;
import org.batfish.common.topology.Layer1Topology;
import org.batfish.common.topology.TopologyUtil;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.AsPathAccessList;
import org.batfish.datamodel.AuthenticationKeyChain;
import org.batfish.datamodel.CommunityList;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.IkePolicy;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Interface.Dependency;
import org.batfish.datamodel.Interface.DependencyType;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Ip6AccessList;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpsecPolicy;
import org.batfish.datamodel.IpsecProposal;
import org.batfish.datamodel.IpsecVpn;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Route6FilterList;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.Zone;
import org.batfish.datamodel.answers.Answer;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.answers.AnswerStatus;
import org.batfish.datamodel.answers.ParseStatus;
import org.batfish.datamodel.answers.ParseVendorConfigurationAnswerElement;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.questions.TestQuestion;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.identifiers.AnalysisId;
import org.batfish.identifiers.NetworkId;
import org.batfish.identifiers.QuestionId;
import org.batfish.identifiers.QuestionSettingsId;
import org.batfish.identifiers.TestIdResolver;
import org.batfish.job.ParseVendorConfigurationResult;
import org.batfish.referencelibrary.AddressGroup;
import org.batfish.referencelibrary.ReferenceBook;
import org.batfish.referencelibrary.ReferenceLibrary;
import org.batfish.representation.host.HostConfiguration;
import org.batfish.storage.TestStorageProvider;
import org.batfish.vendor.VendorConfiguration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

/** Tests for {@link Batfish}. */
public class BatfishTest {

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Rule public ExpectedException _thrown = ExpectedException.none();

  private static final Question TEST_QUESTION =
      new TestQuestion() {
        @Override
        public String getName() {
          return "blah";
        }
      };

  @Test
  public void testAnswerBadQuestion() throws IOException {
    // missing class field
    Batfish batfish =
        BatfishTestUtils.getBatfish(
            new TestStorageProvider() {
              @Override
              public String loadQuestion(
                  NetworkId network, QuestionId analysis, AnalysisId question) {
                return "{"
                    + "\"differential\": false,"
                    + "\"instance\": {"
                    + "\"description\": \"Outputs cases where undefined structures (e.g., ACL, routemaps) "
                    + "are referenced.\","
                    + "\"instanceName\": \"undefinedReferences\","
                    + "\"longDescription\": \"Such occurrences indicate configuration errors and can have"
                    + "serious consequences with some vendors.\","
                    + "\"tags\": [\"default\"],"
                    + "\"variables\": {\"nodeRegex\": {"
                    + "\"description\": \"Only check nodes whose name matches this regex\","
                    + "\"type\": \"javaRegex\","
                    + "\"value\": \".*\""
                    + "}}"
                    + "},"
                    + "\"nodeRegex\": \"${nodeRegex}\""
                    + "}";
              }
            },
            new TestIdResolver());
    Answer answer = batfish.answer();
    assertThat(answer.getQuestion(), is(nullValue()));
    assertEquals(answer.getStatus(), AnswerStatus.FAILURE);
    assertEquals(answer.getAnswerElements().size(), 1);
    assertThat(
        answer.getAnswerElements().get(0).prettyPrint(),
        containsString("Could not parse question"));
  }

  @Test
  public void testOverlayIptables() throws IOException {
    SortedMap<String, String> configurationsText = new TreeMap<>();
    String[] configurationNames = new String[] {"host1.cfg"};
    String testConfigsPrefix = "org/batfish/grammar/host/testrigs/router-iptables/configs/";

    SortedMap<String, String> hostsText = new TreeMap<>();
    String[] hostNames = new String[] {"host1.json"};
    String testHostsPrefix = "org/batfish/grammar/host/testrigs/router-iptables/hosts/";

    SortedMap<String, String> iptablesFilesText = new TreeMap<>();
    String[] iptablesNames = new String[] {"host1.iptables"};
    String testIptablesPrefix = "org/batfish/grammar/host/testrigs/router-iptables/iptables/";

    for (String configurationName : configurationNames) {
      String configurationText = CommonUtil.readResource(testConfigsPrefix + configurationName);
      configurationsText.put(configurationName, configurationText);
    }
    for (String hostName : hostNames) {
      String hostText = CommonUtil.readResource(testHostsPrefix + hostName);
      hostsText.put(hostName, hostText);
    }
    for (String iptablesName : iptablesNames) {
      String iptablesText = CommonUtil.readResource(testIptablesPrefix + iptablesName);
      iptablesFilesText.put(iptablesName, iptablesText);
    }
    Batfish batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder()
                .setConfigurationText(configurationsText)
                .setHostsText(hostsText)
                .setIptablesFilesText(iptablesFilesText)
                .build(),
            _folder);
    Map<String, Configuration> configurations = batfish.loadConfigurations();
    assertThat(
        configurations.get("host1").getAllInterfaces().get("Ethernet0").getIncomingFilterName(),
        is(notNullValue()));
  }

  @Test
  public void testInitTestrigWithDuplicateHostnames() throws IOException {
    // rtr1 and rtr2 have the same hostname
    String testrigResourcePrefix = "org/batfish/main/snapshots/duplicate_hostnames";
    List<String> configurationNames = ImmutableList.of("rtr1", "rtr2", "rtr3");

    Batfish batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder()
                .setConfigurationText(testrigResourcePrefix, configurationNames)
                .build(),
            _folder);

    // We should get all three configs, with modified hostnames for the first two
    assertThat(
        batfish.loadConfigurations().keySet(),
        containsInAnyOrder(
            ParseVendorConfigurationResult.getModifiedNameBase("rtr1", "configs/rtr1"),
            ParseVendorConfigurationResult.getModifiedNameBase("rtr1", "configs/rtr2"),
            "rtr3"));

    // hostnames are unique in rtr1 and rtr2
    String testrigResourcePrefix2 = "org/batfish/main/snapshots/duplicate_hostnames2";
    List<String> configurationNames2 = ImmutableList.of("rtr1", "rtr2");

    Batfish batfish2 =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder()
                .setConfigurationText(testrigResourcePrefix2, configurationNames2)
                .build(),
            _folder);

    // we should get only two configs, with real names -- no memory of old duplicates
    assertThat(batfish2.loadConfigurations().keySet(), equalTo(ImmutableSet.of("rtr1", "rtr2")));
  }

  @Test
  public void testInitTestrigWithLayer1Topology() throws IOException {
    String testrigResourcePrefix = "org/batfish/common/topology/testrigs/layer1";
    TestrigText.Builder testrigTextBuilder =
        TestrigText.builder()
            .setLayer1TopologyText(testrigResourcePrefix)
            .setHostsText(testrigResourcePrefix, ImmutableSet.of("c1.json", "c2.json"));
    Batfish batfish =
        BatfishTestUtils.getBatfishFromTestrigText(testrigTextBuilder.build(), _folder);

    assertThat(
        batfish.computeTestrigTopology(batfish.loadConfigurations()).getEdges(),
        containsInAnyOrder(Edge.of("c1", "i1", "c2", "i2"), Edge.of("c2", "i2", "c1", "i1")));
  }

  @Test
  public void testInitTestrigWithLegacyTopology() throws IOException {
    String testrigResourcePrefix = "org/batfish/common/topology/testrigs/legacy";
    TestrigText.Builder testrigTextBuilder =
        TestrigText.builder()
            .setLegacyTopologyText("org/batfish/common/topology/testrigs/legacy")
            .setHostsText(testrigResourcePrefix, ImmutableSet.of("c1.json", "c2.json"));
    Batfish batfish =
        BatfishTestUtils.getBatfishFromTestrigText(testrigTextBuilder.build(), _folder);

    assertThat(
        batfish.computeTestrigTopology(batfish.loadConfigurations()).getEdges(),
        containsInAnyOrder(Edge.of("c1", "i1", "c2", "i2"), Edge.of("c2", "i2", "c1", "i1")));
  }

  @Test
  public void testFlatten() throws IOException {
    Path root = _folder.getRoot().toPath();
    Path inputDir = root.resolve("input");
    Path outputDir = root.resolve("output");
    Path inputFile = inputDir.resolve(BfConsts.RELPATH_CONFIGURATIONS_DIR).resolve("conf");
    Path outputFile = outputDir.resolve(BfConsts.RELPATH_CONFIGURATIONS_DIR).resolve("conf");
    inputFile.getParent().toFile().mkdirs();
    CommonUtil.writeFile(
        inputFile, CommonUtil.readResource("org/batfish/grammar/juniper/testconfigs/hierarchical"));
    Batfish batfish =
        BatfishTestUtils.getBatfishFromTestrigText(TestrigText.builder().build(), _folder);
    batfish.flatten(inputDir, outputDir);

    assertThat(
        CommonUtil.readFile(outputFile),
        equalTo(CommonUtil.readResource("org/batfish/grammar/juniper/testconfigs/flat")));
  }

  @Test
  public void testLoadLayer1Topology() throws IOException {
    TestrigText.Builder testrigTextBuilder =
        TestrigText.builder().setLayer1TopologyText("org/batfish/common/topology/testrigs/layer1");
    Batfish batfish =
        BatfishTestUtils.getBatfishFromTestrigText(testrigTextBuilder.build(), _folder);
    Layer1Topology layer1Topology = batfish.getLayer1Topology();

    Layer1Node c1i1 = new Layer1Node("c1", "i1");
    Layer1Node c2i2 = new Layer1Node("c2", "i2");
    Layer1Node c1i3 = new Layer1Node("c1", "i3");
    Layer1Node c2i4 = new Layer1Node("c2", "i4");
    assertThat(
        layer1Topology.getGraph().edges(),
        equalTo(
            ImmutableSet.of(
                new Layer1Edge(c1i1, c2i2),
                new Layer1Edge(c2i2, c1i1),
                new Layer1Edge(c1i3, c2i4),
                new Layer1Edge(c2i4, c1i3))));
  }

  @Test
  public void testMultipleBestVrrpCandidates() throws IOException {
    String testrigResourcePrefix = "org/batfish/grammar/cisco/testrigs/vrrp_multiple_best";
    List<String> configurationNames = ImmutableList.of("r1", "r2");

    Ip vrrpAddress = Ip.parse("1.0.0.10");
    Batfish batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder()
                .setConfigurationText(testrigResourcePrefix, configurationNames)
                .build(),
            _folder);
    Map<String, Configuration> configurations = batfish.loadConfigurations();
    Map<Ip, Set<String>> ipOwners = TopologyUtil.computeIpNodeOwners(configurations, true);
    assertThat(ipOwners.get(vrrpAddress), equalTo(Collections.singleton("r2")));
  }

  @Test
  public void testNoFileUnderPath() throws IOException {
    Path emptyFolder = _folder.newFolder("emptyFolder").toPath();
    List<Path> result = Batfish.listAllFiles(emptyFolder);
    assertThat(result, empty());
  }

  @Test
  public void testCheckValidTopology() {
    Map<String, Configuration> configs = new HashMap<>();
    configs.put(
        "h1", BatfishTestUtils.createTestConfiguration("h1", ConfigurationFormat.HOST, "eth0"));
    configs.put(
        "h2", BatfishTestUtils.createTestConfiguration("h2", ConfigurationFormat.HOST, "e0"));
    SortedSet<Edge> edges =
        new TreeSet<>(Collections.singletonList(Edge.of("h1", "eth0", "h2", "e0")));
    Topology topology = new Topology(edges);

    // test that checkTopology does not throw
    Batfish.checkTopology(configs, topology);
  }

  @Test
  public void testCheckTopologyInvalidNode() {
    Map<String, Configuration> configs = new HashMap<>();
    configs.put(
        "h1", BatfishTestUtils.createTestConfiguration("h1", ConfigurationFormat.HOST, "eth0"));
    SortedSet<Edge> edges =
        new TreeSet<>(Collections.singletonList(Edge.of("h1", "eth0", "h2", "e0")));
    Topology topology = new Topology(edges);

    _thrown.expect(BatfishException.class);
    _thrown.expectMessage("Topology contains a non-existent node 'h2'");
    Batfish.checkTopology(configs, topology);
  }

  @Test
  public void testCheckTopologyInvalidInterface() {
    Map<String, Configuration> configs = new HashMap<>();
    configs.put(
        "h1", BatfishTestUtils.createTestConfiguration("h1", ConfigurationFormat.HOST, "eth0"));
    configs.put(
        "h2", BatfishTestUtils.createTestConfiguration("h2", ConfigurationFormat.HOST, "e0"));
    SortedSet<Edge> edges =
        new TreeSet<>(Collections.singletonList(Edge.of("h1", "eth1", "h2", "e0")));
    Topology topology = new Topology(edges);

    _thrown.expect(BatfishException.class);
    _thrown.expectMessage("Topology contains a non-existent interface 'eth1' on node 'h1'");
    Batfish.checkTopology(configs, topology);
  }

  @Test
  public void testReadMissingIptableFile() throws IOException {
    HostConfiguration host1 = new HostConfiguration();
    String filename = Paths.get("iptables", "host1.iptables").toString();
    host1.setHostname("host1");
    host1.setIptablesFile(filename);
    SortedMap<String, VendorConfiguration> hostConfigurations = new TreeMap<>();
    hostConfigurations.put("host1", host1);
    SortedMap<Path, String> iptablesData = new TreeMap<>();
    Path testRigPath = _folder.newFolder("testrig").toPath();
    ParseVendorConfigurationAnswerElement answerElement =
        new ParseVendorConfigurationAnswerElement();
    answerElement.getParseStatus().put("configs/host1.cfg", ParseStatus.PASSED);
    Batfish batfish = BatfishTestUtils.getBatfish(new TreeMap<>(), _folder);
    String failureMessage =
        "Iptables file iptables"
            + File.separator
            + "host1.iptables for host host1 is not contained within the testrig";
    batfish.readIptableFiles(testRigPath, hostConfigurations, iptablesData, answerElement);
    assertThat(
        answerElement.getParseStatus().get(Paths.get("iptables", "host1.iptables").toString()),
        equalTo(ParseStatus.FAILED));
    assertThat(
        answerElement.getErrors().get("host1").prettyPrint(), containsString(failureMessage));
    // When host file failed, verify that error message contains both failure messages
    answerElement.getErrors().clear();
    answerElement
        .getErrors()
        .put(
            "host1",
            new BatfishException("Failed to parse host file: host1").getBatfishStackTrace());
    batfish.readIptableFiles(testRigPath, hostConfigurations, iptablesData, answerElement);
    assertThat(
        answerElement.getErrors().get("host1").prettyPrint(), containsString(failureMessage));
    assertThat(
        answerElement.getErrors().get("host1").prettyPrint(),
        containsString("Failed to parse host file: host1"));
    // When the haltonparseerror flag is set to true
    batfish.getSettings().setHaltOnParseError(true);
    answerElement.getErrors().clear();
    String parseErrorMessage =
        "Fatal exception due to at least one Iptables file is not contained"
            + " within the testrig";
    _thrown.expect(BatfishException.class);
    _thrown.expectMessage(parseErrorMessage);
    batfish.readIptableFiles(testRigPath, hostConfigurations, iptablesData, answerElement);
  }

  @Test
  public void testReadNestedPath() throws IOException {
    Path nestedFolder = _folder.newFolder("nestedDirectory").toPath();
    List<Path> expected = new ArrayList<>();
    expected.add(nestedFolder.resolve("b-test.cfg"));
    expected.add(nestedFolder.resolve("d-test.cfg"));
    expected.add(nestedFolder.resolve("aDirectory").resolve("e-test.cfg"));
    expected.add(nestedFolder.resolve("eDirectory").resolve("a-test.cfg"));
    expected.add(nestedFolder.resolve("eDirectory").resolve("c-test.cfg"));
    for (Path path : expected) {
      path.getParent().toFile().mkdir();
      assertThat(path.toFile().createNewFile(), is(true));
    }
    List<Path> actual = Batfish.listAllFiles(nestedFolder);
    Collections.sort(expected);
    assertThat(expected, equalTo(actual));
  }

  @Test
  public void testReadStartWithDotFile() throws IOException {
    Path startWithDot = _folder.newFolder("startWithDot").toPath();
    File file = startWithDot.resolve(".cfg").toFile();
    file.getParentFile().mkdir();
    assertThat(file.createNewFile(), is(true));
    List<Path> result = Batfish.listAllFiles(startWithDot);
    assertThat(result, is(empty()));
  }

  @Test
  public void testReadUnNestedPath() throws IOException {
    Path unNestedFolder = _folder.newFolder("unNestedDirectory").toPath();
    List<Path> expected = new ArrayList<>();
    expected.add(unNestedFolder.resolve("test1.cfg"));
    expected.add(unNestedFolder.resolve("test2.cfg"));
    expected.add(unNestedFolder.resolve("test3.cfg"));
    for (Path path : expected) {
      path.getParent().toFile().mkdir();
      assertThat(path.toFile().createNewFile(), is(true));
    }
    List<Path> actual = Batfish.listAllFiles(unNestedFolder);
    Collections.sort(expected);
    assertThat(expected, equalTo(actual));
  }

  @Test
  public void testUnusableVrrpHandledCorrectly() throws Exception {
    String configurationText =
        String.join(
            "\n",
            new String[] {
              "hostname host1", "!", "interface Vlan65", "   vrrp 1 ip 1.2.3.4", "!",
            });
    SortedMap<String, String> configMap = ImmutableSortedMap.of("host1", configurationText);
    Batfish batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder().setConfigurationText(configMap).build(), _folder);
    Map<String, Configuration> configs = batfish.loadConfigurations();

    // Assert that the config parsed successfully
    assertThat(configs, hasKey("host1"));
    assertThat(configs.get("host1").getAllInterfaces(), hasKey("Vlan65"));
    assertThat(
        configs.get("host1").getAllInterfaces().get("Vlan65").getVrrpGroups().keySet(), hasSize(1));

    // Tests that computing IP owners with such a bad interface does not crash.
    TopologyUtil.computeIpNodeOwners(configs, false);
  }

  @Test
  public void testReadValidIptableFile() throws IOException {
    HostConfiguration host1 = new HostConfiguration();
    host1.setHostname("host1");
    Path iptablePath = Paths.get("iptables").resolve("host1.iptables");
    host1.setIptablesFile(iptablePath.toString());
    SortedMap<String, VendorConfiguration> hostConfigurations = new TreeMap<>();
    hostConfigurations.put("host1", host1);
    SortedMap<Path, String> iptablesData = new TreeMap<>();
    Path testRigPath = _folder.newFolder("testrig").toPath();
    File iptableFile = Paths.get(testRigPath.toString(), iptablePath.toString()).toFile();
    iptableFile.getParentFile().mkdir();
    assertThat(iptableFile.createNewFile(), is(true));
    ParseVendorConfigurationAnswerElement answerElement =
        new ParseVendorConfigurationAnswerElement();
    answerElement.getParseStatus().put("host1", ParseStatus.PASSED);
    Batfish batfish = BatfishTestUtils.getBatfish(new TreeMap<>(), _folder);
    batfish.readIptableFiles(testRigPath, hostConfigurations, iptablesData, answerElement);
    assertThat(answerElement.getParseStatus().get("host1"), equalTo(ParseStatus.PASSED));
    assertThat(answerElement.getErrors().size(), is(0));
  }

  @Test
  public void throwsExceptionWithSpecificType() {
    Path nonExistPath = _folder.getRoot().toPath().resolve("nonExistent");
    _thrown.expect(BatfishException.class);
    _thrown.expectMessage("Failed to walk path: " + nonExistPath);
    Batfish.listAllFiles(nonExistPath);
  }

  @Test
  public void testLoadQuestionSettingsPresent() {
    String questionSettings = "{}";

    Batfish batfish =
        BatfishTestUtils.getBatfish(
            new TestStorageProvider() {
              @Override
              public String loadQuestionSettings(
                  NetworkId network, QuestionSettingsId questionSettingsId) throws IOException {
                return questionSettings;
              }
            },
            new TestIdResolver() {
              @Override
              public boolean hasQuestionSettingsId(String questionClassId, NetworkId networkId) {
                return true;
              }

              @Override
              public QuestionSettingsId getQuestionSettingsId(
                  String questionClassId, NetworkId networkId) {
                return new QuestionSettingsId("blah");
              }
            });

    assertThat(batfish.loadQuestionSettings(TEST_QUESTION), equalTo(questionSettings));
  }

  @Test
  public void testLoadQuestionSettingsAbsent() {
    Batfish batfish =
        BatfishTestUtils.getBatfish(
            new TestStorageProvider() {
              @Override
              public String loadQuestionSettings(
                  NetworkId networkId, QuestionSettingsId questionSettingsId) throws IOException {
                return null;
              }
            },
            new TestIdResolver() {
              @Override
              public boolean hasQuestionSettingsId(String questionClassId, NetworkId networkId) {
                return false;
              }
            });

    assertThat(batfish.loadQuestionSettings(TEST_QUESTION), nullValue());
  }

  @Test
  public void testLoadQuestionSettingsError() {
    Batfish batfish =
        BatfishTestUtils.getBatfish(
            new TestStorageProvider() {
              @Override
              public String loadQuestionSettings(
                  NetworkId networkId, QuestionSettingsId questionSettingsId) throws IOException {
                throw new IOException("simulated error");
              }
            },
            new TestIdResolver() {
              @Override
              public QuestionSettingsId getQuestionSettingsId(
                  String questionClassId, NetworkId networkId) {
                return new QuestionSettingsId("foo");
              }

              @Override
              public boolean hasQuestionSettingsId(String questionClassId, NetworkId networkId) {
                return true;
              }
            });

    _thrown.expect(BatfishException.class);
    assertThat(batfish.loadQuestionSettings(TEST_QUESTION), nullValue());
  }

  @Test
  public void testCreateAnswerer() {
    Batfish batfish = BatfishTestUtils.getBatfish(new TestStorageProvider(), new TestIdResolver());
    Question testQuestion =
        new TestQuestion() {
          @Override
          public String getName() {
            return "q1";
          }
        };
    Question testQuestionMissing =
        new TestQuestion() {
          @Override
          public String getName() {
            return "q2";
          }
        };
    Answerer testAnswerer =
        new Answerer(testQuestion, batfish) {
          @Override
          public AnswerElement answer() {
            throw new UnsupportedOperationException("no implementation for generated method");
          }
        };

    batfish.registerAnswerer("q1", "q1ClassName", (q, b) -> testAnswerer);

    // should get the answerer the creator supplies
    assertThat(batfish.createAnswerer(testQuestion), equalTo(testAnswerer));

    // should get null answerer if no creator available
    assertThat(batfish.createAnswerer(testQuestionMissing), nullValue());
  }

  // all of these interfaces should not be ignored by processManagementInterfaces()
  @Test
  public void testNotIgnoredManagementInterfaces() {
    String notIgnored = "notIgnored";
    String notIgnored2 = "them0";
    String notIgnored3 = "mgt-me0";
    String notIgnored4 = "manage";
    String notIgnored5 = "Afxp0";

    Map<String, Configuration> configs = new HashMap<>();
    Configuration config1 =
        BatfishTestUtils.createTestConfiguration(
            "config1",
            ConfigurationFormat.HOST,
            notIgnored,
            notIgnored2,
            notIgnored3,
            notIgnored4,
            notIgnored5);
    config1.getAllInterfaces().get(notIgnored).setVrfName("vrf-mgmt");
    config1.getAllInterfaces().get(notIgnored2).setVrfName("Manageme");
    config1.getAllInterfaces().get(notIgnored3).setVrfName("fxp0");
    configs.put("config1", config1);

    Batfish.processManagementInterfaces(configs);

    // all of the interfaces should still be active
    assertThat(
        config1.activeInterfaces(),
        equalTo(ImmutableSet.of(notIgnored, notIgnored2, notIgnored3, notIgnored4, notIgnored5)));
  }

  // all of these interfaces should be ignored by processManagementInterfaces()
  @Test
  public void testIgnoredManagementInterfaces() {
    String ignoredIface1 = "ignoredIface1";
    String ignoredIface2 = "ignoredIface2";

    Map<String, Configuration> configs = new HashMap<>();
    Configuration config1 =
        BatfishTestUtils.createTestConfiguration(
            "config1",
            ConfigurationFormat.HOST,
            ignoredIface1,
            ignoredIface2,
            "mgmt0",
            "Management",
            "fxp0-0",
            "em0.0",
            "me0.10");
    config1.getAllInterfaces().get(ignoredIface1).setVrfName("Mgmt-intf");
    config1.getAllInterfaces().get(ignoredIface2).setVrfName("ManagementVrf");
    configs.put("config1", config1);

    Batfish.processManagementInterfaces(configs);

    // none of the interfaces should be active
    assertThat(config1.activeInterfaces(), equalTo(ImmutableSet.of()));
  }

  @Test
  public void testGetAddressBooks() {
    String book1 = "book1";
    String book2 = "book2";

    ReferenceBook refBook1 = ReferenceBook.builder(book1).build();
    ReferenceBook refBook2 = ReferenceBook.builder(book2).build();

    ReferenceLibrary referenceLibrary = new ReferenceLibrary(ImmutableList.of(refBook1, refBook2));

    assertThat(Batfish.getAddressBooks(referenceLibrary), equalTo(ImmutableSet.of(book1, book2)));
  }

  @Test
  public void testGetAddressGroups() {
    String group1 = "group1";
    String group2 = "group2";

    AddressGroup addressGroup1 = new AddressGroup(null, group1);
    AddressGroup addressGroup2 = new AddressGroup(null, group2);

    ReferenceBook refBook1 =
        ReferenceBook.builder("book1")
            .setAddressGroups(ImmutableList.of(addressGroup1, addressGroup2))
            .build();
    ReferenceBook refBook2 =
        ReferenceBook.builder("book2").setAddressGroups(ImmutableList.of(addressGroup1)).build();
    ReferenceBook refBook3 = ReferenceBook.builder("book3").build();

    ReferenceLibrary referenceLibrary =
        new ReferenceLibrary(ImmutableList.of(refBook1, refBook2, refBook3));

    assertThat(
        Batfish.getAddressGroups(referenceLibrary), equalTo(ImmutableSet.of(group1, group2)));
  }

  @Test
  public void testGetFilterNames() {
    String filter1 = "filter1";
    String filter2 = "filter2";

    IpAccessList ipAccessList1 = IpAccessList.builder().setName(filter1).build();
    IpAccessList ipAccessList2 = IpAccessList.builder().setName(filter2).build();

    Map<String, Configuration> configs = new HashMap<>();
    Configuration config =
        BatfishTestUtils.createTestConfiguration("config1", ConfigurationFormat.HOST);
    config.setIpAccessLists(ImmutableSortedMap.of(filter1, ipAccessList1, filter2, ipAccessList2));
    configs.put("config1", config);

    assertThat(Batfish.getFilterNames(configs), equalTo(ImmutableSet.of(filter1, filter2)));
  }

  @Test
  public void testGetInterfaces() {
    String nodeName = "nodeName";

    String int1 = "int1";
    String int2 = "int2";

    Map<String, Configuration> configs = new HashMap<>();
    Configuration config =
        BatfishTestUtils.createTestConfiguration(nodeName, ConfigurationFormat.HOST, int1, int2);
    configs.put(nodeName, config);

    assertThat(
        Batfish.getInterfaces(configs),
        equalTo(
            ImmutableSet.of(
                new NodeInterfacePair(nodeName, int1), new NodeInterfacePair(nodeName, int2))));
  }

  @Test
  public void testGetIps() {
    String nodeName = "nodeName";

    String int1 = "int1";
    String int2 = "int2";

    String ip1 = "10.1.3.7";
    String ip2 = "128.212.155.30";
    String ip3 = "124.51.32.2";

    String address1 = ip1 + "/30";
    String address2 = ip2 + "/24";
    String address3 = ip3 + "/20";

    InterfaceAddress interfaceAddress1 = new InterfaceAddress(address1);
    InterfaceAddress interfaceAddress2 = new InterfaceAddress(address2);
    InterfaceAddress interfaceAddress3 = new InterfaceAddress(address3);

    Map<String, Configuration> configs = new HashMap<>();
    Configuration config =
        BatfishTestUtils.createTestConfiguration(nodeName, ConfigurationFormat.HOST, int1, int2);

    config
        .getAllInterfaces()
        .get(int1)
        .setAllAddresses(ImmutableSet.of(interfaceAddress1, interfaceAddress2));
    config
        .getAllInterfaces()
        .get(int2)
        .setAllAddresses(ImmutableSet.of(interfaceAddress2, interfaceAddress3));

    configs.put(nodeName, config);

    assertThat(Batfish.getIps(configs), equalTo(ImmutableSet.of(ip1, ip2, ip3)));
  }

  @Test
  public void testGetPrefixes() {
    String nodeName = "nodeName";

    String int1 = "int1";
    String int2 = "int2";

    String address1 = "10.1.3.7/30";
    String address2 = "128.212.155.30/24";
    String address3 = "124.51.32.2/20";

    InterfaceAddress interfaceAddress1 = new InterfaceAddress(address1);
    InterfaceAddress interfaceAddress2 = new InterfaceAddress(address2);
    InterfaceAddress interfaceAddress3 = new InterfaceAddress(address3);

    Map<String, Configuration> configs = new HashMap<>();
    Configuration config =
        BatfishTestUtils.createTestConfiguration(nodeName, ConfigurationFormat.HOST, int1, int2);

    config
        .getAllInterfaces()
        .get(int1)
        .setAllAddresses(ImmutableSet.of(interfaceAddress1, interfaceAddress2));
    config
        .getAllInterfaces()
        .get(int2)
        .setAllAddresses(ImmutableSet.of(interfaceAddress2, interfaceAddress3));

    configs.put(nodeName, config);

    assertThat(
        Batfish.getPrefixes(configs),
        equalTo(
            ImmutableSet.of(
                interfaceAddress1.getPrefix().toString(),
                interfaceAddress2.getPrefix().toString(),
                interfaceAddress3.getPrefix().toString())));
  }

  @Test
  public void testGetStructureNames() {
    String nodeName = "nodeName";

    String asPathAccessListName = "asPathAccessList";
    String authenticationKeyChainName = "authenticationKeyChain";
    String communityListName = "communityList";
    String ikePolicyName = "ikePolicy";
    String ipAccessListName = "ipAccessList";
    String ip6AccessListName = "ip6AccessList";
    String ipsecPolicyName = "ipsecPolicy";
    String ipsecProposalName = "ipsecProposal";
    String ipsecVpnName = "ipsecVpn";
    String routeFilterListName = "routeFilterList";
    String route6FilterListName = "route6FilterList";
    String routingPolicyName = "routingPolicyName";
    String vrfName = "vrf";
    String zoneName = "zone";

    Map<String, Configuration> configs = new HashMap<>();
    Configuration config =
        BatfishTestUtils.createTestConfiguration(nodeName, ConfigurationFormat.HOST);

    config.setAsPathAccessLists(
        ImmutableSortedMap.of(
            asPathAccessListName, new AsPathAccessList(asPathAccessListName, null)));
    config.setAuthenticationKeyChains(
        ImmutableSortedMap.of(
            authenticationKeyChainName, new AuthenticationKeyChain(authenticationKeyChainName)));
    config.setCommunityLists(
        ImmutableSortedMap.of(
            communityListName, new CommunityList(communityListName, ImmutableList.of(), true)));
    config.setIkePolicies(ImmutableSortedMap.of(ikePolicyName, new IkePolicy(ikePolicyName)));
    config.setIpAccessLists(
        ImmutableSortedMap.of(ipAccessListName, new IpAccessList(ipAccessListName)));
    config.setIp6AccessLists(
        ImmutableSortedMap.of(ip6AccessListName, new Ip6AccessList(ip6AccessListName)));
    config.setIpsecPolicies(
        ImmutableSortedMap.of(ipsecPolicyName, new IpsecPolicy(ipsecPolicyName)));
    config.setIpsecProposals(
        ImmutableSortedMap.of(ipsecProposalName, new IpsecProposal(ipsecProposalName)));
    config.setIpsecVpns(ImmutableSortedMap.of(ipsecVpnName, new IpsecVpn(ipsecVpnName)));
    config.setRouteFilterLists(
        ImmutableSortedMap.of(routeFilterListName, new RouteFilterList(routeFilterListName)));
    config.setRoute6FilterLists(
        ImmutableSortedMap.of(route6FilterListName, new Route6FilterList(route6FilterListName)));
    config.setRoutingPolicies(
        ImmutableSortedMap.of(routingPolicyName, new RoutingPolicy(routingPolicyName, null)));
    config.setVrfs(ImmutableSortedMap.of(vrfName, new Vrf(vrfName)));
    config.setZones(ImmutableSortedMap.of(zoneName, new Zone(zoneName)));

    configs.put(nodeName, config);

    assertThat(
        Batfish.getStructureNames(configs),
        equalTo(
            ImmutableSet.of(
                asPathAccessListName,
                authenticationKeyChainName,
                communityListName,
                ikePolicyName,
                ipAccessListName,
                ip6AccessListName,
                ipsecPolicyName,
                ipsecProposalName,
                ipsecVpnName,
                routeFilterListName,
                route6FilterListName,
                routingPolicyName,
                vrfName,
                zoneName)));
  }

  @Test
  public void testGetVrfs() {
    String int1 = "int1";
    String int2 = "int2";
    String int3 = "int3";

    String vrf1 = "vrf1";
    String vrf2 = "vrf2";

    Map<String, Configuration> configs = new HashMap<>();
    Configuration config =
        BatfishTestUtils.createTestConfiguration(
            "config1", ConfigurationFormat.HOST, int1, int2, int3);
    config.getAllInterfaces().get(int1).setVrfName(vrf1);
    config.getAllInterfaces().get(int2).setVrfName(vrf2);
    config.getAllInterfaces().get(int3).setVrfName(vrf1);
    configs.put("config1", config);

    assertThat(Batfish.getVrfs(configs), equalTo(ImmutableSet.of(vrf1, vrf2)));
  }

  @Test
  public void testGetZones() {
    String zone1 = "zone1";
    String zone2 = "zone2";

    Map<String, Configuration> configs = new HashMap<>();
    Configuration config =
        BatfishTestUtils.createTestConfiguration("config1", ConfigurationFormat.HOST);

    config.setZones(ImmutableSortedMap.of(zone1, new Zone(zone1), zone2, new Zone(zone2)));

    configs.put("config1", config);

    assertThat(Batfish.getZones(configs), equalTo(ImmutableSet.of(zone1, zone2)));
  }

  @Test
  public void testPostProcessInterfaceDependenciesBind() {
    NetworkFactory nf = new NetworkFactory();
    Configuration c1 =
        nf.configurationBuilder()
            .setHostname("c1")
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .build();
    Vrf vrf = nf.vrfBuilder().setOwner(c1).setName(Configuration.DEFAULT_VRF_NAME).build();

    Interface.Builder ib = nf.interfaceBuilder().setOwner(c1).setVrf(vrf);

    ib.setName("eth0").setActive(false).build();
    ib.setName("eth1")
        .setActive(true)
        .setDependencies(ImmutableSet.of(new Dependency("eth0", DependencyType.BIND)))
        .build();
    ib.setName("eth2")
        .setActive(true)
        .setDependencies(ImmutableSet.of(new Dependency("eth1", DependencyType.BIND)))
        .build();
    ib.setName("eth9").setActive(true).build();

    ImmutableSet<String> activeIfaces = ImmutableSet.of("eth9");
    ImmutableSet<String> inactiveIfaces = ImmutableSet.of("eth0", "eth1", "eth2");

    // Test
    postProcessInterfaceDependencies(ImmutableMap.of("c1", c1));

    activeIfaces.forEach(
        name -> assertThat(c1.getAllInterfaces().get(name).getActive(), equalTo(true)));
    inactiveIfaces.forEach(
        name -> assertThat(c1.getAllInterfaces().get(name).getActive(), equalTo(false)));
  }

  @Test
  public void testPostProcessInterfaceDependenciesAggregate() {
    NetworkFactory nf = new NetworkFactory();
    Configuration c1 =
        nf.configurationBuilder()
            .setHostname("c1")
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .build();
    Vrf vrf = nf.vrfBuilder().setOwner(c1).setName(Configuration.DEFAULT_VRF_NAME).build();

    Interface.Builder ib = nf.interfaceBuilder().setOwner(c1).setVrf(vrf);

    ib.setName("eth0").setActive(false).setType(InterfaceType.PHYSICAL).build();
    ib.setName("eth1").setActive(true).setType(InterfaceType.PHYSICAL).build();
    ib.setName("eth2")
        .setActive(true)
        .setType(InterfaceType.AGGREGATED)
        .setDependencies(
            ImmutableSet.of(
                new Dependency("eth1", DependencyType.AGGREGATE),
                new Dependency("eth0", DependencyType.AGGREGATE)))
        .build();

    ib.setName("eth3").setActive(false).setType(InterfaceType.PHYSICAL).build();
    ib.setName("eth4")
        .setActive(true)
        .setType(InterfaceType.AGGREGATED)
        .setDependencies(
            ImmutableSet.of(
                new Dependency("eth0", DependencyType.AGGREGATE),
                new Dependency("eth3", DependencyType.AGGREGATE)))
        .build();

    ImmutableSet<String> activeIfaces = ImmutableSet.of("eth1", "eth2");
    ImmutableSet<String> inactiveIfaces = ImmutableSet.of("eth0", "eth3", "eth4");

    // Test
    postProcessInterfaceDependencies(ImmutableMap.of("c1", c1));

    activeIfaces.forEach(
        name -> assertThat(c1.getAllInterfaces().get(name).getActive(), equalTo(true)));
    inactiveIfaces.forEach(
        name -> assertThat(c1.getAllInterfaces().get(name).getActive(), equalTo(false)));
  }
}
