package org.batfish.representation.cisco;

import java.util.List;

import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.PolicyMapSetAsPathPrependLine;
import org.batfish.datamodel.PolicyMapSetLine;
import org.batfish.datamodel.routing_policy.expr.AsExpr;
import org.batfish.datamodel.routing_policy.statement.PrependAsPath;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.main.Warnings;

public class RouteMapSetAsPathPrependLine extends RouteMapSetLine {

   private static final long serialVersionUID = 1L;

   private List<AsExpr> _asList;

   public RouteMapSetAsPathPrependLine(List<AsExpr> asList) {
      _asList = asList;
   }

   @Override
   public void applyTo(List<Statement> statements, CiscoConfiguration cc,
         Configuration c, Warnings w) {
      statements.add(new PrependAsPath(_asList));
   }

   public List<AsExpr> getAsList() {
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
