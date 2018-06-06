package org.batfish.coordinator.resources;

import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.Optional;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import org.batfish.common.BfConsts;
import org.batfish.common.CoordConsts;
import org.batfish.common.CoordConstsV2;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.common.util.CommonUtil;
import org.batfish.coordinator.Main;
import org.batfish.coordinator.TestrigMetadataMgr;
import org.batfish.coordinator.WorkMgrServiceV2TestBase;
import org.batfish.coordinator.WorkMgrTestUtils;
import org.batfish.datamodel.TestrigMetadata;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.pojo.Topology;
import org.batfish.role.NodeRole;
import org.batfish.role.NodeRoleDimension;
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

  private WebTarget getNodeRolesTarget(String container) {
    return target(CoordConsts.SVC_CFG_WORK_MGR2)
        .path(CoordConstsV2.RSC_CONTAINERS)
        .path(container)
        .path(CoordConstsV2.RSC_NODE_ROLES);
  }

  @Test
  public void getNodeRolesWithTestrig() throws IOException {
    String container = "someContainer";
    Main.getWorkMgr().initContainer(container, null);

    // create a testrig with a topology file
    Path containerDir =
        Main.getSettings().getContainersLocation().resolve(container).toAbsolutePath();
    Files.createDirectories(containerDir.resolve(BfConsts.RELPATH_TESTRIGS_DIR).resolve("testrig"));
    TestrigMetadataMgr.writeMetadata(
        new TestrigMetadata(new Date().toInstant(), "env"), container, "testrig");
    Topology topology = new Topology("testrig");
    topology.setNodes(ImmutableSet.of(new Node("a1"), new Node("b1")));
    CommonUtil.writeFile(
        Main.getWorkMgr()
            .getdirTestrig(container, "testrig")
            .resolve(BfConsts.RELPATH_TESTRIG_POJO_TOPOLOGY_PATH),
        BatfishObjectMapper.mapper().writeValueAsString(topology));

    // write node roles data to in the right place
    NodeRolesData data =
        new NodeRolesData(
            null,
            null,
            ImmutableSortedSet.of(
                new NodeRoleDimension(
                    "someDimension",
                    ImmutableSortedSet.of(new NodeRole("someRole", "a.*")),
                    null,
                    null)));
    NodeRolesData.write(data, containerDir.resolve(BfConsts.RELPATH_NODE_ROLES_PATH));

    // we should get OK and the expected bean
    Response response = getNodeRolesTarget(container).request().get();
    assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
    assertThat(
        response.readEntity(NodeRolesDataBean.class),
        equalTo(new NodeRolesDataBean(data, Optional.of("testrig"), ImmutableSet.of("a1", "b1"))));
  }

  @Test
  public void getNodeRolesEmptyContainer() throws JsonProcessingException {
    String container = "someContainer";
    Main.getWorkMgr().initContainer(container, null);
    Response response = getNodeRolesTarget(container).request().get();

    // got OK and what we got back equalled what we wrote
    assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
    assertThat(
        response.readEntity(NodeRolesDataBean.class),
        equalTo(
            new NodeRolesDataBean(
                new NodeRolesData(null, null, null), Optional.empty(), ImmutableSet.of())));
  }
}
