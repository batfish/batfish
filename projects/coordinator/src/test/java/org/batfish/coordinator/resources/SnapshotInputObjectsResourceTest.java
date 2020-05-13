package org.batfish.coordinator.resources;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.OK;
import static org.batfish.coordinator.resources.SnapshotInputObjectsResource.QP_KEY;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.util.List;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import org.batfish.common.BfConsts;
import org.batfish.common.CoordConsts;
import org.batfish.common.CoordConstsV2;
import org.batfish.coordinator.Main;
import org.batfish.coordinator.WorkMgrServiceV2TestBase;
import org.batfish.coordinator.WorkMgrTestUtils;
import org.batfish.storage.StoredObjectMetadata;
import org.batfish.version.BatfishVersion;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class SnapshotInputObjectsResourceTest extends WorkMgrServiceV2TestBase {

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private Builder getTarget(String network, String snapshot, String key) {
    return target(CoordConsts.SVC_CFG_WORK_MGR2)
        .path(CoordConstsV2.RSC_NETWORKS)
        .path(network)
        .path(CoordConstsV2.RSC_SNAPSHOTS)
        .path(snapshot)
        .path(CoordConstsV2.RSC_INPUT)
        .queryParam(QP_KEY, key)
        .request()
        .header(CoordConstsV2.HTTP_HEADER_BATFISH_APIKEY, CoordConsts.DEFAULT_API_KEY)
        .header(CoordConstsV2.HTTP_HEADER_BATFISH_VERSION, BatfishVersion.getVersionStatic());
  }

  private Builder listKeysTarget(String network, String snapshot) {
    return target(CoordConsts.SVC_CFG_WORK_MGR2)
        .path(CoordConstsV2.RSC_NETWORKS)
        .path(network)
        .path(CoordConstsV2.RSC_SNAPSHOTS)
        .path(snapshot)
        .path(CoordConstsV2.RSC_INPUT)
        .path(CoordConstsV2.RSC_LIST)
        .request()
        .header(CoordConstsV2.HTTP_HEADER_BATFISH_APIKEY, CoordConsts.DEFAULT_API_KEY)
        .header(CoordConstsV2.HTTP_HEADER_BATFISH_VERSION, BatfishVersion.getVersionStatic());
  }

  @Before
  public void initTestEnvironment() {
    WorkMgrTestUtils.initWorkManager(_folder);
  }

  @Test
  public void testGetAbsent() throws IOException {
    String network = "network1";
    String snapshot = "snapshot1";
    Main.getWorkMgr().initNetwork(network, null);
    WorkMgrTestUtils.initSnapshotWithTopology(network, snapshot, ImmutableSet.of());
    String key = "foo/bar";
    try (Response response = getTarget(network, snapshot, key).get()) {
      assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
    }
  }

  @Test
  public void testGetMissingNetwork() {
    String network = "network1";
    String snapshot = "snapshot1";
    String key = "foo/bar";
    try (Response response = getTarget(network, snapshot, key).get()) {
      assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
    }
  }

  @Test
  public void testGetMissingSnapshot() {
    String network = "network1";
    String snapshot = "snapshot1";
    Main.getWorkMgr().initNetwork(network, null);
    String key = "foo/bar";
    try (Response response = getTarget(network, snapshot, key).get()) {
      assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
    }
  }

  @Test
  public void testGetPresent() throws IOException {
    String network = "network1";
    String snapshot = "snapshot1";
    String node = "node1";
    String content = "stuff";
    Main.getWorkMgr().initNetwork(network, null);
    WorkMgrTestUtils.uploadTestSnapshot(network, snapshot, node, content, _folder);
    String key = String.format("%s/%s", BfConsts.RELPATH_CONFIGURATIONS_DIR, node);
    try (Response response = getTarget(network, snapshot, key).get()) {
      assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
      assertThat(response.readEntity(String.class), equalTo(content));
    }
  }

  @Test
  public void testListKeys() throws IOException {
    String network = "network1";
    String snapshot = "snapshot1";
    String node = "node1";
    String content = "stuff";
    Main.getWorkMgr().initNetwork(network, null);
    WorkMgrTestUtils.uploadTestSnapshot(network, snapshot, node, content, _folder);

    try (Response response = listKeysTarget(network, snapshot).get()) {
      assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
      assertThat(
          response.readEntity(new GenericType<List<StoredObjectMetadata>>() {}),
          equalTo(
              ImmutableList.of(
                  new StoredObjectMetadata("configs/" + node, content.getBytes().length))));
    }
  }

  @Test
  public void testListKeysMissingNetwork() {
    String network = "network1";
    String snapshot = "snapshot1";

    try (Response response = listKeysTarget(network, snapshot).get()) {
      assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
    }
  }

  @Test
  public void testListKeysMissingSnapshot() {
    String network = "network1";
    String snapshot = "snapshot1";
    Main.getWorkMgr().initNetwork(network, null);
    try (Response response = listKeysTarget(network, snapshot).get()) {
      assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
    }
  }
}
