package batfish.representation.juniper;

import java.util.List;

public class PolicyStatementSetNextHopLine extends PolicyStatementSetLine {

   private List<String> _nextHops;

   public PolicyStatementSetNextHopLine(List<String> nextHops) {
      _nextHops = nextHops;
   }

   @Override
   public SetType getSetType() {
      return SetType.NEXT_HOP;
   }

   public List<String> getNextHops() {
      return _nextHops;
   }
   
}
