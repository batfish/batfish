package org.batfish.question.policy_map_clause_expr;

import org.batfish.question.Environment;

public abstract class BasePolicyMapClauseExpr implements PolicyMapClauseExpr {

   public static String print(PolicyMapClauseExpr expr, Environment environment) {
      return expr.evaluate(environment).toString();
   }

   @Override
   public final String print(Environment environment) {
      return print(this, environment);
   }

}
