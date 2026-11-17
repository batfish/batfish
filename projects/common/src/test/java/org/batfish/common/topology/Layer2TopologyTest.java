package org.batfish.common.topology;

import static org.batfish.common.topology.Layer2Topology.fromEdges;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.ImmutableSet;
import com.google.common.testing.EqualsTester;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.jupiter.api.Test;

final class Layer2TopologyTest {

  @Test
  void testFromEdgesAsymmetric() {
    // should properly register nodes and initialize without error
    fromEdges(ImmutableSet.of(new Layer2Edge("node1", "i", null, "node2", "i", null)));
  }

  @Test
  void testEquals() {
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

  @Test
  void testJacksonSerialization() {
    Layer2Topology topology =
        Layer2Topology.fromDomains(
            ImmutableSet.of(
                ImmutableSet.of(new Layer2Node("a", "b", 1), new Layer2Node("c", "d", 1)),
                ImmutableSet.of(new Layer2Node("e", "f", 1), new Layer2Node("g", "h", 1))));

    assertEquals(topology, BatfishObjectMapper.clone(topology, Layer2Topology.class));
  }
}
