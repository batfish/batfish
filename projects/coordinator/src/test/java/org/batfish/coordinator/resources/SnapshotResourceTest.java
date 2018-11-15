package org.batfish.coordinator.resources;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.OK;
import static org.batfish.coordinator.WorkMgrTestUtils.uploadTestSnapshot;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.batfish.common.CoordConsts;
import org.batfish.common.CoordConstsV2;
import org.batfish.common.Version;
import org.batfish.coordinator.Main;
import org.batfish.coordinator.WorkMgrServiceV2TestBase;
import org.batfish.coordinator.WorkMgrTestUtils;
import org.batfish.coordinator.id.IdManager;
import org.batfish.datamodel.SnapshotMetadata;
import org.batfish.datamodel.Topology;
import org.batfish.identifiers.NetworkId;
import org.batfish.identifiers.SnapshotId;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class SnapshotResourceTest extends WorkMgrServiceV2TestBase {

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private Builder getCompletedWorkTarget(String network, String snapshot) {
    return target(CoordConsts.SVC_CFG_WORK_MGR2)
        .path(CoordConstsV2.RSC_CONTAINERS)
        .path(network)
        .path(CoordConstsV2.RSC_SNAPSHOTS)
        .path(snapshot)
        .path(CoordConstsV2.RSC_COMPLETED_WORK)
        .request()
        .header(CoordConstsV2.HTTP_HEADER_BATFISH_APIKEY, CoordConsts.DEFAULT_API_KEY)
        .header(CoordConstsV2.HTTP_HEADER_BATFISH_VERSION, Version.getVersion());
  }

  private Builder getPojoTopologyTarget(String network, String snapshot) {
    return target(CoordConsts.SVC_CFG_WORK_MGR2)
        .path(CoordConstsV2.RSC_CONTAINERS)
        .path(network)
        .path(CoordConstsV2.RSC_SNAPSHOTS)
        .path(snapshot)
        .path(CoordConstsV2.RSC_POJO_TOPOLOGY)
        .request()
        .header(CoordConstsV2.HTTP_HEADER_BATFISH_APIKEY, CoordConsts.DEFAULT_API_KEY)
        .header(CoordConstsV2.HTTP_HEADER_BATFISH_VERSION, Version.getVersion());
  }

  private Builder getTarget(String network, String snapshot) {
    return target(CoordConsts.SVC_CFG_WORK_MGR2)
        .path(CoordConstsV2.RSC_CONTAINERS)
        .path(network)
        .path(CoordConstsV2.RSC_SNAPSHOTS)
        .path(snapshot)
        .request()
        .header(CoordConstsV2.HTTP_HEADER_BATFISH_APIKEY, CoordConsts.DEFAULT_API_KEY)
        .header(CoordConstsV2.HTTP_HEADER_BATFISH_VERSION, Version.getVersion());
  }

  private Builder getTopologyTarget(String network, String snapshot) {
    return target(CoordConsts.SVC_CFG_WORK_MGR2)
        .path(CoordConstsV2.RSC_CONTAINERS)
        .path(network)
        .path(CoordConstsV2.RSC_SNAPSHOTS)
        .path(snapshot)
        .path(CoordConstsV2.RSC_TOPOLOGY)
        .request()
        .header(CoordConstsV2.HTTP_HEADER_BATFISH_APIKEY, CoordConsts.DEFAULT_API_KEY)
        .header(CoordConstsV2.HTTP_HEADER_BATFISH_VERSION, Version.getVersion());
  }

  private Builder getWorkLogTarget(String network, String snapshot, String workId) {
    return target(CoordConsts.SVC_CFG_WORK_MGR2)
        .path(CoordConstsV2.RSC_CONTAINERS)
        .path(network)
        .path(CoordConstsV2.RSC_SNAPSHOTS)
        .path(snapshot)
        .path(CoordConstsV2.RSC_WORK_LOG)
        .path(workId)
        .request()
        .header(CoordConstsV2.HTTP_HEADER_BATFISH_APIKEY, CoordConsts.DEFAULT_API_KEY)
        .header(CoordConstsV2.HTTP_HEADER_BATFISH_VERSION, Version.getVersion());
  }

  @Before
  public void initContainerEnvironment() throws Exception {
    WorkMgrTestUtils.initWorkManager(_folder);
  }

  @Test
  public void testDeleteSnapshotMissingNetwork() {
    String network = "network1";
    String snapshot = "snapshot1";
    Response response = getTarget(network, snapshot).delete();

    assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
  }

  @Test
  public void testDeleteSnapshotMissingSnapshot() {
    String network = "network1";
    String snapshot = "snapshot1";
    Main.getWorkMgr().initNetwork(network, null);
    Response response = getTarget(network, snapshot).delete();

    assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
  }

  @Test
  public void testDeleteSnapshotSuccess() throws IOException {
    String network = "network1";
    String snapshot = "snapshot1";
    Main.getWorkMgr().initNetwork(network, null);
    uploadTestSnapshot(network, snapshot, _folder);
    Response response = getTarget(network, snapshot).delete();

    // should succeed first time
    assertThat(response.getStatus(), equalTo(OK.getStatusCode()));

    response = getTarget(network, snapshot).delete();

    // should fail second time
    assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
  }

  @Test
  public void testCompletedWorkMissingNetwork() {
    String network = "network1";
    String snapshot = "snapshot1";
    Response response = getCompletedWorkTarget(network, snapshot).get();

    assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
  }

  @Test
  public void testCompletedWorkMissingSnapshot() {
    String network = "network1";
    String snapshot = "snapshot1";
    Main.getWorkMgr().initNetwork(network, null);
    Response response = getCompletedWorkTarget(network, snapshot).get();

    assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
  }

  @Test
  public void testCompletedWork() throws IOException {
    String network = "network1";
    String snapshot = "snapshot1";
    Main.getWorkMgr().initNetwork(network, null);
    WorkMgrTestUtils.initSnapshotWithTopology(network, snapshot, ImmutableSet.of());

    Response response = getCompletedWorkTarget(network, snapshot).get();

    assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
  }

  @Test
  public void testGetPojoTopologyMissingNetwork() throws IOException {
    String network = "network1";
    String snapshot = "snapshot1";
    Response response = getPojoTopologyTarget(network, snapshot).get();

    assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
  }

  @Test
  public void testGetPojoTopologyMissingSnapshot() throws IOException {
    String network = "network1";
    String snapshot = "snapshot1";
    Main.getWorkMgr().initNetwork(network, null);
    Response response = getPojoTopologyTarget(network, snapshot).get();

    assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
  }

  @Test
  public void testGetPojoTopologyPresent() throws IOException {
    String network = "network1";
    String snapshot = "snapshot1";
    Main.getWorkMgr().initNetwork(network, null);
    WorkMgrTestUtils.initSnapshotWithTopology(network, snapshot, ImmutableSet.of());
    Response response = getPojoTopologyTarget(network, snapshot).get();

    assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
    assertThat(response.readEntity(org.batfish.datamodel.pojo.Topology.class), notNullValue());
  }

  @Test
  public void testGetSnapshotMetadataMissingNetwork() throws IOException {
    String network = "network1";
    String snapshot = "snapshot1";
    Response response = getTarget(network, snapshot).get();

    assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
  }

  @Test
  public void testGetSnapshotMetadataMissingSnapshot() throws IOException {
    String network = "network1";
    String snapshot = "snapshot1";
    Main.getWorkMgr().initNetwork(network, null);
    Response response = getTarget(network, snapshot).get();

    assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
  }

  @Test
  public void testGetSnapshotMetadataPresent() throws IOException {
    String network = "network1";
    String snapshot = "snapshot1";
    Main.getWorkMgr().initNetwork(network, null);
    WorkMgrTestUtils.initSnapshotWithTopology(network, snapshot, ImmutableSet.of());
    Response response = getTarget(network, snapshot).get();

    assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
    assertThat(response.readEntity(SnapshotMetadata.class), notNullValue());
  }

  @Test
  public void testGetTopologyMissingNetwork() throws IOException {
    String network = "network1";
    String snapshot = "snapshot1";
    Response response = getTopologyTarget(network, snapshot).get();

    assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
  }

  @Test
  public void testGetTopologyMissingSnapshot() throws IOException {
    String network = "network1";
    String snapshot = "snapshot1";
    Main.getWorkMgr().initNetwork(network, null);
    Response response = getTopologyTarget(network, snapshot).get();

    assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
  }

  @Test
  public void testGetTopologyPresent() throws IOException {
    String network = "network1";
    String snapshot = "snapshot1";
    Main.getWorkMgr().initNetwork(network, null);
    WorkMgrTestUtils.initSnapshotWithTopology(network, snapshot, ImmutableSet.of());
    Response response = getTopologyTarget(network, snapshot).get();

    assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
    assertThat(response.readEntity(Topology.class), notNullValue());
  }

  @Test
  public void testGetWorkLogMissingNetwork() {
    String network = "network1";
    String snapshot = "snapshot1";

    Response response = getWorkLogTarget(network, snapshot, "workId").get();

    assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
  }

  @Test
  public void testGetWorkLogMissingSnapshot() {
    String network = "network1";
    String snapshot = "snapshot1";
    Main.getWorkMgr().initNetwork(network, null);

    Response response = getWorkLogTarget(network, snapshot, "workid").get();

    assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
  }

  @Test
  public void testGetWorkLogMissingFile() throws IOException {
    String network = "network1";
    String snapshot = "snapshot1";
    Main.getWorkMgr().initNetwork(network, null);
    WorkMgrTestUtils.uploadTestSnapshot(network, snapshot, _folder);

    Response response = getWorkLogTarget(network, snapshot, "missingworkid").get();

    assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
  }

  @Test
  public void testGetWorkLogPresent() throws IOException {
    String network = "network1";
    String snapshot = "snapshot1";
    Main.getWorkMgr().initNetwork(network, null);
    WorkMgrTestUtils.uploadTestSnapshot(network, snapshot, _folder);
    IdManager idm = Main.getWorkMgr().getIdManager();
    NetworkId networkId = idm.getNetworkId(network);
    SnapshotId snapshotId = idm.getSnapshotId(snapshot, networkId);
    Main.getWorkMgr().getStorage().storeWorkLog("logoutput", networkId, snapshotId, "workid");

    Builder target = getWorkLogTarget(network, snapshot, "workid");
    Response response = target.get();

    assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
    assertThat(response.getMediaType(), equalTo(MediaType.TEXT_PLAIN_TYPE));
    assertThat(response.readEntity(String.class), equalTo("logoutput"));
  }
}
