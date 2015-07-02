package org.batfish.question;

import org.batfish.main.BatfishException;
import org.batfish.main.BatfishLogger;
import org.batfish.main.Settings;

public class Assertion {

   private BooleanExpr _expr;

   public Assertion(BooleanExpr expr) {
      _expr = expr;
   }

   public void check(AssertionCtx context, BatfishLogger logger,
         Settings settings) {
      boolean pass = _expr.evaluate(context);
      if (pass) {
         String successMessage = "Assertion: " + _expr.toString()
               + " succeeded with context: " + context.toString() + "\n";
         logger.info(successMessage);
      }
      else {
         String errorMessage = "Assertion: " + _expr.toString()
               + " failed with context: " + context.toString() + "\n";
         if (settings.getExitOnFirstError()) {
            throw new BatfishException(errorMessage);
         }
         else {
            logger.error(errorMessage);
         }
      }
   }

}
