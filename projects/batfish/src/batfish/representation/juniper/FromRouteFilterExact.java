package batfish.representation.juniper;

import batfish.representation.PolicyMapMatchLine;
import batfish.representation.Prefix;

public final class FromRouteFilterExact extends FromRouteFilter {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   public FromRouteFilterExact(Prefix prefix) {
      super(prefix);
   }

   @Override
   public PolicyMapMatchLine toPolicyMapMatchLine() {
      // TODO Auto-generated method stub
      return null;
   }

}
