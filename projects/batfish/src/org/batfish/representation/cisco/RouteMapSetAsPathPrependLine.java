package org.batfish.representation.cisco;

import java.util.List;

import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.PolicyMapSetAsPathPrependLine;
import org.batfish.datamodel.PolicyMapSetLine;
import org.batfish.main.Warnings;

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
   public PolicyMapSetLine toPolicyMapSetLine(CiscoConfiguration v,
         Configuration c, Warnings w) {
      return new PolicyMapSetAsPathPrependLine(_asList);
   }

}
