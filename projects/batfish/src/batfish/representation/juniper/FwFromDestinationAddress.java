package batfish.representation.juniper;

import batfish.representation.Prefix;

public final class FwFromDestinationAddress extends FwFrom {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private final Prefix _prefix;

   public FwFromDestinationAddress(Prefix prefix) {
      _prefix = prefix;
   }

   public Prefix getPrefix() {
      return _prefix;
   }

}
