package org.batfish.datamodel.routing_policy.expr;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.batfish.datamodel.routing_policy.Environment;

public class DecrementMetric extends LongExpr {

  private static final long serialVersionUID = 1L;

  private long _subtrahend;

  @JsonCreator
  private DecrementMetric() {}

  public DecrementMetric(long subtrahend) {
    _subtrahend = subtrahend;
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
    DecrementMetric other = (DecrementMetric) obj;
    if (_subtrahend != other._subtrahend) {
      return false;
    }
    return true;
  }

  @Override
  public long evaluate(Environment environment) {
    long oldMetric = environment.getOriginalRoute().getMetric();
    long newVal = oldMetric - _subtrahend;
    return newVal;
  }

  public long getSubtrahend() {
    return _subtrahend;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Long.hashCode(_subtrahend);
    return result;
  }

  public void setSubtrahend(long subtrahend) {
    _subtrahend = subtrahend;
  }
}
