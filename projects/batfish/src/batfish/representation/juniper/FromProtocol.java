package batfish.representation.juniper;

import batfish.representation.PolicyMapMatchLine;
import batfish.representation.RoutingProtocol;

public final class FromProtocol extends From {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private final RoutingProtocol _protocol;

   public FromProtocol(RoutingProtocol protocol) {
      _protocol = protocol;
   }

   public RoutingProtocol getProtocol() {
      return _protocol;
   }

   @Override
   public PolicyMapMatchLine toPolicyMapMatchLine() {
      // TODO Auto-generated method stub
      return null;
   }

}
