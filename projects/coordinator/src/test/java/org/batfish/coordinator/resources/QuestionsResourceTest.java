package org.batfish.coordinator.resources;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedSet;
import java.io.IOException;
import java.util.SortedSet;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.batfish.common.CoordConsts;
import org.batfish.common.CoordConstsV2;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.coordinator.Main;
import org.batfish.coordinator.WorkMgrServiceV2TestBase;
import org.batfish.coordinator.WorkMgrTestUtils;
import org.batfish.datamodel.questions.TestQuestion;
import org.batfish.version.BatfishVersion;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

@ParametersAreNonnullByDefault
public final class QuestionsResourceTest extends WorkMgrServiceV2TestBase {

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private Builder getQuestionTargetAdHoc(String network, String question) {
    return target(CoordConsts.SVC_CFG_WORK_MGR2)
        .path(CoordConstsV2.RSC_NETWORKS)
        .path(network)
        .path(CoordConstsV2.RSC_QUESTIONS)
        .path(question)
        .request()
        .header(CoordConstsV2.HTTP_HEADER_BATFISH_APIKEY, CoordConsts.DEFAULT_API_KEY)
        .header(CoordConstsV2.HTTP_HEADER_BATFISH_VERSION, BatfishVersion.getVersionStatic());
  }

  private Builder getQuestionTargetAnalysis(String network, String question, String analysis) {
    return target(CoordConsts.SVC_CFG_WORK_MGR2)
        .path(CoordConstsV2.RSC_NETWORKS)
        .path(network)
        .path(CoordConstsV2.RSC_ANALYSES)
        .path(analysis)
        .path(CoordConstsV2.RSC_QUESTIONS)
        .path(question)
        .request()
        .header(CoordConstsV2.HTTP_HEADER_BATFISH_APIKEY, CoordConsts.DEFAULT_API_KEY)
        .header(CoordConstsV2.HTTP_HEADER_BATFISH_VERSION, BatfishVersion.getVersionStatic());
  }

  private Builder getTargetAdHoc(String network) {
    return target(CoordConsts.SVC_CFG_WORK_MGR2)
        .path(CoordConstsV2.RSC_NETWORKS)
        .path(network)
        .path(CoordConstsV2.RSC_QUESTIONS)
        .request()
        .header(CoordConstsV2.HTTP_HEADER_BATFISH_APIKEY, CoordConsts.DEFAULT_API_KEY)
        .header(CoordConstsV2.HTTP_HEADER_BATFISH_VERSION, BatfishVersion.getVersionStatic());
  }

  private Builder getTargetAnalysis(String network, String analysis) {
    return target(CoordConsts.SVC_CFG_WORK_MGR2)
        .path(CoordConstsV2.RSC_NETWORKS)
        .path(network)
        .path(CoordConstsV2.RSC_ANALYSES)
        .path(analysis)
        .path(CoordConstsV2.RSC_QUESTIONS)
        .request()
        .header(CoordConstsV2.HTTP_HEADER_BATFISH_APIKEY, CoordConsts.DEFAULT_API_KEY)
        .header(CoordConstsV2.HTTP_HEADER_BATFISH_VERSION, BatfishVersion.getVersionStatic());
  }

  @Before
  public void initTestEnvironment() {
    WorkMgrTestUtils.initWorkManager(_folder);
  }

  @Test
  public void testDeleteQuestionAdHocMissingNetwork() {
    String network = "network1";
    String question = "question1";
    try (Response response = getQuestionTargetAdHoc(network, question).delete()) {
      assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
    }
  }

  @Test
  public void testDeleteQuestionAdHocMissingQuestion() {
    String network = "network1";
    String question = "question1";
    Main.getWorkMgr().initNetwork(network, null);
    try (Response response = getQuestionTargetAdHoc(network, question).delete()) {
      assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
    }
  }

  @Test
  public void testDeleteQuestionAdHocSuccess() throws IOException {
    String network = "network1";
    String question = "question1";
    Main.getWorkMgr().initNetwork(network, null);
    Main.getWorkMgr()
        .uploadQuestion(network, question, BatfishObjectMapper.writeString(new TestQuestion()));
    try (Response response = getQuestionTargetAdHoc(network, question).delete()) {
      // should succed the first time
      assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
    }

    try (Response response = getQuestionTargetAdHoc(network, question).delete()) {
      // should fail the second time
      assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
    }
  }

