package batfish.representation;

import java.util.List;

import batfish.util.Util;

public class CommunityList {

   private String _name;
   private List<CommunityListLine> _lines;

   public CommunityList(String name, List<CommunityListLine> lines) {
      _name = name;
      _lines = lines;
   }

   public List<CommunityListLine> getLines() {
      return _lines;
   }

   public String getName() {
      return _name;
   }

   public String getIFString(int indentLevel) {
	   String retString = Util.getIndentString(indentLevel) + "CommunityList " + _name;
	   
	   for (CommunityListLine cll : _lines) {
	       retString += "\n" + cll.getIFString(indentLevel + 1);
	   }
	   
	   return retString;
   }

   public boolean sameParseTree(CommunityList list, String prefix) {
      boolean res = (_name.equals(list._name));
      boolean finalRes = res;

      if (_lines.size() != list._lines.size()) {
         System.out.println("CommList:Line:Size "+prefix);
         return false;
      }
      else {
         for (int i = 0; i < _lines.size(); i++) {
            res = _lines.get(i).sameParseTree(list._lines.get(i));
            if (res == false) {
               System.out.println("CommList:Line " + prefix);
               finalRes = false;
            }
         }
      }

      return finalRes;
   }

}
