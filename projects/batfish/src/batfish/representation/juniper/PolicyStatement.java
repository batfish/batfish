package batfish.representation.juniper;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

public final class PolicyStatement implements Serializable {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private final String _name;

   private final Term _singletonTerm;

   private final Map<String, Term> _terms;

   public PolicyStatement(String name) {
      _name = name;
      String singletonTermName = "__" + _name + "__SINGLETON__";
      _singletonTerm = new Term(singletonTermName);
      _terms = new LinkedHashMap<String, Term>();
   }

   public String getName() {
      return _name;
   }

   public Term getSingletonTerm() {
      return _singletonTerm;
   }

   public Map<String, Term> getTerms() {
      return _terms;
   }

}
