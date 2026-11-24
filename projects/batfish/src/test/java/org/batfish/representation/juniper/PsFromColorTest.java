package org.batfish.representation.juniper;

import com.google.common.testing.EqualsTester;
import org.junit.jupiter.api.Test;

class PsFromColorTest {
  @Test
  void testEquals() {
    new EqualsTester()
        .addEqualityGroup(5)
        .addEqualityGroup(new PsFromColor(5), new PsFromColor(5))
        .addEqualityGroup(new PsFromColor(6))
        .testEquals();
  }
}
