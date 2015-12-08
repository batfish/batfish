package org.batfish.question.boolean_expr.iface;

import org.batfish.question.Environment;
import org.batfish.question.interface_expr.InterfaceExpr;
import org.batfish.representation.Interface;
import org.batfish.representation.IsisInterfaceMode;

public final class IsisL2PassiveInterfaceBooleanExpr extends
      InterfaceBooleanExpr {

   public IsisL2PassiveInterfaceBooleanExpr(InterfaceExpr caller) {
      super(caller);
   }

   @Override
   public Boolean evaluate(Environment environment) {
      Interface iface = _caller.evaluate(environment);
      return iface.getIsisL2InterfaceMode() == IsisInterfaceMode.PASSIVE;
   }

}
