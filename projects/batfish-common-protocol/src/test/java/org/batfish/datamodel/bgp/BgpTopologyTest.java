package org.batfish.datamodel.bgp;

import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import com.google.common.testing.EqualsTester;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.BgpPeerConfigId;
import org.batfish.datamodel.BgpSessionProperties;
import org.junit.Test;

/** Test of {@link BgpTopology}. */
@ParametersAreNonnullByDefault
public final class BgpTopologyTest {

  @Test
  public void testEquals() {
    MutableValueGraph<BgpPeerConfigId, BgpSessionProperties> graph =
        ValueGraphBuilder.directed().allowsSelfLoops(false).build();
    graph.addNode(new BgpPeerConfigId("a", "b", "c"));

    new EqualsTester()
        .addEqualityGroup(new Object())
        .addEqualityGroup(
            BgpTopology.EMPTY,
            BgpTopology.EMPTY,
            new BgpTopology(ValueGraphBuilder.directed().allowsSelfLoops(false).build()))
        .addEqualityGroup(new BgpTopology(graph))
        .testEquals();
  }
}
