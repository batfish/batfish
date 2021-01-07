package org.batfish.datamodel.routing_policy.statement;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.BgpRoute;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.route.nh.NextHopDiscard;
import org.batfish.datamodel.route.nh.NextHopIp;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;
import org.batfish.datamodel.routing_policy.expr.NextHopExpr;

/** Changes the next hop attribute for a BGP route */
@ParametersAreNonnullByDefault
public final class SetNextHop extends Statement {
  private static final String PROP_EXPR = "expr";

  @Nonnull private NextHopExpr _expr;

  @JsonCreator
  private static SetNextHop jsonCreator(@Nullable @JsonProperty(PROP_EXPR) NextHopExpr expr) {
    checkArgument(expr != null, "%s must be provided", PROP_EXPR);
    return new SetNextHop(expr);
  }

  public SetNextHop(NextHopExpr expr) {
    _expr = expr;
  }

  @Override
  public <T, U> T accept(StatementVisitor<T, U> visitor, U arg) {
    return visitor.visitSetNextHop(this, arg);
  }

  @Override
  public Result execute(Environment environment) {
    Result result = new Result();
    // Do nothing for a route that is not BGP.
    if (!(environment.getOutputRoute() instanceof BgpRoute.Builder<?, ?>)) {
      return result;
    }

    // Handle "discard" next hop, where the route acts as a null route.
    if (_expr.getDiscard()) {
      environment.getOutputRoute().setNextHop(NextHopDiscard.instance());
    }

    // Evaluate our next hop expression. If the result is non-null, modify the next hop IP.
    Ip nextHop = _expr.getNextHopIp(environment);
    if (nextHop == null) {
      return result;
    }
    environment.getOutputRoute().setNextHop(NextHopIp.of(nextHop));
    return result;
  }

  @JsonProperty(PROP_EXPR)
  @Nonnull
  public NextHopExpr getExpr() {
    return _expr;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof SetNextHop)) {
      return false;
    }
    SetNextHop that = (SetNextHop) o;
    return _expr.equals(that._expr);
  }

  @Override
  public int hashCode() {
    return _expr.hashCode();
  }
}
