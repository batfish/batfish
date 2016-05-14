package org.batfish.question.ip_expr.iface;

import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.question.Environment;
import org.batfish.question.interface_expr.InterfaceExpr;

public final class IpInterfaceIpExpr extends InterfaceIpExpr {

   public IpInterfaceIpExpr(InterfaceExpr caller) {
      super(caller);
   }

   @Override
   public Ip evaluate(Environment environment) {
      Interface iface = _caller.evaluate(environment);
      return iface.getPrefix().getAddress();
   }

}
