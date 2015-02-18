package org.batfish.representation;

import java.io.Serializable;

public abstract class Route implements Serializable {

   private static final long serialVersionUID = 1L;

   protected Ip _nextHopIp;

   protected Prefix _prefix;

   public Route(Prefix prefix, Ip nextHopIp) {
      _prefix = prefix;
      _nextHopIp = nextHopIp;
   }

   @Override
   public abstract boolean equals(Object o);

   public abstract int getAdministrativeCost();

   public Ip getNextHopIp() {
      return _nextHopIp;
   }

   public Prefix getPrefix() {
      return _prefix;
   }

   public abstract RouteType getRouteType();

}
