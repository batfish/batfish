package org.batfish.question.edges;

import static org.batfish.datamodel.vxlan.Layer2Vni.testBuilder;
import static org.batfish.question.edges.EdgesAnswerer.COL_INTERFACE;
import static org.batfish.question.edges.EdgesAnswerer.COL_MULTICAST_GROUP;
import static org.batfish.question.edges.EdgesAnswerer.COL_NODE;
import static org.batfish.question.edges.EdgesAnswerer.COL_REMOTE_INTERFACE;
import static org.batfish.question.edges.EdgesAnswerer.COL_REMOTE_NODE;
import static org.batfish.question.edges.EdgesAnswerer.COL_REMOTE_VLAN;
import static org.batfish.question.edges.EdgesAnswerer.COL_REMOTE_VTEP_ADDRESS;
import static org.batfish.question.edges.EdgesAnswerer.COL_UDP_PORT;
import static org.batfish.question.edges.EdgesAnswerer.COL_VLAN;
import static org.batfish.question.edges.EdgesAnswerer.COL_VNI;
import static org.batfish.question.edges.EdgesAnswerer.COL_VTEP_ADDRESS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import javax.annotation.Nonnull;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.plugin.IBatfishTestAdapter;
import org.batfish.common.topology.Layer1Edge;
import org.batfish.common.topology.Layer1Node;
import org.batfish.common.topology.Layer1Topology;
import org.batfish.common.topology.TopologyProvider;
import org.batfish.datamodel.BumTransportMethod;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.vxlan.Layer2Vni;
import org.batfish.identifiers.NetworkId;
import org.batfish.identifiers.SnapshotId;
import org.batfish.question.edges.EdgesQuestion.EdgeType;
import org.batfish.specifier.Location;
import org.batfish.specifier.LocationInfo;
import org.junit.Before;
import org.junit.Test;

/** End-to-end tests of {@link org.batfish.question.edges}. */
public final class EdgesTest {

  private NetworkFactory _nf;
  private Configuration.Builder _cb;
  private Vrf.Builder _vb;
  private Interface.Builder _ib;

  @Before
  public void setup() {
    _nf = new NetworkFactory();
    _cb = _nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    _vb = _nf.vrfBuilder().setName(Configuration.DEFAULT_VRF_NAME);
    _ib = _nf.interfaceBuilder();
  }

  @Test
  public void testAnswerLayer1() {
    String c1Name = "c1";
    String c2Name = "c2";
    String c1a1Name = "c1a1";
    String c1i1aName = "c1i1a";
    String c1i1bName = "c1i1b";
    String c2a1Name = "c2a1";
    String c2i1aName = "c2i1a";
    String c2i1bName = "c2i1b";

    // A pair of physical interfaces on first node is connected to a pair of physical interfaces on
    // second node. On each node, the pair is aggregated into a port-channel in the layer-1 logical
    // topology.
    Configuration c1 = _cb.setHostname(c1Name).build();
    Vrf v1 = _vb.setOwner(c1).build();
    _ib.setOwner(c1).setVrf(v1);
    _ib.setName(c1i1aName).build().setChannelGroup(c1a1Name);
    _ib.setName(c1i1bName).build().setChannelGroup(c1a1Name);
    _ib.setName(c1a1Name).build();

    Configuration c2 = _cb.setHostname(c2Name).build();
    Vrf v2 = _vb.setOwner(c2).build();
    _ib.setOwner(c2).setVrf(v2);
    _ib.setName(c2a1Name).build();
    _ib.setName(c2i1aName).build().setChannelGroup(c2a1Name);
    _ib.setName(c2i1bName).build().setChannelGroup(c2a1Name);

    SortedMap<String, Configuration> configurations = ImmutableSortedMap.of(c1Name, c1, c2Name, c2);
    Layer1Topology layer1PhysicalTopology =
        new Layer1Topology(
            new Layer1Edge(new Layer1Node(c1Name, c1i1aName), new Layer1Node(c2Name, c2i1aName)),
            new Layer1Edge(new Layer1Node(c2Name, c2i1aName), new Layer1Node(c1Name, c1i1aName)),
            new Layer1Edge(new Layer1Node(c1Name, c1i1bName), new Layer1Node(c2Name, c2i1bName)),
            new Layer1Edge(new Layer1Node(c2Name, c2i1bName), new Layer1Node(c1Name, c1i1bName)));

    IBatfishTestAdapter batfish =
        new IBatfishTestAdapter() {
          @Override
          public SortedMap<String, Configuration> loadConfigurations(NetworkSnapshot snapshot) {
            return configurations;
          }

          @Override
          public NetworkSnapshot getSnapshot() {
            return new NetworkSnapshot(new NetworkId("a"), new SnapshotId("b"));
          }

          @Override
          public Map<Location, LocationInfo> getLocationInfo(NetworkSnapshot networkSnapshot) {
            return ImmutableMap.of();
          }

          @Override
          public TopologyProvider getTopologyProvider() {
            return new TopologyProviderTestAdapter(this) {
              @Override
              public Topology getInitialLayer3Topology(NetworkSnapshot networkSnapshot) {
                return new Topology(ImmutableSortedSet.of());
              }

              @Override
              public @Nonnull Optional<Layer1Topology> getRawLayer1PhysicalTopology(
                  NetworkSnapshot networkSnapshot) {
                return Optional.of(layer1PhysicalTopology);
              }

              @Override
              public @Nonnull Optional<Layer1Topology> getSynthesizedLayer1Topology(
                  @Nonnull NetworkSnapshot networkSnapshot) {
                return Optional.of(Layer1Topology.EMPTY);
              }
            };
          }
        };
    TableAnswerElement answer =
        (TableAnswerElement)
            new EdgesAnswerer(new EdgesQuestion(null, null, EdgeType.LAYER1, true), batfish)
                .answer(batfish.getSnapshot());

    // Each node should have an edge to the other node via the port-channel in the layer-1 logical
    // topology.
    assertThat(
        answer.getRowsList(),
        containsInAnyOrder(
            Row.builder()
                .put(COL_INTERFACE, NodeInterfacePair.of(c1Name, c1a1Name))
                .put(COL_REMOTE_INTERFACE, NodeInterfacePair.of(c2Name, c2a1Name))
                .build(),
            Row.builder()
                .put(COL_INTERFACE, NodeInterfacePair.of(c2Name, c2a1Name))
                .put(COL_REMOTE_INTERFACE, NodeInterfacePair.of(c1Name, c1a1Name))
                .build()));
  }

