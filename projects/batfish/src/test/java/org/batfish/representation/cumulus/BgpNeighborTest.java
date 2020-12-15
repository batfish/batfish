package org.batfish.representation.cumulus;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import com.google.common.testing.EqualsTester;
import org.batfish.datamodel.BgpPeerConfig;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.LongSpace;
import org.batfish.representation.cumulus.BgpNeighbor.RemoteAs;
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
    pg.setRemoteAs(RemoteAs.explicit(8));
    {
      // all props set, inherit nothing
      BgpIpNeighbor leaf = new BgpIpNeighbor("leaf", Ip.parse("1.2.3.4"));
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
      leaf.setRemoteAs(RemoteAs.external());

      leaf.inheritFrom(ImmutableMap.of("pg", pg));

      assertThat(leaf.getBgpNeighborSource(), equalTo(new BgpNeighborSourceAddress(Ip.ZERO)));
      assertThat(leaf.getDescription(), equalTo("leaf desc"));
      assertThat(leaf.getEbgpMultihop(), equalTo(1L));
      assertThat(leaf.getIpv4UnicastAddressFamily().getNextHopSelf(), not(equalTo(Boolean.TRUE)));
      assertThat(
          leaf.getL2vpnEvpnAddressFamily().getRouteReflectorClient(), not(equalTo(Boolean.TRUE)));
      assertThat(leaf.getPeerGroup(), equalTo("pg"));
      assertThat(leaf.getRemoteAs(), equalTo(RemoteAs.external()));
    }
    {
      // no props set, inherit everything applicable
      BgpIpNeighbor leaf = new BgpIpNeighbor("leaf", Ip.parse("1.2.3.4"));
      leaf.setPeerGroup("pg");
      leaf.inheritFrom(ImmutableMap.of("pg", pg));

      assertThat(leaf.getBgpNeighborSource(), equalTo(new BgpNeighborSourceAddress(Ip.MAX)));
      // don't inherit description
      assertThat(leaf.getDescription(), nullValue());
      assertThat(leaf.getEbgpMultihop(), equalTo(5L));
      assertThat(leaf.getIpv4UnicastAddressFamily().getNextHopSelf(), equalTo(Boolean.TRUE));
      assertThat(leaf.getL2vpnEvpnAddressFamily().getRouteReflectorClient(), equalTo(Boolean.TRUE));
      assertThat(leaf.getRemoteAs(), equalTo(RemoteAs.explicit(8)));
    }
  }

  @Test
  public void testRemoteAsEquals() {
    new EqualsTester()
        .addEqualityGroup(1)
        .addEqualityGroup(RemoteAs.explicit(5), RemoteAs.explicit(5))
        .addEqualityGroup(RemoteAs.explicit(7))
        .addEqualityGroup(RemoteAs.internal())
        .addEqualityGroup(RemoteAs.external())
        .testEquals();
  }

  @Test
  public void testRemoteAsExplicit() {
    RemoteAs explicit5 = RemoteAs.explicit(5);
    // remoteAs should always resolve to 5
    assertThat(explicit5.getRemoteAs(null), equalTo(LongSpace.of(5)));
    assertThat(explicit5.getRemoteAs(7L), equalTo(LongSpace.of(5)));
    // Only 5 is known iBGP
    assertFalse(explicit5.isKnownIbgp(4L));
    assertTrue(explicit5.isKnownIbgp(5L));
    assertFalse(explicit5.isKnownIbgp(6L));
    // All but 5 are known eBGP
    assertTrue(explicit5.isKnownEbgp(4L));
    assertFalse(explicit5.isKnownEbgp(5L));
    assertTrue(explicit5.isKnownEbgp(6L));
    // Neither is known with null
    assertFalse(explicit5.isKnownIbgp(null));
    assertFalse(explicit5.isKnownEbgp(null));
  }

  @Test
  public void testRemoteAsExternal() {
    RemoteAs external = RemoteAs.external();
    // RemoteAS is anything but argument, empty for null.
    assertThat(external.getRemoteAs(null), equalTo(LongSpace.EMPTY));
    assertThat(
        external.getRemoteAs(5L),
        equalTo(BgpPeerConfig.ALL_AS_NUMBERS.difference(LongSpace.of(5))));
    // Never known iBGP
    assertFalse(external.isKnownIbgp(null));
    assertFalse(external.isKnownIbgp(5L));
    // Always known eBGP
    assertTrue(external.isKnownEbgp(null));
    assertTrue(external.isKnownEbgp(5L));
  }

  @Test
  public void testRemoteAsInternal() {
    RemoteAs internal = RemoteAs.internal();
    // RemoteAS is same as argument, empty for null.
    assertThat(internal.getRemoteAs(null), equalTo(LongSpace.EMPTY));
    assertThat(internal.getRemoteAs(5L), equalTo(LongSpace.of(5)));
    assertThat(internal.getRemoteAs(7L), equalTo(LongSpace.of(7)));
    // Always known iBGP
    assertTrue(internal.isKnownIbgp(null));
    assertTrue(internal.isKnownIbgp(5L));
    // Never known eBGP
    assertFalse(internal.isKnownEbgp(null));
    assertFalse(internal.isKnownEbgp(5L));
  }
}
