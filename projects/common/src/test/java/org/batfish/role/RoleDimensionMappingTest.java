package org.batfish.role;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import java.util.Set;
import java.util.SortedMap;
import org.junit.Test;

public class RoleDimensionMappingTest {

  @Test
  public void testRegex() {
    RoleDimensionMapping rdMap = new RoleDimensionMapping("x(.+)y.*");
    Set<String> nodes = ImmutableSet.of("xbordery", "core", "xbordery2", "xcorey");
    SortedMap<String, String> nodeRolesMap = rdMap.createNodeRolesMap(nodes);
    assertThat(
        nodeRolesMap,
        equalTo(
            ImmutableSortedMap.of("xbordery", "border", "xbordery2", "border", "xcorey", "core")));
  }

  @Test
  public void testNodeRole() {
    NodeRole nr = new NodeRole("name", "x(.+)y.*");
    RoleDimensionMapping rdMap = new RoleDimensionMapping(nr);
    Set<String> nodes = ImmutableSet.of("xbordery", "core", "xbordery2", "xcorey");
    SortedMap<String, String> nodeRolesMap = rdMap.createNodeRolesMap(nodes);
    assertThat(
        nodeRolesMap,
        equalTo(ImmutableSortedMap.of("xbordery", "name", "xbordery2", "name", "xcorey", "name")));
  }

  @Test
  public void testGroups() {
    RoleDimensionMapping rdMap =
        new RoleDimensionMapping("x(b.*d)(.+)y.*", ImmutableList.of(2, 1), null);
    Set<String> nodes = ImmutableSet.of("xbordery", "core", "xbordery2", "xcorey");
    SortedMap<String, String> nodeRolesMap = rdMap.createNodeRolesMap(nodes);
    assertThat(
        nodeRolesMap,
        equalTo(ImmutableSortedMap.of("xbordery", "er-bord", "xbordery2", "er-bord")));
  }

  @Test
  public void testCanonicalNameMap() {
    RoleDimensionMapping rdMap =
        new RoleDimensionMapping("x(.+)y.*", null, ImmutableMap.of("border", "something"));
    Set<String> nodes = ImmutableSet.of("xbordery", "core", "xbordery2", "xcorey");
    SortedMap<String, String> nodeRolesMap = rdMap.createNodeRolesMap(nodes);
    assertThat(
        nodeRolesMap,
        equalTo(
            ImmutableSortedMap.of(
                "xbordery", "something", "xbordery2", "something", "xcorey", "core")));
  }
}
