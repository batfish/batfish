package batfish.representation;

import java.util.List;

public interface VendorConfiguration {
   List<String> getConversionWarnings();
   Configuration toVendorIndependentConfiguration() throws VendorConversionException;
}
