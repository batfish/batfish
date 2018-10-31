package org.batfish.role;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.function.Predicate;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.Topology;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests of {@link InferRoles}. */
@RunWith(JUnit4.class)
public class InferRolesMixedCaseTest {
  private static final Set<String> EXAMPLE_NODES =
      ImmutableSet.of(
          "as1BORDER1",
          "as1bORder2",
          "as1core1",
          "as2border1",
          "as2border2",
          "as2core1",
          "as2core2",
          "as2DEPT1",
          "as2dist1",
          "as2dist2",
          "as3border1",
          "as3border2",
          "as3core1",
          "host1",
          "host2");

  private static final Topology EXAMPLE_TOPOLOGY =
      new Topology(
          ImmutableSortedSet.of(
              Edge.of("as1BORDER1", "GigabitEthernet0/0", "as1core1", "GigabitEthernet1/0"),
              Edge.of("as1BORDER1", "GigabitEthernet1/0", "as2border1", "GigabitEthernet0/0"),
              Edge.of("as1bORder2", "GigabitEthernet0/0", "as3border2", "GigabitEthernet0/0"),
              Edge.of("as1bORder2", "GigabitEthernet1/0", "as1core1", "GigabitEthernet0/0"),
              Edge.of("as1core1", "GigabitEthernet0/0", "as1bORder2", "GigabitEthernet1/0"),
              Edge.of("as1core1", "GigabitEthernet1/0", "as1BORDER1", "GigabitEthernet0/0"),
              Edge.of("as2border1", "GigabitEthernet0/0", "as1BORDER1", "GigabitEthernet1/0"),
              Edge.of("as2border1", "GigabitEthernet1/0", "as2core1", "GigabitEthernet0/0"),
              Edge.of("as2border1", "GigabitEthernet2/0", "as2core2", "GigabitEthernet1/0"),
              Edge.of("as2border2", "GigabitEthernet0/0", "as3border1", "GigabitEthernet1/0"),
              Edge.of("as2border2", "GigabitEthernet1/0", "as2core2", "GigabitEthernet0/0"),
              Edge.of("as2border2", "GigabitEthernet2/0", "as2core1", "GigabitEthernet1/0"),
              Edge.of("as2core1", "GigabitEthernet0/0", "as2border1", "GigabitEthernet1/0"),
              Edge.of("as2core1", "GigabitEthernet1/0", "as2border2", "GigabitEthernet2/0"),
              Edge.of("as2core1", "GigabitEthernet2/0", "as2dist1", "GigabitEthernet0/0"),
              Edge.of("as2core1", "GigabitEthernet3/0", "as2dist2", "GigabitEthernet1/0"),
              Edge.of("as2core2", "GigabitEthernet0/0", "as2border2", "GigabitEthernet1/0"),
              Edge.of("as2core2", "GigabitEthernet1/0", "as2border1", "GigabitEthernet2/0"),
              Edge.of("as2core2", "GigabitEthernet2/0", "as2dist2", "GigabitEthernet0/0"),
              Edge.of("as2core2", "GigabitEthernet3/0", "as2dist1", "GigabitEthernet1/0"),
              Edge.of("as2DEPT1", "GigabitEthernet0/0", "as2dist1", "GigabitEthernet2/0"),
              Edge.of("as2DEPT1", "GigabitEthernet1/0", "as2dist2", "GigabitEthernet2/0"),
              Edge.of("as2DEPT1", "GigabitEthernet2/0", "host1", "eth0"),
              Edge.of("as2DEPT1", "GigabitEthernet3/0", "host2", "eth0"),
              Edge.of("as2dist1", "GigabitEthernet0/0", "as2core1", "GigabitEthernet2/0"),
              Edge.of("as2dist1", "GigabitEthernet1/0", "as2core2", "GigabitEthernet3/0"),
              Edge.of("as2dist1", "GigabitEthernet2/0", "as2DEPT1", "GigabitEthernet0/0"),
              Edge.of("as2dist2", "GigabitEthernet0/0", "as2core2", "GigabitEthernet2/0"),
              Edge.of("as2dist2", "GigabitEthernet1/0", "as2core1", "GigabitEthernet3/0"),
              Edge.of("as2dist2", "GigabitEthernet2/0", "as2DEPT1", "GigabitEthernet1/0"),
              Edge.of("as3border1", "GigabitEthernet0/0", "as3core1", "GigabitEthernet1/0"),
              Edge.of("as3border1", "GigabitEthernet1/0", "as2border2", "GigabitEthernet0/0"),
              Edge.of("as3border2", "GigabitEthernet0/0", "as1bORder2", "GigabitEthernet0/0"),
              Edge.of("as3border2", "GigabitEthernet1/0", "as3core1", "GigabitEthernet0/0"),
              Edge.of("as3core1", "GigabitEthernet0/0", "as3border2", "GigabitEthernet1/0"),
              Edge.of("as3core1", "GigabitEthernet1/0", "as3border1", "GigabitEthernet0/0"),
              Edge.of("as3core1", "GigabitEthernet2/0", "as3core1", "GigabitEthernet3/0"),
              Edge.of("as3core1", "GigabitEthernet3/0", "as3core1", "GigabitEthernet2/0"),
              Edge.of("host1", "eth0", "as2DEPT1", "GigabitEthernet2/0"),
              Edge.of("host2", "eth0", "as2DEPT1", "GigabitEthernet3/0")));

