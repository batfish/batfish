package org.batfish.question.node_expr;

import org.batfish.common.BatfishException;
import org.batfish.datamodel.Configuration;
import org.batfish.question.Environment;

public enum BaseCaseNodeExpr implements NodeExpr {
   NODE;

   @Override
   public Configuration evaluate(Environment environment) {
      switch (this) {
      case NODE:
         return environment.getNode();

      default:
         throw new BatfishException("Invalid "
               + this.getClass().getSimpleName());
      }
   }

   @Override
   public String print(Environment environment) {
      return BaseNodeExpr.print(this, environment);
   }

}
