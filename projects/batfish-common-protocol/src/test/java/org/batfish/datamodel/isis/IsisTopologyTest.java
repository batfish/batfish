package org.batfish.datamodel.isis;

import static org.junit.Assert.assertEquals;

import com.google.common.graph.MutableNetwork;
import com.google.common.graph.NetworkBuilder;
import com.google.common.testing.EqualsTester;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Test of {@link IsisTopology}. */
@ParametersAreNonnullByDefault
public final class IsisTopologyTest {

  private static @Nonnull IsisTopology nonTrivialTopology() {
    MutableNetwork<IsisNode, IsisEdge> network =
        NetworkBuilder.directed().allowsParallelEdges(false).allowsSelfLoops(false).build();
    IsisNode n1 = new IsisNode("a", "b");
    IsisNode n2 = new IsisNode("c", "d");
    network.addEdge(n1, n2, new IsisEdge(IsisLevel.LEVEL_1_2, n1, n2));
    return new IsisTopology(network);
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(new Object())
        .addEqualityGroup(
            IsisTopology.EMPTY,
            IsisTopology.EMPTY,
            new IsisTopology(
                NetworkBuilder.directed()
                    .allowsParallelEdges(false)
                    .allowsSelfLoops(false)
                    .build()))
        .addEqualityGroup(nonTrivialTopology())
        .testEquals();
  }

  @Test
  public void testJacksonSerialization() {
    assertEquals(
        nonTrivialTopology(), BatfishObjectMapper.clone(nonTrivialTopology(), IsisTopology.class));
  }
}
