package org.batfish.representation.cisco;

import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.AsExpr;
import org.batfish.datamodel.routing_policy.expr.IntExpr;
import org.batfish.datamodel.routing_policy.expr.MultipliedAs;
import org.batfish.datamodel.routing_policy.statement.PrependAsPath;
import org.batfish.datamodel.routing_policy.statement.Statement;

public class RoutePolicyPrependAsPath extends RoutePolicyStatement {

  /** */
  private static final long serialVersionUID = 1L;

  private AsExpr _expr;

  private IntExpr _number;

  public RoutePolicyPrependAsPath(AsExpr expr, IntExpr number) {
    _expr = expr;
    _number = number;
  }

  @Override
  public void applyTo(
      List<Statement> statements, CiscoConfiguration cc, Configuration c, Warnings w) {
    statements.add(new PrependAsPath(new MultipliedAs(_expr, _number)));
  }
}
