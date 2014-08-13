package batfish.grammar.juniper.bgp;

import batfish.representation.juniper.BGPPeerAS;

public class BGGR_PeerAsStanza extends BG_GRStanza {
   
   BGPPeerAS _peerAs;
   
   /* ------------------------------ Constructor ----------------------------*/
   public BGGR_PeerAsStanza(BGPPeerAS p) {
      _peerAs = p;
      set_stanzaStatus(p.get_stanzaStatus());
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   public int GetASNum() {
      return _peerAs.get_peerASNum();
   }
   
   /* ---------------------------- Getters/Setters --------------------------*/
   
   /* --------------------------- Inherited Methods -------------------------*/  
   @Override
   public BG_GRType getType() {
      return BG_GRType.PEER_AS;
   }

}
