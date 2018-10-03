package org.batfish.coordinator.resources;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.io.IOException;
import org.batfish.coordinator.Main;
import org.batfish.coordinator.WorkMgrServiceV2TestBase;
import org.batfish.coordinator.WorkMgrTestUtils;
import org.batfish.role.NodeRole;
import org.batfish.role.NodeRoleDimension;
import org.batfish.role.NodeRoleDimension.Type;
import org.batfish.role.NodeRolesData;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class NodeRoleDimensionBeanTest extends WorkMgrServiceV2TestBase {

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Before
  public void initContainerEnvironment() throws Exception {
    WorkMgrTestUtils.initWorkManager(_folder);
  }

  @Test
  public void create() throws IOException {
    String container = "someContainer";
    Main.getWorkMgr().initNetwork(container, null);

    // create a testrig with a topology file
    WorkMgrTestUtils.initTestrigWithTopology(container, "testrig", ImmutableSet.of("a", "b"));

    // write node roles data to in the right place
    NodeRoleDimension dimension1 =
        new NodeRoleDimension("dimension1", ImmutableSortedSet.of(), null, null);
    NodeRoleDimension dimension2 =
        new NodeRoleDimension(
            "dimension2", ImmutableSortedSet.of(new NodeRole("role2", "a.*")), null, null);
    Main.getWorkMgr()
        .writeNodeRoles(
            new NodeRolesData(null, null, ImmutableSortedSet.of(dimension1, dimension2)),
            container);

    // we should the expected bean for dimension2
    assertThat(
        NodeRoleDimensionBean.create("someContainer", "dimension2"),
        equalTo(new NodeRoleDimensionBean(dimension2, "testrig", ImmutableSet.of("a", "b"))));

    // we should get null for dimension3 (Which does not exist)
    assertThat(NodeRoleDimensionBean.create("someContainer", "dimension3"), equalTo(null));
  }

  @Test
  public void toNodeRoleDimension() {
    NodeRoleDimensionBean dimBean = new NodeRoleDimensionBean("name", null, null, Type.CUSTOM);
    NodeRoleDimension dim = dimBean.toNodeRoleDimension();

    // we should get the expected object
    assertThat(dim, equalTo(new NodeRoleDimension("name", null, Type.CUSTOM, null)));
  }
}
