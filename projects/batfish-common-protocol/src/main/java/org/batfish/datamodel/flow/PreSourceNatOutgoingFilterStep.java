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
    private static final String PROP_OUTPUT_INTERFACE = "outputInterface";
    private static final String PROP_FILTER = "preSourceNatFilter";

    private @Nonnull String _node;
    private @Nonnull String _outputInterface;
    private @Nullable String _filter;

    private PreSourceNatOutgoingFilterStepDetail(
        String node, String outInterface, @Nullable String filter) {
      _node = node;
      _outputInterface = outInterface;
      _filter = filter;
    }

    @JsonCreator
    private static PreSourceNatOutgoingFilterStepDetail jsonCreator(
        @JsonProperty(PROP_NODE) @Nullable String node,
        @JsonProperty(PROP_OUTPUT_INTERFACE) @Nullable String outInterface,
        @JsonProperty(PROP_FILTER) @Nullable String filter) {
      checkArgument(node != null, "Missing %s", PROP_NODE);
      checkArgument(outInterface != null, "Missing %s", PROP_OUTPUT_INTERFACE);
      return new PreSourceNatOutgoingFilterStepDetail(node, outInterface, filter);
    }

    @JsonProperty(PROP_NODE)
    @Nonnull
    public String getNode() {
      return _node;
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

    public static Builder builder() {
      return new Builder();
    }

    /** Chained builder to create a {@link PreSourceNatOutgoingFilterStepDetail} object */
    public static class Builder {
      private @Nullable String _node;
      private @Nullable String _outputInterface;
      private @Nullable String _filter;

      public PreSourceNatOutgoingFilterStepDetail build() {
        checkState(_node != null, "Missing %s", _node);
        checkState(_outputInterface != null, "Missing %s", _outputInterface);
        return new PreSourceNatOutgoingFilterStepDetail(_node, _outputInterface, _filter);
      }

      public Builder setNode(String node) {
        _node = node;
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

      /** Only for use by {@link PreSourceNatOutgoingFilterStepDetail#builder()}. */
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
      @Nullable @JsonProperty(PROP_DETAIL) PreSourceNatOutgoingFilterStepDetail detail,
      @Nullable @JsonProperty(PROP_ACTION) StepAction action) {
    checkArgument(action != null, "Missing %s", PROP_ACTION);
    checkArgument(detail != null, "Missing %s", PROP_DETAIL);
    return new PreSourceNatOutgoingFilterStep(detail, action);
  }

  private PreSourceNatOutgoingFilterStep(
      PreSourceNatOutgoingFilterStepDetail detail, StepAction action) {
    super(detail, action);
  }
}
