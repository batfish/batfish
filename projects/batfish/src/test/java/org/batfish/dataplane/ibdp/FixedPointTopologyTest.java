package org.batfish.dataplane.ibdp;

import static org.batfish.common.topology.TopologyUtil.computeLayer2Topology;
import static org.batfish.common.topology.TopologyUtil.computeLayer3Topology;
import static org.batfish.common.topology.TopologyUtil.computeRawLayer3Topology;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.BatfishLogger;
import org.batfish.common.plugin.DataPlanePlugin.ComputeDataPlaneResult;
import org.batfish.common.topology.Layer1Edge;
import org.batfish.common.topology.Layer1Topology;
import org.batfish.common.topology.Layer2Node;
import org.batfish.common.topology.Layer2Topology;
import org.batfish.common.topology.TopologyContainer;
import org.batfish.datamodel.BumTransportMethod;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpsecPeerConfigId;
import org.batfish.datamodel.IpsecPhase2Proposal;
import org.batfish.datamodel.IpsecSession;
import org.batfish.datamodel.IpsecStaticPeerConfig;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.SwitchportMode;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.VniSettings;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.ipsec.IpsecTopology;
import org.batfish.datamodel.ospf.OspfTopology;
import org.batfish.datamodel.vxlan.VxlanNode;
import org.batfish.datamodel.vxlan.VxlanTopology;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** Tests of correct computation of fixed-point topologies */
@ParametersAreNonnullByDefault
public final class FixedPointTopologyTest {

  private static final String E1_NAME = "E1";
  private static final String E12_NAME = "E12";
  private static final String E2_NAME = "E2";
  private static final String E21_NAME = "E21";
  private static final InterfaceAddress H1_ADDRESS = new InterfaceAddress("172.16.0.1/24");
  private static final String H1_NAME = "H1";
  private static final InterfaceAddress H2_ADDRESS = new InterfaceAddress("172.16.0.2/24");
  private static final String H2_NAME = "H2";
  private static final InterfaceAddress S1_ADDRESS = new InterfaceAddress("10.0.0.0/31");
  private static final String S1_NAME = "S1";
  private static final InterfaceAddress S2_ADDRESS = new InterfaceAddress("10.0.0.1/31");
  private static final String S2_NAME = "S2";
  private static final String SWP1_NAME = "SWP1";
  private static final String SWP2_NAME = "SWP2";
  private static final int UDP_PORT = 5555;
  private static final int VLAN = 2;
  private static final int VNI = 10002;

  private static final @Nonnull Layer1Topology generateVxlanLayer1Topology() {
    return new Layer1Topology(
        ImmutableList.of(
            new Layer1Edge(H1_NAME, E1_NAME, S1_NAME, SWP1_NAME),
            new Layer1Edge(S1_NAME, SWP1_NAME, H1_NAME, E1_NAME),
            new Layer1Edge(S1_NAME, E12_NAME, S2_NAME, E21_NAME),
            new Layer1Edge(S2_NAME, E21_NAME, S1_NAME, E12_NAME),
            new Layer1Edge(H2_NAME, E2_NAME, S2_NAME, SWP2_NAME),
            new Layer1Edge(S2_NAME, SWP2_NAME, H2_NAME, E2_NAME)));
  }

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private Configuration _h1;
  private Configuration _h2;
  private Configuration _s1;
  private Configuration _s2;

