package org.batfish.representation.mrv;

import java.util.Set;

import org.batfish.datamodel.collections.RoleSet;
import org.batfish.main.ConfigurationFormat;
import org.batfish.main.Warnings;
import org.batfish.representation.Configuration;
import org.batfish.representation.VendorConfiguration;
import org.batfish.representation.VendorConversionException;

public class MrvVendorConfiguration extends MrvConfiguration implements
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
