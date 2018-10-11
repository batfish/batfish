package org.batfish.datamodel.flow;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import java.io.Serializable;
import javax.annotation.Nullable;
import org.batfish.datamodel.Flow;

/**
 * Represents a step in a {@link Hop}, steps are operations through which a packet ({@link Flow})
 * goes through while traversing {@link Hop}s to reach the destination
 */
@JsonSchemaDescription("Represents an operation within a hop")
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
public abstract class Step implements Serializable {

  private static final String PROP_DETAIL = "detail";
  private static final String PROP_ACTION = "action";

  /** */
  private static final long serialVersionUID = 1L;

  /** Metadata about the {@link Step} */
  protected @Nullable final StepDetail _detail;

  /** Information about the action which was taken at the end of this step */
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
