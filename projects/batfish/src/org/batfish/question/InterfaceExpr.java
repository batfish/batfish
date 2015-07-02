package org.batfish.question;

import org.batfish.main.BatfishException;
import org.batfish.representation.Configuration;
import org.batfish.representation.Interface;
import org.batfish.representation.IsisInterfaceMode;

public enum InterfaceExpr implements BooleanExpr {
   INTERFACE_IS_LOOPBACK,
   INTERFACE_ISIS_ACTIVE,
   INTERFACE_ISIS_PASSIVE;

   @Override
   public boolean evaluate(AssertionCtx context) {
      Configuration node = context.getNode();
      Interface iface = context.getInterface();
      switch (this) {

      case INTERFACE_ISIS_ACTIVE:
         return iface.getIsisInterfaceMode() == IsisInterfaceMode.ACTIVE;

      case INTERFACE_ISIS_PASSIVE:
         return iface.getIsisInterfaceMode() == IsisInterfaceMode.PASSIVE;

      case INTERFACE_IS_LOOPBACK:
         return iface.isLoopback(node.getVendor());

      default:
         throw new BatfishException("Invalid interface property expression");
      }
   }

}
