package org.batfish.datamodel.flow2;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nullable;
import org.batfish.datamodel.collections.NodeInterfacePair;

public class EnterSrcIfaceStep extends Step {
  public static class EnterSrcIfaceDetail extends StepDetail {

    private static final String PROP_INPUT_INTERFACE = "inputInterface";
    private static final String PROP_INPUT_VRF = "inputVrf";
    private static final String PROP_FILTER_IN = "filterIn";

    private @Nullable NodeInterfacePair _inputInterface;
    private @Nullable String _inputVrf;
    private @Nullable String _filterIn;

    @JsonCreator
    private EnterSrcIfaceDetail(
        @JsonProperty(PROP_INPUT_INTERFACE) @Nullable NodeInterfacePair inputInterface,
        @JsonProperty(PROP_FILTER_IN) @Nullable String filterIn,
        @JsonProperty(PROP_INPUT_VRF) @Nullable String inputVrf,
        @JsonProperty(PROP_NAME) @Nullable String name) {
      super(firstNonNull(name, "EnterSrcIface"));
      _inputInterface = inputInterface;
      _filterIn = filterIn;
      _inputVrf = inputVrf;
    }

    @JsonProperty(PROP_INPUT_INTERFACE)
    @Nullable
    public NodeInterfacePair getInputInterface() {
      return _inputInterface;
    }

    @JsonProperty(PROP_FILTER_IN)
    @Nullable
    public String getFilterIn() {
      return _filterIn;
    }

    @JsonProperty(PROP_INPUT_VRF)
    @Nullable
    public String getInputVrf() {
      return _inputVrf;
    }

    public static Builder builder() {
      return new Builder();
    }

    public static class Builder {
      private NodeInterfacePair _inputInterface;
      private String _inputVrf;
      private String _filterIn;
      private String _name;

      public EnterSrcIfaceDetail build() {
        return new EnterSrcIfaceDetail(_inputInterface, _filterIn, _inputVrf, _name);
      }

      public Builder setInputInterface(NodeInterfacePair inputInterface) {
        _inputInterface = inputInterface;
        return this;
      }

      public Builder setInputVrf(String inputVrf) {
        _inputVrf = inputVrf;
        return this;
      }

      public Builder setFilterIn(String filterIn) {
        _filterIn = filterIn;
        return this;
      }

      public Builder setName(String name) {
        _name = name;
        return this;
      }
    }
  }

  public static class EnterSrcIfaceAction extends StepAction {

    private static final String PROP_ACTION_RESULT = "actionResult";

    private @Nullable StepActionResult _actionResult;

    @JsonCreator
    public EnterSrcIfaceAction(
        @JsonProperty(PROP_ACTION_RESULT) @Nullable StepActionResult result,
        @JsonProperty(PROP_NAME) @Nullable String name) {
      super(firstNonNull(name, "EnterSrcIface"));
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
  private EnterSrcIfaceStep(
      @JsonProperty(PROP_DETAIL) EnterSrcIfaceDetail stepDetail,
      @JsonProperty(PROP_ACTION) EnterSrcIfaceAction stepAction) {
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
