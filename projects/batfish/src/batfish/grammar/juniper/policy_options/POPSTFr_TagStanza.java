package batfish.grammar.juniper.policy_options;

import batfish.grammar.juniper.StanzaStatusType;

public class POPSTFr_TagStanza extends POPST_FromStanza {
   
	private int _tag;
   
   /* ------------------------------ Constructor ----------------------------*/
   public POPSTFr_TagStanza(int i) {
      _tag = i;
      set_postProcessTitle("Tag " + i);
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   
   /* ---------------------------- Getters/Setters --------------------------*/
   public int get_tag () {
      return _tag;
   }
   /* --------------------------- Inherited Methods -------------------------*/
	@Override
	public POPST_FromType getType() {
		return POPST_FromType.TAG;
	}

}
