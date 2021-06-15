package org.batfish.datamodel.tracking;

/**
 * Evaluates the action of a {@link TrackAction} on a given HSRP {@code priority}. Visiting an
 * action returns the updated priority value after applying the action.
 */
public class HsrpPriorityEvaluator implements GenericTrackActionVisitor<Integer> {

  public final int MAX_PRIORITY = 255;
  public final int MIN_PRIORITY = 0;

  public HsrpPriorityEvaluator(int priority) {
    _priority = priority;
  }

  public int getPriority() {
    return _priority;
  }

  @Override
  public Integer visitDecrementPriority(DecrementPriority decrementPriority) {
    _priority =
        Math.min(
            Math.max(_priority - decrementPriority.getSubtrahend(), MIN_PRIORITY), MAX_PRIORITY);

    return _priority;
  }

  private int _priority;
}
