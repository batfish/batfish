package org.batfish.question.statement;

import java.util.List;

import org.batfish.common.BatfishException;
import org.batfish.common.BatfishLogger;
import org.batfish.main.Settings;
import org.batfish.question.Environment;
import org.batfish.question.boolean_expr.BooleanExpr;

public class Assertion implements Statement {

   private final String _assertionText;

   private final BooleanExpr _expr;

   private List<Statement> _onErrorStatements;

   public Assertion(BooleanExpr expr, String assertionText,
         List<Statement> onErrorStatements) {
      _expr = expr;
      _assertionText = assertionText;
      _onErrorStatements = onErrorStatements;
   }

   @Override
   public void execute(Environment environment, BatfishLogger logger,
         Settings settings) {
      environment.setAssertions(true);
      boolean pass = _expr.evaluate(environment);
      environment.incrementAssertionCount();
      if (pass) {
         String successMessage = "Assertion: " + _assertionText
               + " succeeded with context: " + environment.toString() + "\n";
         logger.info(successMessage);
      }
      else {
         environment.setUnsafe(true);
         environment.incrementFailedAssertionCount();
         String debugErrorMessage = "Assertion: " + _assertionText
               + " failed with context: " + environment.toString() + "\n";
         if (settings.getExitOnFirstError()) {
            executeOnErrorStatements(environment, logger, settings);
            throw new BatfishException(debugErrorMessage);
         }
         else {
            if (_onErrorStatements.size() == 0) {
               logger.output(debugErrorMessage);
            }
            else {
               logger.debug(debugErrorMessage);
            }
            executeOnErrorStatements(environment, logger, settings);
         }
      }
   }

   private void executeOnErrorStatements(Environment environment,
         BatfishLogger logger, Settings settings) {
      for (Statement statement : _onErrorStatements) {
         statement.execute(environment, logger, settings);
      }
   }

}
