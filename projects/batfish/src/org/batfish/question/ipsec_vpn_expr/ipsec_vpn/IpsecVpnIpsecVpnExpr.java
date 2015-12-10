package org.batfish.question.ipsec_vpn_expr.ipsec_vpn;

import org.batfish.question.ipsec_vpn_expr.BaseIpsecVpnExpr;
import org.batfish.question.ipsec_vpn_expr.IpsecVpnExpr;

public abstract class IpsecVpnIpsecVpnExpr extends BaseIpsecVpnExpr {

   protected final IpsecVpnExpr _caller;

   public IpsecVpnIpsecVpnExpr(IpsecVpnExpr caller) {
      _caller = caller;
   }

}
