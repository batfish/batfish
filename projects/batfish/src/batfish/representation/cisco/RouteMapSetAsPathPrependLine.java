package batfish.representation.cisco;

import java.util.List;

import batfish.representation.Configuration;
import batfish.representation.PolicyMapSetAsPathPrependLine;
import batfish.representation.PolicyMapSetLine;

public class RouteMapSetAsPathPrependLine extends RouteMapSetLine {

   private static final long serialVersionUID = 1L;

   private List<Integer> _asList;

   public RouteMapSetAsPathPrependLine(List<Integer> asList) {
      _asList = asList;
   }

   public List<Integer> getAsList() {
      return _asList;
   }

   @Override
   public RouteMapSetType getType() {
      return RouteMapSetType.AS_PATH_PREPEND;
   }

   @Override
   public PolicyMapSetLine toPolicyMapSetLine(Configuration c) {
      return new PolicyMapSetAsPathPrependLine(_asList);
   }

}