  private @Nonnull Map<String, Configuration> generateVxlanConfigs() {
    /*
     *       VLAN2    VNI10002    VLAN2
     *    E1  SWP1    E12  E21    SWP2 E2
     * H1 <======> S1 <======> S2 <=====> H2
     */
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    _s1 = cb.setHostname(S1_NAME).build();
    _s2 = cb.setHostname(S2_NAME).build();
    _h1 = cb.setHostname(H1_NAME).build();
    _h2 = cb.setHostname(H2_NAME).build();
    Vrf.Builder vb = nf.vrfBuilder().setName(Configuration.DEFAULT_VRF_NAME);
    Vrf h1Vrf = vb.setOwner(_h1).build();
    Vrf h2Vrf = vb.setOwner(_h2).build();
    Vrf s1Vrf = vb.setOwner(_s1).build();
    Vrf s2Vrf = vb.setOwner(_s2).build();
    Interface.Builder l3Builder =
        Interface.builder().setType(InterfaceType.PHYSICAL).setActive(true);
    l3Builder.setName(E1_NAME).setAddresses(H1_ADDRESS).setOwner(_h1).setVrf(h1Vrf).build();
    l3Builder.setName(E2_NAME).setAddresses(H2_ADDRESS).setOwner(_h2).setVrf(h2Vrf).build();
    l3Builder.setName(E12_NAME).setAddresses(S1_ADDRESS).setOwner(_s1).setVrf(s1Vrf).build();
    l3Builder.setName(E21_NAME).setAddresses(S2_ADDRESS).setOwner(_s2).setVrf(s2Vrf).build();
    Interface.Builder l2Builder =
        Interface.builder()
            .setType(InterfaceType.PHYSICAL)
            .setActive(true)
            .setAccessVlan(VLAN)
            .setSwitchport(true)
            .setSwitchportMode(SwitchportMode.ACCESS);
    l2Builder.setName(SWP1_NAME).setOwner(_s1).setVrf(s1Vrf).build();
    l2Builder.setName(SWP2_NAME).setOwner(_s2).setVrf(s2Vrf).build();

    VniSettings.Builder vsb =
        VniSettings.builder()
            .setBumTransportMethod(BumTransportMethod.UNICAST_FLOOD_GROUP)
            .setUdpPort(UDP_PORT)
            .setVlan(VLAN)
            .setVni(VNI);
    s1Vrf
        .getVniSettings()
        .put(
            VNI,
            vsb.setBumTransportIps(ImmutableSortedSet.of(S2_ADDRESS.getIp()))
                .setSourceAddress(S1_ADDRESS.getIp())
                .build());
    s2Vrf
        .getVniSettings()
        .put(
            VNI,
            vsb.setBumTransportIps(ImmutableSortedSet.of(S1_ADDRESS.getIp()))
                .setSourceAddress(S2_ADDRESS.getIp())
                .build());
    return ImmutableMap.of(H1_NAME, _h1, H2_NAME, _h2, S1_NAME, _s1, S2_NAME, _s2);
  }

  private TopologyContext getCallerTopologyContext(Map<String, Configuration> configs) {
    Layer1Topology l1 = generateVxlanLayer1Topology();
    Layer2Topology l2 = computeLayer2Topology(l1, VxlanTopology.EMPTY, configs);
    return TopologyContext.builder()
        .setLayer1LogicalTopology(Optional.of(l1))
        .setLayer2Topology(Optional.of(l2))
        .setLayer3Topology(
            computeLayer3Topology(
                computeRawLayer3Topology(Optional.of(l1), Optional.of(l2), configs),
                ImmutableSet.of(),
                configs))
        .setOspfTopology(OspfTopology.EMPTY)
        .setRawLayer1PhysicalTopology(Optional.of(l1))
        .build();
  }

  @Test
  public void testFixedPointVxlanTopology() {
    Map<String, Configuration> configs = generateVxlanConfigs();
    IncrementalBdpEngine engine =
        new IncrementalBdpEngine(
            new IncrementalDataPlaneSettings(),
            new BatfishLogger(BatfishLogger.LEVELSTR_DEBUG, false));
    TopologyContext callerTopologyContext = getCallerTopologyContext(configs);

    // Initially there should be no VXLAN tunnel
    assertThat(callerTopologyContext.getVxlanTopology().getGraph().edges(), empty());

    // Initially, the two host interfaces should NOT be layer-2 adjacent
    assertFalse(
        callerTopologyContext
            .getLayer2Topology()
            .get()
            .inSameBroadcastDomain(
                new Layer2Node(H1_NAME, E1_NAME, null), new Layer2Node(H2_NAME, E2_NAME, null)));

    // Initially, the two host-interfaces should NOT be layer-3 adjacent
    // The two host interfaces should be layer-3 adjacent
    assertThat(
        callerTopologyContext.getLayer3Topology().getEdges(),
        not(hasItem(Edge.of(H1_NAME, E1_NAME, H2_NAME, E2_NAME))));

    ComputeDataPlaneResult dp =
        engine.computeDataPlane(generateVxlanConfigs(), callerTopologyContext, ImmutableSet.of());
    TopologyContainer topologies = dp._topologies;

    // There should be an active VXLAN tunnel
    assertThat(
        topologies.getVxlanTopology().getGraph().edges(),
        contains(EndpointPair.unordered(new VxlanNode(S1_NAME, VNI), new VxlanNode(S2_NAME, VNI))));

    // The two host interfaces should be in the same broadcast domain due to VXLAN tunnel
    assertTrue(
        topologies
            .getLayer2Topology()
            .get()
            .inSameBroadcastDomain(
                new Layer2Node(H1_NAME, E1_NAME, null), new Layer2Node(H2_NAME, E2_NAME, null)));

    // The two host interfaces should be layer-3 adjacent
    assertThat(
        topologies.getLayer3Topology().getEdges(),
        hasItem(Edge.of(H1_NAME, E1_NAME, H2_NAME, E2_NAME)));
  }

