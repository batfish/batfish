package batfish.representation;

import java.io.Serializable;
import java.util.List;

import batfish.util.Util;

public class CommunityList implements Serializable {

   private static final long serialVersionUID = 1L;

   private List<CommunityListLine> _lines;
   private String _name;

   public CommunityList(String name, List<CommunityListLine> lines) {
      _name = name;
      _lines = lines;
   }

   public String getIFString(int indentLevel) {
      String retString = Util.getIndentString(indentLevel) + "CommunityList "
            + _name;

      for (CommunityListLine cll : _lines) {
         retString += "\n" + cll.getIFString(indentLevel + 1);
      }

      return retString;
   }

   public List<CommunityListLine> getLines() {
      return _lines;
   }

   public String getName() {
      return _name;
   }

}
