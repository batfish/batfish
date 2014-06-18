package batfish.grammar.logicblox;

import java.util.ArrayList;
import java.util.List;

public class ConstructorInsertion extends Insertion {

   private Term _applicationTerm;
   private Term _entityTerm;
   private List<Term> _headTerms;
   private List<Term> _tailTerms;
   
   public ConstructorInsertion(Term entityTerm, Term applicationTerm,
         List<Term> tailTerms) {
      _entityTerm = entityTerm;
      _applicationTerm = applicationTerm;
      _tailTerms = tailTerms;
      _headTerms = new ArrayList<Term>();
      _headTerms.add(_entityTerm);
      _headTerms.add(_applicationTerm);
      initialize();
   }

   public Term getApplicationTerm() {
      return _applicationTerm;
   }

   public Term getEntityTerm() {
      return _entityTerm;
   }

   @Override
   public List<Term> getHeadTerms() {
      return _headTerms;
   }

   @Override
   public List<Term> getTailTerms() {
      return _tailTerms;
   }

}
