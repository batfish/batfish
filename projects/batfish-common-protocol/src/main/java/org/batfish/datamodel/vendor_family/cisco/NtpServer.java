package org.batfish.datamodel.vendor_family.cisco;

import org.batfish.common.util.ComparableStructure;

import com.fasterxml.jackson.annotation.JsonProperty;

public class NtpServer extends ComparableStructure<String> {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private String _vrf;

   public NtpServer(@JsonProperty(NAME_VAR) String hostname) {
      super(hostname);
   }

   public String getVrf() {
      return _vrf;
   }

   public void setVrf(String vrf) {
      _vrf = vrf;
   }

}
