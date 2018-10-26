package org.batfish.datamodel.flow;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.flow.EnterInputIfaceStep.EnterInputIfaceStepDetail;

/** {@link Step} to represent the entering of a {@link Flow} on an {@link Interface} in a node */
@JsonTypeName("EnterInputInterface")
public final class EnterInputIfaceStep extends Step<EnterInputIfaceStepDetail> {

  /**
   * Details of the {@link Step} when a {@link Flow} enters a node through an input {@link
   * Interface}
   */
  public static final class EnterInputIfaceStepDetail {

    private static final String PROP_INPUT_INTERFACE = "inputInterface";
    private static final String PROP_INPUT_VRF = "inputVrf";
    private static final String PROP_INPUT_FILTER = "inputFilter";

    private @Nonnull NodeInterfacePair _inputInterface;
    private @Nullable String _inputVrf;
    private @Nullable String _inputFilter;

    private EnterInputIfaceStepDetail(
        NodeInterfacePair inputInterface, @Nullable String inputFilter, @Nullable String inputVrf) {
      _inputInterface = inputInterface;
      _inputFilter = inputFilter;
      _inputVrf = inputVrf;
    }

    @JsonCreator
    private static EnterInputIfaceStepDetail jsonCreator(
        @JsonProperty(PROP_INPUT_INTERFACE) @Nullable NodeInterfacePair inputInterface,
        @JsonProperty(PROP_INPUT_FILTER) @Nullable String inputFilter,
        @JsonProperty(PROP_INPUT_VRF) @Nullable String inputVrf) {
      checkArgument(inputInterface != null, "Input interface should be set");
      return new EnterInputIfaceStepDetail(inputInterface, inputFilter, inputVrf);
    }

    @JsonProperty(PROP_INPUT_INTERFACE)
    @Nonnull
    public NodeInterfacePair getInputInterface() {
      return _inputInterface;
    }

    @JsonProperty(PROP_INPUT_FILTER)
    @Nullable
    public String getInputFilter() {
      return _inputFilter;
    }

    @JsonProperty(PROP_INPUT_VRF)
    @Nullable
    public String getInputVrf() {
      return _inputVrf;
    }

    public static Builder builder() {
      return new Builder();
    }

    /** Chained builder to create a {@link EnterInputIfaceStepDetail} object */
    public static final class Builder {
      private NodeInterfacePair _inputInterface;
      private String _inputVrf;
      private String _inputFilter;

      public EnterInputIfaceStepDetail build() {
        checkState(_inputInterface != null, "Must call setInputInterface before building");
        return new EnterInputIfaceStepDetail(_inputInterface, _inputFilter, _inputVrf);
      }

      public Builder setInputInterface(NodeInterfacePair inputInterface) {
        _inputInterface = inputInterface;
        return this;
      }

      public Builder setInputVrf(@Nullable String inputVrf) {
        _inputVrf = inputVrf;
        return this;
      }

      public Builder setInputFilter(@Nullable String inputFilter) {
        _inputFilter = inputFilter;
        return this;
      }

      /** Only for use by {@link EnterInputIfaceStepDetail#builder()}. */
      private Builder() {}
    }
  }

  private static final String PROP_DETAIL = "detail";
  private static final String PROP_ACTION = "action";

  public static Builder builder() {
    return new Builder();
  }

  private EnterInputIfaceStep(EnterInputIfaceStepDetail detail, StepAction action) {
    super(detail, action);
  }

  @JsonCreator
  private static EnterInputIfaceStep jsonCreator(
      @Nullable @JsonProperty(PROP_DETAIL) EnterInputIfaceStepDetail detail,
      @Nullable @JsonProperty(PROP_ACTION) StepAction action) {
    checkArgument(action != null, "Missing %s", PROP_ACTION);
    checkArgument(detail != null, "Missing %s", PROP_DETAIL);
    return new EnterInputIfaceStep(detail, action);
  }

  /** Chained builder to create an {@link EnterInputIfaceStep} object */
  public static final class Builder {
    @Nullable private EnterInputIfaceStepDetail _detail;
    @Nullable private StepAction _action;

    public EnterInputIfaceStep build() {
      checkState(_action != null, "setAction must be called before building");
      checkState(_detail != null, "setDetail must be called before building");
      return new EnterInputIfaceStep(_detail, _action);
    }

    public Builder setDetail(@Nullable EnterInputIfaceStepDetail detail) {
      _detail = detail;
      return this;
    }

    public Builder setAction(@Nullable StepAction action) {
      _action = action;
      return this;
    }

    /** Only for use by {@link EnterInputIfaceStep#builder()}. */
    private Builder() {}
  }
}
