package org.batfish.coordinator.resources;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.io.IOException;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import org.batfish.common.BfConsts;
import org.batfish.common.CoordConsts;
import org.batfish.common.CoordConstsV2;
import org.batfish.coordinator.Main;
import org.batfish.coordinator.WorkMgrServiceV2TestBase;
import org.batfish.coordinator.WorkMgrTestUtils;
import org.batfish.role.NodeRoleDimension;
import org.batfish.role.NodeRolesData;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class NodeRoleDimensionResourceTest extends WorkMgrServiceV2TestBase {

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Before
  public void initContainerEnvironment() throws Exception {
    WorkMgrTestUtils.initWorkManager(_folder);
  }

  private WebTarget getNodeRoleDimensionTarget(String container, String dimension) {
    return target(CoordConsts.SVC_CFG_WORK_MGR2)
        .path(CoordConstsV2.RSC_CONTAINERS)
        .path(container)
        .path(CoordConstsV2.RSC_NODE_ROLES)
        .path(dimension);
  }

  @Test
  public void delNodeRoleDimension() throws IOException {
    String container = "someContainer";
    Main.getWorkMgr().initContainer(container, null);

    // write node roles data to in the right place
    NodeRolesData data =
        new NodeRolesData(
            null,
            null,
            ImmutableSortedSet.of(
                new NodeRoleDimension("dimension1", ImmutableSortedSet.of(), null, null)));
    NodeRolesData.write(
        data,
        Main.getWorkMgr().getdirContainer(container).resolve(BfConsts.RELPATH_NODE_ROLES_PATH));

    Response response = getNodeRoleDimensionTarget(container, "dimension1").request().delete();

    // response should be OK dimension1 should have disappeared from NodeRolesData
    assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
    assertThat(
        Main.getWorkMgr()
            .getNodeRolesData(container)
            .getNodeRoleDimension("dimension1")
            .isPresent(),
        equalTo(false));

    // deleting again should fail
    Response response2 = getNodeRoleDimensionTarget(container, "dimension1").request().delete();
    assertThat(response2.getStatus(), equalTo(BAD_REQUEST.getStatusCode()));
  }

  @Test
  public void getNodeRoleDimension() throws JsonProcessingException {
    String container = "someContainer";
    Main.getWorkMgr().initContainer(container, null);

    // write node roles data to in the right place
    NodeRoleDimension dimension1 =
        new NodeRoleDimension("dimension1", ImmutableSortedSet.of(), null, null);
    NodeRolesData.write(
        new NodeRolesData(null, null, ImmutableSortedSet.of(dimension1)),
        Main.getWorkMgr().getdirContainer(container).resolve(BfConsts.RELPATH_NODE_ROLES_PATH));

    // we only check that the right type of object is returned at the expected URL target
    // we rely on NodeRolesDataBean to have created the object with the right content
    Response response = getNodeRoleDimensionTarget(container, "dimension1").request().get();
    assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
    assertThat(
        response.readEntity(NodeRoleDimensionBean.class),
        equalTo(new NodeRoleDimensionBean(dimension1, null, ImmutableSet.of())));

    // should get 404 for non-existent dimension
    Response response2 = getNodeRoleDimensionTarget(container, "dimension2").request().get();
    assertThat(response2.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
  }
}
