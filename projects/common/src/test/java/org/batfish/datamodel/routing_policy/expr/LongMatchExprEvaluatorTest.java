package org.batfish.datamodel.routing_policy.expr;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** Test of {@link LongMatchExprEvaluator}. */
public final class LongMatchExprEvaluatorTest {

  @Test
  public void testVisitLongComparison() {
    assertTrue(
        new LongComparison(IntComparator.EQ, new LiteralLong(1L))
            .accept(LongMatchExprEvaluator.instance(), new LiteralLong(1L)));
    assertFalse(
        new LongComparison(IntComparator.EQ, new LiteralLong(1L))
            .accept(LongMatchExprEvaluator.instance(), new LiteralLong(2L)));
  }

  @Test
  public void testVisitLongMatchAll() {
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
