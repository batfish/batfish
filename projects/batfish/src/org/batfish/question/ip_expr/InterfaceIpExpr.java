package org.batfish.question.ip_expr;

import org.batfish.common.BatfishException;
import org.batfish.question.Environment;
//import org.batfish.representation.Configuration;
import org.batfish.representation.Interface;
import org.batfish.representation.Ip;

public enum InterfaceIpExpr implements IpExpr {
   INTERFACE_IP;

   @Override
   public Ip evaluate(Environment environment) {
      // Configuration node = context.getNode();
      Interface iface = environment.getInterface();
      switch (this) {

      case INTERFACE_IP:
         return iface.getPrefix().getAddress();

      default:
         throw new BatfishException("Invalid interface ip expression");
      }
   }

   @Override
   public String print(Environment environment) {
      return BaseIpExpr.print(this, environment);
   }

}
