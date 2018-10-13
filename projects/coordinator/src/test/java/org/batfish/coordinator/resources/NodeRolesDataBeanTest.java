package org.batfish.coordinator.resources;

import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.io.IOException;
import java.util.Set;
import org.batfish.coordinator.WorkMgrServiceV2TestBase;
import org.batfish.role.NodeRole;
import org.batfish.role.NodeRoleDimension;
import org.batfish.role.NodeRolesData;
import org.junit.Test;

public class NodeRolesDataBeanTest extends WorkMgrServiceV2TestBase {

  @Test
  public void testProperties() throws IOException {
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
    NodeRolesData data =
        NodeRolesData.builder().setRoleDimensions(ImmutableSortedSet.of(nodeRoleDimension)).build();
    NodeRolesDataBean bean = new NodeRolesDataBean(data, snapshot, nodes);

    assertThat(
        bean.roleDimensions,
        equalTo(ImmutableSet.of(new NodeRoleDimensionBean(nodeRoleDimension, snapshot, nodes))));
    assertThat(bean.defaultDimension, nullValue());
  }
}
