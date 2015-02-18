package org.batfish.representation.juniper;

import org.batfish.representation.Configuration;
import org.batfish.representation.PolicyMapClause;
import org.batfish.representation.PolicyMapMatchProtocolLine;
import org.batfish.representation.RoutingProtocol;

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
   public void applyTo(PolicyMapClause clause, Configuration c) {
      clause.getMatchLines().add(new PolicyMapMatchProtocolLine(_protocol));
   }

   public RoutingProtocol getProtocol() {
      return _protocol;
   }

}
