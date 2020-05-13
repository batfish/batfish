package org.batfish.coordinator.resources;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.Response;
import org.batfish.common.CoordConsts;
import org.batfish.common.CoordConstsV2;
import org.batfish.common.WorkItem;
import org.batfish.coordinator.Main;
import org.batfish.coordinator.WorkMgrServiceV2TestBase;
import org.batfish.coordinator.WorkMgrTestUtils;
import org.batfish.datamodel.pojo.WorkStatus;
import org.batfish.version.BatfishVersion;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class WorkResourceTest extends WorkMgrServiceV2TestBase {

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private Builder getWorkItemTarget(String network, String workId) {
    return target(CoordConsts.SVC_CFG_WORK_MGR2)
        .path(CoordConstsV2.RSC_NETWORKS)
        .path(network)
        .path(CoordConstsV2.RSC_WORK)
        .path(workId)
        .request()
        .header(CoordConstsV2.HTTP_HEADER_BATFISH_APIKEY, CoordConsts.DEFAULT_API_KEY)
        .header(CoordConstsV2.HTTP_HEADER_BATFISH_VERSION, BatfishVersion.getVersionStatic());
  }

  @Before
  public void initTestEnvironment() {
    WorkMgrTestUtils.initWorkManager(_folder);
  }

  @Test
  public void testGetWorkStatusMissingNetwork() {
    String network = "network1";
    UUID workId = UUID.randomUUID();
    try (Response response = getWorkItemTarget(network, workId.toString()).get()) {
      assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
    }
  }

  @Test
  public void testGetWorkStatusMissingWorkItem() {
    String network = "network1";
    UUID workId = UUID.randomUUID();
    Main.getWorkMgr().initNetwork(network, null);
    try (Response response = getWorkItemTarget(network, workId.toString()).get()) {
      assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
    }
  }

  @Test
  public void testGetWorkStatusInvalidUuid() {
    String network = "network1";
    Main.getWorkMgr().initNetwork(network, null);
    try (Response response = getWorkItemTarget(network, "@@@").get()) {
      assertThat(response.getStatus(), equalTo(BAD_REQUEST.getStatusCode()));
    }
  }

  @Test
  public void testGetWorkStatusPresent() throws IOException {
    String network = "network1";
    String snapshot = "snapshot1";
    UUID workId = UUID.randomUUID();
    Main.getWorkMgr().initNetwork(network, null);
    WorkMgrTestUtils.initSnapshotWithTopology(network, snapshot, ImmutableSet.of());
    WorkItem workItem = new WorkItem(workId, network, snapshot, new HashMap<>());
    Main.getWorkMgr().queueWork(workItem);
    try (Response response = getWorkItemTarget(network, workId.toString()).get()) {
      // work item should exist
      assertThat(response.getStatus(), equalTo(OK.getStatusCode()));

      WorkStatus workStatus = response.readEntity(WorkStatus.class);

      // work ID should match
      assertThat(workStatus.getWorkItem().getId(), equalTo(workId));
    }
  }
}
