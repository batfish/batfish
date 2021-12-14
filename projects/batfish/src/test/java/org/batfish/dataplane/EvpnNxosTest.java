package org.batfish.dataplane;

import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.datamodel.Names.generatedTenantVniInterfaceName;
import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.hasNextHop;
import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.hasPrefix;
import static org.batfish.datamodel.matchers.HopMatchers.hasAcceptingInterface;
import static org.batfish.datamodel.matchers.TraceMatchers.hasDisposition;
import static org.batfish.datamodel.matchers.TraceMatchers.hasLastHop;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;

import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
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

/** End-to-end-ish test of EVPN Type 5 routes and VTEP forwarding on NX-OS devices */
public class EvpnNxosTest {
  private static final String SNAPSHOT_FOLDER = "org/batfish/dataplane/testrigs/evpn-l3-vnis";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private Batfish _batfish;

  private static final int L3VNI = 10001;
  private static final String TENANT_VRF_NAME = "tenant";

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
            allOf(hasPrefix(Prefix.strict("10.0.1.0/24")), hasNextHop(NextHopDiscard.instance())),
            allOf(
                hasPrefix(Prefix.strict("10.0.2.0/24")),
                hasNextHop(NextHopVtep.of(L3VNI, Ip.parse("10.0.4.2"))))));
    assertThat(
        r2EvpnRoutes,
        containsInAnyOrder(
            allOf(hasPrefix(Prefix.strict("10.0.2.0/24")), hasNextHop(NextHopDiscard.instance())),
            allOf(
                hasPrefix(Prefix.strict("10.0.1.0/24")),
                hasNextHop(NextHopVtep.of(L3VNI, Ip.parse("10.0.4.1"))))));

    // test bgpv4 routes
    Set<Bgpv4Route> r1BgpRoutes = dp.getBgpRoutes().get("r1", TENANT_VRF_NAME);
    Set<Bgpv4Route> r2BgpRoutes = dp.getBgpRoutes().get("r2", TENANT_VRF_NAME);

    assertThat(
        r1BgpRoutes,
        containsInAnyOrder(
            allOf(hasPrefix(Prefix.strict("10.0.1.0/24")), hasNextHop(NextHopDiscard.instance())),
            allOf(
                hasPrefix(Prefix.strict("10.0.2.0/24")),
                hasNextHop(NextHopVtep.of(L3VNI, Ip.parse("10.0.4.2"))))));
    assertThat(
        r2BgpRoutes,
        containsInAnyOrder(
            allOf(hasPrefix(Prefix.strict("10.0.2.0/24")), hasNextHop(NextHopDiscard.instance())),
            allOf(
                hasPrefix(Prefix.strict("10.0.1.0/24")),
                hasNextHop(NextHopVtep.of(L3VNI, Ip.parse("10.0.4.1"))))));
  }

  @Test(expected = AssertionError.class) // xfail this until l3vni topology edges are fixed
  public void testTopology() {
    Topology topology = _batfish.getTopologyProvider().getLayer3Topology(_batfish.getSnapshot());
    String vniIface = generatedTenantVniInterfaceName(L3VNI);

    assertThat(
        topology.getEdges(),
        allOf(
            hasItem(Edge.of("r1", vniIface, "r2", vniIface)),
            hasItem(Edge.of("r2", vniIface, "r1", vniIface))));
  }

  @Test(expected = AssertionError.class) // xfail this until l3vni topology edges are fixed
  public void testConnectivity() {
    Ip h1Ip = Ip.parse("10.0.1.1");
    Ip h2Ip = Ip.parse("10.0.2.2");
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
}
