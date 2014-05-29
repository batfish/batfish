package batfish.grammar.cisco.bgp;

import batfish.representation.cisco.BgpAddressFamily;

public class NeighborDefaultOriginateAFStanza implements AFStanza {

   private String _mapName;
   private String _neighbor;

   public NeighborDefaultOriginateAFStanza(String neighbor, String mapName) {
      _neighbor = neighbor;
      _mapName = mapName;
   }

   @Override
   public void process(BgpAddressFamily af) {
      af.getDefaultOriginateNeighbors().put(_neighbor, _mapName);
   }

}
