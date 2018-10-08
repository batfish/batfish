package org.batfish.coordinator.resources;

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
import java.io.IOException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.batfish.common.Version;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.coordinator.Main;
import org.batfish.coordinator.WorkMgrServiceV2TestBase;
import org.batfish.coordinator.WorkMgrTestUtils;
import org.batfish.identifiers.NetworkId;
import org.batfish.identifiers.QuestionSettingsId;
import org.batfish.storage.TestStorageProvider;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public final class QuestionSettingsResourceTest extends WorkMgrServiceV2TestBase {

  private static final class LocalStorageProvider extends TestStorageProvider {

    private String _questionSettings;

    @Override
    public boolean checkNetworkExists(NetworkId network) {
      return true;
    }

    @Override
    public String loadQuestionSettings(NetworkId network, QuestionSettingsId questionSettingsId)
        throws IOException {
      if (questionSettingsId.equals(BAD_QUESTION_SETTINGS_ID)) {
        throw new IOException("simulated exception");
      }
      return _questionSettings;
    }

    @Override
    public void storeQuestionSettings(
        String settings, NetworkId network, QuestionSettingsId questionSettingsId)
        throws IOException {
      if (questionSettingsId.equals(BAD_QUESTION_SETTINGS_ID)) {
        throw new IOException("simulated exception");
      }
      _questionSettings = settings;
    }
  }

  private static final String BAD_QUESTION = "badquestion";

  private static final QuestionSettingsId BAD_QUESTION_SETTINGS_ID = new QuestionSettingsId("bad");

  private static final String NETWORK = "network1";

  private static final String QUESTION = "qclass1";

  private LocalIdManager _idManager;

  private LocalStorageProvider _storage;

  @Rule public ExpectedException _thrown = ExpectedException.none();

  private Builder getQuestionSettingsTarget(String questionClass) {
    return target(SVC_CFG_WORK_MGR2)
        .path(RSC_CONTAINERS)
        .path(NETWORK)
        .path(RSC_SETTINGS)
        .path(RSC_QUESTIONS)
        .path(questionClass)
        .request()
        .header(HTTP_HEADER_BATFISH_APIKEY, DEFAULT_API_KEY)
        .header(HTTP_HEADER_BATFISH_VERSION, Version.getVersion());
  }

  @Before
  public void initContainerEnvironment() throws Exception {
    _idManager =
        new LocalIdManager() {
          @Override
          public QuestionSettingsId getQuestionSettingsId(
              String questionClassId, NetworkId networkId) {
            if (questionClassId.equals(BAD_QUESTION)) {
              return BAD_QUESTION_SETTINGS_ID;
            }
            return super.getQuestionSettingsId(questionClassId, networkId);
          }

          @Override
          public boolean hasQuestionSettingsId(String questionClassId, NetworkId networkId) {
            if (questionClassId.equals(BAD_QUESTION)) {
              throw new RuntimeException();
            }
            return super.hasQuestionSettingsId(questionClassId, networkId);
          }
        };

    _storage = new LocalStorageProvider();
    WorkMgrTestUtils.initWorkManager(_idManager, _storage);
    Main.getWorkMgr().initNetwork(NETWORK, null);
  }

  @Test
  public void testGetQuestionSettingsAbsent() {
    Response response = getQuestionSettingsTarget(QUESTION).get();

    assertThat(response.getStatus(), equalTo(Status.NOT_FOUND.getStatusCode()));
  }

  @Test
  public void testGetQuestionSettingsError() {
    Response response = getQuestionSettingsTarget(BAD_QUESTION).get();

    assertThat(response.getStatus(), equalTo(Status.INTERNAL_SERVER_ERROR.getStatusCode()));
  }

  @Test
  public void testGetQuestionSettingsPresent() {
    String settings = "{}";
    _storage._questionSettings = settings;
    QuestionSettingsId questionSettingsId = _idManager.generateQuestionSettingsId();
    _idManager.assignQuestionSettingsId(
        QUESTION, _idManager.getNetworkId(NETWORK), questionSettingsId);
    Response response = getQuestionSettingsTarget(QUESTION).get();

    assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
    assertThat(response.readEntity(String.class), equalTo(settings));
  }

  @Test
  public void testPutQuestionSettingsAbsentSuccess() throws IOException {
    String settings = "{}";
    JsonNode settingsNode = BatfishObjectMapper.mapper().readTree(settings);
    Response response =
        getQuestionSettingsTarget(QUESTION)
            .put(Entity.entity(settingsNode, MediaType.APPLICATION_JSON));

    assertThat(response.getStatus(), equalTo(Status.OK.getStatusCode()));
    assertThat(
        BatfishObjectMapper.mapper().readTree(_storage._questionSettings), equalTo(settingsNode));
  }

  @Test
  public void testPutQuestionSettingsError() throws IOException {
    String settings = "{}";
    JsonNode settingsNode = BatfishObjectMapper.mapper().readTree(settings);
    Response response =
        getQuestionSettingsTarget(BAD_QUESTION)
            .put(Entity.entity(settingsNode, MediaType.APPLICATION_JSON));

    assertThat(response.getStatus(), equalTo(Status.INTERNAL_SERVER_ERROR.getStatusCode()));
  }

  @Test
  public void testPutQuestionSettingsPresentSuccess() throws IOException {
    String settings = "{}";
    _storage._questionSettings = "{\"a\":\"b\"}";
    JsonNode settingsNode = BatfishObjectMapper.mapper().readTree(settings);
    Response response =
        getQuestionSettingsTarget(QUESTION)
            .put(Entity.entity(settingsNode, MediaType.APPLICATION_JSON));

    assertThat(response.getStatus(), equalTo(Status.OK.getStatusCode()));
    assertThat(
        BatfishObjectMapper.mapper().readTree(_storage._questionSettings), equalTo(settingsNode));
  }
}
