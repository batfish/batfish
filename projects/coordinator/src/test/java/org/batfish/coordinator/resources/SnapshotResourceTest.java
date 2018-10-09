package org.batfish.coordinator.resources;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.Response;
import org.batfish.common.CoordConsts;
import org.batfish.common.CoordConstsV2;
import org.batfish.common.Version;
import org.batfish.coordinator.Main;
import org.batfish.coordinator.WorkMgrServiceV2TestBase;
import org.batfish.coordinator.WorkMgrTestUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class SnapshotResourceTest extends WorkMgrServiceV2TestBase {

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Before
  public void init() throws Exception {
    WorkMgrTestUtils.initWorkManager(_folder);
  }

  private Builder getSnapshotTarget(String network, String snapshot) {
    return target(CoordConsts.SVC_CFG_WORK_MGR2)
        .path(CoordConstsV2.RSC_CONTAINERS)
        .path(network)
        .path(CoordConstsV2.RSC_SNAPSHOTS)
        .path(snapshot)
        .request()
        .header(CoordConstsV2.HTTP_HEADER_BATFISH_APIKEY, CoordConsts.DEFAULT_API_KEY)
        .header(CoordConstsV2.HTTP_HEADER_BATFISH_VERSION, Version.getVersion());
  }

  @Test
  public void forkSnapshot() throws IOException {
    String networkName = "network";
    String snapshotName = "snapshot";
    String baseSnapshotName = "baseSnapshot";
    ForkSnapshotBean forkSnapshotBean =
        new ForkSnapshotBean(
            baseSnapshotName, ImmutableList.of(), ImmutableList.of(), ImmutableList.of("node1"));

    Main.getWorkMgr().initNetwork(networkName, null);
    WorkMgrTestUtils.initTestrigWithTopology(networkName, baseSnapshotName, ImmutableSet.of());
    Response response =
        getSnapshotTarget(networkName, snapshotName).put(Entity.json(forkSnapshotBean));

    // Confirm forking existing snapshot is successful
    assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
  }

  @Test
  public void forkSnapshotDuplicate() throws IOException {
    String networkName = "network";
    String snapshotName = "snapshot";
    String baseSnapshotName = "baseSnapshot";
    ForkSnapshotBean forkSnapshotBean =
        new ForkSnapshotBean(
            baseSnapshotName, ImmutableList.of(), ImmutableList.of(), ImmutableList.of("node1"));

    Main.getWorkMgr().initNetwork(networkName, null);
    WorkMgrTestUtils.initTestrigWithTopology(networkName, baseSnapshotName, ImmutableSet.of());
    WorkMgrTestUtils.initTestrigWithTopology(networkName, snapshotName, ImmutableSet.of());
    Response response =
        getSnapshotTarget(networkName, snapshotName).put(Entity.json(forkSnapshotBean));

    // Confirm duplicate snapshot name fails with bad request
    assertThat(response.getStatus(), equalTo(BAD_REQUEST.getStatusCode()));
  }

  @Test
  public void forkSnapshotMissingBaseSnapshot() throws IOException {
    String networkName = "network";
    String snapshotName = "snapshot";
    String baseSnapshotName = "baseSnapshot";
    ForkSnapshotBean forkSnapshotBean =
        new ForkSnapshotBean(
            baseSnapshotName, ImmutableList.of(), ImmutableList.of(), ImmutableList.of("node1"));

    Main.getWorkMgr().initNetwork(networkName, null);
    Response response =
        getSnapshotTarget(networkName, snapshotName).put(Entity.json(forkSnapshotBean));

    // Confirm missing snapshot causes not found
    assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
  }

  @Test
  public void forkSnapshotMissingNetwork() throws IOException {
    String networkName = "network";
    String snapshotName = "snapshot";
    String baseSnapshotName = "baseSnapshot";
    ForkSnapshotBean forkSnapshotBean =
        new ForkSnapshotBean(
            baseSnapshotName, ImmutableList.of(), ImmutableList.of(), ImmutableList.of("node1"));

    Response response =
        getSnapshotTarget(networkName, snapshotName).put(Entity.json(forkSnapshotBean));

    // Confirm missing network causes not found
    assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
  }
}
