package org.batfish.representation.juniper;

import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.SubRange;
import org.batfish.common.Warnings;

public class FwFromIcmpType extends FwFrom {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private SubRange _icmpTypeRange;

   public FwFromIcmpType(SubRange icmpTypeRange) {
      _icmpTypeRange = icmpTypeRange;
   }

   @Override
   public void applyTo(IpAccessListLine line, JuniperConfiguration jc,
         Warnings w, Configuration c) {
      line.getIcmpTypes().add(_icmpTypeRange);
   }

}
