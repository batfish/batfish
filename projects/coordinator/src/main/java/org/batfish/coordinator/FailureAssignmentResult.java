package org.batfish.coordinator;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Work could not be assigned because of a transient problem. Assignment should be attempted again.
 */
@ParametersAreNonnullByDefault
public final class FailureAssignmentResult implements AssignmentResult {

  public FailureAssignmentResult(QueuedWork work) {
    _work = work;
  }

  @Override
  public void applyTo(WorkQueueMgr mgr) {
    mgr.markAssignmentFailure(_work);
  }

  private final @Nonnull QueuedWork _work;
}
