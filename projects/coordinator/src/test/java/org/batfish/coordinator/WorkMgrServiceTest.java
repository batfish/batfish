package org.batfish.coordinator;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import javax.ws.rs.core.Response;
import org.apache.commons.io.FileUtils;
import org.batfish.common.BatfishLogger;
import org.batfish.common.BfConsts;
import org.batfish.common.Container;
import org.batfish.common.CoordConsts;
import org.batfish.common.Version;
import org.batfish.common.WorkItem;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.common.util.CommonUtil;
import org.batfish.coordinator.config.Settings;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.questions.Question.InstanceData;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** Tests for {@link WorkMgrService}. */
public class WorkMgrServiceTest {

  @Rule public TemporaryFolder _containersFolder = new TemporaryFolder();
  @Rule public TemporaryFolder _questionsTemplatesFolder = new TemporaryFolder();

  private WorkMgrService _service;

  private String _containerName = "myContainer";

  private void initContainerEnvironment() throws Exception {
    Settings settings = new Settings(new String[] {});
    BatfishLogger logger = new BatfishLogger("debug", false);
    Main.mainInit(
        new String[] {
          "-containerslocation",
          _containersFolder.getRoot().toString(),
          "-templatedirs",
          _questionsTemplatesFolder.getRoot().toString()
        });
    Main.setLogger(logger);
    Main.initAuthorizer();
    WorkMgr manager = new WorkMgr(settings, logger);
    Main.setWorkMgr(manager);
    manager.initContainer(_containerName, null);
    _service = new WorkMgrService();
  }

  @Test
  public void getEmptyContainer() throws Exception {
    initContainerEnvironment();
    Response response = _service.getNetwork("100", "0.0.0", _containerName);
    String containerJson = response.getEntity().toString();
    assertThat(containerJson, equalTo("{\"name\":\"myContainer\"}"));
  }

  @Test
  public void getNonExistContainer() throws Exception {
    String containerName = "non-existing-folder";
    initContainerEnvironment();
    Response response = _service.getNetwork("100", "0.0.0", containerName);
    String actualMessage = response.getEntity().toString();
    String expected = "Network '" + containerName + "' not found";
    assertThat(actualMessage, equalTo(expected));
  }

  @Test
  public void getContainerWithBadVersion() throws Exception {
    initContainerEnvironment();
    Response response = _service.getNetwork("100", "invalid version", _containerName);
    String actualMessage = response.getEntity().toString();
    String expected = "Illegal version 'invalid version' for Client";
    assertThat(actualMessage, equalTo(expected));
  }

  @Test
  public void getNonEmptyContainer() throws Exception {
    initContainerEnvironment();
    Path containerPath = _containersFolder.getRoot().toPath().resolve(_containerName);
    Path testrigPath = containerPath.resolve(BfConsts.RELPATH_TESTRIGS_DIR).resolve("testrig");
    assertThat(testrigPath.toFile().mkdirs(), is(true));
    Response response = _service.getNetwork("100", "0.0.0", _containerName);
    Container container =
        BatfishObjectMapper.mapper().readValue(response.getEntity().toString(), Container.class);
    Container expected =
        Container.of(_containerName, Sets.newTreeSet(Collections.singleton("testrig")));
    assertThat(container, equalTo(expected));
  }

