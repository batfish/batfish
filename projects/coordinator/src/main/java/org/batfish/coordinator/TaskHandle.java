package org.batfish.coordinator;

import javax.annotation.Nonnull;
import org.batfish.common.Task;

/**
 * A handle to an assigned task that supports checking the task's current status. Also provides a
 * callback to be called post-task-termination.
 */
public interface TaskHandle {

  /** Fetches the status of queued work in the form of a {@link Task}. */
  @Nonnull
  Task checkTask();
}
