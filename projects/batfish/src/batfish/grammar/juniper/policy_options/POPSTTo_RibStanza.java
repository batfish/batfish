package batfish.grammar.juniper.policy_options;

import batfish.grammar.juniper.StanzaStatusType;

public class POPSTTo_RibStanza extends POPST_ToStanza {
   
   private String _ribName;
   
   /* ------------------------------ Constructor ----------------------------*/
   public POPSTTo_RibStanza(String s) {
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
   public POPST_ToType getType() {
      return POPST_ToType.RIB;
   }
}
