package org.batfish.representation.juniper;

import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.SubRange;
import org.batfish.common.Warnings;

public final class FwFromSourcePort extends FwFrom {

   /**
       *
       */
   private static final long serialVersionUID = 1L;

   private final SubRange _portRange;

   public FwFromSourcePort(int port) {
      _portRange = new SubRange(port, port);
   }

   public FwFromSourcePort(SubRange subrange) {
      _portRange = subrange;
   }

   @Override
   public void applyTo(IpAccessListLine line, JuniperConfiguration jc,
         Warnings w, Configuration c) {
      line.getSrcPorts().add(_portRange);
   }

   public SubRange getPortRange() {
      return _portRange;
   }

}
