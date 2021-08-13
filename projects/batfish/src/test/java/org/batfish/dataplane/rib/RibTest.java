package org.batfish.dataplane.rib;

import static org.batfish.dataplane.ibdp.TestUtils.annotateRoute;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.AnnotatedRoute;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.ConnectedRoute;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.OspfIntraAreaRoute;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.ResolutionRestriction;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.route.nh.NextHopIp;
import org.junit.Test;

/** Tests of {@link Rib} */
public class RibTest {
  @Test
  public void testCreatedEmpty() {
    assertThat(new Rib().getRoutes(), empty());
  }

  @Test
  public void testNonRoutingIsNotInstalled() {
    Rib rib = new Rib();
    AnnotatedRoute<AbstractRoute> route =
        annotateRoute(
            StaticRoute.testBuilder()
                .setNextHopInterface("foo")
                .setNetwork(Prefix.ZERO)
                .setAdministrativeCost(1)
                .setNonRouting(true)
                .build());

    assertThat(rib.mergeRouteGetDelta(route), equalTo(RibDelta.empty()));
    assertThat(rib.mergeRoute(route), equalTo(false));
  }

  @Test
  public void testComparePreferenceAdmin() {
    Rib rib = new Rib();
    // Identical routes, different admin distance.
    StaticRoute.Builder sb =
        StaticRoute.testBuilder().setNextHopInterface("foo").setNetwork(Prefix.ZERO);
    AbstractRoute route1 = sb.setAdministrativeCost(100).build();
    AbstractRoute route2 = sb.setAdministrativeCost(101).build();

    assertThat(rib.comparePreference(annotateRoute(route1), annotateRoute(route2)), greaterThan(0));
    assertThat(rib.comparePreference(annotateRoute(route2), annotateRoute(route1)), lessThan(0));
  }

  @Test
  public void testComparePreferenceAdminEqual() {
    Rib rib = new Rib();
    // Identical routes should be equally preferred
    StaticRoute.Builder sb =
        StaticRoute.testBuilder()
            .setNextHopInterface("foo")
            .setNetwork(Prefix.ZERO)
            .setAdministrativeCost(100);
    AbstractRoute route1 = sb.build();
    AbstractRoute route2 = sb.build();

    assertThat(rib.comparePreference(annotateRoute(route1), annotateRoute(route2)), equalTo(0));
  }

  @Test
  public void testBackup() {
    Prefix prefix = Prefix.strict("1.0.0.0/31");
    Ip ip = Ip.parse("1.0.0.0");
    Rib rib = new Rib();
    AnnotatedRoute<AbstractRoute> r1 =
        annotateRoute(
            OspfIntraAreaRoute.builder()
                .setNetwork(prefix)
                .setArea(0L)
                .setNextHopInterface("foo")
                .setAdmin(110)
                .setMetric(1L)
                .setNextHopIp(Ip.parse("2.0.0.1"))
                .build());
    AnnotatedRoute<AbstractRoute> r2 =
        annotateRoute(
            Bgpv4Route.testBuilder()
                .setAdmin(20)
                .setNetwork(prefix)
                .setNextHopInterface("bar")
                .setOriginatorIp(Ip.parse("1.1.1.1"))
                .setOriginType(OriginType.IGP)
                .setProtocol(RoutingProtocol.BGP)
                .build());
    rib.mergeRoute(r1);
    assertThat(rib.longestPrefixMatch(ip, ResolutionRestriction.alwaysTrue()), contains(r1));
    rib.mergeRoute(r2);
    assertThat(rib.longestPrefixMatch(ip, ResolutionRestriction.alwaysTrue()), contains(r2));
    rib.removeRoute(r2);
    assertThat(rib.longestPrefixMatch(ip, ResolutionRestriction.alwaysTrue()), contains(r1));
  }

