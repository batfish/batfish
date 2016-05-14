package org.batfish.question.ipsec_vpn_expr;

import org.batfish.datamodel.IpsecVpn;
import org.batfish.question.Environment;

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
