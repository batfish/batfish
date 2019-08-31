package org.batfish.coordinator.resources;

import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import org.batfish.coordinator.WorkMgrServiceV2TestBase;
import org.batfish.role.NodeRoleDimension;
import org.batfish.role.NodeRolesData;
import org.batfish.role.RoleDimensionMapping;
import org.junit.Test;

public class NodeRolesDataBeanTest extends WorkMgrServiceV2TestBase {

  @Test
  public void testProperties() {
    String snapshot = "snapshot1";
    String dimension = "someDimension";
    RoleDimensionMapping rdMapping = new RoleDimensionMapping("\\(.*\\)");
    NodeRoleDimension nodeRoleDimension =
        NodeRoleDimension.builder()
            .setName(dimension)
            .setRoleDimensionMappings(ImmutableList.of(rdMapping))
            .build();
    NodeRolesData data =
        NodeRolesData.builder().setRoleDimensions(ImmutableSortedSet.of(nodeRoleDimension)).build();
    NodeRolesDataBean bean = new NodeRolesDataBean(data, snapshot);

    assertThat(
        bean.roleDimensions,
        equalTo(ImmutableSet.of(new NodeRoleDimensionBean(nodeRoleDimension, snapshot))));
    assertThat(bean.defaultDimension, nullValue());
  }
}
