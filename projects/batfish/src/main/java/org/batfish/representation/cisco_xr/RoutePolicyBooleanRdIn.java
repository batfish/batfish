package org.batfish.representation.cisco_xr;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs;

/**
 * A {@link RoutePolicyBoolean} that evaluates to true iff the NLRI of the route under evaluation
 * contains a route distinguisher matched by a given {@link RdMatchExpr}.
 */
@ParametersAreNonnullByDefault
public class RoutePolicyBooleanRdIn extends RoutePolicyBoolean {

  public RoutePolicyBooleanRdIn(RdMatchExpr expr) {
    _expr = expr;
  }

  @Override
  public BooleanExpr toBooleanExpr(CiscoXrConfiguration cc, Configuration c, Warnings w) {
    // TODO: implement
    return BooleanExprs.FALSE;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof RoutePolicyBooleanRdIn)) {
      return false;
    }
    RoutePolicyBooleanRdIn that = (RoutePolicyBooleanRdIn) o;
    return _expr.equals(that._expr);
  }

  @Override
  public int hashCode() {
    return _expr.hashCode();
  }

  @Nonnull private final RdMatchExpr _expr;
}
