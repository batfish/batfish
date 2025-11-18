package org.batfish.datamodel.vxlan;

import static com.google.common.collect.ImmutableSortedSet.of;
import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.datamodel.Names.generatedTenantVniInterfaceName;
import static org.batfish.datamodel.vxlan.VniLayer.LAYER_2;
import static org.batfish.datamodel.vxlan.VniLayer.LAYER_3;
import static org.batfish.datamodel.vxlan.VxlanTopologyUtils.addLayer2VniEdge;
import static org.batfish.datamodel.vxlan.VxlanTopologyUtils.addLayer2VniEdges;
import static org.batfish.datamodel.vxlan.VxlanTopologyUtils.addLayer3VniEdge;
import static org.batfish.datamodel.vxlan.VxlanTopologyUtils.addLayer3VniEdges;
import static org.batfish.datamodel.vxlan.VxlanTopologyUtils.addTenantVniInterfaces;
import static org.batfish.datamodel.vxlan.VxlanTopologyUtils.buildLayer2VxlanNode;
import static org.batfish.datamodel.vxlan.VxlanTopologyUtils.buildLayer3VxlanNode;
import static org.batfish.datamodel.vxlan.VxlanTopologyUtils.compatibleLayer2VniSettings;
import static org.batfish.datamodel.vxlan.VxlanTopologyUtils.compatibleLayer3VniSettings;
import static org.batfish.datamodel.vxlan.VxlanTopologyUtils.computeNextVxlanTopologyModuloReachability;
import static org.batfish.datamodel.vxlan.VxlanTopologyUtils.prunedVxlanTopology;
import static org.batfish.datamodel.vxlan.VxlanTopologyUtils.vxlanFlowDelivered;
import static org.batfish.datamodel.vxlan.VxlanTopologyUtils.vxlanTopologyToLayer3Edges;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Table;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.stream.Stream;
import org.batfish.common.plugin.TracerouteEngine;
import org.batfish.common.traceroute.TraceDag;
import org.batfish.datamodel.BumTransportMethod;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.NetworkConfigurations;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.TestInterface;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.flow.FirewallSessionTraceInfo;
import org.batfish.datamodel.flow.Hop;
import org.batfish.datamodel.flow.LoopStep;
import org.batfish.datamodel.flow.Trace;
import org.batfish.datamodel.flow.TraceAndReverseFlow;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.vxlan.VxlanTopologyUtils.VrfId;
import org.junit.Before;
import org.junit.Test;

/** Test of {@link VxlanTopologyUtils}. */
public final class VxlanTopologyUtilsTest {

  private static class TestTracerouteEngine implements TracerouteEngine {

    private final Map<String, List<TraceAndReverseFlow>> _results;

    /** Builds a fake {@link TraceAndReverseFlow} for the given result. */
    private static TraceAndReverseFlow resultToTarf(TestTracerouteEngineResult result) {
      Trace forwardTrace =
          new Trace(
              result._accept ? FlowDisposition.ACCEPTED : FlowDisposition.DENIED_OUT,
              result._addHop
                  ? ImmutableList.of(
                      new Hop(new Node(result._receivingNode), ImmutableList.of(LoopStep.INSTANCE)))
                  : ImmutableList.of());
      return new TraceAndReverseFlow(
          forwardTrace,
          result._accept ? Flow.builder().setIngressNode(result._receivingNode).build() : null,
          ImmutableSet.of());
    }

    private TestTracerouteEngine(Map<String, TestTracerouteEngineResult> results) {
      _results =
          results.entrySet().stream()
              .collect(
                  ImmutableMap.toImmutableMap(
                      Entry::getKey,
                      resultEntry -> ImmutableList.of(resultToTarf(resultEntry.getValue()))));
    }

    @Override
    public SortedMap<Flow, List<Trace>> computeTraces(Set<Flow> flows, boolean ignoreFilters) {
      throw new UnsupportedOperationException();
    }

    @Override
    public SortedMap<Flow, List<TraceAndReverseFlow>> computeTracesAndReverseFlows(
        Set<Flow> flows, Set<FirewallSessionTraceInfo> sessions, boolean ignoreFilters) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Map<Flow, TraceDag> computeTraceDags(
        Set<Flow> flows, Set<FirewallSessionTraceInfo> sessions, boolean ignoreFilters) {
      return flows.stream()
          .map(
              flow -> {
                List<TraceAndReverseFlow> result = _results.get(flow.getIngressNode());
                TraceDag dag =
                    new TraceDag() {
                      @Override
                      public int countEdges() {
                        throw new UnsupportedOperationException();
                      }

                      @Override
                      public int countNodes() {
                        throw new UnsupportedOperationException();
                      }

                      @Override
                      public int size() {
                        throw new UnsupportedOperationException();
                      }

                      @Override
                      public Stream<TraceAndReverseFlow> getAllTraces() {
                        return result.stream();
                      }
                    };
                return new SimpleEntry<>(flow, dag);
              })
          .collect(ImmutableMap.toImmutableMap(Entry::getKey, Entry::getValue));
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
        TestInterface.builder().setType(InterfaceType.PHYSICAL).setName(IFACE_NAME);
    ib.setAddresses(ConcreteInterfaceAddress.create(SRC_IP1, 31)).setOwner(_c1).setVrf(_v1).build();
    ib.setAddresses(ConcreteInterfaceAddress.create(SRC_IP2, 31)).setOwner(_c2).setVrf(_v2).build();
    Layer2Vni.Builder vsb =
        Layer2Vni.testBuilder()
            .setBumTransportMethod(BumTransportMethod.UNICAST_FLOOD_GROUP)
            .setUdpPort(UDP_PORT)
            .setVlan(VLAN1)
            .setVni(VNI);
    _v1.addLayer2Vni(vsb.setBumTransportIps(of(SRC_IP2)).setSourceAddress(SRC_IP1).build());
    _v2.addLayer2Vni(vsb.setBumTransportIps(of(SRC_IP1)).setSourceAddress(SRC_IP2).build());
    return ImmutableMap.of(NODE1, _c1, NODE2, _c2);
  }

