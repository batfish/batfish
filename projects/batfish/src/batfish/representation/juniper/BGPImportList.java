package batfish.representation.juniper;

import java.util.ArrayList;
import java.util.List;

import batfish.grammar.juniper.StanzaWithStatus;

public class BGPImportList extends StanzaWithStatus {
   
   private List<String> _policyNames;
   
   /* ------------------------------ Constructor ----------------------------*/
   public BGPImportList() {
      _policyNames = new ArrayList<String>();
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   public void AddPolicyName(String n) {
      _policyNames.add(n);
   }
   
   /* ---------------------------- Getters/Setters --------------------------*/
   public List<String> get_policyNames () {
      return _policyNames;
   }
   
   /* --------------------------- Inherited Methods -------------------------*/  

}
