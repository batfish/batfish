package org.batfish.storage;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.pojo.Analysis;
import org.batfish.datamodel.pojo.Environment;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

/** Tests for {@link FileStorageImpl} */
public class FileStorageImplTest {

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Rule public ExpectedException _thrown = ExpectedException.none();

  private Storage _storage;

  @Before
  public void setup() throws IOException {
    _storage = new FileStorageImpl(_folder.newFolder("containers").toPath());
  }

  @Test
  public void testInvalidGetAnalysis() {
    _thrown.expect(BatfishException.class);
    _thrown.expectMessage(
        String.format(
            "Analysis '%s' doesn't exist for container '%s'", "fakeanalysis", "fakecontainer"));

    //throws since the analysis is not found in the container
    _storage.getAnalysis("fakecontainer", "fakeanalysis");
  }

  @Test
  public void testSaveAnalysis() {
    Map<String, String> question =
        Collections.singletonMap("testquestionname", "testquestionvalue");
    Analysis analysis = new Analysis("testanalysis", question);
    Analysis persistedCopy = _storage.saveAnalysis("testcontainer", analysis);

    //Test equality of persistent and transient instances
    Assert.assertThat(persistedCopy, equalTo(analysis));
  }

  @Test
  public void testInvalidSaveAnalysis() {
    String containerName = "testcontainer";
    String analysisNmae = "testanalysis";
    Map<String, String> question =
        Collections.singletonMap("testquestionname", "testquestionvalue");
    Analysis analysis = new Analysis(analysisNmae, question);
    _storage.saveAnalysis(containerName, analysis);

    //Test that save analysis throws for duplicate analysis
    _thrown.expect(BatfishException.class);
    _thrown.expectMessage(
        String.format(
            "Analysis '%s' already exists for container '%s'", analysisNmae, containerName));
    _storage.saveAnalysis(containerName, analysis);
  }

  @Test
  public void testAddQuestion() {
    Analysis analysis =
        new Analysis(
            "testanalysisupdate", Collections.singletonMap("testquestion", "testquestionvalue"));
    Analysis savedAnalysis = _storage.saveAnalysis("testcontainerupdate", analysis);
    savedAnalysis.addQuestion("modifiedquestion", "modifiedquestionvalue");
    Analysis updatedAnalysis = _storage.updateAnalysis("testcontainerupdate", savedAnalysis);

    //test that attributes were updated
    assertThat(updatedAnalysis, equalTo(savedAnalysis));
  }

  @Test
  public void testDeleteQuestion() {
    Analysis analysis =
        new Analysis(
            "testanalysisupdate", Collections.singletonMap("testquestion", "testquestionvalue"));
    Analysis savedAnalysis = _storage.saveAnalysis("testcontainerupdate", analysis);
    savedAnalysis.deleteQuestion("testquestion");
    Analysis updatedAnalysis = _storage.updateAnalysis("testcontainerupdate", savedAnalysis);

    //test that attributes were updated
    assertThat(updatedAnalysis, equalTo(savedAnalysis));
  }

  @Test
  public void testInvalidUpdateAnalysis() {
    Analysis newAnalysis =
        new Analysis(
            "testanalysisupdate", Collections.singletonMap("testquestion", "testquestionvalue"));
    _thrown.expect(BatfishException.class);
    _thrown.expectMessage(
        String.format(
            "Analysis '%s' doesn't exist for container '%s'",
            "testanalysisupdate", "testcontainer"));

    //test that update throws for non-existent analysis
    _storage.updateAnalysis("testcontainer", newAnalysis);
  }

  @Test
  public void testDeleteEmptyAnalysis() {
    Analysis analysis = new Analysis("testanalysis", new HashMap<>());
    _storage.saveAnalysis("testcontainer", analysis);
    _storage.deleteAnalysis("testcontainer", "testanalysis", false);
    _thrown.expect(BatfishException.class);
    _thrown.expectMessage(
        String.format(
            "Analysis '%s' doesn't exist for container '%s'", "testanalysis", "testcontainer"));

    //should throw for the deleted analysis
    _storage.getAnalysis("testcontainer", "testanalysis");
  }

  @Test
  public void testDeleteNonEmptyAnalysis() {
    Analysis analysis =
        new Analysis(
            "testanalysis", Collections.singletonMap("testquestion", "testquestioncontent"));
    _storage.saveAnalysis("testcontainer", analysis);
    _thrown.expect(BatfishException.class);
    _thrown.expectMessage(
        String.format("%s' is not empty, deletion must be forced", "testanalysis"));

    //Test that throws when analysis contains questions, with force=false
    _storage.deleteAnalysis("testcontainer", "testanalysis", false);
  }

  @Test
  public void testForceDeleteAnalysis() {
    Analysis analysis =
        new Analysis(
            "testanalysis", Collections.singletonMap("testquestion", "testquestioncontent"));
    _storage.saveAnalysis("testcontainer", analysis);
    assertTrue("Deletion Error", _storage.deleteAnalysis("testcontainer", "testanalysis", true));
    _thrown.expect(BatfishException.class);
    _thrown.expectMessage(
        String.format(
            "Analysis '%s' doesn't exist for container '%s'", "testanalysis", "testcontainer"));

    //should throw for the deleted analysis
    _storage.getAnalysis("testcontainer", "testanalysis");
  }

