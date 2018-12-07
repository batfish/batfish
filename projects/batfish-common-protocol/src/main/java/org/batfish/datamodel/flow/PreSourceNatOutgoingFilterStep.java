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
import org.batfish.datamodel.flow.PreSourceNatOutgoingFilterStep.PreSourceNatOutgoingFilterStepDetail;

/**
 * {@link Step} to represent the checking of the pre-source-nat outgoing filter on an interface for
 * a {@link org.batfish.datamodel.Flow}
 */
@JsonTypeName("PreSourceNatOutgoingFilter")
public class PreSourceNatOutgoingFilterStep extends Step<PreSourceNatOutgoingFilterStepDetail> {

  /**
   * Details of {@link Step} about applying pre-sourcenat outgoing filter to direct a {@link Flow}
   * from an input {@link Interface} to output {@link Interface}
   */
  public static final class PreSourceNatOutgoingFilterStepDetail {
    private static final String PROP_NODE = "node";
    private static final String PROP_INPUT_INTERFACE = "inputInterface";
    private static final String PROP_OUTPUT_INTERFACE = "outputInterface";
    private static final String PROP_FILTER = "preSourceNatFilter";
    private static final String PROP_TRANSFORMED_FLOW = "transformedFlow";

    private @Nonnull String _node;
    private @Nonnull String _inputInterface;
    private @Nonnull String _outputInterface;
    private @Nullable String _filter;
    private @Nullable Flow _transformedFlow;

    private PreSourceNatOutgoingFilterStepDetail(
        String node,
        String inInterface,
        String outInterface,
        @Nullable String filter,
        @Nullable() Flow transformedFlow) {
      _node = node;
      _inputInterface = inInterface;
      _outputInterface = outInterface;
      _filter = filter;
      _transformedFlow = transformedFlow;
    }

    @JsonCreator
    private static PreSourceNatOutgoingFilterStepDetail jsonCreator(
        @JsonProperty(PROP_NODE) @Nonnull String node,
        @JsonProperty(PROP_INPUT_INTERFACE) @Nonnull String inInterface,
        @JsonProperty(PROP_OUTPUT_INTERFACE) @Nonnull String outInterface,
        @JsonProperty(PROP_FILTER) @Nullable String filter,
        @JsonProperty(PROP_TRANSFORMED_FLOW) @Nullable() Flow transformedFlow) {
      checkArgument(
          node != null && inInterface != null && outInterface != null,
          "Missing one of %s, %s, %s",
          PROP_NODE,
          PROP_INPUT_INTERFACE,
          PROP_OUTPUT_INTERFACE);
      return new PreSourceNatOutgoingFilterStepDetail(
          node, inInterface, outInterface, filter, transformedFlow);
    }

    @JsonProperty(PROP_NODE)
    @Nonnull
    public String getNode() {
      return _node;
    }

    @JsonProperty(PROP_INPUT_INTERFACE)
    @Nonnull
    public String getInputInterface() {
      return _inputInterface;
    }

    @JsonProperty(PROP_OUTPUT_INTERFACE)
    @Nonnull
    public String getOutputInterface() {
      return _outputInterface;
    }

    @JsonProperty(PROP_FILTER)
    @Nullable
    public String getFilter() {
      return _filter;
    }

    @JsonProperty(PROP_TRANSFORMED_FLOW)
    @Nullable
    public Flow getTransformedFlow() {
      return _transformedFlow;
    }

    public static Builder builder() {
      return new Builder();
    }

    /** Chained builder to create a {@link PreSourceNatOutgoingFilterStepDetail} object */
    public static class Builder {
      private @Nullable String _node;
      private @Nullable String _inputInterface;
      private @Nullable String _outputInterface;
      private @Nullable String _filter;
      private @Nullable Flow _transformedFlow;

      public PreSourceNatOutgoingFilterStepDetail build() {
        checkState(
            _node != null && _inputInterface != null && _outputInterface != null,
            "Must call setNode, setInputInterface and setOutputInterface before building");
        return new PreSourceNatOutgoingFilterStepDetail(
            _node, _inputInterface, _outputInterface, _filter, _transformedFlow);
      }

      public Builder setNode(String node) {
        _node = node;
        return this;
      }

      public Builder setInputInterface(String inputIface) {
        _inputInterface = inputIface;
        return this;
      }

      public Builder setOutputInterface(String outputIface) {
        _outputInterface = outputIface;
        return this;
      }

      public Builder setFilter(@Nullable String filter) {
        _filter = filter;
        return this;
      }

      public Builder setTransformedFlow(@Nullable Flow transformedFlow) {
        _transformedFlow = transformedFlow;
        return this;
      }

      /**
       * Only for use by {@link
       * PreSourceNatOutgoingFilterStepDetail#builder()}.
       */
      private Builder() {}
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  /** Chained builder to create a {@link PreSourceNatOutgoingFilterStep} object */
  public static final class Builder {
    private @Nullable PreSourceNatOutgoingFilterStepDetail _detail;
    private @Nullable StepAction _action;

    public PreSourceNatOutgoingFilterStep build() {
      checkState(_action != null, "Must call setAction before building");
      checkState(_detail != null, "Must call setDetail before building");
      return new PreSourceNatOutgoingFilterStep(_detail, _action);
    }

    public Builder setDetail(
        PreSourceNatOutgoingFilterStep.PreSourceNatOutgoingFilterStepDetail detail) {
      _detail = detail;
      return this;
    }

    public Builder setAction(StepAction action) {
      _action = action;
      return this;
    }

    /** Only for use by {@link PreSourceNatOutgoingFilterStep#builder()}. */
    private Builder() {}
  }

  private static final String PROP_DETAIL = "detail";
  private static final String PROP_ACTION = "action";

  @JsonCreator
  private static PreSourceNatOutgoingFilterStep jsonCreator(
      @Nullable @JsonProperty(PROP_DETAIL)
          PreSourceNatOutgoingFilterStepDetail detail,
      @Nullable @JsonProperty(PROP_ACTION) StepAction action) {
    checkArgument(action != null, "Missing %s", PROP_ACTION);
    checkArgument(detail != null, "Missing %s", PROP_DETAIL);
    return new PreSourceNatOutgoingFilterStep(detail, action);
  }

  private PreSourceNatOutgoingFilterStep(
      PreSourceNatOutgoingFilterStepDetail detail,
      StepAction action) {
    super(detail, action);
  }
}
