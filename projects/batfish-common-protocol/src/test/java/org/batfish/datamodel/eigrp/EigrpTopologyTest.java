package org.batfish.datamodel.eigrp;

import com.google.common.graph.MutableNetwork;
import com.google.common.graph.NetworkBuilder;
import com.google.common.testing.EqualsTester;
import javax.annotation.ParametersAreNonnullByDefault;
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
}
