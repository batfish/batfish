package batfish.representation;

import java.io.Serializable;
import java.util.List;

public interface VendorConfiguration extends Serializable {

   List<String> getConversionWarnings();

   String getHostname();

   Configuration toVendorIndependentConfiguration()
         throws VendorConversionException;

}
