package org.batfish.representation.juniper;

import com.google.common.testing.EqualsTester;
import org.junit.jupiter.api.Test;

class PsFromTagTest {

  @Test
  void testEquals() {
    new EqualsTester()
        .addEqualityGroup(5)
        .addEqualityGroup(new PsFromTag(5), new PsFromTag(5))
        .addEqualityGroup(new PsFromTag(6))
        .testEquals();
  }
}
