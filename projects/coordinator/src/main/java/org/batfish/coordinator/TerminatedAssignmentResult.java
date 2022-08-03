package org.batfish.coordinator;

import javax.annotation.ParametersAreNonnullByDefault;

/** The assigned work has already terminated. */
@ParametersAreNonnullByDefault
public final class TerminatedAssignmentResult implements AssignmentResult {

  public static TerminatedAssignmentResult instance() {
    return INSTANCE;
  }

  private TerminatedAssignmentResult() {}

  @Override
  public void applyTo(WorkQueueMgr mgr) {}

  private static final TerminatedAssignmentResult INSTANCE = new TerminatedAssignmentResult();
}
