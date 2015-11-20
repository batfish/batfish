package org.batfish.question.ipsec_vpn_expr;

import org.batfish.common.BatfishException;
import org.batfish.question.Environment;
import org.batfish.representation.IpsecVpn;

public enum BaseCaseIpsecVpnExpr implements IpsecVpnExpr {
   IPSEC_VPN,
   REMOTE_IPSEC_VPN;

   @Override
   public IpsecVpn evaluate(Environment environment) {
      IpsecVpn ipsecVpn = environment.getIpsecVpn();
      switch (this) {
      case IPSEC_VPN:
         return ipsecVpn;

      case REMOTE_IPSEC_VPN:
         environment.initRemoteIpsecVpns();
         return ipsecVpn.getRemoteIpsecVpn();

      default:
         throw new BatfishException("invalid BaseCaseIpsecVpnExpr");

      }
   }

   @Override
   public String print(Environment environment) {
      return BaseIpsecVpnExpr.print(this, environment);
   }

}
