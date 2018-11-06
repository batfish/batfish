package org.batfish.dataplane.rib;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import com.google.common.collect.ImmutableSortedSet;
import java.util.Collections;
import org.batfish.datamodel.AsPath;
import org.batfish.datamodel.BgpRoute;
import org.batfish.datamodel.BgpTieBreaker;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.MultipathEquivalentAsPathMatchMode;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.StaticRoute;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests of {@link BgpRib} */
@RunWith(JUnit4.class)
public class BgpRibTest {

  private BgpRoute.Builder _rb;
  private BgpRib _multiPathRib;

  @Rule public ExpectedException thrown = ExpectedException.none();

  @Before
  public void setup() {
    _rb = BgpRoute.builder();
    _rb.setAsPath(AsPath.ofSingletonAsSets(1L, 2L))
        .setNetwork(Prefix.parse("10.0.0.0/8"))
        .setProtocol(RoutingProtocol.BGP)
        .setLocalPreference(100)
        .setClusterList(ImmutableSortedSet.of(3L, 4L))
        .setCommunities(ImmutableSortedSet.of(5L, 6L))
        .setOriginatorIp(new Ip("1.1.1.1"))
        .setOriginType(OriginType.IGP)
        .setReceivedFromIp(new Ip("1.1.1.1"))
        .setSrcProtocol(RoutingProtocol.CONNECTED)
        .setWeight(0);

    _multiPathRib =
        new BgpRib(
            null,
            null,
            BgpTieBreaker.ROUTER_ID,
            null,
            MultipathEquivalentAsPathMatchMode.EXACT_PATH);
  }

