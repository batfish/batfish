package org.batfish.representation.juniper;

import java.util.List;

import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.SubRange;
import org.batfish.common.Warnings;

public class FwFromPacketLength extends FwFrom {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private boolean _except;

   private final List<SubRange> _range;

   public FwFromPacketLength(List<SubRange> range, boolean except) {
      _range = range;
      _except = except;
   }

   @Override
   public void applyTo(IpAccessListLine line, JuniperConfiguration jc,
         Warnings w, Configuration c) {
      if (_except) {
         line.getNotPacketLengths().addAll(_range);
      }
      else {
         line.getPacketLengths().addAll(_range);
      }
   }

}
