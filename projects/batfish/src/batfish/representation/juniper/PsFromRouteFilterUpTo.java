package batfish.representation.juniper;

import batfish.representation.PolicyMapClause;
import batfish.representation.Prefix;

public final class PsFromRouteFilterUpTo extends PsFromRouteFilter {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private final int _maxPrefixLength;

   public PsFromRouteFilterUpTo(Prefix prefix, int maxPrefixLength) {
      super(prefix);
      _maxPrefixLength = maxPrefixLength;
   }

   public int getMaxPrefixLength() {
      return _maxPrefixLength;
   }

   @Override
   public void applyTo(PolicyMapClause clause) {
      throw new UnsupportedOperationException("no implementation for generated method"); // TODO Auto-generated method stub
   }

}
