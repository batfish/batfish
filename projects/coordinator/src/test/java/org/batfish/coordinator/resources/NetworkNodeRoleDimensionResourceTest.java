package org.batfish.coordinator.resources;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.io.IOException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.batfish.common.CoordConsts;
import org.batfish.common.CoordConstsV2;
import org.batfish.common.Version;
import org.batfish.coordinator.Main;
import org.batfish.coordinator.WorkMgrServiceV2TestBase;
import org.batfish.coordinator.WorkMgrTestUtils;
import org.batfish.role.NodeRole;
import org.batfish.role.NodeRoleDimension;
import org.batfish.role.NodeRolesData;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class NetworkNodeRoleDimensionResourceTest extends WorkMgrServiceV2TestBase {

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private Builder getNodeRoleDimensionTarget(String network, String dimension) {
    return target(CoordConsts.SVC_CFG_WORK_MGR2)
        .path(CoordConstsV2.RSC_NETWORKS)
        .path(network)
        .path(CoordConstsV2.RSC_NODE_ROLES)
        .path(dimension)
        .request()
        .header(CoordConstsV2.HTTP_HEADER_BATFISH_APIKEY, CoordConsts.DEFAULT_API_KEY)
        .header(CoordConstsV2.HTTP_HEADER_BATFISH_VERSION, Version.getVersion());
  }

  @Before
  public void initTestEnvironment() throws Exception {
    WorkMgrTestUtils.initWorkManager(_folder);
  }

  @Test
  public void testDelNodeRoleDimensionMissingDimension() throws IOException {
    String network = "network1";
    String dimension = "dimension1";
    Main.getWorkMgr().initNetwork(network, null);
    Main.getWorkMgr().putNetworkNodeRoles(NodeRolesData.builder().build(), network);

    Response response = getNodeRoleDimensionTarget(network, dimension).delete();
    assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
  }

  @Test
  public void testDelNodeRoleDimensionMissingNetwork() {
    String network = "network1";
    String dimension = "dimension1";

    Response response = getNodeRoleDimensionTarget(network, dimension).delete();
    assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
  }

  @Test
  public void testDelNodeRoleDimensionSuccess() throws IOException {
    String network = "network1";
    String dimension = "dimension1";
    Main.getWorkMgr().initNetwork(network, null);
    NodeRoleDimension nodeRoleDimension = NodeRoleDimension.builder().setName(dimension).build();
    Main.getWorkMgr()
        .putNetworkNodeRoles(
            NodeRolesData.builder()
                .setRoleDimensions(ImmutableSortedSet.of(nodeRoleDimension))
                .build(),
            network);
    Response response = getNodeRoleDimensionTarget(network, dimension).delete();

    assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
    assertThat(
        Main.getWorkMgr().getNetworkNodeRoles(network).getNodeRoleDimension(dimension).isPresent(),
        equalTo(false));

    // deleting again should fail
    Response response2 = getNodeRoleDimensionTarget(network, dimension).delete();
    assertThat(response2.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
  }

  @Test
  public void testGetNodeRoleDimensionMissingDimension() throws IOException {
    String network = "network1";
    String dimension = "dimension1";
    Main.getWorkMgr().initNetwork(network, null);
    Main.getWorkMgr().putNetworkNodeRoles(NodeRolesData.builder().build(), network);

    Response response = getNodeRoleDimensionTarget(network, dimension).get();
    assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
  }

  @Test
  public void testGetNodeRoleDimensionMissingNetwork() throws IOException {
    String network = "network1";
    String dimension = "dimension1";

    Response response = getNodeRoleDimensionTarget(network, dimension).get();
    assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
  }

  @Test
  public void testGetNodeRoleDimensionSuccess() throws IOException {
    String network = "network1";
    String dimension = "dimension1";
    Main.getWorkMgr().initNetwork(network, null);
    NodeRoleDimension nodeRoleDimension = NodeRoleDimension.builder().setName(dimension).build();
    Main.getWorkMgr()
        .putNetworkNodeRoles(
            NodeRolesData.builder()
                .setRoleDimensions(ImmutableSortedSet.of(nodeRoleDimension))
                .build(),
            network);

    Response response = getNodeRoleDimensionTarget(network, dimension).get();
    assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
    assertThat(
        response.readEntity(NodeRoleDimensionBean.class).toNodeRoleDimension(),
        equalTo(nodeRoleDimension));
  }

  @Test
  public void testPutNodeRoleDimensionMissingNetwork() throws IOException {
    String network = "network1";
    String dimension = "dimension1";

    NodeRoleDimensionBean dimBean = new NodeRoleDimensionBean("dimension1", null, null, null);
    Response response =
        getNodeRoleDimensionTarget(network, dimension)
            .put(Entity.entity(dimBean, MediaType.APPLICATION_JSON));
    assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
  }

  @Test
  public void testPutNodeRoleDimensionSuccess() throws IOException {
    String network = "network1";
    String dimension = "dimension1";
    Main.getWorkMgr().initNetwork(network, null);

    NodeRoleDimensionBean dimBean = new NodeRoleDimensionBean(dimension, null, null, null);
    Response response =
        getNodeRoleDimensionTarget(network, dimension)
            .put(Entity.entity(dimBean, MediaType.APPLICATION_JSON));

    assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
    NodeRolesData nrData = Main.getWorkMgr().getNetworkNodeRoles(network);
    assertThat(nrData.getNodeRoleDimension(dimension).isPresent(), equalTo(true));

    // put again should succeed and have new content
    NodeRole role = new NodeRole("role", ".*");
    dimBean.roles = ImmutableSet.of(new NodeRoleBean(role, ImmutableSet.of()));
    Response response2 =
        getNodeRoleDimensionTarget(network, dimension)
            .put(Entity.entity(dimBean, MediaType.APPLICATION_JSON));
    assertThat(response2.getStatus(), equalTo(OK.getStatusCode()));
    NodeRoleDimension dim2 =
        Main.getWorkMgr().getNetworkNodeRoles(network).getNodeRoleDimension(dimension).get();
    assertThat(dim2.getRoles(), equalTo(ImmutableSet.of(role)));
  }
}
