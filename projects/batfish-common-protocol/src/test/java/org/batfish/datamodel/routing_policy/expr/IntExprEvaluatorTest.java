package org.batfish.datamodel.routing_policy.expr;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Ignore;
import org.junit.Test;

/** Test of {@link IntExprEvaluator}. */
public final class IntExprEvaluatorTest {

  @Test
  public void testVisitLiteralInt() {
    assertThat(new LiteralInt(1).accept(IntExprEvaluator.instance(), null), equalTo(1));
  }

  @Ignore
  @Test
  public void testVisitVarInt() {
    // TODO: support integer variable evaluation
    assert false;
  }
}
