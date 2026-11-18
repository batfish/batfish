package org.batfish.datamodel.routing_policy.expr;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Test of {@link LongMatchExprEvaluator}. */
final class LongMatchExprEvaluatorTest {

  @Test
  void testVisitLongComparison() {
    assertTrue(
        new LongComparison(IntComparator.EQ, new LiteralLong(1L))
            .accept(LongMatchExprEvaluator.instance(), new LiteralLong(1L)));
    assertFalse(
        new LongComparison(IntComparator.EQ, new LiteralLong(1L))
            .accept(LongMatchExprEvaluator.instance(), new LiteralLong(2L)));
  }

  @Test
  void testVisitLongMatchAll() {
    // empty matches everything
    assertTrue(LongMatchAll.of().accept(LongMatchExprEvaluator.instance(), new LiteralLong(1)));

    assertTrue(
        LongMatchAll.of(
                new LongComparison(IntComparator.EQ, new LiteralLong(1L)),
                new LongComparison(IntComparator.EQ, new LiteralLong(1L)))
            .accept(LongMatchExprEvaluator.instance(), new LiteralLong(1L)));
    assertFalse(
        LongMatchAll.of(
                new LongComparison(IntComparator.EQ, new LiteralLong(1L)),
                new LongComparison(IntComparator.EQ, new LiteralLong(2L)))
            .accept(LongMatchExprEvaluator.instance(), new LiteralLong(1L)));
  }
}
