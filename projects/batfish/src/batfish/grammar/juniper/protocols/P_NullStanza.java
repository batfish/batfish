package batfish.grammar.juniper.protocols;

import batfish.grammar.juniper.StanzaStatusType;

public class P_NullStanza extends PStanza {
   
   /* ------------------------------ Constructor ----------------------------*/
   public P_NullStanza (String ign) {
      this.set_stanzaStatus(StanzaStatusType.IGNORED);
      this.addIgnoredStatement(ign);
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   
   /* ---------------------------- Getters/Setters --------------------------*/
   
   /* --------------------------- Inherited Methods -------------------------*/  
   @Override
   public PType getType() {
      return PType.NULL;
   }

}
