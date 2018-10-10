package org.batfish.datamodel.flow2;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import javax.annotation.Nullable;

/** Represents a step in a {@link TraceHop} */
@JsonSchemaDescription("Represents an operation within a hop")
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
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
