package org.batfish.question;

import org.batfish.common.BatfishException;
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
      return evaluate(environment);
   }

}
