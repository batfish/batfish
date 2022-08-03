package org.batfish.coordinator;

import javax.annotation.ParametersAreNonnullByDefault;

/** Entity that assigns queued work to some abstract batfish worker. */
@ParametersAreNonnullByDefault
public interface Assigner {

  /** Attempt to assign work and return the result of the attempt. */
  AssignmentResult assign(QueuedWork work);
}
