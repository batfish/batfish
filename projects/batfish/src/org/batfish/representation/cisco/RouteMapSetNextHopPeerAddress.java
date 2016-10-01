package org.batfish.representation.cisco;

import java.util.List;

import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.PolicyMapSetLine;
import org.batfish.datamodel.PolicyMapSetNopLine;
import org.batfish.datamodel.routing_policy.expr.NextHopPeerAddress;
import org.batfish.datamodel.routing_policy.statement.SetNextHop;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.main.Warnings;

public class RouteMapSetNextHopPeerAddress extends RouteMapSetLine {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   @Override
   public void applyTo(List<Statement> statements, CiscoConfiguration cc,
         Configuration c, Warnings w) {
      statements.add(new SetNextHop(new NextHopPeerAddress(), false));
   }

   @Override
   public RouteMapSetType getType() {
      return RouteMapSetType.NEXT_HOP_PEER_ADDRESS;
   }

   @Override
   public PolicyMapSetLine toPolicyMapSetLine(CiscoConfiguration v,
         Configuration c, Warnings w) {
      // TODO: implement or deprecate
      return new PolicyMapSetNopLine();
   }

}
