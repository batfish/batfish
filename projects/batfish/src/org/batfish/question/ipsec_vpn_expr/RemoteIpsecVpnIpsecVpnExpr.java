package org.batfish.question.ipsec_vpn_expr;

import org.batfish.question.Environment;
import org.batfish.representation.IpsecVpn;

public final class RemoteIpsecVpnIpsecVpnExpr extends BaseIpsecVpnExpr {

   private final IpsecVpnExpr _caller;

   public RemoteIpsecVpnIpsecVpnExpr(IpsecVpnExpr caller) {
      _caller = caller;
   }

   @Override
   public IpsecVpn evaluate(Environment environment) {
      environment.initRemoteIpsecVpns();
      IpsecVpn caller = _caller.evaluate(environment);
      return caller.getRemoteIpsecVpn();
   }

}
