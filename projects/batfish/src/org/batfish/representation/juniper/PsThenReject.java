package org.batfish.representation.juniper;

import java.util.Collections;
import java.util.List;

import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.datamodel.routing_policy.statement.Statements;
import org.batfish.main.Warnings;

public final class PsThenReject extends PsThen {

   public static final PsThenReject INSTANCE = new PsThenReject();

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private PsThenReject() {
   }

   @Override
   public void applyTo(List<Statement> statements,
         JuniperConfiguration juniperVendorConfiguration, Configuration c,
         Warnings warnings) {
      If ifStatement = new If();
      ifStatement.setGuard(BooleanExprs.CallExprContext.toStaticBooleanExpr());
      ifStatement.setTrueStatements(Collections
            .singletonList(Statements.ReturnFalse.toStaticStatement()));
      ifStatement.setFalseStatements(Collections
            .singletonList(Statements.ExitReject.toStaticStatement()));
      statements.add(ifStatement);
   }

}
