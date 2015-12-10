package org.batfish.question.boolean_expr.iface;

import org.batfish.question.boolean_expr.BaseBooleanExpr;
import org.batfish.question.interface_expr.InterfaceExpr;

public abstract class InterfaceBooleanExpr extends BaseBooleanExpr {

   protected final InterfaceExpr _caller;

   public InterfaceBooleanExpr(InterfaceExpr caller) {
      _caller = caller;
   }

}
