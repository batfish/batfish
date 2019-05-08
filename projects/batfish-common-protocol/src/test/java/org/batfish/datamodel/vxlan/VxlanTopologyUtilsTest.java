package org.batfish.datamodel.vxlan;

import static org.batfish.datamodel.vxlan.VxlanTopologyUtils.addVniEdge;
import static org.batfish.datamodel.vxlan.VxlanTopologyUtils.addVniEdges;
import static org.batfish.datamodel.vxlan.VxlanTopologyUtils.buildVxlanNode;
import static org.batfish.datamodel.vxlan.VxlanTopologyUtils.compatibleVniSettings;
import static org.batfish.datamodel.vxlan.VxlanTopologyUtils.initVniVrfAssociations;
import static org.batfish.datamodel.vxlan.VxlanTopologyUtils.initVrfHostnameMap;
import static org.batfish.datamodel.vxlan.VxlanTopologyUtils.initialVxlanTopology;
import static org.batfish.datamodel.vxlan.VxlanTopologyUtils.prunedVxlanTopology;
import static org.batfish.datamodel.vxlan.VxlanTopologyUtils.vxlanFlowDelivered;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.function.Function;
import java.util.stream.Stream;
import org.batfish.common.plugin.TracerouteEngine;
import org.batfish.datamodel.BumTransportMethod;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.NetworkConfigurations;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.VniSettings;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.flow.FirewallSessionTraceInfo;
import org.batfish.datamodel.flow.Hop;
import org.batfish.datamodel.flow.Trace;
import org.batfish.datamodel.flow.TraceAndReverseFlow;
import org.batfish.datamodel.pojo.Node;
import org.junit.Before;
import org.junit.Test;

/** Test of {@link VxlanTopologyUtils}. */
public final class VxlanTopologyUtilsTest {

  private static class TestTracerouteEngine implements TracerouteEngine {

    private final Map<String, List<Trace>> _results;

    private TestTracerouteEngine(Map<String, TestTracerouteEngineResult> results) {
      _results =
          results.entrySet().stream()
              .collect(
                  ImmutableMap.toImmutableMap(
                      Entry::getKey,
                      resultEntry ->
                          ImmutableList.of(
                              new Trace(
                                  resultEntry.getValue()._accept
                                      ? FlowDisposition.ACCEPTED
                                      : FlowDisposition.DENIED_OUT,
                                  resultEntry.getValue()._addHop
                                      ? ImmutableList.of(
                                          new Hop(
                                              new Node(resultEntry.getValue()._receivingNode),
                                              ImmutableList.of()))
                                      : ImmutableList.of()))));
    }

    @Override
    public SortedMap<Flow, List<Trace>> computeTraces(Set<Flow> flows, boolean ignoreFilters) {
      return flows.stream()
          .collect(
              ImmutableSortedMap.toImmutableSortedMap(
                  Comparator.naturalOrder(),
                  Function.<Flow>identity(),
                  flow -> _results.get(flow.getIngressNode())));
    }

    @Override
    public SortedMap<Flow, List<TraceAndReverseFlow>> computeTracesAndReverseFlows(
        Set<Flow> flows, Set<FirewallSessionTraceInfo> sessions, boolean ignoreFilters) {
      throw new UnsupportedOperationException();
    }
  }

  private static class TestTracerouteEngineResult {
    private final boolean _accept;
    private final boolean _addHop;
    private final String _receivingNode;

    private TestTracerouteEngineResult(String receivingNode, boolean accept, boolean addHop) {
      _receivingNode = receivingNode;
      _accept = accept;
      _addHop = addHop;
    }
  }

  private static final String IFACE_NAME = "i0";
  private static final Ip MULTICAST_GROUP = Ip.parse("224.0.0.1");
  private static final String NODE1 = "n1";
  private static final String NODE2 = "n2";
  private static final Ip SRC_IP1 = Ip.parse("10.0.0.0");
  private static final Ip SRC_IP2 = Ip.parse("10.0.0.1");
  private static final int UDP_PORT = 5555;
  private static final int VLAN1 = 1;
  private static final int VLAN2 = 2;
  private static final int VNI = 5000;

