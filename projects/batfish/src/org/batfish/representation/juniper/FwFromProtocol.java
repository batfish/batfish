package org.batfish.representation.juniper;

import org.batfish.representation.IpAccessListLine;
import org.batfish.representation.IpProtocol;

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
   public void applyTo(IpAccessListLine line) {
      line.getProtocols().add(_protocol);
   }

   public IpProtocol getProtocol() {
      return _protocol;
   }

}
