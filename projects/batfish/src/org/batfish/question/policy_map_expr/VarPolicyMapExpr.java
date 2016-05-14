package org.batfish.question.policy_map_expr;

import org.batfish.datamodel.PolicyMap;
import org.batfish.question.Environment;

public final class VarPolicyMapExpr extends BasePolicyMapExpr {

   private final String _var;

   public VarPolicyMapExpr(String var) {
      _var = var;
   }

   @Override
   public PolicyMap evaluate(Environment environment) {
      return environment.getPolicyMaps().get(_var);
   }

}
