package org.batfish.question.ipsec_vpn_expr;

import org.batfish.question.Environment;
import org.batfish.representation.IpsecVpn;

public final class VarIpsecVpnExpr extends BaseIpsecVpnExpr {

   private final String _var;

   public VarIpsecVpnExpr(String var) {
      _var = var;
   }

   @Override
   public IpsecVpn evaluate(Environment environment) {
      return environment.getIpsecVpns().get(_var);
   }

}
