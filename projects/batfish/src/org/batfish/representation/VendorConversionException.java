package org.batfish.representation;

import org.batfish.main.BatfishException;

public class VendorConversionException extends BatfishException {

   private static final long serialVersionUID = 1L;

   public VendorConversionException(String msg) {
      super(msg);
   }

   public VendorConversionException(String msg, Throwable cause) {
      super(msg, cause);
   }

}
