package org.batfish.common.topology;

import static org.batfish.common.topology.Layer1Topology.EMPTY;

import com.google.common.testing.EqualsTester;
import org.junit.Test;

/** Tests of {@link Layer1Topologies} */
public final class Layer1TopologiesTest {
  @Test
  public void testEquals() {
    Layer1Topology notEmpty = new Layer1Topology(new Layer1Edge("n1", "i1", "n2", "i2"));
    new EqualsTester()
        .addEqualityGroup(
            Layer1Topologies.empty(), new Layer1Topologies(EMPTY, EMPTY, EMPTY, EMPTY))
        .addEqualityGroup(new Layer1Topologies(notEmpty, EMPTY, EMPTY, EMPTY))
        .addEqualityGroup(new Layer1Topologies(EMPTY, notEmpty, EMPTY, EMPTY))
        .addEqualityGroup(new Layer1Topologies(EMPTY, EMPTY, notEmpty, EMPTY))
        .addEqualityGroup(new Layer1Topologies(EMPTY, EMPTY, EMPTY, notEmpty))
        .testEquals();
  }
}
