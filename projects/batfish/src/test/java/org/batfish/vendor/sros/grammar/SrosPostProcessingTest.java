package org.batfish.vendor.sros.grammar;

import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.graph.ValueGraph;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import org.batfish.common.topology.Layer1Topologies;
import org.batfish.datamodel.BgpPeerConfigId;
import org.batfish.datamodel.BgpSessionProperties;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.bgp.BgpTopology;
import org.batfish.datamodel.bgp.BgpTopologyUtils;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.main.TestrigText;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Post-processing (P6) tests for SR-OS. Loads the captured {@code sros_ceos_ebgp} lab — an SR-OS r1
 * and a cEOS r2 with the user-provided Layer-1 topology — through the full Batfish pipeline (parse,
 * convert, and {@code Batfish.postProcessSnapshot}) and asserts that SR-OS constructs flow through
 * post-processing correctly:
 *
 * <ul>
 *   <li>interface dependency resolution leaves the SR-OS interfaces active (no spurious
 *       deactivation),
 *   <li>the physical port / logical router-interface split lets the Layer-1 topology — which names
 *       the physical port — survive into the active logical L1 and drive the cross-vendor L3 edge,
 *       and
 *   <li>the eBGP session between r1 and r2 is established on the post-processed model.
 * </ul>
 *
 * <p>The r2 cEOS config is the captured lab config with the {@code system l1} hardware block
 * removed (that block is L1-only and is not accepted by this Batfish's Arista grammar; it does not
 * affect routing).
 */
public final class SrosPostProcessingTest {

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private static final String SNAPSHOTS_PREFIX =
      "org/batfish/vendor/sros/grammar/snapshots/sros_ceos_ebgp";

  private Batfish getBatfish() throws IOException {
    return BatfishTestUtils.getBatfishFromTestrigText(
        TestrigText.builder()
            .setConfigurationFiles(SNAPSHOTS_PREFIX, ImmutableList.of("r1", "r2"))
            .setLayer1TopologyPrefix(SNAPSHOTS_PREFIX)
            .build(),
        _folder);
  }

  /**
   * Every SR-OS interface stays active through interface-dependency post-processing. The L3
   * router-interface {@code to-r2} survives even though it BINDs the physical port {@code
   * 1/1/c1/1}, because the port is active and present in the Layer-1 topology.
   */
  @Test
  public void testSrosInterfacesActiveAfterPostProcessing() throws IOException {
    Batfish batfish = getBatfish();
    Configuration r1 = batfish.loadConfigurations(batfish.getSnapshot()).get("r1");

    Interface system = r1.getAllInterfaces().get("system");
    assertThat(system.getInterfaceType(), equalTo(InterfaceType.LOOPBACK));
    assertTrue(system.getActive());

    Interface port = r1.getAllInterfaces().get("1/1/c1/1");
    assertThat(port.getInterfaceType(), equalTo(InterfaceType.PHYSICAL));
    assertTrue(port.getActive());
    assertNull(port.getConcreteAddress());

    Interface toR2 = r1.getAllInterfaces().get("to-r2");
    assertThat(toR2.getInterfaceType(), equalTo(InterfaceType.LOGICAL));
    assertTrue(toR2.getActive());
    assertThat(toR2.getConcreteAddress().getPrefix(), equalTo(Prefix.parse("10.0.0.0/31")));
    // The logical interface binds the physical port (fate-sharing + Layer-1 mapping).
    assertThat(
        toR2.getDependencies(),
        contains(new Interface.Dependency("1/1/c1/1", Interface.DependencyType.BIND)));
  }

  /**
   * The user-provided Layer-1 topology — which names r1's <em>physical port</em> {@code 1/1/c1/1}
   * (not the L3 interface {@code to-r2}) — survives into the active logical L1. This is the payoff
   * of modeling the port as a distinct {@link InterfaceType#PHYSICAL} interface: the L1 endpoint
   * lines up with a real interface, so the edge is not dropped.
   */
  @Test
  public void testLayer1TopologyApplied() throws IOException {
    Batfish batfish = getBatfish();
    // Realize post-processing first; topology is computed from the post-processed configs.
    batfish.loadConfigurations(batfish.getSnapshot());
    Layer1Topologies l1 = batfish.getTopologyProvider().getLayer1Topologies(batfish.getSnapshot());

    NodeInterfacePair r1Port = NodeInterfacePair.of("r1", "1/1/c1/1");
    NodeInterfacePair r2Eth = NodeInterfacePair.of("r2", "Ethernet1");
    // The edge between the physical port and r2's Ethernet1 is active in both directions (the
    // factory makes active edges bidirectional). If the port were not modeled, this would be
    // empty.
    assertThat(l1.getActiveLogicalL1().edgeStream().toList(), not(empty()));
    assertTrue(
        l1.getActiveLogicalL1()
            .edgeStream()
            .anyMatch(
                e ->
                    e.getNode1().asNodeInterfacePair().equals(r1Port)
                        && e.getNode2().asNodeInterfacePair().equals(r2Eth)));
  }

  /**
   * The cross-vendor L3 edge forms between r1's {@code to-r2} and r2's {@code Ethernet1}. Because
   * the L3 interface BINDs the port named in the Layer-1 topology, this edge is driven by the L1
   * topology (via {@code PointToPointComputer}), not only by the same-subnet fallback.
   */
  @Test
  public void testLayer3EdgeFormsAcrossVendors() throws IOException {
    Batfish batfish = getBatfish();
    batfish.loadConfigurations(batfish.getSnapshot());
    Topology l3 = batfish.getTopologyProvider().getInitialLayer3Topology(batfish.getSnapshot());
    assertThat(l3.getEdges(), hasItem(Edge.of("r1", "to-r2", "r2", "Ethernet1")));
    assertThat(l3.getEdges(), hasItem(Edge.of("r2", "Ethernet1", "r1", "to-r2")));
  }

  /** The eBGP session between r1 (AS 65001) and r2 (AS 65002) is established post-processing. */
  @Test
  public void testEbgpSessionEstablished() throws IOException {
    Batfish batfish = getBatfish();
    Map<String, Configuration> configs = batfish.loadConfigurations(batfish.getSnapshot());
    Map<Ip, Map<String, Set<String>>> ipOwners =
        batfish.getTopologyProvider().getInitialIpOwners(batfish.getSnapshot()).getIpVrfOwners();
    BgpTopology bgpTopo =
        BgpTopologyUtils.initBgpTopology(
            configs,
            ipOwners,
            false,
            batfish.getTopologyProvider().getInitialL3Adjacencies(batfish.getSnapshot()));
    ValueGraph<BgpPeerConfigId, BgpSessionProperties> g = bgpTopo.getGraph();
    BgpPeerConfigId r1Peer =
        new BgpPeerConfigId("r1", DEFAULT_VRF_NAME, Prefix.parse("10.0.0.1/32"), false);
    BgpPeerConfigId r2Peer =
        new BgpPeerConfigId("r2", DEFAULT_VRF_NAME, Prefix.parse("10.0.0.0/32"), false);
    assertTrue(g.nodes().contains(r1Peer));
    assertTrue(g.hasEdgeConnecting(r1Peer, r2Peer));
    assertFalse(g.adjacentNodes(r1Peer).isEmpty());
  }
}
