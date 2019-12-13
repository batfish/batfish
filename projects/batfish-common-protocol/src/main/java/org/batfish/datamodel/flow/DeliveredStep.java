package org.batfish.datamodel.flow;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.flow.DeliveredStep.DeliveredStepDetail;

/**
 * {@link Step} to represent that the packet is delivered to a subnet or exits the current network
 */
@JsonTypeName("DeliveredStep")
public final class DeliveredStep extends Step<DeliveredStepDetail> {

  /** Details of the {@link DeliveredStep} */
  public static final class DeliveredStepDetail {
    private static final String PROP_OUTPUT_INTERFACE = "outputInterface";
    private static final String PROP_RESOLVED_NEXTHOP_IP = "resolvedNexthopIp";

    private @Nonnull NodeInterfacePair _outputInterface;
    private @Nullable Ip _resolvedNexthopIp;

    private DeliveredStepDetail(NodeInterfacePair outInterface, @Nullable Ip resolvedNexthopIp) {
      _outputInterface = outInterface;
      _resolvedNexthopIp = resolvedNexthopIp;
    }

    @JsonCreator
    private static DeliveredStepDetail jsonCreator(
        @JsonProperty(PROP_OUTPUT_INTERFACE) @Nullable NodeInterfacePair outInterface,
        @JsonProperty(PROP_RESOLVED_NEXTHOP_IP) @Nullable() Ip resolvedNexthopIp) {
      checkArgument(outInterface != null, "Missing %s", PROP_OUTPUT_INTERFACE);
      return new DeliveredStepDetail(outInterface, resolvedNexthopIp);
    }

    @JsonProperty(PROP_OUTPUT_INTERFACE)
    @Nonnull
    public NodeInterfacePair getOutputInterface() {
      return _outputInterface;
    }

    @JsonProperty(PROP_RESOLVED_NEXTHOP_IP)
    @Nullable
    public Ip getResolvedNexthopIp() {
      return _resolvedNexthopIp;
    }

    public static Builder builder() {
      return new Builder();
    }

    /** Chained builder to create a {@link DeliveredStepDetail} object */
    public static final class Builder {
      private @Nullable NodeInterfacePair _outputInterface;
      private @Nullable Ip _resolvedNextHopIp;

      public DeliveredStepDetail build() {
        checkState(_outputInterface != null, "Must call setOutputInterface before building");
        return new DeliveredStepDetail(_outputInterface, _resolvedNextHopIp);
      }

      public Builder setOutputInterface(NodeInterfacePair outputIface) {
        _outputInterface = outputIface;
        return this;
      }

      public Builder setResolvedNexthopIp(@Nullable Ip resolvedNexthopIp) {
        _resolvedNextHopIp = resolvedNexthopIp;
        return this;
      }

      /** Only for use by {@link DeliveredStepDetail#builder()}. */
      private Builder() {}
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  /** Chained builder to create a {@link DeliveredStep} object */
  public static final class Builder {
    private @Nullable DeliveredStepDetail _detail;
    private @Nullable StepAction _action;

    public DeliveredStep build() {
      checkState(_action != null, "Must call setAction before building");
      checkState(
          _action == StepAction.DELIVERED_TO_SUBNET || _action == StepAction.EXITS_NETWORK,
          "action must DELIVERED_TO_SUBNET or EXITS_NETWORK");
      checkState(_detail != null, "Must call setDetail before building");
      return new DeliveredStep(_detail, _action);
    }

    public Builder setDetail(DeliveredStepDetail detail) {
      _detail = detail;
      return this;
    }

    public Builder setAction(StepAction action) {
      _action = action;
      return this;
    }

    /** Only for use by {@link DeliveredStep#builder()}. */
    private Builder() {}
  }

  private static final String PROP_DETAIL = "detail";
  private static final String PROP_ACTION = "action";

  @JsonCreator
  private static DeliveredStep jsonCreator(
      @Nullable @JsonProperty(PROP_DETAIL) DeliveredStepDetail detail,
      @Nullable @JsonProperty(PROP_ACTION) StepAction action) {
    checkArgument(action != null, "Missing %s", PROP_ACTION);
    checkArgument(detail != null, "Missing %s", PROP_DETAIL);
    return new DeliveredStep(detail, action);
  }

  private DeliveredStep(DeliveredStepDetail detail, StepAction action) {
    super(detail, action);
  }
}
