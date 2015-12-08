package org.batfish.question.node_expr;

import org.batfish.common.BatfishException;
import org.batfish.question.Environment;
import org.batfish.representation.Configuration;

public class VarNodeExpr extends BaseNodeExpr {

   private final String _variable;

   public VarNodeExpr(String variable) {
      _variable = variable;
   }

   @Override
   public Configuration evaluate(Environment environment) {
      Configuration value = environment.getNodes().get(_variable);
      if (value == null) {
         throw new BatfishException("Reference to undefined node variable: \""
               + _variable + "\"");
      }
      else {
         return value;
      }
   }

}
