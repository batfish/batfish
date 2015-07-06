package org.batfish.question;

import org.batfish.main.BatfishException;
import org.batfish.main.BatfishLogger;
import org.batfish.main.Settings;

public class Assertion implements Statement {

   private String _assertionText;

   private BooleanExpr _expr;

   public Assertion(BooleanExpr expr, String assertionText) {
      _expr = expr;
      _assertionText = assertionText;
   }

   @Override
   public void execute(Environment environment, BatfishLogger logger,
         Settings settings) {
      boolean pass = _expr.evaluate(environment);
      if (pass) {
         String successMessage = "Assertion: " + _assertionText
               + " succeeded with context: " + environment.toString() + "\n";
         logger.info(successMessage);
      }
      else {
         String errorMessage = "Assertion: " + _assertionText
               + " failed with context: " + environment.toString() + "\n";
         if (settings.getExitOnFirstError()) {
            throw new BatfishException(errorMessage);
         }
         else {
            logger.error(errorMessage);
         }
      }
   }

}