  private Configuration _c1;
  private Configuration _c2;
  private Vrf _v1;
  private Vrf _v2;

  private Map<String, Configuration> compatibleVxlanConfigs() {
    Interface.Builder ib =
        Interface.builder().setType(InterfaceType.PHYSICAL).setName(IFACE_NAME).setActive(true);
    ib.setAddresses(new InterfaceAddress(SRC_IP1, 31)).setOwner(_c1).setVrf(_v1).build();
    ib.setAddresses(new InterfaceAddress(SRC_IP2, 31)).setOwner(_c2).setVrf(_v2).build();
    VniSettings.Builder vsb =
        VniSettings.builder()
            .setBumTransportMethod(BumTransportMethod.UNICAST_FLOOD_GROUP)
            .setUdpPort(UDP_PORT)
            .setVlan(VLAN1)
            .setVni(VNI);
    _v1.getVniSettings()
        .put(
            VNI,
            vsb.setBumTransportIps(ImmutableSortedSet.of(SRC_IP2))
                .setSourceAddress(SRC_IP1)
                .build());
    _v2.getVniSettings()
        .put(
            VNI,
            vsb.setBumTransportIps(ImmutableSortedSet.of(SRC_IP1))
                .setSourceAddress(SRC_IP2)
                .build());
    return ImmutableMap.of(NODE1, _c1, NODE2, _c2);
  }

  @Before
  public void setup() {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    _c1 = cb.setHostname(NODE1).build();
    _c2 = cb.setHostname(NODE2).build();
    Vrf.Builder vb = nf.vrfBuilder().setName(Configuration.DEFAULT_VRF_NAME);
    _v1 = vb.setOwner(_c1).build();
    _v2 = vb.setOwner(_c2).build();
  }

  @Test
  public void testAddVniEdge() {
    Map<String, Configuration> configurations = ImmutableMap.of(NODE1, _c1, NODE2, _c2);
    MutableGraph<VxlanNode> graph = GraphBuilder.undirected().allowsSelfLoops(false).build();
    Map<Vrf, String> vrfHostnames = initVrfHostnameMap(configurations);
    VniSettings.Builder vniSettingsBuilder =
        VniSettings.builder()
            .setBumTransportIps(ImmutableSortedSet.of(MULTICAST_GROUP))
            .setBumTransportMethod(BumTransportMethod.MULTICAST_GROUP)
            .setUdpPort(UDP_PORT)
            .setVni(VNI);
    VniSettings vniSettingsTail =
        vniSettingsBuilder.setSourceAddress(SRC_IP1).setVlan(VLAN1).setVni(VNI).build();
    _v1.setVniSettings(ImmutableSortedMap.of(VNI, vniSettingsTail));
    _v2.setVniSettings(
        ImmutableSortedMap.of(
            VNI, vniSettingsBuilder.setSourceAddress(SRC_IP2).setVlan(VLAN2).setVni(VNI).build()));
    addVniEdge(graph, vrfHostnames, VNI, _v1, vniSettingsTail, _v2);
    Set<EndpointPair<VxlanNode>> edges = graph.edges();

    assertThat(
        edges,
        equalTo(
            ImmutableSet.of(
                EndpointPair.unordered(
                    VxlanNode.builder().setHostname(NODE2).setVni(VNI).build(),
                    VxlanNode.builder().setHostname(NODE1).setVni(VNI).build()))));
  }

  @Test
  public void testAddVniEdgeIncompatible() {
    Map<String, Configuration> configurations = ImmutableMap.of(NODE1, _c1, NODE2, _c2);
    MutableGraph<VxlanNode> graph = GraphBuilder.undirected().allowsSelfLoops(false).build();
    Map<Vrf, String> vrfHostnames = initVrfHostnameMap(configurations);
    VniSettings.Builder vniSettingsBuilder =
        VniSettings.builder()
            .setBumTransportIps(ImmutableSortedSet.of(MULTICAST_GROUP))
            .setBumTransportMethod(BumTransportMethod.MULTICAST_GROUP)
            .setUdpPort(UDP_PORT)
            .setVni(VNI);
    VniSettings vniSettingsTail =
        vniSettingsBuilder.setSourceAddress(SRC_IP1).setVlan(VLAN1).build();
    _v1.setVniSettings(ImmutableSortedMap.of(VNI, vniSettingsTail));
    _v2.setVniSettings(
        ImmutableSortedMap.of(
            VNI, vniSettingsBuilder.setSourceAddress(SRC_IP1).setVlan(VLAN2).build()));
    addVniEdge(graph, vrfHostnames, VNI, _v1, vniSettingsTail, _v2);
    Set<EndpointPair<VxlanNode>> edges = graph.edges();

    assertThat(edges, empty());
  }

