package org.batfish.coordinator.resources;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import com.google.common.collect.ImmutableSet;
import org.batfish.role.NodeRole;
import org.junit.Test;

public class NodeRoleBeanTest {

  /** Checks if we properly populate the nodes field */
  @Test
  public void constructor() {
    NodeRoleBean role = new NodeRoleBean(new NodeRole("name", "a.*"), ImmutableSet.of("a", "b"));

    assertThat(role.nodes, equalTo(ImmutableSet.of("a")));
  }
}
