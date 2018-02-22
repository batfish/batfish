package org.batfish.bdp;

import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.isIn;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.batfish.common.BatfishException;
import org.batfish.common.BatfishLogger;
import org.batfish.common.BdpOscillationException;
import org.batfish.config.Settings;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.AsPath;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.BgpRoute;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.MultipathEquivalentAsPathMatchMode;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Route;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.SourceNat;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.answers.BdpAnswerElement;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.collections.RoutesByVrf;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.main.TestrigText;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

/** Tests for {@link BdpDataPlanePlugin}. */
public class BdpDataPlanePluginTest {

  private static class BdpOscillationExceptionMatchers {
    private static class HasMessage extends FeatureMatcher<BdpOscillationException, String> {

      public HasMessage(Matcher<? super String> subMatcher) {
        super(subMatcher, "message", "message");
      }

      @Override
      protected String featureValueOf(BdpOscillationException actual) {
        return actual.getMessage();
      }
    }

    public static HasMessage hasMessage(Matcher<? super String> subMatcher) {
      return new HasMessage(subMatcher);
    }
  }

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

    Flow transformed = BdpEngine.applySourceNat(flow, singletonList(nat));
    assertThat(transformed.getSrcIp(), equalTo(new Ip("4.5.6.7")));
  }

  @Test
  public void testApplySourceNatSingleAclNoMatch() {
    Flow flow = makeFlow();

    SourceNat nat = new SourceNat();
    nat.setAcl(makeAcl("reject", LineAction.REJECT));
    nat.setPoolIpFirst(new Ip("4.5.6.7"));

    Flow transformed = BdpEngine.applySourceNat(flow, singletonList(nat));
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

    Flow transformed = BdpEngine.applySourceNat(flow, Lists.newArrayList(nat, secondNat));
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

    Flow transformed = BdpEngine.applySourceNat(flow, Lists.newArrayList(nat, secondNat));
    assertThat(transformed.getSrcIp(), equalTo(new Ip("4.5.6.8")));
  }

  @Test
  public void testApplySourceNatInvalidAclThrows() {
    Flow flow = makeFlow();

    SourceNat nat = new SourceNat();
    nat.setAcl(makeAcl("matchAll", LineAction.ACCEPT));

    _thrown.expect(BatfishException.class);
    _thrown.expectMessage("missing NAT address or pool");
    BdpEngine.applySourceNat(flow, singletonList(nat));
  }

  private void testBgpAsPathMultipathHelper(
      MultipathEquivalentAsPathMatchMode multipathEquivalentAsPathMatchMode,
      boolean primeBestPathInMultipathBgpRib,
      boolean expectRoute2,
      boolean expectRoute3a,
      boolean expectRoute3b,
      boolean expectRoute3c,
      boolean expectRoute3d) {
    /*
     * Properties of the routes
     */
    // Should appear only for path-length match
    List<SortedSet<Integer>> asPath2 = AsPath.ofSingletonAsSets(2, 4, 6).getAsSets();
    // Should appear only for first-as match and path-length match
    List<SortedSet<Integer>> asPath3a = AsPath.ofSingletonAsSets(3, 5, 6).getAsSets();
    // Should never appear
    List<SortedSet<Integer>> asPath3b = AsPath.ofSingletonAsSets(3, 4, 4, 6).getAsSets();
    // Should always appear
    AsPath bestAsPath = AsPath.ofSingletonAsSets(3, 4, 6);
    List<SortedSet<Integer>> asPath3c = bestAsPath.getAsSets();
    List<SortedSet<Integer>> asPath3d = bestAsPath.getAsSets();
    Ip nextHop2 = new Ip("2.0.0.0");
    Ip nextHop3a = new Ip("3.0.0.1");
    Ip nextHop3b = new Ip("3.0.0.2");
    Ip nextHop3c = new Ip("3.0.0.3");
    Ip nextHop3d = new Ip("3.0.0.4");

    /*
     * Common attributes for all routes
     */
    Prefix p = Prefix.ZERO;
    BgpRoute.Builder b =
        new BgpRoute.Builder()
            .setNetwork(p)
            .setProtocol(RoutingProtocol.BGP)
            .setOriginType(OriginType.INCOMPLETE);

    /*
     * Boilerplate virtual-router setup
     */
    String hostname = "r1";
    Configuration c =
        BatfishTestUtils.createTestConfiguration(hostname, ConfigurationFormat.CISCO_IOS);
    BgpProcess proc = new BgpProcess();
    c.getVrfs().computeIfAbsent(Configuration.DEFAULT_VRF_NAME, Vrf::new).setBgpProcess(proc);
    Map<String, Node> nodes = new HashMap<String, Node>();
    Node node = new Node(c);
    nodes.put(hostname, node);
    VirtualRouter vr = new VirtualRouter(Configuration.DEFAULT_VRF_NAME, c);

    /*
     * Instantiate routes
     */
    BgpRoute route2 =
        b.setAsPath(asPath2)
            .setNextHopIp(nextHop2)
            .setOriginatorIp(nextHop2)
            .setReceivedFromIp(nextHop2)
            .build();
    BgpRoute route3a =
        b.setAsPath(asPath3a)
            .setNextHopIp(nextHop3a)
            .setOriginatorIp(nextHop3a)
            .setReceivedFromIp(nextHop3a)
            .build();
    BgpRoute route3b =
        b.setAsPath(asPath3b)
            .setNextHopIp(nextHop3b)
            .setOriginatorIp(nextHop3b)
            .setReceivedFromIp(nextHop3b)
            .build();
    BgpRoute route3c =
        b.setAsPath(asPath3c)
            .setNextHopIp(nextHop3c)
            .setOriginatorIp(nextHop3c)
            .setReceivedFromIp(nextHop3c)
            .build();
    BgpRoute route3d =
        b.setAsPath(asPath3d)
            .setNextHopIp(nextHop3d)
            .setOriginatorIp(nextHop3d)
            .setReceivedFromIp(nextHop3d)
            .build();

    /*
     * Set the as-path match mode prior to instantiating bgp multipath RIB
     */
    proc.setMultipathEquivalentAsPathMatchMode(multipathEquivalentAsPathMatchMode);
    BgpMultipathRib bmr = new BgpMultipathRib(vr);

    /*
     * Prime bgp multipath RIB with best path for the prefix
     */
    if (primeBestPathInMultipathBgpRib) {
      bmr.setBestAsPaths(Collections.singletonMap(p, bestAsPath));
    }

    /*
     * Add routes to multipath RIB.
     */
    bmr.mergeRoute(route2);
    bmr.mergeRoute(route3a);
    bmr.mergeRoute(route3b);
    bmr.mergeRoute(route3c);
    bmr.mergeRoute(route3d);

    /*
     * Initialize the matchers with respect to the output route set
     */
    Set<BgpRoute> postMergeRoutes = bmr.getRoutes();
    Matcher<BgpRoute> present = isIn(postMergeRoutes);
    Matcher<BgpRoute> absent = not(present);

    /*
     * ASSERTIONS:
     * Only the expected routes for the given match mode should be present at end
     */
    assertThat(route2, expectRoute2 ? present : absent);
    assertThat(route3a, expectRoute3a ? present : absent);
    assertThat(route3b, expectRoute3b ? present : absent);
    assertThat(route3c, expectRoute3c ? present : absent);
    assertThat(route3c, expectRoute3d ? present : absent);
  }

  @Test
  public void testBgpAsPathMultipathExactPath() {
    /*
     * Only routes with first-as matching that of best as path should appear in RIB post-merge.
     */
    testBgpAsPathMultipathHelper(
        MultipathEquivalentAsPathMatchMode.EXACT_PATH, true, false, false, false, true, true);
  }

  @Test
  public void testBgpAsPathMultipathFirstAs() {
    /*
     * Only routes with first-as matching that of best as path should appear in RIB post-merge.
     */
    testBgpAsPathMultipathHelper(
        MultipathEquivalentAsPathMatchMode.FIRST_AS, true, false, true, false, true, true);
  }

  @Test
  public void testBgpAsPathMultipathPathLength() {
    /*
     * All routes with same as-path-length as that of best as-path should appear in RIB post-merge.
     */
    testBgpAsPathMultipathHelper(
        MultipathEquivalentAsPathMatchMode.PATH_LENGTH, true, true, true, false, true, true);
  }

  @Test
  public void testBgpAsPathMultipathUnprimed() {
    /*
     * Without priming best as path map, all paths except the longer one should be considered
     * equivalent. Results should be independent of chosen mode.
     */
    for (MultipathEquivalentAsPathMatchMode mode : MultipathEquivalentAsPathMatchMode.values()) {
      testBgpAsPathMultipathHelper(mode, false, true, true, false, true, true);
    }
  }

  @Test
  public void testBgpCompareOriginId() {
    String hostname = "r1";
    Configuration c =
        BatfishTestUtils.createTestConfiguration(hostname, ConfigurationFormat.CISCO_IOS);
    BgpProcess proc = new BgpProcess();
    c.getVrfs().computeIfAbsent(Configuration.DEFAULT_VRF_NAME, Vrf::new).setBgpProcess(proc);
    Map<String, Node> nodes = new HashMap<String, Node>();
    Node node = new Node(c);
    nodes.put(hostname, node);
    VirtualRouter vr = new VirtualRouter(Configuration.DEFAULT_VRF_NAME, c);
    BgpBestPathRib bbr = BgpBestPathRib.initial(vr);
    BgpMultipathRib bmr = new BgpMultipathRib(vr);
    Prefix p = Prefix.ZERO;
    BgpRoute.Builder b = new BgpRoute.Builder().setNetwork(p).setProtocol(RoutingProtocol.IBGP);

    /*
     *  Initialize with different originator ips, which should not affect comparison of routes with
     *  different origin type.
     */
    Map<OriginType, List<BgpRoute>> routesByOriginType = new LinkedHashMap<>();
    for (OriginType originType : OriginType.values()) {
      List<BgpRoute> routes =
          routesByOriginType.computeIfAbsent(originType, o -> new ArrayList<>());
      routes.add(
          b.setOriginatorIp(Ip.ZERO).setReceivedFromIp(Ip.ZERO).setOriginType(originType).build());
      routes.add(
          b.setOriginatorIp(Ip.MAX).setReceivedFromIp(Ip.MAX).setOriginType(originType).build());
    }

    /*
     * Whenever origin type is different, it should be overriding factor in preference.
     */
    for (OriginType o1 : OriginType.values()) {
      List<BgpRoute> lhsList = routesByOriginType.get(o1);
      for (OriginType o2 : OriginType.values()) {
        List<BgpRoute> rhsList = routesByOriginType.get(o2);
        for (BgpRoute lhs : lhsList) {
          for (BgpRoute rhs : rhsList) {
            if (o1.getPreference() > o2.getPreference()) {
              assertThat(bbr.comparePreference(lhs, rhs), greaterThan(0));
              assertThat(bmr.comparePreference(lhs, rhs), greaterThan(0));
            } else if (o1.getPreference() < o2.getPreference()) {
              assertThat(bbr.comparePreference(lhs, rhs), lessThan(0));
              assertThat(bmr.comparePreference(lhs, rhs), lessThan(0));
            }
          }
        }
      }
    }
  }

  @Test
  public void testBgpOscillation() throws IOException {
    String testrigName = "bgp-oscillation";
    List<String> configurationNames = ImmutableList.of("r1", "r2", "r3");

    Batfish batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder()
                .setConfigurationText(TESTRIGS_PREFIX + testrigName, configurationNames)
                .build(),
            _folder);
    batfish.getSettings().setBdpMaxOscillationRecoveryAttempts(0);
    BdpDataPlanePlugin dataPlanePlugin = new BdpDataPlanePlugin();
    dataPlanePlugin.initialize(batfish);

    _thrown.expect(BdpOscillationException.class);
    dataPlanePlugin.computeDataPlane(false);
  }

  @Test
  public void testBgpOscillationPrintingEnoughInfo() throws IOException {
    TestBdpSettings settings = new TestBdpSettings();
    settings.setBdpDetail(true);
    settings.setBdpMaxOscillationRecoveryAttempts(0);
    settings.setBdpPrintAllIterations(true);
    settings.setBdpPrintOscillatingIterations(true);
    settings.setBdpRecordAllIterations(true);

    /*
     * Data plane computation should fail due to oscillation. Detailed information about the
     * oscillation should appear in the thrown exception.
     */
    _thrown.expect(
        allOf(
            isA(BdpOscillationException.class),
            BdpOscillationExceptionMatchers.hasMessage(
                containsString("Changed routes (iteration 2 ==> 3)"))));
    /*
     *  Assertions in test function below are not reached. In this test we only care about a proper
     *  exception being thrown during data plane computation.
     */
    testBgpOscillationRecovery(settings);
  }

  @Test
  public void testBgpOscillationPrintingNotEnoughInfo() throws IOException {
    TestBdpSettings settings = new TestBdpSettings();
    settings.setBdpDetail(true);
    settings.setBdpMaxOscillationRecoveryAttempts(0);
    settings.setBdpMaxRecordedIterations(0);
    settings.setBdpPrintAllIterations(true);
    settings.setBdpPrintOscillatingIterations(true);
    settings.setBdpRecordAllIterations(false);

    /*
     * Data plane computation should fail due to oscillation. Despite not recording enough info for
     * proper debugging initially, detailed information about the oscillation should still appear
     * in the thrown exception.
     */
    _thrown.expect(
        allOf(
            isA(BdpOscillationException.class),
            BdpOscillationExceptionMatchers.hasMessage(
                containsString("Changed routes (iteration 2 ==> 3)"))));
    /*
     *  Assertions in test function below are not reached. In this test we only care about a proper
     *  exception being thrown during data plane computation.
     */
    testBgpOscillationRecovery(settings);
  }

  @Test
  public void testBgpOscillationNoPrinting() throws IOException {
    TestBdpSettings settings = new TestBdpSettings();
    settings.setBdpDetail(true);
    settings.setBdpMaxOscillationRecoveryAttempts(0);
    settings.setBdpMaxRecordedIterations(0);
    settings.setBdpPrintAllIterations(false);
    settings.setBdpPrintOscillatingIterations(false);
    settings.setBdpRecordAllIterations(false);

    /*
     * Data plane computation should fail due to oscillation. Since printing is off, we should not
     * see detailed information about the oscillation.
     */
    _thrown.expect(
        allOf(
            isA(BdpOscillationException.class),
            BdpOscillationExceptionMatchers.hasMessage(
                not(containsString("Changed routes (iteration 2 ==> 3)")))));
    /*
     *  Assertions in test function are not reached. In this test we only care about a proper
     *  exception being thrown during data plane computation.
     */
    testBgpOscillationRecovery(settings);
  }

  @Test
  public void testBgpOscillationRecoveryEnoughInfo() throws IOException {
    TestBdpSettings settings = new TestBdpSettings();
    settings.setBdpDetail(true);
    settings.setBdpMaxOscillationRecoveryAttempts(1);
    settings.setBdpPrintAllIterations(false);
    settings.setBdpPrintOscillatingIterations(false);
    settings.setBdpRecordAllIterations(true);

    /*
     * Data plane computation should succeed despite oscillation, since we have enabled recovery.
     * Assertions about proper data plane computation results are made in the below helper
     * function.
     */
    testBgpOscillationRecovery(settings);
  }

  @Test
  public void testBgpOscillationRecoveryNotEnoughInfo() throws IOException {
    TestBdpSettings settings = new TestBdpSettings();
    settings.setBdpDetail(true);
    settings.setBdpMaxOscillationRecoveryAttempts(1);
    settings.setBdpMaxRecordedIterations(0);
    settings.setBdpPrintAllIterations(false);
    settings.setBdpPrintOscillatingIterations(false);
    settings.setBdpRecordAllIterations(false);

    /*
     * Data plane computation should succeed despite oscillation, since we have enabled recovery.
     * We do not initially record enough information to perform recovery. Success of assertions
     * contained in below helper function implies correct behavior of functionality to alter
     * recording settings and rerun with enough information to perform recovery.
     */
    testBgpOscillationRecovery(settings);
  }

  private void testBgpOscillationRecovery(TestBdpSettings bdpSettings) throws IOException {
    String testrigName = "bgp-oscillation";
    List<String> configurationNames = ImmutableList.of("r1", "r2", "r3");

    Batfish batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder()
                .setConfigurationText(TESTRIGS_PREFIX + testrigName, configurationNames)
                .build(),
            _folder);
    Settings settings = batfish.getSettings();
    settings.setBdpDetail(bdpSettings.getBdpDetail());
    settings.setBdpMaxOscillationRecoveryAttempts(
        bdpSettings.getBdpMaxOscillationRecoveryAttempts());
    settings.setBdpMaxRecordedIterations(bdpSettings.getBdpMaxRecordedIterations());
    settings.setBdpPrintAllIterations(bdpSettings.getBdpPrintAllIterations());
    settings.setBdpPrintOscillatingIterations(bdpSettings.getBdpPrintOscillatingIterations());
    settings.setBdpRecordAllIterations(bdpSettings.getBdpRecordAllIterations());
    BdpDataPlanePlugin dataPlanePlugin = new BdpDataPlanePlugin();
    dataPlanePlugin.initialize(batfish);

    /*
     * Data plane computation succeeds iff recovery is enabled. If disabled, an exception is thrown
     * and should be expected by caller.
     */
    batfish.computeDataPlane(false);

    SortedMap<String, SortedMap<String, SortedSet<AbstractRoute>>> routes =
        dataPlanePlugin.getRoutes(batfish.loadDataPlane());
    Prefix bgpPrefix = Prefix.parse("1.1.1.1/32");
    SortedSet<AbstractRoute> r2Routes = routes.get("r2").get(Configuration.DEFAULT_VRF_NAME);
    SortedSet<AbstractRoute> r3Routes = routes.get("r3").get(Configuration.DEFAULT_VRF_NAME);
    Stream<AbstractRoute> r2MatchingRoutes =
        r2Routes.stream().filter(r -> r.getNetwork().equals(bgpPrefix));
    Stream<AbstractRoute> r3MatchingRoutes =
        r3Routes.stream().filter(r -> r.getNetwork().equals(bgpPrefix));
    AbstractRoute r2Route =
        r2Routes.stream().filter(r -> r.getNetwork().equals(bgpPrefix)).findAny().get();
    AbstractRoute r3Route =
        r3Routes.stream().filter(r -> r.getNetwork().equals(bgpPrefix)).findAny().get();
    String r2NextHop = r2Route.getNextHop();
    String r3NextHop = r3Route.getNextHop();
    int routesWithR1AsNextHop = 0;
    if (r2Route.getNextHop().equals("r1")) {
      routesWithR1AsNextHop++;
    }
    if (r3Route.getNextHop().equals("r1")) {
      routesWithR1AsNextHop++;
    }
    boolean r2AsNextHop = r3NextHop.equals("r2");
    boolean r3AsNextHop = r2NextHop.equals("r3");

    /*
     * Data plane computation should succeed as follows if recovery is enabled.
     */
    assertThat(r2MatchingRoutes.count(), equalTo(1L));
    assertThat(r3MatchingRoutes.count(), equalTo(1L));
    assertThat(routesWithR1AsNextHop, equalTo(1));
    assertTrue((r2AsNextHop && !r3AsNextHop) || (!r2AsNextHop && r3AsNextHop));
  }

  @Test
  public void testBgpTieBreaker() {
    String hostname = "r1";
    Configuration c =
        BatfishTestUtils.createTestConfiguration(hostname, ConfigurationFormat.CISCO_IOS);
    BgpProcess proc = new BgpProcess();
    c.getVrfs().computeIfAbsent(Configuration.DEFAULT_VRF_NAME, Vrf::new).setBgpProcess(proc);
    Map<String, Node> nodes = new HashMap<String, Node>();
    Node node = new Node(c);
    nodes.put(hostname, node);
    VirtualRouter vr = new VirtualRouter(Configuration.DEFAULT_VRF_NAME, c);

    // good for both ebgp and ibgp
    BgpMultipathRib bmr = new BgpMultipathRib(vr);
    // ebgp
    BgpBestPathRib ebgpOldBbr = BgpBestPathRib.initial(vr);
    BgpBestPathRib ebgpNewBbr = new BgpBestPathRib(vr, ebgpOldBbr, false);
    BgpRoute.Builder ebgpBuilder =
        new BgpRoute.Builder()
            .setNetwork(Prefix.ZERO)
            .setOriginType(OriginType.INCOMPLETE)
            .setOriginatorIp(Ip.ZERO)
            .setProtocol(RoutingProtocol.BGP)
            .setReceivedFromIp(Ip.ZERO);
    BgpRoute ebgpOlderHigherOriginator = ebgpBuilder.setOriginatorIp(Ip.MAX).build();
    BgpRoute ebgpNewerHigherOriginator = ebgpBuilder.setOriginatorIp(Ip.MAX).build();
    BgpRoute ebgpLowerOriginator = ebgpBuilder.setOriginatorIp(Ip.ZERO).build();
    // ibgp
    BgpBestPathRib ibgpOldBbr = BgpBestPathRib.initial(vr);
    BgpBestPathRib ibgpNewBbr = new BgpBestPathRib(vr, ibgpOldBbr, false);
    BgpRoute.Builder ibgpBuilder =
        new BgpRoute.Builder()
            .setNetwork(Prefix.ZERO)
            .setOriginType(OriginType.INCOMPLETE)
            .setOriginatorIp(Ip.ZERO)
            .setProtocol(RoutingProtocol.IBGP)
            .setReceivedFromIp(Ip.ZERO);
    BgpRoute ibgpOlderHigherOriginator = ibgpBuilder.setOriginatorIp(Ip.MAX).build();
    BgpRoute ibgpNewerHigherOriginator = ibgpBuilder.setOriginatorIp(Ip.MAX).build();
    BgpRoute ibgpLowerOriginator = ibgpBuilder.setOriginatorIp(Ip.ZERO).build();

    ebgpOldBbr.mergeRoute(ebgpOlderHigherOriginator);
    ibgpOldBbr.mergeRoute(ibgpOlderHigherOriginator);

    /*
     * Given default tie-breaking, and all more important attributes being equivalent:
     * - When comparing two eBGP adverts, best-path rib prefers older advert.
     * - If neither is older, or one is iBGP, best-path rib prefers advert with higher router-id.
     * - Multipath RIB ignores both age and router-id, seeing both adverts as equal.
     */
    assertThat(
        ebgpOldBbr.comparePreference(ebgpNewerHigherOriginator, ebgpLowerOriginator), lessThan(0));
    assertThat(
        ebgpNewBbr.comparePreference(ebgpNewerHigherOriginator, ebgpLowerOriginator),
        greaterThan(0));
    assertThat(bmr.comparePreference(ebgpNewerHigherOriginator, ebgpLowerOriginator), equalTo(0));
    assertThat(
        ibgpOldBbr.comparePreference(ibgpNewerHigherOriginator, ibgpLowerOriginator), lessThan(0));
    assertThat(
        ibgpNewBbr.comparePreference(ibgpNewerHigherOriginator, ibgpLowerOriginator), lessThan(0));
    assertThat(bmr.comparePreference(ibgpNewerHigherOriginator, ibgpLowerOriginator), equalTo(0));
  }

  @Test
  public void testEbgpAcceptSameNeighborID() throws IOException {
    String testrigName = "ebgp-accept-routerid-match";
    List<String> configurationNames = ImmutableList.of("r1", "r2", "r3");

    Batfish batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder()
                .setConfigurationText(TESTRIGS_PREFIX + testrigName, configurationNames)
                .build(),
            _folder);
    BdpDataPlanePlugin dataPlanePlugin = new BdpDataPlanePlugin();
    dataPlanePlugin.initialize(batfish);
    batfish.computeDataPlane(false);
    SortedMap<String, SortedMap<String, SortedSet<AbstractRoute>>> routes =
        dataPlanePlugin.getRoutes(batfish.loadDataPlane());

    SortedSet<AbstractRoute> r1Routes = routes.get("r1").get(Configuration.DEFAULT_VRF_NAME);
    SortedSet<AbstractRoute> r3Routes = routes.get("r3").get(Configuration.DEFAULT_VRF_NAME);
    Set<Prefix> r1Prefixes = r1Routes.stream().map(r -> r.getNetwork()).collect(Collectors.toSet());
    Set<Prefix> r3Prefixes = r3Routes.stream().map(r -> r.getNetwork()).collect(Collectors.toSet());
    Prefix r1Loopback0Prefix = Prefix.parse("1.0.0.1/32");
    Prefix r3Loopback0Prefix = Prefix.parse("3.0.0.3/32");

    // Ensure that r3loopback was accepted by r1
    assertThat(r3Loopback0Prefix, isIn(r1Prefixes));
    // Check the other direction (r1loopback is accepted by r3)
    assertThat(r1Loopback0Prefix, isIn(r3Prefixes));
  }

  @Test
  public void testContainsRoute() {
    String hostname = "r1";
    Configuration c =
        BatfishTestUtils.createTestConfiguration(hostname, ConfigurationFormat.CISCO_IOS);
    BgpProcess proc = new BgpProcess();
    c.getVrfs().computeIfAbsent(Configuration.DEFAULT_VRF_NAME, Vrf::new).setBgpProcess(proc);
    Map<String, Node> nodes = new HashMap<String, Node>();
    Node node = new Node(c);
    nodes.put(hostname, node);
    VirtualRouter vr = new VirtualRouter(Configuration.DEFAULT_VRF_NAME, c);
    BgpBestPathRib bbr = BgpBestPathRib.initial(vr);
    BgpMultipathRib bmr = new BgpMultipathRib(vr);
    Ip ip1 = new Ip("1.0.0.0");
    Ip ip2 = new Ip("2.2.0.0");
    BgpRoute.Builder b1 =
        new BgpRoute.Builder()
            .setNextHopIp(Ip.ZERO)
            .setOriginType(OriginType.INCOMPLETE)
            .setOriginatorIp(Ip.ZERO)
            .setProtocol(RoutingProtocol.BGP)
            .setReceivedFromIp(Ip.ZERO);
    BgpRoute.Builder b2 =
        new BgpRoute.Builder()
            .setNextHopIp(Ip.ZERO)
            .setOriginType(OriginType.INCOMPLETE)
            .setOriginatorIp(Ip.MAX)
            .setProtocol(RoutingProtocol.BGP)
            .setReceivedFromIp(Ip.ZERO);

    /*
     * Toss a bunch of different routes in each RIB. In the best-path rib, only lower originatorIp
     * routes should remain. In the multipath RIB, all routes should remain.
     */
    for (int i = 8; i <= Prefix.MAX_PREFIX_LENGTH; i++) {
      Prefix p = new Prefix(ip1, i);
      b1.setNetwork(p);
      b2.setNetwork(p);
      bbr.mergeRoute(b1.build());
      bbr.mergeRoute(b2.build());
      bmr.mergeRoute(b1.build());
      bmr.mergeRoute(b2.build());
    }
    for (int i = 16; i <= Prefix.MAX_PREFIX_LENGTH; i++) {
      Prefix p = new Prefix(ip2, i);
      b1.setNetwork(p);
      b2.setNetwork(p);
      bbr.mergeRoute(b1.build());
      bbr.mergeRoute(b2.build());
      bmr.mergeRoute(b1.build());
      bmr.mergeRoute(b2.build());
    }
    for (int i = 8; i <= Prefix.MAX_PREFIX_LENGTH; i++) {
      Prefix p = new Prefix(ip1, i);
      assertTrue(bbr.containsRoute(b1.setNetwork(p).build()));
      b1.setNetwork(p);
      b2.setNetwork(p);
      assertTrue(bbr.containsRoute(b1.build()));
      assertFalse(bbr.containsRoute(b2.build()));
      assertTrue(bmr.containsRoute(b1.build()));
      assertTrue(bmr.containsRoute(b2.build()));
    }
    for (int i = 16; i <= Prefix.MAX_PREFIX_LENGTH; i++) {
      Prefix p = new Prefix(ip2, i);
      b1.setNetwork(p);
      b2.setNetwork(p);
      assertTrue(bbr.containsRoute(b1.build()));
      assertFalse(bbr.containsRoute(b2.build()));
      assertTrue(bmr.containsRoute(b1.build()));
      assertTrue(bmr.containsRoute(b2.build()));
    }
  }

  @Test
  public void testIbgpRejectOwnAs() throws IOException {
    String testrigName = "ibgp-reject-own-as";
    List<String> configurationNames = ImmutableList.of("r1", "r2a", "r2b");

    Batfish batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder()
                .setConfigurationText(TESTRIGS_PREFIX + testrigName, configurationNames)
                .build(),
            _folder);
    BdpDataPlanePlugin dataPlanePlugin = new BdpDataPlanePlugin();
    dataPlanePlugin.initialize(batfish);
    batfish.computeDataPlane(false);
    SortedMap<String, SortedMap<String, SortedSet<AbstractRoute>>> routes =
        dataPlanePlugin.getRoutes(batfish.loadDataPlane());
    SortedSet<AbstractRoute> r2aRoutes = routes.get("r2a").get(Configuration.DEFAULT_VRF_NAME);
    SortedSet<AbstractRoute> r2bRoutes = routes.get("r2b").get(Configuration.DEFAULT_VRF_NAME);
    Set<Prefix> r2aPrefixes =
        r2aRoutes.stream().map(AbstractRoute::getNetwork).collect(Collectors.toSet());
    Set<Prefix> r2bPrefixes =
        r2bRoutes.stream().map(AbstractRoute::getNetwork).collect(Collectors.toSet());
    Prefix r1Loopback0Prefix = Prefix.parse("1.0.0.1/32");
    Prefix r1Loopback1Prefix = Prefix.parse("1.0.0.2/32");
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
    List<String> configurationNames = ImmutableList.of("r1", "r2", "r3", "r4");

    Batfish batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder()
                .setConfigurationText(TESTRIGS_PREFIX + testrigName, configurationNames)
                .build(),
            _folder);
    BdpDataPlanePlugin dataPlanePlugin = new BdpDataPlanePlugin();
    dataPlanePlugin.initialize(batfish);
    batfish.computeDataPlane(false);
    SortedMap<String, SortedMap<String, SortedSet<AbstractRoute>>> routes =
        dataPlanePlugin.getRoutes(batfish.loadDataPlane());

    SortedSet<AbstractRoute> r2Routes = routes.get("r2").get(Configuration.DEFAULT_VRF_NAME);
    SortedSet<AbstractRoute> r3Routes = routes.get("r3").get(Configuration.DEFAULT_VRF_NAME);
    Set<Prefix> r2Prefixes = r2Routes.stream().map(r -> r.getNetwork()).collect(Collectors.toSet());
    Set<Prefix> r3Prefixes = r3Routes.stream().map(r -> r.getNetwork()).collect(Collectors.toSet());
    // 9.9.9.9/32 is the prefix we test with
    Prefix r1AdvertisedPrefix = Prefix.parse("9.9.9.9/32");

    // Ensure that the prefix is accepted by r2, because router ids are different
    assertThat(r1AdvertisedPrefix, isIn(r2Prefixes));
    // Ensure that the prefix is rejected by r3, because router ids are the same
    assertThat(r1AdvertisedPrefix, not(isIn(r3Prefixes)));
  }

  @Test
  public void testIosRtStaticMatchesBdp() throws IOException {
    String testrigResourcePrefix = TESTRIGS_PREFIX + "ios-rt-static-ad";
    List<String> configurationNames = ImmutableList.of("r1");
    List<String> routingTableNames = ImmutableList.of("r1");

    Batfish batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder()
                .setConfigurationText(testrigResourcePrefix, configurationNames)
                .setRoutingTablesText(testrigResourcePrefix, routingTableNames)
                .build(),
            _folder);
    BdpDataPlanePlugin dataPlanePlugin = new BdpDataPlanePlugin();
    dataPlanePlugin.initialize(batfish);
    batfish.computeDataPlane(false);
    SortedMap<String, RoutesByVrf> environmentRoutes = batfish.loadEnvironmentRoutingTables();
    SortedMap<String, SortedMap<String, SortedSet<AbstractRoute>>> routes =
        dataPlanePlugin.getRoutes(batfish.loadDataPlane());
    Prefix staticRoutePrefix = Prefix.parse("10.0.0.0/8");
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

  @Test
  public void testStaticInterfaceRoutesWithoutEdge() {
    NetworkFactory nf = new NetworkFactory();
    Configuration c =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS).build();
    Vrf vrf = nf.vrfBuilder().setOwner(c).setName(Configuration.DEFAULT_VRF_NAME).build();
    Interface i =
        nf.interfaceBuilder()
            .setOwner(c)
            .setVrf(vrf)
            .setAddress(new InterfaceAddress("10.0.0.0/24"))
            .setActive(true)
            .build();
    StaticRoute srBoth =
        StaticRoute.builder()
            .setNetwork(Prefix.parse("10.0.1.0/24"))
            .setNextHopInterface(i.getName())
            .setNextHopIp(new Ip("10.0.0.1"))
            .build();
    vrf.getStaticRoutes().add(srBoth);
    StaticRoute srJustInterface =
        StaticRoute.builder()
            .setNetwork(Prefix.parse("10.0.2.0/24"))
            .setNextHopInterface(i.getName())
            .build();
    vrf.getStaticRoutes().add(srJustInterface);
    BdpEngine engine =
        new BdpEngine(
            new TestBdpSettings(),
            new BatfishLogger(BatfishLogger.LEVELSTR_DEBUG, false),
            (a, b) -> new AtomicInteger());
    Topology topology = new Topology(Collections.emptySortedSet());
    BdpDataPlane dp =
        engine.computeDataPlane(
            false,
            ImmutableMap.of(c.getName(), c),
            topology,
            Collections.emptySet(),
            ImmutableSet.of(new NodeInterfacePair(c.getName(), i.getName())),
            new BdpAnswerElement());

    // generating fibs should not crash
    dp.getFibs();
  }
}
