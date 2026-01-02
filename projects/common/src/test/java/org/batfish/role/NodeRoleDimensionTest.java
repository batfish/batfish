package org.batfish.role;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import org.junit.Test;

public class NodeRoleDimensionTest {

  @Test
  public void testOne() {
    RoleDimensionMapping rdMap = new RoleDimensionMapping("x(.+)y.*");
    NodeRoleDimension nrDim =
        NodeRoleDimension.builder()
            .setName("mydim")
            .setRoleDimensionMappings(ImmutableList.of(rdMap))
            .build();
    Set<String> nodes = ImmutableSet.of("xbordery", "core", "xbordery2", "xcorey");
    SortedMap<String, String> nodeRolesMap = nrDim.createNodeRolesMap(nodes);
    assertThat(
        nodeRolesMap,
        equalTo(
            ImmutableSortedMap.of("xbordery", "border", "xbordery2", "border", "xcorey", "core")));
  }

  @Test
  public void testMultiple() {
    RoleDimensionMapping rdMap1 = new RoleDimensionMapping("x(.+)y.*");
    RoleDimensionMapping rdMap2 = new RoleDimensionMapping("(.+)y.*");
    NodeRoleDimension nrDim =
        NodeRoleDimension.builder()
            .setName("mydim")
            .setRoleDimensionMappings(ImmutableList.of(rdMap1, rdMap2))
            .build();
    Set<String> nodes = ImmutableSet.of("xbordery", "core", "xbordery2", "corey");
    SortedMap<String, String> nodeRolesMap = nrDim.createNodeRolesMap(nodes);
    assertThat(
        nodeRolesMap,
        equalTo(
            ImmutableSortedMap.of("xbordery", "border", "xbordery2", "border", "corey", "core")));
  }

  @Test
  public void testNodeRoles() {
    NodeRole nodeRole = new NodeRole("roleName", "x(.+)y.*");
    RoleDimensionMapping rdMap = new RoleDimensionMapping(nodeRole);

    assertThat(rdMap.getRegex(), equalTo("(x(.+)y.*)"));

    NodeRoleDimension nrDim =
        NodeRoleDimension.builder()
            .setName("mydim")
            .setRoleDimensionMappings(ImmutableList.of(rdMap))
            .build();
    Set<String> nodes = ImmutableSet.of("xbordery", "core", "xbordery2", "xcorey");
    SortedMap<String, String> nodeRolesMap = nrDim.createNodeRolesMap(nodes);
    assertThat(
        nodeRolesMap,
        equalTo(
            ImmutableSortedMap.of(
                "xbordery", "roleName", "xbordery2", "roleName", "xcorey", "roleName")));
  }

  @Test
  public void testRoleNodesMap() {
    RoleDimensionMapping rdMap1 = new RoleDimensionMapping("x(.+)y.*");
    RoleDimensionMapping rdMap2 = new RoleDimensionMapping("(.+)y.*");
    NodeRoleDimension nrDim =
        NodeRoleDimension.builder()
            .setName("mydim")
            .setRoleDimensionMappings(ImmutableList.of(rdMap1, rdMap2))
            .build();
    Set<String> nodes = ImmutableSet.of("xbordery", "core", "xbordery2", "corey");
    SortedMap<String, SortedSet<String>> roleNodesMap = nrDim.createRoleNodesMap(nodes);
    assertThat(
        roleNodesMap,
        equalTo(
            ImmutableSortedMap.of(
                "border",
                ImmutableSortedSet.of("xbordery", "xbordery2"),
                "core",
                ImmutableSortedSet.of("corey"))));
  }
}
