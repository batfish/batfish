package batfish.representation.juniper;

import java.io.Serializable;

import batfish.representation.LineAction;

public class ExpandedCommunityListLine implements Serializable {

   private static final long serialVersionUID = 1L;

   private String _regex;
   private LineAction _action;

   public ExpandedCommunityListLine(LineAction action, String regex) {
      _action = action;
      _regex = regex;
   }
   
   public String getRegex() {
      return _regex;
   }
   
   public LineAction getAction() {
      return _action;
   }
}