  @Test
  public void testEnforceResolvabilityMergeResolvableRoute() {
    Rib rib = new Rib(ResolutionRestriction.alwaysTrue());
    AnnotatedRoute<AbstractRoute> activatingRoute =
        annotateRoute(new ConnectedRoute(Prefix.strict("10.0.0.0/31"), "foo"));
    AnnotatedRoute<AbstractRoute> nhipRoute =
        annotateRoute(
            StaticRoute.testBuilder()
                .setNetwork(Prefix.strict("10.0.1.0/24"))
                .setNextHop(NextHopIp.of(Ip.parse("10.0.0.1")))
                .build());
    rib.mergeRoute(activatingRoute);

    // NHIP route should be active on merge because it has a resolution path.
    assertThat(
        rib.mergeRouteGetDelta(nhipRoute),
        equalTo(RibDelta.of(RouteAdvertisement.adding(nhipRoute))));
  }

  @Test
  public void testEnforceResolvabilityActivateResolvableRoute() {
    Rib rib = new Rib(ResolutionRestriction.alwaysTrue());
    AnnotatedRoute<AbstractRoute> activatingRoute =
        annotateRoute(new ConnectedRoute(Prefix.strict("10.0.0.0/31"), "foo"));
    AnnotatedRoute<AbstractRoute> nhipRoute =
        annotateRoute(
            StaticRoute.testBuilder()
                .setNetwork(Prefix.strict("10.0.1.0/24"))
                .setNextHop(NextHopIp.of(Ip.parse("10.0.0.1")))
                .build());
    // NHIP route should be inactive on merge because it has no resolution path.
    assertThat(rib.mergeRouteGetDelta(nhipRoute), equalTo(RibDelta.empty()));

    // Both activating and NHIP route should be added on merge of activating route.
    assertThat(
        rib.mergeRouteGetDelta(activatingRoute)
            .getActions()
            .collect(ImmutableList.toImmutableList()),
        containsInAnyOrder(
            RouteAdvertisement.adding(nhipRoute), RouteAdvertisement.adding(activatingRoute)));
  }

  @Test
  public void testEnforceResolvabilityActivateRoutesCascade() {
    Rib rib = new Rib(ResolutionRestriction.alwaysTrue());
    AnnotatedRoute<AbstractRoute> nhipRoute1 =
        annotateRoute(
            StaticRoute.testBuilder()
                .setRecursive(true)
                .setNetwork(Prefix.strict("1.1.1.1/32"))
                .setNextHop(NextHopIp.of(Ip.parse("2.2.2.2")))
                .build());
    AnnotatedRoute<AbstractRoute> nhipRoute2 =
        annotateRoute(
            StaticRoute.testBuilder()
                .setRecursive(true)
                .setNetwork(Prefix.strict("2.2.2.2/32"))
                .setNextHop(NextHopIp.of(Ip.parse("3.3.3.3")))
                .build());
    AnnotatedRoute<AbstractRoute> activatingRoute =
        annotateRoute(new ConnectedRoute(Prefix.strict("3.3.3.2/31"), "foo"));
    // NHIP routes should be inactive on merge because they do not have full resolution path.
    assertThat(rib.mergeRouteGetDelta(nhipRoute1), equalTo(RibDelta.empty()));
    assertThat(rib.mergeRouteGetDelta(nhipRoute2), equalTo(RibDelta.empty()));

    // Activating and NHIP routes should be added on merge of activating route.
    assertThat(
        rib.mergeRouteGetDelta(activatingRoute)
            .getActions()
            .collect(ImmutableList.toImmutableList()),
        containsInAnyOrder(
            RouteAdvertisement.adding(nhipRoute1),
            RouteAdvertisement.adding(nhipRoute2),
            RouteAdvertisement.adding(activatingRoute)));
  }

