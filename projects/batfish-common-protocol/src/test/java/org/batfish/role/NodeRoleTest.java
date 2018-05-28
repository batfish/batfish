package org.batfish.role;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import com.google.common.collect.ImmutableSet;
import org.junit.Test;

public class NodeRoleTest {

  @Test
  public void constructor() {
    NodeRole role = new NodeRole("name", "a.*", ImmutableSet.of("a", "b"));

    assertThat(role.getNodes(), equalTo(ImmutableSet.of("a")));
  }

  @Test
  public void resetNodes() {
    NodeRole role = new NodeRole("name", "a.*", ImmutableSet.of("a1", "a2", "b"));

    role.resetNodes(ImmutableSet.of("a1", "a3"));

    // a2 should disappear and a3 should appear
    assertThat(role.getNodes(), equalTo(ImmutableSet.of("a1", "a3")));
  }
}
