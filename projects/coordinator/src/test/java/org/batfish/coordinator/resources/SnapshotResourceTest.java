package org.batfish.coordinator.resources;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.OK;
import static org.batfish.coordinator.WorkMgrTestUtils.createSingleFileSnapshotZip;
import static org.batfish.coordinator.WorkMgrTestUtils.uploadTestSnapshot;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableSet;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.batfish.common.CoordConsts;
import org.batfish.common.CoordConstsV2;
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
        .path(CoordConstsV2.RSC_NETWORKS)
        .path(network)
        .path(CoordConstsV2.RSC_SNAPSHOTS)
        .path(snapshot)
        .path(CoordConstsV2.RSC_COMPLETED_WORK)
        .request()
        .header(CoordConstsV2.HTTP_HEADER_BATFISH_APIKEY, CoordConsts.DEFAULT_API_KEY);
  }

  private Builder getPojoTopologyTarget(String network, String snapshot) {
    return target(CoordConsts.SVC_CFG_WORK_MGR2)
        .path(CoordConstsV2.RSC_NETWORKS)
        .path(network)
        .path(CoordConstsV2.RSC_SNAPSHOTS)
        .path(snapshot)
        .path(CoordConstsV2.RSC_POJO_TOPOLOGY)
        .request()
        .header(CoordConstsV2.HTTP_HEADER_BATFISH_APIKEY, CoordConsts.DEFAULT_API_KEY);
  }

  private Builder getTarget(String network, String snapshot) {
    return target(CoordConsts.SVC_CFG_WORK_MGR2)
        .path(CoordConstsV2.RSC_NETWORKS)
        .path(network)
        .path(CoordConstsV2.RSC_SNAPSHOTS)
        .path(snapshot)
        .request()
        .header(CoordConstsV2.HTTP_HEADER_BATFISH_APIKEY, CoordConsts.DEFAULT_API_KEY);
  }

  private Builder getTopologyTarget(String network, String snapshot) {
    return target(CoordConsts.SVC_CFG_WORK_MGR2)
        .path(CoordConstsV2.RSC_NETWORKS)
        .path(network)
        .path(CoordConstsV2.RSC_SNAPSHOTS)
        .path(snapshot)
        .path(CoordConstsV2.RSC_TOPOLOGY)
        .request()
        .header(CoordConstsV2.HTTP_HEADER_BATFISH_APIKEY, CoordConsts.DEFAULT_API_KEY);
  }

  private Builder getWorkLogTarget(String network, String snapshot, String workId) {
    return target(CoordConsts.SVC_CFG_WORK_MGR2)
        .path(CoordConstsV2.RSC_NETWORKS)
        .path(network)
        .path(CoordConstsV2.RSC_SNAPSHOTS)
        .path(snapshot)
        .path(CoordConstsV2.RSC_WORK_LOG)
        .path(workId)
        .request()
        .header(CoordConstsV2.HTTP_HEADER_BATFISH_APIKEY, CoordConsts.DEFAULT_API_KEY);
  }

  @Before
  public void initTestEnvironment() {
    WorkMgrTestUtils.initWorkManager(_folder);
  }

  @Test
  public void testDeleteSnapshotMissingNetwork() {
    String network = "network1";
    String snapshot = "snapshot1";
    try (Response response = getTarget(network, snapshot).delete()) {
      assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
    }
  }

  @Test
  public void testDeleteSnapshotMissingSnapshot() {
    String network = "network1";
    String snapshot = "snapshot1";
    Main.getWorkMgr().initNetwork(network, null);
    try (Response response = getTarget(network, snapshot).delete()) {
      assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
    }
  }

  @Test
  public void testDeleteSnapshotSuccess() throws IOException {
    String network = "network1";
    String snapshot = "snapshot1";
    Main.getWorkMgr().initNetwork(network, null);
    uploadTestSnapshot(network, snapshot, _folder);
    try (Response response = getTarget(network, snapshot).delete()) {

      // should succeed first time
      assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
    }

    try (Response response = getTarget(network, snapshot).delete()) {

      // should fail second time
      assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
    }
  }

  @Test
  public void testCompletedWorkMissingNetwork() {
    String network = "network1";
    String snapshot = "snapshot1";
    try (Response response = getCompletedWorkTarget(network, snapshot).get()) {
      assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
    }
  }

  @Test
  public void testCompletedWorkMissingSnapshot() {
    String network = "network1";
    String snapshot = "snapshot1";
    Main.getWorkMgr().initNetwork(network, null);
    try (Response response = getCompletedWorkTarget(network, snapshot).get()) {
      assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
    }
  }

  @Test
  public void testCompletedWork() throws IOException {
    String network = "network1";
    String snapshot = "snapshot1";
    Main.getWorkMgr().initNetwork(network, null);
    WorkMgrTestUtils.initSnapshotWithTopology(network, snapshot, ImmutableSet.of());

    try (Response response = getCompletedWorkTarget(network, snapshot).get()) {
      assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
    }
  }

  @Test
  public void testGetPojoTopologyMissingNetwork() {
    String network = "network1";
    String snapshot = "snapshot1";
    try (Response response = getPojoTopologyTarget(network, snapshot).get()) {
      assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
    }
  }

  @Test
  public void testGetPojoTopologyMissingSnapshot() {
    String network = "network1";
    String snapshot = "snapshot1";
    Main.getWorkMgr().initNetwork(network, null);
    try (Response response = getPojoTopologyTarget(network, snapshot).get()) {
      assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
    }
  }

  @Test
  public void testGetPojoTopologyPresent() throws IOException {
    String network = "network1";
    String snapshot = "snapshot1";
    Main.getWorkMgr().initNetwork(network, null);
    WorkMgrTestUtils.initSnapshotWithTopology(network, snapshot, ImmutableSet.of());
    try (Response response = getPojoTopologyTarget(network, snapshot).get()) {
      assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
      assertThat(response.readEntity(org.batfish.datamodel.pojo.Topology.class), notNullValue());
    }
  }

  @Test
  public void testGetSnapshotMetadataMissingNetwork() {
    String network = "network1";
    String snapshot = "snapshot1";
    try (Response response = getTarget(network, snapshot).get()) {
      assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
    }
  }

  @Test
  public void testGetSnapshotMetadataMissingSnapshot() {
    String network = "network1";
    String snapshot = "snapshot1";
    Main.getWorkMgr().initNetwork(network, null);
    try (Response response = getTarget(network, snapshot).get()) {
      assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
    }
  }

  @Test
  public void testGetSnapshotMetadataPresent() throws IOException {
    String network = "network1";
    String snapshot = "snapshot1";
    Main.getWorkMgr().initNetwork(network, null);
    WorkMgrTestUtils.initSnapshotWithTopology(network, snapshot, ImmutableSet.of());
    try (Response response = getTarget(network, snapshot).get()) {
      assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
      assertThat(response.readEntity(SnapshotMetadata.class), notNullValue());
    }
  }

  @Test
  public void testGetTopologyMissingNetwork() {
    String network = "network1";
    String snapshot = "snapshot1";
    try (Response response = getTopologyTarget(network, snapshot).get()) {
      assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
    }
  }

  @Test
  public void testGetTopologyMissingSnapshot() {
    String network = "network1";
    String snapshot = "snapshot1";
    Main.getWorkMgr().initNetwork(network, null);
    try (Response response = getTopologyTarget(network, snapshot).get()) {
      assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
    }
  }

  @Test
  public void testGetTopologyPresent() throws IOException {
    String network = "network1";
    String snapshot = "snapshot1";
    Main.getWorkMgr().initNetwork(network, null);
    WorkMgrTestUtils.initSnapshotWithTopology(network, snapshot, ImmutableSet.of());
    try (Response response = getTopologyTarget(network, snapshot).get()) {
      assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
      assertThat(response.readEntity(Topology.class), notNullValue());
    }
  }

  @Test
  public void testGetWorkLogMissingNetwork() {
    String network = "network1";
    String snapshot = "snapshot1";

    try (Response response = getWorkLogTarget(network, snapshot, "workId").get()) {
      assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
    }
  }

  @Test
  public void testGetWorkLogMissingSnapshot() {
    String network = "network1";
    String snapshot = "snapshot1";
    Main.getWorkMgr().initNetwork(network, null);

    try (Response response = getWorkLogTarget(network, snapshot, "workid").get()) {
      assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
    }
  }

  @Test
  public void testGetWorkLogMissingFile() throws IOException {
    String network = "network1";
    String snapshot = "snapshot1";
    Main.getWorkMgr().initNetwork(network, null);
    WorkMgrTestUtils.uploadTestSnapshot(network, snapshot, _folder);

    try (Response response = getWorkLogTarget(network, snapshot, "missingworkid").get()) {
      assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
    }
  }

  @Test
  public void testGetWorkLogPresent() throws IOException {
    String network = "network1";
    String snapshot = "snapshot1";
    Main.getWorkMgr().initNetwork(network, null);
    WorkMgrTestUtils.uploadTestSnapshot(network, snapshot, _folder);
    IdManager idm = Main.getWorkMgr().getIdManager();
    NetworkId networkId = idm.getNetworkId(network).get();
    SnapshotId snapshotId = idm.getSnapshotId(snapshot, networkId).get();
    Main.getWorkMgr().getStorage().storeWorkLog("logoutput", networkId, snapshotId, "workid");

    Builder target = getWorkLogTarget(network, snapshot, "workid");
    try (Response response = target.get()) {
      assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
      assertThat(response.getMediaType(), equalTo(MediaType.TEXT_PLAIN_TYPE));
      assertThat(response.readEntity(String.class), equalTo("logoutput"));
    }
  }

  @Test
  public void testUploadSnapshot() throws IOException {
    String network = "network1";
    String snapshot = "snapshot1";
    String filename = "snapshot.zip";
    Main.getWorkMgr().initNetwork(network, null);
    Path tmpSnapshotZip = createSingleFileSnapshotZip(snapshot, filename, "foo", _folder);
    Builder target = getTarget(network, snapshot);
    URI outputUri;
    SnapshotMetadata metadata;

    try (InputStream inputStream = Files.newInputStream(tmpSnapshotZip)) {
      try (Response resp =
          target.post(Entity.entity(inputStream, MediaType.APPLICATION_OCTET_STREAM))) {
        assertThat(resp.getStatus(), equalTo(CREATED.getStatusCode()));
        outputUri = resp.getLocation();
      }
    }
    assertTrue(Main.getWorkMgr().checkSnapshotExists(network, snapshot));
    try (Response resp =
        target(outputUri.getPath())
            .request()
            .header(CoordConstsV2.HTTP_HEADER_BATFISH_APIKEY, CoordConsts.DEFAULT_API_KEY)
            .get()) {
      assertThat(resp.getStatus(), equalTo(OK.getStatusCode()));
      metadata = resp.readEntity(SnapshotMetadata.class);
    }
    assertNotNull(metadata);
  }

  @Test
  public void testUploadSnapshotAlreadyExists() throws IOException {
    String network = "network1";
    String snapshot = "snapshot1";
    String filename = "snapshot.zip";
    Main.getWorkMgr().initNetwork(network, null);
    Path tmpSnapshotZip = createSingleFileSnapshotZip(snapshot, filename, "foo", _folder);
    try (InputStream inputStream = Files.newInputStream(tmpSnapshotZip)) {
      Main.getWorkMgr().uploadSnapshot(network, snapshot, inputStream);
    }
    Builder target = getTarget(network, snapshot);
    try (InputStream inputStream = Files.newInputStream(tmpSnapshotZip)) {
      try (Response resp =
          target.post(Entity.entity(inputStream, MediaType.APPLICATION_OCTET_STREAM))) {
        assertThat(resp.getStatus(), equalTo(BAD_REQUEST.getStatusCode()));
        String msg = resp.readEntity(String.class);
        assertThat(
            msg, equalTo("Snapshot in network 'network1' with name: 'snapshot1' already exists"));
      }
    }
    assertTrue(Main.getWorkMgr().checkSnapshotExists(network, snapshot));
  }

  @Test
  public void testUploadSnapshotBadContent() throws IOException {
    String network = "network1";
    String snapshot = "snapshot1";
    Main.getWorkMgr().initNetwork(network, null);
    Builder target = getTarget(network, snapshot);
    try (ByteArrayInputStream inputStream = new ByteArrayInputStream(new byte[] {})) {
      try (Response resp =
          target.post(Entity.entity(inputStream, MediaType.APPLICATION_OCTET_STREAM))) {
        assertThat(resp.getStatus(), equalTo(BAD_REQUEST.getStatusCode()));
      }
    }
    assertFalse(Main.getWorkMgr().checkSnapshotExists(network, snapshot));
  }
}
