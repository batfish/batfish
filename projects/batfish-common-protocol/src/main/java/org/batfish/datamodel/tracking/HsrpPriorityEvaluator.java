package org.batfish.datamodel.tracking;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Evaluates the action of a {@link TrackAction} on a given HSRP {@code priority}. Visiting an
 * action updates evaluator's priority value according to the action.
 */
@ParametersAreNonnullByDefault
public class HsrpPriorityEvaluator implements GenericTrackActionVisitor {

  public final int MAX_PRIORITY = 255;
  public final int MIN_PRIORITY = 0;

  public HsrpPriorityEvaluator(int priority) {
    _priority = priority;
  }

  public int getPriority() {
    return _priority;
  }

  @Override
  public void visitDecrementPriority(DecrementPriority decrementPriority) {
    _priority =
        Math.min(
            Math.max(_priority - decrementPriority.getSubtrahend(), MIN_PRIORITY), MAX_PRIORITY);
  }

  private int _priority;
}
