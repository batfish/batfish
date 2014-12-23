package batfish.representation.juniper;

import batfish.representation.PolicyMapClause;
import batfish.representation.Prefix;

public final class PsFromRouteFilterThrough extends PsFromRouteFilter {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private final Prefix _throughPrefix;

   public PsFromRouteFilterThrough(Prefix prefix, Prefix throughPrefix) {
      super(prefix);
      _throughPrefix = prefix;
   }

   public Prefix getThroughPrefix() {
      return _throughPrefix;
   }

   @Override
   public void applyTo(PolicyMapClause clause) {
      throw new UnsupportedOperationException("no implementation for generated method"); // TODO Auto-generated method stub
   }

}
