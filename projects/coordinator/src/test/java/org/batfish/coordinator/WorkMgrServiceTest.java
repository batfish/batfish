package org.batfish.coordinator;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.ws.rs.core.Response;
import org.batfish.common.BatfishLogger;
import org.batfish.common.BfConsts;
import org.batfish.common.Container;
import org.batfish.common.CoordConsts;
import org.batfish.common.Version;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.coordinator.config.Settings;
import org.codehaus.jettison.json.JSONArray;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class WorkMgrServiceTest {

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private WorkMgr _manager;

  private WorkMgrService _service;

  private String _containerName = "myContainer";

  private void initContainerEnvironment() throws Exception {
    Settings settings = new Settings(new String[] {});
    BatfishLogger logger = new BatfishLogger("debug", false);
    Main.mainInit(new String[] {});
    _folder.newFolder(_containerName);
    Main.mainInit(new String[] {"-containerslocation", _folder.getRoot().toString()});
    Main.initAuthorizer();
    Main.setLogger(logger);
    _manager = new WorkMgr(settings, logger);
    Main.setWorkMgr(_manager);
    _service = new WorkMgrService();
  }

  @Test
  public void getEmptyContainer() throws Exception {
    initContainerEnvironment();
    Response response = _service.getContainer("100", "0.0.0", _containerName);
    String containerJson = response.getEntity().toString();
    String expected = "{\n  \"name\" : \"myContainer\"\n}";
    assertThat(containerJson, equalTo(expected));
  }

  @Test
  public void getNonExistContainer() throws Exception {
    String containerName = "non-existing-folder";
    initContainerEnvironment();
    Response response = _service.getContainer("100", "0.0.0", containerName);
    String actualMessage = response.getEntity().toString();
    String expected = "Container '" + containerName + "' not found";
    assertThat(actualMessage, equalTo(expected));
  }

  @Test
  public void getContainerWithBadVersion() throws Exception {
    initContainerEnvironment();
    Response response = _service.getContainer("100", "invalid version", _containerName);
    String actualMessage = response.getEntity().toString();
    String expected = "Illegal version 'invalid version' for Client";
    assertThat(actualMessage, equalTo(expected));
  }

  @Test
  public void getNonEmptyContainer() throws Exception {
    initContainerEnvironment();
    Path containerPath = _folder.getRoot().toPath().resolve(_containerName);
    Path testrigPath = containerPath.resolve("testrig1");
    assertThat(testrigPath.toFile().mkdir(), is(true));
    Path testrigPath2 = containerPath.resolve("testrig2");
    assertThat(testrigPath2.toFile().mkdir(), is(true));
    Response response = _service.getContainer("100", "0.0.0", _containerName);
    BatfishObjectMapper mapper = new BatfishObjectMapper();
    Container container = mapper.readValue(response.getEntity().toString(), Container.class);
    assertThat(container.getName(), equalTo(_containerName));
    SortedSet<String> expectedTestrigs = new TreeSet<>();
    expectedTestrigs.add("testrig1");
    expectedTestrigs.add("testrig2");
    assertThat(container.getTestrigs(), equalTo(expectedTestrigs));
  }

  @Test
  public void testDeleteQuestionsFromAnalysis() throws Exception {
    initContainerEnvironment();
    Path containerPath = _folder.getRoot().toPath().resolve(_containerName);
    Path analysisPath = containerPath.resolve(BfConsts.RELPATH_ANALYSES_DIR).resolve("analysis");
    assertTrue(analysisPath.toFile().mkdirs());
    Path question1Path = analysisPath.resolve(BfConsts.RELPATH_QUESTIONS_DIR).resolve("question1");
    assertTrue(question1Path.toFile().mkdirs());
    Path question2Path = analysisPath.resolve(BfConsts.RELPATH_QUESTIONS_DIR).resolve("question2");
    assertTrue(question2Path.toFile().mkdirs());
    Path question3Path = analysisPath.resolve(BfConsts.RELPATH_QUESTIONS_DIR).resolve("question3");
    assertTrue(question3Path.toFile().mkdirs());
    String questionsToDelete = "";
    _service.configureAnalysis(
        CoordConsts.DEFAULT_API_KEY,
        Version.getVersion(),
        _containerName,
        "",
        "analysis",
        null,
        questionsToDelete);
    assertTrue(Files.exists(question1Path));
    assertTrue(Files.exists(question2Path));
    assertTrue(Files.exists(question3Path));
    JSONArray delQuestionsArray = new JSONArray();
    delQuestionsArray.put("question1");
    delQuestionsArray.put("question2");
    questionsToDelete = delQuestionsArray.toString(1);
    _service.configureAnalysis(
        CoordConsts.DEFAULT_API_KEY,
        Version.getVersion(),
        _containerName,
        "",
        "analysis",
        null,
        questionsToDelete);
    assertFalse(Files.exists(question1Path));
    assertFalse(Files.exists(question2Path));
    assertTrue(Files.exists(question3Path));
    delQuestionsArray.remove("question2");
    questionsToDelete = delQuestionsArray.toString(1);
    JSONArray result =
        _service.configureAnalysis(
            CoordConsts.DEFAULT_API_KEY,
            Version.getVersion(),
            _containerName,
            "",
            "analysis",
            null,
            questionsToDelete);
    assertThat(result.getString(0), equalTo(CoordConsts.SVC_KEY_FAILURE));
  }
}
