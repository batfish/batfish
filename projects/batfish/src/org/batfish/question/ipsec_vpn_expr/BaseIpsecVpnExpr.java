package org.batfish.question.ipsec_vpn_expr;

import org.batfish.question.Environment;

public abstract class BaseIpsecVpnExpr implements IpsecVpnExpr {

   public static String print(IpsecVpnExpr expr, Environment environment) {
      return expr.evaluate(environment).toString();
   }

   @Override
   public final String print(Environment environment) {
      return print(this, environment);
   }

}
