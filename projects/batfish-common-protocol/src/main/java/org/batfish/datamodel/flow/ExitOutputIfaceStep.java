package org.batfish.datamodel.flow;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nullable;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.collections.NodeInterfacePair;

/**
 * {@link Step} to represent the exiting of a {@link Flow} from an outgoing {@link
 * org.batfish.datamodel.Interface} on a node
 */
public class ExitOutputIfaceStep extends Step {

  /**
   * {@link StepDetail} containing details of the exiting of a {@link Flow} from an output {@link
   * org.batfish.datamodel.Interface}
   */
  public static class ExitOutputIfaceStepDetail extends StepDetail {
    private static final String PROP_OUTPUT_INTERFACE = "outputInterface";
    private static final String PROP_OUTPUT_FILTER = "outputFilter";
    private static final String PROP_ORIGINAL_FLOW = "originalFlow";
    private static final String PROP_TRANSFORMED_FLOW = "transformedFlow";

    private @Nullable NodeInterfacePair _outputInterface;
    private @Nullable String _outputFilter;
    private @Nullable Flow _originalFlow;
    private @Nullable Flow _transformedFlow;

    private ExitOutputIfaceStepDetail(
        @JsonProperty(PROP_OUTPUT_INTERFACE) @Nullable NodeInterfacePair outInterface,
        @JsonProperty(PROP_OUTPUT_FILTER) @Nullable String outputFilter,
        @JsonProperty(PROP_ORIGINAL_FLOW) @Nullable Flow originalFlow,
        @JsonProperty(PROP_TRANSFORMED_FLOW) @Nullable() Flow transformedFlow,
        @JsonProperty(PROP_NAME) @Nullable String name) {
      super(firstNonNull(name, "ExitOutIface"));
      _outputInterface = outInterface;
      _outputFilter = outputFilter;
      _originalFlow = originalFlow;
      _transformedFlow = transformedFlow;
    }

    @JsonProperty(PROP_OUTPUT_INTERFACE)
    @Nullable
    public NodeInterfacePair getOutputInterface() {
      return _outputInterface;
    }

    @JsonProperty(PROP_OUTPUT_FILTER)
    @Nullable
    public String getOutputFilter() {
      return _outputFilter;
    }

    @JsonProperty(PROP_ORIGINAL_FLOW)
    @Nullable
    public Flow getOriginalFlow() {
      return _originalFlow;
    }

    @JsonProperty(PROP_TRANSFORMED_FLOW)
    @Nullable
    public Flow getTransformedFlow() {
      return _transformedFlow;
    }

    public static Builder builder() {
      return new Builder();
    }

    /** Chained builder to create a {@link ExitOutputIfaceStepDetail} object */
    public static class Builder {
      private NodeInterfacePair _outputInterface;
      private String _outputFilter;
      private Flow _originalFlow;
      private Flow _transformedFlow;
      private String _name;

      public ExitOutputIfaceStepDetail build() {
        return new ExitOutputIfaceStepDetail(
            _outputInterface, _outputFilter, _originalFlow, _transformedFlow, _name);
      }

      public Builder setName(String name) {
        _name = name;
        return this;
      }

      public Builder setOutputInterface(NodeInterfacePair outputIface) {
        _outputInterface = outputIface;
        return this;
      }

      public Builder setOutputFilter(String outputFilter) {
        _outputFilter = outputFilter;
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


  private static final String PROP_DETAIL = "detail";
  private static final String PROP_ACTION = "action";

  @JsonCreator
  public ExitOutputIfaceStep(
      @JsonProperty(PROP_DETAIL) ExitOutputIfaceStepDetail detail,
      @JsonProperty(PROP_ACTION) StepAction action) {
    super(detail, action);
  }

  public static Builder builder() {
    return new Builder();
  }

  /** Chained builder to create a {@link ExitOutputIfaceStep} object */
  public static class Builder {
    private ExitOutputIfaceStepDetail _detail;
    private StepAction _action;

    public ExitOutputIfaceStep build() {
      return new ExitOutputIfaceStep(_detail, _action);
    }

    public Builder setDetail(ExitOutputIfaceStepDetail detail) {
      _detail = detail;
      return this;
    }

    public Builder setAction(StepAction action) {
      _action = action;
      return this;
    }
  }
}