  @Test
  public void testFixedPointIpsecTopology() {
    // the setup network contains bidirectional underlay edges between host1:interface1 and
    // host2:interface2, and also overlay edges between host1:Tunnel1 and host2:Tunnel2
    IpsecStaticPeerConfig ipsecPeerConfig1 =
        IpsecStaticPeerConfig.builder()
            .setSourceInterface("Interface1")
            .setTunnelInterface("Tunnel1")
            .build();
    IpsecStaticPeerConfig ipsecPeerConfig2 =
        IpsecStaticPeerConfig.builder()
            .setSourceInterface("Interface2")
            .setTunnelInterface("Tunnel2")
            .build();
    MutableValueGraph<IpsecPeerConfigId, IpsecSession> graph =
        ValueGraphBuilder.directed().allowsSelfLoops(false).build();
    IpsecSession establishedSession =
        IpsecSession.builder().setNegotiatedIpsecP2Proposal(new IpsecPhase2Proposal()).build();
    // populate IPsec topology
    graph.putEdgeValue(
        new IpsecPeerConfigId("peer1", "host1"),
        new IpsecPeerConfigId("peer2", "host2"),
        establishedSession);
    graph.putEdgeValue(
        new IpsecPeerConfigId("peer2", "host2"),
        new IpsecPeerConfigId("peer1", "host1"),
        establishedSession);

    NetworkFactory nf = new NetworkFactory();

    Configuration host1 =
        nf.configurationBuilder()
            .setHostname("host1")
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .build();
    Configuration host2 =
        nf.configurationBuilder()
            .setHostname("host2")
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .build();
    Vrf vrf1 = nf.vrfBuilder().setOwner(host1).build();
    Vrf vrf2 = nf.vrfBuilder().setOwner(host2).build();
    Interface iface1 =
        nf.interfaceBuilder()
            .setName("Interface1")
            .setOwner(host1)
            .setVrf(vrf1)
            .setAddress(new InterfaceAddress(Ip.parse("1.1.1.1"), 24))
            .build();
    Interface tunnel1 =
        nf.interfaceBuilder()
            .setName("Tunnel1")
            .setOwner(host1)
            .setVrf(vrf1)
            .setAddress(new InterfaceAddress(Ip.parse("11.12.13.1"), 24))
            .build();
    tunnel1.setInterfaceType(InterfaceType.TUNNEL);
    Interface iface2 =
        nf.interfaceBuilder()
            .setName("Interface2")
            .setOwner(host2)
            .setVrf(vrf2)
            .setAddress(new InterfaceAddress(Ip.parse("1.1.1.2"), 24))
            .build();
    Interface tunnel2 =
        nf.interfaceBuilder()
            .setName("Tunnel2")
            .setOwner(host2)
            .setVrf(vrf2)
            .setAddress(new InterfaceAddress(Ip.parse("11.12.13.2"), 24))
            .build();
    tunnel2.setInterfaceType(InterfaceType.TUNNEL);
    host1.setIpsecPeerConfigs(ImmutableSortedMap.of("peer1", ipsecPeerConfig1));
    host2.setIpsecPeerConfigs(ImmutableSortedMap.of("peer2", ipsecPeerConfig2));

    Topology layer3Topology = new Topology(ImmutableSortedSet.of(new Edge(iface1, iface2)));

    TopologyContext topologyContext =
        TopologyContext.builder()
            .setIpsecTopology(new IpsecTopology(graph))
            .setLayer3Topology(layer3Topology)
            .build();
    IncrementalBdpEngine engine =
        new IncrementalBdpEngine(
            new IncrementalDataPlaneSettings(),
            new BatfishLogger(BatfishLogger.LEVELSTR_DEBUG, false));

    ComputeDataPlaneResult dp =
        engine.computeDataPlane(
            ImmutableMap.of("host1", host1, "host2", host2), topologyContext, ImmutableSet.of());
    TopologyContainer topologies = dp._topologies;

    assertThat(topologies, instanceOf(TopologyContext.class));
    TopologyContext fixPointTopoContext = (TopologyContext) topologies;
    // Ipsec topology should not change after fixed point since we are not pruning based on
    // reachability
    assertThat(fixPointTopoContext.getIpsecTopology(), equalTo(topologyContext.getIpsecTopology()));
    // Layer 3 topology should now contain the overlay IPsec tunnel edges
    assertThat(
        fixPointTopoContext.getLayer3Topology().getEdges(),
        equalTo(
            ImmutableSortedSet.of(
                new Edge(iface1, iface2),
                new Edge(iface2, iface1),
                new Edge(tunnel1, tunnel2),
                new Edge(tunnel2, tunnel1))));
  }
}
