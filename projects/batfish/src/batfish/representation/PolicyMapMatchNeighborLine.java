package batfish.representation;

import batfish.representation.PolicyMapMatchType;
import batfish.representation.PolicyMapMatchLine;

public class PolicyMapMatchNeighborLine extends PolicyMapMatchLine {

   private static final long serialVersionUID = 1L;

   private Ip _neighborIp;

   public PolicyMapMatchNeighborLine(Ip neighborIP) {
      _neighborIp = neighborIP;
   }

   public Ip getNeighborIp() {
      return _neighborIp;
   }

   @Override
   public PolicyMapMatchType getType() {
      return PolicyMapMatchType.NEIGHBOR;
   }

}
