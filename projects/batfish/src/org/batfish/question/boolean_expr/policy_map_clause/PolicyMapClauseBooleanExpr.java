package org.batfish.question.boolean_expr.policy_map_clause;

import org.batfish.question.boolean_expr.BaseBooleanExpr;
import org.batfish.question.policy_map_clause_expr.PolicyMapClauseExpr;

public abstract class PolicyMapClauseBooleanExpr extends BaseBooleanExpr {

   protected final PolicyMapClauseExpr _caller;

   public PolicyMapClauseBooleanExpr(PolicyMapClauseExpr caller) {
      _caller = caller;
   }

}
