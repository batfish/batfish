package org.batfish.coordinator.resources;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.util.Set;
import org.batfish.coordinator.WorkMgrServiceV2TestBase;
import org.batfish.coordinator.WorkMgrTestUtils;
import org.batfish.role.NodeRole;
import org.batfish.role.NodeRoleDimension;
import org.batfish.role.NodeRoleDimension.Type;
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
  public void testProperties() {
    String snapshot = "snapshot1";
    String dimension = "someDimension";
    String role = "someRole";
    Set<String> nodes = ImmutableSet.of("a", "b");
    NodeRole nodeRole = new NodeRole(role, "a.*");
    NodeRoleDimension nodeRoleDimension =
        NodeRoleDimension.builder()
            .setName(dimension)
            .setRoles(ImmutableSortedSet.of(nodeRole))
            .build();
    NodeRoleDimensionBean bean = new NodeRoleDimensionBean(nodeRoleDimension, snapshot, nodes);

    assertThat(bean.name, equalTo(dimension));
    assertThat(bean.roles, equalTo(ImmutableSet.of(new NodeRoleBean(nodeRole, nodes))));
    assertThat(bean.snapshot, equalTo(snapshot));
    assertThat(bean.type, equalTo(NodeRoleDimension.Type.CUSTOM));
  }

  @Test
  public void toNodeRoleDimension() {
    NodeRoleDimensionBean dimBean = new NodeRoleDimensionBean("name", null, null, Type.CUSTOM);
    NodeRoleDimension dim = dimBean.toNodeRoleDimension();

    // we should get the expected object
    assertThat(dim, equalTo(NodeRoleDimension.builder().setName("name").build()));
  }
}
