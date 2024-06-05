package org.batfish.datamodel.ospf;

import static org.batfish.datamodel.ospf.OspfTopology.EMPTY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableSet;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import com.google.common.testing.EqualsTester;
import javax.annotation.Nonnull;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpLink;
import org.batfish.datamodel.ospf.OspfTopology.EdgeId;
import org.junit.Test;

/** Tests of {@link OspfTopology} */
public final class OspfTopologyTest {

  private static @Nonnull OspfTopology nonTrivialTopology() {
    MutableValueGraph<OspfNeighborConfigId, OspfSessionProperties> graph =
        ValueGraphBuilder.directed().allowsSelfLoops(false).build();
    graph.putEdgeValue(
        new OspfNeighborConfigId(
            "a", "b", "c", "d", ConcreteInterfaceAddress.create(Ip.FIRST_CLASS_A_PRIVATE_IP, 1)),
        new OspfNeighborConfigId(
            "e", "f", "g", "h", ConcreteInterfaceAddress.create(Ip.FIRST_CLASS_B_PRIVATE_IP, 1)),
        new OspfSessionProperties(
            5L, new IpLink(Ip.FIRST_CLASS_A_PRIVATE_IP, Ip.FIRST_CLASS_B_PRIVATE_IP)));
    return new OspfTopology(graph);
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            EMPTY,
            EMPTY,
            new OspfTopology(ValueGraphBuilder.directed().allowsSelfLoops(false).build()))
        .addEqualityGroup(nonTrivialTopology())
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
    OspfNeighborConfigId n =
        new OspfNeighborConfigId(
            "h1", "vrf1", "p", "i1", ConcreteInterfaceAddress.parse("192.0.2.0/31"));
    assertThat(EMPTY.neighbors(n), empty());
  }

  @Test
  public void testGetIncomingEdgesNonExistentNode() {
    OspfNeighborConfigId n =
        new OspfNeighborConfigId(
            "h1", "vrf1", "p", "i1", ConcreteInterfaceAddress.parse("192.0.2.0/31"));
    assertThat(EMPTY.incomingEdges(n), empty());
  }

  @Test
  public void testGetIncomingEdges() {
    // Setup: small topo with one edge
    OspfNeighborConfigId n =
        new OspfNeighborConfigId(
            "h1", "vrf1", "p", "i1", ConcreteInterfaceAddress.parse("192.0.2.0/31"));
    OspfNeighborConfigId n2 =
        new OspfNeighborConfigId(
            "h2", "vrf2", "p2", "i2", ConcreteInterfaceAddress.parse("192.0.2.1/31"));
    MutableValueGraph<OspfNeighborConfigId, OspfSessionProperties> graph =
        ValueGraphBuilder.directed().build();
    graph.addNode(n);
    graph.addNode(n2);
    OspfSessionProperties session =
        new OspfSessionProperties(0, new IpLink(Ip.parse("192.0.2.0"), Ip.parse("192.0.2.1")));
    graph.putEdgeValue(n, n2, session);
    OspfTopology topo = new OspfTopology(graph);

    // One edge only
    assertThat(topo.incomingEdges(n2), equalTo(ImmutableSet.of(OspfTopology.makeEdge(n, n2))));
    // Session can be retrieved from the edge
    assertThat(topo.getSession(topo.incomingEdges(n2).iterator().next()).get(), equalTo(session));
  }

  @Test
  public void testEdgeIdEquals() {
    OspfNeighborConfigId n =
        new OspfNeighborConfigId(
            "h1", "vrf1", "p", "i1", ConcreteInterfaceAddress.parse("192.0.2.0/31"));
    OspfNeighborConfigId n2 =
        new OspfNeighborConfigId(
            "h2", "vrf2", "p", "i2", ConcreteInterfaceAddress.parse("192.0.2.1/31"));
    new EqualsTester()
        .addEqualityGroup(OspfTopology.makeEdge(n, n2), OspfTopology.makeEdge(n, n2))
        .addEqualityGroup(OspfTopology.makeEdge(n, n))
        .addEqualityGroup(OspfTopology.makeEdge(n2, n))
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  public void testEdgeIdReverse() {
    OspfNeighborConfigId n =
        new OspfNeighborConfigId(
            "h1", "vrf1", "p", "i1", ConcreteInterfaceAddress.parse("192.0.2.0/31"));
    OspfNeighborConfigId n2 =
        new OspfNeighborConfigId(
            "h2", "vrf2", "p", "i2", ConcreteInterfaceAddress.parse("192.0.2.1/31"));
    EdgeId edgeId = OspfTopology.makeEdge(n, n2);
    assertThat(edgeId.reverse(), equalTo(OspfTopology.makeEdge(n2, n)));
  }

  @Test
  public void testJacksonSerialization() {
    assertEquals(
        nonTrivialTopology(), BatfishObjectMapper.clone(nonTrivialTopology(), OspfTopology.class));
  }
}
