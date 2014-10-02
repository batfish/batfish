package batfish.grammar.juniper.policy_options;

import java.util.ArrayList;
import java.util.List;

import batfish.representation.juniper.ASPathAccessList;

public class POPSTFr_CommunityStanza extends POPST_FromStanza {
   
   private List<String> _commNames;
   
   /* ------------------------------ Constructor ----------------------------*/
   public POPSTFr_CommunityStanza() {
      _commNames = new ArrayList<String>();
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   public void addListName(String c) {
      _commNames.add(c);
   }
   
   /* ---------------------------- Getters/Setters --------------------------*/
   public List<String> get_commNames() {
      return _commNames;
   }
   
   /* --------------------------- Inherited Methods -------------------------*/
   @Override
   public POPST_FromType getType() {
      return POPST_FromType.COMMUNITY;
   }

}
