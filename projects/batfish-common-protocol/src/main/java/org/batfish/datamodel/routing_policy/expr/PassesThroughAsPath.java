package org.batfish.datamodel.routing_policy.expr;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

@ParametersAreNonnullByDefault
public final class PassesThroughAsPath extends BooleanExpr {
  private static final String PROP_EXACT = "exact";
  private static final String PROP_RANGE = "range";

  /** */
  private static final long serialVersionUID = 1L;

  private boolean _exact;

  @Nonnull private List<SubRangeExpr> _range;

  @JsonCreator
  private static PassesThroughAsPath jsonCreator(
      @Nullable @JsonProperty(PROP_EXACT) Boolean exact,
      @Nullable @JsonProperty(PROP_RANGE) List<SubRangeExpr> range) {
    checkArgument(exact != null, "%s must be provided", PROP_EXACT);
    return new PassesThroughAsPath(firstNonNull(range, ImmutableList.of()), exact);
  }

  public PassesThroughAsPath(List<SubRangeExpr> range, boolean exact) {
    _range = range;
    _exact = exact;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof PassesThroughAsPath)) {
      return false;
    }
    PassesThroughAsPath other = (PassesThroughAsPath) obj;
    if (_exact != other._exact) {
      return false;
    }
    return _range.equals(other._range);
  }

  @Override
  public Result evaluate(Environment environment) {
    throw new UnsupportedOperationException("no implementation for generated method");
  }

  @JsonProperty(PROP_EXACT)
  public boolean getExact() {
    return _exact;
  }

  @JsonProperty(PROP_RANGE)
  @Nonnull
  public List<SubRangeExpr> getRange() {
    return _range;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (_exact ? 1231 : 1237);
    result = prime * result + _range.hashCode();
    return result;
  }

  public void setExact(boolean exact) {
    _exact = exact;
  }

  public void setRange(List<SubRangeExpr> range) {
    _range = range;
  }
}