  @Test
  public void testConfigureAnalysis() throws Exception {
    initContainerEnvironment();
    // test init and add questions to analysis
    String analysisJsonString = "{\"question\":{\"question\":\"questionContent\"}}";
    File analysisFile = _containersFolder.newFile("analysis");
    FileUtils.writeStringToFile(analysisFile, analysisJsonString);
    _service.configureAnalysis(
        CoordConsts.DEFAULT_API_KEY,
        Version.getVersion(),
        _containerName,
        "new",
        "analysis",
        new FileInputStream(analysisFile),
        "",
        null);
    Path questionPath =
        _containersFolder
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
        questionsToDelete,
        null);
    assertFalse(Files.exists(questionPath));
    JSONArray result =
        _service.configureAnalysis(
            CoordConsts.DEFAULT_API_KEY,
            Version.getVersion(),
            _containerName,
            "",
            "analysis",
            null,
            questionsToDelete,
            null);
    assertThat(result.getString(0), equalTo(CoordConsts.SVC_KEY_FAILURE));
  }

  @Test
  public void testGetAnalysisAnswer() throws Exception {
    String testrigName = "testrig1";
    String analysisName = "analysis1";
    String questionName = "question1";
    String questionContent = "questionContent";
    String question2Name = "question2Name";
    String answer = "answerContent";

    initContainerEnvironment();

    String analysisJsonString =
        String.format("{\"%s\":{\"question\":\"%s\"}}", questionName, questionContent);
    File analysisFile = _containersFolder.newFile(analysisName);
    FileUtils.writeStringToFile(analysisFile, analysisJsonString);

    _service.configureAnalysis(
        CoordConsts.DEFAULT_API_KEY,
        Version.getVersion(),
        _containerName,
        "new",
        analysisName,
        new FileInputStream(analysisFile),
        "",
        null);

    Path answerDir =
        _containersFolder
            .getRoot()
            .toPath()
            .resolve(
                Paths.get(
                    _containerName,
                    BfConsts.RELPATH_TESTRIGS_DIR,
                    testrigName,
                    BfConsts.RELPATH_ANALYSES_DIR,
                    analysisName,
                    BfConsts.RELPATH_QUESTIONS_DIR,
                    questionName,
                    BfConsts.RELPATH_ENVIRONMENTS_DIR,
                    BfConsts.RELPATH_DEFAULT_ENVIRONMENT_NAME));

    Path answer1Path = answerDir.resolve(BfConsts.RELPATH_ANSWER_JSON);
    answerDir.toFile().mkdirs();
    CommonUtil.writeFile(answer1Path, answer);

    WorkItem workItem = new WorkItem(_containerName, testrigName);
    String workItemString = BatfishObjectMapper.mapper().writeValueAsString(workItem);

    JSONArray answer1Output =
        _service.getAnalysisAnswer(
            CoordConsts.DEFAULT_API_KEY,
            Version.getVersion(),
            _containerName,
            testrigName,
            BfConsts.RELPATH_DEFAULT_ENVIRONMENT_NAME,
            null,
            null,
            analysisName,
            questionName,
            workItemString);

    JSONArray answer2Output =
        _service.getAnalysisAnswer(
            CoordConsts.DEFAULT_API_KEY,
            Version.getVersion(),
            _containerName,
            testrigName,
            BfConsts.RELPATH_DEFAULT_ENVIRONMENT_NAME,
            null,
            null,
            analysisName,
            question2Name,
            null);

    assertThat(answer1Output.get(0), equalTo(CoordConsts.SVC_KEY_SUCCESS));
    assertThat(answer2Output.get(0), equalTo(CoordConsts.SVC_KEY_FAILURE));

    JSONObject answerJsonObject = (JSONObject) answer1Output.get(1);
    String answerJsonString = answerJsonObject.getString(CoordConsts.SVC_KEY_ANSWER);
    String answerString =
        BatfishObjectMapper.mapper().readValue(answerJsonString, new TypeReference<String>() {});
    assertThat(answerString, equalTo(answer));
  }

  @Test
  public void getConfigNonExistContainer() throws Exception {
    initContainerEnvironment();
    Response response =
        _service.getConfiguration(
            CoordConsts.DEFAULT_API_KEY,
            Version.getVersion(),
            "nonExistContainer",
            "testrig",
            "config1.cfg");
    String actualMessage = response.getEntity().toString();
    assertThat(actualMessage, equalTo("Network 'nonExistContainer' not found"));
  }

  @Test
  public void getNonExistConfig() throws Exception {
    initContainerEnvironment();
    Path containerDir = _containersFolder.getRoot().toPath().resolve(_containerName);
    Path testrigPath =
        containerDir.resolve(
            Paths.get(BfConsts.RELPATH_TESTRIGS_DIR, "testrig", BfConsts.RELPATH_TEST_RIG_DIR));
    assertTrue(testrigPath.toFile().mkdirs());
    Response response =
        _service.getConfiguration(
            CoordConsts.DEFAULT_API_KEY,
            Version.getVersion(),
            _containerName,
            "testrig",
            "config.cfg");
    String actualMessage = response.getEntity().toString();
    String expected =
        "Configuration file config.cfg does not exist in testrig testrig for container myContainer";
    assertThat(actualMessage, equalTo(expected));
  }

  @Test
  public void getConfigContent() throws Exception {
    initContainerEnvironment();
    Path containerPath = _containersFolder.getRoot().toPath().resolve(_containerName);
    Path configPath =
        containerPath.resolve(
            Paths.get(
                BfConsts.RELPATH_TESTRIGS_DIR,
                "testrig",
                BfConsts.RELPATH_TEST_RIG_DIR,
                BfConsts.RELPATH_CONFIGURATIONS_DIR));
    assertTrue(configPath.toFile().mkdirs());
    CommonUtil.writeFile(configPath.resolve("config.cfg"), "config content");
    Response response =
        _service.getConfiguration(
            CoordConsts.DEFAULT_API_KEY,
            Version.getVersion(),
            _containerName,
            "testrig",
            "config.cfg");
    String actualMessage = response.getEntity().toString();
    assertThat(actualMessage, equalTo("config content"));
  }

  @Test
  public void getQuestionTemplates() throws Exception {
    initContainerEnvironment();
    Question testQuestion = createTestQuestion("testquestion", "test description");
    ObjectMapper mapper = BatfishObjectMapper.mapper();
    // serializing the question in the temp questions folder
    String questionJsonString = mapper.writeValueAsString(testQuestion);
    CommonUtil.writeFile(
        _questionsTemplatesFolder.newFile("testQuestion.json").toPath(), questionJsonString);
    JSONArray questionsResponse = _service.getQuestionTemplates(CoordConsts.DEFAULT_API_KEY);

    // testting if the response is valid and contains testquestion
    if (questionsResponse.get(0).equals(CoordConsts.SVC_KEY_SUCCESS)) {
      JSONObject questionsJsonObject = (JSONObject) questionsResponse.get(1);
      String questionsJsonString = questionsJsonObject.getString(CoordConsts.SVC_KEY_QUESTION_LIST);
      Map<String, String> questionsMap =
          mapper.readValue(questionsJsonString, new TypeReference<Map<String, String>>() {});
      if (questionsMap.containsKey("testquestion")) {
        assertThat(questionsMap.get("testquestion"), is(equalTo(questionJsonString)));
      } else {
        fail("Question not found in the response");
      }
    } else {
      fail("Service call was not successful");
    }
  }

  private Question createTestQuestion(String name, String description) {
    InstanceData instanceData = new InstanceData();
    instanceData.setDescription(description);
    instanceData.setInstanceName(name);
    Question testQuestion =
        new Question() {
          @Override
          public String getName() {
            return "test";
          }

          @Override
          public boolean getDataPlane() {
            return false;
          }

          @Override
          public boolean equals(Object obj) {
            if (obj == null) {
              return false;
            }
            if (getClass() != obj.getClass()) {
              return false;
            }
            final Question other = (Question) obj;
            return Objects.equals(
                    getInstance().getInstanceName(), other.getInstance().getInstanceName())
                && Objects.equals(
                    getInstance().getDescription(), other.getInstance().getDescription());
          }

          @Override
          public int hashCode() {
            return Objects.hash(getInstance().getInstanceName(), getInstance().getDescription());
          }
        };

    testQuestion.setInstance(instanceData);
    return testQuestion;
  }
}