  @Test
  public void testEnforceResolvabilityDeactivateRoutesCascade() {
    Rib rib = new Rib(ResolutionRestriction.alwaysTrue());
    AnnotatedRoute<AbstractRoute> nhipRoute1 =
        annotateRoute(
            StaticRoute.testBuilder()
                .setRecursive(true)
                .setNetwork(Prefix.strict("1.1.1.1/32"))
                .setNextHop(NextHopIp.of(Ip.parse("2.2.2.2")))
                .build());
    AnnotatedRoute<AbstractRoute> nhipRoute2 =
        annotateRoute(
            StaticRoute.testBuilder()
                .setRecursive(true)
                .setNetwork(Prefix.strict("2.2.2.2/32"))
                .setNextHop(NextHopIp.of(Ip.parse("3.3.3.3")))
                .build());
    AnnotatedRoute<AbstractRoute> activatingRoute =
        annotateRoute(new ConnectedRoute(Prefix.strict("3.3.3.2/31"), "foo"));
    rib.mergeRoute(activatingRoute);

    // NHIP routes should be active on merge because they have full resolution path.
    assertThat(
        rib.mergeRouteGetDelta(nhipRoute2),
        equalTo(RibDelta.of(RouteAdvertisement.adding(nhipRoute2))));
    assertThat(
        rib.mergeRouteGetDelta(nhipRoute1),
        equalTo(RibDelta.of(RouteAdvertisement.adding(nhipRoute1))));

    // Activating and NHIP routes should be removed on withdrawal of activating route.
    assertThat(
        rib.removeRouteGetDelta(activatingRoute)
            .getActions()
            .collect(ImmutableList.toImmutableList()),
        containsInAnyOrder(
            RouteAdvertisement.withdrawing(nhipRoute1),
            RouteAdvertisement.withdrawing(nhipRoute2),
            RouteAdvertisement.withdrawing(activatingRoute)));
  }

  @Test
  public void testEnforceResolvabilitySimpleLoop() {
    Rib rib = new Rib(ResolutionRestriction.alwaysTrue());
    AnnotatedRoute<AbstractRoute> activatingRoute =
        annotateRoute(new ConnectedRoute(Prefix.strict("1.0.0.0/8"), "foo"));
    AnnotatedRoute<AbstractRoute> nhipRoute =
        annotateRoute(
            StaticRoute.testBuilder()
                .setRecursive(true)
                .setNetwork(Prefix.strict("2.0.0.0/8"))
                .setNextHop(NextHopIp.of(Ip.parse("1.0.0.1")))
                .build());
    AnnotatedRoute<AbstractRoute> loopingRoute =
        annotateRoute(
            StaticRoute.testBuilder()
                .setRecursive(true)
                .setNetwork(Prefix.strict("1.0.0.0/16"))
                .setNextHop(NextHopIp.of(Ip.parse("2.0.0.2")))
                .build());
    rib.mergeRoute(activatingRoute);

    // First NHIP route should be active on merge because it has full resolution path.
    assertThat(
        rib.mergeRouteGetDelta(nhipRoute),
        equalTo(RibDelta.of(RouteAdvertisement.adding(nhipRoute))));

    // Looping route should not be activated because it induces a loop. No other routes should be
    // affected.
    assertThat(rib.mergeRouteGetDelta(loopingRoute), equalTo(RibDelta.empty()));
  }

  @Test
  public void testEnforceResolvabilityLargeLoop() {
    Rib rib = new Rib(ResolutionRestriction.alwaysTrue());
    AnnotatedRoute<AbstractRoute> activatingRoute =
        annotateRoute(new ConnectedRoute(Prefix.strict("1.0.0.0/24"), "foo"));
    AnnotatedRoute<AbstractRoute> nhipRoute1 =
        annotateRoute(
            StaticRoute.testBuilder()
                .setRecursive(true)
                .setNetwork(Prefix.strict("1.0.0.0/16"))
                .setNextHop(NextHopIp.of(Ip.parse("2.0.0.1")))
                .build());
    AnnotatedRoute<AbstractRoute> nhipRoute2 =
        annotateRoute(
            StaticRoute.testBuilder()
                .setRecursive(true)
                .setNetwork(Prefix.strict("2.0.0.0/16"))
                .setNextHop(NextHopIp.of(Ip.parse("3.0.0.1")))
                .build());
    AnnotatedRoute<AbstractRoute> nhipRoute3 =
        annotateRoute(
            StaticRoute.testBuilder()
                .setRecursive(true)
                .setNetwork(Prefix.strict("3.0.0.0/16"))
                .setNextHop(NextHopIp.of(Ip.parse("1.0.0.1")))
                .build());
    rib.mergeRoute(activatingRoute);
    rib.mergeRoute(nhipRoute1);
    rib.mergeRoute(nhipRoute2);
    rib.mergeRoute(nhipRoute3);

    // All routes should be removed due to loop created on removal of activating route
    assertThat(
        rib.removeRouteGetDelta(activatingRoute)
            .getActions()
            .collect(ImmutableList.toImmutableList()),
        containsInAnyOrder(
            RouteAdvertisement.withdrawing(activatingRoute),
            RouteAdvertisement.withdrawing(nhipRoute1),
            RouteAdvertisement.withdrawing(nhipRoute2),
            RouteAdvertisement.withdrawing(nhipRoute3)));

    // All route should be re-added when activating route is re-merged, breaking the loop.
    assertThat(
        rib.mergeRouteGetDelta(activatingRoute)
            .getActions()
            .collect(ImmutableList.toImmutableList()),
        containsInAnyOrder(
            RouteAdvertisement.adding(activatingRoute),
            RouteAdvertisement.adding(nhipRoute1),
            RouteAdvertisement.adding(nhipRoute2),
            RouteAdvertisement.adding(nhipRoute3)));
  }

