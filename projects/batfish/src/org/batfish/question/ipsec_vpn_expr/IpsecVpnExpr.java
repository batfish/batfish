package org.batfish.question.ipsec_vpn_expr;

import org.batfish.question.Environment;
import org.batfish.question.Expr;
import org.batfish.representation.IpsecVpn;

public interface IpsecVpnExpr extends Expr {

   @Override
   IpsecVpn evaluate(Environment environment);

}
