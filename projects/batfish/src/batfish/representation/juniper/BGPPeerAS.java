package batfish.representation.juniper;

import batfish.grammar.juniper.StanzaWithStatus;

public class BGPPeerAS extends StanzaWithStatus {
   
   private int _peerASNum;
   
   /* ------------------------------ Constructor ----------------------------*/
   public BGPPeerAS(int a) {
      _peerASNum = a;
   }
   
   /* ----------------------------- Other Methods ---------------------------*/

   /* ---------------------------- Getters/Setters --------------------------*/
   public int get_peerASNum () {
      return _peerASNum;
   }
   
   /* --------------------------- Inherited Methods -------------------------*/  

}
