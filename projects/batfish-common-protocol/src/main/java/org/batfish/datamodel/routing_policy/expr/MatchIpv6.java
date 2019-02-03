package org.batfish.datamodel.routing_policy.expr;

import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

/** Boolean expression that evaluates to true if the given {@link Environment} has an IPv6 route. */
public final class MatchIpv6 extends BooleanExpr {

  private static final long serialVersionUID = 1L;

  public MatchIpv6() {}

  @Override
  public Result evaluate(Environment environment) {
    boolean match = environment.getOriginalRoute6() != null;
    Result result = new Result();
    result.setBooleanValue(match);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof MatchIpv6;
  }

  @Override
  public int hashCode() {
    return 0;
  }
}
