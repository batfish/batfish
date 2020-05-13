package org.batfish.coordinator.resources;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.OK;
import static org.batfish.coordinator.resources.NetworkNodeRolesResource.noDuplicateRoleMappings;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import java.io.IOException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.batfish.common.CoordConsts;
import org.batfish.common.CoordConstsV2;
import org.batfish.coordinator.Main;
import org.batfish.coordinator.WorkMgrServiceV2TestBase;
import org.batfish.coordinator.WorkMgrTestUtils;
import org.batfish.role.NodeRoleDimension;
import org.batfish.role.NodeRolesData;
import org.batfish.role.RoleDimensionMapping;
import org.batfish.role.RoleMapping;
import org.batfish.version.BatfishVersion;
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
        .header(CoordConstsV2.HTTP_HEADER_BATFISH_VERSION, BatfishVersion.getVersionStatic());
  }

  @Before
  public void initTestEnvironment() {
    WorkMgrTestUtils.initWorkManager(_folder);
  }

  @Test
  public void testAddNodeRoleDimension() throws IOException {
    String container = "someContainer";
    Main.getWorkMgr().initNetwork(container, null);

    NodeRoleDimensionBean dimBean =
        new NodeRoleDimensionBean(
            NodeRoleDimension.builder("dimension1")
                .setRoleDimensionMappings(ImmutableList.of(new RoleDimensionMapping("(.*)")))
                .build(),
            null);
    try (Response response =
        getNodeRolesTarget(container).post(Entity.entity(dimBean, MediaType.APPLICATION_JSON))) {

      assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
      NodeRolesData nrData = Main.getWorkMgr().getNetworkNodeRoles(container);
      assertThat(nrData.nodeRoleDimensionFor("dimension1").isPresent(), equalTo(true));
    }

    try (Response response =
        getNodeRolesTarget(container).post(Entity.entity(dimBean, MediaType.APPLICATION_JSON))) {
      assertThat(response.getStatus(), equalTo(BAD_REQUEST.getStatusCode()));
    }
  }

  @Test
  public void testGetNodeRolesDefault() {
    String network = "someContainer";
    Main.getWorkMgr().initNetwork(network, null);
    NodeRolesData nodeRolesData = NodeRolesData.builder().build();
    try (Response response = getNodeRolesTarget(network).get()) {
      assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
      assertThat(
          response.readEntity(NodeRolesDataBean.class).toNodeRolesData(), equalTo(nodeRolesData));
    }
  }

  @Test
  public void testGetNodeRolesMissingNetwork() {
    String network = "someContainer";
    try (Response response = getNodeRolesTarget(network).get()) {
      assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
    }
  }

  @Test
  public void testGetNodeRolesPresent() throws IOException {
    String network = "someContainer";
    Main.getWorkMgr().initNetwork(network, null);
    NodeRolesData nodeRolesData =
        NodeRolesData.builder()
            .setDefaultDimension("a")
            .setRoleDimensions(ImmutableList.of(NodeRoleDimension.builder().setName("a").build()))
            .build();
    Main.getWorkMgr().putNetworkNodeRoles(nodeRolesData, network);
    try (Response response = getNodeRolesTarget(network).get()) {
      assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
      assertThat(
          response.readEntity(NodeRolesDataBean.class).toNodeRolesData(), equalTo(nodeRolesData));
    }
  }

  @Test
  public void testNoDuplicateRoleMappingsSameName() {
    String name = "auto0";
    NodeRolesDataBean nodeRolesDataBean =
        new NodeRolesDataBean(
            NodeRolesData.builder()
                .setRoleMappings(
                    ImmutableList.of(
                        new RoleMapping(name, "", null, null),
                        new RoleMapping(name, "", null, null)))
                .build(),
            null);

    assertThat(noDuplicateRoleMappings(nodeRolesDataBean), equalTo(false));
  }

  @Test
  public void testNoDuplicateRoleMappingsSameNameDifferentCase() {
    String name1 = "auto0";
    String name2 = "AuTo0";
    NodeRolesDataBean nodeRolesDataBean =
        new NodeRolesDataBean(
            NodeRolesData.builder()
                .setRoleMappings(
                    ImmutableList.of(
                        new RoleMapping(name1, "", null, null),
                        new RoleMapping(name2, "", null, null)))
                .build(),
            null);

    assertThat(noDuplicateRoleMappings(nodeRolesDataBean), equalTo(false));
  }

  @Test
  public void testNoDuplicateRoleMappingsValid() {
    String name1 = "auto0";
    String name2 = "manual0";
    NodeRolesDataBean nodeRolesDataBean =
        new NodeRolesDataBean(
            NodeRolesData.builder()
                .setRoleMappings(
                    ImmutableList.of(
                        new RoleMapping(name1, "", null, null),
                        new RoleMapping(name2, "", null, null)))
                .build(),
            null);

    assertThat(noDuplicateRoleMappings(nodeRolesDataBean), equalTo(true));
  }

  @Test
  public void testPutNodeRolesDuplicateRoleMappings() {
    String network = "someContainer";
    Main.getWorkMgr().initNetwork(network, null);
    String name = "auto0";
    NodeRolesDataBean nodeRolesDataBean =
        new NodeRolesDataBean(
            NodeRolesData.builder()
                .setRoleMappings(
                    ImmutableList.of(
                        new RoleMapping(name, "", null, null),
                        new RoleMapping(name, "", null, null)))
                .build(),
            null);
    try (Response response =
        getNodeRolesTarget(network)
            .put(Entity.entity(nodeRolesDataBean, MediaType.APPLICATION_JSON))) {
      assertThat(response.getStatus(), equalTo(BAD_REQUEST.getStatusCode()));
    }
  }

  @Test
  public void testPutNodeRolesMissingNetwork() {
    String network = "someContainer";
    NodeRolesDataBean nodeRolesDataBean =
        new NodeRolesDataBean(NodeRolesData.builder().build(), null);
    try (Response response =
        getNodeRolesTarget(network)
            .put(Entity.entity(nodeRolesDataBean, MediaType.APPLICATION_JSON))) {
      assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
    }
  }

  @Test
  public void testPutNodeRolesNull() {
    String network = "someContainer";
    Main.getWorkMgr().initNetwork(network, null);
    try (Response response =
        getNodeRolesTarget(network).put(Entity.entity("", MediaType.APPLICATION_JSON))) {
      assertThat(response.getStatus(), equalTo(BAD_REQUEST.getStatusCode()));
    }
  }

  @Test
  public void testPutNodeRolesSuccess() throws IOException {
    String network = "someContainer";
    Main.getWorkMgr().initNetwork(network, null);
    NodeRolesData nodeRolesData =
        NodeRolesData.builder()
            .setDefaultDimension("a")
            .setRoleDimensions(ImmutableList.of(NodeRoleDimension.builder().setName("a").build()))
            .build();
    NodeRolesDataBean nodeRolesDataBean = new NodeRolesDataBean(nodeRolesData, null);
    try (Response response =
        getNodeRolesTarget(network)
            .put(Entity.entity(nodeRolesDataBean, MediaType.APPLICATION_JSON))) {
      assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
      assertThat(Main.getWorkMgr().getNetworkNodeRoles(network), equalTo(nodeRolesData));
    }
  }
}