  @Test
  public void testAddVniEdges() {
    Map<String, Configuration> configurations = ImmutableMap.of(NODE1, _c1, NODE2, _c2);
    MutableGraph<VxlanNode> graph = GraphBuilder.undirected().allowsSelfLoops(false).build();
    Map<Vrf, String> vrfHostnames = initVrfHostnameMap(configurations);
    VniSettings.Builder vniSettingsBuilder =
        VniSettings.builder()
            .setBumTransportIps(ImmutableSortedSet.of(MULTICAST_GROUP))
            .setBumTransportMethod(BumTransportMethod.MULTICAST_GROUP)
            .setUdpPort(UDP_PORT)
            .setVni(VNI);
    VniSettings vniSettingsTail =
        vniSettingsBuilder.setSourceAddress(SRC_IP1).setVlan(VLAN1).build();
    _v1.setVniSettings(ImmutableSortedMap.of(VNI, vniSettingsTail));
    _v2.setVniSettings(
        ImmutableSortedMap.of(
            VNI, vniSettingsBuilder.setSourceAddress(SRC_IP2).setVlan(VLAN2).build()));
    addVniEdges(graph, vrfHostnames, VNI, ImmutableList.of(_v1, _v2));

    VxlanNode nodeTail = VxlanNode.builder().setHostname(NODE1).setVni(VNI).build();
    VxlanNode nodeHead = VxlanNode.builder().setHostname(NODE2).setVni(VNI).build();

    assertThat(graph.edges(), equalTo(ImmutableSet.of(EndpointPair.unordered(nodeTail, nodeHead))));
  }

  @Test
  public void testBuildVxlanNode() {
    Map<Vrf, String> vrfHostnames = ImmutableMap.of(_v1, NODE1);
    VniSettings vniSettings =
        VniSettings.builder()
            .setSourceAddress(SRC_IP1)
            .setVlan(VLAN1)
            .setVni(VNI)
            .setBumTransportMethod(BumTransportMethod.MULTICAST_GROUP)
            .build();
    assertThat(
        buildVxlanNode(vrfHostnames, _v1, vniSettings),
        equalTo(VxlanNode.builder().setHostname(NODE1).setVni(VNI).build()));
  }

  @Test
  public void testCompatibleVniSettingsMismatchBumTransportHeadFloodGroup() {
    VniSettings vniSettingsTail =
        VniSettings.builder()
            .setBumTransportIps(ImmutableSortedSet.of(SRC_IP2))
            .setBumTransportMethod(BumTransportMethod.UNICAST_FLOOD_GROUP)
            .setSourceAddress(SRC_IP1)
            .setUdpPort(UDP_PORT)
            .setVlan(VLAN1)
            .setVni(VNI)
            .build();
    VniSettings vniSettingsHead =
        VniSettings.builder()
            .setBumTransportIps(ImmutableSortedSet.of(SRC_IP2))
            .setBumTransportMethod(BumTransportMethod.UNICAST_FLOOD_GROUP)
            .setSourceAddress(SRC_IP2)
            .setUdpPort(UDP_PORT)
            .setVlan(VLAN2)
            .setVni(VNI)
            .build();

    assertThat(compatibleVniSettings(vniSettingsTail, vniSettingsHead), equalTo(false));
  }

