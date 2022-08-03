package org.batfish.coordinator;

import java.io.IOException;
import javax.annotation.ParametersAreNonnullByDefault;

/** The result of attempting to assign work. */
@ParametersAreNonnullByDefault
public interface AssignmentResult {
  /** Apply the effects of this assignment result to the work queue manager. */
  void applyTo(WorkQueueMgr mgr) throws IOException;
}
