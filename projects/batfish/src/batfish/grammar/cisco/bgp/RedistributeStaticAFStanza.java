package batfish.grammar.cisco.bgp;

import batfish.representation.cisco.BgpAddressFamily;

public class RedistributeStaticAFStanza implements AFStanza {

   private String _routeMapName;

   public RedistributeStaticAFStanza(String routeMapName) {
      _routeMapName = routeMapName;
   }

   @Override
   public void process(BgpAddressFamily af) {
      af.setRedistributeStaticMap(_routeMapName);
      af.setRedistributeStatic(true);
   }

}
