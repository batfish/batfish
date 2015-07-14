package org.batfish.question;

import org.batfish.main.BatfishException;
import org.batfish.representation.StaticRoute;

public enum StaticRouteBooleanExpr implements BooleanExpr {
   STATICROUTE_HAS_NEXT_HOP_INTERFACE,
   STATICROUTE_HAS_NEXT_HOP_IP;

   @Override
   public boolean evaluate(Environment environment) {
      StaticRoute staticRoute = environment.getStaticRoute();
      switch (this) {

      case STATICROUTE_HAS_NEXT_HOP_INTERFACE:
         return staticRoute.getNextHopInterface() != null;

      case STATICROUTE_HAS_NEXT_HOP_IP:
         return staticRoute.getNextHopIp() != null;

      default:
         throw new BatfishException("invalid staticroute boolean expr");
      }
   }

   @Override
   public String print(Environment environment) {
      return Boolean.toString(evaluate(environment));
   }

}
