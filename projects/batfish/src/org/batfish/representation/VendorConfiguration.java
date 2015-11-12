package org.batfish.representation;

import java.io.Serializable;
import java.util.Set;

import org.batfish.collections.RoleSet;
import org.batfish.main.ConfigurationFormat;
import org.batfish.main.Warnings;

public interface VendorConfiguration extends Serializable {

   String getHostname();

   RoleSet getRoles();

   Set<String> getUnimplementedFeatures();

   Warnings getWarnings();

   void setHostname(String hostname);

   void setRoles(RoleSet roles);

   void setVendor(ConfigurationFormat format);

   Configuration toVendorIndependentConfiguration(Warnings warnings)
         throws VendorConversionException;

}
