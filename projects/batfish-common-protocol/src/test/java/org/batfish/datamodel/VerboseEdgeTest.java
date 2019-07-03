package org.batfish.datamodel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

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
    Interface i1 = Interface.builder().setName("eth0").build();
    VerboseEdge edge1 = new VerboseEdge(i1, i1, Edge.of("node1", "eth0", "node2", "eth0"));
    assertEquals(edge1, edge1);

    VerboseEdge edge2 = new VerboseEdge(i1, i1, Edge.of("node2", "eth0", "node1", "eth0"));
    assertNotEquals(edge1, edge2);
  }
}
