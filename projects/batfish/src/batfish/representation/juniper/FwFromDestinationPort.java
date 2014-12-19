package batfish.representation.juniper;

public final class FwFromDestinationPort extends FwFrom {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private final int _port;

   public FwFromDestinationPort(int port) {
      _port = port;
   }

   public int getPort() {
      return _port;
   }

}
