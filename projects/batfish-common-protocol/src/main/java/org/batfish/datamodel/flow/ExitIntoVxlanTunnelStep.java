package org.batfish.datamodel.flow;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.flow.ExitIntoVxlanTunnelStep.ExitIntoVxlanTunnelStepDetail;

/** {@link Step} to represent a flow exting a node into a VXLAN tunnel. */
public final class ExitIntoVxlanTunnelStep extends Step<ExitIntoVxlanTunnelStepDetail> {

  /** Details of an {@link ExitIntoVxlanTunnelStep}. */
  public static final class ExitIntoVxlanTunnelStepDetail {

    public static final class Builder {
      public @Nonnull ExitIntoVxlanTunnelStepDetail build() {
        checkState(_dstVtepIp != null, "Missing %s", PROP_DST_VTEP_IP);
        checkState(_outputVrf != null, "Missing %s", PROP_OUTPUT_VRF);
        checkState(_srcVtepIp != null, "Missing %s", PROP_SRC_VTEP_IP);
        checkState(_vni != null, "Missing %s", PROP_VNI);

        return new ExitIntoVxlanTunnelStepDetail(_dstVtepIp, _outputVrf, _srcVtepIp, _vni);
      }

      public @Nonnull Builder setDstVtepIp(@Nullable Ip dstVtepIp) {
        _dstVtepIp = dstVtepIp;
        return this;
      }

      public @Nonnull Builder setOutputVrf(@Nullable String outputVrf) {
        _outputVrf = outputVrf;
        return this;
      }

      public @Nonnull Builder setSrcVtepIp(@Nullable Ip srcVtepIp) {
        _srcVtepIp = srcVtepIp;
        return this;
      }

      public @Nonnull Builder setVni(@Nullable Integer vni) {
        _vni = vni;
        return this;
      }

      private @Nullable String _outputVrf;
      private @Nullable Integer _vni;
      private @Nullable Ip _srcVtepIp;
      private @Nullable Ip _dstVtepIp;

      private Builder() {}
    }

    public static @Nonnull Builder builder() {
      return new Builder();
    }

    @JsonProperty(PROP_DST_VTEP_IP)
    public @Nonnull Ip getDstVtepIp() {
      return _dstVtepIp;
    }

    @JsonProperty(PROP_OUTPUT_VRF)
    public @Nonnull String getOutputVrf() {
      return _outputVrf;
    }

    @JsonProperty(PROP_SRC_VTEP_IP)
    public @Nonnull Ip getSrcVtepIp() {
      return _srcVtepIp;
    }

    @JsonProperty(PROP_VNI)
    public int getVni() {
      return _vni;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      } else if (!(o instanceof ExitIntoVxlanTunnelStepDetail)) {
        return false;
      }
      ExitIntoVxlanTunnelStepDetail that = (ExitIntoVxlanTunnelStepDetail) o;
      return _dstVtepIp.equals(that._dstVtepIp)
          && _outputVrf.equals(that._outputVrf)
          && _srcVtepIp.equals(that._srcVtepIp)
          && _vni == that._vni;
    }

    @Override
    public int hashCode() {
      return Objects.hash(_dstVtepIp, _outputVrf, _srcVtepIp, _vni);
    }

    private static final String PROP_DST_VTEP_IP = "dstVtepIp";
    private static final String PROP_OUTPUT_VRF = "outputVrf";
    private static final String PROP_SRC_VTEP_IP = "srcVtepIp";
    private static final String PROP_VNI = "vni";

    @JsonCreator
    private static @Nonnull ExitIntoVxlanTunnelStepDetail jsonCreator(
        @JsonProperty(PROP_DST_VTEP_IP) @Nullable Ip dstVtepIp,
        @JsonProperty(PROP_OUTPUT_VRF) @Nullable String outputVrf,
        @JsonProperty(PROP_SRC_VTEP_IP) @Nullable Ip srcVtepIp,
        @JsonProperty(PROP_VNI) @Nullable Integer vni) {
      checkArgument(dstVtepIp != null, "Missing %s", PROP_DST_VTEP_IP);
      checkArgument(outputVrf != null, "Missing %s", PROP_OUTPUT_VRF);
      checkArgument(srcVtepIp != null, "Missing %s", PROP_SRC_VTEP_IP);
      checkArgument(vni != null, "Missing %s", PROP_VNI);
      return new ExitIntoVxlanTunnelStepDetail(dstVtepIp, outputVrf, srcVtepIp, vni);
    }

    private final @Nonnull String _outputVrf;
    private final int _vni;
    private final @Nonnull Ip _srcVtepIp;
    private final @Nonnull Ip _dstVtepIp;

    private ExitIntoVxlanTunnelStepDetail(Ip dstVtepIp, String outputVrf, Ip srcVtepIp, int vni) {
      _dstVtepIp = dstVtepIp;
      _outputVrf = outputVrf;
      _srcVtepIp = srcVtepIp;
      _vni = vni;
    }
  }

  public static final class Builder {
    public @Nonnull ExitIntoVxlanTunnelStep build() {
      checkState(_action != null, "Missing %s", PROP_ACTION);
      checkState(_detail != null, "Missing %s", PROP_DETAIL);
      return new ExitIntoVxlanTunnelStep(_detail, _action);
    }

    public Builder setDetail(ExitIntoVxlanTunnelStepDetail detail) {
      _detail = detail;
      return this;
    }

    public Builder setAction(StepAction action) {
      _action = action;
      return this;
    }

    private @Nullable ExitIntoVxlanTunnelStepDetail _detail;
    private @Nullable StepAction _action;

    private Builder() {}
  }

  public static @Nonnull Builder builder() {
    return new Builder();
  }

  @JsonCreator
  private static ExitIntoVxlanTunnelStep jsonCreator(
      @Nullable @JsonProperty(PROP_DETAIL) ExitIntoVxlanTunnelStepDetail detail,
      @Nullable @JsonProperty(PROP_ACTION) StepAction action) {
    checkArgument(action != null, "Missing %s", PROP_ACTION);
    checkArgument(detail != null, "Missing %s", PROP_DETAIL);
    return new ExitIntoVxlanTunnelStep(detail, action);
  }

  private ExitIntoVxlanTunnelStep(ExitIntoVxlanTunnelStepDetail detail, StepAction action) {
    super(detail, action);
  }
}
