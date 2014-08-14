package batfish.representation;

import java.io.Serializable;

import batfish.util.Util;

public class AsPathAccessListLine implements Serializable {

   private static final long serialVersionUID = 1L;

   private String _regex;

   public AsPathAccessListLine(String regex) {
      _regex = regex;
   }

   public String getIFString(int indentLevel) {
      return Util.getIndentString(indentLevel) + _regex;
   }

   public String getRegex() {
      return _regex;
   }
}
