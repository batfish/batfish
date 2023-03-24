package org.batfish.representation.cisco;

import com.google.common.testing.EqualsTester;
import org.junit.Test;

/** Tests of {@link RoutingProtocolInstance}. */
public class RoutingProtocolInstanceTest {

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(RoutingProtocolInstance.bgp(10L), RoutingProtocolInstance.bgp(10L))
        .addEqualityGroup(RoutingProtocolInstance.bgp(20L))
        .addEqualityGroup(RoutingProtocolInstance.connected())
        .addEqualityGroup(RoutingProtocolInstance.eigrp(10L), RoutingProtocolInstance.eigrp(10L))
        .addEqualityGroup(RoutingProtocolInstance.eigrp(20L))
        .addEqualityGroup(RoutingProtocolInstance.ospf())
        .addEqualityGroup(RoutingProtocolInstance.rip())
        .addEqualityGroup(RoutingProtocolInstance.isis_l1())
        .addEqualityGroup(RoutingProtocolInstance.staticRoutingProtocol())
        .testEquals();
  }
}
