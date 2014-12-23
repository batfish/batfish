package batfish.representation.juniper;

import batfish.representation.IpAccessListLine;
import batfish.util.SubRange;

public final class FwFromDestinationPort extends FwFrom {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private final int _port;

   public FwFromDestinationPort(int port) {
      _port = port;
   }

   @Override
   public void applyTo(IpAccessListLine line) {
      line.getDstPortRanges().add(new SubRange(_port, _port));
   }

   public int getPort() {
      return _port;
   }

}
