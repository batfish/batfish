package org.batfish.representation;

import java.io.Serializable;
import java.util.Set;

import org.batfish.common.VendorConversionException;
import org.batfish.datamodel.collections.RoleSet;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.GenericConfigObject;
import org.batfish.main.Warnings;

public interface VendorConfiguration extends Serializable, GenericConfigObject {

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
