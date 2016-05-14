package org.batfish.question.ip_expr.ipsec_vpn;

import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpsecVpn;
import org.batfish.question.Environment;
import org.batfish.question.ipsec_vpn_expr.IpsecVpnExpr;

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
