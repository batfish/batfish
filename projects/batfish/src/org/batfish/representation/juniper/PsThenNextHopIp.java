package org.batfish.representation.juniper;

import java.util.Collections;
import java.util.List;

import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.PolicyMapClause;
import org.batfish.datamodel.PolicyMapSetNextHopLine;
import org.batfish.datamodel.routing_policy.expr.NextHopIp;
import org.batfish.datamodel.routing_policy.statement.SetNextHop;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.main.Warnings;

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
   public void applyTo(List<Statement> statements,
         JuniperConfiguration juniperVendorConfiguration, Configuration c,
         Warnings warnings) {
      // todo: something with destination-vrf
      statements.add(new SetNextHop(
            new NextHopIp(Collections.singletonList(_nextHopIp)), false));
   }

   @Override
   public void applyTo(PolicyMapClause clause, Configuration c,
         Warnings warnings) {
      PolicyMapSetNextHopLine line = new PolicyMapSetNextHopLine(
            Collections.singletonList(_nextHopIp));
      clause.getSetLines().add(line);
   }

   public Ip getNextHopIp() {
      return _nextHopIp;
   }

}
