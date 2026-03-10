package org.batfish.datamodel.routing_policy.statement;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.EigrpRoute.Builder;
import org.batfish.datamodel.eigrp.ClassicMetric;
import org.batfish.datamodel.eigrp.EigrpMetric;
import org.batfish.datamodel.eigrp.EigrpMetricValues;
import org.batfish.datamodel.eigrp.EigrpProcess;
import org.batfish.datamodel.eigrp.EigrpProcessMode;
import org.batfish.datamodel.eigrp.WideMetric;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;
import org.batfish.datamodel.routing_policy.expr.EigrpMetricExpr;

@ParametersAreNonnullByDefault
public final class SetEigrpMetric extends Statement {
  private static final String PROP_METRIC = "metric";

  private final @Nonnull EigrpMetricExpr _metric;

  @JsonCreator
  private static SetEigrpMetric jsonCreator(
      @JsonProperty(PROP_METRIC) @Nullable EigrpMetricExpr metric) {
    checkArgument(metric != null, "%s must be provided", PROP_METRIC);
    return new SetEigrpMetric(metric);
  }

  public SetEigrpMetric(EigrpMetricExpr metric) {
    _metric = metric;
  }

  @Override
  public <T, U> T accept(StatementVisitor<T, U> visitor, U arg) {
    return visitor.visitSetEigrpMetric(this, arg);
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof SetEigrpMetric)) {
      return false;
    }
    SetEigrpMetric rhs = (SetEigrpMetric) obj;
    return _metric.equals(rhs._metric);
  }

  @Override
  public int hashCode() {
    return _metric.hashCode();
  }

  @Override
  public Result execute(Environment environment) {
    Result result = new Result();
    EigrpMetricValues metricValues = _metric.evaluate(environment);
    EigrpProcess eigrpProcess = environment.getEigrpProcess();
    checkState(eigrpProcess != null);
    Builder<?, ?> outputRoute = (Builder<?, ?>) environment.getOutputRoute();
    EigrpMetric newMetric =
        eigrpProcess.getMode() == EigrpProcessMode.NAMED
            ? WideMetric.builder().setValues(metricValues).build()
            : ClassicMetric.builder().setValues(metricValues).build();
    outputRoute.setEigrpMetric(newMetric);
    return result;
  }

  @JsonProperty(PROP_METRIC)
  public @Nonnull EigrpMetricExpr getMetric() {
    return _metric;
  }
}
