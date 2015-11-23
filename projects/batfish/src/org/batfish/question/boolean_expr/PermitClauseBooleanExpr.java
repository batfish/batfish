package org.batfish.question.boolean_expr;

import org.batfish.question.Environment;
import org.batfish.question.policy_map_clause_expr.PolicyMapClauseExpr;
import org.batfish.representation.PolicyMapAction;
import org.batfish.representation.PolicyMapClause;

public class PermitClauseBooleanExpr extends BaseBooleanExpr {

   private PolicyMapClauseExpr _caller;

   public PermitClauseBooleanExpr(PolicyMapClauseExpr caller) {
      _caller = caller;
   }

   @Override
   public Boolean evaluate(Environment environment) {
      PolicyMapClause caller = _caller.evaluate(environment);
      return caller.getAction() == PolicyMapAction.PERMIT;
   }

}
