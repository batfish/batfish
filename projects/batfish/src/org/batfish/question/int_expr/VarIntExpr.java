package org.batfish.question.int_expr;

import org.batfish.common.BatfishException;
import org.batfish.question.Environment;

public class VarIntExpr extends BaseIntExpr {

   private final String _variable;

   public VarIntExpr(String variable) {
      _variable = variable;
   }

   @Override
   public Integer evaluate(Environment environment) {
      Integer value = environment.getIntegers().get(_variable);
      if (value == null) {
         throw new BatfishException(
               "Reference to undefined integer variable: \"" + _variable + "\"");
      }
      else {
         return value;
      }
   }

}
