package batfish.grammar.juniper.bgp;

import batfish.grammar.juniper.StanzaStatusType;

public class BGGRN_NullStanza extends BGGR_NStanza {
   
   /* ------------------------------ Constructor ----------------------------*/
   public BGGRN_NullStanza (String ign) {
      this.set_stanzaStatus(StanzaStatusType.IGNORED);
      this.addIgnoredStatement(ign);
   }
   /* ----------------------------- Other Methods ---------------------------*/
   
   /* ---------------------------- Getters/Setters --------------------------*/
   
   /* --------------------------- Inherited Methods -------------------------*/   

   @Override
   public BGGR_NType getType() {
      return BGGR_NType.NULL;
   }

}