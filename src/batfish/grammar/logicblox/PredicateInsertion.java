package batfish.grammar.logicblox;

import java.util.ArrayList;
import java.util.List;

public class PredicateInsertion extends Insertion {

   private List<Term> _headTerms;
   private Term _predicateTerm;
   private List<Term> _tailTerms;

   public PredicateInsertion(Term predicateTerm, List<Term> tailTerms) {
      _predicateTerm = predicateTerm;
      _tailTerms = tailTerms;
      _headTerms = new ArrayList<Term>();
      _headTerms.add(_predicateTerm);
      initialize();
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
