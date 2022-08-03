package org.batfish.coordinator;

import java.io.IOException;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/** The work was successfully assigned and given a task handle. */
@ParametersAreNonnullByDefault
public final class SuccessAssignmentResult implements AssignmentResult {

  public SuccessAssignmentResult(QueuedWork work, TaskHandle handle) {
    _work = work;
    _handle = handle;
  }

  @Override
  public void applyTo(WorkQueueMgr mgr) throws IOException {
    mgr.markAssignmentSuccess(_work, _handle);
  }

  private final @Nonnull TaskHandle _handle;
  private final @Nonnull QueuedWork _work;
}
