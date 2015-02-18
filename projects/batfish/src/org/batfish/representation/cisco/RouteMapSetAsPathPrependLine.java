package org.batfish.representation.cisco;

import java.util.List;

import org.batfish.representation.Configuration;
import org.batfish.representation.PolicyMapSetAsPathPrependLine;
import org.batfish.representation.PolicyMapSetLine;

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
