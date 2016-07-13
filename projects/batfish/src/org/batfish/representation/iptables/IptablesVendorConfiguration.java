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

   @Override
   public String getHostname() {
      throw new UnsupportedOperationException(
            "no implementation for generated method"); // TODO Auto-generated
                                                       // method stub
   }

   @Override
   public RoleSet getRoles() {
      throw new UnsupportedOperationException(
            "no implementation for generated method"); // TODO Auto-generated
                                                       // method stub
   }

   @Override
   public Set<String> getUnimplementedFeatures() {
      throw new UnsupportedOperationException(
            "no implementation for generated method"); // TODO Auto-generated
                                                       // method stub
   }

   @Override
   public Warnings getWarnings() {
      throw new UnsupportedOperationException(
            "no implementation for generated method"); // TODO Auto-generated
                                                       // method stub
   }

   @Override
   public void setHostname(String hostname) {
      throw new UnsupportedOperationException(
            "no implementation for generated method"); // TODO Auto-generated
                                                       // method stub
   }

   @Override
   public void setRoles(RoleSet roles) {
      throw new UnsupportedOperationException(
            "no implementation for generated method"); // TODO Auto-generated
                                                       // method stub
   }

   @Override
   public void setVendor(ConfigurationFormat format) {
      throw new UnsupportedOperationException(
            "no implementation for generated method"); // TODO Auto-generated
                                                       // method stub
   }

   @Override
   public Configuration toVendorIndependentConfiguration(Warnings warnings)
         throws VendorConversionException {
      throw new UnsupportedOperationException(
            "no implementation for generated method"); // TODO Auto-generated
                                                       // method stub
   }
}
