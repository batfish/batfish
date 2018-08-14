package org.batfish.datamodel.routing_policy.statement;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;
import org.batfish.datamodel.routing_policy.expr.LongExpr;

public class SetMetric extends Statement {

  private static final String PROP_METRIC = "metric";

  /** */
  private static final long serialVersionUID = 1L;

  private LongExpr _metric;

  @JsonCreator
  private SetMetric() {}

  public SetMetric(LongExpr metric) {
    _metric = metric;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    SetMetric other = (SetMetric) obj;
    if (_metric == null) {
      if (other._metric != null) {
        return false;
      }
    } else if (!_metric.equals(other._metric)) {
      return false;
    }
    return true;
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
  public LongExpr getMetric() {
    return _metric;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_metric == null) ? 0 : _metric.hashCode());
    return result;
  }

  @JsonProperty(PROP_METRIC)
  public void setMetric(LongExpr metric) {
    _metric = metric;
  }
}
