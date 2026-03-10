package org.batfish.datamodel.routing_policy.statement;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;
import org.batfish.datamodel.routing_policy.expr.LongExpr;

@ParametersAreNonnullByDefault
public final class SetMetric extends Statement {
  private static final String PROP_METRIC = "metric";

  private final @Nonnull LongExpr _metric;

  @JsonCreator
  private static SetMetric jsonCreator(@JsonProperty(PROP_METRIC) @Nullable LongExpr metric) {
    checkArgument(metric != null, "%s must be provided", PROP_METRIC);
    return new SetMetric(metric);
  }

  public SetMetric(LongExpr metric) {
    _metric = metric;
  }

  @Override
  public <T, U> T accept(StatementVisitor<T, U> visitor, U arg) {
    return visitor.visitSetMetric(this, arg);
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
  public @Nonnull LongExpr getMetric() {
    return _metric;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _metric.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("metric", _metric).toString();
  }
}
