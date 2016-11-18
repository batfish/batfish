package org.batfish.datamodel.vendor_family.cisco;

import java.io.Serializable;

public class SntpServer implements Serializable {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   public Integer _version;

   public Integer getVersion() {
      return _version;
   }

   public void setVersion(Integer version) {
      _version = version;
   }

}
