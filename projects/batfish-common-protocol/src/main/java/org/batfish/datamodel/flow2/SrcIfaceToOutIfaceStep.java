package org.batfish.datamodel.flow2;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import javax.annotation.Nullable;

public class SrcIfaceToOutIfaceStep extends Step {
  public static class SrcIfaceToOutIfaceStepDetail extends StepDetail {

    private static final String PROP_ROUTE_CONSIDERED = "routesConsidered";

    private Set<String> _routesConsidered;

    @JsonCreator
    public SrcIfaceToOutIfaceStepDetail(
        @JsonProperty(PROP_ROUTE_CONSIDERED) @Nullable Set<String> routesConsidered) {
      super("SrcIfaceToOutIface");

      _routesConsidered = firstNonNull(routesConsidered, ImmutableSet.of());
    }

    @JsonProperty(PROP_ROUTE_CONSIDERED)
    public Set<String> getRoutesConsidered() {
      return _routesConsidered;
    }

    public static Builder builder() {
      return new Builder();
    }

    public static class Builder {
      private Set<String> _routesConsidered;

      public SrcIfaceToOutIfaceStepDetail build() {
        return new SrcIfaceToOutIfaceStepDetail(_routesConsidered);
      }

      public Builder setRoutesConsidered(Set<String> routesConsidered) {
        _routesConsidered = routesConsidered;
        return this;
      }
    }
  }

  public static class SrcIfaceToOutIfaceStepAction extends StepAction {

    private static final String PROP_ACTION_RESULT = "actionResult";

    private @Nullable StepActionResult _actionResult;

    @JsonCreator
    public SrcIfaceToOutIfaceStepAction(
        @JsonProperty(PROP_ACTION_RESULT) @Nullable StepActionResult result) {
      super("SrcIfaceToOutIfaceStepAction");
      _actionResult = result;
    }

    @Nullable
    @JsonProperty(PROP_ACTION_RESULT)
    public StepActionResult getActionResult() {
      return _actionResult;
    }
  }

  public SrcIfaceToOutIfaceStep(
      SrcIfaceToOutIfaceStepDetail stepDetail, SrcIfaceToOutIfaceStepAction stepAction) {
    super(stepDetail, stepAction);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private SrcIfaceToOutIfaceStepDetail _detail;
    private SrcIfaceToOutIfaceStepAction _action;

    public SrcIfaceToOutIfaceStep build() {
      return new SrcIfaceToOutIfaceStep(_detail, _action);
    }

    public Builder setDetail(SrcIfaceToOutIfaceStepDetail detail) {
      _detail = detail;
      return this;
    }

    public Builder setAction(SrcIfaceToOutIfaceStepAction action) {
      _action = action;
      return this;
    }
  }
}
