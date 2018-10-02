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
import org.batfish.role.NodeRolesData;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class NodeRolesDataBeanTest extends WorkMgrServiceV2TestBase {

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Before
  public void initContainerEnvironment() throws Exception {
    WorkMgrTestUtils.initWorkManager(_folder);
  }

  @Test
  public void create() throws IOException {
    String container = "someContainer";
    Main.getWorkMgr().initContainer(container, null);

    // create a testrig with a topology file
    WorkMgrTestUtils.initTestrigWithTopology(container, "testrig", ImmutableSet.of("a", "b"));

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
    Main.getWorkMgr().writeNodeRoles(data, container);

    // we should get OK and the expected bean
    assertThat(
        NodeRolesDataBean.create("someContainer"),
        equalTo(new NodeRolesDataBean(data, "testrig", ImmutableSet.of("a", "b"))));
  }

  @Test
  public void createEmptyContainer() throws IOException {
    String container = "someContainer";
    Main.getWorkMgr().initContainer(container, null);

    assertThat(
        NodeRolesDataBean.create("someContainer"),
        equalTo(
            new NodeRolesDataBean(new NodeRolesData(null, null, null), null, ImmutableSet.of())));
  }
}