  @Test
  public void testNoEnforceResolvabilityMergeOwnNextHopRoute() {
    Rib rib = new Rib(null);
    AnnotatedRoute<AbstractRoute> ownNextHopRoute =
        annotateRoute(
            StaticRoute.testBuilder()
                .setNetwork(Prefix.strict("10.0.0.0/24"))
                .setNextHop(NextHopIp.of(Ip.parse("10.0.0.1")))
                .build());

    // Since own next hop check is disabled, route should be added.
    assertThat(
        rib.mergeRouteGetDelta(ownNextHopRoute),
        equalTo(RibDelta.of(RouteAdvertisement.adding(ownNextHopRoute))));
  }

  @Test
  public void testNoEnforceResolvabilityPreserveOwnNextHopRoute() {
    Rib rib = new Rib(null);
    AnnotatedRoute<AbstractRoute> ownNextHopRoute =
        annotateRoute(
            StaticRoute.testBuilder()
                .setNetwork(Prefix.strict("10.0.0.0/24"))
                .setNextHop(NextHopIp.of(Ip.parse("10.0.0.1")))
                .build());
    AnnotatedRoute<AbstractRoute> moreSpecificRoute =
        annotateRoute(new ConnectedRoute(Prefix.strict("10.0.0.0/31"), "i1"));

    // Since own next hop check is disabled, ownNextHopRoute should remain active when more specific
    // route is removed.
    rib.mergeRoute(moreSpecificRoute);
    rib.mergeRoute(ownNextHopRoute);
    assertThat(
        rib.removeRouteGetDelta(moreSpecificRoute),
        equalTo(RibDelta.of(RouteAdvertisement.withdrawing(moreSpecificRoute))));
  }

  @Test
  public void testEnforceResolvabilityMergeInvalidOwnNextHopRoute() {
    Rib rib = new Rib(ResolutionRestriction.alwaysTrue());
    AnnotatedRoute<AbstractRoute> ownNextHopRoute =
        annotateRoute(
            StaticRoute.testBuilder()
                .setNetwork(Prefix.strict("10.0.0.0/24"))
                .setNextHop(NextHopIp.of(Ip.parse("10.0.0.1")))
                .build());

    // Route should not be added since it resolves its own next hop.
    assertThat(rib.mergeRouteGetDelta(ownNextHopRoute), equalTo(RibDelta.empty()));
  }

  @Test
  public void testEnforceResolvabilityMergeValidOwnNextHopRoute() {
    Rib rib = new Rib(ResolutionRestriction.alwaysTrue());
    AnnotatedRoute<AbstractRoute> ownNextHopRoute =
        annotateRoute(
            StaticRoute.testBuilder()
                .setNetwork(Prefix.strict("10.0.0.0/24"))
                .setNextHop(NextHopIp.of(Ip.parse("10.0.0.1")))
                .build());
    rib.mergeRoute(annotateRoute(new ConnectedRoute(Prefix.strict("10.0.0.0/31"), "i1")));

    // Route should be added since there is a more specific route that resolves its next hop.
    assertThat(
        rib.mergeRouteGetDelta(ownNextHopRoute),
        equalTo(RibDelta.of(RouteAdvertisement.adding(ownNextHopRoute))));
  }

