package batfish.representation;

import batfish.util.Util;

public class PolicyMapSetCommunityNoneLine extends PolicyMapSetLine {

   private static final long serialVersionUID = 1L;

   @Override
   public String getIFString(int indentLevel) {
      String retString = Util.getIndentString(indentLevel) + "NoCommunity";
      return retString;
   }

   @Override
   public PolicyMapSetType getType() {
      return PolicyMapSetType.COMMUNITY_NONE;
   }

   @Override
   public boolean sameParseTree(PolicyMapSetLine line, String prefix) {
      if (line.getType() != PolicyMapSetType.COMMUNITY_NONE) {
         System.out.println("PolicyMapSetCommunityNoneLine:Type " + prefix);
         return false;
      }
      return true;
   }

}