  @Test
  public void testCompatibleVniSettingsMismatchBumTransportMethod() {
    VniSettings vniSettingsTail =
        VniSettings.builder()
            .setBumTransportIps(ImmutableSortedSet.of(MULTICAST_GROUP))
            .setBumTransportMethod(BumTransportMethod.MULTICAST_GROUP)
            .setSourceAddress(SRC_IP1)
            .setUdpPort(UDP_PORT)
            .setVlan(VLAN1)
            .setVni(VNI)
            .build();
    VniSettings vniSettingsHead =
        VniSettings.builder()
            .setBumTransportIps(ImmutableSortedSet.of(MULTICAST_GROUP))
            .setBumTransportMethod(BumTransportMethod.UNICAST_FLOOD_GROUP)
            .setSourceAddress(SRC_IP2)
            .setUdpPort(UDP_PORT)
            .setVlan(VLAN2)
            .setVni(VNI)
            .build();

    assertThat(compatibleVniSettings(vniSettingsTail, vniSettingsHead), equalTo(false));
  }

  @Test
  public void testCompatibleVniSettingsMismatchBumTransportMulticastGroup() {
    VniSettings vniSettingsTail =
        VniSettings.builder()
            .setBumTransportIps(ImmutableSortedSet.of(MULTICAST_GROUP))
            .setBumTransportMethod(BumTransportMethod.MULTICAST_GROUP)
            .setSourceAddress(SRC_IP1)
            .setUdpPort(UDP_PORT)
            .setVlan(VLAN1)
            .setVni(VNI)
            .build();
    VniSettings vniSettingsHead =
        VniSettings.builder()
            .setBumTransportIps(ImmutableSortedSet.of(Ip.parse("224.0.0.5")))
            .setBumTransportMethod(BumTransportMethod.MULTICAST_GROUP)
            .setSourceAddress(SRC_IP2)
            .setUdpPort(UDP_PORT)
            .setVlan(VLAN2)
            .setVni(VNI)
            .build();

    assertThat(compatibleVniSettings(vniSettingsTail, vniSettingsHead), equalTo(false));
  }

  @Test
  public void testCompatibleVniSettingsMismatchBumTransportMulticastGroupUnicast() {
    VniSettings vniSettingsTail =
        VniSettings.builder()
            .setBumTransportIps(ImmutableSortedSet.of(SRC_IP2))
            .setBumTransportMethod(BumTransportMethod.MULTICAST_GROUP)
            .setSourceAddress(SRC_IP1)
            .setUdpPort(UDP_PORT)
            .setVlan(VLAN1)
            .setVni(VNI)
            .build();
    VniSettings vniSettingsHead =
        VniSettings.builder()
            .setBumTransportIps(ImmutableSortedSet.of(SRC_IP1))
            .setBumTransportMethod(BumTransportMethod.MULTICAST_GROUP)
            .setSourceAddress(SRC_IP2)
            .setUdpPort(UDP_PORT)
            .setVlan(VLAN2)
            .setVni(VNI)
            .build();

    assertThat(compatibleVniSettings(vniSettingsTail, vniSettingsHead), equalTo(false));
  }

  @Test
  public void testCompatibleVniSettingsMismatchBumTransportTailFloodGroup() {
    VniSettings vniSettingsTail =
        VniSettings.builder()
            .setBumTransportIps(ImmutableSortedSet.of(SRC_IP1))
            .setBumTransportMethod(BumTransportMethod.UNICAST_FLOOD_GROUP)
            .setSourceAddress(SRC_IP1)
            .setUdpPort(UDP_PORT)
            .setVlan(VLAN1)
            .setVni(VNI)
            .build();
    VniSettings vniSettingsHead =
        VniSettings.builder()
            .setBumTransportIps(ImmutableSortedSet.of(SRC_IP1))
            .setBumTransportMethod(BumTransportMethod.UNICAST_FLOOD_GROUP)
            .setSourceAddress(SRC_IP2)
            .setUdpPort(UDP_PORT)
            .setVlan(VLAN2)
            .setVni(VNI)
            .build();

    assertThat(compatibleVniSettings(vniSettingsTail, vniSettingsHead), equalTo(false));
  }

