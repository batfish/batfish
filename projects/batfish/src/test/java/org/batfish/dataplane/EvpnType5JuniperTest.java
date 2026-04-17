package org.batfish.dataplane;

import static org.batfish.datamodel.FlowDisposition.DELIVERED_TO_SUBNET;
import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.hasPrefix;
import static org.batfish.datamodel.matchers.BgpRouteMatchers.isEvpnType5RouteThat;
import static org.batfish.datamodel.matchers.HopMatchers.hasNodeName;
import static org.batfish.datamodel.matchers.TraceMatchers.hasDisposition;
import static org.batfish.datamodel.matchers.TraceMatchers.hasHops;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Table;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.plugin.TracerouteEngine;
import org.batfish.config.Settings;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.AnnotatedRoute;
import org.batfish.datamodel.EvpnRoute;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.GenericRib;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;
import org.batfish.datamodel.flow.Trace;
import org.batfish.dataplane.ibdp.IncrementalDataPlane;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.main.TestrigText;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** End-to-end dataplane test for testing BGP EVPN Type 5 Route propagation and Traceroute. */
public class EvpnType5JuniperTest {

  private static final String TESTRIGS_PREFIX = "org/batfish/dataplane/testrigs/";
  private static final String TESTRIG_NAME = "bgp-evpn-type5-route-test";
  private static final List<String> TESTRIG_NODE_NAMES =
      ImmutableList.of("edge1", "router1", "node2-1", "node1-1");

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private Batfish loadTestrigAndComputeDataPlane() throws IOException {
    Batfish batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder()
                .setConfigurationFiles(TESTRIGS_PREFIX + TESTRIG_NAME, TESTRIG_NODE_NAMES)
                .build(),
            _folder);
    Settings settings = batfish.getSettings();
    settings.setDisableUnrecognized(false);
    settings.setHaltOnConvertError(false);
    settings.setHaltOnParseError(false);
    settings.setThrowOnLexerError(false);
    settings.setThrowOnParserError(false);
    batfish.computeDataPlane(batfish.getSnapshot());
    return batfish;
  }

  @Test
  public void testEvpnType5RoutePropagation() throws IOException {
    Batfish batfish = loadTestrigAndComputeDataPlane();
    NetworkSnapshot snapshot = batfish.getSnapshot();

    IncrementalDataPlane dp = (IncrementalDataPlane) batfish.loadDataPlane(snapshot);
    Table<String, String, Set<EvpnRoute<?, ?>>> evpnRoutes = dp.getEvpnRoutes();
    Prefix targetPrefix = Prefix.parse("192.168.99.0/24");

    // edge1 distributes prefix via BGP to router1.TENANT-A (group 'edge')
    // router1.TENANT-A advertises the prefix as EVPN Type 5 route to node2-1
    Set<EvpnRoute<?, ?>> node2Routes =
        evpnRoutes.get("node2-1", org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME);
    assertThat(node2Routes, hasItem(isEvpnType5RouteThat(hasPrefix(targetPrefix))));

    // node2-1 redistributes to node1-1 as EVPN
    Set<EvpnRoute<?, ?>> node1Routes =
        evpnRoutes.get("node1-1", org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME);
    assertThat(node1Routes, hasItem(isEvpnType5RouteThat(hasPrefix(targetPrefix))));

    // node1-1 installs EVPN Type 5 into its TENANT-A RIB
    Map<String, java.util.SortedMap<String, GenericRib<AnnotatedRoute<AbstractRoute>>>> ribs =
        dp.getRibsForTesting();
    GenericRib<AnnotatedRoute<AbstractRoute>> node1MainRib = ribs.get("node1-1").get("TENANT-A");

    boolean foundInNode1 = false;
    for (AnnotatedRoute<AbstractRoute> route : node1MainRib.getRoutes()) {
      if (route.getNetwork().equals(targetPrefix)) {
        foundInNode1 = true;
        assertThat(route.getRoute().getTag(), equalTo(999L));
      }
    }
    assertTrue(
        String.format("Route %s should be in %s %s", targetPrefix, "node1-1", "TENANT-A"),
        foundInNode1);
  }

  @Test
  public void testEvpnType5RoutePropagation_router1() throws IOException {
    Batfish batfish = loadTestrigAndComputeDataPlane();
    NetworkSnapshot snapshot = batfish.getSnapshot();

    IncrementalDataPlane dp = (IncrementalDataPlane) batfish.loadDataPlane(snapshot);
    Prefix targetPrefix = Prefix.parse("192.168.99.0/24");

    Map<String, java.util.SortedMap<String, GenericRib<AnnotatedRoute<AbstractRoute>>>> ribs =
        dp.getRibsForTesting();
    GenericRib<AnnotatedRoute<AbstractRoute>> router1MainRib = ribs.get("router1").get("TENANT-A");

    boolean foundInRouter1 = false;
    for (AnnotatedRoute<AbstractRoute> route : router1MainRib.getRoutes()) {
      if (route.getNetwork().equals(targetPrefix)) {
        foundInRouter1 = true;
      }
    }
    assertTrue(
        String.format("Route %s should be in %s %s", targetPrefix, "router1", "TENANT-A"),
        foundInRouter1);
  }

  @Test
  public void testTracerouteNode1ToEdge1() throws IOException {
    Batfish batfish = loadTestrigAndComputeDataPlane();
    NetworkSnapshot snapshot = batfish.getSnapshot();

    TracerouteEngine tracerouteEngine = batfish.getTracerouteEngine(snapshot);

    Flow flow =
        Flow.builder()
            .setIngressNode("node1-1")
            .setIngressInterface("irb.100")
            .setSrcIp(Ip.parse("172.16.100.6"))
            .setDstIp(Ip.parse("192.168.99.10"))
            .build();

    Map<Flow, List<Trace>> tracesMap = tracerouteEngine.computeTraces(ImmutableSet.of(flow), false);
    List<Trace> traces = tracesMap.get(flow);

    assertNotNull(traces);
    assertFalse(traces.isEmpty());

    for (Trace trace : traces) {
      assertThat(trace, hasDisposition(DELIVERED_TO_SUBNET));
      assertThat(
          trace,
          hasHops(contains(hasNodeName("node1-1"), hasNodeName("router1"), hasNodeName("edge1"))));
    }
  }

  @Test
  public void testEvpnType5RouteOriginatorIp() throws IOException {
    Batfish batfish = loadTestrigAndComputeDataPlane();
    NetworkSnapshot snapshot = batfish.getSnapshot();

    IncrementalDataPlane dp = (IncrementalDataPlane) batfish.loadDataPlane(snapshot);
    Prefix targetPrefix = Prefix.parse("192.168.99.0/24");

    Map<String, java.util.SortedMap<String, GenericRib<AnnotatedRoute<AbstractRoute>>>> ribs =
        dp.getRibsForTesting();
    GenericRib<AnnotatedRoute<AbstractRoute>> node1MainRib = ribs.get("node1-1").get("TENANT-A");

    Ip originatorIp = null;
    for (AnnotatedRoute<AbstractRoute> route : node1MainRib.getRoutes()) {
      if (route.getNetwork().equals(targetPrefix)
          && route.getRoute() instanceof org.batfish.datamodel.BgpRoute) {
        originatorIp = ((org.batfish.datamodel.BgpRoute<?, ?>) route.getRoute()).getOriginatorIp();
      }
    }

    assertNotNull("Route should be present and be a BGP route", originatorIp);
    assertNotEquals("Originator IP should not be 0.0.0.0", Ip.ZERO, originatorIp);

    // We expect the Originator ID to match router1's router ID (172.16.0.100).
    assertThat(originatorIp, org.hamcrest.Matchers.equalTo(Ip.parse("172.16.0.100")));
  }

  /**
   * Tests that the vrf-export policy on router1's TENANT-A is actually called. Router1 has a static
   * route 10.10.10.0/24 in TENANT-A and a vrf-export policy (TENANT-A-vrf-export-redist) that
   * rejects that prefix but accepts all other routes with 'community add gateway-community'.
   * Without vrf-export support, the default redistribution would export connected+static routes,
   * causing 10.10.10.0/24 to appear in node1-1's TENANT-A RIB via EVPN. With vrf-export, the route
   * is blocked and accepted routes get the gateway-community (target:65000:99) attached.
   */
  @Test
  public void testEvpnType5VrfExportFiltering() throws IOException {
    Batfish batfish = loadTestrigAndComputeDataPlane();
    NetworkSnapshot snapshot = batfish.getSnapshot();

    IncrementalDataPlane dp = (IncrementalDataPlane) batfish.loadDataPlane(snapshot);
    Prefix blockedPrefix = Prefix.parse("10.10.10.0/24");

    Map<String, java.util.SortedMap<String, GenericRib<AnnotatedRoute<AbstractRoute>>>> ribs =
        dp.getRibsForTesting();

    // Verify 10.10.10.0/24 IS present in router1's TENANT-A (local static route)
    GenericRib<AnnotatedRoute<AbstractRoute>> router1Rib = ribs.get("router1").get("TENANT-A");
    assertThat(router1Rib.getRoutes(), hasItem(hasPrefix(blockedPrefix)));

    // Verify 10.10.10.0/24 is NOT present in node1-1's TENANT-A
    // (vrf-export rejected it, so it was never redistributed into EVPN)
    GenericRib<AnnotatedRoute<AbstractRoute>> node1Rib = ribs.get("node1-1").get("TENANT-A");
    for (AnnotatedRoute<AbstractRoute> route : node1Rib.getRoutes()) {
      assertNotEquals(
          "Route 10.10.10.0/24 should be blocked by vrf-export policy",
          blockedPrefix,
          route.getNetwork());
    }

    // Verify 192.168.99.0/24 STILL propagates (vrf-export accepts it)
    Prefix allowedPrefix = Prefix.parse("192.168.99.0/24");
    assertThat(node1Rib.getRoutes(), hasItem(hasPrefix(allowedPrefix)));

    // NOTE: The vrf-export policy includes 'community add gateway-community' in its accept-rest
    // term, but Batfish currently wraps the vrf-export policy as a boolean CallExpr filter
    // (accept/reject only). Route attribute modifications like 'community add' are NOT evaluated
    // by the redistribution policy. This assertion documents that current limitation.
    Table<String, String, Set<EvpnRoute<?, ?>>> evpnRoutes = dp.getEvpnRoutes();
    Set<EvpnRoute<?, ?>> node2EvpnRoutes =
        evpnRoutes.get("node2-1", org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME);
    ExtendedCommunity gatewayCommunity = ExtendedCommunity.target(65000, 99);
    boolean foundRouteWithGatewayCommunity = false;
    for (EvpnRoute<?, ?> route : node2EvpnRoutes) {
      if (route.getNetwork().equals(allowedPrefix)
          && route.getCommunities().getCommunities().contains(gatewayCommunity)) {
        foundRouteWithGatewayCommunity = true;
      }
    }
    // TODO: When redistribution policy attribute modification is supported, change to assertTrue
    assertFalse(
        "community-add in vrf-export is not currently evaluated (attribute modification unsupported"
            + " in redistribution CallExpr filter)",
        foundRouteWithGatewayCommunity);
  }
}
