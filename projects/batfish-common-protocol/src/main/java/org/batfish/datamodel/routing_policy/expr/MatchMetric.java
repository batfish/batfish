package org.batfish.datamodel.routing_policy.expr;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

@ParametersAreNonnullByDefault
public final class MatchMetric extends BooleanExpr {
  private static final String PROP_COMPARATOR = "comparator";
  private static final String PROP_METRIC = "metric";

  @Nonnull private final IntComparator _comparator;
  @Nonnull private final LongExpr _metric;

  @JsonCreator
  private static MatchMetric jsonCreator(
      @Nullable @JsonProperty(PROP_COMPARATOR) IntComparator comparator,
      @Nullable @JsonProperty(PROP_METRIC) LongExpr metric) {
    checkArgument(comparator != null, "%s must be provided", PROP_COMPARATOR);
    checkArgument(metric != null, "%s must be provided", PROP_METRIC);
    return new MatchMetric(comparator, metric);
  }

  public MatchMetric(IntComparator comparator, LongExpr metric) {
    _comparator = comparator;
    _metric = metric;
  }

  @Override
  public <T, U> T accept(BooleanExprVisitor<T, U> visitor, U arg) {
    return visitor.visitMatchMetric(this, arg);
  }

  @Override
  public Result evaluate(Environment environment) {
    return _comparator.apply(
        Math.toIntExact(environment.getOriginalRoute().getMetric()), _metric.evaluate(environment));
  }

  @JsonProperty(PROP_COMPARATOR)
  @Nonnull
  public IntComparator getComparator() {
    return _comparator;
  }

  @JsonProperty(PROP_METRIC)
  @Nonnull
  public LongExpr getMetric() {
    return _metric;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof MatchMetric)) {
      return false;
    }
    MatchMetric other = (MatchMetric) obj;
    return _comparator == other._comparator && Objects.equals(_metric, other._metric);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_comparator.ordinal(), _metric);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(MatchMetric.class)
        .add(PROP_COMPARATOR, _comparator)
        .add(PROP_METRIC, _metric)
        .toString();
  }
}