  @Test
  public void testCompatibleVniSettingsMismatchDifferentUdpPort() {
    VniSettings vniSettingsTail =
        VniSettings.builder()
            .setBumTransportIps(ImmutableSortedSet.of(MULTICAST_GROUP))
            .setBumTransportMethod(BumTransportMethod.MULTICAST_GROUP)
            .setSourceAddress(SRC_IP1)
            .setUdpPort(UDP_PORT)
            .setVlan(VLAN1)
            .setVni(VNI)
            .build();
    VniSettings vniSettingsHead =
        VniSettings.builder()
            .setBumTransportIps(ImmutableSortedSet.of(MULTICAST_GROUP))
            .setBumTransportMethod(BumTransportMethod.MULTICAST_GROUP)
            .setSourceAddress(SRC_IP2)
            .setUdpPort(UDP_PORT + 1)
            .setVlan(VLAN2)
            .setVni(VNI)
            .build();

    assertThat(compatibleVniSettings(vniSettingsTail, vniSettingsHead), equalTo(false));
  }

  @Test
  public void testCompatibleVniSettingsMismatchNoVlanHead() {
    VniSettings vniSettingsTail =
        VniSettings.builder()
            .setBumTransportIps(ImmutableSortedSet.of(MULTICAST_GROUP))
            .setBumTransportMethod(BumTransportMethod.MULTICAST_GROUP)
            .setSourceAddress(SRC_IP1)
            .setUdpPort(UDP_PORT)
            .setVlan(VLAN1)
            .setVni(VNI)
            .build();
    VniSettings vniSettingsHead =
        VniSettings.builder()
            .setBumTransportIps(ImmutableSortedSet.of(MULTICAST_GROUP))
            .setBumTransportMethod(BumTransportMethod.MULTICAST_GROUP)
            .setSourceAddress(SRC_IP2)
            .setUdpPort(UDP_PORT)
            .setVni(VNI)
            .build();

    assertThat(compatibleVniSettings(vniSettingsTail, vniSettingsHead), equalTo(false));
  }

  @Test
  public void testCompatibleVniSettingsMismatchNoVlanTail() {
    VniSettings vniSettingsTail =
        VniSettings.builder()
            .setBumTransportIps(ImmutableSortedSet.of(MULTICAST_GROUP))
            .setBumTransportMethod(BumTransportMethod.MULTICAST_GROUP)
            .setSourceAddress(SRC_IP1)
            .setUdpPort(UDP_PORT)
            .setVni(VNI)
            .build();
    VniSettings vniSettingsHead =
        VniSettings.builder()
            .setBumTransportIps(ImmutableSortedSet.of(MULTICAST_GROUP))
            .setBumTransportMethod(BumTransportMethod.MULTICAST_GROUP)
            .setSourceAddress(SRC_IP2)
            .setUdpPort(UDP_PORT)
            .setVlan(VLAN2)
            .setVni(VNI)
            .build();

    assertThat(compatibleVniSettings(vniSettingsTail, vniSettingsHead), equalTo(false));
  }

  @Test
  public void testCompatibleVniSettingsMismatchNullHeadSourceAddress() {
    VniSettings vniSettingsTail =
        VniSettings.builder()
            .setBumTransportIps(ImmutableSortedSet.of(MULTICAST_GROUP))
            .setBumTransportMethod(BumTransportMethod.MULTICAST_GROUP)
            .setSourceAddress(SRC_IP1)
            .setUdpPort(UDP_PORT)
            .setVlan(VLAN1)
            .setVni(VNI)
            .build();
    VniSettings vniSettingsHead =
        VniSettings.builder()
            .setBumTransportIps(ImmutableSortedSet.of(MULTICAST_GROUP))
            .setBumTransportMethod(BumTransportMethod.MULTICAST_GROUP)
            .setSourceAddress(null)
            .setUdpPort(UDP_PORT)
            .setVlan(VLAN2)
            .setVni(VNI)
            .build();

    assertThat(compatibleVniSettings(vniSettingsTail, vniSettingsHead), equalTo(false));
  }

