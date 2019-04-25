package org.batfish.datamodel.routing_policy.expr;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.routing_policy.Environment;

public final class DecrementLocalPreference extends LongExpr {

  private static final long serialVersionUID = 1L;

  private long _subtrahend;

  @JsonCreator
  private DecrementLocalPreference() {}

  public DecrementLocalPreference(int subtrahend) {
    _subtrahend = subtrahend;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof DecrementLocalPreference)) {
      return false;
    }
    return _subtrahend == ((DecrementLocalPreference) obj)._subtrahend;
  }

  @Override
  public long evaluate(Environment environment) {
    Bgpv4Route oldRoute = (Bgpv4Route) environment.getOriginalRoute();
    long oldLp = oldRoute.getLocalPreference();
    long newVal = oldLp - _subtrahend;
    return newVal;
  }

  public long getSubtrahend() {
    return _subtrahend;
  }

  @Override
  public int hashCode() {
    return Long.hashCode(_subtrahend);
  }

  public void setSubtrahend(long subtrahend) {
    _subtrahend = subtrahend;
  }
}
