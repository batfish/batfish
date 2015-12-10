package org.batfish.question.boolean_expr.ipsec_vpn;

import org.batfish.question.boolean_expr.BaseBooleanExpr;
import org.batfish.question.ipsec_vpn_expr.IpsecVpnExpr;

public abstract class IpsecVpnBooleanExpr extends BaseBooleanExpr {

   protected final IpsecVpnExpr _caller;

   public IpsecVpnBooleanExpr(IpsecVpnExpr caller) {
      _caller = caller;
   }

}
