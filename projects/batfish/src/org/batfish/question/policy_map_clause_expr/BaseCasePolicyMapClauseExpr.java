package org.batfish.question.policy_map_clause_expr;

import org.batfish.common.BatfishException;
import org.batfish.question.Environment;
import org.batfish.representation.PolicyMapClause;

public enum BaseCasePolicyMapClauseExpr implements PolicyMapClauseExpr {
   CLAUSE;

   @Override
   public PolicyMapClause evaluate(Environment environment) {
      switch (this) {
      case CLAUSE:
         return environment.getClause();

      default:
         throw new BatfishException("invalid BaseCaseRouteFilterExpr");

      }
   }

   @Override
   public String print(Environment environment) {
      return BasePolicyMapClauseExpr.print(this, environment);
   }

}
