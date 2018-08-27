package org.batfish.datamodel.routing_policy.statement;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.isis.IsisMetricType;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

@ParametersAreNonnullByDefault
public final class SetIsisMetricType extends Statement {
  private static final String PROP_METRIC_TYPE = "metricType";
  /** */
  private static final long serialVersionUID = 1L;

  @Nonnull private IsisMetricType _metricType;

  @JsonCreator
  private static SetIsisMetricType jsonCreator(
      @Nullable @JsonProperty(PROP_METRIC_TYPE) IsisMetricType type) {
    checkArgument(type != null, "%s must be provided", PROP_METRIC_TYPE);
    return new SetIsisMetricType(type);
  }

  public SetIsisMetricType(IsisMetricType metricType) {
    _metricType = metricType;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof SetIsisMetricType)) {
      return false;
    }
    SetIsisMetricType other = (SetIsisMetricType) obj;
    return _metricType == other._metricType;
  }

  @Override
  public Result execute(Environment environment) {
    throw new UnsupportedOperationException("no implementation for generated method");
    // TODO Auto-generated method stub
  }

  @JsonProperty(PROP_METRIC_TYPE)
  @Nonnull
  public IsisMetricType getMetricType() {
    return _metricType;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _metricType.ordinal();
    return result;
  }

  public void setMetricType(IsisMetricType metricType) {
    _metricType = metricType;
  }
}
