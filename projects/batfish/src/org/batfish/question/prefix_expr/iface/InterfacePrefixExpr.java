package org.batfish.question.prefix_expr.iface;

import org.batfish.question.interface_expr.InterfaceExpr;
import org.batfish.question.prefix_expr.BasePrefixExpr;

public abstract class InterfacePrefixExpr extends BasePrefixExpr {

   protected final InterfaceExpr _caller;

   public InterfacePrefixExpr(InterfaceExpr caller) {
      _caller = caller;
   }

}
