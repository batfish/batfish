package org.batfish.datamodel.vxlan;

import static org.batfish.datamodel.vxlan.VxlanTopology.EMPTY;
import static org.junit.Assert.assertEquals;

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
    graph.putEdge(new VxlanNode("a", 1), new VxlanNode("b", 2));
    return new VxlanTopology(graph);
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
