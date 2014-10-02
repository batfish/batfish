package batfish.grammar.juniper.routing_options;

import batfish.grammar.juniper.StanzaStatusType;

public class RO_NullStanza extends ROStanza {
   
   /* ------------------------------ Constructor ----------------------------*/
   public RO_NullStanza (String ign) {
      this.set_stanzaStatus(StanzaStatusType.IGNORED);
      this.addIgnoredStatement(ign);
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   
   /* ---------------------------- Getters/Setters --------------------------*/
   
   /* --------------------------- Inherited Methods -------------------------*/ 
	@Override
	public ROType getType() {
		return ROType.NULL;
	}

}
