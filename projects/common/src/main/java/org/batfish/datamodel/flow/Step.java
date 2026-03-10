package org.batfish.datamodel.flow;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.Objects;
import javax.annotation.Nonnull;

/**
 * Represents a step in a {@link Hop}, steps are operations through which a packet ({@link
 * org.batfish.datamodel.Flow}) goes through while traversing {@link Hop}s to reach the destination.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = ArpErrorStep.class, name = "ArpError"),
  @JsonSubTypes.Type(value = DeliveredStep.class, name = "Delivered"),
  @JsonSubTypes.Type(value = EnterInputIfaceStep.class, name = "EnterInputInterface"),
  @JsonSubTypes.Type(value = ExitOutputIfaceStep.class, name = "ExitOutputInterface"),
  @JsonSubTypes.Type(value = FilterStep.class, name = "Filter"),
  @JsonSubTypes.Type(value = InboundStep.class, name = "Inbound"),
  @JsonSubTypes.Type(value = LoopStep.class, name = "Loop"),
  @JsonSubTypes.Type(value = MatchSessionStep.class, name = "MatchSession"),
  @JsonSubTypes.Type(value = OriginateStep.class, name = "Originate"),
  @JsonSubTypes.Type(value = PolicyStep.class, name = "Policy"),
  @JsonSubTypes.Type(value = RoutingStep.class, name = "Routing"),
  @JsonSubTypes.Type(value = SetupSessionStep.class, name = "SetupSession"),
  @JsonSubTypes.Type(value = TransformationStep.class, name = "Transformation"),
})
public abstract class Step<D> {
  protected static final String PROP_DETAIL = "detail";
  protected static final String PROP_ACTION = "action";

  /** Metadata about the {@link Step} */
  private final @Nonnull D _detail;

  /** The action which was taken at the end of this step */
  private final @Nonnull StepAction _action;

  Step(D detail, StepAction action) {
    _detail = detail;
    _action = action;
  }

  @JsonProperty(PROP_DETAIL)
  public final @Nonnull D getDetail() {
    return _detail;
  }

  @JsonProperty(PROP_ACTION)
  public final @Nonnull StepAction getAction() {
    return _action;
  }

  @Override
  public final boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Step<?> step = (Step<?>) o;
    return _detail.equals(step._detail) && _action == step._action;
  }

  @Override
  public final int hashCode() {
    return Objects.hash(_detail, _action);
  }
}