  @Test
  public void testDeleteQuestionAnalysisMissingAnalysis() {
    String network = "network1";
    String question = "question1";
    String analysis = "analysis1";
    Main.getWorkMgr().initNetwork(network, null);
    try (Response response = getQuestionTargetAnalysis(network, question, analysis).delete()) {
      assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
    }
  }

  @Test
  public void testDeleteQuestionAnalysisMissingNetwork() {
    String network = "network1";
    String question = "question1";
    String analysis = "analysis1";
    try (Response response = getQuestionTargetAnalysis(network, question, analysis).delete()) {
      assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
    }
  }

  @Test
  public void testDeleteQuestionAnalysisMissingQuestion() throws IOException {
    String network = "network1";
    String question = "question1";
    String analysis = "analysis1";
    Main.getWorkMgr().initNetwork(network, null);
    Main.getWorkMgr()
        .configureAnalysis(
            network,
            true,
            analysis,
            ImmutableMap.of(question, BatfishObjectMapper.writeString(new TestQuestion())),
            ImmutableList.of(),
            false);

    try (Response response =
        getQuestionTargetAnalysis(network, "someOtherQuestion", analysis).delete()) {
      assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
    }
  }

  @Test
  public void testDeleteQuestionAnalysisSuccess() throws IOException {
    String network = "network1";
    String question = "question1";
    String analysis = "analysis1";
    Main.getWorkMgr().initNetwork(network, null);
    Main.getWorkMgr()
        .configureAnalysis(
            network,
            true,
            analysis,
            ImmutableMap.of(question, BatfishObjectMapper.writeString(new TestQuestion())),
            ImmutableList.of(),
            false);
    try (Response response = getQuestionTargetAnalysis(network, question, analysis).delete()) {
      // should succed the first time
      assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
    }

    try (Response response = getQuestionTargetAnalysis(network, question, analysis).delete()) {
      // should fail the second time
      assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
    }
  }

  @Test
  public void testGetQuestionAdHocMissingNetwork() {
    String network = "network1";
    String question = "question1";
    try (Response response = getQuestionTargetAdHoc(network, question).get()) {
      assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
    }
  }

  @Test
  public void testGetQuestionAdHocMissingQuestion() {
    String network = "network1";
    String question = "question1";
    Main.getWorkMgr().initNetwork(network, null);
    try (Response response = getQuestionTargetAdHoc(network, question).get()) {
      assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
    }
  }

  @Test
  public void testGetQuestionAdHocSuccess() throws IOException {
    String network = "network1";
    String question = "question1";
    String questionJson = BatfishObjectMapper.writeString(new TestQuestion());
    Main.getWorkMgr().initNetwork(network, null);
    Main.getWorkMgr().uploadQuestion(network, question, questionJson);
    try (Response response = getQuestionTargetAdHoc(network, question).get()) {
      assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
      assertThat(response.readEntity(String.class), equalTo(questionJson));
    }
  }

  @Test
  public void testGetQuestionAnalysisMissingAnalysis() {
    String network = "network1";
    String question = "question1";
    String analysis = "analysis1";
    Main.getWorkMgr().initNetwork(network, null);
    try (Response response = getQuestionTargetAnalysis(network, question, analysis).get()) {
      assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
    }
  }

  @Test
  public void testGetQuestionAnalysisMissingNetwork() {
    String network = "network1";
    String question = "question1";
    String analysis = "analysis1";
    try (Response response = getQuestionTargetAnalysis(network, question, analysis).get()) {
      assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
    }
  }

  @Test
  public void testGetQuestionAnalysisMissingQuestion() throws IOException {
    String network = "network1";
    String question = "question1";
    String analysis = "analysis1";
    String questionJson = BatfishObjectMapper.writeString(new TestQuestion());
    Main.getWorkMgr().initNetwork(network, null);
    Main.getWorkMgr()
        .configureAnalysis(
            network,
            true,
            analysis,
            ImmutableMap.of(question, questionJson),
            ImmutableList.of(),
            false);
    try (Response response =
        getQuestionTargetAnalysis(network, "someOtherQuestion", analysis).get()) {
      assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
    }
  }

  @Test
  public void testGetQuestionAnalysisSuccess() throws IOException {
    String network = "network1";
    String question = "question1";
    String analysis = "analysis1";
    String questionJson = BatfishObjectMapper.writeString(new TestQuestion());
    Main.getWorkMgr().initNetwork(network, null);
    Main.getWorkMgr()
        .configureAnalysis(
            network,
            true,
            analysis,
            ImmutableMap.of(question, questionJson),
            ImmutableList.of(),
            false);
    try (Response response = getQuestionTargetAnalysis(network, question, analysis).get()) {
      assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
      assertThat(response.readEntity(String.class), equalTo(questionJson));
    }
  }

