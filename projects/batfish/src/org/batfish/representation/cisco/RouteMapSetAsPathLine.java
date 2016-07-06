package org.batfish.representation.cisco;

import java.util.List;

import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.PolicyMapSetLine;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.main.Warnings;

public class RouteMapSetAsPathLine extends RouteMapSetLine {

   private static final long serialVersionUID = 1L;

   // private List<Integer> _asList;

   public RouteMapSetAsPathLine(List<Integer> asList) {
      // _asList = asList;
   }

   @Override
   public void applyTo(List<Statement> statements, CiscoConfiguration cc,
         Configuration c, Warnings w) {
      w.unimplemented("Do not currently support setting as-path in route-map");
      // TODO: implement
   }

   @Override
   public RouteMapSetType getType() {
      return RouteMapSetType.AS_PATH;
   }

   @Override
   public PolicyMapSetLine toPolicyMapSetLine(CiscoConfiguration v,
         Configuration c, Warnings w) {
      // TODO Auto-generated method stub
      return null;
   }

}
