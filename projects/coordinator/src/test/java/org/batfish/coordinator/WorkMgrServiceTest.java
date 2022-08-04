package org.batfish.coordinator;

import static org.batfish.identifiers.NodeRolesId.DEFAULT_NETWORK_NODE_ROLES_ID;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import javax.ws.rs.core.Response;
import org.batfish.common.AnswerRowsOptions;
import org.batfish.common.BatfishLogger;
import org.batfish.common.ColumnFilter;
import org.batfish.common.ColumnSortOption;
import org.batfish.common.Container;
import org.batfish.common.CoordConsts;
import org.batfish.common.WorkItem;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.common.util.CommonUtil;
import org.batfish.coordinator.authorizer.Authorizer;
import org.batfish.coordinator.config.Settings;
import org.batfish.coordinator.id.IdManager;
import org.batfish.coordinator.id.StorageBasedIdManager;
import org.batfish.datamodel.answers.Answer;
import org.batfish.datamodel.answers.AnswerMetadataUtil;
import org.batfish.datamodel.answers.AnswerStatus;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.questions.DisplayHints;
import org.batfish.datamodel.questions.InstanceData;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.questions.TestQuestion;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableMetadata;
import org.batfish.datamodel.table.TableView;
import org.batfish.identifiers.AnswerId;
import org.batfish.identifiers.NetworkId;
import org.batfish.identifiers.QuestionId;
import org.batfish.identifiers.SnapshotId;
import org.batfish.storage.FileBasedStorage;
import org.batfish.version.BatfishVersion;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** Tests for {@link WorkMgrService}. */
public class WorkMgrServiceTest {

  @Rule public TemporaryFolder _networksFolder = new TemporaryFolder();
  @Rule public TemporaryFolder _questionsTemplatesFolder = new TemporaryFolder();

  private WorkMgrService _service;

  private String _networkName;
  private String _snapshotName;
  private SnapshotId _snapshotId;
  private NetworkId _networkId;

  public WorkMgrServiceTest() {
    _networkName = "myNetwork";
    _networkId = new NetworkId(_networkName + "_id");
    _snapshotName = "mySnapshot";
    _snapshotId = new SnapshotId(_snapshotName + "_id");
  }

  private void initNetwork() {
    Settings settings = new Settings(new String[] {});
    BatfishLogger logger = new BatfishLogger("debug", false);
    Main.mainInit(
        new String[] {
          "-containerslocation",
          _networksFolder.getRoot().toString(),
          "-templatedirs",
          _questionsTemplatesFolder.getRoot().toString()
        });
    Main.setLogger(logger);
    Main.initAuthorizer();
    FileBasedStorage fbs = new FileBasedStorage(Main.getSettings().getContainersLocation(), logger);
    WorkMgr manager =
        new WorkMgr(
            settings, logger, new StorageBasedIdManager(fbs), fbs, new TestWorkExecutorCreator());
    Main.setWorkMgr(manager);
    manager.initNetwork(_networkName, null);
    manager.getIdManager().assignNetwork(_networkName, _networkId);
    _service = new WorkMgrService();
  }

  @Test
  public void getEmptyNetwork() {
    initNetwork();
    try (Response response = _service.getNetwork("100", "0.0.0", _networkName)) {
      String networkJson = response.getEntity().toString();
      assertThat(networkJson, equalTo("{\"name\":\"myNetwork\"}"));
    }
  }

  @Test
  public void getNonExistNetwork() {
    String networkName = "non-existing-folder";
    initNetwork();
    try (Response response = _service.getNetwork("100", "0.0.0", networkName)) {
      String actualMessage = response.getEntity().toString();
      String expected = "Network '" + networkName + "' not found";
      assertThat(actualMessage, equalTo(expected));
    }
  }

  @Test
  public void getNonEmptyNetwork() throws Exception {
    initNetwork();
    initSnapshot();
    try (Response response = _service.getNetwork("100", "0.0.0", _networkName)) {
      Container network =
          BatfishObjectMapper.mapper().readValue(response.getEntity().toString(), Container.class);
      Container expected =
          Container.of(_networkName, Sets.newTreeSet(Collections.singleton(_snapshotName)));
      assertThat(network, equalTo(expected));
    }
  }

