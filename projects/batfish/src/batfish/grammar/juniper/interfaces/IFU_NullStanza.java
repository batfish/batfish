package batfish.grammar.juniper.interfaces;

import batfish.grammar.juniper.StanzaStatusType;

public class IFU_NullStanza extends IF_UStanza {
   
   /* ------------------------------ Constructor ----------------------------*/
   public IFU_NullStanza (String ign) {
      this.set_stanzaStatus(StanzaStatusType.IGNORED);
      this.addIgnoredStatement(ign);
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   
   /* ---------------------------- Getters/Setters --------------------------*/
   
   /* --------------------------- Inherited Methods -------------------------*/ 
	@Override
	public IF_UType getType() {
		return IF_UType.NULL;
	}

}
