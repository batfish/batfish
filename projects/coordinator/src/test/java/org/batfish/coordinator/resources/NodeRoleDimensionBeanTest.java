package org.batfish.coordinator.resources;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import org.batfish.coordinator.WorkMgrServiceV2TestBase;
import org.batfish.coordinator.WorkMgrTestUtils;
import org.batfish.role.NodeRoleDimension;
import org.batfish.role.RoleDimensionMapping;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class NodeRoleDimensionBeanTest extends WorkMgrServiceV2TestBase {

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Before
  public void initTestEnvironment() {
    WorkMgrTestUtils.initWorkManager(_folder);
  }

  @Test
  public void testProperties() {
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
  public void toNodeRoleDimension() {
    NodeRoleDimensionBean dimBean =
        new NodeRoleDimensionBean(NodeRoleDimension.builder("name").build(), null);
    NodeRoleDimension dim = dimBean.toNodeRoleDimension();

    // we should get the expected object
    assertThat(dim, equalTo(NodeRoleDimension.builder().setName("name").build()));
  }
}
