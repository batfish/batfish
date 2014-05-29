package batfish.grammar.logicblox;

import java.util.ArrayList;
import java.util.List;

public class RefmodeInsertion extends Insertion {

   private Term _entityTerm;
   private List<Term> _headTerms;
   private Term _refmodeTerm;
   private List<Term> _tailTerms;

   public RefmodeInsertion(Term entityTerm, Term refmodeTerm,
         List<Term> tailTerms) {
      _entityTerm = entityTerm;
      _refmodeTerm = refmodeTerm;
      _tailTerms = tailTerms;
      _headTerms = new ArrayList<Term>();
      _headTerms.add(_entityTerm);
      _headTerms.add(_refmodeTerm);
      initialize();
   }

   public Term getEntityTerm() {
      return _entityTerm;
   }

   @Override
   public List<Term> getHeadTerms() {
      return _headTerms;
   }

   public Term getRefmodeTerm() {
      return _refmodeTerm;
   }

   @Override
   public List<Term> getTailTerms() {
      return _tailTerms;
   }

}
