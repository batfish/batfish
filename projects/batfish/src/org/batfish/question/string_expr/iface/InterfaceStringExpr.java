package org.batfish.question.string_expr.iface;

import org.batfish.question.interface_expr.InterfaceExpr;
import org.batfish.question.string_expr.BaseStringExpr;

public abstract class InterfaceStringExpr extends BaseStringExpr {

   protected final InterfaceExpr _caller;

   public InterfaceStringExpr(InterfaceExpr caller) {
      _caller = caller;
   }

}
