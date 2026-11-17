package org.batfish.common.topology;

import com.google.common.testing.EqualsTester;
import org.junit.jupiter.api.Test;

final class Layer1NodeTest {
  @Test
  void testEquals() {
    new EqualsTester()
        .addEqualityGroup(new Layer1Node("a", "b"), new Layer1Node("A", "b"))
        .addEqualityGroup(new Layer1Node("a", "c"))
        .addEqualityGroup(new Layer1Node("d", "c"))
        .addEqualityGroup(new Layer1Node("d", "C"))
        .testEquals();
  }
}
