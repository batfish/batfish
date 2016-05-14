package org.batfish.question.policy_map_clause_expr;

import org.batfish.datamodel.PolicyMapClause;
import org.batfish.question.Environment;

public final class VarPolicyMapClauseExpr extends BasePolicyMapClauseExpr {

   private final String _var;

   public VarPolicyMapClauseExpr(String var) {
      _var = var;
   }

   @Override
   public PolicyMapClause evaluate(Environment environment) {
      return environment.getPolicyMapClauses().get(_var);
   }

}
