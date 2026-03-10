package org.batfish.datamodel.routing_policy.expr;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** Test of {@link IntMatchExprEvaluator}. */
public final class IntMatchExprEvaluatorTest {

  @Test
  public void testVisitIntComparison() {
    assertTrue(
        new IntComparison(IntComparator.EQ, new LiteralInt(1))
            .accept(IntMatchExprEvaluator.instance(), new LiteralInt(1)));
    assertFalse(
        new IntComparison(IntComparator.EQ, new LiteralInt(1))
            .accept(IntMatchExprEvaluator.instance(), new LiteralInt(2)));
  }

  @Test
  public void testVisitIntMatchAll() {
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
