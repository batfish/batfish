package org.batfish.datamodel.ospf;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableSet;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import com.google.common.testing.EqualsTester;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpLink;
import org.batfish.datamodel.ospf.OspfTopology.EdgeId;
import org.junit.Test;

/** Tests of {@link OspfTopology} */
public class OspfTopologyTest {
  @Test
  public void testEquals() {
    MutableValueGraph<OspfNeighborConfigId, OspfSessionProperties> g1 =
        ValueGraphBuilder.directed().build();
    g1.addNode(new OspfNeighborConfigId("h1", "vrf1", "p", "i1"));
    MutableValueGraph<OspfNeighborConfigId, OspfSessionProperties> g2 =
        ValueGraphBuilder.from(g1).build();
    g2.addNode(new OspfNeighborConfigId("h2", "vrf2", "p", "i2"));
    new EqualsTester()
        .addEqualityGroup(OspfTopology.empty())
        .addEqualityGroup(new OspfTopology(g1), new OspfTopology(g1))
        .addEqualityGroup(g2)
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  public void testEmptyTopology() {
    OspfTopology topo = OspfTopology.empty();
    assertThat(topo.getGraph().nodes(), empty());
  }

  @Test
  public void testGetNeighborsNonExistentNode() {
    OspfNeighborConfigId n = new OspfNeighborConfigId("h1", "vrf1", "p", "i1");
    assertThat(OspfTopology.empty().neighbors(n), empty());
  }

  @Test
  public void testGetIncomingEdgesNonExistentNode() {
    OspfNeighborConfigId n = new OspfNeighborConfigId("h1", "vrf1", "p", "i1");
    assertThat(OspfTopology.empty().incomingEdges(n), empty());
  }

  @Test
  public void testGetIncomingEdges() {
    // Setup: small topo with one edge
    OspfNeighborConfigId n = new OspfNeighborConfigId("h1", "vrf1", "p", "i1");
    OspfNeighborConfigId n2 = new OspfNeighborConfigId("h2", "vrf2", "p2", "i2");
    MutableValueGraph<OspfNeighborConfigId, OspfSessionProperties> graph =
        ValueGraphBuilder.directed().build();
    graph.addNode(n);
    graph.addNode(n2);
    OspfSessionProperties session =
        new OspfSessionProperties(0, new IpLink(Ip.parse("1.1.1.2"), Ip.parse("1.1.1.3")));
    graph.putEdgeValue(n, n2, session);
    OspfTopology topo = new OspfTopology(graph);

    // One edge only
    assertThat(topo.incomingEdges(n2), equalTo(ImmutableSet.of(OspfTopology.makeEdge(n, n2))));
    // Session can be retrieved from the edge
    assertThat(topo.getSession(topo.incomingEdges(n2).iterator().next()).get(), equalTo(session));
  }

  @Test
  public void testEdgeIdEquals() {
    OspfNeighborConfigId n = new OspfNeighborConfigId("h1", "vrf1", "p", "i1");
    OspfNeighborConfigId n2 = new OspfNeighborConfigId("h2", "vrf2", "p", "i2");
    new EqualsTester()
        .addEqualityGroup(OspfTopology.makeEdge(n, n2), OspfTopology.makeEdge(n, n2))
        .addEqualityGroup(OspfTopology.makeEdge(n, n))
        .addEqualityGroup(OspfTopology.makeEdge(n2, n))
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  public void testEdgeIdReverse() {
    OspfNeighborConfigId n = new OspfNeighborConfigId("h1", "vrf1", "p", "i1");
    OspfNeighborConfigId n2 = new OspfNeighborConfigId("h2", "vrf2", "p", "i2");
    EdgeId edgeId = OspfTopology.makeEdge(n, n2);
    assertThat(edgeId.reverse(), equalTo(OspfTopology.makeEdge(n2, n)));
  }
}
