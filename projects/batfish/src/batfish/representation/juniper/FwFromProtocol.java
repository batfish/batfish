package batfish.representation.juniper;

public final class FwFromProtocol extends FwFrom {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private final int _protocol;

   public FwFromProtocol(int protocol) {
      _protocol = protocol;
   }

   public int getProtocol() {
      return _protocol;
   }

}
