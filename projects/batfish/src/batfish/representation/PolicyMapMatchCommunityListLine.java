package batfish.representation;

import java.util.Set;

import batfish.util.Util;

public class PolicyMapMatchCommunityListLine extends PolicyMapMatchLine {

   private static final long serialVersionUID = 1L;

   private Set<CommunityList> _lists;

   public PolicyMapMatchCommunityListLine(Set<CommunityList> lists) {
      _lists = lists;
   }

   @Override
   public String getIFString(int indentLevel) {

      String retString = Util.getIndentString(indentLevel) + "CommunityList";

      for (CommunityList cl : _lists) {
         retString += " " + cl.getName();
      }

      return retString;
   }

   public Set<CommunityList> getLists() {
      return _lists;
   }

   @Override
   public PolicyMapMatchType getType() {
      return PolicyMapMatchType.COMMUNITY_LIST;
   }

   @Override
   public boolean sameParseTree(PolicyMapMatchLine line, String prefix) {
      boolean res = (line.getType() == PolicyMapMatchType.COMMUNITY_LIST);
      boolean finalRes = res;

      if (res == false) {
         System.out.println("PoliMapMatchCommListLine:Type " + prefix);
         finalRes = false;
      }

      PolicyMapMatchCommunityListLine commLine = (PolicyMapMatchCommunityListLine) line;

      if (_lists.size() != commLine._lists.size()) {
         System.out.println("PoliMapMatchCommListLine:Size " + prefix);
         return false;
      }
      else {
         Object[] lhs = _lists.toArray();
         Object[] rhs = commLine._lists.toArray();
         for (int i = 0; i < _lists.size(); i++) {
            res = ((CommunityList) lhs[i]).getName().equals(
                  ((CommunityList) rhs[i]).getName());
            if (res == false) {
               System.out.println("PoliMapMatchCommListLine " + prefix);
               finalRes = false;
            }
         }
      }

      return finalRes;
   }

}