  @Before
  public void setup() {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    _c1 = cb.setHostname(NODE1).build();
    _c2 = cb.setHostname(NODE2).build();
    Vrf.Builder vb = nf.vrfBuilder().setName(DEFAULT_VRF_NAME);
    _v1 = vb.setOwner(_c1).build();
    _v2 = vb.setOwner(_c2).build();
  }

  @Test
  public void testAddLayer2VniEdge() {
    MutableGraph<VxlanNode> graph = GraphBuilder.undirected().allowsSelfLoops(false).build();
    Layer2Vni.Builder vniSettingsBuilder =
        Layer2Vni.testBuilder()
            .setBumTransportIps(ImmutableSortedSet.of(MULTICAST_GROUP))
            .setBumTransportMethod(BumTransportMethod.MULTICAST_GROUP)
            .setUdpPort(UDP_PORT)
            .setVni(VNI);
    Layer2Vni vniSettingsTail =
        vniSettingsBuilder.setSourceAddress(SRC_IP1).setVlan(VLAN1).setVni(VNI).build();
    _v1.setLayer2Vnis(ImmutableSet.of(vniSettingsTail));
    Layer2Vni vniSettingsHead =
        vniSettingsBuilder.setSourceAddress(SRC_IP2).setVlan(VLAN2).setVni(VNI).build();
    _v2.setLayer2Vnis(ImmutableSet.of(vniSettingsHead));
    addLayer2VniEdge(
        graph,
        new VrfId(_c1.getHostname(), _v1.getName()),
        vniSettingsTail,
        new VrfId(_c2.getHostname(), _v2.getName()),
        vniSettingsHead);
    Set<EndpointPair<VxlanNode>> edges = graph.edges();

    assertThat(
        edges,
        equalTo(
            ImmutableSet.of(
                EndpointPair.unordered(
                    VxlanNode.builder().setHostname(NODE2).setVni(VNI).setVniLayer(LAYER_2).build(),
                    VxlanNode.builder()
                        .setHostname(NODE1)
                        .setVni(VNI)
                        .setVniLayer(LAYER_2)
                        .build()))));
  }

  @Test
  public void testAddLayer2VniEdgeIncompatible() {
    MutableGraph<VxlanNode> graph = GraphBuilder.undirected().allowsSelfLoops(false).build();
    Layer2Vni.Builder vniSettingsBuilder =
        Layer2Vni.testBuilder()
            .setBumTransportIps(ImmutableSortedSet.of(MULTICAST_GROUP))
            .setBumTransportMethod(BumTransportMethod.MULTICAST_GROUP)
            .setUdpPort(UDP_PORT)
            .setVni(VNI);
    Layer2Vni vniSettingsTail = vniSettingsBuilder.setSourceAddress(SRC_IP1).setVlan(VLAN1).build();
    _v1.setLayer2Vnis(ImmutableSet.of(vniSettingsTail));
    Layer2Vni vniSettingsHead = vniSettingsBuilder.setSourceAddress(SRC_IP1).setVlan(VLAN2).build();
    _v2.setLayer2Vnis(ImmutableSet.of(vniSettingsHead));
    addLayer2VniEdge(
        graph,
        new VrfId(_c1.getHostname(), _v1.getName()),
        vniSettingsTail,
        new VrfId(_c2.getHostname(), _v2.getName()),
        vniSettingsHead);
    Set<EndpointPair<VxlanNode>> edges = graph.edges();

    assertThat(edges, empty());
  }

  @Test
  public void testAddLayer3VniEdge() {
    MutableGraph<VxlanNode> graph = GraphBuilder.undirected().allowsSelfLoops(false).build();
    Layer3Vni.Builder vniSettingsBuilder = Layer3Vni.testBuilder().setUdpPort(UDP_PORT).setVni(VNI);
    Layer3Vni vniSettingsTail =
        vniSettingsBuilder
            .setSourceAddress(SRC_IP1)
            .setVni(VNI)
            .setLearnedNexthopVtepIps(ImmutableSet.of(SRC_IP2))
            .build();
    _v1.setLayer3Vnis(ImmutableSet.of(vniSettingsTail));
    Layer3Vni vniSettingsHead =
        vniSettingsBuilder
            .setSourceAddress(SRC_IP2)
            .setVni(VNI)
            .setLearnedNexthopVtepIps(ImmutableSet.of(SRC_IP1))
            .build();
    _v2.setLayer3Vnis(ImmutableSet.of(vniSettingsHead));
    addLayer3VniEdge(
        graph,
        new VrfId(_c1.getHostname(), _v1.getName()),
        vniSettingsTail,
        new VrfId(_c2.getHostname(), _v2.getName()),
        vniSettingsHead);
    Set<EndpointPair<VxlanNode>> edges = graph.edges();

    assertThat(
        edges,
        equalTo(
            ImmutableSet.of(
                EndpointPair.unordered(
                    VxlanNode.builder().setHostname(NODE2).setVni(VNI).setVniLayer(LAYER_3).build(),
                    VxlanNode.builder()
                        .setHostname(NODE1)
                        .setVni(VNI)
                        .setVniLayer(LAYER_3)
                        .build()))));
  }

