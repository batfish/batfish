package batfish.grammar.juniper.bgp;

import batfish.representation.juniper.BGPPeerAS;

public class BGGRN_PeerAsStanza extends BGGR_NStanza {
   
   private BGPPeerAS _peerAs;
   
   /* ------------------------------ Constructor ----------------------------*/
   public BGGRN_PeerAsStanza(BGPPeerAS p) {
      _peerAs = p;
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   public int GetASNum() {
      return _peerAs.get_peerASNum();
   }

   /* ---------------------------- Getters/Setters --------------------------*/
   
   /* --------------------------- Inherited Methods -------------------------*/   
   @Override
   public BGGR_NType getType() {
      return BGGR_NType.PEER_AS;
   }

}
