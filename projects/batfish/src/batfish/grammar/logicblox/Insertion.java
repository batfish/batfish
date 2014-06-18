package batfish.grammar.logicblox;

import java.util.ArrayList;
import java.util.List;

public abstract class Insertion {
   private String _asString;
   private int _hashCode;

   @Override
   public boolean equals(Object obj) {
      Insertion rhs = (Insertion) obj;
      return _asString.equals(rhs._asString);
   }

   public abstract List<Term> getHeadTerms();

   public abstract List<Term> getTailTerms();

   @Override
   public int hashCode() {
      return _hashCode;
   }

   protected void initialize() {
      String insertionText = "";
      List<Term> headTerms = getHeadTerms();
      List<Term> tailTerms = getTailTerms();
      for (Term headTerm : headTerms) {
         insertionText += "+" + headTerm.toString() + ", ";
      }
      insertionText = insertionText.substring(0, insertionText.length() - 2);
      if (tailTerms != null) {
         insertionText += " <- ";
         for (Term tailTerm : tailTerms) {
//            if (tailTerm.getPredicateName() != null) {
//               insertionText += "+";
//            }
            insertionText += tailTerm.toString() + ", ";
         }
         insertionText = insertionText.substring(0, insertionText.length() - 2);
      }
      insertionText += ".\n";
      _asString = insertionText;
      _hashCode = _asString.hashCode();
   }

   public String toDeletion() {
      String output = "";
      Term headTerm = getHeadTerms().get(0);
      output += "-" + headTerm.getPredicateName() + headTerm.getRemainder()
            + " <- ";
      List<Term> newTailTerms = new ArrayList<Term>();
      newTailTerms.addAll(getHeadTerms());
      List<Term> tailTerms = getTailTerms();
      if (tailTerms != null) {
         newTailTerms.addAll(tailTerms);
      }
      for (Term tailTerm : newTailTerms) {
         if (tailTerm.getPredicateName() != null) {
            output += tailTerm.getPredicateName() + "@prev";
         }
         output += tailTerm.getRemainder() + ", ";
      }
      if (newTailTerms.size() > 0) {
         output = output.substring(0, output.length() - 2);
      }
      output += ".\n";
      return output;
   }

   @Override
   public String toString() {
      return _asString;
   }

}
