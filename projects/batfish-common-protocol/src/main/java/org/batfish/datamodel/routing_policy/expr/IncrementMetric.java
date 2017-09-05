package org.batfish.datamodel.routing_policy.expr;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.batfish.datamodel.routing_policy.Environment;

public class IncrementMetric extends LongExpr {

  /** */
  private static final long serialVersionUID = 1L;

  private long _addend;

  @JsonCreator
  private IncrementMetric() {}

  public IncrementMetric(long addend) {
    _addend = addend;
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
    IncrementMetric other = (IncrementMetric) obj;
    if (_addend != other._addend) {
      return false;
    }
    return true;
  }

  @Override
  public long evaluate(Environment environment) {
    long oldMetric = environment.getOriginalRoute().getMetric();
    long newVal = oldMetric + _addend;
    return newVal;
  }

  public long getAddend() {
    return _addend;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Long.hashCode(_addend);
    return result;
  }

  public void setAddend(int addend) {
    _addend = addend;
  }
}
