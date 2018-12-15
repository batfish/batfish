package org.batfish.datamodel.flow;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.flow.FilterStep.FilterStepDetail;

/** {@link Step} to represent the checking of a filter for a {@link org.batfish.datamodel.Flow} */
@JsonTypeName("Filter")
public class FilterStep extends Step<FilterStepDetail> {

  public enum FilterType {
    /** ingress filter */
    INGRESS_FILTER,
    /** egress filter */
    EGRESS_FILTER,
    /** preSourceNat filter */
    PRE_SOURCE_NAT_FILTER
  }

  /** Details of {@link Step} about applying the filter to a {@link Flow} */
  public static final class FilterStepDetail {
    private static final String PROP_FILTER = "filter";
    private static final String PROP_TYPE = "type";

    private @Nonnull String _filter;
    private @Nonnull FilterType _type;

    public FilterStepDetail(@Nonnull String filter, @Nonnull FilterType type) {
      _filter = filter;
      _type = type;
    }

    @JsonCreator
    private static FilterStepDetail jsonCreator(
        @JsonProperty(PROP_FILTER) @Nullable String filter,
        @JsonProperty(PROP_TYPE) @Nullable FilterType type) {
      checkArgument(filter != null, "Missing %s", PROP_FILTER);
      checkArgument(type != null, "Missing %s", PROP_TYPE);
      return new FilterStepDetail(filter, type);
    }

    @JsonProperty(PROP_FILTER)
    @Nonnull
    public String getFilter() {
      return _filter;
    }

    @JsonProperty(PROP_TYPE)
    @Nonnull
    public FilterType getType() {
      return _type;
    }
  }

  private static final String PROP_DETAIL = "detail";
  private static final String PROP_ACTION = "action";

  @JsonCreator
  private static FilterStep jsonCreator(
      @Nullable @JsonProperty(PROP_DETAIL) FilterStepDetail detail,
      @Nullable @JsonProperty(PROP_ACTION) StepAction action) {
    checkArgument(action != null, "Missing %s", PROP_ACTION);
    checkArgument(detail != null, "Missing %s", PROP_DETAIL);
    return new FilterStep(detail, action);
  }

  public FilterStep(FilterStepDetail detail, StepAction action) {
    super(detail, action);
  }
}
