package org.batfish.datamodel.flow;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.flow.InboundStep.InboundStepDetail;

/** {@link InboundStep} represents processing when a flow is directed to the local device. */
@JsonTypeName("Inbound")
@ParametersAreNonnullByDefault
public final class InboundStep extends Step<InboundStepDetail> {

  /* Currently empty, what goes here? Inbound filter? */
  /** Detail about {@link InboundStep}. */
  public static class InboundStepDetail {}

  private static final String PROP_DETAIL = "detail";
  private static final String PROP_ACTION = "action";

  private InboundStep(InboundStepDetail detail, StepAction action) {
    super(detail, action);
  }

  @JsonCreator
  private static InboundStep jsonCreator(
      @Nullable @JsonProperty(PROP_DETAIL) InboundStepDetail detail,
      @Nullable @JsonProperty(PROP_ACTION) StepAction action) {
    checkArgument(detail != null, "Missing detail");
    checkArgument(action != null, "Missing action");
    return new InboundStep(detail, action);
  }

  public static Builder builder() {
    return new Builder();
  }

  /** Chained builder to create a {@link InboundStep} object */
  public static class Builder {
    private InboundStepDetail _detail = new InboundStepDetail();
    private @Nullable StepAction _action;

    public InboundStep build() {
      checkState(_action != null, "must call setAction before building");
      return new InboundStep(_detail, _action);
    }

    public Builder setDetail(InboundStepDetail detail) {
      _detail = detail;
      return this;
    }

    public Builder setAction(StepAction action) {
      _action = action;
      return this;
    }

    /** Only for use by {@link InboundStep#builder()}. */
    private Builder() {}
  }
}
