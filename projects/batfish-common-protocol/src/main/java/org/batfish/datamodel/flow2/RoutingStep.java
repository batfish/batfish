package org.batfish.datamodel.flow2;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import javax.annotation.Nullable;

public class RoutingStep extends Step {
  public static class RoutingStepDetail extends StepDetail {

    private static final String PROP_ROUTE_CONSIDERED = "routesConsidered";

    private Set<String> _routesConsidered;

    @JsonCreator
    public RoutingStepDetail(
        @JsonProperty(PROP_ROUTE_CONSIDERED) @Nullable Set<String> routesConsidered,
        @JsonProperty(PROP_NAME) @Nullable String name) {
      super(firstNonNull(name, "RoutingStep"));
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
      private String _name;

      public RoutingStepDetail build() {
        return new RoutingStepDetail(_routesConsidered, _name);
      }

      public Builder setName(String name) {
        _name = name;
        return this;
      }

      public Builder setRoutesConsidered(Set<String> routesConsidered) {
        _routesConsidered = routesConsidered;
        return this;
      }
    }
  }

  public static class RoutingStepAction extends StepAction {

    private static final String PROP_ACTION_RESULT = "actionResult";

    private @Nullable StepActionResult _actionResult;

    @JsonCreator
    public RoutingStepAction(
        @JsonProperty(PROP_ACTION_RESULT) @Nullable StepActionResult result,
        @JsonProperty(PROP_NAME) @Nullable String name) {
      super(firstNonNull(name, "RoutingAction"));
      _actionResult = result;
    }

    @Nullable
    @JsonProperty(PROP_ACTION_RESULT)
    public StepActionResult getActionResult() {
      return _actionResult;
    }
  }

  private static final String PROP_DETAIL = "detail";
  private static final String PROP_ACTION = "action";

  @JsonCreator
  public RoutingStep(
      @JsonProperty(PROP_DETAIL) RoutingStepDetail stepDetail,
      @JsonProperty(PROP_ACTION) RoutingStepAction stepAction) {
    super(stepDetail, stepAction);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private RoutingStepDetail _detail;
    private RoutingStepAction _action;

    public RoutingStep build() {
      return new RoutingStep(_detail, _action);
    }

    public Builder setDetail(RoutingStepDetail detail) {
      _detail = detail;
      return this;
    }

    public Builder setAction(RoutingStepAction action) {
      _action = action;
      return this;
    }
  }
}
