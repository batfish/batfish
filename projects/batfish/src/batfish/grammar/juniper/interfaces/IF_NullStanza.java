package batfish.grammar.juniper.interfaces;

import batfish.grammar.juniper.StanzaStatusType;

public class IF_NullStanza extends IFStanza {
   
   /* ------------------------------ Constructor ----------------------------*/
   public IF_NullStanza (String ign) {
      this.set_stanzaStatus(StanzaStatusType.IGNORED);
      this.addIgnoredStatement(ign);
      set_postProcessTitle("[ignored statement]");
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   
   /* ---------------------------- Getters/Setters --------------------------*/
   
   /* --------------------------- Inherited Methods -------------------------*/     
	@Override
	public IFType getType() {
		return IFType.NULL;
	}

}
