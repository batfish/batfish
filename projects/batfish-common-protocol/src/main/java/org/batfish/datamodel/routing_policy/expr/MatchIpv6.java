package org.batfish.datamodel.routing_policy.expr;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

/** Boolean expression that evaluates to true if the given {@link Environment} has an IPv6 route. */
public final class MatchIpv6 extends BooleanExpr {

  private static final MatchIpv6 INSTANCE = new MatchIpv6();

  private MatchIpv6() {}

  @JsonCreator
  public static MatchIpv6 instance() {
    return INSTANCE;
  }

  @Override
  public <T, U> T accept(BooleanExprVisitor<T, U> visitor, U arg) {
    return visitor.visitMatchIpv6(this, arg);
  }

  @Override
  public Result evaluate(Environment environment) {
    return new Result(environment.getOriginalRoute6() != null);
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
