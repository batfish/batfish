package org.batfish.datamodel.vxlan;

import static org.batfish.datamodel.vxlan.VxlanTopology.EMPTY;
import static org.batfish.datamodel.vxlan.VxlanTopology.addVniEdge;
import static org.batfish.datamodel.vxlan.VxlanTopology.addVniEdges;
import static org.batfish.datamodel.vxlan.VxlanTopology.buildVxlanNode;
import static org.batfish.datamodel.vxlan.VxlanTopology.compatibleVniSettings;
import static org.batfish.datamodel.vxlan.VxlanTopology.initVniVrfAssociations;
import static org.batfish.datamodel.vxlan.VxlanTopology.initVrfHostnameMap;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import com.google.common.testing.EqualsTester;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.BumTransportMethod;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.VniSettings;
import org.batfish.datamodel.Vrf;
import org.junit.Before;
import org.junit.Test;

/** Test of {@link VxlanTopology}. */
public final class VxlanTopologyTest {

  private static final Ip MULTICAST_GROUP = Ip.parse("224.0.0.1");
  private static final String NODE1 = "n1";
  private static final String NODE2 = "n2";
  private static final Ip SRC_IP1 = Ip.parse("1.1.1.1");
  private static final Ip SRC_IP2 = Ip.parse("2.2.2.2");
  private static final int UDP_PORT = 5555;
  private static final int VLAN1 = 1;
  private static final int VLAN2 = 2;
  private static final int VNI = 5000;

  private static @Nonnull VxlanTopology nonTrivialTopology() {
    MutableGraph<VxlanNode> graph = GraphBuilder.undirected().allowsSelfLoops(false).build();
    graph.putEdge(new VxlanNode("a", 1), new VxlanNode("b", 2));
    return new VxlanTopology(graph);
  }

  private Configuration _c1;
  private Configuration _c2;
  private Vrf _v1;
  private Vrf _v2;

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
  public void testVxlanTopologyConstructor() {
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
        new VxlanTopology(configurations).getGraph().edges(),
        equalTo(ImmutableSet.of(EndpointPair.unordered(nodeTail, nodeHead))));
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(new Object())
        .addEqualityGroup(
            EMPTY,
            EMPTY,
            new VxlanTopology(GraphBuilder.undirected().allowsSelfLoops(false).build()))
        .addEqualityGroup(nonTrivialTopology())
        .testEquals();
  }

  @Test
  public void testJacksonSerialization() throws IOException {
    assertEquals(
        nonTrivialTopology(), BatfishObjectMapper.clone(nonTrivialTopology(), VxlanTopology.class));
  }
}
