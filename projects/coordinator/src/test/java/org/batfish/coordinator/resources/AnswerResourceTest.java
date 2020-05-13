package org.batfish.coordinator.resources;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.OK;
import static org.batfish.coordinator.WorkMgrTestUtils.setupQuestionAndAnswer;
import static org.batfish.coordinator.WorkMgrTestUtils.uploadTestSnapshot;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.iterableWithSize;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import org.batfish.common.AnswerRowsOptions;
import org.batfish.common.CoordConsts;
import org.batfish.common.CoordConstsV2;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.coordinator.Main;
import org.batfish.coordinator.WorkMgrServiceV2TestBase;
import org.batfish.coordinator.WorkMgrTestUtils;
import org.batfish.datamodel.answers.Answer;
import org.batfish.datamodel.answers.AnswerSummary;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.answers.StringAnswerElement;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableMetadata;
import org.batfish.datamodel.table.TableView;
import org.batfish.datamodel.table.TableViewRow;
import org.batfish.version.BatfishVersion;
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

  private Builder filterAnswerTarget(String network, String question, @Nullable String snapshot) {
    return addHeader(
        addSnapshotQuery(
            target(CoordConsts.SVC_CFG_WORK_MGR2)
                .path(CoordConstsV2.RSC_NETWORKS)
                .path(network)
                .path(CoordConstsV2.RSC_QUESTIONS)
                .path(question)
                .path(CoordConstsV2.RSC_ANSWER)
                .path(CoordConstsV2.RSC_FILTER),
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
        .header(CoordConstsV2.HTTP_HEADER_BATFISH_VERSION, BatfishVersion.getVersionStatic());
  }

  @Before
  public void initTestEnvironment() {
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

    try (Response response = getAnswerTarget(network, question, snapshot).get()) {
      // Confirm the existing answer is successfully fetched
      assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
      assertThat(
          BatfishObjectMapper.writeString(response.readEntity(Answer.class)),
          equalTo(expectedAnswerString));
    }
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

    try (Response response = getAnalysisAnswerTarget(network, question, analysis, snapshot).get()) {
      // Confirm the existing analysis answer is successfully fetched
      assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
      assertThat(
          BatfishObjectMapper.writeString(response.readEntity(Answer.class)),
          equalTo(expectedAnswerString));
    }
  }

  @Test
  public void testGetAnswerBadQuery() throws IOException {
    String network = "network";
    String snapshot = "snapshot";
    String question = "question";

    Main.getWorkMgr().initNetwork(network, null);
    uploadTestSnapshot(network, snapshot, _folder);
    setupQuestionAndAnswer(network, snapshot, question, null, null);

    try (Response responseNoSnapshot = getAnswerTarget(network, question, null).get()) {
      // Missing snapshot name should result in bad request
      assertThat(responseNoSnapshot.getStatus(), equalTo(BAD_REQUEST.getStatusCode()));
      assertThat(
          responseNoSnapshot.readEntity(String.class),
          containsString("Snapshot must be specified"));
    }
  }

  @Test
  public void testGetAnswerNotFound() throws IOException {
    String network = "network";
    String snapshot = "snapshot";
    String bogusSnapshot = "bogusSnapshot";
    String question = "question";
    // No network should result in 404
    try (Response responseNoNetwork = getAnswerTarget(network, question, null).get()) {
      assertThat(responseNoNetwork.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
      assertThat(
          responseNoNetwork.readEntity(String.class),
          containsString(String.format("Network '%s' does not exist", network)));
    }

    Main.getWorkMgr().initNetwork(network, null);
    try (Response responseNoQuestion = getAnswerTarget(network, question, null).get()) {
      // No question should result in 404
      assertThat(responseNoQuestion.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
      assertThat(
          responseNoQuestion.readEntity(String.class),
          containsString(String.format("Question '%s' does not exist", question)));
    }

    uploadTestSnapshot(network, snapshot, _folder);
    setupQuestionAndAnswer(network, snapshot, question, null, null);
    try (Response responseBadSnap = getAnswerTarget(network, question, bogusSnapshot).get()) {
      // Invalid snapshot name should result in 404
      assertThat(responseBadSnap.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
      assertThat(
          responseBadSnap.readEntity(String.class),
          containsString(String.format("Snapshot %s not found", bogusSnapshot)));
    }

    // No answer should result in 404
    try (Response responseNoAns = getAnswerTarget(network, question, snapshot).get()) {
      assertThat(responseNoAns.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
      assertThat(responseNoAns.readEntity(String.class), containsString("Answer not found"));
    }
  }

  @Test
  public void testFilterAnswer() throws IOException {
    String network = "network";
    String snapshot = "snapshot";
    String question = "question";

    AnswerRowsOptions filterOptions =
        new AnswerRowsOptions(
            ImmutableSet.of(), ImmutableList.of(), 1, 0, ImmutableList.of(), false);
    FilterAnswerBean filterAnswer = new FilterAnswerBean(snapshot, null, filterOptions);

    // Build the baseAnswer we will be filtering
    TableMetadata baseTableMetadata =
        new TableMetadata(
            ImmutableList.of(new ColumnMetadata("colName", Schema.STRING, "col description")));
    TableAnswerElement baseTableAnswerElement = new TableAnswerElement(baseTableMetadata);
    baseTableAnswerElement.addRow(Row.of("colName", "value1"));
    baseTableAnswerElement.addRow(Row.of("colName", "value2"));
    Answer baseAnswer = new Answer();
    baseAnswer.addAnswerElement(baseTableAnswerElement);
    // Setup infrastructure + answer
    Main.getWorkMgr().initNetwork(network, null);
    uploadTestSnapshot(network, snapshot, _folder);
    setupQuestionAndAnswer(network, snapshot, question, null, baseAnswer);

    // expectedAnswer is same as baseAnswer but only contains the first row
    TableViewRow expectedRow = new TableViewRow(0, Row.of("colName", "value1"));
    TableView expectedTableView =
        new TableView(filterOptions, ImmutableList.of(expectedRow), baseTableMetadata);
    // Original answer had two results, so the view needs to have two results
    expectedTableView.setSummary(new AnswerSummary("", 0, 0, 2));
    String expectedTableViewString = BatfishObjectMapper.writeString(expectedTableView);

    try (Response response =
        filterAnswerTarget(network, question, snapshot).post(Entity.json(filterAnswer))) {
      // Confirm the filtered answer is successfully fetched
      assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
      Answer actual = response.readEntity(Answer.class);
      // Confirm the filtered answer contains one answer element, matching the expected table view
      assertThat(actual.getAnswerElements(), iterableWithSize(1));
      assertThat(
          BatfishObjectMapper.writeString(actual.getAnswerElements().get(0)),
          equalTo(expectedTableViewString));
    }
  }

  @Test
  public void testFilterAnswerNoFilter() throws IOException {
    String network = "network";
    String snapshot = "snapshot";
    String question = "question";

    AnswerRowsOptions filterOptions = AnswerRowsOptions.NO_FILTER;
    FilterAnswerBean filterAnswer = FilterAnswerBean.create(snapshot, null, null);

    // Build the baseAnswer
    TableMetadata baseTableMetadata =
        new TableMetadata(
            ImmutableList.of(new ColumnMetadata("colName", Schema.STRING, "col description")));
    TableAnswerElement baseTableAnswerElement = new TableAnswerElement(baseTableMetadata);
    Row row1 = Row.of("colName", "value1");
    Row row2 = Row.of("colName", "value2");
    baseTableAnswerElement.addRow(row1);
    baseTableAnswerElement.addRow(row2);
    Answer baseAnswer = new Answer();
    baseAnswer.addAnswerElement(baseTableAnswerElement);
    // Setup infrastructure + answer
    Main.getWorkMgr().initNetwork(network, null);
    uploadTestSnapshot(network, snapshot, _folder);
    setupQuestionAndAnswer(network, snapshot, question, null, baseAnswer);

    // expectedAnswer just contains a TableView with the same rows as baseAnswer
    TableView expectedTableView =
        new TableView(
            filterOptions,
            ImmutableList.of(new TableViewRow(0, row1), new TableViewRow(1, row2)),
            baseTableMetadata);
    // Original answer had two results, so the view needs to have two results
    expectedTableView.setSummary(new AnswerSummary("", 0, 0, 2));
    String expectedAnswerString = BatfishObjectMapper.writeString(expectedTableView);

    try (Response response =
        filterAnswerTarget(network, question, snapshot).post(Entity.json(filterAnswer))) {
      // Confirm the (un)filtered answer is successfully fetched
      assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
      Answer actual = response.readEntity(Answer.class);
      // Confirm the (un)filtered answer contains one answer element, matching the expected table
      // view
      assertThat(actual.getAnswerElements(), iterableWithSize(1));
      assertThat(
          BatfishObjectMapper.writeString(actual.getAnswerElements().get(0)),
          equalTo(expectedAnswerString));
    }
  }

  @Test
  public void testFilterAnswerBadRequest() throws IOException {
    String network = "network";
    String snapshot = "snapshot";
    String question = "question";

    Main.getWorkMgr().initNetwork(network, null);
    uploadTestSnapshot(network, snapshot, _folder);
    setupQuestionAndAnswer(network, snapshot, question, null, new Answer());

    // Post arbitrary item that is not an AnswerRowsOptions object
    try (Response responseBadFilter =
        filterAnswerTarget(network, question, null).post(Entity.json(true))) {

      // Bogus filterAnswer should result in bad request
      assertThat(responseBadFilter.getStatus(), equalTo(BAD_REQUEST.getStatusCode()));
      assertThat(
          responseBadFilter.readEntity(String.class), containsString("Cannot construct instance"));
    }
  }

  @Test
  public void testFilterAnswerMissing() throws IOException {
    String network = "network";
    String snapshot = "snapshot";
    String bogusSnapshot = "bogusSnapshot";
    String question = "question";
    String bogusQuestion = "bogusQuestion";

    AnswerRowsOptions filterOptions =
        new AnswerRowsOptions(
            ImmutableSet.of(), ImmutableList.of(), 1, 0, ImmutableList.of(), false);
    FilterAnswerBean filterAnswer = new FilterAnswerBean(snapshot, null, filterOptions);
    FilterAnswerBean filterBadSnapshot = new FilterAnswerBean(bogusSnapshot, null, filterOptions);

    Main.getWorkMgr().initNetwork(network, null);
    uploadTestSnapshot(network, snapshot, _folder);
    setupQuestionAndAnswer(network, snapshot, question, null, null);

    try (Response responseBadSnapshot =
        filterAnswerTarget(network, question, null).post(Entity.json(filterBadSnapshot))) {
      // Bogus snapshot should result in not found
      assertThat(responseBadSnapshot.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
      assertThat(
          responseBadSnapshot.readEntity(String.class),
          containsString(String.format("Snapshot %s not found", bogusSnapshot)));
    }

    try (Response responseMissingQuestion =
        filterAnswerTarget(network, bogusQuestion, null).post(Entity.json(filterAnswer))) {
      // Bogus question should result in not found
      assertThat(responseMissingQuestion.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
      assertThat(
          responseMissingQuestion.readEntity(String.class),
          containsString(String.format("Question '%s' does not exist", bogusQuestion)));
    }

    try (Response responseMissingAnswer =
        filterAnswerTarget(network, question, null).post(Entity.json(filterAnswer))) {
      // Filtering non existent question should result in not found
      assertThat(responseMissingAnswer.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
      assertThat(
          responseMissingAnswer.readEntity(String.class),
          containsString(String.format("Answer not found for question %s", question)));
    }
  }
}
