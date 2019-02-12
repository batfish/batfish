package org.batfish.dataplane.rib;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import com.google.common.testing.EqualsTester;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.BgpRoute;
import org.batfish.datamodel.BgpTieBreaker;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.MultipathEquivalentAsPathMatchMode;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.StaticRoute;
import org.batfish.dataplane.rib.RibDelta.Builder;
import org.batfish.dataplane.rib.RouteAdvertisement.Reason;
import org.junit.Before;
import org.junit.Test;

/** Tests for {@link RibDelta} */
public class RibDeltaTest {

  private static final int PREFIX_LENGTH = 24;
  private RibDelta.Builder<AbstractRoute> _builder;

  @Before
  public void setupNewBuilder() {
    _builder = RibDelta.builder();
  }

  /** Check that empty {@link Builder} produces a null delta */
  @Test
  public void testBuildEmptyDelta() {
    RibDelta<AbstractRoute> delta = _builder.build();
    assertThat(delta, equalTo(RibDelta.empty()));
  }

  @Test
  public void testEquals() {
    Prefix p1 = Prefix.parse("1.1.1.0/24");
    Prefix p2 = Prefix.parse("2.2.2.0/24");
    StaticRoute sr1 =
        StaticRoute.builder().setNetwork(p1).setAdmin(1).setNextHopIp(Ip.parse("2.2.2.2")).build();
    StaticRoute sr2 =
        StaticRoute.builder().setNetwork(p2).setAdmin(1).setNextHopIp(Ip.parse("2.2.2.2")).build();
    StaticRoute sr3 =
        StaticRoute.builder().setNetwork(p2).setAdmin(1).setNextHopIp(Ip.parse("3.3.3.3")).build();
    new EqualsTester()
        .addEqualityGroup(RibDelta.builder().build(), RibDelta.builder().build())
        .addEqualityGroup(
            RibDelta.builder().add(p1, sr1).build(),
            RibDelta.<StaticRoute>builder().add(p1, sr1).build())
        .addEqualityGroup(RibDelta.builder().remove(p1, sr1, Reason.WITHDRAW).build())
        .addEqualityGroup(RibDelta.builder().remove(p1, sr1, Reason.REPLACE).build())
        .addEqualityGroup(RibDelta.builder().add(p2, sr2))
        .addEqualityGroup(RibDelta.builder().add(p2, sr3))
        .addEqualityGroup(RibDelta.builder().remove(p2, sr2, Reason.WITHDRAW).build())
        .addEqualityGroup(RibDelta.builder().add(p1, sr1).add(p2, sr2).build())
        .addEqualityGroup(RibDelta.builder().add(p1, sr1).remove(p2, sr2, Reason.WITHDRAW).build())
        .addEqualityGroup(new Object())
        .testEquals();
  }

  /** Check {@link Builder} route addition and that duplicate adds get squashed */
  @Test
  public void testBuilderAddRoute() {
    StaticRoute route1 =
        StaticRoute.builder()
            .setNetwork(Prefix.create(Ip.parse("1.1.1.0"), PREFIX_LENGTH))
            .setNextHopIp(Ip.ZERO)
            .setNextHopInterface(null)
            .setAdministrativeCost(1)
            .setMetric(0L)
            .setTag(1)
            .build();
    // Route 2 & 3 should be equal
    StaticRoute route2 =
        StaticRoute.builder()
            .setNetwork(Prefix.create(Ip.parse("2.1.1.0"), PREFIX_LENGTH))
            .setNextHopIp(Ip.ZERO)
            .setNextHopInterface(null)
            .setAdministrativeCost(1)
            .setMetric(0L)
            .setTag(1)
            .build();
    StaticRoute route3 =
        StaticRoute.builder()
            .setNetwork(Prefix.create(Ip.parse("2.1.1.0"), PREFIX_LENGTH))
            .setNextHopIp(Ip.ZERO)
            .setNextHopInterface(null)
            .setAdministrativeCost(1)
            .setMetric(0L)
            .setTag(1)
            .build();
    _builder.add(route1.getNetwork(), route1);
    _builder.add(route2.getNetwork(), route2);

    // Ensure routes are added in order
    RibDelta<AbstractRoute> delta = _builder.build();
    assertThat(delta.getRoutes(), contains(route1, route2));

    // Test that re-adding a route does not change resulting set
    _builder.add(route3.getNetwork(), route3);
    delta = _builder.build();
    assertThat(delta.getRoutes(), contains(route1, route2));
  }

