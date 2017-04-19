package org.batfish.representation.juniper;

import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.IpProtocol;
import org.batfish.common.Warnings;

public final class FwFromProtocol extends FwFrom {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private final IpProtocol _protocol;

   public FwFromProtocol(IpProtocol protocol) {
      _protocol = protocol;
   }

   @Override
   public void applyTo(IpAccessListLine line, JuniperConfiguration jc,
         Warnings w, Configuration c) {
      line.getIpProtocols().add(_protocol);
   }

   public IpProtocol getProtocol() {
      return _protocol;
   }

}
