package org.batfish.representation.cisco_xr;

import static com.google.common.base.MoreObjects.firstNonNull;

import java.util.List;
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

public class RoutePolicyPrependAsPath extends RoutePolicyStatement {

  @Nonnull private AsExpr _expr;
  @Nullable private IntExpr _number;

  public RoutePolicyPrependAsPath(@Nonnull AsExpr expr, @Nullable IntExpr number) {
    _expr = expr;
    _number = number;
  }

  @Override
  public void applyTo(
      List<Statement> statements, CiscoXrConfiguration cc, Configuration c, Warnings w) {
    statements.add(
        // prepend once by default, unless number modifier is present. TODO: verify this is correct
        new PrependAsPath(new MultipliedAs(_expr, firstNonNull(_number, new LiteralInt(1)))));
  }
}
