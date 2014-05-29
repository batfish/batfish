package batfish.representation.cisco;

public class RouteMapMatchNeighborLine extends RouteMapMatchLine {

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
