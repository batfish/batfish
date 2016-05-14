package org.batfish.question.string_expr.ipsec_vpn;

import org.batfish.datamodel.IpsecVpn;
import org.batfish.question.Environment;
import org.batfish.question.ipsec_vpn_expr.IpsecVpnExpr;

public class IpsecPolicyNameIpsecVpnStringExpr extends IpsecVpnStringExpr {

   public IpsecPolicyNameIpsecVpnStringExpr(IpsecVpnExpr caller) {
      super(caller);
   }

   @Override
   public String evaluate(Environment environment) {
      IpsecVpn caller = _caller.evaluate(environment);
      return caller.getIpsecPolicy().getName();
   }

}
