package org.batfish.coordinator;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.nio.file.Files;
import java.nio.file.Path;
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

/** Tests for {@link WorkMgrService}. */
public class WorkMgrServiceTest {

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private WorkMgr _manager;

  private WorkMgrService _service;

  private String _containerName = "myContainer";

  private void initContainerEnvironment() throws Exception {
    Settings settings = new Settings(new String[] {});
    BatfishLogger logger = new BatfishLogger("debug", false);
    Main.mainInit(new String[] {"-containerslocation", _folder.getRoot().toString()});
    Main.initAuthorizer();
    Main.setLogger(logger);
    _manager = new WorkMgr(settings, logger);
    Main.setWorkMgr(_manager);
    _manager.initContainer(_containerName, null);
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
    Path testrigPath = containerPath.resolve(BfConsts.RELPATH_TESTRIGS_DIR).resolve("testrig");
    assertThat(testrigPath.toFile().mkdirs(), is(true));
    Response response = _service.getContainer("100", "0.0.0", _containerName);
    BatfishObjectMapper mapper = new BatfishObjectMapper();
    Container container = mapper.readValue(response.getEntity().toString(), Container.class);
    Container expected =
        new Container(_containerName, Lists.newArrayList("testrig"), Maps.newHashMap());
    assertThat(container, equalTo(expected));
  }

  @Test
  public void testDeleteQuestionsFromAnalysis() throws Exception {
    initContainerEnvironment();
    Path containerPath = _folder.getRoot().toPath().resolve(_containerName);
    Path analysisPath = containerPath.resolve(BfConsts.RELPATH_ANALYSES_DIR).resolve("analysis");
    assertTrue(analysisPath.toFile().mkdirs());
    Path questionPath = analysisPath.resolve(BfConsts.RELPATH_QUESTIONS_DIR).resolve("question");
    assertTrue(questionPath.toFile().mkdirs());
    String questionsToDelete = "[question]";
    _service.configureAnalysis(
        CoordConsts.DEFAULT_API_KEY,
        Version.getVersion(),
        _containerName,
        "",
        "analysis",
        null,
        questionsToDelete);
    assertFalse(Files.exists(questionPath));
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
