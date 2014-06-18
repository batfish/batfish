package batfish.grammar.cisco.bgp;

import batfish.representation.cisco.BgpAddressFamily;

public class NeighborSendCommunityAFStanza implements AFStanza {

   private String _pgName;

   public NeighborSendCommunityAFStanza(String pgName) {
      _pgName = pgName;
   }

   @Override
   public void process(BgpAddressFamily af) {
      af.getSCPeerGroups().add(_pgName);
   }

}
