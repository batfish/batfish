package org.batfish.datamodel.flow2;

import java.util.Set;
import org.batfish.datamodel.flow2.SrcIfaceToOutIfaceStep.Builder;

public class SrcIfaceToOutIfaceStep extends Step {
  public static class SrcIfaceToOutIfaceStepDetail extends StepDetail {
    private Set<String> _routesConsidered;

    public SrcIfaceToOutIfaceStepDetail(Set<String> routesConsidered) {
      super("SrcIfaceToOutIface");
      _routesConsidered = routesConsidered;
    }

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

    private StepActionResult _actionResult;

    public SrcIfaceToOutIfaceStepAction(StepActionResult result) {
      super("SrcIfaceToOutIfaceStepAction");
      _actionResult = result;
    }

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
