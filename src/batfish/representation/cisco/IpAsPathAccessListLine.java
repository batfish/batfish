package batfish.representation.cisco;

import batfish.representation.LineAction;

public class IpAsPathAccessListLine {
   private LineAction _action;
   private String _regex;

   public IpAsPathAccessListLine(LineAction action, String regex) {
      _action = action;
      _regex = regex;
   }

   public LineAction getAction() {
      return _action;
   }

   public String getRegex() {
      return _regex;
   }

}