  /** Check duplicate removes get squashed */
  @Test
  public void testBuilderRemoveRoute() {
    StaticRoute route1 =
        StaticRoute.builder()
            .setNetwork(Prefix.create(Ip.parse("1.1.1.0"), PREFIX_LENGTH))
            .setNextHopIp(Ip.ZERO)
            .setNextHopInterface(null)
            .setAdministrativeCost(1)
            .setMetric(0L)
            .setTag(1)
            .build();
    // Route 2 & 3 should be equal
    StaticRoute route2 =
        StaticRoute.builder()
            .setNetwork(Prefix.create(Ip.parse("2.1.1.0"), PREFIX_LENGTH))
            .setNextHopIp(Ip.ZERO)
            .setNextHopInterface(null)
            .setAdministrativeCost(1)
            .setMetric(0L)
            .setTag(1)
            .build();
    StaticRoute route3 =
        StaticRoute.builder()
            .setNetwork(Prefix.create(Ip.parse("2.1.1.0"), PREFIX_LENGTH))
            .setNextHopIp(Ip.ZERO)
            .setNextHopInterface(null)
            .setAdministrativeCost(1)
            .setMetric(0L)
            .setTag(1)
            .build();
    _builder.remove(route1.getNetwork(), route1, Reason.WITHDRAW);
    _builder.remove(route2.getNetwork(), route2, Reason.WITHDRAW);

    // Ensure routes are added in order
    RibDelta<AbstractRoute> delta = _builder.build();
    assertThat(delta.getRoutes(), contains(route1, route2));

    // Test that re-removing a route does not change resulting set
    _builder.remove(route3.getNetwork(), route3, Reason.WITHDRAW);
    delta = _builder.build();
    assertThat(delta.getRoutes(), contains(route1, route2));
  }

  /** Test that deltas are chained correctly using the {@link RibDelta.Builder#from} function */
  @Test
  public void testChainDeltas() {
    BgpRib rib =
        new BgpRib(
            null,
            null,
            BgpTieBreaker.CLUSTER_LIST_LENGTH,
            null,
            MultipathEquivalentAsPathMatchMode.EXACT_PATH);
    BgpRoute.Builder routeBuilder = new BgpRoute.Builder();
    routeBuilder
        .setNetwork(Prefix.create(Ip.parse("1.1.1.1"), Prefix.MAX_PREFIX_LENGTH))
        .setProtocol(RoutingProtocol.IBGP)
        .setOriginType(OriginType.IGP)
        .setOriginatorIp(Ip.parse("7.7.7.7"))
        .setReceivedFromIp(Ip.parse("7.7.7.7"))
        .build();
    BgpRoute oldGoodRoute = routeBuilder.build();
    // Better preference, kicks out oldGoodRoute
    routeBuilder.setLocalPreference(oldGoodRoute.getLocalPreference() + 1);
    BgpRoute newGoodRoute = routeBuilder.build();

    RibDelta.Builder<BgpRoute> builder = RibDelta.builder();
    builder.from(rib.mergeRouteGetDelta(oldGoodRoute), rib::getNetwork);
    builder.from(rib.mergeRouteGetDelta(newGoodRoute), rib::getNetwork);

    RibDelta<BgpRoute> delta = builder.build();
    assertThat(delta.getActions(), hasSize(2));
    // Route withdrawn
    assertThat(
        delta.getActions().get(0), equalTo(new RouteAdvertisement<>(oldGoodRoute, Reason.REPLACE)));
    // Route added
    assertThat(delta.getActions().get(1), equalTo(new RouteAdvertisement<>(newGoodRoute)));
  }

  /** Test that the routes are exact route matches are removed from the RIB by default */
  @Test
  public void testImportRibExactRemoval() {
    BgpRib rib =
        new BgpRib(
            null,
            null,
            BgpTieBreaker.ROUTER_ID,
            null,
            MultipathEquivalentAsPathMatchMode.EXACT_PATH);
    BgpRoute r1 =
        new BgpRoute.Builder()
            .setNetwork(Prefix.create(Ip.parse("1.1.1.1"), Prefix.MAX_PREFIX_LENGTH))
            .setProtocol(RoutingProtocol.IBGP)
            .setOriginType(OriginType.IGP)
            .setOriginatorIp(Ip.parse("7.7.7.7"))
            .setReceivedFromIp(Ip.parse("7.7.7.7"))
            .build();
    BgpRoute r2 =
        new BgpRoute.Builder()
            .setNetwork(Prefix.create(Ip.parse("1.1.1.1"), Prefix.MAX_PREFIX_LENGTH))
            .setProtocol(RoutingProtocol.BGP)
            .setOriginType(OriginType.IGP)
            .setOriginatorIp(Ip.parse("7.7.7.7"))
            .setReceivedFromIp(Ip.parse("7.7.7.7"))
            .build();

    // Setup
    rib.mergeRoute(r1);
    RibDelta<BgpRoute> delta =
        RibDelta.<BgpRoute>builder()
            .add(r2.getNetwork(), r2)
            .remove(r1.getNetwork(), r1, Reason.WITHDRAW)
            .build();
    // Test
    RibDelta.importRibDelta(rib, delta);
    // r1 remains due to different protocol
    assertThat(rib.getRoutes(), contains(r2));
  }
}
