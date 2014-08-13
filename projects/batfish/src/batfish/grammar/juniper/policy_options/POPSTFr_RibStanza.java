package batfish.grammar.juniper.policy_options;

import batfish.grammar.juniper.StanzaStatusType;

public class POPSTFr_RibStanza extends POPST_FromStanza {
   
	private String _ribName;
   
   /* ------------------------------ Constructor ----------------------------*/
   public POPSTFr_RibStanza(String s) {
      _ribName = s; 
      if (_ribName.equalsIgnoreCase("inet6.0")) {
         set_stanzaStatus(StanzaStatusType.IPV6);
      }
      else if (_ribName.equalsIgnoreCase("inet.3") || _ribName.equalsIgnoreCase("mpls.0")) {
         set_stanzaStatus(StanzaStatusType.IGNORED);
      }
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   
   /* ---------------------------- Getters/Setters --------------------------*/
   public String get_ribName () {
      return _ribName;
   }
   /* --------------------------- Inherited Methods -------------------------*/
	@Override
	public POPST_FromType getType() {
		return POPST_FromType.RIB;
	}

}
