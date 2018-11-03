package org.batfish.datamodel.routing_policy.expr;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

@ParametersAreNonnullByDefault
public final class MatchLocalPreference extends BooleanExpr {
  private static final String PROP_COMPARATOR = "comparator";
  private static final String PROP_METRIC = "metric";

  /** */
  private static final long serialVersionUID = 1L;

  @Nonnull private IntComparator _comparator;

  @Nonnull private IntExpr _metric;

  @JsonCreator
  private static MatchLocalPreference jsonCreator(
      @Nullable @JsonProperty(PROP_COMPARATOR) IntComparator comparator,
      @Nullable @JsonProperty(PROP_METRIC) IntExpr metric) {
    checkArgument(comparator != null, "%s must be provided", PROP_COMPARATOR);
    checkArgument(metric != null, "%s must be provided", PROP_METRIC);
    return new MatchLocalPreference(comparator, metric);
  }

  public MatchLocalPreference(IntComparator comparator, IntExpr metric) {
    _comparator = comparator;
    _metric = metric;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof MatchLocalPreference)) {
      return false;
    }
    MatchLocalPreference other = (MatchLocalPreference) obj;
    return _comparator == other._comparator && _metric.equals(other._metric);
  }

  @Override
  public Result evaluate(Environment environment) {
    throw new UnsupportedOperationException("no implementation for generated method");
  }

  @JsonProperty(PROP_COMPARATOR)
  @Nonnull
  public IntComparator getComparator() {
    return _comparator;
  }

  @JsonProperty(PROP_METRIC)
  @Nonnull
  public IntExpr getMetric() {
    return _metric;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_comparator == null) ? 0 : _comparator.ordinal());
    result = prime * result + ((_metric == null) ? 0 : _metric.hashCode());
    return result;
  }

  public void setComparator(IntComparator comparator) {
    _comparator = comparator;
  }

  public void setMetric(IntExpr metric) {
    _metric = metric;
  }
}
