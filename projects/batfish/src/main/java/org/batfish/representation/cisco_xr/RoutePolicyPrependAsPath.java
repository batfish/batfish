package org.batfish.representation.cisco_xr;

import static com.google.common.base.MoreObjects.firstNonNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.AsExpr;
import org.batfish.datamodel.routing_policy.expr.IntExpr;
import org.batfish.datamodel.routing_policy.expr.LiteralInt;
import org.batfish.datamodel.routing_policy.expr.MultipliedAs;
import org.batfish.datamodel.routing_policy.statement.PrependAsPath;
import org.batfish.datamodel.routing_policy.statement.Statement;

public class RoutePolicyPrependAsPath extends RoutePolicySetStatement {

  private @Nonnull AsExpr _expr;
  private @Nullable IntExpr _number;

  public RoutePolicyPrependAsPath(@Nonnull AsExpr expr, @Nullable IntExpr number) {
    _expr = expr;
    _number = number;
  }

  @Override
  public Statement toSetStatement(CiscoXrConfiguration cc, Configuration c, Warnings w) {
    // prepend once by default, unless number modifier is present. TODO: verify this is correct
    return new PrependAsPath(new MultipliedAs(_expr, firstNonNull(_number, new LiteralInt(1))));
  }
}
