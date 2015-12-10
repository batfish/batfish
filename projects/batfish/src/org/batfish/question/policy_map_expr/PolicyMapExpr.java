package org.batfish.question.policy_map_expr;

import org.batfish.question.Environment;
import org.batfish.question.Expr;
import org.batfish.representation.PolicyMap;

public interface PolicyMapExpr extends Expr {

   @Override
   public PolicyMap evaluate(Environment environment);

}
