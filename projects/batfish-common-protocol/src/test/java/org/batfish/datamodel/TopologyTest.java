package org.batfish.datamodel;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.util.Set;
import java.util.SortedSet;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.junit.Test;

public class TopologyTest {

  // Set up two edges with 3 nodes with 1 interface each:
  // n1:i1 -> n2:i2
  // n2:i2 -> n3:i3
  NodeInterfacePair _nip1 = new NodeInterfacePair("n1", "i1");
  NodeInterfacePair _nip2 = new NodeInterfacePair("n2", "i2");
  NodeInterfacePair _nip3 = new NodeInterfacePair("n3", "i3");
  Set<Edge> _edge1to2Set = ImmutableSet.of(new Edge(_nip1, _nip2));
  Set<Edge> _edge2to3Set = ImmutableSet.of(new Edge(_nip2, _nip3));
  SortedSet<Edge> _bothEdges =
      ImmutableSortedSet.of(new Edge(_nip1, _nip2), new Edge(_nip2, _nip3));

  @Test
  public void testEmptyTopology() {
    SortedSet<Edge> edges = ImmutableSortedSet.of();
    Topology topo = new Topology(edges);

    assertThat(topo.getEdges(), equalTo(ImmutableSet.of()));
    assertThat(topo.getInterfaceEdges(), equalTo(ImmutableMap.of()));
    assertThat(topo.getNodeEdges(), equalTo(ImmutableMap.of()));
    assertThat(
        topo.getNeighbors(new NodeInterfacePair("node", "iface")), equalTo(ImmutableSet.of()));
  }

  @Test
  public void testOneEdge() {
    NodeInterfacePair nip1 = new NodeInterfacePair("n1", "i1");
    NodeInterfacePair nip2 = new NodeInterfacePair("n2", "i2");
    Edge edge = new Edge(nip1, nip2);
    SortedSet<Edge> edges = ImmutableSortedSet.of(edge);
    Topology topo = new Topology(edges);

    assertThat(topo.getEdges(), equalTo(edges));
    assertThat(topo.getInterfaceEdges(), equalTo(ImmutableMap.of(nip1, edges, nip2, edges)));
    assertThat(topo.getNodeEdges(), equalTo(ImmutableMap.of("n1", edges, "n2", edges)));
    assertThat(topo.getNeighbors(nip1), equalTo(ImmutableSet.of(nip2)));
    assertThat(topo.getNeighbors(nip2), equalTo(ImmutableSet.of()));
    assertThat(topo.getNeighbors(new NodeInterfacePair("x", "y")), equalTo(ImmutableSet.of()));
  }

  @Test
  public void testTwoEdges() {
    Topology topo = new Topology(_bothEdges);
    assertThat(topo.getEdges(), equalTo(_bothEdges));
    assertThat(
        topo.getInterfaceEdges(),
        equalTo(ImmutableMap.of(_nip1, _edge1to2Set, _nip2, _bothEdges, _nip3, _edge2to3Set)));
    assertThat(
        topo.getNodeEdges(),
        equalTo(ImmutableMap.of("n1", _edge1to2Set, "n2", _bothEdges, "n3", _edge2to3Set)));
    assertThat(topo.getNeighbors(_nip1), equalTo(ImmutableSet.of(_nip2)));
    assertThat(topo.getNeighbors(_nip2), equalTo(ImmutableSet.of(_nip3)));
    assertThat(topo.getNeighbors(_nip3), equalTo(ImmutableSet.of()));
  }

  @Test
  public void testPruneEdge() {
    Topology topo = new Topology(_bothEdges);
    topo.prune(_edge1to2Set, null, null);
    assertThat(topo.getEdges(), equalTo(_edge2to3Set));
    assertThat(
        topo.getInterfaceEdges(),
        equalTo(ImmutableMap.of(_nip2, _edge2to3Set, _nip3, _edge2to3Set)));
    assertThat(
        topo.getNodeEdges(), equalTo(ImmutableMap.of("n2", _edge2to3Set, "n3", _edge2to3Set)));
    assertThat(topo.getNeighbors(_nip1), equalTo(ImmutableSet.of()));
    assertThat(topo.getNeighbors(_nip2), equalTo(ImmutableSet.of(_nip3)));
    assertThat(topo.getNeighbors(_nip3), equalTo(ImmutableSet.of()));
  }

