package org.batfish.datamodel.ospf;

import static org.batfish.datamodel.ospf.OspfTopology.EMPTY;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableSet;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpLink;
import org.batfish.datamodel.ospf.OspfTopology.EdgeId;
import org.junit.Test;

/** Tests of {@link OspfTopology} */
public final class OspfTopologyTest {
  @Test
  public void testEquals() {
    MutableValueGraph<OspfNeighborConfigId, OspfSessionProperties> g1 =
        ValueGraphBuilder.directed().build();
    g1.addNode(new OspfNeighborConfigId("h1", "vrf1", "p", "i1"));
    MutableValueGraph<OspfNeighborConfigId, OspfSessionProperties> g2 =
        ValueGraphBuilder.from(g1).build();
    g2.addNode(new OspfNeighborConfigId("h2", "vrf2", "p", "i2"));
    new EqualsTester()
        .addEqualityGroup(EMPTY)
        .addEqualityGroup(new OspfTopology(g1), new OspfTopology(g1))
        .addEqualityGroup(g2)
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  public void testEmptyTopology() {
    OspfTopology topo = EMPTY;
    assertThat(topo.getGraph().nodes(), empty());
  }

  @Test
  public void testGetNeighborsNonExistentNode() {
    OspfNeighborConfigId n = new OspfNeighborConfigId("h1", "vrf1", "p", "i1");
    assertThat(EMPTY.neighbors(n), empty());
  }

  @Test
  public void testGetIncomingEdgesNonExistentNode() {
    OspfNeighborConfigId n = new OspfNeighborConfigId("h1", "vrf1", "p", "i1");
    assertThat(EMPTY.incomingEdges(n), empty());
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

  @Test
  public void testJavaSerialization() {
    MutableValueGraph<OspfNeighborConfigId, OspfSessionProperties> graph =
        ValueGraphBuilder.directed().allowsSelfLoops(false).build();
    OspfNeighborConfigId n1 = new OspfNeighborConfigId("a", "b", "c", "d");
    OspfNeighborConfigId n2 = new OspfNeighborConfigId("e", "f", "g", "h");
    OspfSessionProperties v =
        new OspfSessionProperties(
            5L, new IpLink(Ip.FIRST_CLASS_A_PRIVATE_IP, Ip.FIRST_CLASS_B_PRIVATE_IP));
    graph.addNode(n1);
    graph.addNode(n2);
    graph.putEdgeValue(n1, n2, v);
    OspfTopology topology = new OspfTopology(graph);

    assertEquals(topology, SerializationUtils.clone(topology));
  }
}
