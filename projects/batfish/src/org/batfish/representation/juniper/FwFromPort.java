package org.batfish.representation.juniper;

import org.batfish.main.Warnings;
import org.batfish.representation.Configuration;
import org.batfish.representation.IpAccessListLine;
import org.batfish.util.SubRange;

public final class FwFromPort extends FwFrom {

   /**
       *
       */
   private static final long serialVersionUID = 1L;

   private final SubRange _portRange;

   public FwFromPort(int port) {
      _portRange = new SubRange(port, port);
   }

   public FwFromPort(SubRange subrange) {
      _portRange = subrange;
   }

   @Override
   public void applyTo(IpAccessListLine line, JuniperConfiguration jc,
         Warnings w, Configuration c) {
      line.getSrcOrDstPortRanges().add(_portRange);
   }

   public SubRange getPortRange() {
      return _portRange;
   }

}
