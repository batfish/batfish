package org.batfish.question.statement;

import java.util.List;

import org.batfish.common.BatfishLogger;
import org.batfish.main.Settings;
import org.batfish.question.Environment;
import org.batfish.question.boolean_expr.BooleanExpr;

public class IfStatement implements Statement {

   private final List<Statement> _falseStatements;

   private final BooleanExpr _guard;

   private final List<Statement> _trueStatements;

   public IfStatement(BooleanExpr guard, List<Statement> trueStatements,
         List<Statement> falseStatements) {
      _guard = guard;
      _trueStatements = trueStatements;
      _falseStatements = falseStatements;
   }

   @Override
   public void execute(Environment environment, BatfishLogger logger,
         Settings settings) {
      boolean guard = _guard.evaluate(environment);
      if (guard) {
         for (Statement statement : _trueStatements) {
            statement.execute(environment, logger, settings);
         }
      }
      else {
         for (Statement statement : _falseStatements) {
            statement.execute(environment, logger, settings);
         }
      }
   }

}