  @Test
  public void testEnforceResolvabilityActivateOwnNextHopRoute() {
    Rib rib = new Rib(ResolutionRestriction.alwaysTrue());
    AnnotatedRoute<AbstractRoute> ownNextHopRoute =
        annotateRoute(
            StaticRoute.testBuilder()
                .setNetwork(Prefix.strict("10.0.0.0/24"))
                .setNextHop(NextHopIp.of(Ip.parse("10.0.0.1")))
                .build());
    AnnotatedRoute<AbstractRoute> activatingRoute =
        annotateRoute(new ConnectedRoute(Prefix.strict("10.0.0.0/31"), "i1"));

    rib.mergeRoute(ownNextHopRoute);

    // ownNextHopRoute should be activated by activatingRoute.
    assertThat(
        rib.mergeRouteGetDelta(activatingRoute)
            .getActions()
            .collect(ImmutableList.toImmutableList()),
        containsInAnyOrder(
            RouteAdvertisement.adding(activatingRoute),
            RouteAdvertisement.adding(ownNextHopRoute)));
  }

  @Test
  public void testEnforceResolvabilityActivateOwnNextHopRouteCascade() {
    Rib rib = new Rib(ResolutionRestriction.alwaysTrue());
    AnnotatedRoute<AbstractRoute> moreSpecificOwnNextHopRoute =
        annotateRoute(
            StaticRoute.testBuilder()
                .setNetwork(Prefix.strict("10.0.1.0/24"))
                .setNextHop(NextHopIp.of(Ip.parse("10.0.1.1")))
                .build());
    AnnotatedRoute<AbstractRoute> lessSpecificOwnNextHopRoute =
        annotateRoute(
            StaticRoute.testBuilder()
                .setNetwork(Prefix.strict("10.0.0.0/16"))
                .setNextHop(NextHopIp.of(Ip.parse("10.0.1.2")))
                .build());
    AnnotatedRoute<AbstractRoute> activatingRoute =
        annotateRoute(new ConnectedRoute(Prefix.strict("10.0.1.1/32"), "lo"));

    rib.mergeRoute(moreSpecificOwnNextHopRoute);
    rib.mergeRoute(lessSpecificOwnNextHopRoute);

    // moreSpecificOwnNextHopRoute should be activated by activatingRoute, and the former should
    // activate lessSpecificOwnNextHopRoute.
    assertThat(
        rib.mergeRouteGetDelta(activatingRoute)
            .getActions()
            .collect(ImmutableList.toImmutableList()),
        containsInAnyOrder(
            RouteAdvertisement.adding(activatingRoute),
            RouteAdvertisement.adding(lessSpecificOwnNextHopRoute),
            RouteAdvertisement.adding(moreSpecificOwnNextHopRoute)));
  }

  @Test
  public void testEnforceResolvabilityRemoveActiveOwnNextHopRoute() {
    Rib rib = new Rib(ResolutionRestriction.alwaysTrue());
    AnnotatedRoute<AbstractRoute> ownNextHopRoute =
        annotateRoute(
            StaticRoute.testBuilder()
                .setNetwork(Prefix.strict("10.0.0.0/24"))
                .setNextHop(NextHopIp.of(Ip.parse("10.0.0.1")))
                .build());
    AnnotatedRoute<AbstractRoute> activatingRoute =
        annotateRoute(new ConnectedRoute(Prefix.strict("10.0.0.1/32"), "lo"));

    rib.mergeRoute(activatingRoute);
    rib.mergeRoute(ownNextHopRoute);

    // Removing active ownNextHopRoute should result in withdrawal.
    assertThat(
        rib.removeRouteGetDelta(ownNextHopRoute)
            .getActions()
            .collect(ImmutableList.toImmutableList()),
        contains(RouteAdvertisement.withdrawing(ownNextHopRoute)));
  }

