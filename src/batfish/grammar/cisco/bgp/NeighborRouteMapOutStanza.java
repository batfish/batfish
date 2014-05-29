package batfish.grammar.cisco.bgp;

import batfish.representation.cisco.BgpAddressFamily;

public class NeighborRouteMapOutStanza implements AFStanza {

   private String _peerGroupName;
   private String _routeMapName;

   public NeighborRouteMapOutStanza(String peerGroupName, String routeMapName) {
      _peerGroupName = peerGroupName;
      _routeMapName = routeMapName;
   }

   @Override
   public void process(BgpAddressFamily af) {
      af.getOutboundRouteMaps().put(_peerGroupName, _routeMapName);
   }
}