  @Test
  public void testAddLayer3VniEdgeIncompatible() {
    MutableGraph<VxlanNode> graph = GraphBuilder.undirected().allowsSelfLoops(false).build();
    Layer3Vni.Builder vniSettingsBuilder = Layer3Vni.testBuilder().setUdpPort(UDP_PORT).setVni(VNI);
    Layer3Vni vniSettingsTail = vniSettingsBuilder.setSourceAddress(SRC_IP1).setVni(VNI).build();
    _v1.setLayer3Vnis(ImmutableSet.of(vniSettingsTail));
    Layer3Vni vniSettingsHead = vniSettingsBuilder.setSourceAddress(SRC_IP2).setVni(VNI).build();
    addLayer3VniEdge(
        graph,
        new VrfId(_c1.getHostname(), _v1.getName()),
        vniSettingsTail,
        new VrfId(_c2.getHostname(), _v2.getName()),
        vniSettingsHead);
    Set<EndpointPair<VxlanNode>> edges = graph.edges();

    assertThat(edges, empty());
  }

  @Test
  public void testAddLayer2VniEdges() {
    MutableGraph<VxlanNode> graph = GraphBuilder.undirected().allowsSelfLoops(false).build();
    Layer2Vni.Builder vniSettingsBuilder =
        Layer2Vni.testBuilder()
            .setBumTransportIps(ImmutableSortedSet.of(MULTICAST_GROUP))
            .setBumTransportMethod(BumTransportMethod.MULTICAST_GROUP)
            .setUdpPort(UDP_PORT)
            .setVni(VNI);
    Layer2Vni vniSettingsTail = vniSettingsBuilder.setSourceAddress(SRC_IP1).setVlan(VLAN1).build();
    _v1.setLayer2Vnis(ImmutableSet.of(vniSettingsTail));
    Layer2Vni vniSettingsHead = vniSettingsBuilder.setSourceAddress(SRC_IP2).setVlan(VLAN2).build();
    _v2.setLayer2Vnis(ImmutableSet.of(vniSettingsHead));
    addLayer2VniEdges(
        graph,
        ImmutableMap.of(
            new VrfId(_c1.getHostname(), _v1.getName()), vniSettingsTail,
            new VrfId(_c2.getHostname(), _v2.getName()), vniSettingsHead));

    VxlanNode nodeTail =
        VxlanNode.builder().setHostname(NODE1).setVni(VNI).setVniLayer(LAYER_2).build();
    VxlanNode nodeHead =
        VxlanNode.builder().setHostname(NODE2).setVni(VNI).setVniLayer(LAYER_2).build();

    assertThat(graph.edges(), equalTo(ImmutableSet.of(EndpointPair.unordered(nodeTail, nodeHead))));
  }

  @Test
  public void testAddLayer3VniEdges() {
    MutableGraph<VxlanNode> graph = GraphBuilder.undirected().allowsSelfLoops(false).build();
    Layer3Vni.Builder vniSettingsBuilder = Layer3Vni.testBuilder().setUdpPort(UDP_PORT).setVni(VNI);
    Layer3Vni vniSettingsTail =
        vniSettingsBuilder
            .setSourceAddress(SRC_IP1)
            .setLearnedNexthopVtepIps(ImmutableSet.of(SRC_IP2))
            .build();
    _v1.setLayer3Vnis(ImmutableSet.of(vniSettingsTail));
    Layer3Vni vniSettingsHead =
        vniSettingsBuilder
            .setSourceAddress(SRC_IP2)
            .setLearnedNexthopVtepIps(ImmutableSet.of(SRC_IP1))
            .build();
    _v2.setLayer3Vnis(ImmutableSet.of(vniSettingsHead));
    addLayer3VniEdges(
        graph,
        ImmutableMap.of(
            new VrfId(_c1.getHostname(), _v1.getName()), vniSettingsTail,
            new VrfId(_c2.getHostname(), _v2.getName()), vniSettingsHead));

    VxlanNode nodeTail =
        VxlanNode.builder().setHostname(NODE1).setVni(VNI).setVniLayer(LAYER_3).build();
    VxlanNode nodeHead =
        VxlanNode.builder().setHostname(NODE2).setVni(VNI).setVniLayer(LAYER_3).build();

    assertThat(graph.edges(), equalTo(ImmutableSet.of(EndpointPair.unordered(nodeTail, nodeHead))));
  }

  @Test
  public void testBuildLayer2VxlanNode() {
    Layer2Vni vniSettings =
        Layer2Vni.testBuilder()
            .setSourceAddress(SRC_IP1)
            .setVlan(VLAN1)
            .setVni(VNI)
            .setBumTransportMethod(BumTransportMethod.MULTICAST_GROUP)
            .build();
    assertThat(
        buildLayer2VxlanNode(new VrfId(_c1.getHostname(), _v1.getName()), vniSettings),
        equalTo(VxlanNode.builder().setHostname(NODE1).setVni(VNI).setVniLayer(LAYER_2).build()));
  }

  @Test
  public void testBuildLayer3VxlanNode() {
    Layer3Vni vniSettings = Layer3Vni.testBuilder().setSourceAddress(SRC_IP1).setVni(VNI).build();
    assertThat(
        buildLayer3VxlanNode(new VrfId(_c1.getHostname(), _v1.getName()), vniSettings),
        equalTo(VxlanNode.builder().setHostname(NODE1).setVni(VNI).setVniLayer(LAYER_3).build()));
  }

