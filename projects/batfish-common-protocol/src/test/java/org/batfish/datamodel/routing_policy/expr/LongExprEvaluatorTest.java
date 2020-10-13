package org.batfish.datamodel.routing_policy.expr;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Ignore;
import org.junit.Test;

/** Test of {@link LongExprEvaluator}. */
public final class LongExprEvaluatorTest {

  @Test
  public void testVisitLiteralLong() {
    assertThat(new LiteralLong(1).accept(LongExprEvaluator.instance(), null), equalTo(1L));
  }

  @Test
  public void testVisitUint32HighLowExpr() {
    assertThat(
        new Uint32HighLowExpr(new LiteralInt(1), new LiteralInt(1))
            .accept(LongExprEvaluator.instance(), null),
        equalTo((1L << 16) | 1L));
  }

  @Ignore
  @Test
  public void testVisitVarLong() {
    // TODO: support integer variable evaluation
    assert false;
  }
}
