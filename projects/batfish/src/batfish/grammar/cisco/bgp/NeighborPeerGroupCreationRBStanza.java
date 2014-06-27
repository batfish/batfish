package batfish.grammar.cisco.bgp;

//import batfish.representation.cisco.BgpPeerGroup;
import batfish.representation.cisco.BgpProcess;

public class NeighborPeerGroupCreationRBStanza implements RBStanza {
//   private String _groupName;

   public NeighborPeerGroupCreationRBStanza(String groupName) {
//      _groupName = groupName;
   }

   @Override
   public void process(BgpProcess p) {
//      BgpPeerGroup newPeerGroup = new BgpPeerGroup(_groupName);
//      newPeerGroup.setClusterId(p.getClusterId());
//      p.addPeerGroup(newPeerGroup);
   }

}
