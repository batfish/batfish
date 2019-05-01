package org.batfish.datamodel.eigrp;

import static org.junit.Assert.assertEquals;

import com.google.common.graph.MutableNetwork;
import com.google.common.graph.NetworkBuilder;
import com.google.common.testing.EqualsTester;
import javax.annotation.ParametersAreNonnullByDefault;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;

/** Test of {@link EigrpTopology}. */
@ParametersAreNonnullByDefault
public final class EigrpTopologyTest {

  @Test
  public void testEquals() {
    MutableNetwork<EigrpInterface, EigrpEdge> network =
        NetworkBuilder.directed().allowsParallelEdges(false).allowsSelfLoops(false).build();
    network.addNode(new EigrpInterface("a", "b", "c"));

    new EqualsTester()
        .addEqualityGroup(new Object())
        .addEqualityGroup(
            EigrpTopology.EMPTY,
            EigrpTopology.EMPTY,
            new EigrpTopology(
                NetworkBuilder.directed()
                    .allowsParallelEdges(false)
                    .allowsSelfLoops(false)
                    .build()))
        .addEqualityGroup(new EigrpTopology(network))
        .testEquals();
  }

  @Test
  public void testJavaSerialization() {
    MutableNetwork<EigrpInterface, EigrpEdge> network =
        NetworkBuilder.directed().allowsParallelEdges(false).allowsSelfLoops(false).build();
    EigrpInterface n1 = new EigrpInterface("a", "b", "c");
    EigrpInterface n2 = new EigrpInterface("d", "e", "f");
    network.addNode(n1);
    network.addNode(n2);
    network.addEdge(n1, n2, new EigrpEdge(n1, n2));
    EigrpTopology topology = new EigrpTopology(network);

    assertEquals(topology, SerializationUtils.clone(topology));
  }
}
