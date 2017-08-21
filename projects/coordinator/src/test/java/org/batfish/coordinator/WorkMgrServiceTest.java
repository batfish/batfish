package org.batfish.coordinator;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Sets;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import javax.ws.rs.core.Response;
import org.apache.commons.io.FileUtils;
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

  private WorkMgrService _service;

  private String _containerName = "myContainer";

  private void initContainerEnvironment() throws Exception {
    Settings settings = new Settings(new String[] {});
    BatfishLogger logger = new BatfishLogger("debug", false);
    Main.mainInit(new String[] {"-containerslocation", _folder.getRoot().toString()});
    Main.initAuthorizer();
    Main.setLogger(logger);
    WorkMgr manager = new WorkMgr(settings, logger);
    Main.setWorkMgr(manager);
    manager.initContainer(_containerName, null);
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
        Container.of(_containerName, Sets.newTreeSet(Collections.singleton("testrig")));
    assertThat(container, equalTo(expected));
  }

  @Test
  public void testConfigureAnalysis() throws Exception {
    initContainerEnvironment();
    // test init and add questions to analysis
    String analysisJsonString = "{question:{question: questionContent}}";
    File analysisFile = _folder.newFile("analysis");
    FileUtils.writeStringToFile(analysisFile, analysisJsonString);
    _service.configureAnalysis(
        CoordConsts.DEFAULT_API_KEY,
        Version.getVersion(),
        _containerName,
        "new",
        "analysis",
        new FileInputStream(analysisFile),
        "");
    Path questionPath =
        _folder
            .getRoot()
            .toPath()
            .resolve(
                Paths.get(
                    _containerName,
                    BfConsts.RELPATH_ANALYSES_DIR,
                    "analysis",
                    BfConsts.RELPATH_QUESTIONS_DIR,
                    "question"));
    assertTrue(Files.exists(questionPath));
    // test delete questions
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
