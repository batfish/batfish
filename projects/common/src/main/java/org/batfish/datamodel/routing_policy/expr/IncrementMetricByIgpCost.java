package org.batfish.datamodel.routing_policy.expr;

import com.fasterxml.jackson.annotation.JsonCreator;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.routing_policy.Environment;

/**
 * The route's metric plus the IGP cost to its next hop, as the exporting VRF resolves that next
 * hop, for vendors whose export policies can add the IGP distance into the metric.
 *
 * <p>The IGP cost comes from the {@link Environment}; when the environment has none (the policy is
 * not being evaluated for a BGP export), the metric is left as is.
 */
@ParametersAreNonnullByDefault
public final class IncrementMetricByIgpCost extends LongExpr {
  private static final long MAX_INT_VALUE = 0xFFFFFFFFL;

  private static final IncrementMetricByIgpCost INSTANCE = new IncrementMetricByIgpCost();

  @JsonCreator
  public static IncrementMetricByIgpCost instance() {
    return INSTANCE;
  }

  private IncrementMetricByIgpCost() {}

  @Override
  public <T, U> T accept(LongExprVisitor<T, U> visitor, U arg) {
    return visitor.visitIncrementMetricByIgpCost(this, arg);
  }

  @Override
  public long evaluate(Environment environment) {
    long metric = environment.getOriginalRoute().getMetric();
    @Nullable Long igpCost = environment.getIgpCostToNextHop();
    if (igpCost == null) {
      return metric;
    }
    return Math.min(metric + igpCost, MAX_INT_VALUE);
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    return obj instanceof IncrementMetricByIgpCost;
  }

  @Override
  public int hashCode() {
    return IncrementMetricByIgpCost.class.hashCode();
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}
