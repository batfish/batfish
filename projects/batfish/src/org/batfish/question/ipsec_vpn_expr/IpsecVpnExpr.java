package org.batfish.question.ipsec_vpn_expr;

import org.batfish.datamodel.IpsecVpn;
import org.batfish.question.Environment;
import org.batfish.question.Expr;

public interface IpsecVpnExpr extends Expr {

   @Override
   IpsecVpn evaluate(Environment environment);

}
