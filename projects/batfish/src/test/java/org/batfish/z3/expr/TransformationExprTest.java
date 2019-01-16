package org.batfish.z3.expr;

import com.google.common.testing.EqualsTester;
import org.junit.Test;

/** Tests of {@link TransformationExpr}. */
public class TransformationExprTest {
  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            new TransformationExpr("node1", "iface1", "node2", "iface2", "tag1", 1),
            new TransformationExpr("node1", "iface1", "node2", "iface2", "tag1", 1))
        .addEqualityGroup(new TransformationExpr("node1", "iface1", "node2", "iface2", "tag1", 2))
        .addEqualityGroup(new TransformationExpr("node1", "iface1", "node2", "iface2", "tag2", 1))
        .addEqualityGroup(new TransformationExpr("node1", "iface1", "node2", "iface3", "tag1", 1))
        .addEqualityGroup(new TransformationExpr("node1", "iface1", "node3", "iface2", "tag1", 1))
        .addEqualityGroup(new TransformationExpr("node1", "iface2", "node2", "iface2", "tag1", 1))
        .addEqualityGroup(new TransformationExpr("node2", "iface1", "node2", "iface2", "tag1", 1))
        .testEquals();
  }
}
