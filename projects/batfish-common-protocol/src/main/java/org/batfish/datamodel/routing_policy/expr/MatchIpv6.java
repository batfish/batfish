package org.batfish.datamodel.routing_policy.expr;

import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

public class MatchIpv6 extends BooleanExpr {

  /** */
  private static final long serialVersionUID = 1L;

  public MatchIpv6() {}

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
    return true;
  }

  @Override
  public Result evaluate(Environment environment) {
    boolean match = environment.getOriginalRoute6() != null;
    Result result = new Result();
    result.setBooleanValue(match);
    return result;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + 0x12345678;
    return result;
  }
}
