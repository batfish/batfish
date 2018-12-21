package org.batfish.datamodel.flow;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Flow;

/**
 * Represents a step in a {@link Hop}, steps are operations through which a packet ({@link Flow})
 * goes through while traversing {@link Hop}s to reach the destination
 */
@JsonSchemaDescription("Represents an operation within a hop")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = EnterInputIfaceStep.class, name = "EnterInputInterface"),
  @JsonSubTypes.Type(value = ExitOutputIfaceStep.class, name = "ExitOutputInterface"),
  @JsonSubTypes.Type(value = InboundStep.class, name = "Inbound"),
  @JsonSubTypes.Type(value = OriginateStep.class, name = "Originate"),
  @JsonSubTypes.Type(value = FilterStep.class, name = "Filter"),
  @JsonSubTypes.Type(value = RoutingStep.class, name = "Routing"),
  @JsonSubTypes.Type(value = TransformationStep.class, name = "Transformation"),
})
public abstract class Step<D> {

  private static final String PROP_DETAIL = "detail";
  private static final String PROP_ACTION = "action";

  /** Metadata about the {@link Step} */
  @Nonnull private final D _detail;

  /** The action which was taken at the end of this step */
  @Nonnull private final StepAction _action;

  Step(D detail, StepAction action) {
    _detail = detail;
    _action = action;
  }

  @JsonProperty(PROP_DETAIL)
  @Nonnull
  public final D getDetail() {
    return _detail;
  }

  @JsonProperty(PROP_ACTION)
  @Nonnull
  public final StepAction getAction() {
    return _action;
  }
}
