package org.batfish.common.topology.bridge_domain.node;

import com.google.common.testing.EqualsTester;
import org.junit.Test;

public class BridgeDomainTest {
  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(new BridgeDomain("h"), new BridgeDomain("h"))
        .addEqualityGroup(new BridgeDomain("h2"))
        .addEqualityGroup(new EthernetHub("h"))
        .testEquals();
  }
}
