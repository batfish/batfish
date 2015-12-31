package org.batfish.question.prefix_set_expr.iface;

import org.batfish.question.interface_expr.InterfaceExpr;
import org.batfish.question.prefix_set_expr.BasePrefixSetExpr;

public abstract class InterfacePrefixSetExpr extends BasePrefixSetExpr {

   protected final InterfaceExpr _caller;

   public InterfacePrefixSetExpr(InterfaceExpr caller) {
      _caller = caller;
   }

}
