package org.batfish.datamodel.routing_policy.expr;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Test of {@link IntMatchExprEvaluator}. */
final class IntMatchExprEvaluatorTest {

  @Test
  void testVisitIntComparison() {
    assertTrue(
        new IntComparison(IntComparator.EQ, new LiteralInt(1))
            .accept(IntMatchExprEvaluator.instance(), new LiteralInt(1)));
    assertFalse(
        new IntComparison(IntComparator.EQ, new LiteralInt(1))
            .accept(IntMatchExprEvaluator.instance(), new LiteralInt(2)));
  }

  @Test
  void testVisitIntMatchAll() {
    // empty matches everything
    assertTrue(IntMatchAll.of().accept(IntMatchExprEvaluator.instance(), new LiteralInt(1)));

    assertTrue(
        IntMatchAll.of(
                new IntComparison(IntComparator.EQ, new LiteralInt(1)),
                new IntComparison(IntComparator.EQ, new LiteralInt(1)))
            .accept(IntMatchExprEvaluator.instance(), new LiteralInt(1)));
    assertFalse(
        IntMatchAll.of(
                new IntComparison(IntComparator.EQ, new LiteralInt(1)),
                new IntComparison(IntComparator.EQ, new LiteralInt(2)))
            .accept(IntMatchExprEvaluator.instance(), new LiteralInt(1)));
  }
}
