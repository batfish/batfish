package org.batfish.common.topology;

import static org.batfish.common.topology.Layer2Topology.fromEdges;

import com.google.common.collect.ImmutableSet;
import org.junit.Test;

public final class Layer2TopologyTest {

  @Test
  public void testFromEdgesAsymmetric() {
    // should properly register nodes and initialize without error
    fromEdges(ImmutableSet.of(new Layer2Edge("node1", "i", null, "node2", "i", null, null)));
  }
}
