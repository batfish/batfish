package org.batfish.datamodel.flow2;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nullable;

/** Represents a step in {@link TraceHop} */
public abstract class Step {

  private static final String PROP_DETAIL = "detail";
  private static final String PROP_ACTION = "action";

  protected @Nullable final StepDetail _detail;

  protected @Nullable final StepAction _action;

  @JsonCreator
  Step(
      @JsonProperty(PROP_DETAIL) @Nullable StepDetail detail,
      @JsonProperty(PROP_ACTION) @Nullable StepAction action) {
    _detail = detail;
    _action = action;
  }

  @JsonProperty(PROP_DETAIL)
  @Nullable
  public StepDetail getDetail() {
    return _detail;
  }

  @JsonProperty(PROP_ACTION)
  @Nullable
  public StepAction getAction() {
    return _action;
  }
}
