package org.batfish.datamodel.routing_policy.statement;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;
import org.batfish.datamodel.routing_policy.expr.LongExpr;

@ParametersAreNonnullByDefault
public final class SetMetric extends Statement {
  private static final String PROP_METRIC = "metric";

  /** */
  private static final long serialVersionUID = 1L;

  @Nonnull private final LongExpr _metric;

  @JsonCreator
  private static SetMetric jsonCreator(@Nullable @JsonProperty(PROP_METRIC) LongExpr metric) {
    checkArgument(metric != null, "%s must be provided", PROP_METRIC);
    return new SetMetric(metric);
  }

  public SetMetric(LongExpr metric) {
    _metric = metric;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof SetMetric)) {
      return false;
    }
    SetMetric other = (SetMetric) obj;
    return _metric.equals(other._metric);
  }

  @Override
  public Result execute(Environment environment) {
    Result result = new Result();
    long metric = _metric.evaluate(environment);
    environment.getOutputRoute().setMetric(metric);
    if (environment.getWriteToIntermediateBgpAttributes()) {
      environment.getIntermediateBgpAttributes().setMetric(metric);
    }
    return result;
  }

  @JsonProperty(PROP_METRIC)
  @Nonnull
  public LongExpr getMetric() {
    return _metric;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _metric.hashCode();
    return result;
  }
}
