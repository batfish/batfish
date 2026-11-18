package org.batfish.common.topology.broadcast;

import com.google.common.testing.EqualsTester;
import org.junit.jupiter.api.Test;

class EthernetTagTest {
  @Test
  void testEquals() {
    new EqualsTester()
        .addEqualityGroup(EthernetTag.tagged(5), EthernetTag.tagged(5))
        .addEqualityGroup(EthernetTag.untagged())
        .testEquals();
  }
}
