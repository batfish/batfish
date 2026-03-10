package org.batfish.datamodel.flow;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.flow.ArpErrorStep.ArpErrorStepDetail;

/**
 * {@link Step} to represent that the packet is ended with Neighbor Unreachable or Insufficient Info
 * disposition
 */
@JsonTypeName("ArpError")
public final class ArpErrorStep extends Step<ArpErrorStepDetail> {

  /** Details of the {@link ArpErrorStep} */
  public static final class ArpErrorStepDetail {
    private static final String PROP_OUTPUT_INTERFACE = "outputInterface";
    private static final String PROP_RESOLVED_NEXTHOP_IP = "resolvedNexthopIp";

    private final @Nonnull NodeInterfacePair _outputInterface;
    private final @Nullable Ip _resolvedNexthopIp;

    private ArpErrorStepDetail(NodeInterfacePair outInterface, @Nullable Ip resolvedNexthopIp) {
      _outputInterface = outInterface;
      _resolvedNexthopIp = resolvedNexthopIp;
    }

    @JsonCreator
    private static ArpErrorStepDetail jsonCreator(
        @JsonProperty(PROP_OUTPUT_INTERFACE) @Nullable NodeInterfacePair outInterface,
        @JsonProperty(PROP_RESOLVED_NEXTHOP_IP) @Nullable() Ip resolvedNexthopIp) {
      checkArgument(outInterface != null, "Missing %s", PROP_OUTPUT_INTERFACE);
      return new ArpErrorStepDetail(outInterface, resolvedNexthopIp);
    }

    @JsonProperty(PROP_OUTPUT_INTERFACE)
    public @Nonnull NodeInterfacePair getOutputInterface() {
      return _outputInterface;
    }

    @JsonProperty(PROP_RESOLVED_NEXTHOP_IP)
    public @Nullable Ip getResolvedNexthopIp() {
      return _resolvedNexthopIp;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      } else if (!(o instanceof ArpErrorStepDetail)) {
        return false;
      }
      ArpErrorStepDetail that = (ArpErrorStepDetail) o;
      return _outputInterface.equals(that._outputInterface)
          && Objects.equals(_resolvedNexthopIp, that._resolvedNexthopIp);
    }

    @Override
    public int hashCode() {
      return Objects.hash(_outputInterface, _resolvedNexthopIp);
    }

    public static Builder builder() {
      return new Builder();
    }

    /** Chained builder to create a {@link ArpErrorStepDetail} object */
    public static final class Builder {
      private @Nullable NodeInterfacePair _outputInterface;
      private @Nullable Ip _resolvedNextHopIp;

      public ArpErrorStepDetail build() {
        checkState(_outputInterface != null, "Must call setOutputInterface before building");
        return new ArpErrorStepDetail(_outputInterface, _resolvedNextHopIp);
      }

      public Builder setOutputInterface(NodeInterfacePair outputIface) {
        _outputInterface = outputIface;
        return this;
      }

      public Builder setResolvedNexthopIp(@Nullable Ip resolvedNexthopIp) {
        _resolvedNextHopIp = resolvedNexthopIp;
        return this;
      }

      /** Only for use by {@link ArpErrorStepDetail#builder()}. */
      private Builder() {}
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  /** Chained builder to create a {@link ArpErrorStep} object */
  public static final class Builder {
    private @Nullable ArpErrorStepDetail _detail;
    private @Nullable StepAction _action;

    public ArpErrorStep build() {
      checkState(_action != null, "Must call setAction before building");
      checkState(
          _action == StepAction.NEIGHBOR_UNREACHABLE || _action == StepAction.INSUFFICIENT_INFO,
          "action must be NEIGHBOR_UNREACHABLE or INSUFFICIENT_INFO");
      checkState(_detail != null, "Must call setDetail before building");
      return new ArpErrorStep(_detail, _action);
    }

    public Builder setDetail(ArpErrorStepDetail detail) {
      _detail = detail;
      return this;
    }

    public Builder setAction(StepAction action) {
      _action = action;
      return this;
    }

    /** Only for use by {@link ArpErrorStep#builder()}. */
    private Builder() {}
  }

  @JsonCreator
  private static ArpErrorStep jsonCreator(
      @JsonProperty(PROP_DETAIL) @Nullable ArpErrorStepDetail detail,
      @JsonProperty(PROP_ACTION) @Nullable StepAction action) {
    checkArgument(action != null, "Missing %s", PROP_ACTION);
    checkArgument(detail != null, "Missing %s", PROP_DETAIL);
    return new ArpErrorStep(detail, action);
  }

  private ArpErrorStep(ArpErrorStepDetail detail, StepAction action) {
    super(detail, action);
  }
}
