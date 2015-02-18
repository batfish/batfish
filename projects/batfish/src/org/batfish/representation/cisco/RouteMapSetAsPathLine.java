package org.batfish.representation.cisco;

import java.util.List;

import org.batfish.representation.Configuration;
import org.batfish.representation.PolicyMapSetLine;

public class RouteMapSetAsPathLine extends RouteMapSetLine {

   private static final long serialVersionUID = 1L;

   // private List<Integer> _asList;

   public RouteMapSetAsPathLine(List<Integer> asList) {
      // _asList = asList;
   }

   @Override
   public RouteMapSetType getType() {
      return RouteMapSetType.AS_PATH;
   }

   @Override
   public PolicyMapSetLine toPolicyMapSetLine(Configuration c) {
      // TODO Auto-generated method stub
      return null;
   }

}
