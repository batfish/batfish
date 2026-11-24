package org.batfish.role;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Test;

class NodeRoleTest {

  @Test
  void matches() {
    NodeRole role = new NodeRole("hello", "a.*");

    assertThat(role.matches("a1"), equalTo(true));
    assertThat(role.matches("b1"), equalTo(false));
  }
}
