package org.batfish.question.boolean_expr;

import org.batfish.common.BatfishException;
import org.batfish.question.Environment;

public enum BaseCaseBooleanExpr implements BooleanExpr {
   FALSE,
   TRUE;

   @Override
   public Boolean evaluate(Environment environment) {
      switch (this) {
      case FALSE:
         return false;
      case TRUE:
         return true;
      default:
         throw new BatfishException("Invalid StaticBooleanExpr");
      }
   }

   @Override
   public String print(Environment environment) {
      return BaseBooleanExpr.print(this, environment);
   }

}
