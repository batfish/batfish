package org.batfish.representation.juniper;

import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.Prefix;
import org.batfish.common.Warnings;

public final class FwFromDestinationAddressExcept extends FwFrom {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private final Prefix _prefix;

   public FwFromDestinationAddressExcept(Prefix prefix) {
      _prefix = prefix;
   }

   @Override
   public void applyTo(IpAccessListLine line, JuniperConfiguration jc,
         Warnings w, Configuration c) {
      line.getNotDstIps().add(new IpWildcard(_prefix));
   }

   public Prefix getPrefix() {
      return _prefix;
   }

}
