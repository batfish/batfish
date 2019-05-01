package org.batfish.datamodel.isis;

import static org.junit.Assert.assertEquals;

import com.google.common.graph.MutableNetwork;
import com.google.common.graph.NetworkBuilder;
import com.google.common.testing.EqualsTester;
import javax.annotation.ParametersAreNonnullByDefault;
import org.apache.commons.lang3.SerializationUtils;
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

  @Test
  public void testJavaSerialization() {
    MutableNetwork<IsisNode, IsisEdge> network =
        NetworkBuilder.directed().allowsParallelEdges(false).allowsSelfLoops(false).build();
    IsisNode n1 = new IsisNode("a", "b");
    IsisNode n2 = new IsisNode("c", "d");
    network.addNode(n1);
    network.addNode(n2);
    network.addEdge(n1, n2, new IsisEdge(IsisLevel.LEVEL_1_2, n1, n2));
    IsisTopology topology = new IsisTopology(network);

    assertEquals(topology, SerializationUtils.clone(topology));
  }
}
