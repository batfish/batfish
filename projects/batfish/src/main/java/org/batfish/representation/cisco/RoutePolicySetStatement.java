package org.batfish.representation.cisco;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs;
import org.batfish.datamodel.routing_policy.statement.BufferedStatement;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.datamodel.routing_policy.statement.Statements;

public abstract class RoutePolicySetStatement extends RoutePolicyStatement {

  /** */
  private static final long serialVersionUID = 1L;

  @Override
  public final void applyTo(
      List<Statement> statements, CiscoConfiguration cc, Configuration c, Warnings w) {
    Statement setStatement = toSetStatement(cc, c, w);
    Statement bufferedStatement = new BufferedStatement(setStatement);
    statements.add(bufferedStatement);
    If ifStatement =
        new If(
            BooleanExprs.CALL_EXPR_CONTEXT,
            ImmutableList.of(Statements.SetLocalDefaultActionAccept.toStaticStatement()),
            ImmutableList.of(Statements.SetDefaultActionAccept.toStaticStatement()));
    statements.add(ifStatement);
  }

  protected abstract Statement toSetStatement(CiscoConfiguration cc, Configuration c, Warnings w);
}
