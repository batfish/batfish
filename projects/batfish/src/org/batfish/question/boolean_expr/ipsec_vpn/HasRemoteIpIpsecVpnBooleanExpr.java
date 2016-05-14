package org.batfish.question.boolean_expr.ipsec_vpn;

import org.batfish.datamodel.IpsecVpn;
import org.batfish.question.Environment;
import org.batfish.question.ipsec_vpn_expr.IpsecVpnExpr;

public final class HasRemoteIpIpsecVpnBooleanExpr extends IpsecVpnBooleanExpr {

   public HasRemoteIpIpsecVpnBooleanExpr(IpsecVpnExpr caller) {
      super(caller);
   }

   @Override
   public Boolean evaluate(Environment environment) {
      IpsecVpn caller = _caller.evaluate(environment);
      return caller.getGateway().getAddress() != null;
   }

}
