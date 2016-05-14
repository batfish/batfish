package org.batfish.question.ipsec_vpn_expr;

import org.batfish.common.BatfishException;
import org.batfish.datamodel.IpsecVpn;
import org.batfish.question.Environment;

public enum BaseCaseIpsecVpnExpr implements IpsecVpnExpr {
   IPSEC_VPN,
   REMOTE_IPSEC_VPN;

   @Override
   public IpsecVpn evaluate(Environment environment) {
      switch (this) {
      case IPSEC_VPN:
         IpsecVpn ipsecVpn = environment.getIpsecVpn();
         return ipsecVpn;

      case REMOTE_IPSEC_VPN:
         IpsecVpn remoteIpsecVpn = environment.getRemoteIpsecVpn();
         return remoteIpsecVpn;

      default:
         throw new BatfishException("invalid BaseCaseIpsecVpnExpr");

      }
   }

   @Override
   public String print(Environment environment) {
      return BaseIpsecVpnExpr.print(this, environment);
   }

}
