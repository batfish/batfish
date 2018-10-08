package org.batfish.datamodel.flow2;

import javax.annotation.Nonnull;

/** Represents a step in {@link TraceHop} */
public abstract class Step {

  protected final StepDetail _detail;

  protected final StepAction _action;

  Step(@Nonnull StepDetail detail, @Nonnull StepAction action) {
    _detail = detail;
    _action = action;
  }

  public StepDetail getDetail() {
    return _detail;
  }

  public StepAction getAction() {
    return _action;
  }


}
