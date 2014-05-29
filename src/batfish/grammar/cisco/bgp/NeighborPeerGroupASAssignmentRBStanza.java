package batfish.grammar.cisco.bgp;

import batfish.representation.cisco.BgpPeerGroup;
import batfish.representation.cisco.BgpProcess;

public class NeighborPeerGroupASAssignmentRBStanza implements RBStanza {

   private int _asNum;
   private String _groupName;
   private boolean _ip;

   public NeighborPeerGroupASAssignmentRBStanza(String groupName, int asNum,
         boolean ip) {
      _groupName = groupName;
      _asNum = asNum;
      _ip = ip;
   }

   @Override
   public void process(BgpProcess p) {
      BgpPeerGroup pg = p.getPeerGroup(_groupName);
      if (pg == null) {
         BgpPeerGroup newGroup = new BgpPeerGroup(_groupName);
         newGroup.setRemoteAS(_asNum);
         if (_ip) {
            newGroup.addNeighborAddress(_groupName);
            if (p.getDefaultNeighborActivate()) {
               p.addActivatedNeighbor(_groupName);
            }
         }
         p.addPeerGroup(newGroup);
      }
      else {
         pg.setRemoteAS(_asNum);
      }
   }

}
