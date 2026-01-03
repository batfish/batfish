package org.batfish.datamodel.routing_policy.statement;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.HasWritableOspfMetricType;
import org.batfish.datamodel.ospf.OspfMetricType;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

public class SetOspfMetricType extends Statement {
  private static final String PROP_METRIC_TYPE = "metricType";

  private OspfMetricType _metricType;

  @JsonCreator
  private SetOspfMetricType() {}

  public SetOspfMetricType(OspfMetricType metricType) {
    _metricType = metricType;
    if (_metricType == null) {
      throw new BatfishException("Cannot set null ospf metric type");
    }
  }

  @Override
  public <T, U> T accept(StatementVisitor<T, U> visitor, U arg) {
    return visitor.visitSetOspfMetricType(this, arg);
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
    SetOspfMetricType other = (SetOspfMetricType) obj;
    if (_metricType != other._metricType) {
      return false;
    }
    return true;
  }

  @Override
  public Result execute(Environment environment) {
    Result result = new Result();
    if ((environment.getOutputRoute() instanceof HasWritableOspfMetricType<?, ?>)) {
      HasWritableOspfMetricType<?, ?> outputRoute =
          (HasWritableOspfMetricType<?, ?>) environment.getOutputRoute();
      outputRoute.setOspfMetricType(_metricType);
    }
    return result;
  }

  @JsonProperty(PROP_METRIC_TYPE)
  public OspfMetricType getMetricType() {
    return _metricType;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_metricType == null) ? 0 : _metricType.ordinal());
    return result;
  }

  @JsonProperty(PROP_METRIC_TYPE)
  public void setMetricType(OspfMetricType metricType) {
    _metricType = metricType;
  }
}
