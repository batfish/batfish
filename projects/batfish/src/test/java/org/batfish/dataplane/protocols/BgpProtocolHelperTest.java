package org.batfish.dataplane.protocols;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertNotNull;

import com.google.common.collect.ImmutableSortedSet;
import org.batfish.common.WellKnownCommunity;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.BgpPeerConfig;
import org.batfish.datamodel.BgpRoute;
import org.batfish.datamodel.BgpSessionProperties;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.Vrf;
import org.batfish.dataplane.exceptions.BgpRoutePropagationException;
import org.junit.Test;

public final class BgpProtocolHelperTest {

  /** Test that transformBgpRouteOnExport copies the tag from the input route. */
  @Test
  public void transformBgpRouteOnExport_setTag() throws BgpRoutePropagationException {
    NetworkFactory nf = new NetworkFactory();
    BgpActivePeerConfig fromNeighbor = nf.bgpNeighborBuilder().build();
    BgpPeerConfig toNeighbor = nf.bgpNeighborBuilder().build();
    BgpSessionProperties sessionProperties = BgpSessionProperties.from(fromNeighbor, toNeighbor);
    Ip fromVrfIp = new Ip("2.2.2.2");
    Vrf fromVrf = nf.vrfBuilder().build();
    nf.bgpProcessBuilder().setVrf(fromVrf).setRouterId(fromVrfIp).build();
    assertNotNull(fromVrf.getBgpProcess());
    Vrf toVrf = nf.vrfBuilder().build();
    AbstractRoute route =
        StaticRoute.builder()
            .setNetwork(Prefix.parse("1.0.0.0/8"))
            .setTag(12345)
            .setAdministrativeCost(1)
            .build();
    BgpRoute.Builder transformedRoute =
        BgpProtocolHelper.transformBgpRouteOnExport(
            fromNeighbor, toNeighbor, sessionProperties, fromVrf, toVrf, route);
    assertThat(transformedRoute.getTag(), equalTo(12345));
  }

  /**
   * Test that transformBgpRouteOnExport returns {@code null} (meaning do not export the route) if
   * it has the {@value org.batfish.common.WellKnownCommunity#NO_ADVERTISE} community.
   */
  @Test
  public void transformBgpRouteOnExport_noAdvertise() throws BgpRoutePropagationException {
    NetworkFactory nf = new NetworkFactory();
    BgpActivePeerConfig fromNeighbor = nf.bgpNeighborBuilder().build();
    BgpPeerConfig toNeighbor = nf.bgpNeighborBuilder().build();
    BgpSessionProperties sessionProperties = BgpSessionProperties.from(fromNeighbor, toNeighbor);
    Vrf fromVrf = nf.vrfBuilder().build();
    Vrf toVrf = nf.vrfBuilder().build();
    BgpRoute route =
        new BgpRoute.Builder()
            .setOriginatorIp(new Ip("1.1.1.1"))
            .setOriginType(OriginType.IGP)
            .setCommunities(ImmutableSortedSet.of(WellKnownCommunity.NO_ADVERTISE))
            .setNetwork(Prefix.ZERO)
            .setProtocol(RoutingProtocol.IBGP)
            .setReceivedFromIp(new Ip("2.2.2.2"))
            .build();
    BgpRoute.Builder transformedRoute =
        BgpProtocolHelper.transformBgpRouteOnExport(
            fromNeighbor, toNeighbor, sessionProperties, fromVrf, toVrf, route);
    assertThat(transformedRoute, nullValue());
  }
}
