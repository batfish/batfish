package org.batfish.coordinator.resources;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.OK;
import static org.batfish.common.CoordConsts.DEFAULT_API_KEY;
import static org.batfish.common.CoordConsts.SVC_CFG_WORK_MGR2;
import static org.batfish.common.CoordConstsV2.HTTP_HEADER_BATFISH_APIKEY;
import static org.batfish.common.CoordConstsV2.HTTP_HEADER_BATFISH_VERSION;
import static org.batfish.common.CoordConstsV2.RSC_CONTAINERS;
import static org.batfish.common.CoordConstsV2.RSC_QUESTIONS;
import static org.batfish.common.CoordConstsV2.RSC_SETTINGS;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.batfish.common.Version;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.coordinator.WorkMgrServiceV2TestBase;
import org.batfish.coordinator.WorkMgrTestUtils;
import org.batfish.storage.TestStorageProvider;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public final class QuestionSettingsJsonPathResourceTest extends WorkMgrServiceV2TestBase {

  private static final class LocalStorageProvider extends TestStorageProvider {

    private String _questionSettings;

    @Override
    public boolean checkNetworkExists(String network) {
      return true;
    }

    @Override
    public String loadQuestionSettings(String network, String className) throws IOException {
      if (className.equals(BAD_QUESTION)) {
        throw new IOException("simulated exception");
      }
      return _questionSettings;
    }

    @Override
    public void storeQuestionSettings(String settings, String network, String questionClass)
        throws IOException {
      if (questionClass.equals(BAD_QUESTION)) {
        throw new IOException("simulated exception");
      }
      _questionSettings = settings;
    }
  }

  private static final String BAD_QUESTION = "badquestion";

  private static final String NETWORK = "network1";

  private static final String PROP1 = "prop1";

  private static final String PROP2 = "prop2";

  private static final String QUESTION = "qclass1";

  private static final int VAL = 5;

  private LocalStorageProvider _storage;

  @Rule public ExpectedException _thrown = ExpectedException.none();

  private Builder getQuestionSettingsJsonPathTarget(String questionClass, String jsonPath) {
    return target(SVC_CFG_WORK_MGR2)
        .path(RSC_CONTAINERS)
        .path(NETWORK)
        .path(RSC_SETTINGS)
        .path(RSC_QUESTIONS)
        .path(questionClass)
        .path(jsonPath)
        .request()
        .header(HTTP_HEADER_BATFISH_APIKEY, DEFAULT_API_KEY)
        .header(HTTP_HEADER_BATFISH_VERSION, Version.getVersion());
  }

  @Before
  public void initContainerEnvironment() throws Exception {
    _storage = new LocalStorageProvider();
    WorkMgrTestUtils.initWorkManager(_storage);
  }

  @Test
  public void testGetQuestionSettingsError() {
    Response response = getQuestionSettingsJsonPathTarget(BAD_QUESTION, "foo").get();

    assertThat(response.getStatus(), equalTo(Status.INTERNAL_SERVER_ERROR.getStatusCode()));
  }

  @Test
  public void testGetQuestionSettingsJsonPathQuestionAbsent() {
    Response response = getQuestionSettingsJsonPathTarget(QUESTION, "foo").get();

    assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
  }

  @Test
  public void testGetQuestionSettingsJsonPathQuestionPresentDepth1Absent() {
    String settings = "{}";
    _storage._questionSettings = settings;
    Response response = getQuestionSettingsJsonPathTarget(QUESTION, PROP1).get();

    assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
  }

  @Test
  public void testGetQuestionSettingsJsonPathQuestionPresentDepth1Present() {
    String settings = String.format("{\"%s\":{\"%s\":%d}}", PROP1, PROP2, VAL);
    _storage._questionSettings = settings;
    Response response = getQuestionSettingsJsonPathTarget(QUESTION, PROP1).get();

    assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
    assertThat(
        response.readEntity(String.class), equalTo(String.format("{\"%s\":%d}", PROP2, VAL)));
  }

  @Test
  public void testGetQuestionSettingsJsonPathQuestionPresentDepth2Absent() {
    String settings = String.format("{\"%s\":{\"%s\":%d}}", PROP1, PROP2, VAL);
    _storage._questionSettings = settings;
    Response response =
        getQuestionSettingsJsonPathTarget(QUESTION, String.format("%s/%s", PROP1, "foo")).get();

    assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
  }

  @Test
  public void testGetQuestionSettingsJsonPathQuestionPresentDepth2Present() {
    String settings = String.format("{\"%s\":{\"%s\":%d}}", PROP1, PROP2, VAL);
    _storage._questionSettings = settings;
    Response response =
        getQuestionSettingsJsonPathTarget(QUESTION, String.format("%s/%s", PROP1, PROP2)).get();

    assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
    assertThat(response.readEntity(String.class), equalTo("5"));
  }

  @Test
  public void testPutQuestionSettingsAbsentDepth1Success() throws IOException {
    String settings = "{}";
    JsonNode settingsNode = BatfishObjectMapper.mapper().readTree(settings);
    JsonNodeFactory factory = BatfishObjectMapper.mapper().getNodeFactory();
    ObjectNode rootSettingsNode = new ObjectNode(factory);
    rootSettingsNode.set(PROP1, settingsNode);
    Response response =
        getQuestionSettingsJsonPathTarget(QUESTION, PROP1)
            .put(Entity.entity(settingsNode, MediaType.APPLICATION_JSON));

    assertThat(response.getStatus(), equalTo(Status.OK.getStatusCode()));
    assertThat(
        BatfishObjectMapper.mapper().readTree(_storage._questionSettings),
        equalTo(rootSettingsNode));
  }

  @Test
  public void testPutQuestionSettingsAbsentDepth2Success() throws IOException {
    String settings = "{}";
    JsonNode settingsNode = BatfishObjectMapper.mapper().readTree(settings);
    JsonNodeFactory factory = BatfishObjectMapper.mapper().getNodeFactory();
    ObjectNode rootSettingsNode = new ObjectNode(factory);
    ObjectNode leafNode = new ObjectNode(factory);
    leafNode.set(PROP2, settingsNode);
    rootSettingsNode.set(PROP1, leafNode);
    Response response =
        getQuestionSettingsJsonPathTarget(QUESTION, String.format("%s/%s", PROP1, PROP2))
            .put(Entity.entity(settingsNode, MediaType.APPLICATION_JSON));

    assertThat(response.getStatus(), equalTo(Status.OK.getStatusCode()));
    assertThat(
        BatfishObjectMapper.mapper().readTree(_storage._questionSettings),
        equalTo(rootSettingsNode));
  }

  @Test
  public void testPutQuestionSettingsError() throws IOException {
    String settings = "{}";
    JsonNode settingsNode = BatfishObjectMapper.mapper().readTree(settings);
    JsonNodeFactory factory = BatfishObjectMapper.mapper().getNodeFactory();
    ObjectNode rootSettingsNode = new ObjectNode(factory);
    rootSettingsNode.set(PROP1, settingsNode);
    Response response =
        getQuestionSettingsJsonPathTarget(BAD_QUESTION, PROP1)
            .put(Entity.entity(settingsNode, MediaType.APPLICATION_JSON));

    assertThat(response.getStatus(), equalTo(Status.INTERNAL_SERVER_ERROR.getStatusCode()));
  }

  @Test
  public void testPutQuestionSettingsPresentDepth2Success() throws IOException {
    String settings = "{}";
    String oldKey = "foo";
    int oldVal = 1;
    _storage._questionSettings = String.format("{\"%s\":%d}", oldKey, oldVal);
    JsonNode settingsNode = BatfishObjectMapper.mapper().readTree(settings);
    JsonNodeFactory factory = BatfishObjectMapper.mapper().getNodeFactory();
    ObjectNode rootSettingsNode = new ObjectNode(factory);
    ObjectNode leafNode = new ObjectNode(factory);
    leafNode.set(PROP2, settingsNode);
    rootSettingsNode.set(PROP1, leafNode);
    rootSettingsNode.set(oldKey, new IntNode(oldVal));
    Response response =
        getQuestionSettingsJsonPathTarget(QUESTION, String.format("%s/%s", PROP1, PROP2))
            .put(Entity.entity(settingsNode, MediaType.APPLICATION_JSON));

    assertThat(response.getStatus(), equalTo(Status.OK.getStatusCode()));
    assertThat(
        BatfishObjectMapper.mapper().readTree(_storage._questionSettings),
        equalTo(rootSettingsNode));
  }
}
