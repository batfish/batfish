package org.batfish.dataplane.ibdp;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.graph.MutableNetwork;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.NetworkBuilder;
import com.google.common.graph.ValueGraphBuilder;
import com.google.common.testing.EqualsTester;
import org.batfish.common.topology.Layer2Node;
import org.batfish.common.topology.Layer2Topology;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.eigrp.EigrpEdge;
import org.batfish.datamodel.eigrp.EigrpInterface;
import org.batfish.datamodel.isis.IsisEdge;
import org.batfish.datamodel.isis.IsisNode;
import org.batfish.datamodel.ospf.OspfNeighborConfigId;
import org.batfish.datamodel.ospf.OspfSessionProperties;
import org.batfish.datamodel.ospf.OspfTopology;
import org.junit.Test;

/** Test of {@link TopologyContext}. */
public final class TopologyContextTest {

  @Test
  public void testEquals() {
    TopologyContext.Builder builder = TopologyContext.builder();
    TopologyContext base = builder.build();
    MutableNetwork<EigrpInterface, EigrpEdge> eigrpTopology = NetworkBuilder.directed().build();
    eigrpTopology.addNode(new EigrpInterface("a", "b", "c"));
    MutableNetwork<IsisNode, IsisEdge> isisTopology = NetworkBuilder.directed().build();
    isisTopology.addNode(new IsisNode("a", "b"));
    MutableValueGraph<OspfNeighborConfigId, OspfSessionProperties> ospfTopology =
        ValueGraphBuilder.directed().build();
    ospfTopology.addNode(new OspfNeighborConfigId("a", "b", "c", "d"));

    new EqualsTester()
        .addEqualityGroup(new Object())
        .addEqualityGroup(base, base, builder.build())
        .addEqualityGroup(builder.setEigrpTopology(eigrpTopology).build())
        .addEqualityGroup(builder.setIsisTopology(isisTopology).build())
        .addEqualityGroup(
            builder
                .setLayer2Topology(
                    Layer2Topology.fromDomains(
                        ImmutableList.of(ImmutableSet.of(new Layer2Node("a", "b", 5)))))
                .build())
        .addEqualityGroup(
            builder
                .setLayer3Topology(new Topology(ImmutableSortedSet.of(Edge.of("a", "b", "c", "d"))))
                .build())
        .addEqualityGroup(builder.setOspfTopology(new OspfTopology(ospfTopology)).build())
        .testEquals();
  }
}
