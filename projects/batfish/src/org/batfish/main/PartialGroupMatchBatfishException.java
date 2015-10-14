package org.batfish.main;

import org.batfish.common.BatfishException;

public class PartialGroupMatchBatfishException extends BatfishException {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   public PartialGroupMatchBatfishException(String msg) {
      super(msg);
   }

   public PartialGroupMatchBatfishException(String msg, Throwable cause) {
      super(msg, cause);
   }

}
