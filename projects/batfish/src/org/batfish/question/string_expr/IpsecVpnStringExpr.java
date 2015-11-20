package org.batfish.question.string_expr;

import org.batfish.question.ipsec_vpn_expr.IpsecVpnExpr;

public abstract class IpsecVpnStringExpr extends BaseStringExpr {

   protected final IpsecVpnExpr _caller;

   public IpsecVpnStringExpr(IpsecVpnExpr caller) {
      _caller = caller;
   }

}
