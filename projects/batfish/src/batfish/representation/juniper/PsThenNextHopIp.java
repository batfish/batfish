package batfish.representation.juniper;

import java.util.Collections;

import batfish.representation.Configuration;
import batfish.representation.Ip;
import batfish.representation.PolicyMapClause;
import batfish.representation.PolicyMapSetNextHopLine;

public final class PsThenNextHopIp extends PsThen {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private final Ip _nextHopIp;

   public PsThenNextHopIp(Ip nextHopIp) {
      _nextHopIp = nextHopIp;
   }

   @Override
   public void applyTo(PolicyMapClause clause, Configuration c) {
      PolicyMapSetNextHopLine line = new PolicyMapSetNextHopLine(
            Collections.singletonList(_nextHopIp));
      clause.getSetLines().add(line);
   }

   public Ip getNextHopIp() {
      return _nextHopIp;
   }

}