  @Test
  public void testCompatibleLayer2VniSettingsMismatchBumTransportHeadFloodGroup() {
    Layer2Vni vniSettingsTail =
        Layer2Vni.testBuilder()
            .setBumTransportIps(ImmutableSortedSet.of(SRC_IP2))
            .setBumTransportMethod(BumTransportMethod.UNICAST_FLOOD_GROUP)
            .setSourceAddress(SRC_IP1)
            .setUdpPort(UDP_PORT)
            .setVlan(VLAN1)
            .setVni(VNI)
            .build();
    Layer2Vni vniSettingsHead =
        Layer2Vni.testBuilder()
            .setBumTransportIps(ImmutableSortedSet.of(SRC_IP2))
            .setBumTransportMethod(BumTransportMethod.UNICAST_FLOOD_GROUP)
            .setSourceAddress(SRC_IP2)
            .setUdpPort(UDP_PORT)
            .setVlan(VLAN2)
            .setVni(VNI)
            .build();

    assertThat(compatibleLayer2VniSettings(vniSettingsTail, vniSettingsHead), equalTo(false));
  }

  @Test
  public void testCompatibleLayer2VniSettingsMismatchBumTransportMethod() {
    Layer2Vni vniSettingsTail =
        Layer2Vni.testBuilder()
            .setBumTransportIps(ImmutableSortedSet.of(MULTICAST_GROUP))
            .setBumTransportMethod(BumTransportMethod.MULTICAST_GROUP)
            .setSourceAddress(SRC_IP1)
            .setUdpPort(UDP_PORT)
            .setVlan(VLAN1)
            .setVni(VNI)
            .build();
    Layer2Vni vniSettingsHead =
        Layer2Vni.testBuilder()
            .setBumTransportIps(ImmutableSortedSet.of(MULTICAST_GROUP))
            .setBumTransportMethod(BumTransportMethod.UNICAST_FLOOD_GROUP)
            .setSourceAddress(SRC_IP2)
            .setUdpPort(UDP_PORT)
            .setVlan(VLAN2)
            .setVni(VNI)
            .build();

    assertThat(compatibleLayer2VniSettings(vniSettingsTail, vniSettingsHead), equalTo(false));
  }

  @Test
  public void testCompatibleLayer2VniSettingsMismatchBumTransportMulticastGroup() {
    Layer2Vni vniSettingsTail =
        Layer2Vni.testBuilder()
            .setBumTransportIps(ImmutableSortedSet.of(MULTICAST_GROUP))
            .setBumTransportMethod(BumTransportMethod.MULTICAST_GROUP)
            .setSourceAddress(SRC_IP1)
            .setUdpPort(UDP_PORT)
            .setVlan(VLAN1)
            .setVni(VNI)
            .build();
    Layer2Vni vniSettingsHead =
        Layer2Vni.testBuilder()
            .setBumTransportIps(ImmutableSortedSet.of(Ip.parse("224.0.0.5")))
            .setBumTransportMethod(BumTransportMethod.MULTICAST_GROUP)
            .setSourceAddress(SRC_IP2)
            .setUdpPort(UDP_PORT)
            .setVlan(VLAN2)
            .setVni(VNI)
            .build();

    assertThat(compatibleLayer2VniSettings(vniSettingsTail, vniSettingsHead), equalTo(false));
  }

  @Test
  public void testCompatibleLayer2VniSettingsMismatchBumTransportMulticastGroupUnicast() {
    Layer2Vni vniSettingsTail =
        Layer2Vni.testBuilder()
            .setBumTransportIps(ImmutableSortedSet.of(SRC_IP2))
            .setBumTransportMethod(BumTransportMethod.MULTICAST_GROUP)
            .setSourceAddress(SRC_IP1)
            .setUdpPort(UDP_PORT)
            .setVlan(VLAN1)
            .setVni(VNI)
            .build();
    Layer2Vni vniSettingsHead =
        Layer2Vni.testBuilder()
            .setBumTransportIps(ImmutableSortedSet.of(SRC_IP1))
            .setBumTransportMethod(BumTransportMethod.MULTICAST_GROUP)
            .setSourceAddress(SRC_IP2)
            .setUdpPort(UDP_PORT)
            .setVlan(VLAN2)
            .setVni(VNI)
            .build();

    assertThat(compatibleLayer2VniSettings(vniSettingsTail, vniSettingsHead), equalTo(false));
  }

  @Test
  public void testCompatibleLayer2VniSettingsMismatchBumTransportTailFloodGroup() {
    Layer2Vni vniSettingsTail =
        Layer2Vni.testBuilder()
            .setBumTransportIps(ImmutableSortedSet.of(SRC_IP1))
            .setBumTransportMethod(BumTransportMethod.UNICAST_FLOOD_GROUP)
            .setSourceAddress(SRC_IP1)
            .setUdpPort(UDP_PORT)
            .setVlan(VLAN1)
            .setVni(VNI)
            .build();
    Layer2Vni vniSettingsHead =
        Layer2Vni.testBuilder()
            .setBumTransportIps(ImmutableSortedSet.of(SRC_IP1))
            .setBumTransportMethod(BumTransportMethod.UNICAST_FLOOD_GROUP)
            .setSourceAddress(SRC_IP2)
            .setUdpPort(UDP_PORT)
            .setVlan(VLAN2)
            .setVni(VNI)
            .build();

    assertThat(compatibleLayer2VniSettings(vniSettingsTail, vniSettingsHead), equalTo(false));
  }

