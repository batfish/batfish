package org.batfish.datamodel.routing_policy.statement;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.BgpRoute;
import org.batfish.datamodel.route.nh.NextHop;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;
import org.batfish.datamodel.routing_policy.expr.NextHopExpr;

/** Changes the next hop attribute for a BGP route */
@ParametersAreNonnullByDefault
public final class SetNextHop extends Statement {
  private static final String PROP_EXPR = "expr";

  private @Nonnull NextHopExpr _expr;

  @JsonCreator
  private static SetNextHop jsonCreator(@JsonProperty(PROP_EXPR) @Nullable NextHopExpr expr) {
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
    // Do nothing for a route that is not BGP.
    if (!(environment.getOutputRoute() instanceof BgpRoute.Builder<?, ?>)) {
      return new Result();
    }

    NextHop nextHop = _expr.evaluate(environment);
    if (nextHop != null) {
      environment.getOutputRoute().setNextHop(nextHop);
      if (environment.getWriteToIntermediateBgpAttributes()) {
        environment.getIntermediateBgpAttributes().setNextHop(nextHop);
      }
    }
    return new Result();
  }

  @JsonProperty(PROP_EXPR)
  public @Nonnull NextHopExpr getExpr() {
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
