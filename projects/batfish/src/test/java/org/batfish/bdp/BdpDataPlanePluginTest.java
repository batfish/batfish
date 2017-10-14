package org.batfish.bdp;

import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isIn;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.batfish.common.BatfishException;
import org.batfish.common.BdpOscillationException;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Route;
import org.batfish.datamodel.SourceNat;
import org.batfish.datamodel.collections.RoutesByVrf;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

/** Tests for {@link BdpDataPlanePlugin}. */
public class BdpDataPlanePluginTest {

  private static String TESTRIGS_PREFIX = "org/batfish/grammar/cisco/testrigs/";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();
  @Rule public ExpectedException _thrown = ExpectedException.none();

  @Test(timeout = 5000)
  public void testComputeFixedPoint() throws IOException {
    SortedMap<String, Configuration> configurations = new TreeMap<>();
    // creating configurations with no vrfs
    configurations.put(
        "h1", BatfishTestUtils.createTestConfiguration("h1", ConfigurationFormat.HOST, "eth0"));
    configurations.put(
        "h2", BatfishTestUtils.createTestConfiguration("h2", ConfigurationFormat.HOST, "e0"));
    Batfish batfish = BatfishTestUtils.getBatfish(configurations, _folder);
    BdpDataPlanePlugin dataPlanePlugin = new BdpDataPlanePlugin();
    dataPlanePlugin.initialize(batfish);

    // Test that compute Data Plane finishes in a finite time
    dataPlanePlugin.computeDataPlane(false);
  }

  private static Flow makeFlow() {
    Flow.Builder builder = new Flow.Builder();
    builder.setSrcIp(new Ip("1.2.3.4"));
    builder.setIngressNode("foo");
    builder.setTag("TEST");
    return builder.build();
  }

  @SuppressWarnings("unused")
  private static IpAccessListLine makeAclLine(LineAction action) {
    IpAccessListLine aclLine = new IpAccessListLine();
    aclLine.setAction(action);
    return aclLine;
  }

  private static IpAccessList makeAcl(String name, LineAction action) {
    IpAccessListLine aclLine = new IpAccessListLine();
    aclLine.setAction(action);
    return new IpAccessList(name, singletonList(aclLine));
  }

  @Test
  public void testApplySourceNatSingleAclMatch() {
    Flow flow = makeFlow();

    SourceNat nat = new SourceNat();
    nat.setAcl(makeAcl("accept", LineAction.ACCEPT));
    nat.setPoolIpFirst(new Ip("4.5.6.7"));

    Flow transformed = BdpDataPlanePlugin.applySourceNat(flow, singletonList(nat));
    assertThat(transformed.getSrcIp(), equalTo(new Ip("4.5.6.7")));
  }

  @Test
  public void testApplySourceNatSingleAclNoMatch() {
    Flow flow = makeFlow();

    SourceNat nat = new SourceNat();
    nat.setAcl(makeAcl("reject", LineAction.REJECT));
    nat.setPoolIpFirst(new Ip("4.5.6.7"));

    Flow transformed = BdpDataPlanePlugin.applySourceNat(flow, singletonList(nat));
    assertThat(transformed, is(flow));
  }

  @Test
  public void testApplySourceNatFirstMatchWins() {
    Flow flow = makeFlow();

    SourceNat nat = new SourceNat();
    nat.setAcl(makeAcl("firstAccept", LineAction.ACCEPT));
    nat.setPoolIpFirst(new Ip("4.5.6.7"));

    SourceNat secondNat = new SourceNat();
    secondNat.setAcl(makeAcl("secondAccept", LineAction.ACCEPT));
    secondNat.setPoolIpFirst(new Ip("4.5.6.8"));

    Flow transformed = BdpDataPlanePlugin.applySourceNat(flow, Lists.newArrayList(nat, secondNat));
    assertThat(transformed.getSrcIp(), equalTo(new Ip("4.5.6.7")));
  }

  @Test
  public void testApplySourceNatLateMatchWins() {
    Flow flow = makeFlow();

    SourceNat nat = new SourceNat();
    nat.setAcl(makeAcl("rejectAll", LineAction.REJECT));
    nat.setPoolIpFirst(new Ip("4.5.6.7"));

    SourceNat secondNat = new SourceNat();
    secondNat.setAcl(makeAcl("acceptAnyway", LineAction.ACCEPT));
    secondNat.setPoolIpFirst(new Ip("4.5.6.8"));

    Flow transformed = BdpDataPlanePlugin.applySourceNat(flow, Lists.newArrayList(nat, secondNat));
    assertThat(transformed.getSrcIp(), equalTo(new Ip("4.5.6.8")));
  }

