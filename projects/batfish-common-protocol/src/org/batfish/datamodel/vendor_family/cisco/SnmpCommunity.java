package org.batfish.datamodel.vendor_family.cisco;

import org.batfish.common.util.ComparableStructure;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SnmpCommunity extends ComparableStructure<String> {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private String _accessList;

   private boolean _ro;

   private boolean _rw;

   public SnmpCommunity(@JsonProperty(NAME_VAR) String name) {
      super(name);
   }

   public String getAccessList() {
      return _accessList;
   }

   public boolean getRo() {
      return _ro;
   }

   public boolean getRw() {
      return _rw;
   }

   public void setAccessList(String accessList) {
      _accessList = accessList;
   }

   public void setRo(boolean ro) {
      _ro = ro;
   }

   public void setRw(boolean rw) {
      _rw = rw;
   }

}
