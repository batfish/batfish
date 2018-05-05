package org.batfish.representation.juniper;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.datamodel.routing_policy.statement.Statements;

public final class PsThenAccept extends PsThen {

  public static final PsThenAccept INSTANCE = new PsThenAccept();

  /** */
  private static final long serialVersionUID = 1L;

  private PsThenAccept() {}

  @Override
  public void applyTo(
      List<Statement> statements,
      JuniperConfiguration juniperVendorConfiguration,
      Configuration c,
      Warnings w) {
    If ifStatement =
        new If(
            BooleanExprs.CallExprContext.toStaticBooleanExpr(),
            ImmutableList.of(Statements.ReturnTrue.toStaticStatement()),
            ImmutableList.of(Statements.ExitAccept.toStaticStatement()));
    statements.add(ifStatement);
  }
}