  @Test
  public void testListQuestionsAdHocMissingNetwork() {
    String network = "network1";
    try (Response response = getTargetAdHoc(network).get()) {
      assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
    }
  }

  @Test
  public void testListQuestionsAdHocSuccess() throws IOException {
    String network = "network1";
    String question = "question1";
    Main.getWorkMgr().initNetwork(network, null);
    Main.getWorkMgr()
        .uploadQuestion(network, question, BatfishObjectMapper.writeString(new TestQuestion()));
    try (Response response = getTargetAdHoc(network).get()) {
      assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
      assertThat(response.readEntity(SortedSet.class), equalTo(ImmutableSortedSet.of(question)));
    }
  }

  @Test
  public void testListQuestionsAnalysisMissingAnalysis() {
    String network = "network1";
    String analysis = "analysis1";
    Main.getWorkMgr().initNetwork(network, null);
    try (Response response = getTargetAnalysis(network, analysis).get()) {
      assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
    }
  }

  @Test
  public void testListQuestionsAnalysisMissingNetwork() {
    String network = "network1";
    String analysis = "analysis1";
    try (Response response = getTargetAnalysis(network, analysis).get()) {
      assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
    }
  }

  @Test
  public void testListQuestionsAnalysisSuccess() throws IOException {
    String network = "network1";
    String analysis = "analysis1";
    String question = "question1";
    Main.getWorkMgr().initNetwork(network, null);
    Main.getWorkMgr()
        .configureAnalysis(
            network,
            true,
            analysis,
            ImmutableMap.of(question, BatfishObjectMapper.writeString(new TestQuestion())),
            ImmutableList.of(),
            false);
    try (Response response = getTargetAnalysis(network, analysis).get()) {
      assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
      assertThat(response.readEntity(SortedSet.class), equalTo(ImmutableSortedSet.of(question)));
    }
  }

  @Test
  public void testPutQuestionAdHocMissingNetwork() throws JsonProcessingException {
    String network = "network1";
    String question = "question1";
    String questionJson = BatfishObjectMapper.writeString(new TestQuestion());
    try (Response response =
        getQuestionTargetAdHoc(network, question)
            .put(Entity.entity(questionJson, MediaType.APPLICATION_JSON))) {
      assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
    }
  }

  @Test
  public void testPutQuestionAdHocSuccess() throws IOException {
    String network = "network1";
    String question = "question1";
    String questionJson = BatfishObjectMapper.writeString(new TestQuestion());
    Main.getWorkMgr().initNetwork(network, null);
    Main.getWorkMgr().uploadQuestion(network, question, questionJson);
    try (Response response =
        getQuestionTargetAdHoc(network, question)
            .put(Entity.entity(questionJson, MediaType.APPLICATION_JSON))) {
      assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
    }
  }

  @Test
  public void testPutQuestionAnalysisMissingAnalysis() throws JsonProcessingException {
    String network = "network1";
    String question = "question1";
    String analysis = "analysis1";
    String questionJson = BatfishObjectMapper.writeString(new TestQuestion());
    try (Response response =
        getQuestionTargetAnalysis(network, question, analysis)
            .put(Entity.entity(questionJson, MediaType.APPLICATION_JSON))) {
      assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
    }
  }

  @Test
  public void testPutQuestionAnalysisMissingNetwork() throws JsonProcessingException {
    String network = "network1";
    String question = "question1";
    String questionJson = BatfishObjectMapper.writeString(new TestQuestion());
    try (Response response =
        getQuestionTargetAdHoc(network, question)
            .put(Entity.entity(questionJson, MediaType.APPLICATION_JSON))) {
      assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
    }
  }

  @Test
  public void testPutQuestionAnalysisSuccess() throws IOException {
    String network = "network1";
    String question = "question1";
    String analysis = "analysis1";
    String questionJson = BatfishObjectMapper.writeString(new TestQuestion());
    Main.getWorkMgr().initNetwork(network, null);
    Main.getWorkMgr()
        .configureAnalysis(
            network,
            true,
            analysis,
            ImmutableMap.of(
                "someOtherQuestion", BatfishObjectMapper.writeString(new TestQuestion())),
            ImmutableList.of(),
            false);
    try (Response response =
        getQuestionTargetAnalysis(network, question, analysis)
            .put(Entity.entity(questionJson, MediaType.APPLICATION_JSON))) {
      assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
    }
  }
}