  @Test
  public void testEnforceResolvabilityRemoveInactiveOwnNextHopRoute() {
    Rib rib = new Rib(ResolutionRestriction.alwaysTrue());
    AnnotatedRoute<AbstractRoute> ownNextHopRoute =
        annotateRoute(
            StaticRoute.testBuilder()
                .setNetwork(Prefix.strict("10.0.0.0/24"))
                .setNextHop(NextHopIp.of(Ip.parse("10.0.0.1")))
                .build());

    rib.mergeRoute(ownNextHopRoute);

    // Removing inactive ownNextHopRoute should not affect the RIB.
    assertThat(rib.removeRouteGetDelta(ownNextHopRoute), equalTo(RibDelta.empty()));

    // ownNextHopRoute should not be activated when an activator is added, because
    // it has been completely removed from the RIB.
    AnnotatedRoute<AbstractRoute> activatingRoute =
        annotateRoute(new ConnectedRoute(Prefix.strict("10.0.0.0/31"), "foo"));
    assertThat(
        rib.mergeRouteGetDelta(activatingRoute),
        equalTo(RibDelta.of(RouteAdvertisement.adding(activatingRoute))));
  }

  @Test
  public void testEnforceResolvabilityRemoveRedundantActivatingRoutes() {
    Rib rib = new Rib(ResolutionRestriction.alwaysTrue());
    AnnotatedRoute<AbstractRoute> moreSpecificActivatingRoute =
        annotateRoute(new ConnectedRoute(Prefix.strict("10.0.0.1/32"), "i1"));
    AnnotatedRoute<AbstractRoute> lessSpecificActivatingRoute =
        annotateRoute(new ConnectedRoute(Prefix.strict("10.0.0.0/24"), "i2"));
    AnnotatedRoute<AbstractRoute> ownNextHopRoute =
        annotateRoute(
            StaticRoute.testBuilder()
                .setNetwork(Prefix.strict("10.0.0.0/16"))
                .setNextHop(NextHopIp.of(Ip.parse("10.0.0.1")))
                .build());

    rib.mergeRoute(moreSpecificActivatingRoute);
    rib.mergeRoute(lessSpecificActivatingRoute);
    rib.mergeRoute(ownNextHopRoute);

    // Removing moreSpecificActivatingRoute should not result in removal of ownNextHopRoute since
    // the lessSpecificActivatingRoute remains.
    assertThat(
        rib.removeRouteGetDelta(moreSpecificActivatingRoute),
        equalTo(RibDelta.of(RouteAdvertisement.withdrawing(moreSpecificActivatingRoute))));

    // Removing lessSpecificActivatingRoute should result in removal of ownNextHopRoute since
    // no activating route remains.
    assertThat(
        rib.removeRouteGetDelta(lessSpecificActivatingRoute)
            .getActions()
            .collect(ImmutableList.toImmutableList()),
        containsInAnyOrder(
            RouteAdvertisement.withdrawing(lessSpecificActivatingRoute),
            RouteAdvertisement.withdrawing(ownNextHopRoute)));
  }

  @Test
  public void testEnforceResolvabilityRemoveActivatingRouteCascade() {
    Rib rib = new Rib(ResolutionRestriction.alwaysTrue());
    AnnotatedRoute<AbstractRoute> activatingRoute =
        annotateRoute(new ConnectedRoute(Prefix.strict("10.0.1.1/32"), "lo"));
    AnnotatedRoute<AbstractRoute> moreSpecificOwnNextHopRoute =
        annotateRoute(
            StaticRoute.testBuilder()
                .setNetwork(Prefix.strict("10.0.1.0/24"))
                .setNextHop(NextHopIp.of(Ip.parse("10.0.1.1")))
                .build());
    AnnotatedRoute<AbstractRoute> lessSpecificOwnNextHopRoute =
        annotateRoute(
            StaticRoute.testBuilder()
                .setNetwork(Prefix.strict("10.0.0.0/16"))
                .setNextHop(NextHopIp.of(Ip.parse("10.0.1.2")))
                .build());

    rib.mergeRoute(activatingRoute);
    rib.mergeRoute(moreSpecificOwnNextHopRoute);
    rib.mergeRoute(lessSpecificOwnNextHopRoute);

    // Removing activatingRoute should deactivate moreSpecificOwnNextHopRoute, and deactivating the
    // latter should deactivate lessSpecificOwnNextHopRoute.
    assertThat(
        rib.removeRouteGetDelta(activatingRoute)
            .getActions()
            .collect(ImmutableList.toImmutableList()),
        containsInAnyOrder(
            RouteAdvertisement.withdrawing(activatingRoute),
            RouteAdvertisement.withdrawing(lessSpecificOwnNextHopRoute),
            RouteAdvertisement.withdrawing(moreSpecificOwnNextHopRoute)));
  }

