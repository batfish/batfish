package org.batfish.representation.juniper;

import com.google.common.testing.EqualsTester;
import org.junit.jupiter.api.Test;

class PsFromLocalPreferenceTest {
  @Test
  void testEquals() {
    new EqualsTester()
        .addEqualityGroup(5)
        .addEqualityGroup(new PsFromLocalPreference(5), new PsFromLocalPreference(5))
        .addEqualityGroup(new PsFromLocalPreference(6))
        .testEquals();
  }
}
