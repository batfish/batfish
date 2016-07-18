package org.batfish.representation.iptables;

import java.util.Set;

import org.batfish.common.VendorConversionException;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.collections.RoleSet;
import org.batfish.main.Warnings;
import org.batfish.representation.VendorConfiguration;

public class IptablesVendorConfiguration extends IptablesConfiguration implements
      VendorConfiguration {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private transient Set<String> _unimplementedFeatures;

   private ConfigurationFormat _vendor;

   private String _hostname;

   private transient Warnings _w;

   @Override
   public String getHostname() {
      return _hostname;
   }

   @Override
   public RoleSet getRoles() {
      return _roles;
   }

   @Override
   public Set<String> getUnimplementedFeatures() {
      return _unimplementedFeatures;
   }

   @Override
   public Warnings getWarnings() {
      return _w;
   }

   @Override
   public void setHostname(String hostname) {
      _hostname = hostname;
   }

   @Override
   public void setRoles(RoleSet roles) {
      _roles.addAll(roles);
   }

   @Override
   public void setVendor(ConfigurationFormat format) {
      _vendor = format;
   }

   @Override
   public Configuration toVendorIndependentConfiguration(Warnings warnings)
         throws VendorConversionException {
      throw new UnsupportedOperationException(
            "no implementation for generated method"); // TODO Auto-generated
                                                       // method stub
   }
}
