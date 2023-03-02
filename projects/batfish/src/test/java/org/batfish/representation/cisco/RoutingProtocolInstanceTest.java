package org.batfish.representation.cisco;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.testing.EqualsTester;
import org.batfish.datamodel.RoutingProtocol;
import org.junit.Test;

/** Tests of {@link RoutingProtocolInstance}. */
public class RoutingProtocolInstanceTest {
  @Test
  public void testConstruction() {
    RoutingProtocolInstance bgp = RoutingProtocolInstance.bgp();
    assertThat(bgp.getProtocol(), equalTo(RoutingProtocol.BGP));

    RoutingProtocolInstance connected = RoutingProtocolInstance.connected();
    assertThat(connected.getProtocol(), equalTo(RoutingProtocol.CONNECTED));

    RoutingProtocolInstance eigrp = RoutingProtocolInstance.eigrp(10L);
    assertThat(eigrp.getProtocol(), equalTo(RoutingProtocol.EIGRP));

    RoutingProtocolInstance ospf = RoutingProtocolInstance.ospf();
    assertThat(ospf.getProtocol(), equalTo(RoutingProtocol.OSPF));

    RoutingProtocolInstance rip = RoutingProtocolInstance.rip();
    assertThat(rip.getProtocol(), equalTo(RoutingProtocol.RIP));

    RoutingProtocolInstance isis_l1 = RoutingProtocolInstance.isis_l1();
    assertThat(isis_l1.getProtocol(), equalTo(RoutingProtocol.ISIS_L1));

    RoutingProtocolInstance staticRoutingProtocol = RoutingProtocolInstance.staticRoutingProtocol();
    assertThat(staticRoutingProtocol.getProtocol(), equalTo(RoutingProtocol.STATIC));
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(RoutingProtocolInstance.bgp())
        .addEqualityGroup(RoutingProtocolInstance.connected())
        .addEqualityGroup(RoutingProtocolInstance.eigrp(10L))
        .addEqualityGroup(RoutingProtocolInstance.ospf())
        .addEqualityGroup(RoutingProtocolInstance.rip())
        .addEqualityGroup(RoutingProtocolInstance.isis_l1())
        .addEqualityGroup(RoutingProtocolInstance.staticRoutingProtocol())
        .testEquals();
  }
}
