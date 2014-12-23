package batfish.representation.juniper;

import batfish.representation.PolicyMapMatchLine;
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
   public PolicyMapMatchLine toPolicyMapMatchLine() {
      // TODO Auto-generated method stub
      return null;
   }

}
