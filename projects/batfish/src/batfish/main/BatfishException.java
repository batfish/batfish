package batfish.main;

public class BatfishException extends RuntimeException {

   private static final long serialVersionUID = 1L;

   public BatfishException(String msg) {
      super(msg);
   }

   public BatfishException(String msg, Exception e) {
      super(msg, e);
   }

}