  private static Set<String> filterSet(Set<String> nodes, Predicate<String> filter) {
    return nodes.stream().filter(filter).collect(ImmutableSet.toImmutableSet());
  }

  @Test
  public void inferRolesOnExampleTopology() throws JsonProcessingException {
    SortedSet<NodeRoleDimension> roles =
        new InferRoles(EXAMPLE_NODES, EXAMPLE_TOPOLOGY).inferRoles();

    assertThat(BatfishObjectMapper.writePrettyString(roles), roles, hasSize(2));

    NodeRoleDimension d1 = roles.first();
    assertThat(
        d1.createRoleNodesMap(EXAMPLE_NODES),
        equalTo(
            ImmutableMap.of(
                "border",
                filterSet(EXAMPLE_NODES, s -> s.toLowerCase().contains("border")),
                "core",
                filterSet(EXAMPLE_NODES, s -> s.toLowerCase().contains("core")),
                "dept",
                filterSet(EXAMPLE_NODES, s -> s.toLowerCase().contains("dept")),
                "dist",
                filterSet(EXAMPLE_NODES, s -> s.toLowerCase().contains("dist")))));

    NodeRoleDimension d2 = roles.last();
    assertThat(
        d2.createRoleNodesMap(EXAMPLE_NODES),
        equalTo(
            ImmutableMap.of(
                "as", filterSet(EXAMPLE_NODES, s -> s.toLowerCase().startsWith("as")))));
  }

  @Test
  public void inferCaseSensitiveRolesOnExampleTopology() throws JsonProcessingException {
    SortedSet<NodeRoleDimension> roles =
        new InferRoles(EXAMPLE_NODES, EXAMPLE_TOPOLOGY, true).inferRoles();

    assertThat(BatfishObjectMapper.writePrettyString(roles), roles, hasSize(3));

    Iterator<NodeRoleDimension> i = roles.iterator();

    NodeRoleDimension d1 = i.next();
    Map<String, Set<String>> expected1 = new TreeMap<>();
    expected1.put(
        "as1-BORDER", filterSet(EXAMPLE_NODES, s -> s.contains("as1") && s.contains("BORDER")));
    expected1.put(
        "as1-bORder", filterSet(EXAMPLE_NODES, s -> s.contains("as1") && s.contains("bORder")));
    expected1.put(
        "as1-core", filterSet(EXAMPLE_NODES, s -> s.contains("as1") && s.contains("core")));
    expected1.put(
        "as2-DEPT", filterSet(EXAMPLE_NODES, s -> s.contains("as2") && s.contains("DEPT")));
    expected1.put(
        "as2-border", filterSet(EXAMPLE_NODES, s -> s.contains("as2") && s.contains("border")));
    expected1.put(
        "as2-core", filterSet(EXAMPLE_NODES, s -> s.contains("as2") && s.contains("core")));
    expected1.put(
        "as2-dist", filterSet(EXAMPLE_NODES, s -> s.contains("as2") && s.contains("dist")));
    expected1.put(
        "as3-border", filterSet(EXAMPLE_NODES, s -> s.contains("as3") && s.contains("border")));
    expected1.put(
        "as3-core", filterSet(EXAMPLE_NODES, s -> s.contains("as3") && s.contains("core")));
    assertThat(d1.createRoleNodesMap(EXAMPLE_NODES), equalTo(ImmutableMap.copyOf(expected1)));

    NodeRoleDimension d2 = i.next();
    assertThat(
        d2.createRoleNodesMap(EXAMPLE_NODES),
        equalTo(ImmutableMap.of("as", filterSet(EXAMPLE_NODES, s -> s.startsWith("as")))));

    NodeRoleDimension d3 = i.next();
    Map<String, Set<String>> expected3 = new TreeMap<>();
    expected3.put("BORDER", filterSet(EXAMPLE_NODES, s -> s.contains("BORDER")));
    expected3.put("DEPT", filterSet(EXAMPLE_NODES, s -> s.contains("DEPT")));
    expected3.put("bORder", filterSet(EXAMPLE_NODES, s -> s.contains("bORder")));
    expected3.put("border", filterSet(EXAMPLE_NODES, s -> s.contains("border")));
    expected3.put("core", filterSet(EXAMPLE_NODES, s -> s.contains("core")));
    expected3.put("dist", filterSet(EXAMPLE_NODES, s -> s.contains("dist")));
    assertThat(d3.createRoleNodesMap(EXAMPLE_NODES), equalTo(ImmutableMap.copyOf(expected3)));
  }
}