  @Test
  public void testCompatibleLayer2VniSettingsMismatchDifferentUdpPort() {
    Layer2Vni vniSettingsTail =
        Layer2Vni.testBuilder()
            .setBumTransportIps(ImmutableSortedSet.of(MULTICAST_GROUP))
            .setBumTransportMethod(BumTransportMethod.MULTICAST_GROUP)
            .setSourceAddress(SRC_IP1)
            .setUdpPort(UDP_PORT)
            .setVlan(VLAN1)
            .setVni(VNI)
            .build();
    Layer2Vni vniSettingsHead =
        Layer2Vni.testBuilder()
            .setBumTransportIps(ImmutableSortedSet.of(MULTICAST_GROUP))
            .setBumTransportMethod(BumTransportMethod.MULTICAST_GROUP)
            .setSourceAddress(SRC_IP2)
            .setUdpPort(UDP_PORT + 1)
            .setVlan(VLAN2)
            .setVni(VNI)
            .build();

    assertThat(compatibleLayer2VniSettings(vniSettingsTail, vniSettingsHead), equalTo(false));
  }

  @Test
  public void testCompatibleLayer2VniSettingsMismatchNullHeadSourceAddress() {
    Layer2Vni vniSettingsTail =
        Layer2Vni.testBuilder()
            .setBumTransportIps(ImmutableSortedSet.of(MULTICAST_GROUP))
            .setBumTransportMethod(BumTransportMethod.MULTICAST_GROUP)
            .setSourceAddress(SRC_IP1)
            .setUdpPort(UDP_PORT)
            .setVlan(VLAN1)
            .setVni(VNI)
            .build();
    Layer2Vni vniSettingsHead =
        Layer2Vni.testBuilder()
            .setBumTransportIps(ImmutableSortedSet.of(MULTICAST_GROUP))
            .setBumTransportMethod(BumTransportMethod.MULTICAST_GROUP)
            .setSourceAddress(null)
            .setUdpPort(UDP_PORT)
            .setVlan(VLAN2)
            .setVni(VNI)
            .build();

    assertThat(compatibleLayer2VniSettings(vniSettingsTail, vniSettingsHead), equalTo(false));
  }

  @Test
  public void testCompatibleLayer2VniSettingsMismatchNullTailSourceAddress() {
    Layer2Vni vniSettingsTail =
        Layer2Vni.testBuilder()
            .setBumTransportIps(ImmutableSortedSet.of(MULTICAST_GROUP))
            .setBumTransportMethod(BumTransportMethod.MULTICAST_GROUP)
            .setSourceAddress(null)
            .setUdpPort(UDP_PORT)
            .setVlan(VLAN1)
            .setVni(VNI)
            .build();
    Layer2Vni vniSettingsHead =
        Layer2Vni.testBuilder()
            .setBumTransportIps(ImmutableSortedSet.of(MULTICAST_GROUP))
            .setBumTransportMethod(BumTransportMethod.MULTICAST_GROUP)
            .setSourceAddress(SRC_IP2)
            .setUdpPort(UDP_PORT)
            .setVlan(VLAN2)
            .setVni(VNI)
            .build();

    assertThat(compatibleLayer2VniSettings(vniSettingsTail, vniSettingsHead), equalTo(false));
  }

  @Test
  public void testCompatibleLayer2VniSettingsMismatchSameSourceAddress() {
    Layer2Vni vniSettingsTail =
        Layer2Vni.testBuilder()
            .setBumTransportIps(ImmutableSortedSet.of(MULTICAST_GROUP))
            .setBumTransportMethod(BumTransportMethod.MULTICAST_GROUP)
            .setSourceAddress(SRC_IP1)
            .setUdpPort(UDP_PORT)
            .setVlan(VLAN1)
            .setVni(VNI)
            .build();
    Layer2Vni vniSettingsHead =
        Layer2Vni.testBuilder()
            .setBumTransportIps(ImmutableSortedSet.of(MULTICAST_GROUP))
            .setBumTransportMethod(BumTransportMethod.MULTICAST_GROUP)
            .setSourceAddress(SRC_IP1)
            .setUdpPort(UDP_PORT)
            .setVlan(VLAN2)
            .setVni(VNI)
            .build();

    assertThat(compatibleLayer2VniSettings(vniSettingsTail, vniSettingsHead), equalTo(false));
  }

  @Test
  public void testCompatibleLayer2VniSettingsMulticast() {
    Layer2Vni vniSettingsTail =
        Layer2Vni.testBuilder()
            .setBumTransportIps(ImmutableSortedSet.of(MULTICAST_GROUP))
            .setBumTransportMethod(BumTransportMethod.MULTICAST_GROUP)
            .setSourceAddress(SRC_IP1)
            .setUdpPort(UDP_PORT)
            .setVlan(VLAN1)
            .setVni(VNI)
            .build();
    Layer2Vni vniSettingsHead =
        Layer2Vni.testBuilder()
            .setBumTransportIps(ImmutableSortedSet.of(MULTICAST_GROUP))
            .setBumTransportMethod(BumTransportMethod.MULTICAST_GROUP)
            .setSourceAddress(SRC_IP2)
            .setUdpPort(UDP_PORT)
            .setVlan(VLAN2)
            .setVni(VNI)
            .build();

    assertThat(compatibleLayer2VniSettings(vniSettingsTail, vniSettingsHead), equalTo(true));
  }

