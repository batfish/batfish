package batfish.grammar.juniper.policy_options;

import batfish.grammar.juniper.StanzaStatusType;
import batfish.representation.juniper.FamilyOps;
import batfish.representation.juniper.FamilyType;

public class POPSTFr_FamilyStanza extends POPST_FromStanza {
   
   private FamilyType _famType;
   
   /* ------------------------------ Constructor ----------------------------*/
   public POPSTFr_FamilyStanza(FamilyType ft) {
      _famType = ft;        
      if (_famType == FamilyType.INET6){
         set_stanzaStatus(StanzaStatusType.IPV6); 
      }
      set_postProcessTitle("Family " + FamilyOps.FamilyTypeToString(ft));
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   
   /* ---------------------------- Getters/Setters --------------------------*/
   public FamilyType get_famType () {
      return _famType;
   }
   
   /* --------------------------- Inherited Methods -------------------------*/
   @Override
   public POPST_FromType getType() {
      return POPST_FromType.FAMILY;
   }

}
