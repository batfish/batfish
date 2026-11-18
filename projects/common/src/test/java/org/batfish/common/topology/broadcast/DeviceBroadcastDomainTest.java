package org.batfish.common.topology.broadcast;

import com.google.common.testing.EqualsTester;
import org.junit.jupiter.api.Test;

class DeviceBroadcastDomainTest {
  @Test
  void testEquals() {
    new EqualsTester()
        .addEqualityGroup(new DeviceBroadcastDomain("h"), new DeviceBroadcastDomain("h"))
        .addEqualityGroup(new DeviceBroadcastDomain("h2"))
        .addEqualityGroup(new EthernetHub("h"))
        .testEquals();
  }
}
