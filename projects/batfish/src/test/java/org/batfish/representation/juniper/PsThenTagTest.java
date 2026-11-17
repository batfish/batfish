package org.batfish.representation.juniper;

import com.google.common.testing.EqualsTester;
import org.junit.jupiter.api.Test;

class PsThenTagTest {

  @Test
  void testEquals() {
    new EqualsTester()
        .addEqualityGroup(5)
        .addEqualityGroup(new PsThenTag(5), new PsThenTag(5))
        .addEqualityGroup(new PsThenTag(6))
        .testEquals();
  }
}
