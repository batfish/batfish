package org.batfish.question.boolean_expr;

import org.batfish.common.BatfishException;
import org.batfish.question.Environment;

public final class VarBooleanExpr extends BaseBooleanExpr {

   private final String _variable;

   public VarBooleanExpr(String variable) {
      _variable = variable;
   }

   @Override
   public Boolean evaluate(Environment environment) {
      Boolean value = environment.getBooleans().get(_variable);
      if (value == null) {
         throw new BatfishException(
               "Reference to undefined boolean variable: \"" + _variable + "\"");
      }
      else {
         return value;
      }
   }

}
