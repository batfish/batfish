package org.batfish.datamodel.routing_policy.expr;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.batfish.datamodel.routing_policy.Environment;

public class DecrementMetric extends LongExpr {

  private long _subtrahend;

  @JsonCreator
  private DecrementMetric() {}

  public DecrementMetric(long subtrahend) {
    _subtrahend = subtrahend;
  }

  @Override
  public <T, U> T accept(LongExprVisitor<T, U> visitor, U arg) {
    return visitor.visitDecrementMetric(this, arg);
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
    long newVal = Math.max(oldMetric - _subtrahend, 0);
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
