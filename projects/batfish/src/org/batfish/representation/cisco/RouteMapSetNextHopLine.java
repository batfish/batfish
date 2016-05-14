package org.batfish.representation.cisco;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.PolicyMapSetLine;
import org.batfish.datamodel.PolicyMapSetNextHopLine;
import org.batfish.main.Warnings;

public class RouteMapSetNextHopLine extends RouteMapSetLine {

   private static final long serialVersionUID = 1L;

   private Set<Ip> _nextHops;

   public RouteMapSetNextHopLine(Set<Ip> nextHops) {
      _nextHops = nextHops;
   }

   public Set<Ip> getNextHops() {
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
      List<Ip> nextHopList = new ArrayList<Ip>();
      nextHopList.addAll(_nextHops);
      return new PolicyMapSetNextHopLine(nextHopList);
   }

}