  @Test
  public void testCompatibleVniSettingsMismatchNullTailSourceAddress() {
    VniSettings vniSettingsTail =
        VniSettings.builder()
            .setBumTransportIps(ImmutableSortedSet.of(MULTICAST_GROUP))
            .setBumTransportMethod(BumTransportMethod.MULTICAST_GROUP)
            .setSourceAddress(null)
            .setUdpPort(UDP_PORT)
            .setVlan(VLAN1)
            .setVni(VNI)
            .build();
    VniSettings vniSettingsHead =
        VniSettings.builder()
            .setBumTransportIps(ImmutableSortedSet.of(MULTICAST_GROUP))
            .setBumTransportMethod(BumTransportMethod.MULTICAST_GROUP)
            .setSourceAddress(SRC_IP2)
            .setUdpPort(UDP_PORT)
            .setVlan(VLAN2)
            .setVni(VNI)
            .build();

    assertThat(compatibleVniSettings(vniSettingsTail, vniSettingsHead), equalTo(false));
  }

  @Test
  public void testCompatibleVniSettingsMismatchSameSourceAddress() {
    VniSettings vniSettingsTail =
        VniSettings.builder()
            .setBumTransportIps(ImmutableSortedSet.of(MULTICAST_GROUP))
            .setBumTransportMethod(BumTransportMethod.MULTICAST_GROUP)
            .setSourceAddress(SRC_IP1)
            .setUdpPort(UDP_PORT)
            .setVlan(VLAN1)
            .setVni(VNI)
            .build();
    VniSettings vniSettingsHead =
        VniSettings.builder()
            .setBumTransportIps(ImmutableSortedSet.of(MULTICAST_GROUP))
            .setBumTransportMethod(BumTransportMethod.MULTICAST_GROUP)
            .setSourceAddress(SRC_IP1)
            .setUdpPort(UDP_PORT)
            .setVlan(VLAN2)
            .setVni(VNI)
            .build();

    assertThat(compatibleVniSettings(vniSettingsTail, vniSettingsHead), equalTo(false));
  }

  @Test
  public void testCompatibleVniSettingsMulticast() {
    VniSettings vniSettingsTail =
        VniSettings.builder()
            .setBumTransportIps(ImmutableSortedSet.of(MULTICAST_GROUP))
            .setBumTransportMethod(BumTransportMethod.MULTICAST_GROUP)
            .setSourceAddress(SRC_IP1)
            .setUdpPort(UDP_PORT)
            .setVlan(VLAN1)
            .setVni(VNI)
            .build();
    VniSettings vniSettingsHead =
        VniSettings.builder()
            .setBumTransportIps(ImmutableSortedSet.of(MULTICAST_GROUP))
            .setBumTransportMethod(BumTransportMethod.MULTICAST_GROUP)
            .setSourceAddress(SRC_IP2)
            .setUdpPort(UDP_PORT)
            .setVlan(VLAN2)
            .setVni(VNI)
            .build();

    assertThat(compatibleVniSettings(vniSettingsTail, vniSettingsHead), equalTo(true));
  }

  @Test
  public void testCompatibleVniSettingsUnicast() {
    VniSettings vniSettingsTail =
        VniSettings.builder()
            .setBumTransportIps(ImmutableSortedSet.of(SRC_IP2))
            .setBumTransportMethod(BumTransportMethod.UNICAST_FLOOD_GROUP)
            .setSourceAddress(SRC_IP1)
            .setUdpPort(UDP_PORT)
            .setVlan(VLAN1)
            .setVni(VNI)
            .build();
    VniSettings vniSettingsHead =
        VniSettings.builder()
            .setBumTransportIps(ImmutableSortedSet.of(SRC_IP1))
            .setBumTransportMethod(BumTransportMethod.UNICAST_FLOOD_GROUP)
            .setSourceAddress(SRC_IP2)
            .setUdpPort(UDP_PORT)
            .setVlan(VLAN2)
            .setVni(VNI)
            .build();

    assertThat(compatibleVniSettings(vniSettingsTail, vniSettingsHead), equalTo(true));
  }

