package org.batfish.question.string_expr;

import org.batfish.question.Environment;
import org.batfish.question.ipsec_vpn_expr.IpsecVpnExpr;
import org.batfish.representation.IpsecVpn;

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
