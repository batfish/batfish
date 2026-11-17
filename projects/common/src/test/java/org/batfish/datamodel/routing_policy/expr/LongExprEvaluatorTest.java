package org.batfish.datamodel.routing_policy.expr;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/** Test of {@link LongExprEvaluator}. */
final class LongExprEvaluatorTest {

  @Test
  void testVisitLiteralLong() {
    assertThat(new LiteralLong(1).accept(LongExprEvaluator.instance(), null), equalTo(1L));
  }

  @Test
  void testVisitUint32HighLowExpr() {
    assertThat(
        new Uint32HighLowExpr(new LiteralInt(1), new LiteralInt(1))
            .accept(LongExprEvaluator.instance(), null),
        equalTo((1L << 16) | 1L));
  }

  @Disabled
  @Test
  void testVisitVarLong() {
    // TODO: support integer variable evaluation
    fail();
  }
}
