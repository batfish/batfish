package org.batfish.representation.juniper;

import org.batfish.representation.IpAccessListLine;
import org.batfish.representation.Prefix;

public final class FwFromSourceAddress extends FwFrom {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private final Prefix _prefix;

   public FwFromSourceAddress(Prefix prefix) {
      _prefix = prefix;
   }

   @Override
   public void applyTo(IpAccessListLine line) {
      line.getSourceIpRanges().add(_prefix);
   }

   public Prefix getPrefix() {
      return _prefix;
   }

}
