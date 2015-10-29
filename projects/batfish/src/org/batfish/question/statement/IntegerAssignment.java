package org.batfish.question.statement;

import org.batfish.common.BatfishLogger;
import org.batfish.main.Settings;
import org.batfish.question.Environment;
import org.batfish.question.int_expr.IntExpr;

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
