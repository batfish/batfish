package org.batfish.question.boolean_expr.ipsec_vpn;

import org.batfish.datamodel.IpsecVpn;
import org.batfish.question.Environment;
import org.batfish.question.ipsec_vpn_expr.IpsecVpnExpr;

public final class CompatibleIpsecProposalsIpsecVpnBooleanExpr extends
      IpsecVpnBooleanExpr {

   public CompatibleIpsecProposalsIpsecVpnBooleanExpr(IpsecVpnExpr caller) {
      super(caller);
   }

   @Override
   public Boolean evaluate(Environment environment) {
      environment.initRemoteIpsecVpns();
      IpsecVpn caller = _caller.evaluate(environment);
      IpsecVpn remoteIpsecVpn = caller.getRemoteIpsecVpn();
      return caller.compatibleIpsecProposals(remoteIpsecVpn);
   }

}
