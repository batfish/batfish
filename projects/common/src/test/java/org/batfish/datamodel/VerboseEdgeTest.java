package org.batfish.datamodel;

import com.google.common.testing.EqualsTester;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests of {@link VerboseEdge}. */
@RunWith(JUnit4.class)
public class VerboseEdgeTest {
  /**
   * Tests that two edges that have the same interface definition but different nodes are not equal.
   */
  @Test
  public void testEquals() {
    Interface i1 = TestInterface.builder().setName("eth0").build();
    new EqualsTester()
        .addEqualityGroup(
            new VerboseEdge(i1, i1, Edge.of("node1", "eth0", "node2", "eth0")),
            new VerboseEdge(i1, i1, Edge.of("node1", "eth0", "node2", "eth0")))
        .addEqualityGroup(new VerboseEdge(i1, i1, Edge.of("node2", "eth0", "node1", "eth0")))
        .testEquals();
  }
}
