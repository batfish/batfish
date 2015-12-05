package org.batfish.question.statement;

import java.util.List;

import org.batfish.common.BatfishLogger;
import org.batfish.main.Settings;
import org.batfish.question.Environment;
import org.batfish.question.boolean_expr.BooleanExpr;

public class UnlessStatement implements Statement {

   private final BooleanExpr _guard;

   private final List<Statement> _statements;

   public UnlessStatement(BooleanExpr guard, List<Statement> statements) {
      _guard = guard;
      _statements = statements;
   }

   @Override
   public void execute(Environment environment, BatfishLogger logger,
         Settings settings) {
      boolean guard = _guard.evaluate(environment);
      if (!guard) {
         for (Statement statement : _statements) {
            statement.execute(environment, logger, settings);
         }
      }
   }

}
