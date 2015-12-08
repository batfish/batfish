package org.batfish.question.ip_expr.iface;

import org.batfish.question.interface_expr.InterfaceExpr;
import org.batfish.question.ip_expr.BaseIpExpr;

public abstract class InterfaceIpExpr extends BaseIpExpr {

   protected final InterfaceExpr _caller;

   public InterfaceIpExpr(InterfaceExpr caller) {
      _caller = caller;
   }

}
