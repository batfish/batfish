package batfish.representation.juniper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import batfish.collections.RoleSet;
import batfish.representation.Configuration;
import batfish.representation.VendorConfiguration;
import batfish.representation.VendorConversionException;

public class JuniperVendorConfiguration extends JuniperConfiguration implements
      VendorConfiguration {

   public static final String DEFAULT_ROUTING_INSTANCE = "default_routing_engine";

   private static final long serialVersionUID = 1L;

   private static final String VENDOR_NAME = "juniper";

   private List<String> _conversionWarnings;

   private String _hostname;

   private RoleSet _roles;

   private Map<String, JuniperVendorConfiguration> _routingInstances;

   public JuniperVendorConfiguration() {
      _conversionWarnings = new ArrayList<String>();
      _routingInstances = new HashMap<String, JuniperVendorConfiguration>();
      _routingInstances.put(DEFAULT_ROUTING_INSTANCE, this);
   }

   @Override
   public List<String> getConversionWarnings() {
      return _conversionWarnings;
   }

   @Override
   public String getHostname() {
      return _hostname;
   }

   public Map<String, JuniperVendorConfiguration> getRoutingInstances() {
      return _routingInstances;
   }

   public void setHostname(String hostname) {
      _hostname = hostname;
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