  @Test
  public void testAnswerVxlan() {
    Ip multicastGroup = Ip.parse("224.0.0.1");
    String node1 = "n1";
    String node2 = "n2";
    Ip srcIp1 = Ip.parse("1.1.1.1");
    Ip srcIp2 = Ip.parse("2.2.2.2");
    int udpPort = 5555;
    int vlan1 = 1;
    int vlan2 = 2;
    int vni = 5000;

    Configuration c1 = _cb.setHostname(node1).build();
    Configuration c2 = _cb.setHostname(node2).build();

    Vrf v1 = _vb.setOwner(c1).build();
    Vrf v2 = _vb.setOwner(c2).build();
    SortedMap<String, Configuration> configurations = ImmutableSortedMap.of(node1, c1, node2, c2);
    Layer2Vni.Builder vniSettingsBuilder =
        testBuilder()
            .setBumTransportIps(ImmutableSortedSet.of(multicastGroup))
            .setBumTransportMethod(BumTransportMethod.MULTICAST_GROUP)
            .setUdpPort(udpPort)
            .setVni(vni);
    Layer2Vni vniSettingsTail = vniSettingsBuilder.setSourceAddress(srcIp1).setVlan(vlan1).build();
    v1.setLayer2Vnis(ImmutableSet.of(vniSettingsTail));
    v2.setLayer2Vnis(
        ImmutableSet.of(vniSettingsBuilder.setSourceAddress(srcIp2).setVlan(vlan2).build()));
    IBatfishTestAdapter batfish =
        new IBatfishTestAdapter() {
          @Override
          public SortedMap<String, Configuration> loadConfigurations(NetworkSnapshot snapshot) {
            return configurations;
          }

          @Override
          public TopologyProvider getTopologyProvider() {
            return new TopologyProviderTestAdapter(this) {
              @Override
              public Topology getInitialLayer3Topology(NetworkSnapshot networkSnapshot) {
                return new Topology(ImmutableSortedSet.of());
              }
            };
          }

          @Override
          public NetworkSnapshot getSnapshot() {
            return new NetworkSnapshot(new NetworkId("a"), new SnapshotId("b"));
          }

          @Override
          public Map<Location, LocationInfo> getLocationInfo(NetworkSnapshot networkSnapshot) {
            return ImmutableMap.of();
          }
        };

    TableAnswerElement answer =
        (TableAnswerElement)
            new EdgesAnswerer(new EdgesQuestion(null, null, EdgeType.VXLAN, true), batfish)
                .answer(batfish.getSnapshot());

    assertThat(
        answer.getRowsList(),
        containsInAnyOrder(
            Row.builder()
                .put(COL_VNI, vni)
                .put(COL_NODE, new Node(node1))
                .put(COL_REMOTE_NODE, new Node(node2))
                .put(COL_VTEP_ADDRESS, srcIp1)
                .put(COL_REMOTE_VTEP_ADDRESS, srcIp2)
                .put(COL_VLAN, vlan1)
                .put(COL_REMOTE_VLAN, vlan2)
                .put(COL_UDP_PORT, udpPort)
                .put(COL_MULTICAST_GROUP, multicastGroup)
                .build(),
            Row.builder()
                .put(COL_VNI, vni)
                .put(COL_NODE, new Node(node2))
                .put(COL_REMOTE_NODE, new Node(node1))
                .put(COL_VTEP_ADDRESS, srcIp2)
                .put(COL_REMOTE_VTEP_ADDRESS, srcIp1)
                .put(COL_VLAN, vlan2)
                .put(COL_REMOTE_VLAN, vlan1)
                .put(COL_UDP_PORT, udpPort)
                .put(COL_MULTICAST_GROUP, multicastGroup)
                .build()));
  }
}
