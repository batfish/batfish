package batfish.representation.juniper;

import java.io.Serializable;

public class ASPathAccessListLine implements Serializable {

   private static final long serialVersionUID = 1L;

   private String _regex;

   public ASPathAccessListLine(String regex) {
      _regex = regex;
   }

   public String getRegex() {
      return _regex;
   }

}
