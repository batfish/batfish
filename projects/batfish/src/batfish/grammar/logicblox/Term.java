package batfish.grammar.logicblox;

public class Term {

   private String _predicateName;
   private String _remainder;

   public Term(String predicateName, String remainder) {
      _predicateName = predicateName;
      _remainder = remainder;
   }

   public String getPredicateName() {
      return _predicateName;
   }

   public String getRemainder() {
      return _remainder;
   }

   @Override
   public String toString() {
      if (_predicateName != null) {
         return _predicateName + _remainder;
      }
      else {
         return _remainder;
      }
   }

}
