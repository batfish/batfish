package org.batfish.datamodel.vxlan;

import static org.batfish.datamodel.vxlan.VniLayer.LAYER_2;
import static org.batfish.datamodel.vxlan.VniLayer.LAYER_3;
import static org.batfish.datamodel.vxlan.VxlanTopology.EMPTY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableSet;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import com.google.common.testing.EqualsTester;
import javax.annotation.Nonnull;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Test of {@link VxlanTopology}. */
public final class VxlanTopologyTest {

  private static @Nonnull VxlanTopology nonTrivialTopology() {
    MutableGraph<VxlanNode> graph = GraphBuilder.undirected().allowsSelfLoops(false).build();
    graph.putEdge(new VxlanNode("a", 1, LAYER_2), new VxlanNode("b", 2, LAYER_2));
    graph.putEdge(new VxlanNode("a", 3, LAYER_3), new VxlanNode("b", 4, LAYER_3));
    return new VxlanTopology(graph);
  }

  @Test
  public void testGetLayer2VniEdges() {
    assertThat(
        nonTrivialTopology().getLayer2VniEdges().collect(ImmutableSet.toImmutableSet()),
        contains(
            EndpointPair.unordered(
                new VxlanNode("a", 1, LAYER_2), new VxlanNode("b", 2, LAYER_2))));
  }

  @Test
  public void testGetLayer3VniEdges() {
    assertThat(
        nonTrivialTopology().getLayer3VniEdges().collect(ImmutableSet.toImmutableSet()),
        contains(
            EndpointPair.unordered(
                new VxlanNode("a", 3, LAYER_3), new VxlanNode("b", 4, LAYER_3))));
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
  public void testJacksonSerialization() {
    assertEquals(
        nonTrivialTopology(), BatfishObjectMapper.clone(nonTrivialTopology(), VxlanTopology.class));
  }
}
