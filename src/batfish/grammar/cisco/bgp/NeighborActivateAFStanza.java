package batfish.grammar.cisco.bgp;

import batfish.representation.cisco.BgpAddressFamily;

public class NeighborActivateAFStanza implements AFStanza {
   private String _address;

   public NeighborActivateAFStanza(String address) {
      _address = address;
   }

   public String getAddress() {
      return _address;
   }

   @Override
   public void process(BgpAddressFamily af) {
      af.getNeighbors().add(_address);
   }

}
