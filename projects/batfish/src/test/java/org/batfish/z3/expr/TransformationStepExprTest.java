package org.batfish.z3.expr;

import com.google.common.testing.EqualsTester;
import org.junit.Test;

/** Tests of {@link TransformationStepExpr}. */
public class TransformationStepExprTest {

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            new TransformationStepExpr("node1", "iface1", "tag1", 1, 1),
            new TransformationStepExpr("node1", "iface1", "tag1", 1, 1))
        .addEqualityGroup(new TransformationStepExpr("node1", "iface1", "tag1", 1, 2))
        .addEqualityGroup(new TransformationStepExpr("node1", "iface1", "tag1", 2, 1))
        .addEqualityGroup(new TransformationStepExpr("node1", "iface1", "tag2", 1, 1))
        .addEqualityGroup(new TransformationStepExpr("node1", "iface2", "tag1", 1, 1))
        .addEqualityGroup(new TransformationStepExpr("node2", "iface1", "tag1", 1, 1))
        .testEquals();
  }
}
