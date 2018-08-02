package org.batfish.datamodel.routing_policy.statement;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Objects;
import org.batfish.datamodel.EigrpExternalRoute;
import org.batfish.datamodel.eigrp.EigrpMetric;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;
import org.batfish.datamodel.routing_policy.expr.EigrpMetricExpr;

public class SetEigrpMetric extends Statement {

  private static final long serialVersionUID = 1L;

  private EigrpMetricExpr _metric;

  @JsonCreator
  private SetEigrpMetric() {}

  public SetEigrpMetric(EigrpMetricExpr metric) {
    _metric = metric;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof SetEigrpMetric)) {
      return false;
    }

    SetEigrpMetric rhs = (SetEigrpMetric) obj;
    return Objects.equals(_metric, rhs._metric);
  }

  @Override
  public Result execute(Environment environment) {
    Result result = new Result();
    EigrpMetric metric = _metric.evaluate(environment);
    ((EigrpExternalRoute.Builder) environment.getOutputRoute()).setEigrpMetric(metric);
    return result;
  }

  public EigrpMetricExpr getMetric() {
    return _metric;
  }

  public void setMetric(EigrpMetricExpr metric) {
    _metric = metric;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_metric);
  }
}
