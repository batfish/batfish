package batfish.grammar.cisco.bgp;

import batfish.representation.cisco.BgpAddressFamily;

public class NeighborRouteMapInStanza implements AFStanza {

   private String _peerGroupName;
   private String _routeMapName;

   public NeighborRouteMapInStanza(String peerGroupName, String routeMapName) {
      _peerGroupName = peerGroupName;
      _routeMapName = routeMapName;
   }

   @Override
   public void process(BgpAddressFamily af) {
      af.getInboundRouteMaps().put(_peerGroupName, _routeMapName);
   }
}