  @Test
  public void testCompatibleLayer2VniSettingsUnicast() {
    Layer2Vni vniSettingsTail =
        Layer2Vni.testBuilder()
            .setBumTransportIps(ImmutableSortedSet.of(SRC_IP2))
            .setBumTransportMethod(BumTransportMethod.UNICAST_FLOOD_GROUP)
            .setSourceAddress(SRC_IP1)
            .setUdpPort(UDP_PORT)
            .setVlan(VLAN1)
            .setVni(VNI)
            .build();
    Layer2Vni vniSettingsHead =
        Layer2Vni.testBuilder()
            .setBumTransportIps(ImmutableSortedSet.of(SRC_IP1))
            .setBumTransportMethod(BumTransportMethod.UNICAST_FLOOD_GROUP)
            .setSourceAddress(SRC_IP2)
            .setUdpPort(UDP_PORT)
            .setVlan(VLAN2)
            .setVni(VNI)
            .build();

    assertThat(compatibleLayer2VniSettings(vniSettingsTail, vniSettingsHead), equalTo(true));
  }

  @Test
  public void testCompatibleLayer3VniSettingsOnlyOneSideLearnedAnIp() {
    Layer3Vni vniSettingsTail =
        Layer3Vni.testBuilder().setSourceAddress(SRC_IP1).setUdpPort(UDP_PORT).setVni(VNI).build();
    Layer3Vni vniSettingsHead =
        Layer3Vni.testBuilder()
            .setLearnedNexthopVtepIps(ImmutableSortedSet.of(SRC_IP1))
            .setSourceAddress(SRC_IP2)
            .setUdpPort(UDP_PORT)
            .setVni(VNI)
            .build();

    assertTrue(compatibleLayer3VniSettings(vniSettingsTail, vniSettingsHead));
    assertTrue(compatibleLayer3VniSettings(vniSettingsHead, vniSettingsTail));
  }

  @Test
  public void testCompatibleLayer3VniSettingsMismatchLearnedIps() {
    Layer3Vni vniSettingsTail =
        Layer3Vni.testBuilder()
            .setLearnedNexthopVtepIps(ImmutableSortedSet.of(SRC_IP1))
            .setSourceAddress(SRC_IP1)
            .setUdpPort(UDP_PORT)
            .setVni(VNI)
            .build();
    Layer3Vni vniSettingsHead =
        Layer3Vni.testBuilder()
            .setLearnedNexthopVtepIps(ImmutableSortedSet.of(SRC_IP2))
            .setSourceAddress(SRC_IP2)
            .setUdpPort(UDP_PORT)
            .setVni(VNI)
            .build();
    assertFalse(compatibleLayer3VniSettings(vniSettingsTail, vniSettingsHead));
    assertFalse(compatibleLayer3VniSettings(vniSettingsHead, vniSettingsTail));
  }

  @Test
  public void testCompatibleLayer3VniSettingsMismatchDifferentUdpPort() {
    Layer3Vni vniSettingsTail =
        Layer3Vni.testBuilder()
            .setLearnedNexthopVtepIps(ImmutableSortedSet.of(SRC_IP2))
            .setSourceAddress(SRC_IP1)
            .setUdpPort(UDP_PORT)
            .setVni(VNI)
            .build();
    Layer3Vni vniSettingsHead =
        Layer3Vni.testBuilder()
            .setLearnedNexthopVtepIps(ImmutableSortedSet.of(SRC_IP1))
            .setSourceAddress(SRC_IP2)
            .setUdpPort(UDP_PORT + 1)
            .setVni(VNI)
            .build();

    assertThat(compatibleLayer3VniSettings(vniSettingsTail, vniSettingsHead), equalTo(false));
  }

  @Test
  public void testCompatibleLayer3VniSettingsMismatchNullHeadSourceAddress() {
    Layer3Vni vniSettingsTail =
        Layer3Vni.testBuilder()
            .setLearnedNexthopVtepIps(ImmutableSortedSet.of(SRC_IP2))
            .setSourceAddress(SRC_IP1)
            .setUdpPort(UDP_PORT)
            .setVni(VNI)
            .build();
    Layer3Vni vniSettingsHead =
        Layer3Vni.testBuilder()
            .setLearnedNexthopVtepIps(ImmutableSortedSet.of(SRC_IP1))
            .setSourceAddress(null)
            .setUdpPort(UDP_PORT)
            .setVni(VNI)
            .build();

    assertThat(compatibleLayer3VniSettings(vniSettingsTail, vniSettingsHead), equalTo(false));
  }

  @Test
  public void testCompatibleLayer3VniSettingsMismatchNullTailSourceAddress() {
    Layer3Vni vniSettingsTail =
        Layer3Vni.testBuilder()
            .setLearnedNexthopVtepIps(ImmutableSortedSet.of(SRC_IP2))
            .setSourceAddress(null)
            .setUdpPort(UDP_PORT)
            .setVni(VNI)
            .build();
    Layer3Vni vniSettingsHead =
        Layer3Vni.testBuilder()
            .setLearnedNexthopVtepIps(ImmutableSortedSet.of(SRC_IP1))
            .setSourceAddress(SRC_IP2)
            .setUdpPort(UDP_PORT)
            .setVni(VNI)
            .build();

    assertThat(compatibleLayer3VniSettings(vniSettingsTail, vniSettingsHead), equalTo(false));
  }

  @Test
  public void testCompatibleLayer3VniSettingsMismatchSameSourceAddress() {
    Layer3Vni vniSettingsTail =
        Layer3Vni.testBuilder()
            .setLearnedNexthopVtepIps(ImmutableSortedSet.of(SRC_IP2))
            .setSourceAddress(SRC_IP1)
            .setUdpPort(UDP_PORT)
            .setVni(VNI)
            .build();
    Layer3Vni vniSettingsHead =
        Layer3Vni.testBuilder()
            .setLearnedNexthopVtepIps(ImmutableSortedSet.of(SRC_IP1))
            .setSourceAddress(SRC_IP1)
            .setUdpPort(UDP_PORT)
            .setVni(VNI)
            .build();

    assertThat(compatibleLayer3VniSettings(vniSettingsTail, vniSettingsHead), equalTo(false));
  }

