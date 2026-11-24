package org.batfish.coordinator.resources;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import java.io.File;
import org.batfish.coordinator.WorkMgrServiceV2TestBase;
import org.batfish.coordinator.WorkMgrTestUtils;
import org.batfish.role.NodeRoleDimension;
import org.batfish.role.RoleDimensionMapping;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class NodeRoleDimensionBeanTest extends WorkMgrServiceV2TestBase {

  @TempDir public File _folder;

  @BeforeEach
  void initTestEnvironment() {
    WorkMgrTestUtils.initWorkManager(_folder);
  }

  @Test
  void testProperties() {
    String snapshot = "snapshot1";
    String dimension = "someDimension";
    RoleDimensionMapping rdMapping = new RoleDimensionMapping("(.*)");
    NodeRoleDimension nodeRoleDimension =
        NodeRoleDimension.builder()
            .setName(dimension)
            .setRoleDimensionMappings(ImmutableList.of(rdMapping))
            .build();
    NodeRoleDimensionBean bean = new NodeRoleDimensionBean(nodeRoleDimension, snapshot);

    assertThat(bean.name, equalTo(dimension));
    assertThat(
        bean.roleDimensionMappings,
        equalTo(ImmutableList.of(new RoleDimensionMappingBean(rdMapping))));
    assertThat(bean.snapshot, equalTo(snapshot));
  }

  @Test
  void toNodeRoleDimension() {
    NodeRoleDimensionBean dimBean =
        new NodeRoleDimensionBean(NodeRoleDimension.builder("name").build(), null);
    NodeRoleDimension dim = dimBean.toNodeRoleDimension();

    // we should get the expected object
    assertThat(dim, equalTo(NodeRoleDimension.builder().setName("name").build()));
  }
}
