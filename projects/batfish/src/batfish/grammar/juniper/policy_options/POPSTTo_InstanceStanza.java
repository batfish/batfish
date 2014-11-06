package batfish.grammar.juniper.policy_options;

import batfish.grammar.juniper.StanzaStatusType;

public class POPSTTo_InstanceStanza extends POPST_ToStanza {
   
   private String _iName;
   
   /* ------------------------------ Constructor ----------------------------*/
   public POPSTTo_InstanceStanza(String s) {
      _iName = s; 
      set_postProcessTitle("Instance " + s);
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   
   /* ---------------------------- Getters/Setters --------------------------*/
   public String get_iName () {
      return _iName;
   }
   
   /* --------------------------- Inherited Methods -------------------------*/
   @Override
   public POPST_ToType getType() {
      return POPST_ToType.INSTANCE;
   }
}
