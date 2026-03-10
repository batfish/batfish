package org.batfish.datamodel.flow;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.flow.FilterStep.FilterStepDetail;

/** {@link Step} to represent the checking of a filter for a {@link org.batfish.datamodel.Flow} */
@JsonTypeName("Filter")
public class FilterStep extends Step<FilterStepDetail> {

  public enum FilterType {
    /** ingress filter */
    INGRESS_FILTER,
    /** egress filter */
    EGRESS_FILTER,
    /** egress filter matching on original (pre-transformation) flow */
    EGRESS_ORIGINAL_FLOW_FILTER,
    /** post-transformation ingress filter */
    POST_TRANSFORMATION_INGRESS_FILTER,
    /** pre-transformation egress filter */
    PRE_TRANSFORMATION_EGRESS_FILTER;

    public FlowDisposition deniedDisposition() {
      return switch (this) {
        case INGRESS_FILTER, POST_TRANSFORMATION_INGRESS_FILTER -> FlowDisposition.DENIED_IN;
        case EGRESS_FILTER, EGRESS_ORIGINAL_FLOW_FILTER, PRE_TRANSFORMATION_EGRESS_FILTER ->
            FlowDisposition.DENIED_OUT;
      };
    }
  }

  /** Details of {@link Step} about applying the filter to a {@link Flow} */
  public static final class FilterStepDetail {
    private static final String PROP_FILTER = "filter";
    private static final String PROP_TYPE = "type";
    private static final String PROP_INPUT_INTERFACE = "inputInterface";
    private static final String PROP_FLOW = "flow";

    private final @Nonnull String _filter;
    private final @Nonnull FilterType _type;
    private final @Nullable String _inputInterface;
    private final @Nonnull Flow _flow;

    public FilterStepDetail(
        @Nonnull String filter,
        @Nonnull FilterType type,
        @Nullable String inputInterface,
        @Nonnull Flow flow) {
      _filter = filter;
      _type = type;
      _inputInterface = inputInterface;
      _flow = flow;
    }

    @JsonCreator
    private static FilterStepDetail jsonCreator(
        @JsonProperty(PROP_FILTER) @Nullable String filter,
        @JsonProperty(PROP_TYPE) @Nullable FilterType type,
        @JsonProperty(PROP_INPUT_INTERFACE) @Nullable String inputInterface,
        @JsonProperty(PROP_FLOW) @Nullable Flow flow) {
      checkArgument(filter != null, "Missing %s", PROP_FILTER);
      checkArgument(type != null, "Missing %s", PROP_TYPE);
      checkArgument(flow != null, "Missing %s", PROP_FLOW);
      return new FilterStepDetail(filter, type, inputInterface, flow);
    }

    @JsonProperty(PROP_FILTER)
    public @Nonnull String getFilter() {
      return _filter;
    }

    @JsonProperty(PROP_TYPE)
    public @Nonnull FilterType getType() {
      return _type;
    }

    @JsonProperty(PROP_INPUT_INTERFACE)
    public @Nullable String getInputInterface() {
      return _inputInterface;
    }

    @JsonProperty(PROP_FLOW)
    public @Nonnull Flow getFlow() {
      return _flow;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      } else if (!(o instanceof FilterStepDetail)) {
        return false;
      }
      FilterStepDetail that = (FilterStepDetail) o;
      return _filter.equals(that._filter)
          && _type == that._type
          && Objects.equals(_inputInterface, that._inputInterface)
          && _flow.equals(that._flow);
    }

    @Override
    public int hashCode() {
      return Objects.hash(_filter, _type, _inputInterface, _flow);
    }
  }

  @JsonCreator
  private static FilterStep jsonCreator(
      @JsonProperty(PROP_DETAIL) @Nullable FilterStepDetail detail,
      @JsonProperty(PROP_ACTION) @Nullable StepAction action) {
    checkArgument(action != null, "Missing %s", PROP_ACTION);
    checkArgument(detail != null, "Missing %s", PROP_DETAIL);
    return new FilterStep(detail, action);
  }

  public FilterStep(FilterStepDetail detail, StepAction action) {
    super(detail, action);
  }
}
