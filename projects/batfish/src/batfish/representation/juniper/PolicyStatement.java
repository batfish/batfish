package batfish.representation.juniper;

import java.io.Serializable;
import java.util.List;

public class PolicyStatement implements Serializable {
   
   private static final long serialVersionUID = 1L;

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
