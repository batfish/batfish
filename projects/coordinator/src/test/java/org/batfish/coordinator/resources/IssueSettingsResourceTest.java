package org.batfish.coordinator.resources;

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

import com.google.common.collect.ImmutableList;
import java.io.IOException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.batfish.common.Version;
import org.batfish.coordinator.Main;
import org.batfish.coordinator.WorkMgrServiceV2TestBase;
import org.batfish.coordinator.WorkMgrTestUtils;
import org.batfish.datamodel.answers.MajorIssueConfig;
import org.batfish.datamodel.answers.MinorIssueConfig;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class IssueSettingsResourceTest extends WorkMgrServiceV2TestBase {

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Before
  public void initNetworkEnvironment() throws Exception {
    WorkMgrTestUtils.initWorkManager(_folder);
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
  public void addMinorIssueConfig() throws IOException {
    String network = "myNetwork";
    String major = "major";
    Main.getWorkMgr().initNetwork(network, null);

    // add a minor issue
    MinorIssueConfig minor1Config = new MinorIssueConfig("minor1", 100, "www.cnn.com");
    Response response =
        getIssueSettingsTarget(network)
            .post(
                Entity.entity(
                    new IssueConfigBean(major, minor1Config), MediaType.APPLICATION_JSON));

    // test: minorIssue should have been added
    assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
    MajorIssueConfig config = Main.getWorkMgr().getMajorIssueConfig(network, major);
    assertThat(config, equalTo(new MajorIssueConfig(major, ImmutableList.of(minor1Config))));

    // add two more minor issues, one for the same one as before
    MinorIssueConfig minor1ConfigAgain = new MinorIssueConfig("minor1", 90, "www");
    MinorIssueConfig minor2Config = new MinorIssueConfig("minor2", 9, "www");
    getIssueSettingsTarget(network)
        .post(
            Entity.entity(
                new IssueConfigBean(major, minor1ConfigAgain), MediaType.APPLICATION_JSON));
    getIssueSettingsTarget(network)
        .post(Entity.entity(new IssueConfigBean(major, minor2Config), MediaType.APPLICATION_JSON));

    // test: check the state now
    MajorIssueConfig configAgain = Main.getWorkMgr().getMajorIssueConfig(network, major);
    assertThat(
        configAgain,
        equalTo(new MajorIssueConfig(major, ImmutableList.of(minor1ConfigAgain, minor2Config))));
  }
}
