package org.batfish.datamodel.tracking;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Test;

/** Tests of {@link HsrpPriorityEvaluator} */
public class HsrpPriorityEvaluatorTest {

  @Test
  public void testVisitDecrementPriority() {
    HsrpPriorityEvaluator evaluator = new HsrpPriorityEvaluator(100);
    DecrementPriority decrement25 = new DecrementPriority(25);
    DecrementPriority decrement125 = new DecrementPriority(125);
    DecrementPriority decrementNegative200 = new DecrementPriority(-200);

    assertThat(evaluator.visit(decrement25), equalTo(75));

    // Final priority must be between 0 and 255
    assertThat(evaluator.visit(decrement125), equalTo(0));
    assertThat(evaluator.visit(decrementNegative200), equalTo(255));
  }
}
