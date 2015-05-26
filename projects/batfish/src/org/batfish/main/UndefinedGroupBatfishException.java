package org.batfish.main;

public class UndefinedGroupBatfishException extends BatfishException {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   public UndefinedGroupBatfishException(String msg) {
      super(msg);
   }

   public UndefinedGroupBatfishException(String msg, Throwable cause) {
      super(msg, cause);
   }

}
