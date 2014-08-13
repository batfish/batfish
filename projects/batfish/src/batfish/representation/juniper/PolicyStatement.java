package batfish.representation.juniper;

import java.util.List;

public class PolicyStatement {
   
   private String _name;
   private List<PolicyStatement_Term> _terms;
   
   /* ------------------------------ Constructor ----------------------------*/
   public PolicyStatement(String n, List<PolicyStatement_Term> t) {
      _name = n;
      _terms = t;
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   
   /* ---------------------------- Getters/Setters --------------------------*/
   public List<PolicyStatement_Term> get_terms() {
      return _terms;
   }
   public String get_name () {
      return _name;
   }
   
   /* --------------------------- Inherited Methods -------------------------*/
  
}
