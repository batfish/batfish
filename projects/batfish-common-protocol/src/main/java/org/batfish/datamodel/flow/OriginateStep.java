package org.batfish.datamodel.flow;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.flow.OriginateStep.OriginateStepDetail;

/** {@link Step} to represent the originating of a {@link Flow} in a node {@link Hop} */
@JsonTypeName("Originate")
public final class OriginateStep extends Step<OriginateStepDetail> {

  /** Details of the {@link Step} when a {@link Flow} originates at a {@link Hop} */
  public static final class OriginateStepDetail {

    private static final String PROP_ORIGINATING_VRF = "originatingVrf";

    private @Nonnull String _originatingVrf;

    private OriginateStepDetail(String originatingVrf) {
      _originatingVrf = originatingVrf;
    }

    @JsonCreator
    private static OriginateStepDetail jsonCreator(
        @JsonProperty(PROP_ORIGINATING_VRF) @Nullable String originatingVrf) {
      checkArgument(originatingVrf != null, "%s should be set", PROP_ORIGINATING_VRF);
      return new OriginateStepDetail(originatingVrf);
    }

    @JsonProperty(PROP_ORIGINATING_VRF)
    public String getOriginatingVrf() {
      return _originatingVrf;
    }

    public static Builder builder() {
      return new Builder();
    }

    /** Chained builder to create a {@link OriginateStepDetail} object */
    public static final class Builder {
      private String _originatingVrf;

      public OriginateStepDetail build() {
        checkState(_originatingVrf != null, "Must call setOriginatingVrf before building");
        return new OriginateStepDetail(_originatingVrf);
      }

      public Builder setOriginatingVrf(String originatingVrf) {
        _originatingVrf = originatingVrf;
        return this;
      }

      /** Only for use by {@link OriginateStepDetail#builder()}. */
      private Builder() {}
    }
  }

  private static final String PROP_DETAIL = "detail";
  private static final String PROP_ACTION = "action";

  public static Builder builder() {
    return new Builder();
  }

  private OriginateStep(OriginateStepDetail detail, StepAction action) {
    super(detail, action);
  }

  @JsonCreator
  private static OriginateStep jsonCreator(
      @Nullable @JsonProperty(PROP_DETAIL) OriginateStepDetail detail,
      @Nullable @JsonProperty(PROP_ACTION) StepAction action) {
    checkArgument(action != null, "Missing %s", PROP_ACTION);
    checkArgument(detail != null, "Missing %s", PROP_DETAIL);
    return new OriginateStep(detail, action);
  }

  /** Chained builder to create an {@link OriginateStep} object */
  public static final class Builder {
    @Nullable private OriginateStepDetail _detail;
    @Nullable private StepAction _action;

    public OriginateStep build() {
      checkState(_action != null, "setAction must be called before building");
      checkState(_detail != null, "setDetail must be called before building");
      return new OriginateStep(_detail, _action);
    }

    public Builder setDetail(@Nullable OriginateStepDetail detail) {
      _detail = detail;
      return this;
    }

    public Builder setAction(@Nullable StepAction action) {
      _action = action;
      return this;
    }

    /** Only for use by {@link OriginateStep#builder()}. */
    private Builder() {}
  }
}
