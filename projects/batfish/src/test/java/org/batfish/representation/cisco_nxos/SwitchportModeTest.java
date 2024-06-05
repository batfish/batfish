package org.batfish.representation.cisco_nxos;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.Test;

/** Test of {@link SwitchportMode}. */
public final class SwitchportModeTest {

  @Test
  public void testToSwitchportMode() {
    // Ensure there is conversion for every VS SwitchportMode
    for (SwitchportMode mode : SwitchportMode.values()) {
      assertThat(mode.toSwitchportMode(), notNullValue());
    }
  }
}
