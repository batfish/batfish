package org.batfish.datamodel.routing_policy.expr;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.batfish.datamodel.routing_policy.Environment;

public class ExplicitAs extends AsExpr {

  /** */
  private static final long serialVersionUID = 1L;

  private long _as;

  @JsonCreator
  private ExplicitAs() {}

  public ExplicitAs(long as) {
    _as = as;
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
    ExplicitAs other = (ExplicitAs) obj;
    if (_as != other._as) {
      return false;
    }
    return true;
  }

  @Override
  public long evaluate(Environment environment) {
    return _as;
  }

  public long getAs() {
    return _as;
  }

  @Override
  public int hashCode() {
    return Long.hashCode(_as);
  }

  public void setAs(int as) {
    _as = as;
  }
}
