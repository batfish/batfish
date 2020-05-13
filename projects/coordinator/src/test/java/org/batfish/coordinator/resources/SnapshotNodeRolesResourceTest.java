package org.batfish.coordinator.resources;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.util.Set;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.Response;
import org.batfish.common.CoordConsts;
import org.batfish.common.CoordConstsV2;
import org.batfish.coordinator.Main;
import org.batfish.coordinator.WorkMgrServiceV2TestBase;
import org.batfish.coordinator.WorkMgrTestUtils;
import org.batfish.role.NodeRolesData;
import org.batfish.version.BatfishVersion;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class SnapshotNodeRolesResourceTest extends WorkMgrServiceV2TestBase {
  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private Builder getNodeRolesTarget(String network, String snapshot, boolean inferred) {
    return target(CoordConsts.SVC_CFG_WORK_MGR2)
        .path(CoordConstsV2.RSC_NETWORKS)
        .path(network)
        .path(CoordConstsV2.RSC_SNAPSHOTS)
        .path(snapshot)
        .path(inferred ? CoordConstsV2.RSC_INFERRED_NODE_ROLES : CoordConstsV2.RSC_NODE_ROLES)
        .request()
        .header(CoordConstsV2.HTTP_HEADER_BATFISH_APIKEY, CoordConsts.DEFAULT_API_KEY)
        .header(CoordConstsV2.HTTP_HEADER_BATFISH_VERSION, BatfishVersion.getVersionStatic());
  }

  @Before
  public void initTestEnvironment() {
    WorkMgrTestUtils.initWorkManager(_folder);
  }

  @Test
  public void testInferredGetMissingNetwork() {
    String network = "network1";
    String snapshot = "snapshot1";
    try (Response response = getNodeRolesTarget(network, snapshot, true).get()) {
      assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
    }
  }

  @Test
  public void testInferredGetMissingSnapshot() {
    String network = "network1";
    String snapshot = "snapshot1";
    Main.getWorkMgr().initNetwork(network, null);
    try (Response response = getNodeRolesTarget(network, snapshot, true).get()) {
      assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
    }
  }

  @Test
  public void testInferredGetPresent() throws IOException {
    String network = "network1";
    String snapshot = "snapshot1";
    Main.getWorkMgr().initNetwork(network, null);
    Set<String> nodes = ImmutableSet.of();
    WorkMgrTestUtils.initSnapshotWithTopology(network, snapshot, nodes);
    NodeRolesData nodeRolesData = NodeRolesData.builder().build();
    WorkMgrTestUtils.setSnapshotNodeRoles(nodeRolesData, network, snapshot);
    try (Response response = getNodeRolesTarget(network, snapshot, true).get()) {
      assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
      assertThat(
          response.readEntity(NodeRolesDataBean.class),
          equalTo(new NodeRolesDataBean(nodeRolesData, snapshot)));
    }
  }

  @Test
  public void testNetworkGetMissingNetwork() {
    String network = "network1";
    String snapshot = "snapshot1";
    try (Response response = getNodeRolesTarget(network, snapshot, false).get()) {
      assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
    }
  }

  @Test
  public void testNetworkGetMissingSnapshot() {
    String network = "network1";
    String snapshot = "snapshot1";
    Main.getWorkMgr().initNetwork(network, null);
    try (Response response = getNodeRolesTarget(network, snapshot, false).get()) {
      assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
    }
  }

  @Test
  public void testNetworkGetPresent() throws IOException {
    String network = "network1";
    String snapshot = "snapshot1";
    Main.getWorkMgr().initNetwork(network, null);
    Set<String> nodes = ImmutableSet.of();
    WorkMgrTestUtils.initSnapshotWithTopology(network, snapshot, nodes);
    NodeRolesData nodeRolesData = NodeRolesData.builder().build();
    try (Response response = getNodeRolesTarget(network, snapshot, false).get()) {
      assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
      assertThat(
          response.readEntity(NodeRolesDataBean.class),
          equalTo(new NodeRolesDataBean(nodeRolesData, snapshot)));
    }
  }
}
