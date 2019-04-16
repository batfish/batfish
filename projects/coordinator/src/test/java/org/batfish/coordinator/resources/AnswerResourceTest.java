package org.batfish.coordinator.resources;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.OK;
import static org.batfish.coordinator.WorkMgrTestUtils.setupQuestionAndAnswer;
import static org.batfish.coordinator.WorkMgrTestUtils.uploadTestSnapshot;
import static org.batfish.identifiers.NodeRolesId.DEFAULT_NETWORK_NODE_ROLES_ID;
import static org.batfish.identifiers.QuestionSettingsId.DEFAULT_QUESTION_SETTINGS_ID;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import java.io.IOException;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import org.batfish.common.CoordConsts;
import org.batfish.common.CoordConstsV2;
import org.batfish.common.Version;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.coordinator.Main;
import org.batfish.coordinator.WorkMgr;
import org.batfish.coordinator.WorkMgrServiceV2TestBase;
import org.batfish.coordinator.WorkMgrTestUtils;
import org.batfish.coordinator.id.IdManager;
import org.batfish.datamodel.answers.Answer;
import org.batfish.datamodel.answers.AnswerMetadata;
import org.batfish.datamodel.answers.AnswerMetadataUtil;
import org.batfish.datamodel.answers.StringAnswerElement;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.questions.TestQuestion;
import org.batfish.identifiers.AnalysisId;
import org.batfish.identifiers.AnswerId;
import org.batfish.identifiers.NetworkId;
import org.batfish.identifiers.QuestionId;
import org.batfish.identifiers.SnapshotId;
import org.batfish.storage.StorageProvider;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

@ParametersAreNonnullByDefault
public class AnswerResourceTest extends WorkMgrServiceV2TestBase {
  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private WebTarget getAnswerTarget(String network, String question) {
    return target(CoordConsts.SVC_CFG_WORK_MGR2)
        .path(CoordConstsV2.RSC_NETWORKS)
        .path(network)
        .path(CoordConstsV2.RSC_QUESTIONS)
        .path(question)
        .path(CoordConstsV2.RSC_ANSWER);
  }

  private WebTarget getAnalysisAnswerTarget(String network, String question, String analysis) {
    return target(CoordConsts.SVC_CFG_WORK_MGR2)
        .path(CoordConstsV2.RSC_NETWORKS)
        .path(network)
        .path(CoordConstsV2.RSC_ANALYSES)
        .path(analysis)
        .path(CoordConstsV2.RSC_QUESTIONS)
        .path(question)
        .path(CoordConstsV2.RSC_ANSWER);
  }

  private Builder addHeader(WebTarget target) {
    return target
        .request()
        .header(CoordConstsV2.HTTP_HEADER_BATFISH_APIKEY, CoordConsts.DEFAULT_API_KEY)
        .header(CoordConstsV2.HTTP_HEADER_BATFISH_VERSION, Version.getVersion());
  }

  /** Setup question and optionally answer on specified network and snapshot */
  private void setupQuestionAndAnswerOrg(
      String network,
      String snapshot,
      String questionName,
      @Nullable String analysis,
      @Nullable Answer answer)
      throws IOException {
    WorkMgr manager = Main.getWorkMgr();
    IdManager idManager = manager.getIdManager();
    StorageProvider storage = manager.getStorage();
    Question question = new TestQuestion();
    NetworkId networkId = idManager.getNetworkId(network);
    SnapshotId snapshotId = idManager.getSnapshotId(snapshot, networkId);
    AnalysisId analysisId = null;
    if (analysis != null) {
      manager.configureAnalysis(
          network,
          true,
          analysis,
          ImmutableMap.of(questionName, BatfishObjectMapper.writeString(question)),
          Lists.newArrayList(),
          null);
      analysisId = idManager.getAnalysisId(analysis, networkId);
    } else {
      idManager.assignQuestion(questionName, networkId, idManager.generateQuestionId(), null);
    }
    QuestionId questionId = idManager.getQuestionId(questionName, networkId, analysisId);
    storage.storeQuestion(
        BatfishObjectMapper.writeString(question), networkId, questionId, analysisId);

    // Setup answer iff one was passed in
    if (answer != null) {
      AnswerId answerId =
          idManager.getBaseAnswerId(
              networkId,
              snapshotId,
              questionId,
              DEFAULT_QUESTION_SETTINGS_ID,
              DEFAULT_NETWORK_NODE_ROLES_ID,
              null,
              analysisId);
      String answerStr = BatfishObjectMapper.writeString(answer);
      AnswerMetadata answerMetadata =
          AnswerMetadataUtil.computeAnswerMetadata(answer, Main.getLogger());
      storage.storeAnswer(answerStr, answerId);
      storage.storeAnswerMetadata(answerMetadata, answerId);
    }
  }

