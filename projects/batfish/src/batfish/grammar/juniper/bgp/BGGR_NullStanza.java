package batfish.grammar.juniper.bgp;

import batfish.grammar.juniper.StanzaStatusType;

public class BGGR_NullStanza extends BG_GRStanza {
   
   /* ------------------------------ Constructor ----------------------------*/
   public BGGR_NullStanza (String ign) {
      this.set_stanzaStatus(StanzaStatusType.IGNORED);
      this.addIgnoredStatement(ign);
   }
   /* ----------------------------- Other Methods ---------------------------*/
   
   /* ---------------------------- Getters/Setters --------------------------*/
   
   /* --------------------------- Inherited Methods -------------------------*/  
   @Override
   public BG_GRType getType() {
      return BG_GRType.NULL;
   }

}