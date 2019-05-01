package org.batfish.common.topology;

import static org.batfish.common.topology.Layer2Topology.fromEdges;

import com.google.common.collect.ImmutableSet;
import com.google.common.testing.EqualsTester;
import org.junit.Test;

public final class Layer2TopologyTest {

  @Test
  public void testFromEdgesAsymmetric() {
    // should properly register nodes and initialize without error
    fromEdges(ImmutableSet.of(new Layer2Edge("node1", "i", null, "node2", "i", null, null)));
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(new Object())
        .addEqualityGroup(
            Layer2Topology.EMPTY,
            Layer2Topology.EMPTY,
            Layer2Topology.fromDomains(ImmutableSet.of()))
        .addEqualityGroup(
            Layer2Topology.fromDomains(
                ImmutableSet.of(ImmutableSet.of(new Layer2Node("a", "b", 1)))))
        .testEquals();
  }
}
