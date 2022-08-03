package org.batfish.coordinator;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/** The work could not be assigned because of an unrecoverable error. */
@ParametersAreNonnullByDefault
public final class ErrorAssignmentResult implements AssignmentResult {

  public ErrorAssignmentResult(QueuedWork work) {
    _work = work;
  }

  @Override
  public void applyTo(WorkQueueMgr mgr) {
    mgr.markAssignmentError(_work);
  }

  private final @Nonnull QueuedWork _work;
}
