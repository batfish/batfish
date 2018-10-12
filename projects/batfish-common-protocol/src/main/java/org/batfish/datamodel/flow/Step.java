package org.batfish.datamodel.flow;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import javax.annotation.Nullable;
import org.batfish.datamodel.Flow;

/**
 * Represents a step in a {@link Hop}, steps are operations through which a packet ({@link Flow})
 * goes through while traversing {@link Hop}s to reach the destination
 */
@JsonSchemaDescription("Represents an operation within a hop")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = As.PROPERTY, property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = EnterInputIfaceStep.class, name = "EnterInputInterface"),
  @JsonSubTypes.Type(value = RoutingStep.class, name = "Routing"),
  @JsonSubTypes.Type(value = ExitOutputIfaceStep.class, name = "ExitOutputInterface")
})
public abstract class Step<D extends StepDetail> {

  private static final String PROP_DETAIL = "detail";
  private static final String PROP_ACTION = "action";

  /** Metadata about the {@link Step} */
  protected @Nullable final D _detail;

  /** The action which was taken at the end of this step */
  protected @Nullable final StepAction _action;

  @JsonCreator
  Step(
      @JsonProperty(PROP_DETAIL) @Nullable D detail,
      @JsonProperty(PROP_ACTION) @Nullable StepAction action) {
    _detail = detail;
    _action = action;
  }

  @JsonProperty(PROP_DETAIL)
  @Nullable
  public D getDetail() {
    return _detail;
  }

  @JsonProperty(PROP_ACTION)
  @Nullable
  public StepAction getAction() {
    return _action;
  }
}
