package org.batfish.datamodel.flow2;

import org.batfish.datamodel.Flow;
import org.batfish.datamodel.collections.NodeInterfacePair;

public class ExitOutIfaceStep extends Step {
  public static class ExitOutIfaceStepDetail extends StepDetail {
    private NodeInterfacePair _outputInterface;
    private String _filterOut;
    private Flow _originalFlow;
    private Flow _transformedFlow;

    public ExitOutIfaceStepDetail(
        NodeInterfacePair outInterface, String filterOut, Flow originalFlow, Flow transformedFlow) {
      super("ExitOutIface");
      _outputInterface = outInterface;
      _filterOut = filterOut;
      _originalFlow = originalFlow;
      _transformedFlow = transformedFlow;
    }

    public NodeInterfacePair getOutputInterface() {
      return _outputInterface;
    }

    public String getFilterIOut() {
      return _filterOut;
    }

    public Flow getOriginalFlow(){
      return _originalFlow;
    }

    public Flow getTransformedFlow(){
      return _transformedFlow;
    }

    public static Builder builder() {
      return new Builder();
    }

    public static class Builder {
      private NodeInterfacePair _outputInterface;
      private String _filterOut;

      private Flow _originalFlow;
      private Flow _transformedFlow;

      public ExitOutIfaceStepDetail build() {
        return new ExitOutIfaceStepDetail(
            _outputInterface, _filterOut, _originalFlow, _transformedFlow);
      }

      public Builder setOutputInterface(NodeInterfacePair _outputIface) {
        _outputInterface = _outputIface;
        return this;
      }

      public Builder setFilterOut(String filterOut) {
        _filterOut = filterOut;
        return this;
      }

      public Builder setOriginalFlow(Flow originalFlow) {
        _originalFlow = originalFlow;
        return this;
      }

      public Builder setTransformedFlow(Flow transformedFlow) {
        _transformedFlow = transformedFlow;
        return this;
      }
    }
  }

  public static class ExitOutIfaceAction extends StepAction {

    private StepActionResult _actionResult;

    public ExitOutIfaceAction(StepActionResult result) {
      super("EnterSrcIface");
      _actionResult = result;
    }

    public StepActionResult getActionResult() {
      return _actionResult;
    }
  }

  public ExitOutIfaceStep(ExitOutIfaceStepDetail stepDetail, ExitOutIfaceAction stepAction) {
    super(stepDetail, stepAction);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private ExitOutIfaceStepDetail _detail;
    private ExitOutIfaceAction _action;

    public ExitOutIfaceStep build() {
      return new ExitOutIfaceStep(_detail, _action);
    }

    public Builder setDetail(ExitOutIfaceStepDetail detail) {
      _detail = detail;
      return this;
    }

    public Builder setAction(ExitOutIfaceAction action) {
      _action = action;
      return this;
    }
  }
}
