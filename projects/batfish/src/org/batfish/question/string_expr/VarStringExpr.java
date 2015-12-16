package org.batfish.question.string_expr;

import org.batfish.question.Environment;

public final class VarStringExpr extends BaseStringExpr {

   private final String _var;

   public VarStringExpr(String var) {
      _var = var;
   }

   @Override
   public String evaluate(Environment environment) {
      return environment.getStrings().get(_var);
   }

}
