package batfish.representation.juniper;

import batfish.representation.PolicyMapMatchLine;
import batfish.representation.Prefix;

public final class FromRouteFilterThrough extends FromRouteFilter {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private final Prefix _throughPrefix;

   public FromRouteFilterThrough(Prefix prefix, Prefix throughPrefix) {
      super(prefix);
      _throughPrefix = prefix;
   }

   public Prefix getThroughPrefix() {
      return _throughPrefix;
   }

   @Override
   public PolicyMapMatchLine toPolicyMapMatchLine() {
      // TODO Auto-generated method stub
      return null;
   }

}
