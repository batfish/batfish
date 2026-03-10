package org.batfish.datamodel.flow;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.flow.InboundStep.InboundStepDetail;

/** {@link InboundStep} represents processing when a flow is directed to the local device. */
@JsonTypeName("Inbound")
public final class InboundStep extends Step<InboundStepDetail> {

  /** Detail about {@link InboundStep}. */
  public static final class InboundStepDetail {
    private static final String PROP_INTERFACE = "interface";

    private final @Nonnull String _interface;

    public InboundStepDetail(@Nonnull String iface) {
      _interface = iface;
    }

    @JsonCreator
    private static InboundStepDetail jsonCreator(
        @JsonProperty(PROP_INTERFACE) @Nullable String iface) {
      checkArgument(iface != null, "Missing %s", PROP_INTERFACE);
      return new InboundStepDetail(iface);
    }

    @JsonProperty
    public @Nonnull String getInterface() {
      return _interface;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      } else if (!(o instanceof InboundStepDetail)) {
        return false;
      }
      InboundStepDetail that = (InboundStepDetail) o;
      return _interface.equals(that._interface);
    }

    @Override
    public int hashCode() {
      return _interface.hashCode();
    }
  }

  private InboundStep(InboundStepDetail detail, StepAction action) {
    super(detail, action);
  }

  @JsonCreator
  private static InboundStep jsonCreator(
      @JsonProperty(PROP_DETAIL) @Nullable InboundStepDetail detail,
      @JsonProperty(PROP_ACTION) @Nullable StepAction action) {
    checkArgument(detail != null, "Missing %s", PROP_DETAIL);
    checkArgument(action != null, "Missing %s", PROP_ACTION);
    return new InboundStep(detail, action);
  }

  public static Builder builder() {
    return new Builder();
  }

  /** Chained builder to create a {@link InboundStep} object */
  public static final class Builder {
    private @Nullable InboundStepDetail _detail;

    public InboundStep build() {
      checkState(_detail != null, "must call setDetail before building");
      return new InboundStep(_detail, StepAction.ACCEPTED);
    }

    public Builder setDetail(InboundStepDetail detail) {
      _detail = detail;
      return this;
    }

    /** Only for use by {@link InboundStep#builder()}. */
    private Builder() {}
  }
}
