package org.batfish.question.boolean_expr.ipsec_vpn;

import org.batfish.datamodel.IpsecVpn;
import org.batfish.question.Environment;
import org.batfish.question.ipsec_vpn_expr.IpsecVpnExpr;

public final class HasSingleRemoteIpsecVpnIpsecVpnBooleanExpr extends
      IpsecVpnBooleanExpr {

   public HasSingleRemoteIpsecVpnIpsecVpnBooleanExpr(IpsecVpnExpr caller) {
      super(caller);
   }

   @Override
   public Boolean evaluate(Environment environment) {
      environment.initRemoteIpsecVpns();
      IpsecVpn caller = _caller.evaluate(environment);
      return caller.getCandidateRemoteIpsecVpns().size() == 1;
   }

}
