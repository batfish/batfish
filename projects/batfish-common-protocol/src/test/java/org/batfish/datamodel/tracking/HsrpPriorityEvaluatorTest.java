package org.batfish.datamodel.tracking;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Test;

/** Tests of {@link HsrpPriorityEvaluator} */
public class HsrpPriorityEvaluatorTest {

  @Test
  public void testVisitDecrementPriority() {
    DecrementPriority decrement25 = new DecrementPriority(25);
    DecrementPriority decrement125 = new DecrementPriority(125);
    DecrementPriority decrementNegative200 = new DecrementPriority(-200);

    {
      HsrpPriorityEvaluator evaluator = new HsrpPriorityEvaluator(100);
      evaluator.visitDecrementPriority(decrement25);
      assertThat(evaluator.getPriority(), equalTo(75));
    }

    // Final priority must be between 0 and 255
    {
      HsrpPriorityEvaluator evaluator = new HsrpPriorityEvaluator(100);
      evaluator.visitDecrementPriority(decrement125);
      assertThat(evaluator.getPriority(), equalTo(0));
    }
    {
      HsrpPriorityEvaluator evaluator = new HsrpPriorityEvaluator(100);
      evaluator.visitDecrementPriority(decrementNegative200);
      assertThat(evaluator.getPriority(), equalTo(255));
    }
  }

  @Test
  public void testVisitDecrementPriorityCumulative() {
    HsrpPriorityEvaluator evaluator = new HsrpPriorityEvaluator(250);
    DecrementPriority decrement25 = new DecrementPriority(25);
    DecrementPriority decrement125 = new DecrementPriority(125);

    evaluator.visitDecrementPriority(decrement25);
    assertThat(evaluator.getPriority(), equalTo(225));

    evaluator.visitDecrementPriority(decrement125);
    assertThat(evaluator.getPriority(), equalTo(100));
  }
}
