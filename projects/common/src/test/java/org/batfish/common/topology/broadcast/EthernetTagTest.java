package org.batfish.common.topology.broadcast;

import com.google.common.testing.EqualsTester;
import org.junit.Test;

public class EthernetTagTest {
  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(EthernetTag.tagged(5), EthernetTag.tagged(5))
        .addEqualityGroup(EthernetTag.untagged())
        .testEquals();
  }
}
