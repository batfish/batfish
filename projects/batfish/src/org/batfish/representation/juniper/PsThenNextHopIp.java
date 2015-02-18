package org.batfish.representation.juniper;

import java.util.Collections;

import org.batfish.representation.Configuration;
import org.batfish.representation.Ip;
import org.batfish.representation.PolicyMapClause;
import org.batfish.representation.PolicyMapSetNextHopLine;

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
