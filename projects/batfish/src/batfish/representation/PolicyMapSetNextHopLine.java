package batfish.representation;

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

   @Override
   public boolean sameParseTree(PolicyMapSetLine line, String prefix) {
      boolean res = (line.getType() == PolicyMapSetType.NEXT_HOP);
      boolean finalRes = res;
      if (res == false) {
         System.out.println("PoliMapSetNextHopLine:Type " + prefix);
         return res;
      }

      PolicyMapSetNextHopLine hopLine = (PolicyMapSetNextHopLine) line;
      if (_nextHops.size() != hopLine._nextHops.size()) {
         System.out.println("PoliMapSetNextHopLine:NextHops:Size " + prefix);
         return false;
      }
      else {
         for (int i = 0; i < _nextHops.size(); i++) {
            res = (_nextHops.get(i).equals(hopLine._nextHops.get(i)));
            if (res == false) {
               System.out.println("PoliMapSetNextHopLine:NextHops " + prefix);
               finalRes = res;
            }
         }
      }

      return finalRes;
   }
}
