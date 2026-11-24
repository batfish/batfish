package org.batfish.datamodel.routing_policy.expr;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/** Test of {@link IntExprEvaluator}. */
final class IntExprEvaluatorTest {

  @Test
  void testVisitLiteralInt() {
    assertThat(new LiteralInt(1).accept(IntExprEvaluator.instance(), null), equalTo(1));
  }

  @Disabled
  @Test
  void testVisitVarInt() {
    // TODO: support integer variable evaluation
    fail();
  }
}
