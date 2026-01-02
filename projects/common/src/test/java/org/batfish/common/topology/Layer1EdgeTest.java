package org.batfish.common.topology;

import com.google.common.testing.EqualsTester;
import org.junit.Test;

public final class Layer1EdgeTest {
  @Test
  public void testEquals() {
    Layer1Node n1 = new Layer1Node("a", "b");
    Layer1Node n2 = new Layer1Node("c", "d");
    new EqualsTester()
        .addEqualityGroup(new Layer1Edge(n1, n2), new Layer1Edge(n1, n2))
        .addEqualityGroup(new Layer1Edge(n2, n1))
        .testEquals();
  }
}
