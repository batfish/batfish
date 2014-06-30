package batfish.representation.cisco;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import batfish.representation.Configuration;
import batfish.representation.Ip;
import batfish.representation.PolicyMapSetLine;
import batfish.representation.PolicyMapSetNextHopLine;

public class RouteMapSetNextHopLine extends RouteMapSetLine {

   private Set<Ip> _nextHops;

   public RouteMapSetNextHopLine(Set<Ip> nextHops) {
      _nextHops = nextHops;
   }

   public Set<Ip> getNextHops() {
      return _nextHops;
   }

   @Override
   public PolicyMapSetLine toPolicyMapSetLine(Configuration c) {
      // TODO: change to set in PolicyMapSetNextHopLine if possible
      List<Ip> nextHopList = new ArrayList<Ip>();
      nextHopList.addAll(_nextHops);
      return new PolicyMapSetNextHopLine(nextHopList);
   }
   
}
