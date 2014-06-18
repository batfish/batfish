package batfish.representation.cisco;

import java.util.ArrayList;
import java.util.List;

import batfish.representation.Configuration;
import batfish.representation.Ip;
import batfish.representation.PolicyMapSetLine;
import batfish.representation.PolicyMapSetNextHopLine;

public class RouteMapSetNextHopLine extends RouteMapSetLine {

   private List<String> _nextHops;

   public RouteMapSetNextHopLine(List<String> nextHops) {
      _nextHops = nextHops;
   }

   public List<String> getNextHops() {
      return _nextHops;
   }

   @Override
   public PolicyMapSetLine toPolicyMapSetLine(Configuration c) {
      List<Ip> nextHopsAsIps = new ArrayList<Ip>();
      for (String nextHop : _nextHops) {
         nextHopsAsIps.add(new Ip(nextHop));
      }
      return new PolicyMapSetNextHopLine(nextHopsAsIps);
      
   }
   
}