  @Test
  public void testApplySourceNatInvalidAclThrows() {
    Flow flow = makeFlow();

    SourceNat nat = new SourceNat();
    nat.setAcl(makeAcl("matchAll", LineAction.ACCEPT));

    _thrown.expect(BatfishException.class);
    _thrown.expectMessage("missing NAT address or pool");
    BdpDataPlanePlugin.applySourceNat(flow, singletonList(nat));
  }

  @Test
  public void testBgpOscillation() throws IOException {
    String testrigName = "bgp-oscillation";
    String[] configurationNames = new String[] {"r1", "r2", "r3"};
    Batfish batfish =
        BatfishTestUtils.getBatfishFromTestrigResource(
            TESTRIGS_PREFIX + testrigName, configurationNames, null, null, null, null, _folder);
    BdpDataPlanePlugin dataPlanePlugin = new BdpDataPlanePlugin();
    dataPlanePlugin.initialize(batfish);

    _thrown.expect(BdpOscillationException.class);
    dataPlanePlugin.computeDataPlane(false);
  }

  @Test
  public void testEbgpAcceptSameNeighborID() throws IOException {
    String testrigName = "ebgp-accept-routerid-match";
    String[] configurationNames = new String[] {"r1", "r2", "r3"};
    Batfish batfish =
        BatfishTestUtils.getBatfishFromTestrigResource(
            TESTRIGS_PREFIX + testrigName, configurationNames, null, null, null, null, _folder);
    BdpDataPlanePlugin dataPlanePlugin = new BdpDataPlanePlugin();
    dataPlanePlugin.initialize(batfish);
    dataPlanePlugin.computeDataPlane(false);
    SortedMap<String, SortedMap<String, SortedSet<AbstractRoute>>> routes =
        dataPlanePlugin.getRoutes();

    SortedSet<AbstractRoute> r1Routes = routes.get("r1").get(Configuration.DEFAULT_VRF_NAME);
    SortedSet<AbstractRoute> r3Routes = routes.get("r3").get(Configuration.DEFAULT_VRF_NAME);
    Set<Prefix> r1Prefixes = r1Routes.stream().map(r -> r.getNetwork()).collect(Collectors.toSet());
    Set<Prefix> r3Prefixes = r3Routes.stream().map(r -> r.getNetwork()).collect(Collectors.toSet());
    Prefix r1Loopback0Prefix = new Prefix("1.0.0.1/32");
    Prefix r3Loopback0Prefix = new Prefix("3.0.0.3/32");

    // Ensure that r3loopback was accepted by r1
    assertThat(r3Loopback0Prefix, isIn(r1Prefixes));
    // Check the other direction (r1loopback is accepted by r3)
    assertThat(r1Loopback0Prefix, isIn(r3Prefixes));
  }

  @Test
  public void testIbgpRejectOwnAs() throws IOException {
    String testrigName = "ibgp-reject-own-as";
    String[] configurationNames = new String[] {"r1", "r2a", "r2b"};
    Batfish batfish =
        BatfishTestUtils.getBatfishFromTestrigResource(
            TESTRIGS_PREFIX + testrigName, configurationNames, null, null, null, null, _folder);
    BdpDataPlanePlugin dataPlanePlugin = new BdpDataPlanePlugin();
    dataPlanePlugin.initialize(batfish);
    dataPlanePlugin.computeDataPlane(false);
    SortedMap<String, SortedMap<String, SortedSet<AbstractRoute>>> routes =
        dataPlanePlugin.getRoutes();
    SortedSet<AbstractRoute> r2aRoutes = routes.get("r2a").get(Configuration.DEFAULT_VRF_NAME);
    SortedSet<AbstractRoute> r2bRoutes = routes.get("r2b").get(Configuration.DEFAULT_VRF_NAME);
    Set<Prefix> r2aPrefixes =
        r2aRoutes.stream().map(r -> r.getNetwork()).collect(Collectors.toSet());
    Set<Prefix> r2bPrefixes =
        r2bRoutes.stream().map(r -> r.getNetwork()).collect(Collectors.toSet());
    Prefix r1Loopback0Prefix = new Prefix("1.0.0.1/32");
    Prefix r1Loopback1Prefix = new Prefix("1.0.0.2/32");
    assertTrue(r2aPrefixes.contains(r1Loopback0Prefix));
    assertTrue(r2aPrefixes.contains(r1Loopback1Prefix));
    /*
     * 1.0.0.2/32 should be accepted r2b as a normal iBGP route forwarded from r1.
     */
    assertTrue(r2bPrefixes.contains(r1Loopback1Prefix));
    /*
     * 1.0.0.1/32 should be rejected by r2b since it already contains AS#2 in its AS-path due to
     * r2a prepending 2 in the matching route-map clause.
     */
    assertFalse(r2bPrefixes.contains(r1Loopback0Prefix));
  }