  @Before
  public void initTestEnvironment() throws Exception {
    WorkMgrTestUtils.initWorkManager(_folder);
  }

  @Test
  public void testGetAnswer() throws IOException {
    String network = "network";
    String snapshot = "snapshot";
    String question = "question";

    Main.getWorkMgr().initNetwork(network, null);
    uploadTestSnapshot(network, snapshot, _folder);
    Answer expectedAnswer = new Answer();
    expectedAnswer.addAnswerElement(new StringAnswerElement("foo1"));
    String expectedAnswerString = BatfishObjectMapper.writeString(expectedAnswer);
    setupQuestionAndAnswer(network, snapshot, question, null, expectedAnswer);

    Response response =
        addHeader(getAnswerTarget(network, question).queryParam("snapshot", snapshot)).get();

    // Confirm the existing answer is successfully fetched
    assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
    assertThat(
        BatfishObjectMapper.writeString(response.readEntity(Answer.class)),
        equalTo(expectedAnswerString));
  }

  @Test
  public void testGetAnalysisAnswer() throws IOException {
    String network = "network";
    String analysis = "analysis";
    String snapshot = "snapshot";
    String question = "question";

    Main.getWorkMgr().initNetwork(network, null);
    uploadTestSnapshot(network, snapshot, _folder);
    Answer expectedAnswer = new Answer();
    expectedAnswer.addAnswerElement(new StringAnswerElement("foo1"));
    String expectedAnswerString = BatfishObjectMapper.writeString(expectedAnswer);
    setupQuestionAndAnswer(network, snapshot, question, analysis, expectedAnswer);

    Response response =
        addHeader(
                getAnalysisAnswerTarget(network, question, analysis)
                    .queryParam("snapshot", snapshot))
            .get();

    // Confirm the existing analysis answer is successfully fetched
    assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
    assertThat(
        BatfishObjectMapper.writeString(response.readEntity(Answer.class)),
        equalTo(expectedAnswerString));
  }

  @Test
  public void testGetAnswerBadQuery() throws IOException {
    String network = "network";
    String snapshot = "snapshot";
    String bogusSnapshot = "bogusSnapshot";
    String question = "question";

    Main.getWorkMgr().initNetwork(network, null);
    uploadTestSnapshot(network, snapshot, _folder);
    setupQuestionAndAnswer(network, snapshot, question, null, null);

    Response responseNoSnapshot = addHeader(getAnswerTarget(network, question)).get();
    Response responseBadSnap =
        addHeader(getAnswerTarget(network, question).queryParam("snapshot", bogusSnapshot)).get();

    // Missing snapshot name should result in bad request
    assertThat(responseNoSnapshot.getStatus(), equalTo(BAD_REQUEST.getStatusCode()));
    assertThat(
        responseNoSnapshot.readEntity(String.class), containsString("Snapshot must be specified"));

    // Invalid snapshot name should result bad request
    assertThat(responseBadSnap.getStatus(), equalTo(BAD_REQUEST.getStatusCode()));
    assertThat(responseBadSnap.readEntity(String.class), containsString("non-existent snapshot"));
  }

  @Test
  public void testGetAnswerNotFound() throws IOException {
    String network = "network";
    String snapshot = "snapshot";
    String question = "question";
    Response responseNoNetwork = addHeader(getAnswerTarget(network, question)).get();

    Main.getWorkMgr().initNetwork(network, null);
    Response responseNoQuestion = addHeader(getAnswerTarget(network, question)).get();

    uploadTestSnapshot(network, snapshot, _folder);
    setupQuestionAndAnswer(network, snapshot, question, null, null);
    Response responseNoAns =
        addHeader(getAnswerTarget(network, question).queryParam("snapshot", snapshot)).get();

    // No network should result in 404
    assertThat(responseNoNetwork.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
    assertThat(
        responseNoNetwork.readEntity(String.class),
        containsString(String.format("Network '%s' does not exist", network)));

    // No question should result in 404
    assertThat(responseNoQuestion.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
    assertThat(
        responseNoQuestion.readEntity(String.class),
        containsString(String.format("Question '%s' does not exist", question)));

    // No answer should result in 404
    assertThat(responseNoAns.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
    assertThat(responseNoAns.readEntity(String.class), containsString("Answer not found"));
  }
}
