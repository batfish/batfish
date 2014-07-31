package batfish.representation;

import batfish.util.Util;

public class PolicyMapSetDeleteCommunityLine extends PolicyMapSetLine {

   private static final long serialVersionUID = 1L;

   private CommunityList _list;

   public PolicyMapSetDeleteCommunityLine(CommunityList list) {
      _list = list;
   }

   @Override
   public String getIFString(int indentLevel) {
      String retString = Util.getIndentString(indentLevel)
            + "DeleteCommunity\n" + _list.getIFString(indentLevel + 1);

      return retString;
   }

   public CommunityList getList() {
      return _list;
   }

   @Override
   public PolicyMapSetType getType() {
      return PolicyMapSetType.DELETE_COMMUNITY;
   }

   @Override
   public boolean sameParseTree(PolicyMapSetLine line, String prefix) {
      boolean res = (line.getType() == PolicyMapSetType.DELETE_COMMUNITY);
      if (res == false) {
         System.out.println("PoliMapSetDelCommListLine:Type " + prefix);
         return res;
      }

      PolicyMapSetDeleteCommunityLine commLine = (PolicyMapSetDeleteCommunityLine) line;

      res = (_list.getName().equals(commLine._list.getName()));
      if (res == false) {
         System.out.println("PoliMapSetDelCommListLine " + prefix);

      }
      return res;
   }

}
