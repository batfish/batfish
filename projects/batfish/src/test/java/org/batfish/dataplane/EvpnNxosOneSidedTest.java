package org.batfish.dataplane;

import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.datamodel.Names.generatedTenantVniInterfaceName;
import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.hasNextHop;
import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.hasPrefix;
import static org.batfish.datamodel.matchers.HopMatchers.hasAcceptingInterface;
import static org.batfish.datamodel.matchers.HopMatchers.hasInputInterface;
import static org.batfish.datamodel.matchers.TraceMatchers.hasDisposition;
import static org.batfish.datamodel.matchers.TraceMatchers.hasLastHop;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;

import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.EvpnRoute;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.flow.Trace;
import org.batfish.datamodel.route.nh.NextHopDiscard;
import org.batfish.datamodel.route.nh.NextHopVtep;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.main.TestrigText;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** End-to-end-ish test of one-sided EVPN Type 5 routes and VTEP forwarding on NX-OS devices */
public class EvpnNxosOneSidedTest {
  private static final String SNAPSHOT_FOLDER =
      "org/batfish/dataplane/testrigs/evpn-l3-vnis-one-sided";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private Batfish _batfish;

  private static final int L3VNI = 10001;
  private static final String TENANT_VRF_NAME = "tenant";

  /*
   * Topology:
   *                   10.0.4.1                   10.0.4.2
   * h1 <===============> r1 <=====================> r2 <===============> h2
   *    i1    Ethernet1/2    Ethernet1/1 Ethernet1/1    Ethernet1/2    i1
   *    .2 10.0.1.0/24 .1    .1    10.0.3.0/24    .2    .1 10.0.2.0/24 .2
   * - There is an EVPN session betwen r1 and r2
   * - Ethernet1/1 on r1/r2 are in default vrf
   * - Ethernet1/2 on r1/r2 are in tenant vrf
   * - 10.0.2.0/24 is redistributed into BGP on r2
   * - BGP routes routes in tenant vrf are leaked into EVPN on r2
   * - r1 tenant vrf main RIB should have 10.0.2.0/24 with VTEP next hop 10.0.4.2 for VNI 10001
   * - Subnet of r2 interface connected to h2 is redistributed into BGP
   * - h1 should be able to reach h2 across the tunnel, but not vice versa.
   */
  @Before
  public void setup() throws IOException {
    String r1Filename = "r1";
    String r2Filename = "r2";
    String h1Filename = "h1.json";
    String h2Filename = "h2.json";
    _batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder()
                .setConfigurationFiles(SNAPSHOT_FOLDER, ImmutableSet.of(r1Filename, r2Filename))
                .setHostsFiles(SNAPSHOT_FOLDER, ImmutableSet.of(h1Filename, h2Filename))
                .setLayer1TopologyPrefix(SNAPSHOT_FOLDER)
                .build(),
            _folder);
    _batfish.computeDataPlane(_batfish.getSnapshot());
  }

  @Test
  public void testRoutes() {
    DataPlane dp = _batfish.loadDataPlane(_batfish.getSnapshot());

    // test evpn routes
    Set<EvpnRoute<?, ?>> r1EvpnRoutes = dp.getEvpnRoutes().get("r1", DEFAULT_VRF_NAME);
    Set<EvpnRoute<?, ?>> r2EvpnRoutes = dp.getEvpnRoutes().get("r2", DEFAULT_VRF_NAME);

    assertThat(
        r1EvpnRoutes,
        containsInAnyOrder(
            allOf(
                hasPrefix(Prefix.strict("10.0.2.0/24")),
                hasNextHop(NextHopVtep.of(L3VNI, Ip.parse("10.0.4.2"))))));
    assertThat(
        r2EvpnRoutes,
        containsInAnyOrder(
            allOf(hasPrefix(Prefix.strict("10.0.2.0/24")), hasNextHop(NextHopDiscard.instance()))));

    // test bgpv4 routes
    Set<Bgpv4Route> r1BgpRoutes = dp.getBgpRoutes().get("r1", TENANT_VRF_NAME);
    Set<Bgpv4Route> r2BgpRoutes = dp.getBgpRoutes().get("r2", TENANT_VRF_NAME);

    assertThat(
        r1BgpRoutes,
        containsInAnyOrder(
            allOf(
                hasPrefix(Prefix.strict("10.0.2.0/24")),
                hasNextHop(NextHopVtep.of(L3VNI, Ip.parse("10.0.4.2"))))));
    assertThat(
        r2BgpRoutes,
        containsInAnyOrder(
            allOf(hasPrefix(Prefix.strict("10.0.2.0/24")), hasNextHop(NextHopDiscard.instance()))));

    // test main RIB routes
    Set<AbstractRoute> r1Routes = dp.getRibs().get("r1", TENANT_VRF_NAME).getRoutes();
    Set<AbstractRoute> r2Routes = dp.getRibs().get("r2", TENANT_VRF_NAME).getRoutes();

    assertThat(
        r1Routes,
        hasItem(
            allOf(
                hasPrefix(Prefix.strict("10.0.2.0/24")),
                hasNextHop(NextHopVtep.of(L3VNI, Ip.parse("10.0.4.2"))))));
    assertThat(r2Routes, not(hasItem(hasPrefix(Prefix.strict("10.0.1.0/24")))));
  }

  @Test
  public void testTopology() {
    Topology topology = _batfish.getTopologyProvider().getLayer3Topology(_batfish.getSnapshot());
    String vniIface = generatedTenantVniInterfaceName(L3VNI);

    assertThat(
        topology.getEdges(),
        allOf(
            hasItem(Edge.of("r1", vniIface, "r2", vniIface)),
            hasItem(Edge.of("r2", vniIface, "r1", vniIface))));
  }

  @Test
  public void testConnectivity() {
    Ip h1Ip = Ip.parse("10.0.1.2");
    Ip h2Ip = Ip.parse("10.0.2.2");
    {
      Flow flow =
          Flow.builder()
              .setIngressNode("h1")
              .setIngressInterface("i1")
              .setDstIp(h2Ip)
              .setSrcIp(h1Ip)
              .build();
      SortedMap<Flow, List<Trace>> traces =
          _batfish
              .getTracerouteEngine(_batfish.getSnapshot())
              .computeTraces(ImmutableSet.of(flow), false);

      assertThat(
          traces.get(flow),
          contains(
              allOf(
                  hasDisposition(FlowDisposition.ACCEPTED),
                  hasLastHop(hasAcceptingInterface(NodeInterfacePair.of("h2", "i1"))))));
    }
    {
      Flow flow =
          Flow.builder()
              .setIngressNode("h2")
              .setIngressInterface("i1")
              .setDstIp(h1Ip)
              .setSrcIp(h2Ip)
              .build();
      SortedMap<Flow, List<Trace>> traces =
          _batfish
              .getTracerouteEngine(_batfish.getSnapshot())
              .computeTraces(ImmutableSet.of(flow), false);

      assertThat(
          traces.get(flow),
          contains(
              allOf(
                  hasDisposition(FlowDisposition.NO_ROUTE),
                  hasLastHop(hasInputInterface(NodeInterfacePair.of("r2", "Ethernet1/2"))))));
    }
  }
}