  @Test
  public void testEnforceResolvabilityStaticNonrecursiveRouteCannotLoop() {
    Rib rib = new Rib(ResolutionRestriction.alwaysTrue());
    // would loop if non-recursive route were allowed to use it for resolution
    AnnotatedRoute<AbstractRoute> notLoopingRoute =
        annotateRoute(
            StaticRoute.testBuilder()
                .setNetwork(Prefix.strict("2.0.0.0/24"))
                .setNextHop(NextHopIp.of(Ip.parse("1.0.0.1")))
                .setRecursive(false)
                .build());
    AnnotatedRoute<AbstractRoute> connectedRoute1 =
        annotateRoute(new ConnectedRoute(Prefix.strict("1.0.0.0/16"), "foo"));
    AnnotatedRoute<AbstractRoute> connectedRoute2 =
        annotateRoute(new ConnectedRoute(Prefix.strict("2.0.0.0/16"), "bar"));
    AnnotatedRoute<AbstractRoute> staticNonrecursiveRoute =
        annotateRoute(
            StaticRoute.testBuilder()
                .setNetwork(Prefix.strict("1.0.0.0/24"))
                .setNextHop(NextHopIp.of(Ip.parse("2.0.0.1")))
                .setRecursive(false)
                .build());
    rib.mergeRoute(connectedRoute1);
    rib.mergeRoute(connectedRoute2);
    rib.mergeRoute(notLoopingRoute);

    // Route should be added just fine since it will only resolve via the connected route.
    assertThat(
        rib.mergeRouteGetDelta(staticNonrecursiveRoute),
        equalTo(RibDelta.of(RouteAdvertisement.adding(staticNonrecursiveRoute))));
  }

  @Test
  public void testEnforceResolvabilityRestrictedUnresolvable() {
    // A route forbidden by resolution restriction can still be prevented from being installed
    // if it cannot resolve.
    Rib rib = new Rib(route -> route.getNetwork().getPrefixLength() != 24);
    AnnotatedRoute<AbstractRoute> restrictedRoute =
        annotateRoute(
            StaticRoute.testBuilder()
                .setNetwork(Prefix.strict("1.0.0.0/24"))
                .setNextHop(NextHopIp.of(Ip.parse("2.0.0.1")))
                .setRecursive(true)
                .build());

    assertThat(rib.mergeRouteGetDelta(restrictedRoute), equalTo(RibDelta.empty()));
  }

  @Test
  public void testEnforceResolvabilityRestrictedRouteNoLoopOwnNextHop() {
    // A route forbidden by resolution restriction cannot form a loop, even if it contains its own
    // next hop.
    Rib rib = new Rib(route -> route.getNetwork().getPrefixLength() != 24);
    // would loop if non-recursive route were allowed to use it for resolution
    AnnotatedRoute<AbstractRoute> restrictedRoute =
        annotateRoute(
            StaticRoute.testBuilder()
                .setNetwork(Prefix.strict("1.0.0.0/24"))
                .setNextHop(NextHopIp.of(Ip.parse("1.0.0.1")))
                .setRecursive(true)
                .build());
    AnnotatedRoute<AbstractRoute> connectedRoute =
        annotateRoute(new ConnectedRoute(Prefix.strict("1.0.0.0/16"), "foo"));
    rib.mergeRoute(connectedRoute);

    // Route should be added since it resolves via unrestricted connected route.
    assertThat(
        rib.mergeRouteGetDelta(restrictedRoute),
        equalTo(RibDelta.of(RouteAdvertisement.adding(restrictedRoute))));
  }

