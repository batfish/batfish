package org.batfish.question;

import org.batfish.main.BatfishLogger;
import org.batfish.main.Settings;

public class IntegerAssignment implements Statement {

   private final IntExpr _expr;

   private final String _variable;

   public IntegerAssignment(String variable, IntExpr expr) {
      _variable = variable;
      _expr = expr;
   }

   @Override
   public void execute(Environment environment, BatfishLogger logger,
         Settings settings) {
      int value = _expr.evaluate(environment);
      environment.getIntegers().put(_variable, value);
   }

}
