package org.batfish.common.topology.bridge_domain;

import com.google.common.testing.EqualsTester;
import org.junit.Test;

/** Test of {@link State}. */
public final class StateTest {

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(State.empty(), State.of(null, null))
        .addEqualityGroup(State.of(1, null))
        .addEqualityGroup(State.of(null, 2))
        .testEquals();
  }
}
