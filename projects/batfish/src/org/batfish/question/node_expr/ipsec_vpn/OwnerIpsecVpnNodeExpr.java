package org.batfish.question.node_expr.ipsec_vpn;

import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.IpsecVpn;
import org.batfish.question.Environment;
import org.batfish.question.ipsec_vpn_expr.IpsecVpnExpr;

public final class OwnerIpsecVpnNodeExpr extends IpsecVpnNodeExpr {

   public OwnerIpsecVpnNodeExpr(IpsecVpnExpr caller) {
      super(caller);
   }

   @Override
   public Configuration evaluate(Environment environment) {
      IpsecVpn caller = _caller.evaluate(environment);
      return caller.getConfiguration();
   }

}
