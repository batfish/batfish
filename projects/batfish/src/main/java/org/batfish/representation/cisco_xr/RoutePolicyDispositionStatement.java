package org.batfish.representation.cisco_xr;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.batfish.common.BatfishException;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.datamodel.routing_policy.statement.Statements;

public class RoutePolicyDispositionStatement extends RoutePolicyStatement {

  private RoutePolicyDispositionType _dispositionType;

  public RoutePolicyDispositionStatement(RoutePolicyDispositionType dType) {
    _dispositionType = dType;
  }

  @Override
  public void applyTo(
      List<Statement> statements, CiscoXrConfiguration cc, Configuration c, Warnings w) {
    switch (_dispositionType) {
      case DONE:
        {
          If ifStatement =
              new If(
                  BooleanExprs.CALL_EXPR_CONTEXT,
                  ImmutableList.of(Statements.ReturnTrue.toStaticStatement()),
                  ImmutableList.of(Statements.ExitAccept.toStaticStatement()));
          statements.add(ifStatement);
          break;
        }

      case DROP:
        {
          If ifStatement =
              new If(
                  BooleanExprs.CALL_EXPR_CONTEXT,
                  ImmutableList.of(Statements.ReturnFalse.toStaticStatement()),
                  ImmutableList.of(Statements.ExitReject.toStaticStatement()));
          statements.add(ifStatement);
          break;
        }

      case PASS:
        {
          If ifStatement =
              new If(
                  BooleanExprs.CALL_EXPR_CONTEXT,
                  ImmutableList.of(Statements.SetLocalDefaultActionAccept.toStaticStatement()),
                  ImmutableList.of(Statements.SetDefaultActionAccept.toStaticStatement()));
          statements.add(ifStatement);
          break;
        }

      case UNSUPPRESS_ROUTE:
        statements.add(Statements.Unsuppress.toStaticStatement());
        break;

      default:
        throw new BatfishException("Invalid disposition type");
    }
  }

  public RoutePolicyDispositionType getDispositionType() {
    return _dispositionType;
  }
}
