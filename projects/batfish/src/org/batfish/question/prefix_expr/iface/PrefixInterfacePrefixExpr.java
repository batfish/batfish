package org.batfish.question.prefix_expr.iface;

import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Prefix;
import org.batfish.question.Environment;
import org.batfish.question.interface_expr.InterfaceExpr;

public class PrefixInterfacePrefixExpr extends InterfacePrefixExpr {

   public PrefixInterfacePrefixExpr(InterfaceExpr caller) {
      super(caller);
   }

   @Override
   public Prefix evaluate(Environment environment) {
      Interface iface = _caller.evaluate(environment);
      return iface.getPrefix();
   }

}
