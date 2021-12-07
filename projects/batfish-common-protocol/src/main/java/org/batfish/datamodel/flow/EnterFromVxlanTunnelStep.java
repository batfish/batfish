package org.batfish.datamodel.flow;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.flow.EnterFromVxlanTunnelStep.EnterFromVxlanTunnelStepDetail;

/** {@link Step} to represent a flow entering a node from a VXLAN tunnel. */
public final class EnterFromVxlanTunnelStep extends Step<EnterFromVxlanTunnelStepDetail> {

  /** Details of an {@link EnterFromVxlanTunnelStep}. */
  public static final class EnterFromVxlanTunnelStepDetail {

    public static final class Builder {
      public @Nonnull EnterFromVxlanTunnelStepDetail build() {
        checkState(_dstVtepIp != null, "Missing %s", PROP_DST_VTEP_IP);
        checkState(_inputVrf != null, "Missing %s", PROP_INPUT_VRF);
        checkState(_srcVtepIp != null, "Missing %s", PROP_SRC_VTEP_IP);
        checkState(_vni != null, "Missing %s", PROP_VNI);

        return new EnterFromVxlanTunnelStepDetail(_dstVtepIp, _inputVrf, _srcVtepIp, _vni);
      }

      public @Nonnull Builder setDstVtepIp(@Nullable Ip dstVtepIp) {
        _dstVtepIp = dstVtepIp;
        return this;
      }

      public @Nonnull Builder setInputVrf(@Nullable String inputVrf) {
        _inputVrf = inputVrf;
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

      private @Nullable String _inputVrf;
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

    @JsonProperty(PROP_INPUT_VRF)
    public @Nonnull String getInputVrf() {
      return _inputVrf;
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
      } else if (!(o instanceof EnterFromVxlanTunnelStepDetail)) {
        return false;
      }
      EnterFromVxlanTunnelStepDetail that = (EnterFromVxlanTunnelStepDetail) o;
      return _dstVtepIp.equals(that._dstVtepIp)
          && _inputVrf.equals(that._inputVrf)
          && _srcVtepIp.equals(that._srcVtepIp)
          && _vni == that._vni;
    }

    @Override
    public int hashCode() {
      return Objects.hash(_dstVtepIp, _inputVrf, _srcVtepIp, _vni);
    }

    private static final String PROP_DST_VTEP_IP = "dstVtepIp";
    private static final String PROP_INPUT_VRF = "inputVrf";
    private static final String PROP_SRC_VTEP_IP = "srcVtepIp";
    private static final String PROP_VNI = "vni";

    @JsonCreator
    private static @Nonnull EnterFromVxlanTunnelStepDetail jsonCreator(
        @JsonProperty(PROP_DST_VTEP_IP) @Nullable Ip dstVtepIp,
        @JsonProperty(PROP_INPUT_VRF) @Nullable String inputVrf,
        @JsonProperty(PROP_SRC_VTEP_IP) @Nullable Ip srcVtepIp,
        @JsonProperty(PROP_VNI) @Nullable Integer vni) {
      checkArgument(dstVtepIp != null, "Missing %s", PROP_DST_VTEP_IP);
      checkArgument(inputVrf != null, "Missing %s", PROP_INPUT_VRF);
      checkArgument(srcVtepIp != null, "Missing %s", PROP_SRC_VTEP_IP);
      checkArgument(vni != null, "Missing %s", PROP_VNI);
      return new EnterFromVxlanTunnelStepDetail(dstVtepIp, inputVrf, srcVtepIp, vni);
    }

    private final @Nonnull String _inputVrf;
    private final int _vni;
    private final @Nonnull Ip _srcVtepIp;
    private final @Nonnull Ip _dstVtepIp;

    private EnterFromVxlanTunnelStepDetail(Ip dstVtepIp, String inputVrf, Ip srcVtepIp, int vni) {
      _dstVtepIp = dstVtepIp;
      _inputVrf = inputVrf;
      _srcVtepIp = srcVtepIp;
      _vni = vni;
    }
  }

  public static final class Builder {
    public @Nonnull EnterFromVxlanTunnelStep build() {
      checkState(_action != null, "Missing %s", PROP_ACTION);
      checkState(_detail != null, "Missing %s", PROP_DETAIL);
      return new EnterFromVxlanTunnelStep(_detail, _action);
    }

    public Builder setDetail(EnterFromVxlanTunnelStepDetail detail) {
      _detail = detail;
      return this;
    }

    public Builder setAction(StepAction action) {
      _action = action;
      return this;
    }

    private @Nullable EnterFromVxlanTunnelStepDetail _detail;
    private @Nullable StepAction _action;

    private Builder() {}
  }

  public static @Nonnull Builder builder() {
    return new Builder();
  }

  @JsonCreator
  private static EnterFromVxlanTunnelStep jsonCreator(
      @Nullable @JsonProperty(PROP_DETAIL) EnterFromVxlanTunnelStepDetail detail,
      @Nullable @JsonProperty(PROP_ACTION) StepAction action) {
    checkArgument(action != null, "Missing %s", PROP_ACTION);
    checkArgument(detail != null, "Missing %s", PROP_DETAIL);
    return new EnterFromVxlanTunnelStep(detail, action);
  }

  private EnterFromVxlanTunnelStep(EnterFromVxlanTunnelStepDetail detail, StepAction action) {
    super(detail, action);
  }
}
