package org.batfish.coordinator.resources;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.OK;
import static org.batfish.coordinator.resources.NetworkNodeRolesResource.noDuplicateDimensions;
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

public final class NetworkNodeRolesResourceTest extends WorkMgrServiceV2TestBase {

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private Builder getNodeRolesTarget(String container) {
    return target(CoordConsts.SVC_CFG_WORK_MGR2)
        .path(CoordConstsV2.RSC_NETWORKS)
        .path(container)
        .path(CoordConstsV2.RSC_NODE_ROLES)
        .request()
        .header(CoordConstsV2.HTTP_HEADER_BATFISH_APIKEY, CoordConsts.DEFAULT_API_KEY)
        .header(CoordConstsV2.HTTP_HEADER_BATFISH_VERSION, Version.getVersion());
  }

  @Before
  public void initContainerEnvironment() throws Exception {
    WorkMgrTestUtils.initWorkManager(_folder);
  }

  @Test
  public void testAddNodeRoleDimension() throws IOException {
    String container = "someContainer";
    Main.getWorkMgr().initNetwork(container, null);

    NodeRoleDimensionBean dimBean = new NodeRoleDimensionBean("dimension1", null, null, null);
    Response response =
        getNodeRolesTarget(container).post(Entity.entity(dimBean, MediaType.APPLICATION_JSON));

    assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
    NodeRolesData nrData = Main.getWorkMgr().getNetworkNodeRoles(container);
    assertThat(nrData.getNodeRoleDimension("dimension1").isPresent(), equalTo(true));

    Response response2 =
        getNodeRolesTarget(container).post(Entity.entity(dimBean, MediaType.APPLICATION_JSON));
    assertThat(response2.getStatus(), equalTo(BAD_REQUEST.getStatusCode()));
  }

  @Test
  public void testGetNodeRolesDefault() throws IOException {
    String network = "someContainer";
    Main.getWorkMgr().initNetwork(network, null);
    NodeRolesData nodeRolesData = NodeRolesData.builder().build();
    Response response = getNodeRolesTarget(network).get();

    assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
    assertThat(
        response.readEntity(NodeRolesDataBean.class).toNodeRolesData(), equalTo(nodeRolesData));
  }

  @Test
  public void testGetNodeRolesMissingNetwork() throws IOException {
    String network = "someContainer";
    Response response = getNodeRolesTarget(network).get();

    assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
  }

  @Test
  public void testGetNodeRolesPresent() throws IOException {
    String network = "someContainer";
    Main.getWorkMgr().initNetwork(network, null);
    NodeRolesData nodeRolesData =
        NodeRolesData.builder()
            .setDefaultDimension("a")
            .setRoleDimensions(
                ImmutableSortedSet.of(NodeRoleDimension.builder().setName("a").build()))
            .build();
    Main.getWorkMgr().putNetworkNodeRoles(nodeRolesData, network);
    Response response = getNodeRolesTarget(network).get();

    assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
    assertThat(
        response.readEntity(NodeRolesDataBean.class).toNodeRolesData(), equalTo(nodeRolesData));
  }

  @Test
  public void testNoDuplicateDimensionsSameName() {
    String name = "auto0";
    NodeRolesDataBean nodeRolesDataBean =
        new NodeRolesDataBean(
            NodeRolesData.builder()
                .setRoleDimensions(
                    ImmutableSortedSet.of(
                        NodeRoleDimension.builder()
                            .setName(name)
                            .setRoles(ImmutableSortedSet.of(new NodeRole("foo", "bar")))
                            .build(),
                        NodeRoleDimension.builder().setName(name).build()))
                .build(),
            null,
            ImmutableSet.of());

    assertThat(noDuplicateDimensions(nodeRolesDataBean), equalTo(false));
  }

  @Test
  public void testNoDuplicateDimensionsSameNameDifferentCase() {
    String name1 = "auto0";
    String name2 = "AuTo0";
    NodeRolesDataBean nodeRolesDataBean =
        new NodeRolesDataBean(
            NodeRolesData.builder()
                .setRoleDimensions(
                    ImmutableSortedSet.of(
                        NodeRoleDimension.builder().setName(name1).build(),
                        NodeRoleDimension.builder().setName(name2).build()))
                .build(),
            null,
            ImmutableSet.of());

    assertThat(noDuplicateDimensions(nodeRolesDataBean), equalTo(false));
  }

  @Test
  public void testNoDuplicateDimensionsValid() {
    String name1 = "auto0";
    String name2 = "manual0";
    NodeRolesDataBean nodeRolesDataBean =
        new NodeRolesDataBean(
            NodeRolesData.builder()
                .setRoleDimensions(
                    ImmutableSortedSet.of(
                        NodeRoleDimension.builder().setName(name1).build(),
                        NodeRoleDimension.builder().setName(name2).build()))
                .build(),
            null,
            ImmutableSet.of());

    assertThat(noDuplicateDimensions(nodeRolesDataBean), equalTo(true));
  }

  @Test
  public void testPutNodeRolesDuplicateDimensions() {
    String network = "someContainer";
    Main.getWorkMgr().initNetwork(network, null);
    String name = "auto0";
    NodeRolesDataBean nodeRolesDataBean =
        new NodeRolesDataBean(
            NodeRolesData.builder()
                .setRoleDimensions(
                    ImmutableSortedSet.of(
                        NodeRoleDimension.builder()
                            .setName(name)
                            .setRoles(ImmutableSortedSet.of(new NodeRole("foo", "bar")))
                            .build(),
                        NodeRoleDimension.builder().setName(name).build()))
                .build(),
            null,
            ImmutableSet.of());
    Response response =
        getNodeRolesTarget(network)
            .put(Entity.entity(nodeRolesDataBean, MediaType.APPLICATION_JSON));

    assertThat(response.getStatus(), equalTo(BAD_REQUEST.getStatusCode()));
  }

  @Test
  public void testPutNodeRolesMissingNetwork() {
    String network = "someContainer";
    NodeRolesDataBean nodeRolesDataBean =
        new NodeRolesDataBean(NodeRolesData.builder().build(), null, ImmutableSet.of());
    Response response =
        getNodeRolesTarget(network)
            .put(Entity.entity(nodeRolesDataBean, MediaType.APPLICATION_JSON));

    assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
  }

  @Test
  public void testPutNodeRolesNull() throws IOException {
    String network = "someContainer";
    Main.getWorkMgr().initNetwork(network, null);
    Response response =
        getNodeRolesTarget(network).put(Entity.entity("", MediaType.APPLICATION_JSON));

    assertThat(response.getStatus(), equalTo(BAD_REQUEST.getStatusCode()));
  }

  @Test
  public void testPutNodeRolesSuccess() throws IOException {
    String network = "someContainer";
    Main.getWorkMgr().initNetwork(network, null);
    NodeRolesData nodeRolesData =
        NodeRolesData.builder()
            .setDefaultDimension("a")
            .setRoleDimensions(
                ImmutableSortedSet.of(NodeRoleDimension.builder().setName("a").build()))
            .build();
    NodeRolesDataBean nodeRolesDataBean =
        new NodeRolesDataBean(nodeRolesData, null, ImmutableSet.of());
    Response response =
        getNodeRolesTarget(network)
            .put(Entity.entity(nodeRolesDataBean, MediaType.APPLICATION_JSON));

    assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
    assertThat(Main.getWorkMgr().getNetworkNodeRoles(network), equalTo(nodeRolesData));
  }
}
