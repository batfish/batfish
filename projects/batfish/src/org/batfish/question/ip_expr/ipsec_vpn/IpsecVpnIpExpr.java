package org.batfish.question.ip_expr.ipsec_vpn;

import org.batfish.question.ip_expr.BaseIpExpr;
import org.batfish.question.ipsec_vpn_expr.IpsecVpnExpr;

public abstract class IpsecVpnIpExpr extends BaseIpExpr {

   protected final IpsecVpnExpr _caller;

   public IpsecVpnIpExpr(IpsecVpnExpr caller) {
      _caller = caller;
   }

}