  @Test
  public void testCompatibleLayer3VniSettingsUnicast() {
    Layer3Vni vniSettingsTail =
        Layer3Vni.testBuilder()
            .setLearnedNexthopVtepIps(ImmutableSortedSet.of(SRC_IP2))
            .setSourceAddress(SRC_IP1)
            .setUdpPort(UDP_PORT)
            .setVni(VNI)
            .build();
    Layer3Vni vniSettingsHead =
        Layer3Vni.testBuilder()
            .setLearnedNexthopVtepIps(ImmutableSortedSet.of(SRC_IP1))
            .setSourceAddress(SRC_IP2)
            .setUdpPort(UDP_PORT)
            .setVni(VNI)
            .build();

    assertThat(compatibleLayer3VniSettings(vniSettingsTail, vniSettingsHead), equalTo(true));
  }

  @Test
  public void testInitialVxlanTopology() {
    Map<String, Configuration> configurations = ImmutableMap.of(NODE1, _c1, NODE2, _c2);
    Layer2Vni.Builder vniSettingsBuilder =
        Layer2Vni.testBuilder()
            .setBumTransportIps(ImmutableSortedSet.of(MULTICAST_GROUP))
            .setBumTransportMethod(BumTransportMethod.MULTICAST_GROUP)
            .setUdpPort(UDP_PORT)
            .setVni(VNI);
    Layer2Vni vniSettingsTail = vniSettingsBuilder.setSourceAddress(SRC_IP1).setVlan(VLAN1).build();
    _v1.setLayer2Vnis(ImmutableSet.of(vniSettingsTail));
    _v2.setLayer2Vnis(
        ImmutableSet.of(vniSettingsBuilder.setSourceAddress(SRC_IP2).setVlan(VLAN2).build()));

    VxlanNode nodeTail =
        VxlanNode.builder().setHostname(NODE1).setVni(VNI).setVniLayer(LAYER_2).build();
    VxlanNode nodeHead =
        VxlanNode.builder().setHostname(NODE2).setVni(VNI).setVniLayer(LAYER_2).build();

    assertThat(
        VxlanTopologyUtils.computeInitialVxlanTopology(configurations).getGraph().edges(),
        equalTo(ImmutableSet.of(EndpointPair.unordered(nodeTail, nodeHead))));
  }

  @Test
  public void testInitialVxlanTopologyFromTable() {
    Layer2Vni.Builder vniSettingsBuilder =
        Layer2Vni.testBuilder()
            .setBumTransportIps(ImmutableSortedSet.of(MULTICAST_GROUP))
            .setBumTransportMethod(BumTransportMethod.MULTICAST_GROUP)
            .setUdpPort(UDP_PORT)
            .setVni(VNI);
    Layer2Vni vniSettingsTail = vniSettingsBuilder.setSourceAddress(SRC_IP1).setVlan(VLAN1).build();
    _v1.setLayer2Vnis(ImmutableSet.of(vniSettingsTail));
    Layer2Vni vniSettingsHead = vniSettingsBuilder.setSourceAddress(SRC_IP2).setVlan(VLAN2).build();
    _v2.setLayer2Vnis(ImmutableSet.of(vniSettingsHead));
    Table<String, String, Set<Layer2Vni>> table = HashBasedTable.create();
    table.put(_c1.getHostname(), _v1.getName(), ImmutableSet.of(vniSettingsTail));
    table.put(_c2.getHostname(), _v2.getName(), ImmutableSet.of(vniSettingsHead));

    VxlanNode nodeTail =
        VxlanNode.builder().setHostname(NODE1).setVni(VNI).setVniLayer(LAYER_2).build();
    VxlanNode nodeHead =
        VxlanNode.builder().setHostname(NODE2).setVni(VNI).setVniLayer(LAYER_2).build();

    // TODO: nontrivial l3
    assertThat(
        computeNextVxlanTopologyModuloReachability(table, HashBasedTable.create())
            .getGraph()
            .edges(),
        equalTo(ImmutableSet.of(EndpointPair.unordered(nodeTail, nodeHead))));
  }

  @Test
  public void testPrunedVxlanTopologyDiscard() {
    MutableGraph<VxlanNode> graph = GraphBuilder.undirected().allowsSelfLoops(false).build();
    graph.putEdge(new VxlanNode(NODE1, VNI, LAYER_2), new VxlanNode(NODE2, VNI, LAYER_2));
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
    graph.putEdge(new VxlanNode(NODE1, VNI, LAYER_2), new VxlanNode(NODE2, VNI, LAYER_2));
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
            EndpointPair.unordered(
                new VxlanNode(NODE1, VNI, LAYER_2), new VxlanNode(NODE2, VNI, LAYER_2)),
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
            EndpointPair.unordered(
                new VxlanNode(NODE1, VNI, LAYER_2), new VxlanNode(NODE2, VNI, LAYER_2)),
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
            config -> {
              Vrf vrf = config.getDefaultVrf();
              vrf.setLayer2Vnis(
                  vrf.getLayer2Vnis().values().stream()
                      .map(
                          vni ->
                              vni.toBuilder()
                                  .setBumTransportMethod(BumTransportMethod.MULTICAST_GROUP)
                                  .build())
                      .collect(ImmutableSet.toImmutableSet()));
            });

