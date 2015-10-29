package org.batfish.question.string_expr;

import org.batfish.common.BatfishException;
import org.batfish.question.Environment;
import org.batfish.representation.Interface;

public enum InterfaceStringExpr implements StringExpr {
   INTERFACE_NAME;

   @Override
   public String evaluate(Environment environment) {
      Interface iface = environment.getInterface();
      switch (this) {

      case INTERFACE_NAME:
         return iface.getName();

      default:
         throw new BatfishException("invalid interface string expr");
      }
   }

   @Override
   public String print(Environment environment) {
      return BaseStringExpr.print(this, environment);
   }

}
