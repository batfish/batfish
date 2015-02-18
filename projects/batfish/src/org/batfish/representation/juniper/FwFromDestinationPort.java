package org.batfish.representation.juniper;

import org.batfish.representation.IpAccessListLine;
import org.batfish.util.SubRange;

public final class FwFromDestinationPort extends FwFrom {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private final SubRange _portRange;

   public FwFromDestinationPort(int port) {
      _portRange = new SubRange(port, port);
   }

   public FwFromDestinationPort(SubRange subrange) {
      _portRange = subrange;
   }

   @Override
   public void applyTo(IpAccessListLine line) {
      line.getDstPortRanges().add(_portRange);
   }

   public SubRange getPortRange() {
      return _portRange;
   }

}
