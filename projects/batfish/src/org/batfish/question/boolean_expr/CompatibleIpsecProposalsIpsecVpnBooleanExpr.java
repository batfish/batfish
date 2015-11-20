package org.batfish.question.boolean_expr;

import org.batfish.question.Environment;
import org.batfish.question.ipsec_vpn_expr.IpsecVpnExpr;
import org.batfish.representation.IpsecVpn;

public class CompatibleIpsecProposalsIpsecVpnBooleanExpr extends
      BaseBooleanExpr {

   private IpsecVpnExpr _caller;

   public CompatibleIpsecProposalsIpsecVpnBooleanExpr(IpsecVpnExpr caller) {
      _caller = caller;
   }

   @Override
   public Boolean evaluate(Environment environment) {
      environment.initRemoteIpsecVpns();
      IpsecVpn caller = _caller.evaluate(environment);
      IpsecVpn remoteIpsecVpn = caller.getRemoteIpsecVpn();
      return caller.compatibleIpsecProposals(remoteIpsecVpn);
   }

}
