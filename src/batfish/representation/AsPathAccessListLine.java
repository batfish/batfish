package batfish.representation;

import batfish.util.Util;

public class AsPathAccessListLine {
   
   private String _regex;

   public AsPathAccessListLine(String regex) {
      _regex = regex;
   }

   public String getRegex() {
      return _regex;
   }

   public String getIFString(int indentLevel) {
	   return Util.getIndentString(indentLevel) + _regex;
   }
}
