package org.batfish.representation;

import org.batfish.representation.PolicyMapMatchLine;
import org.batfish.representation.PolicyMapMatchType;

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
