package org.batfish.coordinator.resources;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.OK;
import static org.batfish.common.CoordConsts.DEFAULT_API_KEY;
import static org.batfish.common.CoordConsts.SVC_CFG_WORK_MGR2;
import static org.batfish.common.CoordConstsV2.HTTP_HEADER_BATFISH_APIKEY;
import static org.batfish.common.CoordConstsV2.HTTP_HEADER_BATFISH_VERSION;
import static org.batfish.common.CoordConstsV2.RSC_CONTAINERS;
import static org.batfish.common.CoordConstsV2.RSC_ISSUES;
import static org.batfish.common.CoordConstsV2.RSC_SETTINGS;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.batfish.common.Version;
import org.batfish.coordinator.Main;
import org.batfish.coordinator.WorkMgrServiceV2TestBase;
import org.batfish.coordinator.WorkMgrTestUtils;
import org.batfish.datamodel.answers.MinorIssueConfig;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class IssueConfigResourceTest extends WorkMgrServiceV2TestBase {

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Before
  public void initContainerEnvironment() throws Exception {
    WorkMgrTestUtils.initWorkManager(_folder);
  }

  private void addIssue(String network, String major, String minor, int severity, String url) {
    Response response =
        getIssueSettingsTarget(network)
            .post(
                Entity.entity(
                    new IssueConfigBean(major, new MinorIssueConfig(minor, severity, url)),
                    MediaType.APPLICATION_JSON));
    assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
  }

  private Builder getIssueConfigTarget(String network, String major, String minor) {
    return target(SVC_CFG_WORK_MGR2)
        .path(RSC_CONTAINERS)
        .path(network)
        .path(RSC_SETTINGS)
        .path(RSC_ISSUES)
        .path(major)
        .path(minor)
        .request()
        .header(HTTP_HEADER_BATFISH_APIKEY, DEFAULT_API_KEY)
        .header(HTTP_HEADER_BATFISH_VERSION, Version.getVersion());
  }

  private Builder getIssueSettingsTarget(String network) {
    return target(SVC_CFG_WORK_MGR2)
        .path(RSC_CONTAINERS)
        .path(network)
        .path(RSC_SETTINGS)
        .path(RSC_ISSUES)
        .request()
        .header(HTTP_HEADER_BATFISH_APIKEY, DEFAULT_API_KEY)
        .header(HTTP_HEADER_BATFISH_VERSION, Version.getVersion());
  }

  @Test
  public void testDelIssueConfig() throws IOException {
    String network = "myNetwork";
    String major = "major";
    String minor = "minor";
    Main.getWorkMgr().initNetwork(network, null);

    Response response = getIssueConfigTarget(network, major, minor).delete();
    assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));

    addIssue(network, major, minor, 100, "www.cnn");

    response = getIssueConfigTarget(network, major, minor).delete();
    assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
  }

  @Test
  public void testGetIssueConfig() throws IOException {
    String network = "myNetwork";
    String major = "major";
    String minor = "minor";
    Main.getWorkMgr().initNetwork(network, null);

    Response response = getIssueConfigTarget(network, major, minor).get();
    assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));

    addIssue(network, major, minor, 100, "www.cnn");

    response = getIssueConfigTarget(network, major, minor).get();
    assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
    assertThat(
        response.readEntity(IssueConfigBean.class),
        equalTo(new IssueConfigBean(major, new MinorIssueConfig(minor, 100, "www.cnn"))));
  }
}
