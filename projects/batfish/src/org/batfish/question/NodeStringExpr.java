package org.batfish.question;

import org.batfish.common.BatfishException;
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
      return evaluate(environment);
   }

}
