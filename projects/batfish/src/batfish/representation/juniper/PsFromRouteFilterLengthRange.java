package batfish.representation.juniper;

import batfish.representation.PolicyMapClause;
import batfish.representation.Prefix;

public final class PsFromRouteFilterLengthRange extends PsFromRouteFilter {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private final int _maxPrefixLength;

   private final int _minPrefixLength;

   public PsFromRouteFilterLengthRange(Prefix prefix, int minPrefixLength,
         int maxPrefixLength) {
      super(prefix);
      _minPrefixLength = minPrefixLength;
      _maxPrefixLength = maxPrefixLength;
   }

   public int getMaxPrefixLength() {
      return _maxPrefixLength;
   }

   public int getMinPrefixLength() {
      return _minPrefixLength;
   }

   @Override
   public void applyTo(PolicyMapClause clause) {
      throw new UnsupportedOperationException("no implementation for generated method"); // TODO Auto-generated method stub
   }

}
