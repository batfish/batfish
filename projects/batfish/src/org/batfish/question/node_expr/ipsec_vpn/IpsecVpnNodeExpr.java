package org.batfish.question.node_expr.ipsec_vpn;

import org.batfish.question.ipsec_vpn_expr.IpsecVpnExpr;
import org.batfish.question.node_expr.BaseNodeExpr;

public abstract class IpsecVpnNodeExpr extends BaseNodeExpr {

   protected final IpsecVpnExpr _caller;

   public IpsecVpnNodeExpr(IpsecVpnExpr caller) {
      _caller = caller;
   }

}
