package org.batfish.role;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedSet;
import java.util.List;
import org.junit.Test;

public class NodeRolesSpecificationTest {

  @Test
  public void testEmpty() {
    NodeRolesSpecification spec = new NodeRolesSpecification(null, null);
    NodeRolesData data = spec.toNodeRolesData();
    assertThat(data.getDefaultDimension(), equalTo(null));
    assertThat(data.getNodeRoleDimensions(), equalTo(ImmutableSortedSet.of()));
  }

  @Test
  public void testToNodeRolesData() {
    List<RoleMapping> rMaps =
        ImmutableList.of(
            new RoleMapping(
                "1",
                ImmutableMap.of("dim1", ImmutableList.of(1, 2), "dim2", ImmutableList.of(3, 4)),
                ImmutableMap.of("dim2", ImmutableMap.of("foo", "bar")),
                false),
            new RoleMapping(
                "2",
                ImmutableMap.of("dim1", ImmutableList.of(5), "dim3", ImmutableList.of(1)),
                ImmutableMap.of(
                    "dim2", ImmutableMap.of("foo", "bar"), "dim1", ImmutableMap.of("abc", "def")),
                true));
    NodeRolesSpecification spec = new NodeRolesSpecification(rMaps, null);
    NodeRolesData data = spec.toNodeRolesData();

    assertThat(data.getDefaultDimension(), equalTo(null));
    assertThat(
        data.getNodeRoleDimensions(),
        equalTo(
            ImmutableSortedSet.of(
                NodeRoleDimension.builder()
                    .setName("dim1")
                    .setRoleDimensionMappings(
                        ImmutableList.of(
                            new RoleDimensionMapping("1", ImmutableList.of(1, 2), null, false),
                            new RoleDimensionMapping(
                                "2", ImmutableList.of(5), ImmutableMap.of("abc", "def"), true)))
                    .build(),
                NodeRoleDimension.builder()
                    .setName("dim2")
                    .setRoleDimensionMappings(
                        ImmutableList.of(
                            new RoleDimensionMapping(
                                "1", ImmutableList.of(3, 4), ImmutableMap.of("foo", "bar"), false)))
                    .build(),
                NodeRoleDimension.builder()
                    .setName("dim3")
                    .setRoleDimensionMappings(
                        ImmutableList.of(
                            new RoleDimensionMapping("2", ImmutableList.of(1), null, true)))
                    .build())));
  }
}
