package batfish.grammar.juniper.ospf;

import java.util.ArrayList;
import java.util.List;

public class OP_ExportStanza extends OPStanza {
   
   private List<String> _policyNames;
   
   /* ------------------------------ Constructor ----------------------------*/
   public OP_ExportStanza() {
      _policyNames = new ArrayList<String>();
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   public void addPolicy(String p) {
      _policyNames.add(p);
   }
   
   /* ---------------------------- Getters/Setters --------------------------*/
   public List<String> get_policyNames() {
      return _policyNames;
   }
   
   /* --------------------------- Inherited Methods -------------------------*/   
    @Override
   public OPType getType() {
      return OPType.EXPORT;
   }

}
