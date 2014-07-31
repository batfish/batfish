package batfish.representation;

import java.util.List;

import batfish.util.Util;

public class PolicyMapSetAddCommunityLine extends PolicyMapSetLine {

   private static final long serialVersionUID = 1L;

   private List<Long> _communities;

   public PolicyMapSetAddCommunityLine(List<Long> communities) {
      _communities = communities;
   }

   public List<Long> getCommunities() {
      return _communities;
   }

   @Override
   public String getIFString(int indentLevel) {
      String retString = Util.getIndentString(indentLevel) + "AddCommunity";

      for (long comm : _communities) {
         retString += " " + comm;
      }

      return retString;
   }

   @Override
   public PolicyMapSetType getType() {
      return PolicyMapSetType.ADDITIVE_COMMUNITY;
   }

   @Override
   public boolean sameParseTree(PolicyMapSetLine line, String prefix) {
      boolean res = (line.getType() == PolicyMapSetType.ADDITIVE_COMMUNITY);
      boolean finalRes = res;
      if (res == false) {
         System.out.println("PoliMapSetAddCommLine:Type " + prefix);
         return res;
      }

      PolicyMapSetAddCommunityLine addLine = (PolicyMapSetAddCommunityLine) line;
      if (_communities.size() != addLine._communities.size()) {
         System.out.println("PoliMapSetAddCommLine:Size " + prefix);
         return false;
      }
      else {
         for (int i = 0; i < _communities.size(); i++) {
            res = (_communities.get(i).equals(addLine._communities.get(i)));
            if (res == false) {
               System.out.println("PoliMapSetAddCommLine " + prefix);
               finalRes = res;
            }
         }
      }

      return finalRes;
   }

}
