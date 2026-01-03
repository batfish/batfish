package org.batfish.common.topology.broadcast;

import com.google.common.testing.EqualsTester;
import org.junit.Test;

public class DeviceBroadcastDomainTest {
  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(new DeviceBroadcastDomain("h"), new DeviceBroadcastDomain("h"))
        .addEqualityGroup(new DeviceBroadcastDomain("h2"))
        .addEqualityGroup(new EthernetHub("h"))
        .testEquals();
  }
}
