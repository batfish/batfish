package org.batfish.representation.cisco_xr;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.datamodel.routing_policy.statement.Statements;

public final class RoutePolicyDispositionStatement extends RoutePolicyStatement {

  private final RoutePolicyDispositionType _dispositionType;

  public RoutePolicyDispositionStatement(RoutePolicyDispositionType dType) {
    _dispositionType = dType;
  }

  @Override
  public void applyTo(
      List<Statement> statements, CiscoXrConfiguration cc, Configuration c, Warnings w) {
    Statement statement =
        switch (_dispositionType) {
          case DONE ->
              new If(
                  BooleanExprs.CALL_EXPR_CONTEXT,
                  ImmutableList.of(Statements.ReturnTrue.toStaticStatement()),
                  ImmutableList.of(Statements.ExitAccept.toStaticStatement()));
          case DROP ->
              new If(
                  BooleanExprs.CALL_EXPR_CONTEXT,
                  ImmutableList.of(Statements.ReturnFalse.toStaticStatement()),
                  ImmutableList.of(Statements.ExitReject.toStaticStatement()));
          case PASS ->
              new If(
                  BooleanExprs.CALL_EXPR_CONTEXT,
                  ImmutableList.of(Statements.SetLocalDefaultActionAccept.toStaticStatement()),
                  ImmutableList.of(Statements.SetDefaultActionAccept.toStaticStatement()));
          case UNSUPPRESS_ROUTE -> Statements.Unsuppress.toStaticStatement();
        };
    statements.add(statement);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof RoutePolicyDispositionStatement)) {
      return false;
    }
    RoutePolicyDispositionStatement that = (RoutePolicyDispositionStatement) o;
    return _dispositionType == that._dispositionType;
  }

  @Override
  public int hashCode() {
    return _dispositionType.ordinal();
  }

  public RoutePolicyDispositionType getDispositionType() {
    return _dispositionType;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("dispositionType", _dispositionType).toString();
  }
}
