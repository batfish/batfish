package org.batfish.role;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

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
  public void testGroups() {
    RoleDimensionMapping rdMap =
        new RoleDimensionMapping("x(b.*d)(.+)y.*", ImmutableList.of(2, 1), null, false);
    Set<String> nodes = ImmutableSet.of("xbordery", "core", "xbordery2", "xcorey");
    SortedMap<String, String> nodeRolesMap = rdMap.createNodeRolesMap(nodes);
    assertThat(
        nodeRolesMap,
        equalTo(ImmutableSortedMap.of("xbordery", "er-bord", "xbordery2", "er-bord")));
  }

  @Test
  public void testCanonicalNameMap() {
    RoleDimensionMapping rdMap =
        new RoleDimensionMapping("x(.+)y.*", null, ImmutableMap.of("border", "something"), false);
    Set<String> nodes = ImmutableSet.of("xbordery", "core", "xbordery2", "xcorey");
    SortedMap<String, String> nodeRolesMap = rdMap.createNodeRolesMap(nodes);
    assertThat(
        nodeRolesMap,
        equalTo(
            ImmutableSortedMap.of(
                "xbordery", "something", "xbordery2", "something", "xcorey", "core")));
  }

  @Test
  public void testCaseInsensitive() {
    RoleDimensionMapping rdMap = new RoleDimensionMapping("x(.+)y.*");
    Set<String> nodes = ImmutableSet.of("xBorDery", "core", "xboRderY2", "xcorey");
    SortedMap<String, String> nodeRolesMap = rdMap.createNodeRolesMap(nodes);
    assertThat(
        nodeRolesMap,
        equalTo(
            ImmutableSortedMap.of("xBorDery", "border", "xboRderY2", "border", "xcorey", "core")));
  }

  @Test
  public void testCaseSensitive() {
    RoleDimensionMapping rdMap = new RoleDimensionMapping("x(.+)y.*", null, null, true);
    Set<String> nodes = ImmutableSet.of("xBorDery", "core", "xboRderY2", "xcorey");
    SortedMap<String, String> nodeRolesMap = rdMap.createNodeRolesMap(nodes);
    assertThat(
        nodeRolesMap, equalTo(ImmutableSortedMap.of("xBorDery", "BorDer", "xcorey", "core")));
  }
}
