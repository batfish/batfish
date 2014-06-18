package batfish.representation.juniper;

import batfish.representation.juniper.MatchType;
import batfish.representation.juniper.PolicyStatementMatchLine;

public class PolicyStatementMatchNeighborLine extends PolicyStatementMatchLine {

   private String _neighborIp;

   public PolicyStatementMatchNeighborLine(String neighborIP) {
      _neighborIp = neighborIP;
   }

   @Override
   public MatchType getType() {
      return MatchType.NEIGHBOR;
   }

   public String getNeighborIp() {
      return _neighborIp;
   }
}