  @Test
  public void testEnforceResolvabilityConnectedRouteWhitelist() {
    // A connected route may be used to resolve any next hop IP route regardless of resolution
    // restriction.
    Rib rib = new Rib(route -> route.getNetwork().getPrefixLength() != 16);
    AnnotatedRoute<AbstractRoute> recursiveRoute =
        annotateRoute(
            StaticRoute.testBuilder()
                .setNetwork(Prefix.strict("1.0.0.0/24"))
                .setNextHop(NextHopIp.of(Ip.parse("2.0.0.1")))
                .setRecursive(true)
                .build());
    AnnotatedRoute<AbstractRoute> connectedRoute =
        annotateRoute(new ConnectedRoute(Prefix.strict("2.0.0.0/16"), "foo"));
    rib.mergeRoute(connectedRoute);

    // Recursive route can resolve via connected route even if latter is nominally restricted.
    assertThat(
        rib.mergeRouteGetDelta(recursiveRoute),
        equalTo(RibDelta.of(RouteAdvertisement.adding(recursiveRoute))));
  }

  @Test
  public void testEnforceResolvabilityReplaceSucceeds() {
    Rib rib = new Rib(ResolutionRestriction.alwaysTrue());
    AnnotatedRoute<AbstractRoute> activatingRoute =
        annotateRoute(new ConnectedRoute(Prefix.strict("10.0.0.1/32"), "i1"));
    AnnotatedRoute<AbstractRoute> worseRoute =
        annotateRoute(
            StaticRoute.testBuilder()
                .setNetwork(Prefix.strict("10.0.0.2/32"))
                .setNextHop(NextHopIp.of(Ip.parse("10.0.0.1")))
                .setRecursive(true)
                .build());
    AnnotatedRoute<AbstractRoute> betterRoute =
        annotateRoute(new ConnectedRoute(Prefix.strict("10.0.0.2/32"), "i2"));

    rib.mergeRoute(activatingRoute);
    rib.mergeRoute(worseRoute);

    // Replacing worse recursive route with better route should succeed, and worse route should
    // become backup.
    assertThat(
        rib.mergeRouteGetDelta(betterRoute).getActions().collect(ImmutableList.toImmutableList()),
        containsInAnyOrder(
            RouteAdvertisement.replacing(worseRoute), RouteAdvertisement.adding(betterRoute)));
  }

  @Test
  public void testEnforceResolvabilityNonrecursiveRoute() {
    Rib rib = new Rib(ResolutionRestriction.alwaysTrue());
    AnnotatedRoute<AbstractRoute> activatingRoute =
        annotateRoute(new ConnectedRoute(Prefix.strict("10.0.0.1/32"), "i1"));
    AnnotatedRoute<AbstractRoute> nonrecursiveRoute =
        annotateRoute(
            StaticRoute.testBuilder()
                .setNetwork(Prefix.strict("10.0.0.2/32"))
                .setNextHop(NextHopIp.of(Ip.parse("10.0.0.1")))
                .setRecursive(false)
                .build());
    AnnotatedRoute<AbstractRoute> recursiveRoute =
        annotateRoute(
            StaticRoute.testBuilder()
                .setNetwork(Prefix.strict("10.0.0.3/32"))
                .setNextHop(NextHopIp.of(Ip.parse("10.0.0.2")))
                .setRecursive(true)
                .build());

    rib.mergeRoute(activatingRoute);
    rib.mergeRoute(nonrecursiveRoute);
    rib.mergeRoute(recursiveRoute);

    // Removing nonrecursive route deactivates recursive route.
    assertThat(
        rib.removeRouteGetDelta(nonrecursiveRoute)
            .getActions()
            .collect(ImmutableList.toImmutableList()),
        containsInAnyOrder(
            RouteAdvertisement.withdrawing(nonrecursiveRoute),
            RouteAdvertisement.withdrawing(recursiveRoute)));
  }
}