  @Test
  public void testPruneNode1() {
    Topology topo = new Topology(_bothEdges);
    topo.prune(null, ImmutableSet.of("n1"), null);
    assertThat(topo.getEdges(), equalTo(_edge2to3Set));
    assertThat(
        topo.getInterfaceEdges(),
        equalTo(ImmutableMap.of(_nip2, _edge2to3Set, _nip3, _edge2to3Set)));
    assertThat(
        topo.getNodeEdges(), equalTo(ImmutableMap.of("n2", _edge2to3Set, "n3", _edge2to3Set)));
    assertThat(topo.getNeighbors(_nip2), equalTo(ImmutableSet.of(_nip3)));
    assertThat(topo.getNeighbors(_nip3), equalTo(ImmutableSet.of()));
  }

  @Test
  public void testPruneNode2() {
    Topology topo = new Topology(_bothEdges);
    topo.prune(null, ImmutableSet.of("n2"), null);
    assertThat(topo.getEdges(), equalTo(ImmutableSet.of()));
    assertThat(topo.getInterfaceEdges(), equalTo(ImmutableMap.of()));
    assertThat(topo.getNodeEdges(), equalTo(ImmutableMap.of()));
    assertThat(topo.getNeighbors(_nip1), equalTo(ImmutableSet.of()));
    assertThat(topo.getNeighbors(_nip3), equalTo(ImmutableSet.of()));
  }

  @Test
  public void testPruneNode3() {
    Topology topo = new Topology(_bothEdges);
    topo.prune(null, ImmutableSet.of("n3"), null);
    assertThat(topo.getEdges(), equalTo(_edge1to2Set));
    assertThat(
        topo.getInterfaceEdges(),
        equalTo(ImmutableMap.of(_nip1, _edge1to2Set, _nip2, _edge1to2Set)));
    assertThat(
        topo.getNodeEdges(), equalTo(ImmutableMap.of("n1", _edge1to2Set, "n2", _edge1to2Set)));
    assertThat(topo.getNeighbors(_nip1), equalTo(ImmutableSet.of(_nip2)));
    assertThat(topo.getNeighbors(_nip2), equalTo(ImmutableSet.of()));
  }

  @Test
  public void testPruneInterface1() {
    Topology topo = new Topology(_bothEdges);
    topo.prune(null, null, ImmutableSet.of(_nip1));
    assertThat(topo.getEdges(), equalTo(_edge2to3Set));
    assertThat(
        topo.getInterfaceEdges(),
        equalTo(ImmutableMap.of(_nip2, _edge2to3Set, _nip3, _edge2to3Set)));
    assertThat(
        topo.getNodeEdges(), equalTo(ImmutableMap.of("n2", _edge2to3Set, "n3", _edge2to3Set)));
    assertThat(topo.getNeighbors(_nip2), equalTo(ImmutableSet.of(_nip3)));
    assertThat(topo.getNeighbors(_nip3), equalTo(ImmutableSet.of()));
  }

  @Test
  public void testPruneInterface2() {
    Topology topo = new Topology(_bothEdges);
    topo.prune(null, null, ImmutableSet.of(_nip2));
    assertThat(topo.getEdges(), equalTo(ImmutableSet.of()));
    assertThat(topo.getInterfaceEdges(), equalTo(ImmutableMap.of()));
    assertThat(topo.getNodeEdges(), equalTo(ImmutableMap.of()));
    assertThat(topo.getNeighbors(_nip1), equalTo(ImmutableSet.of()));
    assertThat(topo.getNeighbors(_nip3), equalTo(ImmutableSet.of()));
  }

  @Test
  public void testPruneInterface3() {
    Topology topo = new Topology(_bothEdges);
    topo.prune(null, null, ImmutableSet.of(_nip3));
    assertThat(topo.getEdges(), equalTo(_edge1to2Set));
    assertThat(
        topo.getInterfaceEdges(),
        equalTo(ImmutableMap.of(_nip1, _edge1to2Set, _nip2, _edge1to2Set)));
    assertThat(
        topo.getNodeEdges(), equalTo(ImmutableMap.of("n1", _edge1to2Set, "n2", _edge1to2Set)));
    assertThat(topo.getNeighbors(_nip1), equalTo(ImmutableSet.of(_nip2)));
    assertThat(topo.getNeighbors(_nip2), equalTo(ImmutableSet.of()));
  }
}
