package org.batfish.coordinator.resources;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.Response;
import org.batfish.common.CoordConsts;
import org.batfish.common.CoordConstsV2;
import org.batfish.coordinator.Main;
import org.batfish.coordinator.WorkMgrServiceV2TestBase;
import org.batfish.coordinator.WorkMgrTestUtils;
import org.batfish.version.BatfishVersion;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class AnalysisResourceTest extends WorkMgrServiceV2TestBase {

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private Builder getTarget(String network, String analysis) {
    return target(CoordConsts.SVC_CFG_WORK_MGR2)
        .path(CoordConstsV2.RSC_NETWORKS)
        .path(network)
        .path(CoordConstsV2.RSC_ANALYSES)
        .path(analysis)
        .request()
        .header(CoordConstsV2.HTTP_HEADER_BATFISH_APIKEY, CoordConsts.DEFAULT_API_KEY)
        .header(CoordConstsV2.HTTP_HEADER_BATFISH_VERSION, BatfishVersion.getVersionStatic());
  }

  @Before
  public void initTestEnvironment() {
    WorkMgrTestUtils.initWorkManager(_folder);
  }

  @Test
  public void testDeleteAnalysisMissingNetwork() {
    String network = "network1";
    String analysis = "analysis1";
    try (Response response = getTarget(network, analysis).delete()) {
      assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
    }
  }

  @Test
  public void testDeleteAnalysisMissingAnalysis() {
    String network = "network1";
    String analysis = "analysis1";
    Main.getWorkMgr().initNetwork(network, null);
    try (Response response = getTarget(network, analysis).delete()) {
      assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
    }
  }

  @Test
  public void testDeleteAnalysisSuccess() throws IOException {
    String network = "network1";
    String analysis = "analysis1";
    Main.getWorkMgr().initNetwork(network, null);
    Main.getWorkMgr()
        .configureAnalysis(
            network, true, analysis, ImmutableMap.of("foo", "{}"), ImmutableList.of(), false);
    // should succeed first time
    try (Response response = getTarget(network, analysis).delete()) {
      assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
    }

    // should fail second time
    try (Response response = getTarget(network, analysis).delete()) {
      assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
    }
  }
}
