package org.batfish.coordinator.resources;

import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
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
        NodeRolesData.builder()
            .setRoleDimensions(ImmutableList.of(nodeRoleDimension))
            .setRoleDimensionOrder(ImmutableList.of(dimension))
            .build();
    NodeRolesDataBean bean = new NodeRolesDataBean(data, snapshot);

    assertThat(
        bean.roleDimensions,
        equalTo(ImmutableList.of(new NodeRoleDimensionBean(nodeRoleDimension, snapshot))));
    assertThat(bean.defaultDimension, nullValue());
    assertThat(bean.roleDimensionOrder, equalTo(ImmutableList.of(dimension)));
  }
}
