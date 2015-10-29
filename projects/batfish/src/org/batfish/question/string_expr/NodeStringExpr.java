package org.batfish.question.string_expr;

import org.batfish.common.BatfishException;
import org.batfish.question.Environment;
import org.batfish.representation.Configuration;

public enum NodeStringExpr implements StringExpr {
   NODE_NAME;

   @Override
   public String evaluate(Environment environment) {
      Configuration node = environment.getNode();
      switch (this) {

      case NODE_NAME:
         return node.getHostname();

      default:
         throw new BatfishException("invalid node string expr");
      }
   }

   @Override
   public String print(Environment environment) {
      return BaseStringExpr.print(this, environment);
   }

}