  @Test
  @Deprecated
  public void getQuestionTemplates() throws Exception {
    initNetwork();
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

  // TODO Remove these tests after container and testrig keys are removed

  @Test
  public void getEmptyContainer() {
    initNetwork();
    try (Response response = _service.getNetwork("100", "0.0.0", _networkName)) {
      String networkJson = response.getEntity().toString();
      assertThat(networkJson, equalTo("{\"name\":\"myNetwork\"}"));
    }
  }

  @Test
  public void getNonExistContainer() {
    String networkName = "non-existing-folder";
    initNetwork();
    try (Response response = _service.getNetwork("100", "0.0.0", networkName)) {
      String actualMessage = response.getEntity().toString();
      String expected = "Network '" + networkName + "' not found";
      assertThat(actualMessage, equalTo(expected));
    }
  }

  private void initSnapshot() {
    Main.getWorkMgr().getIdManager().assignSnapshot(_snapshotName, _networkId, _snapshotId);
  }

  @Test
  public void getNonEmptyContainer() throws Exception {
    initNetwork();
    initSnapshot();
    try (Response response = _service.getNetwork("100", "0.0.0", _networkName)) {
      String entityStr = response.getEntity().toString();
      Container network = BatfishObjectMapper.mapper().readValue(entityStr, Container.class);
      Container expected =
          Container.of(_networkName, Sets.newTreeSet(Collections.singleton(_snapshotName)));
      assertThat(network, equalTo(expected));
    }
  }

  private static IdManager idm() {
    return Main.getWorkMgr().getIdManager();
  }

  @Test
  public void testGetAnswerRowsAdHoc() throws Exception {
    initNetwork();
    initSnapshot();
    String question = "question1";
    QuestionId questionId = new QuestionId(question + "_id");
    Question questionObj = new TestQuestion();
    String questionContent = BatfishObjectMapper.writeString(questionObj);
    String columnName = "col";
    AnswerRowsOptions answersRowsOptions =
        new AnswerRowsOptions(
            ImmutableSet.of(columnName),
            ImmutableList.of(new ColumnFilter(columnName, "", false)),
            Integer.MAX_VALUE,
            0,
            ImmutableList.of(new ColumnSortOption(columnName, false)),
            false);
    String answerRowsOptionsStr = BatfishObjectMapper.writePrettyString(answersRowsOptions);

    int value = 5;
    Answer testAnswer = new Answer();
    testAnswer.addAnswerElement(
        new TableAnswerElement(
                new TableMetadata(
                    ImmutableList.of(new ColumnMetadata(columnName, Schema.INTEGER, "foobar")),
                    new DisplayHints().getTextDesc()))
            .addRow(Row.of(columnName, value)));
    testAnswer.setStatus(AnswerStatus.SUCCESS);
    String answer = BatfishObjectMapper.writePrettyString(testAnswer);

    Main.getWorkMgr().getStorage().storeQuestion(questionContent, _networkId, questionId);
    idm().assignQuestion(question, _networkId, questionId);
    AnswerId answerId =
        idm().getAnswerId(_networkId, _snapshotId, questionId, DEFAULT_NETWORK_NODE_ROLES_ID, null);
    Main.getWorkMgr().getStorage().storeAnswer(_networkId, _snapshotId, answer, answerId);
    Main.getWorkMgr()
        .getStorage()
        .storeAnswerMetadata(
            _networkId,
            _snapshotId,
            AnswerMetadataUtil.computeAnswerMetadata(testAnswer, Main.getLogger()),
            answerId);

    JSONArray answerOutput =
        _service.getAnswerRows(
            CoordConsts.DEFAULT_API_KEY,
            BatfishVersion.getVersionStatic(),
            _networkName,
            _snapshotName,
            null,
            question,
            answerRowsOptionsStr,
            null);

    assertThat(answerOutput.get(0), equalTo(CoordConsts.SVC_KEY_SUCCESS));

    JSONObject answerJsonObject = (JSONObject) answerOutput.get(1);
    String answersJsonString = answerJsonObject.getString(CoordConsts.SVC_KEY_ANSWER);
    Answer processedAnswer =
        BatfishObjectMapper.mapper().readValue(answersJsonString, new TypeReference<Answer>() {});

    TableAnswerElement processedTable =
        (TableAnswerElement) processedAnswer.getAnswerElements().get(0);

    assertThat(processedTable.getRowsList(), equalTo(ImmutableList.of(Row.of(columnName, value))));
  }

  @Test
  public void testGetAnswerRows2AdHoc() throws Exception {
    initNetwork();
    initSnapshot();
    String question = "question1";
    QuestionId questionId = new QuestionId(question + "_id");
    Question questionObj = new TestQuestion();
    String questionContent = BatfishObjectMapper.writeString(questionObj);
    String columnName = "col";
    AnswerRowsOptions answersRowsOptions =
        new AnswerRowsOptions(
            ImmutableSet.of(columnName),
            ImmutableList.of(new ColumnFilter(columnName, "", false)),
            Integer.MAX_VALUE,
            0,
            ImmutableList.of(new ColumnSortOption(columnName, false)),
            false);
    String answerRowsOptionsStr = BatfishObjectMapper.writePrettyString(answersRowsOptions);

    int value = 5;
    Answer testAnswer = new Answer();
    testAnswer.addAnswerElement(
        new TableAnswerElement(
                new TableMetadata(
                    ImmutableList.of(new ColumnMetadata(columnName, Schema.INTEGER, "foobar")),
                    new DisplayHints().getTextDesc()))
            .addRow(Row.of(columnName, value)));
    testAnswer.setStatus(AnswerStatus.SUCCESS);
    String answer = BatfishObjectMapper.writePrettyString(testAnswer);

    Main.getWorkMgr().getStorage().storeQuestion(questionContent, _networkId, questionId);
    idm().assignQuestion(question, _networkId, questionId);
    AnswerId answerId =
        idm().getAnswerId(_networkId, _snapshotId, questionId, DEFAULT_NETWORK_NODE_ROLES_ID, null);
    Main.getWorkMgr().getStorage().storeAnswer(_networkId, _snapshotId, answer, answerId);
    Main.getWorkMgr()
        .getStorage()
        .storeAnswerMetadata(
            _networkId,
            _snapshotId,
            AnswerMetadataUtil.computeAnswerMetadata(testAnswer, Main.getLogger()),
            answerId);

    JSONArray answerOutput =
        _service.getAnswerRows2(
            CoordConsts.DEFAULT_API_KEY,
            BatfishVersion.getVersionStatic(),
            _networkName,
            _snapshotName,
            null,
            question,
            answerRowsOptionsStr,
            null);

    assertThat(answerOutput.get(0), equalTo(CoordConsts.SVC_KEY_SUCCESS));

    JSONObject answerJsonObject = (JSONObject) answerOutput.get(1);
    String answersJsonString = answerJsonObject.getString(CoordConsts.SVC_KEY_ANSWER);
    Answer processedAnswer =
        BatfishObjectMapper.mapper().readValue(answersJsonString, new TypeReference<Answer>() {});

    TableView processedTable = (TableView) processedAnswer.getAnswerElements().get(0);

    assertThat(processedTable.getInnerRows(), equalTo(ImmutableList.of(Row.of(columnName, value))));
  }

  @Test
  @Deprecated
  public void testGetWorkStatus() throws InterruptedException, ExecutionException, IOException {
    initNetwork();
    WorkMgrTestUtils.uploadTestSnapshot(_networkName, _snapshotName, _networksFolder);
    final CompletableFuture<String> networkArg = new CompletableFuture<>();

    WorkItem workItem = new WorkItem(_networkName, _snapshotName);
    Main.getWorkMgr().queueWork(workItem);

    Main.setAuthorizer(
        new Authorizer() {

          @Override
          public void authorizeContainer(String apiKey, String containerName) {
            assert Boolean.TRUE;
          }

          @Override
          public boolean isAccessibleNetwork(
              String apiKey, String containerName, boolean logError) {
            networkArg.complete(containerName);
            return true;
          }

          @Override
          public boolean isValidWorkApiKey(String apiKey) {
            return true;
          }
        });
    _service.getWorkStatus(
        CoordConsts.DEFAULT_API_KEY,
        BatfishVersion.getVersionStatic(),
        workItem.getId().toString());

    // networkArg should be name, not ID
    assertTrue(networkArg.isDone());
    assertThat(networkArg.get(), equalTo(_networkName));
  }

  static class TestWorkExecutorCreator implements WorkExecutorCreator {

    @Override
    public WorkExecutor apply(BatfishLogger batfishLogger, Settings settings) {
      return new WorkExecutor() {
        @Override
        public SubmissionResult submit(QueuedWork work) {
          throw new UnsupportedOperationException();
        }
      };
    }
  }
}
