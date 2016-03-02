package org.batfish.main;

public enum ConfigurationFormat {
   ARISTA("arista"),
   CISCO("cisco"),
   CISCO_IOS_XR("cisco"),
   EMPTY("empty"),
   FLAT_JUNIPER("juniper"),
   FLAT_VYOS("vyos"),
   JUNIPER("juniper"),
   JUNIPER_SWITCH("juniper"),
   MRV("mrv"),
   UNKNOWN("unknown"),
   VXWORKS("vxworks"),
   VYOS("vyos");

   private String _vendorString;

   private ConfigurationFormat(String vendorString) {
      _vendorString = vendorString;
   }

   public String getVendorString() {
      return _vendorString;
   }
}
