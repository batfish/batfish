package org.batfish.main;

/**
 * Thrown as a fatal exception. When caught, Batfish should perform any
 * necessary cleanup and terminate gracefully with a non-zero exit status. A
 * BatfishException should always contain a detail message.
 */
public class BatfishException extends RuntimeException {

   private static final long serialVersionUID = 1L;

   /**
    * Constructs a BatfishException with a detail message
    *
    * @param msg
    *           The detail message
    */
   public BatfishException(String msg) {
      super(msg);
   }

   /**
    * Constructs a BatfishException with a detail message and a cause
    *
    * @param msg
    *           The detail message
    * @param cause
    *           The cause of this exception
    */
   public BatfishException(String msg, Throwable cause) {
      super(msg, cause);
   }

}
