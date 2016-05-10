package org.batfish.representation.juniper;

import org.batfish.datamodel.IpProtocol;
import org.batfish.main.Warnings;
import org.batfish.representation.Configuration;
import org.batfish.representation.IpAccessListLine;

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
      line.getProtocols().add(_protocol);
   }

   public IpProtocol getProtocol() {
      return _protocol;
   }

}
