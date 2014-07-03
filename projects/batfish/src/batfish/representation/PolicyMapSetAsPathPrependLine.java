package batfish.representation;

import java.util.List;

import batfish.util.Util;

public class PolicyMapSetAsPathPrependLine extends PolicyMapSetLine {

   private List<Integer> _asList;

   public PolicyMapSetAsPathPrependLine(List<Integer> asList) {
      _asList = asList;
   }

   @Override
   public String getIFString(int indentLevel) {
      String retString = Util.getIndentString(indentLevel) + "AsPathPrepend";
      for (int as : _asList) {
         retString += " " + as;
      }
      return retString;
   }

   @Override
   public PolicyMapSetType getType() {
      return PolicyMapSetType.AS_PATH_PREPEND;
   }

   @Override
   public boolean sameParseTree(PolicyMapSetLine line, String prefix) {
      if (line.getType() != PolicyMapSetType.AS_PATH_PREPEND) {
         System.out.println("PolicyMapSetAsPathPrependLine:Type " + prefix);
         return false;
      }
      PolicyMapSetAsPathPrependLine rhs = (PolicyMapSetAsPathPrependLine) line;
      if (_asList.size() != rhs._asList.size()) {
         System.out.println("PolicyMapSetAsPathPrependLine:Size " + prefix);
         return false;
      }
      for (int i = 0; i < _asList.size(); i++) {
         if (!_asList.get(i).equals(rhs._asList.get(i))) {
            System.out.println("PolicyMapSetAsPathPrependLine:as[" + i + "] "
                  + prefix);
            return false;
         }
      }
      return true;
   }

}
