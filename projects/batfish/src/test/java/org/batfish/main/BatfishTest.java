package org.batfish.main;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import org.apache.commons.collections4.map.LRUMap;
import org.batfish.common.BatfishException;
import org.batfish.common.BatfishLogger;
import org.batfish.common.CompositeBatfishException;
import org.batfish.common.util.CommonUtil;
import org.batfish.config.Settings;
import org.batfish.config.Settings.EnvironmentSettings;
import org.batfish.config.Settings.TestrigSettings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.answers.Answer;
import org.batfish.datamodel.answers.AnswerStatus;
import org.batfish.datamodel.answers.ParseStatus;
import org.batfish.datamodel.answers.ParseVendorConfigurationAnswerElement;
import org.batfish.datamodel.collections.BgpAdvertisementsByVrf;
import org.batfish.datamodel.collections.EdgeSet;
import org.batfish.datamodel.collections.RoutesByVrf;
import org.batfish.representation.host.HostConfiguration;
import org.batfish.vendor.VendorConfiguration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

/** Tests for {@link Batfish}. */
public class BatfishTest {

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Rule public ExpectedException _thrown = ExpectedException.none();

  // Tests for readIptableFiles method
  private Batfish initBatfish() {
    Settings settings = new Settings(new String[] {});
    settings.setLogger(new BatfishLogger("debug", false));
    final Map<TestrigSettings, SortedMap<String, Configuration>> CACHED_TESTRIGS =
        Collections.synchronizedMap(
            new LRUMap<TestrigSettings, SortedMap<String, Configuration>>(5));
    final Map<TestrigSettings, DataPlane> CACHED_DATA_PLANES =
        Collections.synchronizedMap(new LRUMap<TestrigSettings, DataPlane>(2));
    final Map<EnvironmentSettings, SortedMap<String, BgpAdvertisementsByVrf>>
        CACHED_ENVIRONMENT_BGP_TABLES =
            Collections.synchronizedMap(
                new LRUMap<EnvironmentSettings, SortedMap<String, BgpAdvertisementsByVrf>>(4));
    final Map<EnvironmentSettings, SortedMap<String, RoutesByVrf>>
        CACHED_ENVIRONMENT_ROUTING_TABLES =
            Collections.synchronizedMap(
                new LRUMap<EnvironmentSettings, SortedMap<String, RoutesByVrf>>(4));
    Batfish batfish =
        new Batfish(
            settings,
            CACHED_TESTRIGS,
            CACHED_DATA_PLANES,
            CACHED_ENVIRONMENT_BGP_TABLES,
            CACHED_ENVIRONMENT_ROUTING_TABLES);
    return batfish;
  }

  @Test
  public void testAnswerBadQuestion() throws IOException {
   // missing class field
   String badQuestionStr = "{"
         + "\"differential\": false,"
         + "\"instance\": {"
         + "\"description\": \"Outputs cases where undefined structures (e.g., ACL, routemaps) are"
         +                    "referenced.\","
         + "\"instanceName\": \"undefinedReferences\","
         +     "\"longDescription\": \"Such occurrences indicate configuration errors and can have"
         +                            "serious consequences with some vendors.\","
         +     "\"tags\": [\"default\"],"
         +     "\"variables\": {\"nodeRegex\": {"
         +        "\"description\": \"Only check nodes whose name matches this regex\","
         +        "\"type\": \"javaRegex\","
         +        "\"value\": \".*\""
         +      "}}"
         + "},"
         + "\"nodeRegex\": \"${nodeRegex}\""
         + "}";

    Path questionPath =
          CommonUtil.createTempFileWithContent("testAnswerBadQuestion", badQuestionStr);
    Batfish batfish = initBatfish();
    batfish.getSettings().setQuestionPath(questionPath);
    Answer answer = batfish.answer();
    assertThat(answer.getQuestion(), is(nullValue()));
    assertEquals(answer.getStatus(), AnswerStatus.FAILURE);
    assertEquals(answer.getAnswerElements().size(), 1);
    assertThat(
        answer.getAnswerElements().get(0).prettyPrint(),
        containsString("Could not parse question"));
  }

  @Test
  public void testNoFileUnderPath() throws IOException {
    Path emptyFolder = _folder.newFolder("emptyFolder").toPath();
    List<Path> result = Batfish.listAllFiles(emptyFolder);
    assertThat(result, empty());
  }

  @Test
  public void testParseTopologyBadJson() throws IOException {
    //missing node2interface
    String topologyBadJson =
        "["
            + "{ "
            + "\"node1\" : \"as1border1\","
            + "\"node1interface\" : \"GigabitEthernet0/0\","
            + "\"node2\" : \"as1core1\","
            + "},"
            + "]";

    Path topologyFilePath =
        CommonUtil.createTempFileWithContent("testParseTopologyJson", topologyBadJson);
    Batfish batfish = initBatfish();
    String errorMessage = "Topology format error";
    _thrown.expect(BatfishException.class);
    _thrown.expectMessage(errorMessage);
    batfish.parseTopology(topologyFilePath);
  }

