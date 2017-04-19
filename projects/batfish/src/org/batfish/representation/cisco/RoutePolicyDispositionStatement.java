package org.batfish.representation.cisco;

import java.util.Collections;
import java.util.List;

import org.batfish.common.BatfishException;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.datamodel.routing_policy.statement.Statements;
import org.batfish.common.Warnings;

public class RoutePolicyDispositionStatement extends RoutePolicyStatement {

   private static final long serialVersionUID = 1L;

   private RoutePolicyDispositionType _dispositionType;

   public RoutePolicyDispositionStatement(RoutePolicyDispositionType dType) {
      _dispositionType = dType;
   }

   @Override
   public void applyTo(List<Statement> statements, CiscoConfiguration cc,
         Configuration c, Warnings w) {
      switch (_dispositionType) {
      case DONE: {
         If ifStatement = new If();
         ifStatement
               .setGuard(BooleanExprs.CallExprContext.toStaticBooleanExpr());
         ifStatement.setTrueStatements(Collections
               .singletonList(Statements.ReturnFalse.toStaticStatement()));
         ifStatement.setFalseStatements(Collections
               .singletonList(Statements.ExitAccept.toStaticStatement()));
         statements.add(ifStatement);
         break;
      }

      case DROP:
         statements.add(Statements.ExitReject.toStaticStatement());
         break;

      case PASS: {
         If ifStatement = new If();
         ifStatement
               .setGuard(BooleanExprs.CallExprContext.toStaticBooleanExpr());
         ifStatement.setTrueStatements(Collections.singletonList(
               Statements.SetLocalDefaultActionAccept.toStaticStatement()));
         ifStatement.setFalseStatements(Collections.singletonList(
               Statements.SetDefaultActionAccept.toStaticStatement()));
         statements.add(ifStatement);
         break;
      }

      default:
         throw new BatfishException("Invalid disposition type");
      }
   }

   public RoutePolicyDispositionType getDispositionType() {
      return _dispositionType;
   }
}