  @Test
  public void testParameterValidationMaxPaths() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Invalid max-paths value");
    new BgpRib(null, null, BgpTieBreaker.ARRIVAL_ORDER, 0, null);
  }

  @Test
  public void testParameterValidationMatchMode() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Multipath AS-Path-Match-mode must be specified");
    new BgpRib(null, null, BgpTieBreaker.ARRIVAL_ORDER, 2, null);
  }

  @Test
  public void testParameterValidationMatchModeNullMaxPaths() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Multipath AS-Path-Match-mode must be specified");
    new BgpRib(null, null, BgpTieBreaker.ARRIVAL_ORDER, null, null);
  }

  @Test
  public void testIsMultipath() {
    BgpRib rib = new BgpRib(null, null, BgpTieBreaker.ARRIVAL_ORDER, 1, null);
    assertThat("MaxPaths=1, not multipath", !rib.isMultipath());
    rib =
        new BgpRib(
            null,
            null,
            BgpTieBreaker.ARRIVAL_ORDER,
            2,
            MultipathEquivalentAsPathMatchMode.EXACT_PATH);
    assertThat("Maxpaths=2 -> multipath", rib.isMultipath());
    rib =
        new BgpRib(
            null,
            null,
            BgpTieBreaker.ARRIVAL_ORDER,
            null,
            MultipathEquivalentAsPathMatchMode.EXACT_PATH);
    assertThat("Maxpaths=null -> multipath", rib.isMultipath());
  }

  @Test
  public void testWeightPreference() {
    BgpRoute worse = _rb.setWeight(0).build();
    // Higher value is preferred
    BgpRoute best = _rb.setWeight(1).build();
    _multiPathRib.mergeRoute(worse);
    _multiPathRib.mergeRoute(best);

    assertThat(_multiPathRib.getRoutes(), equalTo(Collections.singleton(best)));
    assertThat(_multiPathRib.getBestPathRoutes(), equalTo(Collections.singleton(best)));
  }

  @Test
  public void testLocalPreference() {
    BgpRoute worse = _rb.setLocalPreference(100).build();
    // Higher value is preferred
    BgpRoute best = _rb.setLocalPreference(200).build();
    _multiPathRib.mergeRoute(worse);
    _multiPathRib.mergeRoute(best);

    assertThat(_multiPathRib.getRoutes(), equalTo(Collections.singleton(best)));
    assertThat(_multiPathRib.getBestPathRoutes(), equalTo(Collections.singleton(best)));
  }

  @Test
  public void testAggregatePreference() {
    BgpRoute worse = _rb.setProtocol(RoutingProtocol.BGP).build();
    BgpRoute best = _rb.setProtocol(RoutingProtocol.AGGREGATE).build();
    _multiPathRib.mergeRoute(worse);
    _multiPathRib.mergeRoute(best);

    assertThat(_multiPathRib.getRoutes(), equalTo(Collections.singleton(best)));
    assertThat(_multiPathRib.getBestPathRoutes(), equalTo(Collections.singleton(best)));
  }

  @Test
  public void testAsPathLengthPreference() {
    BgpRoute worse = _rb.setAsPath(AsPath.ofSingletonAsSets(1L, 2L)).build();
    // shorter path is preferred
    BgpRoute best = _rb.setAsPath(AsPath.ofSingletonAsSets(3L)).build();
    _multiPathRib.mergeRoute(worse);
    _multiPathRib.mergeRoute(best);

    assertThat(_multiPathRib.getRoutes(), equalTo(Collections.singleton(best)));
    assertThat(_multiPathRib.getBestPathRoutes(), equalTo(Collections.singleton(best)));
  }

  @Test
  public void testOriginTypePreference() {
    BgpRoute worst = _rb.setOriginType(OriginType.INCOMPLETE).build();
    BgpRoute medium = _rb.setOriginType(OriginType.EGP).build();
    BgpRoute best = _rb.setOriginType(OriginType.IGP).build();

    _multiPathRib.mergeRoute(worst);
    _multiPathRib.mergeRoute(medium);

    assertThat(_multiPathRib.getRoutes(), equalTo(Collections.singleton(medium)));
    assertThat(_multiPathRib.getBestPathRoutes(), equalTo(Collections.singleton(medium)));

    _multiPathRib.mergeRoute(best);
    assertThat(_multiPathRib.getRoutes(), equalTo(Collections.singleton(best)));
    assertThat(_multiPathRib.getBestPathRoutes(), equalTo(Collections.singleton(best)));
  }

  @Test
  public void testEIbgpProtocolPreference() {
    BgpRoute worse = _rb.setProtocol(RoutingProtocol.IBGP).build();
    BgpRoute best = _rb.setProtocol(RoutingProtocol.BGP).build();

    _multiPathRib.mergeRoute(worse);
    _multiPathRib.mergeRoute(best);

    assertThat(_multiPathRib.getRoutes(), equalTo(Collections.singleton(best)));
    assertThat(_multiPathRib.getBestPathRoutes(), equalTo(Collections.singleton(best)));
  }

  @Test
  public void testMedPreference() {
    BgpRoute worse = _rb.setMetric(1).build();
    BgpRoute best = _rb.setMetric(0).build();

    _multiPathRib.mergeRoute(worse);
    _multiPathRib.mergeRoute(best);

    assertThat(_multiPathRib.getRoutes(), equalTo(Collections.singleton(best)));
    assertThat(_multiPathRib.getBestPathRoutes(), equalTo(Collections.singleton(best)));
  }

  @Test
  public void testIgpCostPreference() {
    Rib mainRib = new Rib();
    StaticRoute.Builder sb =
        StaticRoute.builder().setAdministrativeCost(1).setNextHopInterface("eth0");
    mainRib.mergeRoute(sb.setNetwork(Prefix.parse("5.5.5.5/32")).setMetric(1).build());
    mainRib.mergeRoute(sb.setNetwork(Prefix.parse("5.5.5.6/32")).setMetric(2).build());

    BgpRib rib =
        new BgpRib(
            null,
            mainRib,
            BgpTieBreaker.ROUTER_ID,
            null,
            MultipathEquivalentAsPathMatchMode.EXACT_PATH);

    BgpRoute worse = _rb.setNextHopIp(new Ip("5.5.5.6")).build();
    // Lower IGP cost to next hop is better
    BgpRoute best = _rb.setNextHopIp(new Ip("5.5.5.5")).build();

    rib.mergeRoute(worse);
    rib.mergeRoute(best);
    assertThat(rib.getRoutes(), equalTo(Collections.singleton(best)));
    assertThat(rib.getBestPathRoutes(), equalTo(Collections.singleton(best)));
  }

  @Test
  public void testMultipathAsPathModeExactPath() {
    BgpRib rib =
        new BgpRib(
            null,
            null,
            BgpTieBreaker.ROUTER_ID,
            null,
            MultipathEquivalentAsPathMatchMode.EXACT_PATH);

    BgpRoute base = _rb.setAsPath(AsPath.ofSingletonAsSets(1L, 2L)).build();
    BgpRoute candidate1 =
        _rb.setAsPath(AsPath.ofSingletonAsSets(1L, 2L)).setNextHopIp(new Ip("5.5.5.5")).build();
    BgpRoute candidate2 =
        _rb.setAsPath(AsPath.ofSingletonAsSets(1L, 3L)).setNextHopIp(new Ip("5.5.5.6")).build();

    rib.mergeRoute(base);
    assertThat("Exact AS path match, allow merge", rib.mergeRoute(candidate1));
    assertThat("Not an exact AS path match, don't merge", !rib.mergeRoute(candidate2));
    assertThat(rib.getRoutes(), hasSize(2));
    assertThat(rib.getBestPathRoutes(), hasSize(1));
  }

  @Test
  public void testMultipathAsPathModeFirstAs() {
    BgpRib rib =
        new BgpRib(
            null, null, BgpTieBreaker.ROUTER_ID, null, MultipathEquivalentAsPathMatchMode.FIRST_AS);

    BgpRoute base = _rb.setAsPath(AsPath.ofSingletonAsSets(1L, 2L)).build();
    BgpRoute candidate1 =
        _rb.setAsPath(AsPath.ofSingletonAsSets(1L, 3L)).setNextHopIp(new Ip("5.5.5.5")).build();
    BgpRoute candidate2 =
        _rb.setAsPath(AsPath.ofSingletonAsSets(2L, 3L)).setNextHopIp(new Ip("5.5.5.6")).build();

    rib.mergeRoute(base);
    assertThat("Exact AS path match, allow merge", rib.mergeRoute(candidate1));
    assertThat("Not an exact AS path match, don't merge", !rib.mergeRoute(candidate2));
    assertThat(rib.getRoutes(), hasSize(2));
    assertThat(rib.getBestPathRoutes(), hasSize(1));
  }

  @Test
  public void testMultipathDiffOriginator() {
    BgpRoute bestPath = _rb.build();
    _multiPathRib.mergeRoute(_rb.setOriginatorIp(new Ip("2.2.2.2")).build());
    _multiPathRib.mergeRoute(_rb.setOriginatorIp(new Ip("2.2.2.3")).build());
    _multiPathRib.mergeRoute(bestPath);
    assertThat(_multiPathRib.getRoutes(), hasSize(3));
    assertThat(_multiPathRib.getBestPathRoutes(), equalTo(Collections.singleton(bestPath)));
  }

  @Test
  public void testMultipathArrivalOrder() {
    _multiPathRib =
        new BgpRib(
            null,
            null,
            BgpTieBreaker.ARRIVAL_ORDER,
            null,
            MultipathEquivalentAsPathMatchMode.EXACT_PATH);
    BgpRoute best = _rb.build();
    BgpRoute earliest = _rb.setOriginatorIp(new Ip("2.2.2.2")).build();
    _multiPathRib.mergeRoute(earliest);
    _multiPathRib.mergeRoute(_rb.setOriginatorIp(new Ip("2.2.2.3")).build());
    _multiPathRib.mergeRoute(best);

    assertThat(_multiPathRib.getRoutes(), hasSize(3));
    assertThat(_multiPathRib.getBestPathRoutes(), equalTo(Collections.singleton(earliest)));
  }

  @Test
  public void testMultipathDiffClusterList() {
    _rb.setProtocol(RoutingProtocol.IBGP).setClusterList(ImmutableSortedSet.of());
    BgpRoute bestPath = _rb.build();
    _multiPathRib.mergeRoute(bestPath);
    _multiPathRib.mergeRoute(_rb.setClusterList(ImmutableSortedSet.of(11L)).build());
    _multiPathRib.mergeRoute(_rb.setClusterList(ImmutableSortedSet.of(22L, 33L)).build());

    assertThat(_multiPathRib.getRoutes(), hasSize(3));
    assertThat(_multiPathRib.getBestPathRoutes(), equalTo(Collections.singleton(bestPath)));
  }

  @Test
  public void testMultipathDiffNeighbor() {
    BgpRoute bestPath = _rb.build();
    _multiPathRib.mergeRoute(_rb.setReceivedFromIp(new Ip("2.2.2.2")).build());
    _multiPathRib.mergeRoute(_rb.setReceivedFromIp(new Ip("2.2.2.3")).build());
    _multiPathRib.mergeRoute(bestPath);

    assertThat(_multiPathRib.getRoutes(), hasSize(3));
    assertThat(_multiPathRib.getBestPathRoutes(), equalTo(Collections.singleton(bestPath)));
  }

  @Test
  public void testMultipathSamePath() {
    BgpRoute bestPath = _rb.build();
    _multiPathRib.mergeRoute(bestPath);
    _multiPathRib.mergeRoute(bestPath);
    _multiPathRib.mergeRoute(bestPath);

    assertThat(_multiPathRib.getRoutes(), hasSize(1));
    assertThat(_multiPathRib.getBestPathRoutes(), equalTo(Collections.singleton(bestPath)));
  }

  @Test
  public void testMultipathEviction() {
    _multiPathRib.mergeRoute(_rb.setOriginatorIp(new Ip("4.4.4.4")).build());
    _multiPathRib.mergeRoute(_rb.setReceivedFromIp(new Ip("2.2.2.2")).build());
    _multiPathRib.mergeRoute(_rb.setReceivedFromIp(new Ip("2.2.2.3")).build());

    assertThat(_multiPathRib.getRoutes(), hasSize(3));
    assertThat(_multiPathRib.getBestPathRoutes(), hasSize(1));
    BgpRoute bestPath = _rb.setLocalPreference(1000).build();
    _multiPathRib.mergeRoute(bestPath);

    assertThat(_multiPathRib.getRoutes(), equalTo(Collections.singleton(bestPath)));
    assertThat(_multiPathRib.getBestPathRoutes(), equalTo(Collections.singleton(bestPath)));
  }

  @Test
  public void testBestPathsEqualRoutesForBestPathRib() {
    BgpRib bestPathRib = new BgpRib(null, null, BgpTieBreaker.ROUTER_ID, 1, null);
    BgpRoute bestPath = _rb.build();
    bestPathRib.mergeRoute(_rb.setReceivedFromIp(new Ip("2.2.2.2")).build());
    bestPathRib.mergeRoute(_rb.setReceivedFromIp(new Ip("2.2.2.3")).build());
    bestPathRib.mergeRoute(bestPath);

    assertThat(bestPathRib.getRoutes(), equalTo(Collections.singleton(bestPath)));
    assertThat(bestPathRib.getBestPathRoutes(), equalTo(Collections.singleton(bestPath)));
  }

  @Test
  public void testMergeMultiplePrefixes() {
    _multiPathRib.mergeRoute(_rb.build());
    _multiPathRib.mergeRoute(_rb.setNetwork(Prefix.parse("10.1.0.0/16")).build());
    _multiPathRib.mergeRoute(_rb.setNetwork(Prefix.parse("10.1.1.0/24")).build());

    assertThat(_multiPathRib.getRoutes(), hasSize(3));
    assertThat(_multiPathRib.getRoutes(), equalTo(_multiPathRib.getBestPathRoutes()));

    _multiPathRib.mergeRoute(
        _rb.setNetwork(Prefix.parse("10.1.1.0/24")).setOriginatorIp(new Ip("22.22.22.22")).build());
    assertThat(_multiPathRib.getRoutes(), hasSize(4));
    assertThat(_multiPathRib.getBestPathRoutes(), hasSize(3));
  }
}
