package batfish.grammar.cisco.bgp;

import batfish.representation.cisco.BgpAddressFamily;

public class NeighborPeerGroupAssignmentAFStanza implements AFStanza {
   private String _address;
   private String _groupName;

   public NeighborPeerGroupAssignmentAFStanza(String groupName, String address) {
      _groupName = groupName;
      _address = address;
   }

   @Override
   public void process(BgpAddressFamily af) {
      af.getPeerGroupMembership().put(_address, _groupName);
   }

}
