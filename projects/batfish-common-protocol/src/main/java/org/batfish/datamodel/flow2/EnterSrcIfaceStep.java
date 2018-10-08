package org.batfish.datamodel.flow2;

import org.batfish.datamodel.collections.NodeInterfacePair;

public class EnterSrcIfaceStep extends Step {
  public static class EnterSrcIfaceDetail extends StepDetail {
    private NodeInterfacePair _inputInterface;
    private String _filterIn;

    public EnterSrcIfaceDetail(NodeInterfacePair inputInterface, String filterIn) {
      super("EnterSrcIface");
      _inputInterface = inputInterface;
      _filterIn = filterIn;
    }

    public NodeInterfacePair getInputInterface() {
      return _inputInterface;
    }

    public String getFilterIn() {
      return _filterIn;
    }

    public static Builder builder() {
      return new Builder();
    }

    public static class Builder {
      private NodeInterfacePair _inputInterface;
      private String _filterIn;

      public EnterSrcIfaceDetail build() {
        return new EnterSrcIfaceDetail(_inputInterface, _filterIn);
      }

      public Builder setInputInterface(NodeInterfacePair inputInterface) {
        _inputInterface = inputInterface;
        return this;
      }

      public Builder setFilterIn(String filterIn) {
        _filterIn = filterIn;
        return this;
      }
    }
  }

  public static class EnterSrcIfaceAction extends StepAction {

    private StepActionResult _actionResult;

    public EnterSrcIfaceAction(StepActionResult result) {
      super("EnterSrcIface");
      _actionResult = result;
    }

    public StepActionResult getActionResult() {
      return _actionResult;
    }
  }

  public EnterSrcIfaceStep(EnterSrcIfaceDetail stepDetail, EnterSrcIfaceAction stepAction) {
    super(stepDetail, stepAction);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private EnterSrcIfaceDetail _detail;
    private EnterSrcIfaceAction _action;

    public EnterSrcIfaceStep build() {
      return new EnterSrcIfaceStep(_detail, _action);
    }

    public Builder setDetail(EnterSrcIfaceDetail detail) {
      _detail = detail;
      return this;
    }

    public Builder setAction(EnterSrcIfaceAction action) {
      _action = action;
      return this;
    }
  }
}
