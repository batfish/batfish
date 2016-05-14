package org.batfish.question.policy_map_clause_expr;

import org.batfish.datamodel.PolicyMapClause;
import org.batfish.question.Environment;
import org.batfish.question.Expr;

public interface PolicyMapClauseExpr extends Expr {

   @Override
   PolicyMapClause evaluate(Environment environment);

}
