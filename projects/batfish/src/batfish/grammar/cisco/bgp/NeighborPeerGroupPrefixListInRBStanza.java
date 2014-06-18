package batfish.grammar.cisco.bgp;

import java.util.HashMap;
import java.util.Map;

import batfish.representation.cisco.BgpProcess;

public class NeighborPeerGroupPrefixListInRBStanza implements RBStanza {
   private String _neighbor;
   private String _list;

   public NeighborPeerGroupPrefixListInRBStanza(String neighbor, String list) {
      _neighbor = neighbor;
      _list = list;
   }

   @Override
   public void process(BgpProcess p) {
      Map<String,String> listMap = new HashMap<String, String>();
      
      listMap.put(_neighbor, _list);
      p.addPeerGroupInboundPrefixLists(listMap);

   }

}
