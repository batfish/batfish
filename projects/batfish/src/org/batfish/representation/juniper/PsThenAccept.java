package org.batfish.representation.juniper;

import java.util.Collections;
import java.util.List;

import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.datamodel.routing_policy.statement.Statements;
import org.batfish.common.Warnings;

public final class PsThenAccept extends PsThen {

   public static final PsThenAccept INSTANCE = new PsThenAccept();

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private PsThenAccept() {
   }

   @Override
   public void applyTo(List<Statement> statements,
         JuniperConfiguration juniperVendorConfiguration, Configuration c,
         Warnings w) {
      If ifStatement = new If();
      ifStatement.setGuard(BooleanExprs.CallExprContext.toStaticBooleanExpr());
      ifStatement.setTrueStatements(Collections
            .singletonList(Statements.ReturnTrue.toStaticStatement()));
      ifStatement.setFalseStatements(Collections
            .singletonList(Statements.ExitAccept.toStaticStatement()));
      statements.add(ifStatement);
   }

}
