package org.batfish.representation.juniper;

import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.Prefix;
import org.batfish.common.Warnings;

public final class FwFromSourceAddressExcept extends FwFrom {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private final Prefix _prefix;

   public FwFromSourceAddressExcept(Prefix prefix) {
      _prefix = prefix;
   }

   @Override
   public void applyTo(IpAccessListLine line, JuniperConfiguration jc,
         Warnings w, Configuration c) {
      IpWildcard wildcard = new IpWildcard(_prefix);
      line.getNotSrcIps().add(wildcard);
   }

   public Prefix getPrefix() {
      return _prefix;
   }

}
