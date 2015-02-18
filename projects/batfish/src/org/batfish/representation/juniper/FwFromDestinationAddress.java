package org.batfish.representation.juniper;

import org.batfish.representation.IpAccessListLine;
import org.batfish.representation.Prefix;

public final class FwFromDestinationAddress extends FwFrom {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private final Prefix _prefix;

   public FwFromDestinationAddress(Prefix prefix) {
      _prefix = prefix;
   }

   @Override
   public void applyTo(IpAccessListLine line) {
      line.getDestinationIpRanges().add(_prefix);
   }

   public Prefix getPrefix() {
      return _prefix;
   }

}
