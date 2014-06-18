package batfish.representation.cisco;

import java.util.List;

import batfish.representation.LineAction;

public class StandardCommunityListLine {

   private LineAction _action;
   private List<Long> _communities;

   public StandardCommunityListLine(LineAction action,
         List<Long> communities) {
      _action = action;
      _communities = communities;
   }

   public List<Long> getCommunities() {
      return _communities;
   }
   
   public LineAction getAction() {
      return _action;
   }
   
}
