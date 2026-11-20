package org.batfish.bddreachability;

import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import net.sf.javabdd.BDD;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.datamodel.acl.AclLineMatchExprs;
import org.batfish.datamodel.flow.Trace;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.main.TestrigText;
import org.batfish.specifier.InterfaceLocation;
import org.batfish.specifier.IpSpaceAssignment;
import org.batfish.symbolic.IngressLocation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests that BDD reachability and traceroute agree on traffic received on an OSPF point-to-point
 * interface whose only concrete address is a /32. Such an interface also gets a link-local address,
 * and layer-3 topology connects it to its physical neighbor.
 */
final class BDDReachabilityPointToPointTest {

  @TempDir public File temp;

  private static final String R1 = "r1";
  private static final String R2 = "r2";
  private static final Ip R1_IP = Ip.parse("10.0.0.1");
  private static final Ip R2_IP = Ip.parse("10.0.0.2");

  /**
   * A Cumulus router whose swp1 has only the /32 {@code ip} and runs OSPF point-to-point, plus the
   * given extra FRR configuration lines.
   */
  private static String router(String hostname, Ip ip, String... frrLines) {
    return String.join(
        "\n",
        hostname,
        "# This file describes the network interfaces",
        "auto swp1",
        "iface swp1",
        "  address " + ip + "/32",
        "# ports.conf --",
        "frr version 4.0+cl3u8",
        "hostname " + hostname,
        "interface swp1",
        " ip ospf area 0",
        " ip ospf network point-to-point",
        "router ospf",
        " ospf router-id " + ip,
        String.join("\n", frrLines),
        "");
  }

  /** r1 and r2, connected by swp1, with a static route on r1 to r2's swp1 address. */
  private Batfish network() throws IOException {
    String layer1 =
        "{\"edges\": [{"
            + "\"node1\": {\"hostname\": \"r1\", \"interfaceName\": \"swp1\"},"
            + "\"node2\": {\"hostname\": \"r2\", \"interfaceName\": \"swp1\"}}]}";
    Batfish batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder()
                .setConfigurationText(
                    ImmutableMap.of(
                        R1,
                        router(R1, R1_IP, "ip route " + R2_IP + "/32 swp1"),
                        R2,
                        router(R2, R2_IP)))
                .setLayer1TopologyBytes(layer1.getBytes(StandardCharsets.UTF_8))
                .build(),
            temp);
    batfish.computeDataPlane(batfish.getSnapshot());
    return batfish;
  }

  private static FlowDisposition tracerouteDisposition(Batfish batfish, Flow flow) {
    List<Trace> traces =
        batfish
            .getTracerouteEngine(batfish.getSnapshot())
            .computeTraces(ImmutableSet.of(flow), false)
            .get(flow);
    assertThat(traces.size(), equalTo(1));
    return traces.get(0).getDisposition();
  }

  /** The headers to {@code dstIp} originated at {@code node} that BDD reachability accepts. */
  private static BDD bddAccepted(Batfish batfish, BDDPacket pkt, String node, Ip dstIp) {
    BDDReachabilityAnalysis analysis =
        batfish.getBddReachabilityAnalysis(
            batfish.getSnapshot(),
            pkt,
            IpSpaceAssignment.builder()
                .assign(new InterfaceLocation(node, "swp1"), UniverseIpSpace.INSTANCE)
                .build(),
            AclLineMatchExprs.matchDst(dstIp),
            ImmutableSet.of(),
            ImmutableSet.of(),
            ImmutableSet.of(R1, R2),
            ImmutableSet.of(FlowDisposition.ACCEPTED),
            false,
            false);
    return analysis
        .getIngressLocationReachableBDDs()
        .getOrDefault(IngressLocation.vrf(node, DEFAULT_VRF_NAME), pkt.getFactory().zero());
  }

  @Test
  void testTrafficReceivedOnSlash32PointToPointInterface() throws IOException {
    Batfish batfish = network();
    // Layer-3 topology connects the /32 interfaces.
    assertThat(
        batfish.getTopologyProvider().getLayer3Topology(batfish.getSnapshot()).getEdges(),
        hasItem(Edge.of(R1, "swp1", R2, "swp1")));

    Flow flow =
        Flow.builder()
            .setIngressNode(R1)
            .setIngressVrf(DEFAULT_VRF_NAME)
            .setSrcIp(R1_IP)
            .setDstIp(R2_IP)
            .build();
    assertThat(tracerouteDisposition(batfish, flow), equalTo(FlowDisposition.ACCEPTED));

    BDDPacket pkt = new BDDPacket();
    assertFalse(
        bddAccepted(batfish, pkt, R1, R2_IP).isZero(),
        "BDD reachability accepts the traffic that traceroute accepts");
  }
}
