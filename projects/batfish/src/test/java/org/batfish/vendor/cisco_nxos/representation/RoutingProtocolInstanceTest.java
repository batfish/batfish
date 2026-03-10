package org.batfish.vendor.cisco_nxos.representation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

import com.google.common.testing.EqualsTester;
import org.junit.Test;

/** Tests of {@link RoutingProtocolInstance}. */
public class RoutingProtocolInstanceTest {
  @Test
  public void testConstruction() {
    String tag = "tag";

    RoutingProtocolInstance direct = RoutingProtocolInstance.direct();
    assertThat(direct.getProtocol(), equalTo(NxosRoutingProtocol.DIRECT));
    assertThat(direct.getTag(), nullValue());

    RoutingProtocolInstance bgp = RoutingProtocolInstance.bgp(4);
    assertThat(bgp.getProtocol(), equalTo(NxosRoutingProtocol.BGP));
    assertThat(bgp.getTag(), equalTo("4"));

    RoutingProtocolInstance eigrp = RoutingProtocolInstance.eigrp(tag);
    assertThat(eigrp.getProtocol(), equalTo(NxosRoutingProtocol.EIGRP));
    assertThat(eigrp.getTag(), equalTo(tag));

    RoutingProtocolInstance isis = RoutingProtocolInstance.isis(tag);
    assertThat(isis.getProtocol(), equalTo(NxosRoutingProtocol.ISIS));
    assertThat(isis.getTag(), equalTo(tag));

    RoutingProtocolInstance lisp = RoutingProtocolInstance.lisp();
    assertThat(lisp.getProtocol(), equalTo(NxosRoutingProtocol.LISP));
    assertThat(lisp.getTag(), nullValue());

    RoutingProtocolInstance ospf = RoutingProtocolInstance.ospf(tag);
    assertThat(ospf.getProtocol(), equalTo(NxosRoutingProtocol.OSPF));
    assertThat(ospf.getTag(), equalTo(tag));

    RoutingProtocolInstance ospfv3 = RoutingProtocolInstance.ospfv3(tag);
    assertThat(ospfv3.getProtocol(), equalTo(NxosRoutingProtocol.OSPFv3));
    assertThat(ospfv3.getTag(), equalTo(tag));

    RoutingProtocolInstance rip = RoutingProtocolInstance.rip(tag);
    assertThat(rip.getProtocol(), equalTo(NxosRoutingProtocol.RIP));
    assertThat(rip.getTag(), equalTo(tag));

    RoutingProtocolInstance staticc = RoutingProtocolInstance.staticc();
    assertThat(staticc.getProtocol(), equalTo(NxosRoutingProtocol.STATIC));
    assertThat(staticc.getTag(), nullValue());
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(5)
        .addEqualityGroup(RoutingProtocolInstance.direct())
        .addEqualityGroup(RoutingProtocolInstance.ospf("a"), RoutingProtocolInstance.ospf("a"))
        .addEqualityGroup(RoutingProtocolInstance.ospf("b"))
        .addEqualityGroup(RoutingProtocolInstance.ospfv3("a"))
        .addEqualityGroup(RoutingProtocolInstance.eigrp("a"))
        .addEqualityGroup(RoutingProtocolInstance.staticc())
        .testEquals();
  }
}
