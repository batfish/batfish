package batfish.grammar.juniper.interfaces;

import batfish.grammar.juniper.StanzaStatusType;

public class IFUF_NullStanza extends IFU_FamStanza {
   
   /* ------------------------------ Constructor ----------------------------*/
   public IFUF_NullStanza (String ign) {
      this.set_stanzaStatus(StanzaStatusType.IGNORED);
      this.addIgnoredStatement(ign);
      set_postProcessTitle("[ignored statement]");
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   
   /* ---------------------------- Getters/Setters --------------------------*/
   
   /* --------------------------- Inherited Methods -------------------------*/  
	@Override
	public IFU_FamType getType() {
		return IFU_FamType.NULL;
	}

}
