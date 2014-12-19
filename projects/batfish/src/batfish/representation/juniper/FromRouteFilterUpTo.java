package batfish.representation.juniper;

import batfish.representation.PolicyMapMatchLine;
import batfish.representation.Prefix;

public final class FromRouteFilterUpTo extends FromRouteFilter {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private final int _maxPrefixLength;

   public int getMaxPrefixLength() {
      return _maxPrefixLength;
   }

   public FromRouteFilterUpTo(Prefix prefix,
         int maxPrefixLength) {
      super(prefix);
      _maxPrefixLength = maxPrefixLength;
   }

   @Override
   public PolicyMapMatchLine toPolicyMapMatchLine() {
      // TODO Auto-generated method stub
      return null;
   }

}
