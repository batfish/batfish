package batfish.grammar.juniper.routing_options;

import batfish.grammar.juniper.StanzaStatusType;

public class RO_RibStanza extends ROStanza {
   
   private String _ribName;
   
   /* ------------------------------ Constructor ----------------------------*/
   public RO_RibStanza(String s) {
      _ribName = s; 
      if (_ribName.equalsIgnoreCase("inet6.0")) {
         set_stanzaStatus(StanzaStatusType.IPV6);
      }
      else if (_ribName.equalsIgnoreCase("inet.3") || _ribName.equalsIgnoreCase("mpls.0")) {
         set_stanzaStatus(StanzaStatusType.IGNORED);
      }
      set_postProcessTitle("RIB " + _ribName);
   }
   
   /* ----------------------------- Other Methods ---------------------------*/

   /* ---------------------------- Getters/Setters --------------------------*/

   /* --------------------------- Inherited Methods -------------------------*/
	@Override
	public ROType getType() {
		return ROType.RIB;
	}

}
