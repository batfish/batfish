package org.batfish.datamodel.vendor_family;

import java.io.Serializable;

import org.batfish.datamodel.vendor_family.cisco.CiscoFamily;

public class VendorFamily implements Serializable {

   /**
    * 
    */
   private static final long serialVersionUID = 1L;
   private CiscoFamily _cisco;

   public CiscoFamily getCisco() {
      return _cisco;
   }

   public void setCisco(CiscoFamily cisco) {
      _cisco = cisco;
   }
   
}
