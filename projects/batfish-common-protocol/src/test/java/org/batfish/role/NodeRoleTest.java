package org.batfish.role;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

public class NodeRoleTest {

  @Test
  public void matches() {
    NodeRole role = new NodeRole("hello", "a.*");

    assertThat(role.matches("a1"), equalTo(true));
    assertThat(role.matches("b1"), equalTo(false));
  }
}
