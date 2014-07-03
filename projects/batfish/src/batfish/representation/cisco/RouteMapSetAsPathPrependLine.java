package batfish.representation.cisco;

import java.util.List;

import batfish.representation.Configuration;
import batfish.representation.PolicyMapSetAsPathPrependLine;
import batfish.representation.PolicyMapSetLine;

public class RouteMapSetAsPathPrependLine extends RouteMapSetLine {

   private List<Integer> _asList;

   public RouteMapSetAsPathPrependLine(List<Integer> asList) {
      _asList = asList;
   }

   @Override
   public PolicyMapSetLine toPolicyMapSetLine(Configuration c) {
      return new PolicyMapSetAsPathPrependLine(_asList);
   }

}
