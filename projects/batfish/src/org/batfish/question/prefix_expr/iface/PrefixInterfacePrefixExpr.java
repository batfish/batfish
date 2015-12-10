package org.batfish.question.prefix_expr.iface;

import org.batfish.question.Environment;
import org.batfish.question.interface_expr.InterfaceExpr;
import org.batfish.representation.Interface;
import org.batfish.representation.Prefix;

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
