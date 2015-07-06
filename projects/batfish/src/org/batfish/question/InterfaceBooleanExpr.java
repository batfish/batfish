package org.batfish.question;

import org.batfish.main.BatfishException;
import org.batfish.representation.Configuration;
import org.batfish.representation.Interface;
import org.batfish.representation.IsisInterfaceMode;

public enum InterfaceBooleanExpr implements BooleanExpr {
   INTERFACE_HAS_IP,
   INTERFACE_IS_LOOPBACK,
   INTERFACE_ISIS_ACTIVE,
   INTERFACE_ISIS_PASSIVE;

   @Override
   public boolean evaluate(Environment environment) {
      Configuration node = environment.getNode();
      Interface iface = environment.getInterface();
      switch (this) {

      case INTERFACE_HAS_IP:
         return iface.getPrefix() != null;

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

   @Override
   public String print(Environment environment) {
      return Boolean.toString(evaluate(environment));
   }

}
