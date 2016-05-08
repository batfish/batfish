package org.batfish.representation.juniper;

import org.batfish.common.datamodel.RoutingProtocol;
import org.batfish.main.Warnings;
import org.batfish.representation.Configuration;
import org.batfish.representation.PolicyMapClause;
import org.batfish.representation.PolicyMapMatchProtocolLine;

public final class PsFromProtocol extends PsFrom {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private final RoutingProtocol _protocol;

   public PsFromProtocol(RoutingProtocol protocol) {
      _protocol = protocol;
   }

   @Override
   public void applyTo(PolicyMapClause clause, PolicyStatement ps,
         JuniperConfiguration jc, Configuration c, Warnings warnings) {
      clause.getMatchLines().add(new PolicyMapMatchProtocolLine(_protocol));
   }

   public RoutingProtocol getProtocol() {
      return _protocol;
   }

}
