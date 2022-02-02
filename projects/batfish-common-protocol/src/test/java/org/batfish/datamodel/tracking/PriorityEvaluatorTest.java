package org.batfish.datamodel.tracking;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Test;

/** Tests of {@link PriorityEvaluator} */
public class PriorityEvaluatorTest {

  @Test
  public void testVisitDecrementPriority() {
    DecrementPriority decrement25 = new DecrementPriority(25);
    DecrementPriority decrement125 = new DecrementPriority(125);
    DecrementPriority decrementNegative200 = new DecrementPriority(-200);

    {
      PriorityEvaluator evaluator = new PriorityEvaluator(100);
      evaluator.visitDecrementPriority(decrement25);
      assertThat(evaluator.getPriority(), equalTo(75));
    }

    // Final priority must be between 0 and 255
    {
      PriorityEvaluator evaluator = new PriorityEvaluator(100);
      evaluator.visitDecrementPriority(decrement125);
      assertThat(evaluator.getPriority(), equalTo(0));
    }
    {
      PriorityEvaluator evaluator = new PriorityEvaluator(100);
      evaluator.visitDecrementPriority(decrementNegative200);
      assertThat(evaluator.getPriority(), equalTo(255));
    }
  }

  @Test
  public void testVisitDecrementPriorityCumulative() {
    PriorityEvaluator evaluator = new PriorityEvaluator(250);
    DecrementPriority decrement25 = new DecrementPriority(25);
    DecrementPriority decrement125 = new DecrementPriority(125);

    evaluator.visitDecrementPriority(decrement25);
    assertThat(evaluator.getPriority(), equalTo(225));

    evaluator.visitDecrementPriority(decrement125);
    assertThat(evaluator.getPriority(), equalTo(100));
  }
}
