package org.batfish.representation.juniper;

import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.SubRange;
import org.batfish.common.Warnings;

public class FwFromIcmpCode extends FwFrom {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private SubRange _icmpCodeRange;

   public FwFromIcmpCode(SubRange icmpCodeRange) {
      _icmpCodeRange = icmpCodeRange;
   }

   @Override
   public void applyTo(IpAccessListLine line, JuniperConfiguration jc,
         Warnings w, Configuration c) {
      line.getIcmpCodes().add(_icmpCodeRange);
   }

}
