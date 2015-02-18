package org.batfish.representation;

import java.util.List;

public class PolicyMapSetNextHopLine extends PolicyMapSetLine {

   private static final long serialVersionUID = 1L;

   private List<Ip> _nextHops;

   public PolicyMapSetNextHopLine(List<Ip> nextHops) {
      _nextHops = nextHops;
   }

   public List<Ip> getNextHops() {
      return _nextHops;
   }

   @Override
   public PolicyMapSetType getType() {
      return PolicyMapSetType.NEXT_HOP;
   }

}
