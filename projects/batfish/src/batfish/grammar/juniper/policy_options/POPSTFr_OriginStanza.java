package batfish.grammar.juniper.policy_options;

import batfish.grammar.juniper.StanzaStatusType;

public class POPSTFr_OriginStanza extends POPST_FromStanza {
   
	private String _originName;
   
   /* ------------------------------ Constructor ----------------------------*/
   public POPSTFr_OriginStanza(String s) {
      _originName = s; 
      set_postProcessTitle("Origin " + s);
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   
   /* ---------------------------- Getters/Setters --------------------------*/
   public String get_originName () {
      return _originName;
   }
   /* --------------------------- Inherited Methods -------------------------*/
	@Override
	public POPST_FromType getType() {
		return POPST_FromType.ORIGIN;
	}

}
