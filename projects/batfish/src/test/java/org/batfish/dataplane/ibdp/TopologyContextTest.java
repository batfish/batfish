package org.batfish.dataplane.ibdp;

import static org.batfish.datamodel.vxlan.VniLayer.LAYER_2;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import com.google.common.graph.MutableNetwork;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.NetworkBuilder;
import com.google.common.graph.ValueGraphBuilder;
import com.google.common.testing.EqualsTester;
import org.batfish.common.topology.GlobalBroadcastNoPointToPoint;
import org.batfish.common.topology.HybridL3Adjacencies;
import org.batfish.common.topology.Layer1Edge;
import org.batfish.common.topology.Layer1Topologies;
import org.batfish.common.topology.Layer1Topology;
import org.batfish.common.topology.Layer2Topology;
import org.batfish.common.topology.TunnelTopology;
import org.batfish.datamodel.BgpPeerConfigId;
import org.batfish.datamodel.BgpSessionProperties;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.IpsecPeerConfigId;
import org.batfish.datamodel.IpsecSession;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.bgp.BgpTopology;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.eigrp.EigrpEdge;
import org.batfish.datamodel.eigrp.EigrpNeighborConfigId;
import org.batfish.datamodel.eigrp.EigrpTopology;
import org.batfish.datamodel.ipsec.IpsecTopology;
import org.batfish.datamodel.isis.IsisEdge;
import org.batfish.datamodel.isis.IsisNode;
import org.batfish.datamodel.isis.IsisTopology;
import org.batfish.datamodel.ospf.OspfNeighborConfigId;
import org.batfish.datamodel.ospf.OspfSessionProperties;
import org.batfish.datamodel.ospf.OspfTopology;
import org.batfish.datamodel.vxlan.VxlanNode;
import org.batfish.datamodel.vxlan.VxlanTopology;
import org.junit.Test;

/** Test of {@link TopologyContext}. */
public final class TopologyContextTest {

  @Test
  public void testEquals() {
    TopologyContext.Builder builder = TopologyContext.builder();
    TopologyContext base = builder.build();
    MutableValueGraph<BgpPeerConfigId, BgpSessionProperties> bgpTopology =
        ValueGraphBuilder.directed().build();
    bgpTopology.addNode(new BgpPeerConfigId("a", "b", "c"));
    MutableValueGraph<IpsecPeerConfigId, IpsecSession> ipsecTopology =
        ValueGraphBuilder.directed().allowsSelfLoops(false).build();
    ipsecTopology.putEdgeValue(
        new IpsecPeerConfigId("a", "b"),
        new IpsecPeerConfigId("c", "d"),
        IpsecSession.builder().build());
    MutableNetwork<EigrpNeighborConfigId, EigrpEdge> eigrpTopology =
        NetworkBuilder.directed().build();
    eigrpTopology.addNode(new EigrpNeighborConfigId(1L, "a", "b", "c"));
    MutableNetwork<IsisNode, IsisEdge> isisTopology = NetworkBuilder.directed().build();
    isisTopology.addNode(new IsisNode("a", "b"));
    MutableValueGraph<OspfNeighborConfigId, OspfSessionProperties> ospfTopology =
        ValueGraphBuilder.directed().build();
    ospfTopology.addNode(
        new OspfNeighborConfigId(
            "a", "b", "c", "d", ConcreteInterfaceAddress.parse("192.0.2.0/31")));
    MutableGraph<VxlanNode> vxlanTopology =
        GraphBuilder.undirected().allowsSelfLoops(false).build();
    vxlanTopology.addNode(new VxlanNode("a", 5, LAYER_2));

    new EqualsTester()
        .addEqualityGroup(new Object())
        .addEqualityGroup(base, base, builder.build())
        .addEqualityGroup(builder.setBgpTopology(new BgpTopology(bgpTopology)).build())
        .addEqualityGroup(builder.setEigrpTopology(new EigrpTopology(eigrpTopology)).build())
        .addEqualityGroup(builder.setIpsecTopology(new IpsecTopology(ipsecTopology)).build())
        .addEqualityGroup(builder.setIsisTopology(new IsisTopology(isisTopology)).build())
        .addEqualityGroup(
            builder
                .setLayer1Topologies(
                    new Layer1Topologies(
                        new Layer1Topology(new Layer1Edge("a", "b", "c", "d")),
                        Layer1Topology.EMPTY,
                        Layer1Topology.EMPTY,
                        Layer1Topology.EMPTY))
                .build())
        .addEqualityGroup(
            builder
                .setL3Adjacencies(
                    HybridL3Adjacencies.create(
                        Layer1Topologies.empty(), Layer2Topology.EMPTY, ImmutableMap.of()))
                .build())
        .addEqualityGroup(
            builder
                .setLayer3Topology(new Topology(ImmutableSortedSet.of(Edge.of("a", "b", "c", "d"))))
                .build())
        .addEqualityGroup(builder.setOspfTopology(new OspfTopology(ospfTopology)).build())
        .addEqualityGroup(
            builder
                .setTunnelTopology(
                    TunnelTopology.builder()
                        .add(NodeInterfacePair.of("n1", "i1"), NodeInterfacePair.of("n2", "i2"))
                        .build())
                .build())
        .addEqualityGroup(builder.setVxlanTopology(new VxlanTopology(vxlanTopology)).build())
        .testEquals();
  }

