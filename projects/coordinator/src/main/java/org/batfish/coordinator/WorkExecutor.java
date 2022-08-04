package org.batfish.coordinator;

import javax.annotation.ParametersAreNonnullByDefault;

/** Entity that executes work items to be performed work to some abstract batfish worker. */
@ParametersAreNonnullByDefault
public interface WorkExecutor {

  /** Attempt to assign work and return the result of the attempt. */
  SubmissionResult submit(QueuedWork work);
}
