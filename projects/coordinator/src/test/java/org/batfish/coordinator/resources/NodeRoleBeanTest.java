package org.batfish.coordinator.resources;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableSet;
import org.batfish.role.NodeRole;
import org.junit.jupiter.api.Test;

class NodeRoleBeanTest {

  /** Checks if we properly populate the nodes field */
  @Test
  void constructor() {
    NodeRoleBean role = new NodeRoleBean(new NodeRole("name", "a.*"), ImmutableSet.of("a", "b"));

    assertThat(role.nodes, equalTo(ImmutableSet.of("a")));
  }

  @Test
  void toNodeRole() {
    NodeRoleBean roleBean =
        new NodeRoleBean(new NodeRole("name", "a.*"), ImmutableSet.of("a", "b"));
    NodeRole role = roleBean.toNodeRole();

    assertThat(role, equalTo(new NodeRole("name", "a.*")));
  }
}