  @Test
  public void testInvalidGetEnvironment() {
    _thrown.expect(BatfishException.class);
    _thrown.expectMessage(
        String.format(
            String.format(
                "Environment '%s' doesn't exist for container '%s'->testrig '%s'",
                "fakeenvironment", "fakecontainer", "faketestrig")));

    //throws since the environment is not found in the container
    _storage.getEnvironment("fakecontainer", "faketestrig", "fakeenvironment");
  }

  @Test
  public void testSaveEnvironment() {
    Environment environment = getTestEnvironment("env");
    Environment savedEnvironment =
        _storage.saveEnvironment("testcontainer", "testtestrig", environment);

    //Test equality of persistent and transient instances
    Assert.assertThat(environment, equalTo(savedEnvironment));
  }

  @Test
  public void testInvalidSaveEnvironment() {
    Environment environment = getTestEnvironment("env");
    _storage.saveEnvironment("testcontainer", "testtestrig", environment);
    _thrown.expect(BatfishException.class);
    _thrown.expectMessage(
        String.format(
            "Environment '%s' already exists for container '%s' testrig '%s'",
            environment.getName(), "testcontainer", "testtestrig"));

    //Should throw exception for duplicate environment
    _storage.saveEnvironment("testcontainer", "testtestrig", environment);
  }

  @Test
  public void testUpdateEnvironment() {
    Environment environment = getTestEnvironment("env");
    Environment savedEnvironment =
        _storage.saveEnvironment("testcontainer", "testtestrig", environment);
    savedEnvironment.getEdgeBlacklist().add(new Edge("node3", "int3", "node4", "int4"));
    Environment updatedEnvironment =
        _storage.updateEnvironment("testcontainer", "testtestrig", savedEnvironment);

    Assert.assertThat(savedEnvironment, equalTo(updatedEnvironment));
  }

  @Test
  public void testInvalidUpdateEnvironment() {
    Environment environment = getTestEnvironment("env");
    _thrown.expect(BatfishException.class);
    _thrown.expectMessage(
        String.format(
            "Environment '%s' doesn't exist for container '%s'->testrig '%s'",
            environment.getName(), "testcontainer", "testtestrig"));

    //test that update throws for non-existent environment
    _storage.updateEnvironment("testcontainer", "testtestrig", environment);
  }

  @Test
  public void testDeleteEmptyEnvironment() {
    String containerName = "testcontainer";
    String testrigName = "testtestrig";
    String envName = "testenv";
    Environment environment = Environment.builder().setName(envName).build();
    _storage.saveEnvironment(containerName, testrigName, environment);

    assertTrue(_storage.deleteEnvironment(containerName, testrigName, envName, false));

    _thrown.expect(BatfishException.class);
    _thrown.expectMessage(
        String.format(
            String.format(
                "Environment '%s' doesn't exist for container '%s'->testrig '%s'",
                envName, containerName, testrigName)));

    //throws since the environment is not found in the container
    _storage.getEnvironment(containerName, testrigName, envName);
  }

  @Test
  public void testForceDeleteEnvironment() {
    String containerName = "testcontainer";
    String testrigName = "testtestrig";
    String envName = "testenv";
    Environment environment = getTestEnvironment(envName);
    _storage.saveEnvironment(containerName, testrigName, environment);
    _thrown.expect(BatfishException.class);
    _thrown.expectMessage(String.format("'%s' is not empty, deletion must be forced", envName));

    //Should throw for non empty environment
    _storage.deleteEnvironment(containerName, testrigName, envName, false);

    //Deletion should be possible with force=true
    assertTrue(_storage.deleteEnvironment(containerName, testrigName, envName, true));
  }

  @Test
  public void testListEnvironments() {
    String containerName = "testcontainer";
    String testrigName = "testtestrig";
    _storage.saveEnvironment(containerName, testrigName, getTestEnvironment("env1"));
    _storage.saveEnvironment(containerName, testrigName, getTestEnvironment("env2"));

    Assert.assertThat(
        _storage.listEnvironments(containerName, testrigName),
        Matchers.equalTo(Lists.newArrayList("env1", "env2")));
  }

  //Util function to get a sample test environment
  private Environment getTestEnvironment(String envName) {
    return Environment.builder()
        .setName(envName)
        .setEdgeBlacklist(Collections.singletonList(new Edge("node1", "int1", "node2", "int2")))
        .setInterfaceBlacklist(Collections.singletonList(new NodeInterfacePair("node1", "int1")))
        .setNodeBlacklist(Collections.singletonList("node1"))
        .setBgpTables(Collections.singletonMap("testbgp", "testbgpvalue"))
        .setRoutingTables(Collections.singletonMap("testrt", "testrtvalue"))
        .setExternalBgpAnnouncements("testbgpannouncement")
        .build();
  }
}
