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
import org.batfish.datamodel.ConnectedRoute;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
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
  public void testInstallOwnNextHopMergeOwnNextHopRoute() {
    Rib rib = new Rib(false);
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
  public void testInstallOwnNextHopPreserveOwnNextHopRoute() {
    Rib rib = new Rib(false);
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
  public void testNoInstallOwnNextHopMergeInvalidOwnNextHopRoute() {
    Rib rib = new Rib(true);
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
  public void testNoInstallOwnNextHopMergeValidOwnNextHopRoute() {
    Rib rib = new Rib(true);
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
  public void testNoInstallOwnNextHopActivateOwnNextHopRoute() {
    Rib rib = new Rib(true);
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
  public void testNoInstallOwnNextHopActivateOwnNextHopRouteCascade() {
    Rib rib = new Rib(true);
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
  public void testNoInstallOwnNextHopRemoveActiveOwnNextHopRoute() {
    Rib rib = new Rib(true);
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
  public void testNoInstallOwnNextHopRemoveInactiveOwnNextHopRoute() {
    Rib rib = new Rib(true);
    AnnotatedRoute<AbstractRoute> ownNextHopRoute =
        annotateRoute(
            StaticRoute.testBuilder()
                .setNetwork(Prefix.strict("10.0.0.0/24"))
                .setNextHop(NextHopIp.of(Ip.parse("10.0.0.1")))
                .build());

    rib.mergeRoute(ownNextHopRoute);

    // Removing inactive ownNextHopRoute should not affect the RIB.
    assertThat(rib.removeRouteGetDelta(ownNextHopRoute), equalTo(RibDelta.empty()));
  }

  @Test
  public void testNoInstallOwnNextHopRemoveRedundantActivatingRoutes() {
    Rib rib = new Rib(true);
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
  public void testNoInstallOwnNextHopRemoveActivatingRouteCascade() {
    Rib rib = new Rib(true);
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
}
