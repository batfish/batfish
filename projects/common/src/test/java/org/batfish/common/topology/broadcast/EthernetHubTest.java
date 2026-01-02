package org.batfish.common.topology.broadcast;

import com.google.common.testing.EqualsTester;
import org.junit.Test;

public class EthernetHubTest {

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(new EthernetHub("5"), new EthernetHub("5"))
        .addEqualityGroup(new EthernetHub("6"))
        .testEquals();
  }
}
