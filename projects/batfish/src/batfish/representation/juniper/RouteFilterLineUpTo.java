package batfish.representation.juniper;

import batfish.representation.Prefix;
import batfish.representation.RouteFilterList;

public final class RouteFilterLineUpTo extends RouteFilterLine {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private final int _maxPrefixLength;

   public RouteFilterLineUpTo(Prefix prefix, int maxPrefixLength) {
      super(prefix);
      _maxPrefixLength = maxPrefixLength;
   }

   @Override
   public void applyTo(RouteFilterList rfl) {
      throw new UnsupportedOperationException(
            "no implementation for generated method"); // TODO Auto-generated
                                                       // method stub
   }

   public int getMaxPrefixLength() {
      return _maxPrefixLength;
   }

}
