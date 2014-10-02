package batfish.grammar.juniper.ospf;

import batfish.grammar.juniper.StanzaStatusType;

public class OP_NullStanza extends OPStanza {
   
   /* ------------------------------ Constructor ----------------------------*/
   public OP_NullStanza (String ign) {
      this.set_stanzaStatus(StanzaStatusType.IGNORED);
      this.addIgnoredStatement(ign);
   }
   /* ----------------------------- Other Methods ---------------------------*/
   
   /* ---------------------------- Getters/Setters --------------------------*/
   
   /* --------------------------- Inherited Methods -------------------------*/   
	@Override
	public OPType getType() {
		return OPType.NULL;
	}

}
