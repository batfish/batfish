package batfish.grammar.juniper.ospf;

import batfish.grammar.juniper.StanzaStatusType;

public class OPAR_NullStanza extends OP_ARStanza {

   /* ------------------------------ Constructor ----------------------------*/
   public OPAR_NullStanza (String ign) {
      this.set_stanzaStatus(StanzaStatusType.IGNORED);
      this.addIgnoredStatement(ign);
   }
   /* ----------------------------- Other Methods ---------------------------*/
   
   /* ---------------------------- Getters/Setters --------------------------*/
   
   /* --------------------------- Inherited Methods -------------------------*/  
   
	@Override
	public OP_ARType getType() {
		return OP_ARType.NULL;
	}

}
