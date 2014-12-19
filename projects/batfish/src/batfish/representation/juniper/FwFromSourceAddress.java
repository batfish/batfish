package batfish.representation.juniper;

import batfish.representation.Prefix;

public final class FwFromSourceAddress extends FwFrom {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private final Prefix _prefix;

   public FwFromSourceAddress(Prefix prefix) {
      _prefix = prefix;
   }

   public Prefix getPrefix() {
      return _prefix;
   }

}
