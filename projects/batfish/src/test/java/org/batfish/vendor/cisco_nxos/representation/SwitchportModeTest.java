package org.batfish.vendor.cisco_nxos.representation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.jupiter.api.Test;

/** Test of {@link SwitchportMode}. */
final class SwitchportModeTest {

  @Test
  void testToSwitchportMode() {
    // Ensure there is conversion for every VS SwitchportMode
    for (SwitchportMode mode : SwitchportMode.values()) {
      assertThat(mode.toSwitchportMode(), notNullValue());
    }
  }
}
