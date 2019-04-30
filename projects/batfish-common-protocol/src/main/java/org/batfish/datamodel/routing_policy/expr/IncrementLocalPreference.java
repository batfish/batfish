package org.batfish.datamodel.routing_policy.expr;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.batfish.datamodel.BgpRoute;
import org.batfish.datamodel.routing_policy.Environment;

public class IncrementLocalPreference extends LongExpr {

  private static final long serialVersionUID = 1L;

  private long _addend;

  @JsonCreator
  private IncrementLocalPreference() {}

  public IncrementLocalPreference(int addend) {
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
    IncrementLocalPreference other = (IncrementLocalPreference) obj;
    if (_addend != other._addend) {
      return false;
    }
    return true;
  }

  @Override
  public long evaluate(Environment environment) {
    BgpRoute oldRoute = (BgpRoute) environment.getOriginalRoute();
    long oldLp = oldRoute.getLocalPreference();
    long newVal = oldLp + _addend;
    return newVal;
  }

  public long getAddend() {
    return _addend;
  }

  @Override
  public int hashCode() {
    return Long.hashCode(_addend);
  }

  public void setAddend(int addend) {
    _addend = addend;
  }
}
