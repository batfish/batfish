package batfish.representation.juniper;

import java.util.ArrayList;
import java.util.List;

import batfish.collections.RoleSet;
import batfish.representation.Configuration;
import batfish.representation.VendorConfiguration;
import batfish.representation.VendorConversionException;

public class JuniperVendorConfiguration extends JuniperConfiguration implements
      VendorConfiguration {

   private static final long serialVersionUID = 1L;

   private static final String VENDOR_NAME = "juniper";

   private List<String> _conversionWarnings;

   private RoleSet _roles;

   public JuniperVendorConfiguration() {
      _conversionWarnings = new ArrayList<String>();
   }

   @Override
   public List<String> getConversionWarnings() {
      return _conversionWarnings;
   }

   @Override
   public String getHostname() {
      return _system.getHostname();
   }

   @Override
   public void setRoles(RoleSet roles) {
      _roles = roles;
   }

   @Override
   public Configuration toVendorIndependentConfiguration()
         throws VendorConversionException {
      String hostname = getHostname();
      Configuration c = new Configuration(hostname);
      c.setVendor(VENDOR_NAME);
      c.setRoles(_roles);
      return c;
   }

}
