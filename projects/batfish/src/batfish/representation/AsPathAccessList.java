package batfish.representation;

import java.io.Serializable;
import java.util.List;

import batfish.util.Util;

public class AsPathAccessList implements Serializable {

   private static final long serialVersionUID = 1L;

   private List<AsPathAccessListLine> _lines;
   private String _name;

   public AsPathAccessList(String name, List<AsPathAccessListLine> lines) {
      _lines = lines;
      _name = name;
   }

   public String getIFString(int indentLevel) {

      String retString = Util.getIndentString(indentLevel)
            + "AsPathAccessList " + _name;

      for (AsPathAccessListLine apall : _lines) {
         retString += "\n" + apall.getIFString(indentLevel + 1);
      }

      return retString;
   }

   public List<AsPathAccessListLine> getLines() {
      return _lines;
   }

   public String getName() {
      return _name;
   }

   public boolean sameParseTree(AsPathAccessList list, String prefix) {
      boolean res = (_name.equals(list._name));
      boolean finalRes = res;

      if (_lines.size() != list._lines.size()) {
         System.out.println("AsPathAccessLine:Size " + prefix);
         return false;
      }
      for (int i = 0; i < _lines.size(); i++) {
         res = (_lines.get(i).getRegex().equals(list._lines.get(i).getRegex()));
         if (res == false) {
            System.out.println("AsPathAccessLine " + prefix);
            finalRes = false;
         }
      }
      return finalRes;
   }

}
