package org.batfish.datamodel.vendor_family.cisco;

import org.batfish.common.util.ComparableStructure;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SntpServer extends ComparableStructure<String> {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   public Integer _version;

   public SntpServer(@JsonProperty(NAME_VAR) String hostname) {
      super(hostname);
   }

   public Integer getVersion() {
      return _version;
   }

   public void setVersion(Integer version) {
      _version = version;
   }

}
