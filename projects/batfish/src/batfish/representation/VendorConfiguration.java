package batfish.representation;

import java.io.Serializable;
import java.util.List;

import batfish.collections.RoleSet;

public interface VendorConfiguration extends Serializable {

   List<String> getConversionWarnings();

   String getHostname();

   RoleSet getRoles();

   void setRoles(RoleSet roles);

   Configuration toVendorIndependentConfiguration()
         throws VendorConversionException;

}