  @Test
  public void testParseTopologyEmpty() throws IOException {
    String topologyEmpty = "";
    Path topologyFilePath =
        CommonUtil.createTempFileWithContent("testParseTopologyJson", topologyEmpty);
    Batfish batfish = initBatfish();
    String errorMessage = "ERROR: empty topology\n";
    _thrown.expect(BatfishException.class);
    _thrown.expectMessage(errorMessage);
    batfish.parseTopology(topologyFilePath);
  }

  @Test
  public void testParseTopologyJson() throws IOException {
    String topologyJson =
        "["
            + "{ "
            + "\"node1\" : \"as1border1\","
            + "\"node1interface\" : \"GigabitEthernet0/0\","
            + "\"node2\" : \"as1core1\","
            + "\"node2interface\" : \"GigabitEthernet1/0\""
            + "},"
            + "{"
            + "\"node1\" : \"as1border1\","
            + "\"node1interface\" : \"GigabitEthernet1/0\","
            + "\"node2\" : \"as2border1\","
            + "\"node2interface\" : \"GigabitEthernet0/0\""
            + "}"
            + "]";

    Path topologyFilePath =
        CommonUtil.createTempFileWithContent("testParseTopologyJson", topologyJson);
    Batfish batfish = initBatfish();
    Topology topology = batfish.parseTopology(topologyFilePath);
    assertEquals(topology.getEdges().size(), 2);
  }

  @Test
  public void testCheckValidTopology() throws IOException {
    Batfish batfish = initBatfish();
    Map<String, Configuration> configs = new HashMap<>();
    configs.put("h1", createConfiguration("h1", "eth0"));
    configs.put("h2", createConfiguration("h2", "e0"));
    //test that checking a valid topology does not throw any exception
    EdgeSet edges = new EdgeSet();
    edges.add(new Edge("h1", "eth0", "h2", "e0"));
    batfish.checkTopology(configs, new Topology(edges));
  }

  @Test
  public void testCheckTopologyInvalidNode() throws IOException {
    Batfish batfish = initBatfish();
    Map<String, Configuration> configs = new HashMap<>();
    configs.put("h1", createConfiguration("h1", "eth0"));
    _thrown.expect(BatfishException.class);
    _thrown.expectMessage("Topology contains a non-existent node 'h2'");
    EdgeSet edges = new EdgeSet();
    edges.add(new Edge("h1", "eth0", "h2", "e0"));
    batfish.checkTopology(configs, new Topology(edges));
  }

  @Test
  public void testCheckTopologyInvalidInterface() throws IOException {
    Batfish batfish = initBatfish();
    Map<String, Configuration> configs = new HashMap<>();
    configs.put("h1", createConfiguration("h1", "eth0"));
    configs.put("h2", createConfiguration("h2", "e0"));
    _thrown.expect(BatfishException.class);
    _thrown.expectMessage("Node 'h1' doesn't has the interface 'eth1'");
    EdgeSet edges = new EdgeSet();
    edges.add(new Edge("h1", "eth1", "h2", "e0"));
    batfish.checkTopology(configs, new Topology(edges));
  }

  public void testReadMissingIptableFile() throws IOException {
    HostConfiguration host1 = new HostConfiguration();
    host1.setHostname("host1");
    host1.setIptablesFile(Paths.get("iptables").resolve("host1.iptables").toString());
    Map<String, VendorConfiguration> hostConfigurations = new HashMap<>();
    hostConfigurations.put("host1", host1);
    Map<Path, String> iptablesData = new TreeMap<>();
    Path testRigPath = _folder.newFolder("testrig").toPath();
    ParseVendorConfigurationAnswerElement answerElement =
        new ParseVendorConfigurationAnswerElement();
    answerElement.getParseStatus().put("host1", ParseStatus.PASSED);
    Batfish batfish = initBatfish();
    String failureMessage =
        "Iptables file iptables/host1.iptables for host host1 "
            + "is not contained within the testrig";
    batfish.readIptableFiles(testRigPath, hostConfigurations, iptablesData, answerElement);
    assertThat(answerElement.getParseStatus().get("host1"), equalTo(ParseStatus.FAILED));
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
    _thrown.expect(CompositeBatfishException.class);
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
  public void testReadValidIptableFile() throws IOException {
    HostConfiguration host1 = new HostConfiguration();
    host1.setHostname("host1");
    Path iptablePath = Paths.get("iptables").resolve("host1.iptables");
    host1.setIptablesFile(iptablePath.toString());
    Map<String, VendorConfiguration> hostConfigurations = new HashMap<>();
    hostConfigurations.put("host1", host1);
    Map<Path, String> iptablesData = new TreeMap<>();
    Path testRigPath = _folder.newFolder("testrig").toPath();
    File iptableFile = Paths.get(testRigPath.toString(), iptablePath.toString()).toFile();
    iptableFile.getParentFile().mkdir();
    assertThat(iptableFile.createNewFile(), is(true));
    ParseVendorConfigurationAnswerElement answerElement =
        new ParseVendorConfigurationAnswerElement();
    answerElement.getParseStatus().put("host1", ParseStatus.PASSED);
    Batfish batfish = initBatfish();
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

  private static Configuration createConfiguration(String hostname, String interfaceName) {
    Configuration config = new Configuration(hostname);
    config.setConfigurationFormat(ConfigurationFormat.HOST);
    config.getInterfaces().put(interfaceName, new Interface(interfaceName, config));
    return config;
  }
}
