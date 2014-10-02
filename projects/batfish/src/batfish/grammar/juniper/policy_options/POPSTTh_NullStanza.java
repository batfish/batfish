package batfish.grammar.juniper.policy_options;

import batfish.grammar.juniper.StanzaStatusType;

public class POPSTTh_NullStanza extends POPST_ThenStanza {
   
   /* ------------------------------ Constructor ----------------------------*/
   public POPSTTh_NullStanza (String ign) {
      this.set_stanzaStatus(StanzaStatusType.IGNORED);
      this.addIgnoredStatement(ign);
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   
   /* ---------------------------- Getters/Setters --------------------------*/
   
   /* --------------------------- Inherited Methods -------------------------*/
	@Override
	public POPST_ThenType getType() {
		return POPST_ThenType.NULL;
	}

}
