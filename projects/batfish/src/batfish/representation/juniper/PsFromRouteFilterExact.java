package batfish.representation.juniper;

import batfish.representation.PolicyMapClause;
import batfish.representation.Prefix;

public final class PsFromRouteFilterExact extends PsFromRouteFilter {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   public PsFromRouteFilterExact(Prefix prefix) {
      super(prefix);
   }

   @Override
   public void applyTo(PolicyMapClause clause) {
      throw new UnsupportedOperationException("no implementation for generated method"); // TODO Auto-generated method stub
   }

}
