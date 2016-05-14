package org.batfish.question.boolean_expr.iface;

import org.batfish.datamodel.Interface;
import org.batfish.question.Environment;
import org.batfish.question.interface_expr.InterfaceExpr;

public final class HasIpInterfaceBooleanExpr extends InterfaceBooleanExpr {

   public HasIpInterfaceBooleanExpr(InterfaceExpr caller) {
      super(caller);
   }

   @Override
   public Boolean evaluate(Environment environment) {
      Interface iface = _caller.evaluate(environment);
      return iface.getPrefix() != null;
   }

}
