package org.batfish.question;

import org.batfish.common.BatfishException;

public class VarIntExpr extends BaseIntExpr {

   private final String _variable;

   public VarIntExpr(String variable) {
      _variable = variable;
   }

   @Override
   public int evaluate(Environment environment) {
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