  @Test
  public void testInitialVxlanTopology() {
    Map<String, Configuration> configurations = ImmutableMap.of(NODE1, _c1, NODE2, _c2);
    VniSettings.Builder vniSettingsBuilder =
        VniSettings.builder()
            .setBumTransportIps(ImmutableSortedSet.of(MULTICAST_GROUP))
            .setBumTransportMethod(BumTransportMethod.MULTICAST_GROUP)
            .setUdpPort(UDP_PORT)
            .setVni(VNI);
    VniSettings vniSettingsTail =
        vniSettingsBuilder.setSourceAddress(SRC_IP1).setVlan(VLAN1).build();
    _v1.setVniSettings(ImmutableSortedMap.of(VNI, vniSettingsTail));
    _v2.setVniSettings(
        ImmutableSortedMap.of(
            VNI, vniSettingsBuilder.setSourceAddress(SRC_IP2).setVlan(VLAN2).build()));

    VxlanNode nodeTail = VxlanNode.builder().setHostname(NODE1).setVni(VNI).build();
    VxlanNode nodeHead = VxlanNode.builder().setHostname(NODE2).setVni(VNI).build();

    assertThat(
        initialVxlanTopology(configurations).getGraph().edges(),
        equalTo(ImmutableSet.of(EndpointPair.unordered(nodeTail, nodeHead))));
  }

  @Test
  public void testInitVniVrfAssociations() {
    Map<String, Configuration> configurations = ImmutableMap.of(NODE1, _c1, NODE2, _c2);
    VniSettings.Builder b =
        VniSettings.builder().setBumTransportMethod(BumTransportMethod.MULTICAST_GROUP).setVni(VNI);
    _v1.setVniSettings(ImmutableSortedMap.of(VNI, b.build()));
    _v2.setVniSettings(ImmutableSortedMap.of(VNI, b.build()));

    assertThat(
        initVniVrfAssociations(configurations),
        equalTo(ImmutableMap.of(VNI, ImmutableList.of(_v1, _v2))));
  }

  @Test
  public void testInitVrfHostnameMap() {
    Map<String, Configuration> configurations = ImmutableMap.of(NODE1, _c1, NODE2, _c2);
    Map<Vrf, String> vrfHostnameMap = initVrfHostnameMap(configurations);

    // complex assertions because returned map is an IdentityHashMap
    assertThat(vrfHostnameMap.get(_v1), equalTo(NODE1));
    assertThat(vrfHostnameMap.get(_v2), equalTo(NODE2));
    assertThat(vrfHostnameMap, aMapWithSize(2));
  }

  @Test
  public void testPrunedVxlanTopologyDiscard() {
    MutableGraph<VxlanNode> graph = GraphBuilder.undirected().allowsSelfLoops(false).build();
    graph.putEdge(new VxlanNode(NODE1, VNI), new VxlanNode(NODE2, VNI));
    VxlanTopology initial = new VxlanTopology(graph);

    // no reachability
    assertThat(
        prunedVxlanTopology(
                initial,
                compatibleVxlanConfigs(),
                new TestTracerouteEngine(
                    ImmutableMap.of(
                        NODE1,
                        new TestTracerouteEngineResult(NODE2, false, true),
                        NODE2,
                        new TestTracerouteEngineResult(NODE1, false, true))))
            .getGraph()
            .edges(),
        empty());
  }

  @Test
  public void testPrunedVxlanTopologyKeep() {
    MutableGraph<VxlanNode> graph = GraphBuilder.undirected().allowsSelfLoops(false).build();
    graph.putEdge(new VxlanNode(NODE1, VNI), new VxlanNode(NODE2, VNI));
    VxlanTopology initial = new VxlanTopology(graph);

    assertEquals(
        prunedVxlanTopology(
            initial,
            compatibleVxlanConfigs(),
            new TestTracerouteEngine(
                ImmutableMap.of(
                    NODE1,
                    new TestTracerouteEngineResult(NODE2, true, true),
                    NODE2,
                    new TestTracerouteEngineResult(NODE1, true, true)))),
        initial);
  }

  @Test
  public void testReachableEdgeBothDirections() {
    assertTrue(
        VxlanTopologyUtils.reachableEdge(
            EndpointPair.unordered(new VxlanNode(NODE1, VNI), new VxlanNode(NODE2, VNI)),
            NetworkConfigurations.of(compatibleVxlanConfigs()),
            new TestTracerouteEngine(
                ImmutableMap.of(
                    NODE1,
                    new TestTracerouteEngineResult(NODE2, true, true),
                    NODE2,
                    new TestTracerouteEngineResult(NODE1, true, true)))));
  }

