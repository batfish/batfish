package org.batfish.question.ip_expr.ipsec_vpn;

import org.batfish.question.Environment;
import org.batfish.question.ipsec_vpn_expr.IpsecVpnExpr;
import org.batfish.representation.Ip;
import org.batfish.representation.IpsecVpn;

public final class RemoteIpIpsecVpnIpExpr extends IpsecVpnIpExpr {

   public RemoteIpIpsecVpnIpExpr(IpsecVpnExpr caller) {
      super(caller);
   }

   @Override
   public Ip evaluate(Environment environment) {
      IpsecVpn caller = _caller.evaluate(environment);
      return caller.getGateway().getAddress();
   }

}
