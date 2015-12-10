package org.batfish.question.ip_expr.iface;

import org.batfish.question.Environment;
import org.batfish.question.interface_expr.InterfaceExpr;
import org.batfish.representation.Interface;
import org.batfish.representation.Ip;

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
