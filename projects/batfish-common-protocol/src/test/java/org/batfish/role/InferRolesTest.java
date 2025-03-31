package org.batfish.role;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.Topology;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests of {@link InferRoles}. */
@RunWith(JUnit4.class)
public class InferRolesTest {
  private static final Set<String> EXAMPLE_NODES =
      ImmutableSet.of(
          "as1border1",
          "as1border2",
          "as1core1",
          "as2border1",
          "as2border2",
          "as2core1",
          "as2core2",
          "as2dept1",
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
              Edge.of("as1border1", "GigabitEthernet0/0", "as1core1", "GigabitEthernet1/0"),
              Edge.of("as1border1", "GigabitEthernet1/0", "as2border1", "GigabitEthernet0/0"),
              Edge.of("as1border2", "GigabitEthernet0/0", "as3border2", "GigabitEthernet0/0"),
              Edge.of("as1border2", "GigabitEthernet1/0", "as1core1", "GigabitEthernet0/0"),
              Edge.of("as1core1", "GigabitEthernet0/0", "as1border2", "GigabitEthernet1/0"),
              Edge.of("as1core1", "GigabitEthernet1/0", "as1border1", "GigabitEthernet0/0"),
              Edge.of("as2border1", "GigabitEthernet0/0", "as1border1", "GigabitEthernet1/0"),
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
              Edge.of("as2dept1", "GigabitEthernet0/0", "as2dist1", "GigabitEthernet2/0"),
              Edge.of("as2dept1", "GigabitEthernet1/0", "as2dist2", "GigabitEthernet2/0"),
              Edge.of("as2dept1", "GigabitEthernet2/0", "host1", "eth0"),
              Edge.of("as2dept1", "GigabitEthernet3/0", "host2", "eth0"),
              Edge.of("as2dist1", "GigabitEthernet0/0", "as2core1", "GigabitEthernet2/0"),
              Edge.of("as2dist1", "GigabitEthernet1/0", "as2core2", "GigabitEthernet3/0"),
              Edge.of("as2dist1", "GigabitEthernet2/0", "as2dept1", "GigabitEthernet0/0"),
              Edge.of("as2dist2", "GigabitEthernet0/0", "as2core2", "GigabitEthernet2/0"),
              Edge.of("as2dist2", "GigabitEthernet1/0", "as2core1", "GigabitEthernet3/0"),
              Edge.of("as2dist2", "GigabitEthernet2/0", "as2dept1", "GigabitEthernet1/0"),
              Edge.of("as3border1", "GigabitEthernet0/0", "as3core1", "GigabitEthernet1/0"),
              Edge.of("as3border1", "GigabitEthernet1/0", "as2border2", "GigabitEthernet0/0"),
              Edge.of("as3border2", "GigabitEthernet0/0", "as1border2", "GigabitEthernet0/0"),
              Edge.of("as3border2", "GigabitEthernet1/0", "as3core1", "GigabitEthernet0/0"),
              Edge.of("as3core1", "GigabitEthernet0/0", "as3border2", "GigabitEthernet1/0"),
              Edge.of("as3core1", "GigabitEthernet1/0", "as3border1", "GigabitEthernet0/0"),
              Edge.of("as3core1", "GigabitEthernet2/0", "as3core1", "GigabitEthernet3/0"),
              Edge.of("as3core1", "GigabitEthernet3/0", "as3core1", "GigabitEthernet2/0"),
              Edge.of("host1", "eth0", "as2dept1", "GigabitEthernet2/0"),
              Edge.of("host2", "eth0", "as2dept1", "GigabitEthernet3/0")));

  @Test
  public void inferRolesOnExampleTopology() {
    Optional<RoleMapping> roleMappingOpt =
        new InferRoles(EXAMPLE_NODES, EXAMPLE_TOPOLOGY).inferRoles();

    assertTrue(roleMappingOpt.isPresent());

    RoleMapping roleMapping = roleMappingOpt.get();

    assertThat(roleMapping.getRegex(), equalTo("([a-zA-Z]+)[0-9]+([a-zA-Z]+)[0-9]+"));

    assertThat(roleMapping.getRoleDimensionsGroups().entrySet(), hasSize(2));

    for (Map.Entry<String, List<Integer>> entry :
        roleMapping.getRoleDimensionsGroups().entrySet()) {
      String dimName = entry.getKey();
      List<Integer> groups = entry.getValue();
      if (dimName.equals(NodeRoleDimension.AUTO_DIMENSION_PRIMARY)) {
        assertThat(groups, equalTo(ImmutableList.of(2)));
      } else if (dimName.equals(NodeRoleDimension.AUTO_DIMENSION_PREFIX + "1")) {
        assertThat(groups, equalTo(ImmutableList.of(1)));
      } else {
        fail();
      }
    }

    assertTrue(roleMapping.getCanonicalRoleNames().isEmpty());
  }
}
