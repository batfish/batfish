package org.batfish.question.string_expr.ipsec_vpn;

import org.batfish.datamodel.IpsecVpn;
import org.batfish.question.Environment;
import org.batfish.question.ipsec_vpn_expr.IpsecVpnExpr;

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
