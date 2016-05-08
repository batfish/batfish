package org.batfish.representation.juniper;

import org.batfish.common.util.SubRange;
import org.batfish.main.Warnings;
import org.batfish.representation.Configuration;
import org.batfish.representation.IpAccessListLine;

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
   public void applyTo(IpAccessListLine line, JuniperConfiguration jc,
         Warnings w, Configuration c) {
      line.getDstPortRanges().add(_portRange);
   }

   public SubRange getPortRange() {
      return _portRange;
   }

}
