package org.batfish.representation.cisco;

import java.util.ArrayList;
import java.util.List;

import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.PolicyMapSetLine;
import org.batfish.datamodel.PolicyMapSetNextHopLine;
import org.batfish.datamodel.routing_policy.expr.NextHopIp;
import org.batfish.datamodel.routing_policy.statement.SetNextHop;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.main.Warnings;

public class RouteMapSetNextHopLine extends RouteMapSetLine {

   private static final long serialVersionUID = 1L;

   private List<Ip> _nextHops;

   public RouteMapSetNextHopLine(List<Ip> nextHops) {
      _nextHops = nextHops;
   }

   @Override
   public void applyTo(List<Statement> statements, CiscoConfiguration cc,
         Configuration c, Warnings w) {
      // TODO: something with destination-vrf
      statements.add(new SetNextHop(new NextHopIp(_nextHops), false));
   }

   public List<Ip> getNextHops() {
      return _nextHops;
   }

   @Override
   public RouteMapSetType getType() {
      return RouteMapSetType.NEXT_HOP;
   }

   @Override
   public PolicyMapSetLine toPolicyMapSetLine(CiscoConfiguration v,
         Configuration c, Warnings w) {
      // TODO: change to set in PolicyMapSetNextHopLine if possible
      List<Ip> nextHopList = new ArrayList<>();
      nextHopList.addAll(_nextHops);
      return new PolicyMapSetNextHopLine(nextHopList);
   }

}
