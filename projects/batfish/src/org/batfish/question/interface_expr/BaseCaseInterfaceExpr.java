package org.batfish.question.interface_expr;

import org.batfish.common.BatfishException;
import org.batfish.datamodel.Interface;
import org.batfish.question.Environment;

public enum BaseCaseInterfaceExpr implements InterfaceExpr {
   INTERFACE;

   @Override
   public Interface evaluate(Environment environment) {
      switch (this) {
      case INTERFACE:
         return environment.getInterface();

      default:
         throw new BatfishException("Invalid "
               + this.getClass().getSimpleName());
      }
   }

   @Override
   public String print(Environment environment) {
      return BaseInterfaceExpr.print(this, environment);
   }

}
