package org.batfish.main;

import org.batfish.common.BatfishException;

public class PedanticBatfishException extends BatfishException {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   public PedanticBatfishException(String msg) {
      super(msg);
   }

   public PedanticBatfishException(String msg, Throwable cause) {
      super(msg, cause);
   }

}
