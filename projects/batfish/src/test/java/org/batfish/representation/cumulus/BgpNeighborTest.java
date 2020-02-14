package org.batfish.representation.cumulus;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import org.batfish.common.ip.Ip;
import org.junit.Test;

/** Test of {@link BgpNeighbor} and subclasses. */
public final class BgpNeighborTest {

  @Test
  public void testInheritFrom() {
    BgpPeerGroupNeighbor pg = new BgpPeerGroupNeighbor("pg");
    pg.setBgpNeighborSource(new BgpNeighborSourceAddress(Ip.MAX));
    pg.setDescription("pg desc");
    pg.setEbgpMultihop(5L);
    BgpNeighborIpv4UnicastAddressFamily pgIpv4 = new BgpNeighborIpv4UnicastAddressFamily();
    pgIpv4.setNextHopSelf(true);
    pg.setIpv4UnicastAddressFamily(pgIpv4);
    BgpNeighborL2vpnEvpnAddressFamily pgL2vpn = new BgpNeighborL2vpnEvpnAddressFamily();
    pgL2vpn.setRouteReflectorClient(true);
    pg.setL2vpnEvpnAddressFamily(pgL2vpn);
    pg.setRemoteAs(8L);
    pg.setRemoteAsType(RemoteAsType.EXPLICIT);
    {
      // all props set, inherit nothing
      BgpIpNeighbor leaf = new BgpIpNeighbor("leaf");
      leaf.setBgpNeighborSource(new BgpNeighborSourceAddress(Ip.ZERO));
      leaf.setDescription("leaf desc");
      leaf.setEbgpMultihop(1L);
      BgpNeighborIpv4UnicastAddressFamily leafIpv4 = new BgpNeighborIpv4UnicastAddressFamily();
      leafIpv4.setNextHopSelf(false);
      leaf.setIpv4UnicastAddressFamily(leafIpv4);
      BgpNeighborL2vpnEvpnAddressFamily leafL2vpn = new BgpNeighborL2vpnEvpnAddressFamily();
      leafL2vpn.setRouteReflectorClient(false);
      leaf.setL2vpnEvpnAddressFamily(leafL2vpn);
      leaf.setPeerGroup("pg");
      leaf.setRemoteAsType(RemoteAsType.EXTERNAL);

      leaf.inheritFrom(ImmutableMap.of("pg", pg));

      assertThat(leaf.getBgpNeighborSource(), equalTo(new BgpNeighborSourceAddress(Ip.ZERO)));
      assertThat(leaf.getDescription(), equalTo("leaf desc"));
      assertThat(leaf.getEbgpMultihop(), equalTo(1L));
      assertThat(leaf.getIpv4UnicastAddressFamily().getNextHopSelf(), not(equalTo(Boolean.TRUE)));
      assertThat(
          leaf.getL2vpnEvpnAddressFamily().getRouteReflectorClient(), not(equalTo(Boolean.TRUE)));
      assertThat(leaf.getPeerGroup(), equalTo("pg"));
      assertThat(leaf.getRemoteAs(), nullValue());
      assertThat(leaf.getRemoteAsType(), equalTo(RemoteAsType.EXTERNAL));
    }
    {
      // no props set, inherit everything applicable
      BgpIpNeighbor leaf = new BgpIpNeighbor("leaf");
      leaf.setPeerGroup("pg");
      leaf.inheritFrom(ImmutableMap.of("pg", pg));

      assertThat(leaf.getBgpNeighborSource(), equalTo(new BgpNeighborSourceAddress(Ip.MAX)));
      // don't inherit description
      assertThat(leaf.getDescription(), nullValue());
      assertThat(leaf.getEbgpMultihop(), equalTo(5L));
      assertThat(leaf.getIpv4UnicastAddressFamily().getNextHopSelf(), equalTo(Boolean.TRUE));
      assertThat(leaf.getL2vpnEvpnAddressFamily().getRouteReflectorClient(), equalTo(Boolean.TRUE));
      assertThat(leaf.getRemoteAs(), equalTo(8L));
      assertThat(leaf.getRemoteAsType(), equalTo(RemoteAsType.EXPLICIT));
    }
  }
}
