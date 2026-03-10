package org.batfish.common;

import javax.annotation.Nullable;

/**
 * Service that can run Batfish workitems composed of a taskId and arguments, and provide status of
 * running work items.
 */
public interface BatfishWorkerService {

  /** Get task status for taskId, or {@code null} if there is no task with given ID. */
  @Nullable
  Task getTaskStatus(String taskId);

  /** Launch the task defined by args that has the given taskId. */
  LaunchResult runTask(String taskId, String[] args);
}