  @Test
  public void testReachableEdgeFirstDirectionOnly() {
    assertFalse(
        VxlanTopologyUtils.reachableEdge(
            EndpointPair.unordered(new VxlanNode(NODE1, VNI), new VxlanNode(NODE2, VNI)),
            NetworkConfigurations.of(compatibleVxlanConfigs()),
            new TestTracerouteEngine(
                ImmutableMap.of(
                    NODE1,
                    new TestTracerouteEngineResult(NODE2, true, true),
                    NODE2,
                    new TestTracerouteEngineResult(NODE1, false, true)))));
  }

  @Test
  public void testReachableEdgeMulticast() {
    // TODO: passing case when multicast is suppported
    Map<String, Configuration> configs = compatibleVxlanConfigs();
    // make VNI settings use multicast
    Stream.of(configs.get(NODE1), configs.get(NODE2))
        .forEach(
            config ->
                config
                    .getDefaultVrf()
                    .getVniSettings()
                    .compute(
                        VNI,
                        (vni, vniSettings) ->
                            VniSettings.builder()
                                .setBumTransportMethod(BumTransportMethod.MULTICAST_GROUP)
                                .setSourceAddress(vniSettings.getSourceAddress())
                                .setUdpPort(vniSettings.getUdpPort())
                                .setVlan(vniSettings.getVlan())
                                .setVni(vniSettings.getVni())
                                .setBumTransportIps(vniSettings.getBumTransportIps())
                                .build()));

    assertFalse(
        VxlanTopologyUtils.reachableEdge(
            EndpointPair.unordered(new VxlanNode(NODE1, VNI), new VxlanNode(NODE2, VNI)),
            NetworkConfigurations.of(configs),
            new TestTracerouteEngine(
                ImmutableMap.of(
                    NODE1,
                    new TestTracerouteEngineResult(NODE2, true, true),
                    NODE2,
                    new TestTracerouteEngineResult(NODE1, true, true)))));
  }

  @Test
  public void testReachableEdgeSecondDirectionOnly() {
    assertFalse(
        VxlanTopologyUtils.reachableEdge(
            EndpointPair.unordered(new VxlanNode(NODE1, VNI), new VxlanNode(NODE2, VNI)),
            NetworkConfigurations.of(compatibleVxlanConfigs()),
            new TestTracerouteEngine(
                ImmutableMap.of(
                    NODE1,
                    new TestTracerouteEngineResult(NODE2, false, true),
                    NODE2,
                    new TestTracerouteEngineResult(NODE1, true, true)))));
  }

  @Test
  public void testVxlanFlowDeliveredNoHops() {
    assertFalse(
        vxlanFlowDelivered(
            NODE1,
            _v1.getName(),
            SRC_IP1,
            NODE2,
            SRC_IP2,
            UDP_PORT,
            new TestTracerouteEngine(
                ImmutableMap.of(NODE1, new TestTracerouteEngineResult(NODE2, true, false)))));
  }

  @Test
  public void testVxlanFlowDeliveredNotAccepted() {
    assertFalse(
        vxlanFlowDelivered(
            NODE1,
            _v1.getName(),
            SRC_IP1,
            NODE2,
            SRC_IP2,
            UDP_PORT,
            new TestTracerouteEngine(
                ImmutableMap.of(NODE1, new TestTracerouteEngineResult(NODE2, false, true)))));
  }

  @Test
  public void testVxlanFlowDeliveredSuccess() {
    assertTrue(
        vxlanFlowDelivered(
            NODE1,
            _v1.getName(),
            SRC_IP1,
            NODE2,
            SRC_IP2,
            UDP_PORT,
            new TestTracerouteEngine(
                ImmutableMap.of(NODE1, new TestTracerouteEngineResult(NODE2, true, true)))));
  }

  @Test
  public void testVxlanFlowDeliveredWrongNode() {
    assertFalse(
        vxlanFlowDelivered(
            NODE1,
            _v1.getName(),
            SRC_IP1,
            NODE2,
            SRC_IP2,
            UDP_PORT,
            new TestTracerouteEngine(
                ImmutableMap.of(
                    NODE1, new TestTracerouteEngineResult("wrong place", true, true)))));
  }
}
