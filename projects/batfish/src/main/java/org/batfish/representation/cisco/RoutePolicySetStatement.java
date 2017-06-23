package org.batfish.representation.cisco;

import java.util.Collections;
import java.util.List;

import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs;
import org.batfish.datamodel.routing_policy.statement.BufferedStatement;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.datamodel.routing_policy.statement.Statements;
import org.batfish.common.Warnings;

public abstract class RoutePolicySetStatement extends RoutePolicyStatement {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   @Override
   public final void applyTo(List<Statement> statements, CiscoConfiguration cc,
         Configuration c, Warnings w) {
      Statement setStatement = toSetStatement(cc, c, w);
      Statement bufferedStatement = new BufferedStatement(setStatement);
      If ifStatement = new If();
      ifStatement.setGuard(BooleanExprs.CallExprContext.toStaticBooleanExpr());
      ifStatement.setTrueStatements(Collections.singletonList(
            Statements.SetLocalDefaultActionAccept.toStaticStatement()));
      ifStatement.setFalseStatements(Collections.singletonList(
            Statements.SetDefaultActionAccept.toStaticStatement()));
      statements.add(bufferedStatement);
      statements.add(ifStatement);
   }

   protected abstract Statement toSetStatement(CiscoConfiguration cc,
         Configuration c, Warnings w);

}
