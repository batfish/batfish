package org.batfish.question.policy_map_expr;

import org.batfish.question.Environment;

public abstract class BasePolicyMapExpr implements PolicyMapExpr {

   public static String print(PolicyMapExpr expr, Environment environment) {
      return expr.evaluate(environment).toString();
   }

   @Override
   public String print(Environment environment) {
      return print(this, environment);
   }

}
