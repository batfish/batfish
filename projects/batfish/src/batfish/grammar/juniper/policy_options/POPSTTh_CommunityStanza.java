package batfish.grammar.juniper.policy_options;

import java.util.ArrayList;
import java.util.List;

public class POPSTTh_CommunityStanza extends POPST_ThenStanza {
   
   private List<String> _commNames;
   private POPSTTh_CommunityType _commType;
   
   /* ------------------------------ Constructor ----------------------------*/
   
   /* ----------------------------- Other Methods ---------------------------*/
   public void addCommName (String s) {
      if (_commNames == null) {
         _commNames = new ArrayList<String> ();
      }
      _commNames.add(s);
      set_postProcessTitle("Community");
   }
   
   /* ---------------------------- Getters/Setters --------------------------*/
   public List<String> get_commNames() {
      return _commNames;
   }
   public void set_commNames (List<String> l) {
      _commNames = l;
   }
   public POPSTTh_CommunityType get_commType() {
      return _commType;
   }
   public void set_commType(POPSTTh_CommunityType c) {
      _commType = c;
   }
   
   /* --------------------------- Inherited Methods -------------------------*/
   @Override
   public POPST_ThenType getType() {
      return POPST_ThenType.COMMUNITY;
   }

}
