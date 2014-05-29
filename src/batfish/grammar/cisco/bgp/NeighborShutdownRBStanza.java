package batfish.grammar.cisco.bgp;

import batfish.representation.cisco.BgpProcess;

public class NeighborShutdownRBStanza implements RBStanza {
   private String _address;
   
   public NeighborShutdownRBStanza(String address) {
      _address = address;
   }

   public String getAddress() {
      return _address;
   } 

   @Override
   public void process(BgpProcess p) {
      p.getActivatedNeighbors().remove(_address);
      p.addShutDownNeighbor(_address);
      
   }

}
