package batfish.representation.juniper;

import java.util.ArrayList;
import java.util.List;

import batfish.collections.RoleSet;
import batfish.representation.Configuration;
import batfish.representation.VendorConfiguration;
import batfish.representation.VendorConversionException;

public final class JuniperVendorConfiguration extends JuniperConfiguration
      implements VendorConfiguration {

   private static final long serialVersionUID = 1L;

   private static final String VENDOR_NAME = "juniper";

   private final List<String> _conversionWarnings;

   private final RoleSet _roles;

   public JuniperVendorConfiguration() {
      _conversionWarnings = new ArrayList<String>();
      _roles = new RoleSet();
   }

   @Override
   public List<String> getConversionWarnings() {
      return _conversionWarnings;
   }

   @Override
   public String getHostname() {
      return _defaultRoutingInstance.getHostname();
   }

   @Override
   public RoleSet getRoles() {
      return _roles;
   }

   @Override
   public void setRoles(RoleSet roles) {
      _roles.addAll(roles);
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
