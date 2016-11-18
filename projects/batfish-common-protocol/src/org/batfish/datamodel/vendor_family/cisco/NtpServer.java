package org.batfish.datamodel.vendor_family.cisco;

import java.io.Serializable;

public class NtpServer implements Serializable {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private String _vrf;

   public String getVrf() {
      return _vrf;
   }

   public void setVrf(String vrf) {
      _vrf = vrf;
   }

}
