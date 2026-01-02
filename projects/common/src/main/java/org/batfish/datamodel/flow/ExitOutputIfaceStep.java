package org.batfish.datamodel.flow;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.flow.ExitOutputIfaceStep.ExitOutputIfaceStepDetail;

/**
 * {@link Step} to represent the exiting of a {@link Flow} from an outgoing {@link
 * org.batfish.datamodel.Interface} on a node
 */
@JsonTypeName("ExitOutputInterface")
public final class ExitOutputIfaceStep extends Step<ExitOutputIfaceStepDetail> {

  /**
   * Details of the {@link Step} of exiting of a {@link Flow} from an output {@link
   * org.batfish.datamodel.Interface}
   */
  public static final class ExitOutputIfaceStepDetail {
    private static final String PROP_OUTPUT_INTERFACE = "outputInterface";
    private static final String PROP_TRANSFORMED_FLOW = "transformedFlow";

    private final @Nonnull NodeInterfacePair _outputInterface;
    private final @Nullable Flow _transformedFlow;

    private ExitOutputIfaceStepDetail(
        NodeInterfacePair outInterface, @Nullable Flow transformedFlow) {
      _outputInterface = outInterface;
      _transformedFlow = transformedFlow;
    }

    @JsonCreator
    private static ExitOutputIfaceStepDetail jsonCreator(
        @JsonProperty(PROP_OUTPUT_INTERFACE) @Nullable NodeInterfacePair outInterface,
        @JsonProperty(PROP_TRANSFORMED_FLOW) @Nullable() Flow transformedFlow) {
      checkArgument(outInterface != null, "Missing %s", PROP_OUTPUT_INTERFACE);
      return new ExitOutputIfaceStepDetail(outInterface, transformedFlow);
    }

    @JsonProperty(PROP_OUTPUT_INTERFACE)
    public @Nonnull NodeInterfacePair getOutputInterface() {
      return _outputInterface;
    }

    @JsonProperty(PROP_TRANSFORMED_FLOW)
    public @Nullable Flow getTransformedFlow() {
      return _transformedFlow;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      } else if (!(o instanceof ExitOutputIfaceStepDetail)) {
        return false;
      }
      ExitOutputIfaceStepDetail that = (ExitOutputIfaceStepDetail) o;
      return _outputInterface.equals(that._outputInterface)
          && Objects.equals(_transformedFlow, that._transformedFlow);
    }

    @Override
    public int hashCode() {
      return Objects.hash(_outputInterface, _transformedFlow);
    }

    public static Builder builder() {
      return new Builder();
    }

    /** Chained builder to create a {@link ExitOutputIfaceStepDetail} object */
    public static final class Builder {
      private @Nullable NodeInterfacePair _outputInterface;
      private @Nullable Flow _transformedFlow;

      public ExitOutputIfaceStepDetail build() {
        checkState(_outputInterface != null, "Must call setOutputInterface before building");
        return new ExitOutputIfaceStepDetail(_outputInterface, _transformedFlow);
      }

      public Builder setOutputInterface(NodeInterfacePair outputIface) {
        _outputInterface = outputIface;
        return this;
      }

      public Builder setTransformedFlow(@Nullable Flow transformedFlow) {
        _transformedFlow = transformedFlow;
        return this;
      }

      /** Only for use by {@link ExitOutputIfaceStepDetail#builder()}. */
      private Builder() {}
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  /** Chained builder to create a {@link ExitOutputIfaceStep} object */
  public static final class Builder {
    private @Nullable ExitOutputIfaceStepDetail _detail;
    private @Nullable StepAction _action;

    public ExitOutputIfaceStep build() {
      checkState(_action != null, "Must call setAction before building");
      checkState(_detail != null, "Must call setDetail before building");
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

    /** Only for use by {@link ExitOutputIfaceStep#builder()}. */
    private Builder() {}
  }

  @JsonCreator
  private static ExitOutputIfaceStep jsonCreator(
      @JsonProperty(PROP_DETAIL) @Nullable ExitOutputIfaceStepDetail detail,
      @JsonProperty(PROP_ACTION) @Nullable StepAction action) {
    checkArgument(action != null, "Missing %s", PROP_ACTION);
    checkArgument(detail != null, "Missing %s", PROP_DETAIL);
    return new ExitOutputIfaceStep(detail, action);
  }

  private ExitOutputIfaceStep(ExitOutputIfaceStepDetail detail, StepAction action) {
    super(detail, action);
  }
}
