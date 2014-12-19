package batfish.representation.juniper;

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

}
