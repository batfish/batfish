package org.batfish.coordinator.resources;

import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import org.batfish.coordinator.WorkMgrServiceV2TestBase;
import org.batfish.role.NodeRolesData;
import org.batfish.role.RoleMapping;
import org.junit.Test;

public class NodeRolesDataBeanTest extends WorkMgrServiceV2TestBase {

  @Test
  public void testProperties() {
    String snapshot = "snapshot1";
    String dimension = "someDimension";
    RoleMapping rMapping = new RoleMapping("mymap", "\\(.*\\)", null, null);
    NodeRolesData data =
        NodeRolesData.builder()
            .setRoleMappings(ImmutableList.of(rMapping))
            .setRoleDimensionOrder(ImmutableList.of(dimension))
            .build();
    NodeRolesDataBean bean = new NodeRolesDataBean(data, snapshot);

    assertThat(bean.roleMappings, equalTo(ImmutableList.of(new RoleMappingBean(rMapping))));
    assertThat(bean.defaultDimension, nullValue());
    assertThat(bean.roleDimensionOrder, equalTo(ImmutableList.of(dimension)));
  }
}
