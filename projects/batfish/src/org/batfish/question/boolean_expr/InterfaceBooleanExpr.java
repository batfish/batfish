package org.batfish.question.boolean_expr;

import org.batfish.common.BatfishException;
import org.batfish.question.Environment;
import org.batfish.representation.Configuration;
import org.batfish.representation.Interface;
import org.batfish.representation.IsisInterfaceMode;

public enum InterfaceBooleanExpr implements BooleanExpr {
   INTERFACE_ENABLED,
   INTERFACE_HAS_IP,
   INTERFACE_IS_LOOPBACK,
   INTERFACE_ISIS_L1_ACTIVE,
   INTERFACE_ISIS_L1_PASSIVE,
   INTERFACE_ISIS_L2_ACTIVE,
   INTERFACE_ISIS_L2_PASSIVE,
   INTERFACE_OSPF_ACTIVE,
   INTERFACE_OSPF_PASSIVE;

   @Override
   public Boolean evaluate(Environment environment) {
      Configuration node = environment.getNode();
      Interface iface = environment.getInterface();
      switch (this) {

      case INTERFACE_ENABLED:
         return iface.getActive();

      case INTERFACE_HAS_IP:
         return iface.getPrefix() != null;

      case INTERFACE_ISIS_L1_ACTIVE:
         return iface.getIsisL1InterfaceMode() == IsisInterfaceMode.ACTIVE;

      case INTERFACE_ISIS_L1_PASSIVE:
         return iface.getIsisL1InterfaceMode() == IsisInterfaceMode.PASSIVE;

      case INTERFACE_ISIS_L2_ACTIVE:
         return iface.getIsisL2InterfaceMode() == IsisInterfaceMode.ACTIVE;

      case INTERFACE_ISIS_L2_PASSIVE:
         return iface.getIsisL2InterfaceMode() == IsisInterfaceMode.PASSIVE;

      case INTERFACE_IS_LOOPBACK:
         return iface.isLoopback(node.getVendor());

      case INTERFACE_OSPF_ACTIVE:
         return iface.getOspfEnabled() && !iface.getOspfPassive();

      case INTERFACE_OSPF_PASSIVE:
         return iface.getOspfEnabled() && iface.getOspfPassive();

      default:
         throw new BatfishException("Invalid interface property expression");
      }
   }

   @Override
   public String print(Environment environment) {
      return BaseBooleanExpr.print(this, environment);
   }

}
