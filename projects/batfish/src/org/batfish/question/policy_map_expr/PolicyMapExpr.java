package org.batfish.question.policy_map_expr;

import org.batfish.datamodel.PolicyMap;
import org.batfish.question.Environment;
import org.batfish.question.Expr;

public interface PolicyMapExpr extends Expr {

   @Override
   public PolicyMap evaluate(Environment environment);

}
