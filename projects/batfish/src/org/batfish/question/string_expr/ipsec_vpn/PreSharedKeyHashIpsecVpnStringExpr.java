package org.batfish.question.string_expr.ipsec_vpn;

import org.batfish.question.Environment;
import org.batfish.question.ipsec_vpn_expr.IpsecVpnExpr;
import org.batfish.representation.IpsecVpn;

public class PreSharedKeyHashIpsecVpnStringExpr extends IpsecVpnStringExpr {

   public PreSharedKeyHashIpsecVpnStringExpr(IpsecVpnExpr caller) {
      super(caller);
   }

   @Override
   public String evaluate(Environment environment) {
      IpsecVpn caller = _caller.evaluate(environment);
      return caller.getGateway().getIkePolicy().getPreSharedKeyHash();
   }

}
