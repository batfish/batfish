package org.batfish.question;

import org.batfish.common.BatfishException;
import org.batfish.representation.Ip;
import org.batfish.representation.StaticRoute;

public enum StaticRouteIpExpr implements IpExpr {
   STATICROUTE_NEXT_HOP_IP;

   @Override
   public Ip evaluate(Environment environment) {
      StaticRoute staticRoute = environment.getStaticRoute();
      switch (this) {

      case STATICROUTE_NEXT_HOP_IP:
         return staticRoute.getNextHopIp();

      default:
         throw new BatfishException("invalid staticroute ip expr");
      }
   }

   @Override
   public String print(Environment environment) {
      return evaluate(environment).toString();
   }

}
