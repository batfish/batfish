package org.batfish.question;

import java.util.Map;
import java.util.Map.Entry;

import org.batfish.main.BatfishException;
import org.batfish.main.BatfishLogger;
import org.batfish.main.Settings;

public class Assertion implements Statement {

   private final String _assertionText;

   private final BooleanExpr _expr;

   private final Map<String, PrintableExpr> _onErrorPrintables;

   public Assertion(BooleanExpr expr, String assertionText,
         Map<String, PrintableExpr> onErrorPrintables) {
      _expr = expr;
      _assertionText = assertionText;
      _onErrorPrintables = onErrorPrintables;
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
         environment.setUnsafe(true);
         String optionalMessage = "";
         if (_onErrorPrintables.size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (Entry<String, PrintableExpr> e : _onErrorPrintables.entrySet()) {
               String name = e.getKey();
               String value = e.getValue().print(environment);
               sb.append(name + ":" + value + " ");
            }
            optionalMessage = ", special values: { " + sb.toString() + "}";
         }
         String errorMessage = "Assertion: " + _assertionText
               + " failed with context: " + environment.toString()
               + optionalMessage + "\n";
         if (settings.getExitOnFirstError()) {
            throw new BatfishException(errorMessage);
         }
         else {
            logger.error(errorMessage);
         }
      }
   }

}
