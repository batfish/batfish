package batfish.representation.juniper;

import batfish.representation.PolicyMapClause;
import batfish.representation.RoutingProtocol;

public final class PsFromProtocol extends PsFrom {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private final RoutingProtocol _protocol;

   public PsFromProtocol(RoutingProtocol protocol) {
      _protocol = protocol;
   }

   public RoutingProtocol getProtocol() {
      return _protocol;
   }

   @Override
   public void applyTo(PolicyMapClause clause) {
      throw new UnsupportedOperationException("no implementation for generated method"); // TODO Auto-generated method stub
   }

}
