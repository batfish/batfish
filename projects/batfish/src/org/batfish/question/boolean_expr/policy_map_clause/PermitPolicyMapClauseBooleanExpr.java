package org.batfish.question.boolean_expr.policy_map_clause;

import org.batfish.question.Environment;
import org.batfish.question.policy_map_clause_expr.PolicyMapClauseExpr;
import org.batfish.representation.PolicyMapAction;
import org.batfish.representation.PolicyMapClause;

public class PermitPolicyMapClauseBooleanExpr extends
      PolicyMapClauseBooleanExpr {

   private PolicyMapClauseExpr _caller;

   public PermitPolicyMapClauseBooleanExpr(PolicyMapClauseExpr caller) {
      super(caller);
   }

   @Override
   public Boolean evaluate(Environment environment) {
      PolicyMapClause caller = _caller.evaluate(environment);
      return caller.getAction() == PolicyMapAction.PERMIT;
   }

}
