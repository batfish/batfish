package batfish.representation.cisco;

import java.util.Set;

public class RouteMapMatchTagLine extends RouteMapMatchLine {

   private Set<Integer> _tags;

   public RouteMapMatchTagLine(Set<Integer> tags) {
      _tags = tags;
   }

   public Set<Integer> getTags() {
      return _tags;
   }

   @Override
   public RouteMapMatchType getType() {
      return RouteMapMatchType.TAG;
   }

}
