package org.batfish.datamodel.isis;

import com.google.common.graph.MutableNetwork;
import com.google.common.graph.NetworkBuilder;
import com.google.common.testing.EqualsTester;
import javax.annotation.ParametersAreNonnullByDefault;
import org.junit.Test;

/** Test of {@link IsisTopology}. */
@ParametersAreNonnullByDefault
public final class IsisTopologyTest {

  @Test
  public void testEquals() {
    MutableNetwork<IsisNode, IsisEdge> network =
        NetworkBuilder.directed().allowsParallelEdges(false).allowsSelfLoops(false).build();
    network.addNode(new IsisNode("a", "b"));

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
        .addEqualityGroup(new IsisTopology(network))
        .testEquals();
  }
}