  @Test
  public void testToBuilder() {
    TopologyContext.Builder builder = TopologyContext.builder();
    MutableValueGraph<BgpPeerConfigId, BgpSessionProperties> bgpTopology =
        ValueGraphBuilder.directed().build();
    bgpTopology.addNode(new BgpPeerConfigId("a", "b", "c"));
    MutableNetwork<EigrpNeighborConfigId, EigrpEdge> eigrpTopology =
        NetworkBuilder.directed().build();
    eigrpTopology.addNode(new EigrpNeighborConfigId(1L, "a", "b", "c"));
    MutableValueGraph<IpsecPeerConfigId, IpsecSession> ipsecTopology =
        ValueGraphBuilder.directed().allowsSelfLoops(false).build();
    ipsecTopology.putEdgeValue(
        new IpsecPeerConfigId("a", "b"),
        new IpsecPeerConfigId("c", "d"),
        IpsecSession.builder().build());
    MutableNetwork<IsisNode, IsisEdge> isisTopology = NetworkBuilder.directed().build();
    isisTopology.addNode(new IsisNode("a", "b"));
    MutableValueGraph<OspfNeighborConfigId, OspfSessionProperties> ospfTopology =
        ValueGraphBuilder.directed().build();
    ospfTopology.addNode(
        new OspfNeighborConfigId(
            "a", "b", "c", "d", ConcreteInterfaceAddress.parse("192.0.2.0/31")));
    MutableGraph<VxlanNode> vxlanTopology =
        GraphBuilder.undirected().allowsSelfLoops(false).build();
    vxlanTopology.addNode(new VxlanNode("a", 5, LAYER_2));
    builder
        .setBgpTopology(new BgpTopology(bgpTopology))
        .setEigrpTopology(new EigrpTopology(eigrpTopology))
        .setIsisTopology(new IsisTopology(isisTopology))
        .setIpsecTopology(new IpsecTopology(ipsecTopology))
        .setL3Adjacencies(GlobalBroadcastNoPointToPoint.instance())
        .setLayer1Topologies(
            new Layer1Topologies(
                new Layer1Topology(new Layer1Edge("a", "b", "c", "d")),
                Layer1Topology.EMPTY,
                Layer1Topology.EMPTY,
                Layer1Topology.EMPTY))
        .setLayer3Topology(new Topology(ImmutableSortedSet.of(Edge.of("a", "b", "c", "d"))))
        .setOspfTopology(new OspfTopology(ospfTopology))
        .setTunnelTopology(
            TunnelTopology.builder()
                .add(NodeInterfacePair.of("n1", "i1"), NodeInterfacePair.of("n2", "i2"))
                .build())
        .setVxlanTopology(new VxlanTopology(vxlanTopology));

    assertThat(builder.build(), equalTo(builder.build().toBuilder().build()));
  }
}
