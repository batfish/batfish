package batfish.grammar.cisco.bgp;

import batfish.representation.cisco.BgpAddressFamily;

public class NeighborPrefixListInAFStanza implements AFStanza {

   private String _neighbor;
   private String _list;

   public NeighborPrefixListInAFStanza(String neighbor, String list) {
      _neighbor = neighbor;
      _list = list;
   }

   @Override
   public void process(BgpAddressFamily af) {
      af.getInboundPrefixLists().put(_neighbor, _list);
   }

}
