package org.batfish.coordinator.resources;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableSet;
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
import org.batfish.role.NodeRolesData;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class NodeRolesResourceTest extends WorkMgrServiceV2TestBase {

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Before
  public void initContainerEnvironment() throws Exception {
    WorkMgrTestUtils.initWorkManager(_folder);
  }

  private Builder getNodeRolesTarget(String container) {
    return target(CoordConsts.SVC_CFG_WORK_MGR2)
        .path(CoordConstsV2.RSC_CONTAINERS)
        .path(container)
        .path(CoordConstsV2.RSC_NODE_ROLES)
        .request()
        .header(CoordConstsV2.HTTP_HEADER_BATFISH_APIKEY, CoordConsts.DEFAULT_API_KEY)
        .header(CoordConstsV2.HTTP_HEADER_BATFISH_VERSION, Version.getVersion());
  }

  @Test
  public void addNodeRoleDimension() throws IOException {
    String container = "someContainer";
    Main.getWorkMgr().initContainer(container, null);

    NodeRoleDimensionBean dimBean = new NodeRoleDimensionBean("dimension1", null, null, null);
    Response response =
        getNodeRolesTarget(container).post(Entity.entity(dimBean, MediaType.APPLICATION_JSON));

    assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
    NodeRolesData nrData = Main.getWorkMgr().getNodeRolesData(container);
    assertThat(nrData.getNodeRoleDimension("dimension1").isPresent(), equalTo(true));

    Response response2 =
        getNodeRolesTarget(container).post(Entity.entity(dimBean, MediaType.APPLICATION_JSON));
    assertThat(response2.getStatus(), equalTo(BAD_REQUEST.getStatusCode()));
  }

  @Test
  public void getNodeRoles() {
    String container = "someContainer";
    Main.getWorkMgr().initContainer(container, null);

    // we only check that the right type of object is returned at the expected URL target
    // we rely on NodeRolesDataBean to have created the object with the right content
    Response response = getNodeRolesTarget(container).get();
    assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
    assertThat(
        response.readEntity(NodeRolesDataBean.class),
        equalTo(
            new NodeRolesDataBean(new NodeRolesData(null, null, null), null, ImmutableSet.of())));
  }
}
