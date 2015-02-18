package org.batfish.representation.cisco;

public class RouteMapMatchNeighborLine extends RouteMapMatchLine {

   private static final long serialVersionUID = 1L;

   private String _neighborIp;

   public RouteMapMatchNeighborLine(String neighborIP) {
      _neighborIp = neighborIP;
   }

   public String getNeighborIp() {
      return _neighborIp;
   }

   @Override
   public RouteMapMatchType getType() {
      return RouteMapMatchType.NEIGHBOR;
   }

}
