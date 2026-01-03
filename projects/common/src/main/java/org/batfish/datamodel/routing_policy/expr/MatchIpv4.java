package org.batfish.datamodel.routing_policy.expr;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

/** Boolean expression that evaluates to true if the given {@link Environment} has an IPv4 route. */
public final class MatchIpv4 extends BooleanExpr {

  private static final MatchIpv4 INSTANCE = new MatchIpv4();

  private MatchIpv4() {}

  @JsonCreator
  public static MatchIpv4 instance() {
    return INSTANCE;
  }

  @Override
  public <T, U> T accept(BooleanExprVisitor<T, U> visitor, U arg) {
    return visitor.visitMatchIpv4(this, arg);
  }

  @Override
  public Result evaluate(Environment environment) {
    return new Result(environment.getOriginalRoute() != null);
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof MatchIpv4;
  }

  @Override
  public int hashCode() {
    return 0;
  }
}
