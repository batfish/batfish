package org.batfish.question.string_expr.ipsec_vpn;

import org.batfish.question.ipsec_vpn_expr.IpsecVpnExpr;
import org.batfish.question.string_expr.BaseStringExpr;

public abstract class IpsecVpnStringExpr extends BaseStringExpr {

   protected final IpsecVpnExpr _caller;

   public IpsecVpnStringExpr(IpsecVpnExpr caller) {
      _caller = caller;
   }

}
