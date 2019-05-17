package org.batfish.common.topology;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

/** Tests of {@link Layer1Topology} */
public class Layer1TopologyTest {

  @Test
  public void testConstructorIgnoresParallelEdge() {
    // Two equivalent edges. Resulting topology should contain only one copy of the edge.
    Layer1Edge e1 = new Layer1Edge("c1", "i1", "c2", "i2");
    Layer1Edge e2 = new Layer1Edge("c1", "i1", "c2", "i2");
    assertThat(new Layer1Topology(ImmutableList.of(e1, e2)).getGraph().edges(), contains(e1));
  }

  @Test
  public void testConstructorIgnoresSelfLoop() {
    // Edge with equivalent endpoints should be ignored, resulting in an empty topology.
    Layer1Edge selfEdge = new Layer1Edge("c1", "i1", "c1", "i1");
    assertThat(new Layer1Topology(ImmutableList.of(selfEdge)), equalTo(Layer1Topology.EMPTY));
  }

  @Test
  public void testConstructorDoesNotIgnoreEdgeBetweenInterfacesOnSameNode() {
    // Edge between two different interfaces on the same node should not be ignored.
    Layer1Edge e = new Layer1Edge("c1", "i1", "c1", "i2");
    assertThat(new Layer1Topology(ImmutableList.of(e)).getGraph().edges(), contains(e));
  }
}
