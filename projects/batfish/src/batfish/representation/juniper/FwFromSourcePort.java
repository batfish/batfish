package batfish.representation.juniper;

import batfish.representation.IpAccessListLine;
import batfish.util.SubRange;

public final class FwFromSourcePort extends FwFrom {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private final int _port;

   public FwFromSourcePort(int port) {
      _port = port;
   }

   public int getPort() {
      return _port;
   }

   @Override
   public void applyTo(IpAccessListLine line) {
      line.getSrcPortRanges().add(new SubRange(_port, _port));
   }

}
