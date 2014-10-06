package batfish.grammar.juniper.bgp;

import batfish.grammar.juniper.StanzaStatusType;

public class BG_NullStanza extends BGStanza {
   
   /* ------------------------------ Constructor ----------------------------*/
   public BG_NullStanza (String ign) {
      this.set_stanzaStatus(StanzaStatusType.IGNORED);
      this.addIgnoredStatement(ign);
      set_postProcessTitle("[ignored statement]");
   }
   /* ----------------------------- Other Methods ---------------------------*/
   
   /* ---------------------------- Getters/Setters --------------------------*/
   
   /* --------------------------- Inherited Methods -------------------------*/      
   @Override
   public BGType getType() {
      return BGType.NULL;
   }

}