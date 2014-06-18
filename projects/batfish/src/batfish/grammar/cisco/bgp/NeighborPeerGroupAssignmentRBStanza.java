package batfish.grammar.cisco.bgp;

import batfish.representation.cisco.BgpPeerGroup;
import batfish.representation.cisco.BgpProcess;

public class NeighborPeerGroupAssignmentRBStanza implements RBStanza {
   private String _address;
   private String _groupName;

   public NeighborPeerGroupAssignmentRBStanza(String groupName, String address) {
      _groupName = groupName;
      _address = address;
   }

   @Override
   public void process(BgpProcess p) {
      BgpPeerGroup namedPeerGroup = p.getPeerGroup(_groupName);
      if (namedPeerGroup == null) {
         throw new Error("bad peer group name");
      }
      else {
         namedPeerGroup.addNeighborAddress(_address);
         if (p.getDefaultNeighborActivate()) {
            p.addActivatedNeighbor(_address);
         }
         BgpPeerGroup unnamedPeerGroup = p.getPeerGroup(_address);
         if (unnamedPeerGroup == null) {
            unnamedPeerGroup = new BgpPeerGroup(_address);
            unnamedPeerGroup.addNeighborAddress(_address);
            p.addPeerGroup(unnamedPeerGroup);
         }
      }
   }

}
