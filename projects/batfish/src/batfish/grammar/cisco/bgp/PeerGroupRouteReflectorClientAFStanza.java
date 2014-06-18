package batfish.grammar.cisco.bgp;

import batfish.representation.cisco.BgpAddressFamily;

public class PeerGroupRouteReflectorClientAFStanza implements AFStanza {

   String _peerGroup;

   public PeerGroupRouteReflectorClientAFStanza(String peerGroup) {
      _peerGroup = peerGroup;
   }

   @Override
   public void process(BgpAddressFamily af) {
      af.getRRCPeerGroups().add(_peerGroup);
   }

}