  @Test
  public void testIbgpRejectSameNeighborID() throws IOException {
    String testrigName = "ibgp-reject-routerid-match";
    String[] configurationNames = new String[] {"r1", "r2", "r3", "r4"};
    Batfish batfish =
        BatfishTestUtils.getBatfishFromTestrigResource(
            TESTRIGS_PREFIX + testrigName, configurationNames, null, null, null, null, _folder);
    BdpDataPlanePlugin dataPlanePlugin = new BdpDataPlanePlugin();
    dataPlanePlugin.initialize(batfish);
    dataPlanePlugin.computeDataPlane(false);
    SortedMap<String, SortedMap<String, SortedSet<AbstractRoute>>> routes =
        dataPlanePlugin.getRoutes();

    SortedSet<AbstractRoute> r2Routes = routes.get("r2").get(Configuration.DEFAULT_VRF_NAME);
    SortedSet<AbstractRoute> r3Routes = routes.get("r3").get(Configuration.DEFAULT_VRF_NAME);
    Set<Prefix> r2Prefixes = r2Routes.stream().map(r -> r.getNetwork()).collect(Collectors.toSet());
    Set<Prefix> r3Prefixes = r3Routes.stream().map(r -> r.getNetwork()).collect(Collectors.toSet());
    // 9.9.9.9/32 is the prefix we test with
    Prefix r1AdvertisedPrefix = new Prefix("9.9.9.9/32");

    // Ensure that the prefix is accepted by r2, because router ids are different
    assertThat(r1AdvertisedPrefix, isIn(r2Prefixes));
    // Ensure that the prefix is rejected by r3, because router ids are the same
    assertThat(r1AdvertisedPrefix, not(isIn(r3Prefixes)));
  }

  @Test
  public void testIosRtStaticMatchesBdp() throws IOException {
    String testrigName = "ios-rt-static-ad";
    String[] configurationNames = new String[] {"r1"};
    String[] routingTableNames = new String[] {"r1"};
    Batfish batfish =
        BatfishTestUtils.getBatfishFromTestrigResource(
            TESTRIGS_PREFIX + testrigName,
            configurationNames,
            null,
            null,
            null,
            routingTableNames,
            _folder);
    BdpDataPlanePlugin dataPlanePlugin = new BdpDataPlanePlugin();
    dataPlanePlugin.initialize(batfish);
    dataPlanePlugin.computeDataPlane(false);
    SortedMap<String, RoutesByVrf> environmentRoutes = batfish.loadEnvironmentRoutingTables();
    SortedMap<String, SortedMap<String, SortedSet<AbstractRoute>>> routes =
        dataPlanePlugin.getRoutes();
    Prefix staticRoutePrefix = new Prefix("10.0.0.0/8");
    SortedSet<AbstractRoute> r1BdpRoutes = routes.get("r1").get(Configuration.DEFAULT_VRF_NAME);
    AbstractRoute r1BdpRoute =
        r1BdpRoutes
            .stream()
            .filter(r -> r.getNetwork().equals(staticRoutePrefix))
            .findFirst()
            .get();
    SortedSet<Route> r1EnvironmentRoutes =
        environmentRoutes.get("r1").get(Configuration.DEFAULT_VRF_NAME);
    Route r1EnvironmentRoute =
        r1EnvironmentRoutes
            .stream()
            .filter(r -> r.getNetwork().equals(staticRoutePrefix))
            .findFirst()
            .get();
    assertThat(
        r1BdpRoute.getAdministrativeCost(), equalTo(r1EnvironmentRoute.getAdministrativeCost()));
    assertThat(r1BdpRoute.getMetric(), equalTo(r1EnvironmentRoute.getMetric()));
    assertThat(r1BdpRoute.getProtocol(), equalTo(r1EnvironmentRoute.getProtocol()));
  }
}