    assertFalse(
        VxlanTopologyUtils.reachableEdge(
            EndpointPair.unordered(
                new VxlanNode(NODE1, VNI, LAYER_2), new VxlanNode(NODE2, VNI, LAYER_2)),
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
            EndpointPair.unordered(
                new VxlanNode(NODE1, VNI, LAYER_2), new VxlanNode(NODE2, VNI, LAYER_2)),
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

  @Test
  public void testAddTenantVniInterfaces() {
    Configuration c = new Configuration("c", ConfigurationFormat.CISCO_NX);
    c.setDefaultCrossZoneAction(LineAction.PERMIT);
    c.setDefaultInboundAction(LineAction.PERMIT);
    Vrf.Builder vb = Vrf.builder().setOwner(c);
    Vrf v1 = vb.setName("v1").build();
    Vrf v2 = vb.setName("v2").build();
    Ip sourceAddress = Ip.parse("10.0.0.1");
    Layer3Vni.Builder l3b = Layer3Vni.builder().setSrcVrf(DEFAULT_VRF_NAME).setUdpPort(5);
    v1.addLayer3Vni(l3b.setVni(1).setSourceAddress(sourceAddress).build());
    v2.addLayer3Vni(l3b.setVni(2).setSourceAddress(null).build());
    String vni1IfaceName = generatedTenantVniInterfaceName(1);
    String vni2IfaceName = generatedTenantVniInterfaceName(2);
    addTenantVniInterfaces(c);

    // v1 vni 1
    assertThat(c.getAllInterfaces(v1.getName()), hasKey(vni1IfaceName));

    Interface vni1Iface = c.getAllInterfaces().get(vni1IfaceName);

    assertThat(vni1Iface.getAdditionalArpIps(), equalTo(sourceAddress.toIpSpace()));

    // v2 vni 2
    assertThat(c.getAllInterfaces(), not(hasKey(vni2IfaceName)));
  }

  @Test
  public void testVxlanTopologyToLayer3Edges() {
    Configuration c1 = new Configuration("c1", ConfigurationFormat.CISCO_NX);
    c1.setDefaultCrossZoneAction(LineAction.PERMIT);
    c1.setDefaultInboundAction(LineAction.PERMIT);
    Configuration c2 = new Configuration("c2", ConfigurationFormat.CISCO_NX);
    c2.setDefaultCrossZoneAction(LineAction.PERMIT);
    c2.setDefaultInboundAction(LineAction.PERMIT);
    Vrf default1 = Vrf.builder().setOwner(c1).setName(DEFAULT_VRF_NAME).build();
    Vrf default2 = Vrf.builder().setOwner(c2).setName(DEFAULT_VRF_NAME).build();
    Vrf v1 = Vrf.builder().setOwner(c1).setName("v1").build();
    Vrf v2 = Vrf.builder().setOwner(c2).setName("v2").build();
    Ip sourceAddress1 = Ip.parse("10.0.0.1");
    Ip sourceAddress2 = Ip.parse("10.0.0.2");

    // Add layer-2 VNIs, which should not result in layer-3 edges
    int l2Vni = 500;
    Layer2Vni.Builder l2b =
        Layer2Vni.builder()
            .setBumTransportIps(ImmutableSet.of())
            .setBumTransportMethod(BumTransportMethod.UNICAST_FLOOD_GROUP)
            .setSrcVrf(DEFAULT_VRF_NAME)
            .setUdpPort(5)
            .setVlan(500)
            .setVni(l2Vni);
    default1.addLayer2Vni(l2b.setSourceAddress(sourceAddress1).build());
    default2.addLayer2Vni(l2b.setSourceAddress(sourceAddress2).build());

    // Add layer-3 VNIs, which should result in layer-3 edges
    int l3Vni = 1000;
    Layer3Vni.Builder l3b =
        Layer3Vni.builder().setSrcVrf(DEFAULT_VRF_NAME).setUdpPort(5).setVni(l3Vni);
    v1.addLayer3Vni(l3b.setSourceAddress(sourceAddress1).build());
    v2.addLayer3Vni(l3b.setSourceAddress(sourceAddress2).build());

    addTenantVniInterfaces(c1);
    addTenantVniInterfaces(c2);

    MutableGraph<VxlanNode> graph = GraphBuilder.undirected().allowsSelfLoops(false).build();
    VxlanNode l2Node1 = new VxlanNode(c1.getHostname(), l2Vni, LAYER_2);
    VxlanNode l2Node2 = new VxlanNode(c2.getHostname(), l2Vni, LAYER_2);
    VxlanNode l3Node1 = new VxlanNode(c1.getHostname(), l3Vni, LAYER_3);
    VxlanNode l3Node2 = new VxlanNode(c2.getHostname(), l3Vni, LAYER_3);
    // Add VXLAN edges for layer-2 VNIs
    graph.addNode(l2Node1);
    graph.addNode(l2Node2);
    graph.putEdge(l2Node1, l2Node2);
    // Add VXLAN edges for layer-3 VNIs
    graph.addNode(l3Node1);
    graph.addNode(l3Node2);
    graph.putEdge(l3Node1, l3Node2);
    VxlanTopology vxlanTopology = new VxlanTopology(graph);

    String ifaceName = generatedTenantVniInterfaceName(l3Vni);

    // Should see edges for layer-3 VNI interfaces, and nothing else
    assertThat(
        vxlanTopologyToLayer3Edges(
            vxlanTopology, ImmutableMap.of(c1.getHostname(), c1, c2.getHostname(), c2)),
        containsInAnyOrder(
            Edge.of(c1.getHostname(), ifaceName, c2.getHostname(), ifaceName),
            Edge.of(c2.getHostname(), ifaceName, c1.getHostname(), ifaceName)));
  }
}
