package org.batfish.coordinator.resources;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.OK;
import static org.batfish.coordinator.WorkMgrTestUtils.setupQuestionAndAnswer;
import static org.batfish.coordinator.WorkMgrTestUtils.uploadTestSnapshot;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

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
import org.batfish.coordinator.WorkMgrServiceV2TestBase;
import org.batfish.coordinator.WorkMgrTestUtils;
import org.batfish.datamodel.answers.Answer;
import org.batfish.datamodel.answers.StringAnswerElement;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

@ParametersAreNonnullByDefault
public class AnswerResourceTest extends WorkMgrServiceV2TestBase {
  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private Builder getAnswerTarget(String network, String question, @Nullable String snapshot) {
    return addHeader(
        addSnapshotQuery(
            target(CoordConsts.SVC_CFG_WORK_MGR2)
                .path(CoordConstsV2.RSC_NETWORKS)
                .path(network)
                .path(CoordConstsV2.RSC_QUESTIONS)
                .path(question)
                .path(CoordConstsV2.RSC_ANSWER),
            snapshot));
  }

  private Builder getAnalysisAnswerTarget(
      String network, String question, String analysis, @Nullable String snapshot) {
    return addHeader(
        addSnapshotQuery(
            target(CoordConsts.SVC_CFG_WORK_MGR2)
                .path(CoordConstsV2.RSC_NETWORKS)
                .path(network)
                .path(CoordConstsV2.RSC_ANALYSES)
                .path(analysis)
                .path(CoordConstsV2.RSC_QUESTIONS)
                .path(question)
                .path(CoordConstsV2.RSC_ANSWER),
            snapshot));
  }

  private static WebTarget addSnapshotQuery(WebTarget webTarget, @Nullable String snapshot) {
    return snapshot == null ? webTarget : webTarget.queryParam("snapshot", snapshot);
  }

  private static Builder addHeader(WebTarget target) {
    return target
        .request()
        .header(CoordConstsV2.HTTP_HEADER_BATFISH_APIKEY, CoordConsts.DEFAULT_API_KEY)
        .header(CoordConstsV2.HTTP_HEADER_BATFISH_VERSION, Version.getVersion());
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

    Response response = getAnswerTarget(network, question, snapshot).get();

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

    Response response = getAnalysisAnswerTarget(network, question, analysis, snapshot).get();

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

    Response responseNoSnapshot = getAnswerTarget(network, question, null).get();
    Response responseBadSnap = getAnswerTarget(network, question, bogusSnapshot).get();

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
    Response responseNoNetwork = getAnswerTarget(network, question, null).get();

    Main.getWorkMgr().initNetwork(network, null);
    Response responseNoQuestion = getAnswerTarget(network, question, null).get();

    uploadTestSnapshot(network, snapshot, _folder);
    setupQuestionAndAnswer(network, snapshot, question, null, null);
    Response responseNoAns = getAnswerTarget(network, question, snapshot).get();

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
